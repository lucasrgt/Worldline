import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Native process pool and resource simulator for isolated official-runtime smokes. */
public final class HostSmokePool {
    private final Path root = Path.of("").toAbsolutePath().normalize();

    public static void main(String[] arguments) {
        try {
            if (List.of(arguments).equals(List.of("--self-test"))) { selfTest(); return; }
            new HostSmokePool().execute(Options.parse(arguments));
        } catch (Exception error) {
            System.err.println("host smoke pool failed: " + error.getMessage()); System.exit(1);
        }
    }

    private void execute(Options options) throws Exception {
        Config config = Config.load(root, options); List<Task> tasks = parse(options.manifest, root);
        String lane = tasks.get(0).lane; require(tasks.stream().allMatch(task -> task.lane.equals(lane)), "a batch must contain exactly one runtime lane");
        Profile profile = Profile.of(lane, config); Host host = Host.measure(); Model model = Model.of(host, profile);
        if (options.action.equals("simulate")) { simulate(tasks.size(), config, profile, host, model); return; }
        int jobs = config.parallelism == 0 ? model.safeJobs : config.parallelism;
        require(jobs <= profile.maxParallelism, lane + " permits at most " + profile.maxParallelism + " jobs");
        if (lane.equals("windows-client-gui")) require(config.backend.equals("windows-job"), "windows-client-gui requires the windows-job backend");
        require(jobs >= 1 && jobs <= model.safeJobs, "requested " + jobs + " jobs but current host admission is "
                + model.safeJobs + "; use simulate, reduce parallelism, or free resources");
        Path lockPath = config.lockPath(root); Files.createDirectories(lockPath.getParent());
        try (FileChannel channel = FileChannel.open(lockPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
             FileLock lock = channel.tryLock()) {
            require(lock != null, "another official-runtime owner holds " + lockPath);
            require(noForeignRuntime(), "an official smoke, Minecraft JAR, or runClient process is already active");
            if (!options.skipVerify) verify();
            require(noForeignRuntime(), "an official runtime appeared while preparing the batch");
            prepareBackend(config.backend);
            runBatch(tasks, config, profile, host, model, jobs);
        }
    }

    private void simulate(int tasks, Config config, Profile profile, Host host, Model model) {
        System.out.printf(Locale.ROOT, "Worldline host smoke resource simulation%n"
                + "backend: %s%n"
                + "host: cpus=%d total=%.1fGiB free=%.1fGiB reserve=%.1fGiB%n"
                + "worker: estimate=%.3fGiB limit=%.3fGiB heap=%s cpu=%.2f task=%ds%n"
                + "admission: safe=%d configured=%s tasks=%d%n%n",
                config.backend,
                host.cpus, gib(host.totalMemory), gib(host.freeMemory), gib(config.reserveBytes),
                gib(profile.workerBytes), gib(profile.memoryLimitBytes), profile.heap, profile.cpuUnits, profile.durationSeconds,
                model.safeJobs, config.parallelism == 0 ? "auto" : config.parallelism, tasks);
        System.out.println("jobs  waves  ramGiB  cpuDemand  estSeconds  status");
        Set<Integer> widths = new java.util.TreeSet<>(List.of(1, 2, 4, 8, 10, 12, 16, 20, 25));
        if (config.parallelism > 0) widths.add(config.parallelism);
        for (int jobs : widths) {
            int waves = (tasks + jobs - 1) / jobs; double cpu = jobs * profile.cpuUnits;
            double penalty = Math.max(1.0, cpu / Math.max(1.0, host.cpus * 0.85));
            long seconds = Math.round(waves * profile.durationSeconds * penalty);
            String status = jobs <= model.safeJobs && jobs <= profile.maxParallelism ? "SAFE" : "REJECT";
            System.out.printf(Locale.ROOT, "%4d  %5d  %6.2f  %9.2f  %10d  %s%n",
                    jobs, waves, gib(jobs * profile.workerBytes), cpu, seconds, status);
        }
        System.out.println("\nSimulation is an admission model, not a benchmark. Promote a width only after frozen signatures match.");
    }

    private void runBatch(List<Task> tasks, Config config, Profile profile, Host host, Model model, int jobs) throws Exception {
        String stamp = DateTimeFormatter.ofPattern("uuuuMMdd-HHmmss", Locale.ROOT).withZone(ZoneOffset.UTC)
                .format(Instant.now()); Path batch = root.resolve(".worldline/host-smokes").resolve(stamp);
        Files.createDirectories(batch); Path prebuilt = profile.lane.equals("windows-client-gui") ? prebuild(tasks, batch) : null;
        AtomicInteger active = new AtomicInteger(), peak = new AtomicInteger();
        long started = System.nanoTime();
        List<Outcome> outcomes = bounded(tasks, jobs, task -> runTask(task, config, profile, prebuilt, batch, active, peak));
        long passed = outcomes.stream().filter(Outcome::passed).count(); Properties report = new Properties();
        report.setProperty("backend", config.backend); report.setProperty("lane", profile.lane);
        report.setProperty("tasks", Integer.toString(tasks.size()));
        report.setProperty("passed", Long.toString(passed)); report.setProperty("failed", Long.toString(tasks.size() - passed));
        report.setProperty("jobs", Integer.toString(jobs)); report.setProperty("peak", Integer.toString(peak.get()));
        report.setProperty("host.cpus", Integer.toString(host.cpus)); report.setProperty("host.memory.bytes", Long.toString(host.totalMemory));
        report.setProperty("admission.safe", Integer.toString(model.safeJobs));
        report.setProperty("elapsed.millis", Long.toString(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started)));
        try (var writer = Files.newBufferedWriter(batch.resolve("batch.properties"), StandardCharsets.UTF_8)) { report.store(writer, "Worldline native smoke batch"); }
        System.out.println(config.backend + " smoke batch: " + passed + "/" + tasks.size() + " passed, peak=" + peak.get() + ", evidence=" + batch);
        if (passed != tasks.size()) throw new IllegalStateException("candidate failures: " + outcomes.stream().filter(outcome -> !outcome.passed)
                .map(outcome -> outcome.id + " (" + outcome.message + ")").collect(Collectors.joining(", ")));
    }

    private Outcome runTask(Task task, Config config, Profile profile, Path prebuilt, Path batch, AtomicInteger active, AtomicInteger peak) {
        int now = active.incrementAndGet(); peak.accumulateAndGet(now, Math::max);
        Path output = batch.resolve(task.id), log = output.resolve("console.log"), tmp = output.resolve("tmp");
        Path evidence = root.resolve(".worldline/smokes").resolve(task.argument).normalize();
        try {
            require(evidence.startsWith(root.resolve(".worldline/smokes")), "unsafe evidence path");
            deleteTree(evidence); Files.createDirectories(tmp);
            List<String> command = new ArrayList<>(List.of(java(), task.source, task.argument));
            ProcessBuilder builder = isolated(config, profile, task, output, log, command).directory(root.toFile());
            builder.environment().put("JAVA_TOOL_OPTIONS", "-XX:+UseSerialGC -Xms16m -Xmx" + profile.heap);
            builder.environment().put("TEMP", tmp.toString()); builder.environment().put("TMP", tmp.toString());
            builder.environment().put("GRADLE_USER_HOME", (prebuilt == null ? output.resolve("gradle") : root.resolve(".worldline/runtime-fabric/gradle").resolve(task.id)).toString());
            builder.environment().put("WORLDLINE_RUNTIME_SLOT", task.id);
            if (prebuilt != null) { builder.environment().put("WORLDLINE_AERO_PREBUILT", prebuilt.toString()); builder.environment().put("WORLDLINE_RUNTIME_TIMEOUT_EXTRA", "300"); }
            Process process = builder.start(); boolean finished = process.waitFor(task.timeoutSeconds + 15L, TimeUnit.SECONDS);
            if (!finished) killTree(process); require(finished, "timeout after " + task.timeoutSeconds + "s");
            require(process.exitValue() == 0, "exit " + process.exitValue() + "; see " + log);
            if (Files.isDirectory(evidence)) copyTree(evidence, output.resolve("evidence"));
            System.out.println("PASS " + task.id); return new Outcome(task.id, true, "passed");
        } catch (Exception error) {
            try { if (Files.isDirectory(evidence)) copyTree(evidence, output.resolve("evidence-partial")); }
            catch (IOException copyError) { error.addSuppressed(copyError); }
            System.out.println("FAIL " + task.id + " " + error.getMessage());
            return new Outcome(task.id, false, error.getMessage());
        } finally { active.decrementAndGet(); }
    }

    private Path prebuild(List<Task> tasks, Path batch) throws Exception { Path output = batch.resolve("aero-model-lib-3.0.0.jar"); List<String> command = new ArrayList<>(List.of(java(), "tools/containers/AeroPrebuild.java", output.toString()));
        tasks.forEach(task -> command.add(task.argument)); ProcessBuilder builder = new ProcessBuilder(command).directory(root.toFile()).inheritIO(); builder.environment().put("GRADLE_USER_HOME", root.resolve(".worldline/runtime-fabric/gradle/aero-prebuild").toString()); Process process = builder.start();
        require(process.waitFor(12, TimeUnit.MINUTES) && process.exitValue() == 0, "Aero batch prebuild failed"); return output; }

    private void verify() throws Exception {
        Process process = new ProcessBuilder(java(), "tools/harness/Verify.java").directory(root.toFile()).inheritIO().start(); require(process.waitFor(10, TimeUnit.MINUTES) && process.exitValue() == 0, "host Verify failed");
    }

    private void prepareBackend(String backend) throws Exception {
        if (backend.equals("windows-job")) {
            require(isWindows(), "windows-job requires Windows");
            Process process = new ProcessBuilder(java(), "tools/containers/WindowsJobBootstrap.java", "ensure")
                    .directory(root.toFile()).inheritIO().start();
            require(process.waitFor(60, TimeUnit.SECONDS) && process.exitValue() == 0, "Windows Job launcher bootstrap failed");
        } else {
            require(!isWindows(), backend + " requires Linux");
            require(List.of("linux-cgroup", "linux-sandbox").contains(backend), "unsupported backend: " + backend);
            Process process = new ProcessBuilder("bash", "tools/containers/linux-runtime.sh", "doctor")
                    .directory(root.toFile()).inheritIO().start();
            require(process.waitFor(15, TimeUnit.SECONDS) && process.exitValue() == 0, "Linux isolation doctor failed");
        }
    }

    private ProcessBuilder isolated(Config config, Profile profile, Task task, Path output, Path log, List<String> command) {
        if (config.backend.equals("windows-job")) {
            int cpuRate = Math.max(1, (int) Math.floor(10_000.0 * profile.cpuUnits / Host.measure().cpus));
            List<String> wrapped = new ArrayList<>(List.of(root.resolve(".worldline/tools/WindowsJobRunner.exe").toString(),
                    "--memory", Long.toString(profile.memoryLimitBytes), "--cpu-rate", Integer.toString(cpuRate),
                    "--active-processes", Integer.toString(profile.processLimit), "--timeout-seconds", Integer.toString(task.timeoutSeconds),
                    "--cwd", root.toString(), "--log", log.toString(), "--metrics", output.resolve("metrics.properties").toString(), "--"));
            wrapped.addAll(command); return new ProcessBuilder(wrapped).redirectErrorStream(true)
                    .redirectOutput(output.resolve("launcher.log").toFile());
        }
        List<String> wrapped = new ArrayList<>(List.of("bash", "tools/containers/linux-runtime.sh", "run", "--mode",
                config.backend.equals("linux-sandbox") ? "sandbox" : "cgroup", "--memory", Long.toString(profile.memoryLimitBytes),
                "--cpu-percent", Integer.toString(Math.max(1, (int) Math.ceil(profile.cpuUnits * 100))),
                "--processes", Integer.toString(profile.processLimit), "--timeout-seconds", Integer.toString(task.timeoutSeconds),
                "--cwd", root.toString(), "--log", log.toString(), "--metrics", output.resolve("metrics.properties").toString(), "--"));
        wrapped.addAll(command); return new ProcessBuilder(wrapped).redirectErrorStream(true)
                .redirectOutput(output.resolve("launcher.log").toFile());
    }

    private boolean noForeignRuntime() {
        long self = ProcessHandle.current().pid();
        return ProcessHandle.allProcesses().filter(process -> process.pid() != self).noneMatch(process -> {
            String line = process.info().commandLine().orElse("").toLowerCase(Locale.ROOT).replace('\\', '/');
            return line.contains("tools/smoke/") || line.contains("runclient")
                    || line.contains("minecraft-b1.7.3-server.jar") || line.contains("minecraft-b1.7.3-client.jar");
        });
    }

    private static List<Task> parse(Path manifest, Path root) throws Exception {
        require(Files.isRegularFile(manifest), "manifest does not exist: " + manifest);
        List<Task> tasks = new ArrayList<>(); Set<String> ids = new HashSet<>(), cases = new HashSet<>();
        List<String> lines = Files.readAllLines(manifest, StandardCharsets.UTF_8);
        for (int index = 0; index < lines.size(); index++) {
            String line = lines.get(index).trim(); if (line.isEmpty() || line.startsWith("#")) continue;
            String[] fields = line.split("\\t", -1); require(fields.length == 5, "manifest line " + (index + 1) + " must have five fields");
            require(fields[0].matches("[a-z0-9][a-z0-9-]{1,63}") && ids.add(fields[0]), "invalid or duplicate id: " + fields[0]);
            require(List.of("server-headless", "windows-client-gui").contains(fields[1]), "unsupported lane: " + fields[1]);
            require(fields[2].matches("tools/smoke/[A-Za-z0-9]+Cycle\\.java"), "unsafe source: " + fields[2]);
            Path source = root.resolve(fields[2]).normalize(); require(Files.isRegularFile(source), "missing source: " + fields[2]);
            require(fields[3].matches("[a-z0-9][a-z0-9-]{1,79}") && cases.add(fields[2] + fields[3]), "invalid or duplicate case");
            require(Files.readString(source).contains("\"" + fields[3] + "\""), "argument is not declared by source: " + fields[3]);
            int timeout = Integer.parseInt(fields[4]); require(timeout >= 30 && timeout <= 3600, "unsafe timeout");
            tasks.add(new Task(fields[0], fields[1], fields[2], fields[3], timeout));
        }
        require(!tasks.isEmpty(), "manifest contains no tasks"); return List.copyOf(tasks);
    }

    private static <T> List<Outcome> bounded(List<T> inputs, int jobs, TaskRun<T> action) throws Exception {
        Semaphore permits = new Semaphore(jobs); List<Future<Outcome>> futures = new ArrayList<>();
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (T input : inputs) futures.add(executor.submit(() -> { permits.acquire();
                try { return action.run(input); } finally { permits.release(); } }));
            List<Outcome> outcomes = new ArrayList<>(); for (Future<Outcome> future : futures) outcomes.add(future.get()); return outcomes;
        }
    }

    private static void copyTree(Path source, Path target) throws IOException {
        try (Stream<Path> paths = Files.walk(source)) { for (Path path : paths.toList()) {
            Path copy = target.resolve(source.relativize(path));
            if (Files.isDirectory(path)) Files.createDirectories(copy); else Files.copy(path, copy, StandardCopyOption.REPLACE_EXISTING);
        } }
    }

    private static void deleteTree(Path target) throws IOException {
        if (!Files.exists(target)) return;
        try (Stream<Path> paths = Files.walk(target)) { for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) Files.delete(path); }
    }

    private static void killTree(Process process) throws InterruptedException {
        process.descendants().forEach(ProcessHandle::destroyForcibly); process.destroyForcibly(); process.waitFor();
    }
    private static String java() { return Path.of(System.getProperty("java.home"), "bin", "java").toString(); }
    private static boolean isWindows() { return System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("windows"); }
    private static double gib(long bytes) { return bytes / (1024.0 * 1024 * 1024); }
    private static long size(String value) { String text = value.toLowerCase(Locale.ROOT);
        require(text.matches("[1-9][0-9]*[mg]"), "size must use m or g: " + value);
        return Math.multiplyExact(Long.parseLong(text.substring(0, text.length() - 1)), text.endsWith("g") ? 1L << 30 : 1L << 20); }
    private static void require(boolean value, String message) { if (!value) throw new IllegalArgumentException(message); }

    private static void selfTest() throws Exception {
        Path root = Path.of("").toAbsolutePath().normalize();
        require(parse(root.resolve("tools/containers/smokes-25.tsv"), root).size() == 25, "bundled manifest drift");
        Config config = Config.load(root, Options.parse(new String[] {"simulate", "tools/containers/smokes-25.tsv", "--jobs", "25"}));
        Profile server = Profile.of("server-headless", config), gui = Profile.of("windows-client-gui", config);
        require(Model.of(new Host(16, 64L << 30, 48L << 30), server).safeJobs == 25, "25-job admission drift");
        require(Model.of(new Host(16, 64L << 30, 5L << 30), server).safeJobs == 2, "low-memory admission drift");
        require(gui.maxParallelism == 3 && gui.workerBytes == 3L << 30 && gui.cpuUnits == 2.0,
                "GUI profile drift");
        for (int width : List.of(10, 25)) concurrencyTest(width);
        System.out.println("host smoke pool self-test passed");
    }

    private static void concurrencyTest(int width) throws Exception {
        AtomicInteger active = new AtomicInteger(), peak = new AtomicInteger(); List<Integer> inputs = new ArrayList<>();
        for (int i = 0; i < 25; i++) inputs.add(i); var gate = new java.util.concurrent.CountDownLatch(width);
        List<Outcome> outcomes = bounded(inputs, width, ignored -> { int value = active.incrementAndGet(); peak.accumulateAndGet(value, Math::max);
            gate.countDown(); gate.await(); active.decrementAndGet(); return new Outcome("test", true, "passed"); });
        require(outcomes.size() == 25 && peak.get() == width, "bounded peak drift: " + peak.get());
    }

    private record Task(String id, String lane, String source, String argument, int timeoutSeconds) { }
    private record Outcome(String id, boolean passed, String message) { }
    private record Host(int cpus, long totalMemory, long freeMemory) {
        static Host measure() { var os = (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
            return new Host(Runtime.getRuntime().availableProcessors(), os.getTotalMemorySize(), os.getFreeMemorySize()); }
    }
    private record Model(int safeJobs) { static Model of(Host host, Profile profile) {
        long usable = Math.max(0, host.freeMemory - profile.reserveBytes);
        int memory = (int) Math.max(1, usable / profile.workerBytes), cpu = (int) Math.max(1, Math.floor(host.cpus / profile.cpuUnits));
        return new Model(Math.min(profile.maxParallelism, Math.min(memory, cpu))); } }
    private record Profile(String lane, String heap, long workerBytes, long memoryLimitBytes, double cpuUnits, int processLimit, long reserveBytes, int durationSeconds, int maxParallelism) {
        static Profile of(String lane, Config config) { if (lane.equals("server-headless")) return new Profile(lane,
                config.heap, config.workerBytes, config.memoryLimitBytes, config.cpuUnits, config.processLimit,
                config.reserveBytes, config.durationSeconds, config.maxParallelism);
            require(lane.equals("windows-client-gui"), "unsupported lane: " + lane); return new Profile(lane, "512m", 3L << 30, 6L << 30, 2.0, 96, config.reserveBytes, 1800, 3); }
    }
    private record Config(int parallelism, int maxParallelism, String backend, String heap, long workerBytes,
                          long memoryLimitBytes, double cpuUnits, int processLimit,
                          long reserveBytes, int durationSeconds, String lock) {
        static Config load(Path root, Options options) throws Exception {
            Properties values = new Properties(); load(root.resolve("tools/containers/host-pool.properties"), values, true);
            load(root.resolve(".worldline/host-pool.properties"), values, false);
            if (options.config != null) load(options.config, values, true);
            String jobs = options.jobs != null ? options.jobs : System.getenv().getOrDefault("WORLDLINE_SMOKE_JOBS", values.getProperty("parallelism"));
            int parallelism = jobs.equals("auto") ? 0 : Integer.parseInt(jobs), max = Integer.parseInt(required(values, "max.parallelism"));
            require(parallelism >= 0 && parallelism <= max && max <= 25, "parallelism must be auto or 1.." + max);
            String heap = required(values, "worker.heap"); long heapBytes = size(heap);
            String backend = options.backend != null ? options.backend : required(values, "backend");
            if (backend.equals("auto")) backend = isWindows() ? "windows-job" : "linux-cgroup";
            String lock = options.lock != null ? options.lock : values.getProperty("runtime.lock.path", "").trim();
            long worker = size(required(values, "worker.memory.estimate")); double cpu = Double.parseDouble(required(values, "worker.cpu.units"));
            long limit = size(required(values, "worker.memory.limit")); int processes = Integer.parseInt(required(values, "worker.process.limit"));
            int duration = Integer.parseInt(required(values, "task.duration.seconds"));
            require(max >= 1 && worker >= heapBytes * 2 + (64L << 20), "worker estimate must cover two heaps plus 64m");
            require(limit >= worker && processes >= 4 && processes <= 512, "unsafe worker hard limits");
            require(cpu >= 0.1 && cpu <= 4.0 && duration >= 30 && duration <= 3600, "unsafe CPU or duration config");
            return new Config(parallelism, max, backend, heap, worker, limit, cpu, processes,
                    size(required(values, "host.memory.reserve")), duration, lock);
        }
        Path lockPath(Path root) { return lock.isBlank() ? Path.of(System.getProperty("user.home"), ".worldline/official-runtime.lock")
                : (Path.of(lock).isAbsolute() ? Path.of(lock) : root.resolve(lock)).normalize(); }
        private static void load(Path path, Properties values, boolean required) throws IOException { if (!Files.isRegularFile(path)) {
                if (required) throw new IllegalArgumentException("missing config: " + path); return; }
            try (var reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) { values.load(reader); } }
        private static String required(Properties values, String key) { String value = values.getProperty(key);
            require(value != null && !value.isBlank(), "missing config value: " + key); return value.trim(); }
    }
    private record Options(String action, Path manifest, String jobs, Path config, String lock, String backend, boolean skipVerify) {
        static Options parse(String[] arguments) { require(arguments.length >= 2 && List.of("simulate", "run").contains(arguments[0]),
                    "usage: java tools/containers/HostSmokePool.java simulate|run MANIFEST [--jobs auto|1..25]"
                            + " [--config FILE] [--lock FILE] [--skip-verify]");
            String jobs = null, lock = null, backend = null; Path config = null; boolean skip = false;
            for (int i = 2; i < arguments.length; i++) switch (arguments[i]) {
                case "--jobs" -> jobs = next(arguments, ++i); case "--config" -> config = Path.of(next(arguments, ++i)).toAbsolutePath();
                case "--lock" -> lock = next(arguments, ++i); case "--skip-verify" -> skip = true;
                case "--backend" -> backend = next(arguments, ++i);
                default -> throw new IllegalArgumentException("unknown option: " + arguments[i]); }
            return new Options(arguments[0], Path.of(arguments[1]).toAbsolutePath().normalize(), jobs, config, lock, backend, skip); }
        private static String next(String[] values, int index) { require(index < values.length && !values[index].startsWith("--"),
                    "missing option value"); return values[index]; }
    }
    @FunctionalInterface private interface TaskRun<T> { Outcome run(T input) throws Exception; }
}
