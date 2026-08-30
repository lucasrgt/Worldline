import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import worldline.profiling.ProfilerArtifacts;
import worldline.profiling.ProfilerRun;

/** Launches a prepared legacy client and seals its loader/profiler runtime proof. */
final class LegacyProfilerQualificationProcess {
    private LegacyProfilerQualificationProcess() {}

    static Proof execute(Path repository, String loader, LegacyLoaderWorkspace.Prepared prepared,
            LegacyProfilerQualificationConfig config) throws Exception {
        Files.createDirectories(prepared.log().getParent());
        Files.deleteIfExists(prepared.artifact());
        List<String> command = command(loader, prepared, config);
        Process process = new ProcessBuilder(command).directory(prepared.workspace().toFile())
                .redirectErrorStream(true).redirectOutput(prepared.log().toFile()).start();
        if (!process.waitFor(config.timeoutSeconds(), TimeUnit.SECONDS)) {
            terminate(process); throw new IllegalStateException(
                    "legacy client timed out; see " + prepared.log());
        }
        String output = Files.readString(prepared.log(), StandardCharsets.ISO_8859_1);
        Proof proof = validate(loader, process.exitValue(), output, prepared.artifact(), config);
        Path receipt = repository.resolve(".worldline/reports/legacy-profiler")
                .resolve(loader + "-qualification.properties");
        writeReceipt(receipt, loader, prepared, config, proof);
        System.out.println("WORLDLINE_LEGACY_QUALIFICATION=PASS loader=" + loader
                + " frames=" + proof.frames() + " metrics=" + proof.metrics()
                + " receipt=" + receipt);
        return proof;
    }

    static Proof validate(String loader, int exit, String output, Path artifact,
            LegacyProfilerQualificationConfig config) throws Exception {
        String boot = "WORLDLINE_LEGACY_LOADER_BOOT=" + loader
                + " version=" + config.loaderVersion(loader);
        String shutdown = "WORLDLINE_LEGACY_LOADER_SHUTDOWN=" + loader;
        require(exit == 0, "legacy client exited " + exit);
        require(occurrences(output, boot) == 1, "loader boot proof is absent or ambiguous");
        require(occurrences(output, shutdown) == 1, "clean loader shutdown proof is absent");
        require(!output.contains("Failed to load mod") && !output.contains("ExceptionInInitializerError"),
                "legacy loader reported an initialization failure");
        require(Files.isRegularFile(artifact), "profiler artifact was not produced");
        ProfilerRun run = ProfilerArtifacts.read(artifact);
        require(run.census().frames() == config.frames(), "profiler frame count drifted");
        require(config.runtimeVersion().equals(run.tag("runtime.version"))
                && "modloader-forge".equals(run.tag("driver.id"))
                && loader.equals(run.tag("loader.id"))
                && "legacy-loader-boot".equals(run.tag("scenario.id")),
                "profiler runtime tags drifted");
        for (String metric : config.requiredMetrics())
            require(run.schema().contains(metric), "missing qualified profiler metric " + metric);
        String artifactLine = "WORLDLINE_PROFILER_ARTIFACT=";
        require(occurrences(output, artifactLine) == 1, "profiler seal proof is absent or ambiguous");
        return new Proof(run.census().frames(), run.schema().size(),
                LegacyLoaderWorkspace.digest(artifact), run.tag("capture.reason"));
    }

    private static List<String> command(String loader, LegacyLoaderWorkspace.Prepared prepared,
            LegacyProfilerQualificationConfig config) throws Exception {
        Path workspace = prepared.workspace();
        List<Path> classpath = new ArrayList<>();
        classpath.add(workspace.resolve("minecraft/bin"));
        classpath.add(workspace.resolve("minecraft/jars/deobfuscated.jar"));
        try (Stream<Path> paths = Files.walk(workspace.resolve("libraries"))) {
            classpath.addAll(paths.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".jar"))
                    .filter(path -> !path.toString().endsWith("-sources.jar"))
                    .sorted().toList());
        }
        String cp = classpath.stream().map(Path::toString)
                .collect(Collectors.joining(System.getProperty("path.separator")));
        List<String> command = new ArrayList<>(List.of(prepared.java().toString(), "-Xms256M",
                "-Xmx512M", "-Djava.library.path=" + workspace.resolve("libraries/natives"),
                "-Dminecraft.launcher.brand=Worldline", "-Dminecraft.launcher.version=qualification-v1",
                "-Dworldline.profiler.enabled=true", "-Dworldline.profiler.loader=" + loader,
                "-Dworldline.profiler.capacity=" + config.frames(),
                "-Dworldline.profiler.scenario=legacy-loader-boot",
                "-Dworldline.profiler.output=" + prepared.artifact(), "-cp", cp,
                "org.mcphackers.launchwrapper.Launch", "--username", "Worldline", "--uuid", "-",
                "--session", "-", "--version", config.runtimeVersion(), "--gameDir",
                workspace.resolve("minecraft/game").toString(), "--assetsDir",
                workspace.resolve("minecraft/game/assets").toString(), "--assetIndex", "b1.7",
                "--accessToken", "-", "--userProperties", "{}", "--userType", "legacy",
                "--versionType", "release", "--skinProxy", "pre-b1.9-pre4"));
        return command;
    }

    private static void writeReceipt(Path receipt, String loader,
            LegacyLoaderWorkspace.Prepared prepared, LegacyProfilerQualificationConfig config,
            Proof proof) throws Exception {
        List<String> lines = new ArrayList<>(List.of(
                "schema=worldline.legacy-profiler-runtime-proof.v1", "loader=" + loader,
                "loader.version=" + config.loaderVersion(loader),
                "runtime.version=" + config.runtimeVersion(), "java.feature=8",
                "process.exit=0", "boot.count=1", "shutdown.count=1",
                "capture.frames=" + proof.frames(), "capture.metrics=" + proof.metrics(),
                "capture.reason=" + proof.reason(), "capture.sha256=" + proof.artifactHash(),
                "client.sha256=" + config.clientHash(),
                "modloader.sha256=" + config.modLoaderHash(),
                "workspace=" + prepared.workspace().toString().replace('\\', '/')));
        if ("forge".equals(loader)) lines.add("forge.sha256=" + config.forgeHash());
        lines.sort(Comparator.naturalOrder()); Files.createDirectories(receipt.getParent());
        Path temporary = receipt.resolveSibling(receipt.getFileName() + ".tmp");
        Files.writeString(temporary, "# Worldline legacy profiler runtime qualification\n"
                + String.join("\n", lines) + "\n", StandardCharsets.UTF_8);
        Files.move(temporary, receipt, StandardCopyOption.REPLACE_EXISTING);
    }

    private static void terminate(Process process) {
        process.descendants().sorted(Comparator.comparingLong(ProcessHandle::pid).reversed())
                .forEach(handle -> { if (handle.isAlive()) handle.destroyForcibly(); });
        if (process.isAlive()) process.destroyForcibly();
    }
    private static int occurrences(String text, String token) {
        int count = 0, offset = 0;
        while ((offset = text.indexOf(token, offset)) >= 0) { count++; offset += token.length(); }
        return count;
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
    record Proof(int frames, int metrics, String artifactHash, String reason) {}
}
