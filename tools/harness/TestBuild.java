import java.io.IOException;
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
import java.util.HashSet;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Compiles tests with module-scoped classpaths and runs suites in bounded parallel JVMs. */
final class TestBuild {
    private final Path root, build, cache;
    private final Properties config;
    private final List<String> modules;
    private final Map<String, Path> production = new LinkedHashMap<>();
    private final Map<String, Path> tests = new HashMap<>();

    TestBuild(Path root, Path build, Properties config, List<String> modules, List<Path> outputs) {
        this.root = root; this.build = build; this.config = config; this.modules = modules;
        for (int index = 0; index < modules.size(); index++) production.put(modules.get(index), outputs.get(index));
        String control = System.getenv("WORLDLINE_GATE_CONTROL");
        this.cache = (control == null || control.isBlank() ? root.resolve(".worldline/cache")
                : Path.of(control).resolve("cache")).resolve("tests");
    }

    void compileAndRun() throws Exception {
        compileAndRun(new HashSet<>(modules));
    }

    void compileAndRun(Set<String> selected) throws Exception {
        if (selected.isEmpty()) { System.out.println("  no affected module tests"); return; }
        Files.createDirectories(cache);
        compileTests(selected); runSuites(selected);
    }

    private void compileTests(Set<String> selected) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(workers());
        Map<String, Future<Artifact>> futures = new LinkedHashMap<>();
        try {
            for (String module : modules) {
                Path source = moduleRoot(module).resolve("src/test/java");
                if (selected.contains(module) && Files.isDirectory(source))
                    futures.put(module, pool.submit(() -> compile(module, source)));
            }
            for (Map.Entry<String, Future<Artifact>> entry : futures.entrySet()) {
                Artifact artifact = entry.getValue().get();
                Path output = build.resolve("test-modules").resolve(entry.getKey());
                publish(artifact.path, output); tests.put(entry.getKey(), output);
                System.out.println("  " + (artifact.hit ? "cached" : "compiled")
                        + " tests " + entry.getKey());
            }
            publishAggregate();
        } finally { shutdown(pool, "test compiler"); }
    }

    private void publishAggregate() throws IOException {
        Path aggregate = build.resolve("test-classes"); delete(aggregate); Files.createDirectories(aggregate);
        for (String module : modules) {
            Path source = tests.get(module); if (source == null) continue;
            try (Stream<Path> paths = Files.walk(source)) {
                for (Path path : paths.sorted().collect(Collectors.toList())) {
                    Path relative = source.relativize(path); if (relative.toString().isEmpty()) continue;
                    Path destination = aggregate.resolve(relative);
                    if (Files.isDirectory(path)) Files.createDirectories(destination);
                    else {
                        require(!Files.exists(destination), "duplicate compiled test class: " + relative);
                        Files.createDirectories(destination.getParent());
                        Files.copy(path, destination);
                    }
                }
            }
        }
    }

    private Artifact compile(String module, Path sourceRoot) throws Exception {
        List<Path> sources = javaFiles(sourceRoot);
        List<Path> classpath = classpath(module);
        String digest = digest(module, sources, classpath);
        Path directory = cache.resolve(module).resolve(digest), complete = directory.resolve(".complete");
        if (Files.isRegularFile(complete)) return new Artifact(directory, true);
        Path lockPath = cache.resolve(module).resolve(digest + ".lock");
        Files.createDirectories(lockPath.getParent());
        try (FileChannel channel = FileChannel.open(lockPath, StandardOpenOption.CREATE,
                StandardOpenOption.WRITE); FileLock lock = channel.lock()) {
            require(lock.isValid(), "invalid test cache lock: " + module);
            if (Files.isRegularFile(complete)) return new Artifact(directory, true);
            Path temporary = directory.resolveSibling(digest + ".tmp-"
                    + ProcessHandle.current().pid() + "-" + System.nanoTime());
            delete(temporary); Files.createDirectories(temporary);
            try {
                List<String> command = new ArrayList<>(List.of(javaTool("javac"), "-encoding", "UTF-8",
                        "--release", required("test.release"), "-Xlint:all,-options", "-Werror", "-d",
                        temporary.toString(), "-classpath", join(classpath)));
                sources.forEach(path -> command.add(path.toString()));
                Result result = execute(command, 180, cache.resolve(module));
                require(result.exit == 0, "test compilation failed for " + module + "\n" + result.output);
                Files.writeString(temporary.resolve(".complete"), digest + "\n", StandardCharsets.UTF_8);
                delete(directory); move(temporary, directory);
            } finally { delete(temporary); }
        }
        return new Artifact(directory, false);
    }

    private void runSuites(Set<String> selected) throws Exception {
        Map<String, String> owners = suiteOwners();
        ExecutorService pool = Executors.newFixedThreadPool(workers());
        List<String> suites = values("test.suites").stream()
                .filter(suite -> selected.contains(owners.get(suite))).collect(Collectors.toList());
        List<Future<Result>> futures = new ArrayList<>();
        try {
            for (String suite : suites) {
                String module = owners.get(suite);
                require(module != null && tests.containsKey(module), "test suite has no owning module: " + suite);
                List<Path> classpath = classpath(module); classpath.add(build.resolve("test-classes"));
                futures.add(pool.submit(() -> execute(List.of(javaTool("java"), "-ea",
                        "-Dworldline.test.classes=" + build.resolve("test-classes"), "-classpath",
                        join(classpath), suite), timeout(), cache.resolve("runs"))));
            }
            List<String> failures = new ArrayList<>();
            for (int index = 0; index < suites.size(); index++) {
                Result result = futures.get(index).get();
                if (!result.output.isBlank()) System.out.print(result.output);
                if (result.exit != 0) failures.add(suites.get(index) + " exited " + result.exit);
            }
            require(failures.isEmpty(), "test failures: " + failures);
        } finally { shutdown(pool, "test runner"); }
    }

    private Map<String, String> suiteOwners() throws IOException {
        Map<String, String> owners = new HashMap<>();
        for (String module : modules) {
            Path source = moduleRoot(module).resolve("src/test/java");
            if (!Files.isDirectory(source)) continue;
            for (Path file : javaFiles(source)) {
                String relative = source.relativize(file).toString();
                String name = relative.substring(0, relative.length() - 5)
                        .replace('/', '.').replace('\\', '.');
                require(owners.put(name, module) == null, "duplicate test class: " + name);
            }
        }
        return owners;
    }

    private List<Path> classpath(String module) {
        List<Path> paths = new ArrayList<>();
        for (String dependency : closure(module, new HashSet<>())) paths.add(production.get(dependency));
        String testKey = "module." + module + ".test.dependencies";
        if (config.getProperty(testKey) != null)
            for (String dependency : values(testKey))
                for (String transitive : withSelf(dependency))
                    if (!paths.contains(production.get(transitive))) paths.add(production.get(transitive));
        paths.add(production.get(module));
        return paths;
    }

    private List<String> withSelf(String module) {
        List<String> result = closure(module, new HashSet<>()); result.add(module); return result;
    }

    private List<String> closure(String module, Set<String> seen) {
        List<String> ordered = new ArrayList<>();
        for (String dependency : values("module." + module + ".dependencies")) {
            if (seen.add(dependency)) { ordered.addAll(closure(dependency, seen)); ordered.add(dependency); }
        }
        return ordered.stream().distinct().collect(Collectors.toList());
    }

    private String digest(String module, List<Path> sources, List<Path> classpath) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        update(digest, module); update(digest, required("test.release"));
        update(digest, System.getProperty("java.version"));
        for (Path path : classpath)
            update(digest, Files.readString(path.resolve(".worldline-module.sha256"), StandardCharsets.UTF_8).trim());
        for (Path source : sources) {
            update(digest, root.relativize(source).toString().replace('\\', '/'));
            digest.update(Files.readAllBytes(source));
        }
        return HexFormat.of().formatHex(digest.digest());
    }

    private Result execute(List<String> command, int timeout, Path directory) throws Exception {
        Files.createDirectories(directory); Path log = Files.createTempFile(directory, "process-", ".log");
        Process process = new ProcessBuilder(command).directory(root.toFile()).redirectErrorStream(true)
                .redirectOutput(log.toFile()).start();
        try {
            if (!process.waitFor(timeout, TimeUnit.SECONDS)) {
                destroy(process); return new Result(124, Files.readString(log) + "process timed out\n");
            }
            return new Result(process.exitValue(), Files.readString(log, StandardCharsets.UTF_8));
        } finally { Files.deleteIfExists(log); }
    }

    private List<String> values(String key) {
        String raw = required(key).trim();
        if (raw.isEmpty()) return List.of();
        return Stream.of(raw.split(",")).map(String::trim).filter(value -> !value.isEmpty())
                .collect(Collectors.toList());
    }

    private String required(String key) {
        String value = config.getProperty(key);
        if (value == null) throw new IllegalStateException("missing harness property: " + key);
        return value;
    }

    private Path moduleRoot(String module) { return root.resolve("modules").resolve(module); }
    private int workers() { return integerEnvironment("WORLDLINE_TEST_WORKERS", 4, 1, 16); }
    private int timeout() { return integerEnvironment("WORLDLINE_TEST_TIMEOUT_SECONDS", 180, 1, 3600); }

    private static List<Path> javaFiles(Path root) throws IOException {
        try (Stream<Path> paths = Files.walk(root)) {
            return paths.filter(path -> path.toString().endsWith(".java"))
                    .sorted().collect(Collectors.toList());
        }
    }

    private static void publish(Path source, Path target) throws IOException {
        delete(target); Files.createDirectories(target);
        try (Stream<Path> paths = Files.walk(source)) {
            for (Path path : paths.sorted().collect(Collectors.toList())) {
                Path relative = source.relativize(path); if (relative.toString().equals(".complete")) continue;
                Path destination = target.resolve(relative);
                if (Files.isDirectory(path)) Files.createDirectories(destination);
                else Files.copy(path, destination, StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    private static void destroy(Process process) {
        process.descendants().sorted(Comparator.comparingLong(ProcessHandle::pid).reversed())
                .forEach(ProcessHandle::destroyForcibly); process.destroyForcibly();
    }

    private static void shutdown(ExecutorService pool, String name) throws Exception {
        pool.shutdownNow();
        if (!pool.awaitTermination(10, TimeUnit.SECONDS))
            throw new IllegalStateException(name + " did not terminate");
    }

    private static void move(Path source, Path target) throws IOException {
        try { Files.move(source, target, StandardCopyOption.ATOMIC_MOVE); }
        catch (AtomicMoveNotSupportedException error) { Files.move(source, target); }
    }

    private static void delete(Path target) throws IOException {
        if (!Files.exists(target)) return;
        try (Stream<Path> paths = Files.walk(target)) {
            for (Path path : paths.sorted(Comparator.reverseOrder()).collect(Collectors.toList()))
                Files.deleteIfExists(path);
        }
    }

    private static String join(List<Path> paths) {
        return paths.stream().map(Path::toString).collect(Collectors.joining(System.getProperty("path.separator")));
    }

    private static void update(MessageDigest digest, String value) {
        digest.update(value.getBytes(StandardCharsets.UTF_8)); digest.update((byte) 0);
    }

    private static int integerEnvironment(String name, int fallback, int minimum, int maximum) {
        String raw = System.getenv(name); int value = fallback;
        if (raw != null && !raw.isBlank()) try { value = Integer.parseInt(raw); }
        catch (NumberFormatException error) { throw new IllegalArgumentException(name + " must be an integer"); }
        require(value >= minimum && value <= maximum,
                name + " must be between " + minimum + " and " + maximum);
        return value;
    }

    private static String javaTool(String name) {
        boolean windows = System.getProperty("os.name", "").toLowerCase().contains("win");
        return Path.of(System.getProperty("java.home"), "bin", name + (windows ? ".exe" : "")).toString();
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }

    private static final class Artifact {
        final Path path; final boolean hit;
        Artifact(Path path, boolean hit) { this.path = path; this.hit = hit; }
    }
    private static final class Result {
        final int exit; final String output;
        Result(int exit, String output) { this.exit = exit; this.output = output; }
    }
}
