import java.io.IOException;
import java.io.Reader;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Parallel module compiler backed by a cross-worktree immutable content cache. */
final class ModuleBuild {
    private final Path root, build, cache;
    private final Properties config;
    private final List<String> modules;
    private final Map<String, Future<Artifact>> futures = new HashMap<>();

    ModuleBuild(Path root, Path build, Properties config, List<String> modules) {
        this.root = root; this.build = build; this.config = config; this.modules = modules;
        String control = System.getenv("WORLDLINE_GATE_CONTROL");
        this.cache = (control == null || control.isBlank() ? root.resolve(".worldline/cache")
                : Path.of(control).resolve("cache")).resolve("modules");
    }

    List<Path> compileAll() throws Exception {
        Files.createDirectories(cache);
        int workers = integerEnvironment("WORLDLINE_BUILD_WORKERS",
                Math.max(1, Math.min(16, Runtime.getRuntime().availableProcessors() / 2)), 1, 16);
        ExecutorService pool = Executors.newFixedThreadPool(workers);
        try {
            for (String module : modules) {
                List<Future<Artifact>> dependencies = dependencies(module).stream()
                        .map(futures::get).collect(Collectors.toList());
                futures.put(module, pool.submit(() -> compile(module, dependencies)));
            }
            List<Path> outputs = new ArrayList<>();
            for (String module : modules) {
                Artifact artifact = futures.get(module).get();
                if (!artifact.hit) GateWorkMetrics.moduleCompiled();
                Path output = build.resolve("classes").resolve(module);
                link(artifact.path, output);
                outputs.add(output);
                System.out.println("  " + (artifact.hit ? "cached" : "compiled") + " module " + module);
            }
            return outputs;
        } finally {
            pool.shutdownNow();
            if (!pool.awaitTermination(10, TimeUnit.SECONDS))
                throw new IllegalStateException("module compiler did not terminate");
        }
    }

    private Artifact compile(String module, List<Future<Artifact>> dependencyFutures) throws Exception {
        List<Artifact> dependencies = new ArrayList<>();
        for (Future<Artifact> future : dependencyFutures) dependencies.add(future.get());
        List<Path> sources = javaFiles(root.resolve("modules").resolve(module).resolve("src/main/java"));
        String release = config.getProperty("module." + module + ".release",
                required(config, "java.release"));
        String digest = digest(module, release, sources, dependencies);
        Path directory = cache.resolve(module).resolve(digest);
        Path complete = directory.resolve(".complete");
        if (complete(complete, digest)) { touch(module, digest); return new Artifact(directory, digest, true); }
        require(!Files.exists(directory), "corrupt immutable module cache entry " + directory);
        Path lockPath = cache.resolve(module).resolve(digest + ".lock");
        Files.createDirectories(lockPath.getParent());
        try (FileChannel channel = FileChannel.open(lockPath, StandardOpenOption.CREATE,
                StandardOpenOption.WRITE); FileLock lock = channel.lock()) {
            if (!lock.isValid()) throw new IllegalStateException("invalid module cache lock " + module);
            if (complete(complete, digest)) { touch(module, digest); return new Artifact(directory, digest, true); }
            require(!Files.exists(directory), "corrupt immutable module cache entry " + directory);
            Path temporary = directory.resolveSibling(digest + ".tmp-"
                    + ProcessHandle.current().pid() + "-" + System.nanoTime());
            delete(temporary); Files.createDirectories(temporary);
            try {
                List<String> command = new ArrayList<>(List.of(javaTool("javac"), "-encoding", "UTF-8",
                        "--release", release, "-Xlint:all,-options", "-Werror", "-d", temporary.toString()));
                if (!dependencies.isEmpty()) {
                    command.add("-classpath");
                    command.add(dependencies.stream().map(value -> value.path.toString())
                            .collect(Collectors.joining(System.getProperty("path.separator"))));
                }
                sources.forEach(source -> command.add(source.toString()));
                run(command, 180, cache.resolve(module));
                Files.writeString(temporary.resolve(".complete"), digest + "\n", StandardCharsets.UTF_8);
                require(!Files.exists(directory), "module cache target appeared during publication");
                move(temporary, directory); touch(module, digest);
            } finally { delete(temporary); }
        }
        return new Artifact(directory, digest, false);
    }

    private String digest(String module, String release, List<Path> sources,
            List<Artifact> dependencies) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        update(digest, module); update(digest, release); update(digest, System.getProperty("java.version"));
        for (Artifact dependency : dependencies) update(digest, dependency.digest);
        for (Path source : sources) {
            update(digest, root.relativize(source).toString().replace('\\', '/'));
            digest.update(Files.readAllBytes(source));
        }
        return HexFormat.of().formatHex(digest.digest());
    }

    private List<String> dependencies(String module) {
        String raw = required(config, "module." + module + ".dependencies").trim();
        if (raw.isEmpty()) return List.of();
        return Stream.of(raw.split(",")).map(String::trim).filter(value -> !value.isEmpty())
                .collect(Collectors.toList());
    }

    private static List<Path> javaFiles(Path sourceRoot) throws IOException {
        try (Stream<Path> paths = Files.walk(sourceRoot)) {
            List<Path> files = paths.filter(path -> path.toString().endsWith(".java"))
                    .sorted().collect(Collectors.toList());
            if (files.isEmpty()) throw new IllegalStateException("no Java sources under " + sourceRoot);
            return files;
        }
    }

    private void link(Path source, Path target) throws Exception {
        if (Files.exists(target, java.nio.file.LinkOption.NOFOLLOW_LINKS)
                && Files.isSameFile(source, target)) return;
        delete(target); Files.createDirectories(target.getParent());
        if (windows()) {
            Process process = new ProcessBuilder("cmd.exe", "/d", "/c", "mklink", "/J",
                    target.toString(), source.toAbsolutePath().toString()).directory(root.toFile())
                    .redirectErrorStream(true).redirectOutput(ProcessBuilder.Redirect.DISCARD).start();
            if (!process.waitFor(30, TimeUnit.SECONDS)) {
                ProcessCapture.destroy(process);
                throw new IllegalStateException("module cache junction timed out");
            }
            if (process.exitValue() != 0)
                throw new IllegalStateException("module cache junction exited " + process.exitValue());
        } else Files.createSymbolicLink(target, source.toAbsolutePath());
        if (!Files.isSameFile(source, target))
            throw new IllegalStateException("module cache link does not resolve to immutable entry");
    }

    private static void run(List<String> command, int timeout, Path directory) throws Exception {
        Files.createDirectories(directory);
        Path log = Files.createTempFile(directory, "javac-", ".log");
        Process process = new ProcessBuilder(command).redirectErrorStream(true)
                .redirectOutput(log.toFile()).start();
        try {
            if (!process.waitFor(timeout, TimeUnit.SECONDS)) {
                destroy(process); throw new IllegalStateException("javac timed out\n" + Files.readString(log));
            }
            if (process.exitValue() != 0)
                throw new IllegalStateException("javac exited " + process.exitValue() + "\n" + Files.readString(log));
        } finally { Files.deleteIfExists(log); }
    }

    private static void destroy(Process process) {
        process.descendants().sorted(Comparator.comparingLong(ProcessHandle::pid).reversed())
                .forEach(ProcessHandle::destroyForcibly);
        process.destroyForcibly();
    }

    private static void move(Path source, Path target) throws IOException {
        try { Files.move(source, target, StandardCopyOption.ATOMIC_MOVE); }
        catch (AtomicMoveNotSupportedException error) { Files.move(source, target); }
    }

    private static boolean complete(Path marker, String digest) throws IOException {
        return Files.isRegularFile(marker) && Files.readString(marker, StandardCharsets.UTF_8)
                .trim().equals(digest);
    }

    private void touch(String module, String digest) throws IOException {
        Files.writeString(cache.resolve(module).resolve(digest + ".used"),
                Long.toString(System.currentTimeMillis()), StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private static void delete(Path target) throws IOException {
        SafeTreeDelete.delete(target);
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }

    private static void update(MessageDigest digest, String value) {
        digest.update(value.getBytes(StandardCharsets.UTF_8)); digest.update((byte) 0);
    }

    private static String required(Properties properties, String key) {
        String value = properties.getProperty(key);
        if (value == null) throw new IllegalStateException("missing harness property: " + key);
        return value;
    }

    private static int integerEnvironment(String name, int fallback, int minimum, int maximum) {
        String raw = System.getenv(name); int value = fallback;
        if (raw != null && !raw.isBlank()) try { value = Integer.parseInt(raw); }
        catch (NumberFormatException error) { throw new IllegalArgumentException(name + " must be an integer"); }
        if (value < minimum || value > maximum) throw new IllegalArgumentException(
                name + " must be between " + minimum + " and " + maximum);
        return value;
    }

    private static String javaTool(String name) {
        return Path.of(System.getProperty("java.home"), "bin",
                name + (windows() ? ".exe" : "")).toString();
    }

    private static boolean windows() {
        return System.getProperty("os.name", "").toLowerCase().contains("win");
    }

    private static final class Artifact {
        final Path path; final String digest; final boolean hit;
        Artifact(Path path, String digest, boolean hit) {
            this.path = path; this.digest = digest; this.hit = hit;
        }
    }
}
