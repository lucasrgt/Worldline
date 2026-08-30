import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/** Creates one isolated, hash-pinned RetroMCP loader client and compiles the profiler. */
final class LegacyLoaderWorkspace {
    private LegacyLoaderWorkspace() {}

    static Prepared prepare(Path repository, Path base, Path artifacts, Path java8Home,
            String loader, LegacyProfilerQualificationConfig config) throws Exception {
        repository = repository.toAbsolutePath().normalize();
        base = base.toAbsolutePath().normalize(); artifacts = artifacts.toAbsolutePath().normalize();
        require("modloader".equals(loader) || "forge".equals(loader), "unknown legacy loader");
        Path java = java8(java8Home);
        require(Files.isRegularFile(base.resolve("RetroMCP-CLI.jar"))
                && Files.isRegularFile(base.resolve("options.cfg")), "invalid base RetroMCP workspace");
        require(digest(base.resolve("jars/minecraft.jar")).equals(config.clientHash()),
                "official b1.7.3 client hash drifted");
        Path modLoader = artifacts.resolve(config.modLoaderFile()).normalize();
        require(modLoader.startsWith(artifacts) && Files.isRegularFile(modLoader)
                && digest(modLoader).equals(config.modLoaderHash()), "ModLoader artifact drifted");
        Path forge = config.artifact(artifacts, "forge").normalize();
        if ("forge".equals(loader)) require(forge.startsWith(artifacts) && Files.isRegularFile(forge)
                && digest(forge).equals(config.forgeHash()), "Forge artifact drifted");

        Path workspaces = repository.resolve(".worldline/runtime/legacy-profiler/workspaces");
        Path workspace = workspaces.resolve(loader).normalize();
        require(workspace.startsWith(workspaces) && !workspace.equals(workspaces),
                "unsafe legacy qualification workspace");
        SafeTreeDelete.delete(workspace); copyTree(base, workspace);
        patchOptions(workspace.resolve("options.cfg"), workspace);
        overlayJar(base.resolve("jars/minecraft.jar"), modLoader,
                "forge".equals(loader) ? forge : null, workspace.resolve("jars/minecraft.jar"));

        Path logs = repository.resolve(".worldline/reports/legacy-profiler");
        Files.createDirectories(logs);
        runStage(workspace, logs.resolve(loader + "-decompile.log"), config.timeoutSeconds() * 3,
                List.of(javaTool("java"), "-jar", "RetroMCP-CLI.jar", "decompile", "client"));
        Path source = workspace.resolve("minecraft/src");
        require(Files.isRegularFile(source.resolve("net/minecraft/src/ModLoader.java")),
                "ModLoader did not decompile");
        if ("forge".equals(loader)) require(Files.readString(source.resolve("forge/ForgeHooks.java"))
                .contains("revisionVersion = 6"), "Forge 1.0.6 source proof is absent");
        LegacyProfilerInstaller.execute(repository, workspace, loader, true);
        Path probe = repository.resolve("adapters/modloader-forge/qualification-src")
                .resolve(loader).resolve("net/minecraft/src/mod_WorldlineProfilerProbe.java");
        Files.copy(probe, source.resolve("net/minecraft/src/mod_WorldlineProfilerProbe.java"),
                StandardCopyOption.REPLACE_EXISTING);
        runStage(workspace, logs.resolve(loader + "-recompile.log"), config.timeoutSeconds() * 2,
                List.of(javaTool("java"), "-jar", "RetroMCP-CLI.jar", "recompile"));
        Path probeClass = workspace.resolve(
                "minecraft/bin/net/minecraft/src/mod_WorldlineProfilerProbe.class");
        require(classMajor(probeClass) == 52, "legacy probe is not Java 8 bytecode");
        Path modClass = workspace.resolve(
                "minecraft/game/mods/worldline/net/minecraft/src/mod_WorldlineProfilerProbe.class");
        Files.createDirectories(modClass.getParent());
        Files.copy(probeClass, modClass, StandardCopyOption.REPLACE_EXISTING);
        return new Prepared(workspace, java, logs.resolve(loader + "-client.log"),
                workspace.resolve(".worldline-profiler/runtime/" + loader + ".wlpr"));
    }

    static void patchOptions(Path path, Path workspace) throws Exception {
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        Map<String, String> replacements = new LinkedHashMap<>();
        replacements.put("workingDir", workspace.toAbsolutePath().normalize().toString() + "\\.");
        replacements.put("stripgenerics", "true"); replacements.put("source", "8");
        replacements.put("target", "8");
        for (Map.Entry<String, String> replacement : replacements.entrySet()) {
            int found = 0;
            for (int index = 0; index < lines.size(); index++)
                if (lines.get(index).startsWith(replacement.getKey() + "=")) {
                    lines.set(index, replacement.getKey() + "=" + replacement.getValue()); found++;
                }
            require(found == 1, "RetroMCP option drifted: " + replacement.getKey());
        }
        Files.write(path, lines, StandardCharsets.UTF_8);
    }

    static void overlayJar(Path vanilla, Path modLoader, Path forge, Path output) throws Exception {
        Map<String, byte[]> entries = new LinkedHashMap<>();
        readZip(vanilla, entries, false); readZip(modLoader, entries, true);
        if (forge != null) readZip(forge, entries, true);
        require(entries.containsKey("ModLoader.class"), "patched client lacks ModLoader");
        require(forge == null || entries.containsKey("forge/ForgeHooks.class"),
                "patched client lacks Forge");
        Path temporary = output.resolveSibling(output.getFileName() + ".worldline.tmp");
        try (OutputStream bytes = Files.newOutputStream(temporary);
                ZipOutputStream zip = new ZipOutputStream(bytes)) {
            for (Map.Entry<String, byte[]> entry : entries.entrySet()) {
                zip.putNextEntry(new ZipEntry(entry.getKey())); zip.write(entry.getValue());
                zip.closeEntry();
            }
        }
        Files.move(temporary, output, StandardCopyOption.REPLACE_EXISTING);
    }

    private static void readZip(Path source, Map<String, byte[]> target, boolean overlay)
            throws Exception {
        try (InputStream bytes = Files.newInputStream(source); ZipInputStream zip = new ZipInputStream(bytes)) {
            for (ZipEntry entry; (entry = zip.getNextEntry()) != null;) {
                String name = entry.getName().replace('\\', '/');
                if (entry.isDirectory() || name.toUpperCase().startsWith("META-INF/")) continue;
                require(!name.startsWith("/") && !name.contains("../"), "unsafe loader ZIP entry");
                if (!overlay) require(target.put(name, zip.readAllBytes()) == null,
                        "duplicate vanilla JAR entry " + name);
                else target.put(name, zip.readAllBytes());
            }
        }
    }

    private static void copyTree(Path source, Path target) throws Exception {
        try (Stream<Path> paths = Files.walk(source)) {
            for (Path path : paths.sorted(Comparator.naturalOrder()).toList()) {
                require(!SafeTreeDelete.linkLike(path), "base workspace contains a link: " + path);
                Path destination = target.resolve(source.relativize(path)).normalize();
                require(destination.startsWith(target), "unsafe copied workspace path");
                if (Files.isDirectory(path)) Files.createDirectories(destination);
                else Files.copy(path, destination, StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    private static void runStage(Path workspace, Path log, int seconds, List<String> command)
            throws Exception {
        Files.createDirectories(log.getParent());
        Process process = new ProcessBuilder(command).directory(workspace.toFile())
                .redirectErrorStream(true).redirectOutput(log.toFile()).start();
        require(process.waitFor(seconds, TimeUnit.SECONDS), "RetroMCP stage timed out: " + command);
        String output = Files.readString(log, StandardCharsets.ISO_8859_1);
        require(process.exitValue() == 0 && !output.contains("[ERROR]")
                && (output.contains("[INFO]: Finished successfully!")
                        || output.contains("[WARNING]: Finished with warnings!")),
                "RetroMCP stage failed; see " + log);
    }

    private static Path java8(Path home) throws Exception {
        Path executable = home.toAbsolutePath().normalize().resolve("bin")
                .resolve(System.getProperty("os.name").startsWith("Windows") ? "java.exe" : "java");
        require(Files.isRegularFile(executable), "Java 8 executable is absent: " + executable);
        Process process = new ProcessBuilder(executable.toString(), "-version")
                .redirectErrorStream(true).start();
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        require(process.waitFor(30, TimeUnit.SECONDS) && process.exitValue() == 0
                && output.contains("version \"1.8."), "qualification requires a Java 8 runtime");
        return executable;
    }

    private static int classMajor(Path path) throws Exception {
        byte[] value = Files.readAllBytes(path);
        require(value.length >= 8 && value[0] == (byte) 0xca && value[1] == (byte) 0xfe,
                "invalid compiled probe class");
        return (value[6] & 255) << 8 | value[7] & 255;
    }

    static String digest(Path path) throws Exception { return HexFormat.of().formatHex(
            MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(path))); }
    private static String javaTool(String name) {
        return Path.of(System.getProperty("java.home"), "bin", name
                + (System.getProperty("os.name").startsWith("Windows") ? ".exe" : "")).toString();
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
    record Prepared(Path workspace, Path java, Path log, Path artifact) {}
}
