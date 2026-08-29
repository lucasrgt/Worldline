import java.io.Reader;
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
import java.util.Properties;
import java.util.Set;
import java.util.HashSet;

/** Installs the shared profiler and exact hooks into a mapped RetroMCP b1.7.3 client. */
public final class LegacyProfilerInstaller {
    private static final String MANIFEST = "adapters/modloader-forge/runtime-sources.properties";
    private LegacyProfilerInstaller() { }

    public static void main(String[] arguments) {
        try {
            if (List.of(arguments).equals(List.of("--self-test"))) {
                LegacyProfilerInstallerSelfTest.execute(repository()); return;
            }
            require(arguments.length == 3 && ("--check".equals(arguments[0])
                    || "--install".equals(arguments[0])), usage());
            execute(repository(), Path.of(arguments[1]), arguments[2], "--install".equals(arguments[0]));
        } catch (Exception error) {
            System.err.println("legacy profiler installer failed: " + error.getMessage());
            System.exit(1);
        }
    }

    static Result execute(Path repository, Path workspace, String loader, boolean install)
            throws Exception {
        repository = repository.toAbsolutePath().normalize();
        workspace = workspace.toAbsolutePath().normalize();
        require("modloader".equals(loader) || "forge".equals(loader),
                "loader must be modloader or forge");
        Path sourceRoot = sourceRoot(workspace);
        Map<String, String> current = readMapped(sourceRoot);
        boolean instrumented = LegacyProfilerSourceTransform.instrumented(current);
        Path state = workspace.resolve(".worldline-profiler");
        Map<Path, Path> runtime = runtimeSources(repository, sourceRoot);
        if (instrumented) {
            LegacyProfilerSourceTransform.validateInstalled(current);
            Map<String, String> baseline = readMapped(state.resolve("backup-v1"));
            require(current.equals(LegacyProfilerSourceTransform.transform(baseline)),
                    "installed mapped profiler sources drifted");
            validateRuntime(runtime); validateReceipt(state.resolve("install.properties"),
                    repository, workspace, sourceRoot, loader, current, runtime);
            System.out.println("WORLDLINE_LEGACY_PROFILER=" + (install ? "UNCHANGED" : "INSTALLED")
                    + " loader=" + loader + " source=" + sourceRoot);
            return new Result(sourceRoot, true, false);
        }
        Map<String, String> transformed = LegacyProfilerSourceTransform.transform(current);
        validateRuntimeTargets(runtime); require(!Files.exists(state),
                "unmanaged profiler state directory already exists: " + state);
        if (!install) {
            System.out.println("WORLDLINE_LEGACY_PROFILER=READY loader=" + loader
                    + " source=" + sourceRoot + " hooks=6 runtimeSources=" + runtime.size());
            return new Result(sourceRoot, false, false);
        }
        Path backup = state.resolve("backup-v1");
        backup(current, backup); writeMapped(sourceRoot, transformed);
        for (Map.Entry<Path, Path> entry : runtime.entrySet())
            write(entry.getValue(), Files.readString(entry.getKey(), StandardCharsets.UTF_8));
        writeReceipt(state.resolve("install.properties"), repository, workspace, sourceRoot,
                loader, transformed, runtime);
        System.out.println("WORLDLINE_LEGACY_PROFILER=INSTALLED loader=" + loader
                + " source=" + sourceRoot + " hooks=6 runtimeSources=" + runtime.size()
                + " backup=" + backup);
        return new Result(sourceRoot, true, true);
    }

    private static Path repository() {
        for (Path path = Path.of("").toAbsolutePath().normalize(); path != null;
                path = path.getParent())
            if (Files.isRegularFile(path.resolve(MANIFEST))) return path;
        throw new IllegalStateException("run the installer from the Worldline repository");
    }

    private static Path sourceRoot(Path workspace) {
        List<Path> candidates = List.of(workspace.resolve("minecraft/src"), workspace.resolve("src"));
        for (Path candidate : candidates)
            if (Files.isRegularFile(candidate.resolve(LegacyProfilerSourceTransform.MINECRAFT)))
                return candidate.toAbsolutePath().normalize();
        throw new IllegalStateException("mapped b1.7.3 client sources not found under " + workspace);
    }

    private static Map<String, String> readMapped(Path sourceRoot) throws Exception {
        Map<String, String> result = new LinkedHashMap<>();
        for (String relative : LegacyProfilerSourceTransform.FILES) {
            Path path = sourceRoot.resolve(relative).normalize();
            require(path.startsWith(sourceRoot) && Files.isRegularFile(path),
                    "missing mapped source " + path);
            result.put(relative, Files.readString(path, StandardCharsets.UTF_8));
        }
        return result;
    }

    private static Map<Path, Path> runtimeSources(Path repository, Path sourceRoot) throws Exception {
        Properties values = new Properties();
        try (Reader reader = Files.newBufferedReader(repository.resolve(MANIFEST),
                StandardCharsets.UTF_8)) { values.load(reader); }
        require("worldline.legacy-profiler-sources.v1".equals(values.getProperty("schema")),
                "legacy profiler source manifest schema drifted");
        int count = Integer.parseInt(values.getProperty("count", "0"));
        require(count == 15, "legacy profiler runtime source count drifted");
        Map<Path, Path> result = new LinkedHashMap<>();
        Set<Path> targets = new HashSet<>();
        for (int index = 1; index <= count; index++) {
            Path source = repository.resolve(required(values, "source." + index)).normalize();
            Path target = sourceRoot.resolve(required(values, "target." + index)).normalize();
            require(source.startsWith(repository) && Files.isRegularFile(source),
                    "missing profiler runtime source " + source);
            require(target.startsWith(sourceRoot) && targets.add(target)
                    && result.put(source, target) == null,
                    "unsafe or duplicate profiler runtime target " + target);
        }
        return result;
    }

    private static void validateRuntimeTargets(Map<Path, Path> runtime) throws Exception {
        for (Map.Entry<Path, Path> entry : runtime.entrySet())
            if (Files.exists(entry.getValue())) require(Files.isRegularFile(entry.getValue())
                    && Files.mismatch(entry.getKey(), entry.getValue()) == -1L,
                    "conflicting profiler runtime target " + entry.getValue());
    }

    private static void validateRuntime(Map<Path, Path> runtime) throws Exception {
        for (Map.Entry<Path, Path> entry : runtime.entrySet())
            require(Files.isRegularFile(entry.getValue())
                    && Files.mismatch(entry.getKey(), entry.getValue()) == -1L,
                    "installed profiler runtime drifted: " + entry.getValue());
    }

    private static void backup(Map<String, String> sources, Path backup) throws Exception {
        for (Map.Entry<String, String> entry : sources.entrySet()) {
            Path destination = backup.resolve(entry.getKey()).normalize();
            require(destination.startsWith(backup) && !Files.exists(destination),
                    "profiler backup already exists: " + destination);
            write(destination, entry.getValue());
        }
    }

    private static void writeMapped(Path sourceRoot, Map<String, String> sources) throws Exception {
        for (Map.Entry<String, String> entry : sources.entrySet())
            write(sourceRoot.resolve(entry.getKey()), entry.getValue());
    }

    private static void writeReceipt(Path receipt, Path repository, Path workspace, Path sourceRoot,
            String loader, Map<String, String> transformed, Map<Path, Path> runtime) throws Exception {
        List<String> lines = new ArrayList<>();
        lines.add("schema=worldline.legacy-profiler-install.v1"); lines.add("loader=" + loader);
        lines.add("workspace=" + workspace.toString().replace('\\', '/'));
        lines.add("source.root=" + workspace.relativize(sourceRoot).toString().replace('\\', '/'));
        lines.add("runtime.manifest.sha256=" + digest(repository.resolve(MANIFEST)));
        for (Map.Entry<String, String> entry : transformed.entrySet())
            lines.add("mapped." + key(entry.getKey()) + ".sha256=" + digest(entry.getValue()));
        for (Map.Entry<Path, Path> entry : runtime.entrySet())
            lines.add("runtime." + sourceRoot.relativize(entry.getValue()).toString()
                    .replace('\\', '.').replace('/', '.') + ".sha256=" + digest(entry.getKey()));
        lines.sort(Comparator.naturalOrder());
        write(receipt, "# Worldline legacy profiler installation\n"
                + String.join("\n", lines) + "\n");
    }

    private static void validateReceipt(Path receipt, Path repository, Path workspace,
            Path sourceRoot, String loader, Map<String, String> transformed,
            Map<Path, Path> runtime) throws Exception {
        require(Files.isRegularFile(receipt), "missing managed profiler receipt " + receipt);
        Properties values = new Properties();
        try (Reader reader = Files.newBufferedReader(receipt, StandardCharsets.UTF_8)) {
            values.load(reader);
        }
        require("worldline.legacy-profiler-install.v1".equals(values.getProperty("schema"))
                && loader.equals(values.getProperty("loader"))
                && workspace.toString().replace('\\', '/').equals(values.getProperty("workspace"))
                && digest(repository.resolve(MANIFEST)).equals(
                        values.getProperty("runtime.manifest.sha256")),
                "legacy profiler receipt or loader drifted");
        for (Map.Entry<String, String> entry : transformed.entrySet())
            require(digest(entry.getValue()).equals(values.getProperty(
                    "mapped." + key(entry.getKey()) + ".sha256")),
                    "mapped profiler receipt drifted: " + entry.getKey());
        for (Map.Entry<Path, Path> entry : runtime.entrySet())
            require(digest(entry.getKey()).equals(values.getProperty("runtime."
                    + sourceRoot.relativize(entry.getValue()).toString()
                            .replace('\\', '.').replace('/', '.') + ".sha256")),
                    "runtime profiler receipt drifted: " + entry.getValue());
    }

    private static void write(Path target, String content) throws Exception {
        Files.createDirectories(target.getParent());
        Path temporary = target.resolveSibling(target.getFileName() + ".worldline.tmp");
        Files.writeString(temporary, content, StandardCharsets.UTF_8);
        try { Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE,
                StandardCopyOption.REPLACE_EXISTING); }
        catch (java.nio.file.AtomicMoveNotSupportedException ignored) {
            Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static String required(Properties values, String key) {
        String value = values.getProperty(key);
        require(value != null && !value.isBlank(), "missing legacy source manifest key " + key);
        return value.trim();
    }
    private static String key(String path) { return path.replace('/', '.').replace(".java", ""); }
    private static String digest(Path path) throws Exception { return digest(Files.readAllBytes(path)); }
    private static String digest(String value) throws Exception {
        return digest(value.getBytes(StandardCharsets.UTF_8));
    }
    private static String digest(byte[] value) throws Exception { return HexFormat.of().formatHex(
            MessageDigest.getInstance("SHA-256").digest(value)); }
    private static String usage() {
        return "usage: LegacyProfilerInstaller --check|--install WORKSPACE modloader|forge";
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
    record Result(Path sourceRoot, boolean installed, boolean changed) { }
}
