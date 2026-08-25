import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Validates each smoke coordinator through an independent immutable cache entry. */
final class SmokeRunnerBuild {
    private static final String CACHE_VERSION = "smoke-runners-v2";
    private final Path root;
    private final Path cache;

    SmokeRunnerBuild(Path root, Path ignoredBuild) {
        this.root = root;
        String control = System.getenv("WORLDLINE_GATE_CONTROL");
        Path base = control == null || control.isBlank()
                ? root.resolve(".worldline/cache") : Path.of(control).resolve("cache");
        cache = base.resolve(CACHE_VERSION);
    }

    void compile() throws Exception {
        List<Path> sources;
        try (Stream<Path> paths = Files.list(root.resolve("tools/smoke"))) {
            sources = paths.filter(path -> path.toString().endsWith(".java")).sorted().toList();
        }
        Files.createDirectories(cache);
        AtomicInteger hits = new AtomicInteger();
        int workers = workers(sources.size());
        List<Callable<Void>> tasks = sources.stream().<Callable<Void>>map(source -> () -> {
            if (validate(source)) hits.incrementAndGet();
            return null;
        }).toList();
        try (var executor = Executors.newFixedThreadPool(workers)) {
            List<Future<Void>> futures = executor.invokeAll(tasks);
            for (Future<Void> future : futures) future.get();
        }
        int cached = hits.get();
        GateWorkMetrics.smokeRunnersCompiled(sources.size() - cached);
        System.out.println("  " + cached + " cached, " + (sources.size() - cached)
                + " compiled smoke runners (" + workers + " workers)");
    }

    private boolean validate(Path source) throws Exception {
        String digest = digest(source);
        String runner = source.getFileName().toString().replaceFirst("[.]java$", "");
        Path family = cache.resolve(runner);
        Path entry = family.resolve(digest);
        Path complete = entry.resolve(".complete");
        if (Files.isRegularFile(complete)) { CacheUsage.touch(entry); return true; }
        Files.createDirectories(family);
        Path lockPath = family.resolve(digest + ".lock");
        try (FileChannel channel = FileChannel.open(lockPath, StandardOpenOption.CREATE,
                StandardOpenOption.WRITE); FileLock lock = channel.lock()) {
            if (!lock.isValid()) throw new IllegalStateException("invalid runner cache lock: " + runner);
            if (Files.isRegularFile(complete)) { CacheUsage.touch(entry); return true; }
            Path temporary = family.resolve(digest + ".tmp-" + ProcessHandle.current().pid()
                    + "-" + Thread.currentThread().threadId());
            delete(temporary);
            Files.createDirectories(temporary);
            compile(source, runner, digest, temporary);
            delete(entry);
            Files.move(temporary, entry);
            CacheUsage.touch(entry);
            return false;
        }
    }

    private void compile(Path source, String runner, String digest, Path output) throws Exception {
        Path arguments = output.resolve("javac.args");
        List<String> lines = new ArrayList<>(List.of("-encoding", "UTF-8", "--release", "21",
                "-Xlint:all,-options", "-Werror", "-classpath", quote(harnessClasspath()),
                "-d", quote(output.toString()), quote(source.toString())));
        Files.write(arguments, lines, StandardCharsets.UTF_8);
        ProcessCapture.Result result = ProcessCapture.run(root,
                List.of(javaTool("javac"), "@" + arguments), 300);
        if (result.timedOut()) throw new IllegalStateException("smoke runner compilation timed out: "
                + runner + "\n" + ProcessCapture.tail(result.output(), 8_000));
        if (result.exit() != 0) throw new IllegalStateException("smoke runner compilation failed: "
                + runner + "\n" + result.output());
        Files.delete(arguments);
        Files.writeString(output.resolve(".complete"), digest + "\n", StandardCharsets.UTF_8);
    }

    private String digest(Path source) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        update(digest, CACHE_VERSION);
        update(digest, System.getProperty("java.version"));
        update(digest, root.relativize(source).toString().replace('\\', '/'));
        digest.update(Files.readAllBytes(source));
        String text = Files.readString(source, StandardCharsets.UTF_8);
        if (text.contains("SmokeSupport")) addSupport(digest, "SmokeSupport.java");
        if (text.contains("SmokeRetry")) addSupport(digest, "SmokeRetry.java");
        if (text.contains("DataDrivenCyclePlan")) addSupport(digest, "DataDrivenCyclePlan.java");
        if (text.contains("DataDrivenSupport")) {
            addSupport(digest, "DataDrivenSupport.java"); addSupport(digest, "SmokeSupport.java");
        }
        if (text.contains("SmokeRetryBoundary")) {
            addSupport(digest, "SmokeRetryBoundary.java"); addSupport(digest, "SmokeRetry.java");
            addSupport(digest, "SmokeSupport.java");
        }
        if (text.contains("ExceptionalSmokeSupport")) {
            addSupport(digest, "ExceptionalSmokeSupport.java"); addSupport(digest, "SmokeSupport.java");
        }
        return HexFormat.of().formatHex(digest.digest());
    }

    private void addSupport(MessageDigest digest, String name) throws Exception {
        Path path = root.resolve("tools/harness").resolve(name);
        update(digest, root.relativize(path).toString().replace('\\', '/'));
        digest.update(Files.readAllBytes(path));
    }

    private static void update(MessageDigest digest, String value) {
        digest.update(value.getBytes(StandardCharsets.UTF_8));
        digest.update((byte) 0);
    }

    private String harnessClasspath() {
        String value = System.getenv("WORLDLINE_HARNESS_CP");
        if (value == null || value.isBlank()) throw new IllegalStateException("missing harness classpath");
        return value;
    }

    private static int workers(int tasks) {
        String configured = System.getenv("WORLDLINE_BUILD_WORKERS");
        int processors = Math.max(1, Runtime.getRuntime().availableProcessors());
        int value = configured == null || configured.isBlank()
                ? Math.max(1, processors / 2) : Integer.parseInt(configured);
        return Math.max(1, Math.min(Math.min(16, tasks), value));
    }

    private static String quote(String value) {
        return "\"" + value.replace("\\", "/") + "\"";
    }

    private static void delete(Path target) throws Exception {
        SafeTreeDelete.delete(target);
    }

    private static String javaTool(String name) {
        boolean windows = System.getProperty("os.name", "").toLowerCase().contains("win");
        return Path.of(System.getProperty("java.home"), "bin",
                name + (windows ? ".exe" : "")).toString();
    }
}
