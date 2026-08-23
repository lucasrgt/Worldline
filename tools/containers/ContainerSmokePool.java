import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/** Isolated, bounded Docker runner for official headless smoke cycles. */
public final class ContainerSmokePool {
    private static final String IMAGE = "worldline/smoke-server:local";
    private static final String LABEL = "dev.worldline.official-runtime=isolated";
    private static final int MAX_JOBS = 25;
    private final Path root = Path.of("").toAbsolutePath().normalize();

    public static void main(String[] arguments) {
        try {
            if (List.of(arguments).equals(List.of("--self-test"))) { selfTest(); return; }
            new ContainerSmokePool().run(Options.parse(arguments));
        } catch (Exception error) {
            System.err.println("container smoke pool failed: " + error.getMessage());
            System.exit(1);
        }
    }

    private void run(Options options) throws Exception {
        require(Files.isRegularFile(options.manifest), "manifest does not exist: " + options.manifest);
        List<Task> tasks = parse(Files.readAllLines(options.manifest, StandardCharsets.UTF_8), root);
        require(!tasks.isEmpty(), "manifest contains no tasks"); Path jar = officialServerJar();
        DockerCapacity capacity = dockerCapacity(options.memoryBytes);
        int jobs = options.jobs == 0 ? capacity.safeJobs : options.jobs;
        require(jobs >= 1 && jobs <= MAX_JOBS, "jobs must be between 1 and " + MAX_JOBS);
        require(jobs <= capacity.safeJobs, "requested " + jobs + " jobs but Docker capacity safely admits "
                + capacity.safeJobs + "; lower --jobs/--memory or increase Docker resources");
        Path lockPath = root.resolve(".worldline/container-pool.lock"); Files.createDirectories(lockPath.getParent());
        try (FileChannel channel = FileChannel.open(lockPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
             FileLock ignored = channel.tryLock()) {
            require(ignored != null, "another Worldline container batch owns " + lockPath);
            String image = prepareImage(options);
            executeBatch(tasks, jobs, options, jar, capacity, image);
        }
    }

    private String prepareImage(Options options) throws Exception {
        if (!options.skipVerify) {
            Result verified = execute(List.of("java", "tools/harness/Gate.java"), root,
                    Duration.ofMinutes(10), null);
            require(verified.exit == 0, "host Gate failed\n" + verified.output);
        }
        require(Files.isDirectory(root.resolve(".worldline/build/classes/api")),
                "missing prepared classes; run java tools/harness/Gate.java");
        if (!options.skipBuild) {
            Path log = root.resolve(".worldline/container-image-build.log");
            Result build = execute(List.of("docker", "build", "--pull=false", "--label", LABEL,
                    "-t", IMAGE, "-f", "tools/containers/server.Dockerfile", "."), root,
                    Duration.ofMinutes(15), log);
            require(build.exit == 0, "container image build failed; see " + log);
        }
        Result image = execute(List.of("docker", "image", "inspect", "--format", "{{.Id}}", IMAGE),
                root, Duration.ofSeconds(20), null);
        require(image.exit == 0 && image.output.trim().startsWith("sha256:"), "prepared image is unavailable");
        return image.output.trim();
    }

    private void executeBatch(List<Task> tasks, int jobs, Options options, Path jar,
                              DockerCapacity capacity, String image) throws Exception {
        String stamp = DateTimeFormatter.ofPattern("uuuuMMdd-HHmmss", Locale.ROOT).withZone(ZoneOffset.UTC)
                .format(Instant.now());
        Path batch = root.resolve(".worldline/container-smokes").resolve(stamp); Files.createDirectories(batch);
        ContainerPoolContext.Context context = ContainerPoolContext.create(root, batch, image);
        AtomicInteger active = new AtomicInteger(), peak = new AtomicInteger();
        List<Outcome> outcomes = bounded(tasks, jobs,
                task -> runTask(task, options, jar, batch, active, peak, context, image));
        long passed = outcomes.stream().filter(Outcome::passed).count();
        Properties report = new Properties(); report.setProperty("tasks", Integer.toString(tasks.size()));
        report.setProperty("passed", Long.toString(passed)); report.setProperty("failed", Long.toString(tasks.size() - passed));
        report.setProperty("jobs", Integer.toString(jobs)); report.setProperty("peak", Integer.toString(peak.get()));
        report.setProperty("docker.cpus", Integer.toString(capacity.cpus)); report.setProperty("docker.memory.bytes", Long.toString(capacity.memoryBytes));
        report.setProperty("image.id", image);
        try (var writer = Files.newBufferedWriter(batch.resolve("batch.properties"), StandardCharsets.UTF_8)) {
            report.store(writer, "Worldline isolated container smoke batch");
        }
        System.out.println("container smoke batch: " + passed + "/" + tasks.size() + " passed, peak="
                + peak.get() + ", evidence=" + batch);
        if (passed != tasks.size()) {
            String failed = outcomes.stream().filter(outcome -> !outcome.passed)
                    .map(outcome -> outcome.id + " (" + outcome.message + ")").reduce((a, b) -> a + ", " + b).orElse("");
            throw new IllegalStateException("candidate failures: " + failed);
        }
    }

    private Outcome runTask(Task task, Options options, Path jar, Path batch, AtomicInteger active,
                            AtomicInteger peak, ContainerPoolContext.Context context, String image) {
        int now = active.incrementAndGet(); peak.accumulateAndGet(now, Math::max);
        String nonce = Long.toUnsignedString(System.nanoTime(), 36);
        String container = "worldline-smoke-" + task.id + "-" + nonce;
        Path output = batch.resolve(task.id), log = output.resolve("console.log");
        boolean created = false;
        try {
            Files.createDirectories(output);
            List<String> command = ContainerInvocation.command(task.id, task.argument, options.memory,
                    options.cpus, options.heap, jar, container, context, image);
            Result create = execute(command, root, Duration.ofSeconds(30), null);
            require(create.exit == 0, "docker create failed: " + create.output.trim());
            created = true;
            Result run = execute(List.of("docker", "start", "--attach", container), root,
                    Duration.ofSeconds(task.timeoutSeconds), log);
            Result copy = execute(List.of("docker", "cp", container + ":/runtime/.",
                    output.resolve("evidence").toString()), root, Duration.ofMinutes(2), null);
            require(copy.exit == 0, "container evidence copy failed");
            require(run.exit == 0, "exit " + run.exit + "; see " + log);
            System.out.println("PASS " + task.id);
            return new Outcome(task.id, true, "passed");
        } catch (Exception error) {
            System.out.println("FAIL " + task.id + " " + error.getMessage());
            return new Outcome(task.id, false, error.getMessage());
        } finally {
            if (created) quiet(List.of("docker", "rm", "--force", container));
            active.decrementAndGet();
        }
    }


    private DockerCapacity dockerCapacity(long perTask) throws Exception {
        Result info = execute(List.of("docker", "info", "--format", "{{.NCPU}} {{.MemTotal}}"), root,
                Duration.ofSeconds(20), null);
        require(info.exit == 0, "Docker daemon is unavailable: " + info.output.trim());
        String[] fields = info.output.trim().split(" +");
        require(fields.length == 2, "unexpected Docker capacity response");
        int cpus = Integer.parseInt(fields[0]); long memory = Long.parseLong(fields[1]);
        long reserve = Math.min(2L << 30, memory / 4);
        int memoryJobs = (int) Math.max(1, (memory - reserve) / perTask);
        int safe = Math.max(1, Math.min(MAX_JOBS, Math.min(memoryJobs, cpus * 4)));
        return new DockerCapacity(cpus, memory, safe);
    }

    private Path officialServerJar() throws Exception {
        Properties values = new Properties();
        try (var reader = Files.newBufferedReader(root.resolve("artifacts/minecraft-b1.7.3-server.properties"))) {
            values.load(reader);
        }
        Path jar = root.resolve(required(values, "local.path")).toRealPath();
        require(Files.size(jar) == Long.parseLong(required(values, "expected.bytes")), "official server size drift");
        byte[] bytes = Files.readAllBytes(jar);
        String digest = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        require(digest.equals(required(values, "expected.sha256")), "official server SHA-256 drift");
        return jar;
    }

    private void quiet(List<String> command) {
        try { execute(command, root, Duration.ofSeconds(20), null); } catch (Exception ignored) { }
    }

    private static Result execute(List<String> command, Path directory, Duration timeout, Path log) throws Exception {
        ProcessBuilder builder = new ProcessBuilder(command).directory(directory.toFile()).redirectErrorStream(true);
        if (log != null) { Files.createDirectories(log.getParent()); builder.redirectOutput(log.toFile()); }
        Process process = builder.start(); ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        Thread reader = null;
        if (log == null) reader = Thread.ofVirtual().start(() -> { try { process.getInputStream().transferTo(bytes); }
            catch (IOException error) { throw new IllegalStateException(error); } });
        if (!process.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS)) {
            process.descendants().forEach(ProcessHandle::destroyForcibly); process.destroyForcibly();
            process.waitFor(); if (reader != null) reader.join();
            return new Result(124, "timeout after " + timeout);
        }
        if (reader != null) reader.join();
        return new Result(process.exitValue(), log == null ? bytes.toString(StandardCharsets.UTF_8) : "");
    }

    private static <T> List<Outcome> bounded(List<T> inputs, int jobs, TaskRun<T> action) throws Exception {
        Semaphore permits = new Semaphore(jobs); List<Future<Outcome>> futures = new ArrayList<>();
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (T input : inputs) futures.add(executor.submit(() -> {
                permits.acquire(); try { return action.run(input); } finally { permits.release(); }
            }));
            List<Outcome> outcomes = new ArrayList<>();
            for (Future<Outcome> future : futures) outcomes.add(future.get());
            return outcomes;
        }
    }

    private static List<Task> parse(List<String> lines, Path root) {
        List<Task> tasks = new ArrayList<>(); Set<String> ids = new HashSet<>(), cases = new HashSet<>();
        for (int index = 0; index < lines.size(); index++) {
            String line = lines.get(index).trim(); if (line.isEmpty() || line.startsWith("#")) continue;
            String[] fields = line.split("\\t", -1);
            require(fields.length == 5, "manifest line " + (index + 1) + " must have five TSV fields");
            require(fields[0].matches("[a-z0-9][a-z0-9-]{1,63}"), "invalid task id: " + fields[0]);
            require(ids.add(fields[0]), "duplicate task id: " + fields[0]);
            require(fields[1].equals("server-headless"), "unsupported or unsafe lane: " + fields[1]);
            require(fields[2].matches("tools/smoke/[A-Za-z0-9]+Cycle\\.java"), "unsafe source: " + fields[2]);
            try { SmokeManifestSource.validate(root, fields[2], fields[3]); }
            catch (Exception error) { throw new IllegalArgumentException(error.getMessage(), error); }
            require(fields[3].matches("[a-z0-9][a-z0-9-]{1,79}"), "invalid smoke argument");
            require(cases.add(fields[2] + "\t" + fields[3]), "duplicate smoke case: " + fields[3]);
            int timeout = Integer.parseInt(fields[4]); require(timeout >= 30 && timeout <= 3600, "unsafe timeout");
            tasks.add(new Task(fields[0], fields[2], fields[3], timeout));
        }
        return List.copyOf(tasks);
    }

    private static void selfTest() throws Exception {
        Path root = Path.of("").toAbsolutePath().normalize();
        List<Task> tasks = parse(List.of("m1-test\tserver-headless\ttools/smoke/DataDrivenCycle.java"
                + "\tm420-wolf-tame-set\t60"), root);
        require(tasks.size() == 1 && tasks.getFirst().timeoutSeconds == 60, "manifest parse drift");
        expectFailure(() -> parse(List.of("m1-test\tgui\ttools/smoke/DataDrivenCycle.java"
                + "\tm420-wolf-tame-set\t60"), root));
        expectFailure(() -> parse(List.of("m1-test\tserver-headless\t../ApiCycle.java\tm3-domain-api\t60"), root));
        concurrencyTest(10); concurrencyTest(25);
        require(parse(Files.readAllLines(root.resolve("tools/containers/smokes-25.tsv")), root).size() == 25,
                "bundled 25-smoke manifest drift");
        Options options = Options.parse(new String[] {"run", "tools/containers/smokes-25.tsv", "--jobs", "10"});
        require(options.jobs == 10 && options.memoryBytes == 384L * 1024 * 1024, "option defaults drift");
        ContainerPoolContext.Context context = new ContainerPoolContext.Context(
                root.resolve(".worldline/container-context"),
                "00000000-0000-0000-0000-000000000000", "a".repeat(40), "b".repeat(40));
        List<String> command = ContainerInvocation.command(tasks.getFirst().id, tasks.getFirst().argument,
                options.memory, options.cpus, options.heap, Path.of("C:/oracle/server.jar"),
                "test-container", context, "sha256:test");
        require(command.contains("none") && command.contains("--read-only") && command.contains("--cap-drop")
                && command.stream().anyMatch(value -> value.endsWith("server.jar,readonly"))
                && command.contains("tools/harness/Gate.java") && command.contains("--milestone"),
                "canonical container Gate or isolation command drift");
        System.out.println("container smoke pool self-test passed");
    }

    private static void concurrencyTest(int width) throws Exception {
        AtomicInteger active = new AtomicInteger(), peak = new AtomicInteger(); List<Integer> inputs = new ArrayList<>();
        for (int i = 0; i < 25; i++) inputs.add(i); var gate = new java.util.concurrent.CountDownLatch(width);
        List<Outcome> outcomes = bounded(inputs, width, ignored -> { int value = active.incrementAndGet();
            peak.accumulateAndGet(value, Math::max); gate.countDown(); gate.await(); active.decrementAndGet();
            return new Outcome("test", true, "passed"); });
        require(outcomes.size() == 25 && peak.get() == width, "bounded concurrency drift: " + peak.get());
    }

    private static void expectFailure(Checked action) throws Exception {
        try { action.run(); throw new IllegalStateException("expected validation failure"); }
        catch (IllegalArgumentException expected) { }
    }

    private static String required(Properties values, String key) {
        String value = values.getProperty(key); require(value != null && !value.isBlank(), "missing " + key); return value.trim();
    }
    private static void require(boolean value, String message) { if (!value) throw new IllegalArgumentException(message); }

    private record Task(String id, String source, String argument, int timeoutSeconds) { }
    private record Outcome(String id, boolean passed, String message) { }
    private record Result(int exit, String output) { }
    private record DockerCapacity(int cpus, long memoryBytes, int safeJobs) { }
    @FunctionalInterface private interface TaskRun<T> { Outcome run(T input) throws Exception; }
    @FunctionalInterface private interface Checked { void run() throws Exception; }

    private record Options(Path manifest, int jobs, String memory, long memoryBytes, String cpus, String heap,
                           boolean skipBuild, boolean skipVerify) {
        static Options parse(String[] arguments) {
            require(arguments.length >= 2 && arguments[0].equals("run"), "usage: java tools/containers/"
                    + "ContainerSmokePool.java run MANIFEST [--jobs auto|1..25] [--memory 384m] [--cpus .75]"
                    + " [--heap 192m] [--skip-build] [--skip-verify]");
            Path manifest = Path.of(arguments[1]).toAbsolutePath().normalize(); int jobs = 0;
            String memory = "384m", cpus = "0.75", heap = "192m"; boolean skipBuild = false, skipVerify = false;
            for (int i = 2; i < arguments.length; i++) {
                switch (arguments[i]) {
                    case "--jobs" -> { String value = next(arguments, ++i); jobs = value.equals("auto") ? 0 : Integer.parseInt(value); }
                    case "--memory" -> memory = next(arguments, ++i).toLowerCase(Locale.ROOT);
                    case "--cpus" -> cpus = next(arguments, ++i);
                    case "--heap" -> heap = next(arguments, ++i).toLowerCase(Locale.ROOT);
                    case "--skip-build" -> skipBuild = true;
                    case "--skip-verify" -> skipVerify = true;
                    default -> throw new IllegalArgumentException("unknown option: " + arguments[i]);
                }
            }
            require(jobs >= 0 && jobs <= MAX_JOBS, "jobs must be auto or 1..25");
            long memoryBytes = size(memory), heapBytes = size(heap);
            require(memoryBytes >= heapBytes + 128L * 1024 * 1024, "container memory needs at least 128m above heap");
            double cpuValue = Double.parseDouble(cpus); require(cpuValue >= 0.25 && cpuValue <= 4.0, "cpus must be 0.25..4.0");
            return new Options(manifest, jobs, memory, memoryBytes, cpus, heap, skipBuild, skipVerify);
        }
        private static String next(String[] values, int index) {
            require(index < values.length && !values[index].startsWith("--"), "missing option value"); return values[index];
        }
        private static long size(String value) {
            require(value.matches("[1-9][0-9]*[mg]"), "size must use m or g: " + value);
            long amount = Long.parseLong(value.substring(0, value.length() - 1));
            return Math.multiplyExact(amount, value.endsWith("g") ? 1024L * 1024 * 1024 : 1024L * 1024);
        }
    }
}
