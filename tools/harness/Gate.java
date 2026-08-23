import java.io.IOException;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Cross-platform, cross-worktree entry point for every repository gate. */
public final class Gate {
    private static final String ACTIVE = "WORLDLINE_GATE_ACTIVE";
    private static final String RUNTIME_LEASE = "WORLDLINE_RUNTIME_LEASE";
    private final Path root = Paths.get("").toAbsolutePath().normalize();
    private final Path control = controlDirectory();
    private final long waitMillis = waitMillis();

    private Gate() {}

    public static void main(String[] arguments) {
        try {
            if (Arrays.equals(arguments, new String[] {"--self-test"})) {
                selfTest();
                return;
            }
            if (arguments.length == 3 && "--hold".equals(arguments[0])) {
                hold(Path.of(arguments[1]), Long.parseLong(arguments[2]));
                return;
            }
            new Gate().execute(arguments);
        } catch (Exception error) {
            System.err.println("gate failed: " + error.getMessage());
            System.exit(1);
        }
    }

    private void execute(String[] arguments) throws Exception {
        validate(arguments);
        Files.createDirectories(control);
        if (arguments.length == 2 && ("--milestone".equals(arguments[0])
                || "--smoke-id".equals(arguments[0]))) {
            executeMilestone(arguments[1]);
            return;
        }
        boolean smoke = arguments.length > 0 && "--smoke".equals(arguments[0]);
        executePhase(arguments, smoke, true, smoke);
    }

    private void executeMilestone(String id) throws Exception {
        executePhase(new String[] {"--milestone-static", id}, false, true, false);
        executePhase(new String[] {"--milestone-runtime", id}, milestoneUsesOfficialRuntime(id), false, true);
    }

    private void executePhase(String[] arguments, boolean runtime, boolean useSlot, boolean runtimeLease)
            throws Exception {
        Lease runtimeLock = null;
        Lease compatibilityLock = null;
        Lease slot = null;
        Lease worktree = null;
        try {
            worktree = acquire(root.resolve(".worldline/verify.lock"), "worktree");
            if (useSlot) slot = acquireSlot();
            if (runtime) {
                runtimeLock = acquire(control.resolve("official-b173.lock"), "runtime");
                Path legacy = legacyRuntimeLock();
                if (legacy != null && !legacy.equals(control.resolve("official-b173.lock")))
                    compatibilityLock = acquire(legacy, "legacy-runtime");
            }
            Path classes = compileHarness();
            int exit = launchVerify(classes, arguments, runtimeLease);
            if (exit != 0) System.exit(exit);
        } finally {
            close(worktree); close(slot); close(compatibilityLock); close(runtimeLock);
        }
    }

    private boolean milestoneUsesOfficialRuntime(String id) throws IOException {
        Path path = root.resolve("smokes").resolve(id).resolve("smoke.properties");
        Properties descriptor = new Properties();
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            descriptor.load(reader);
        }
        return !"tooling-cycle".equals(descriptor.getProperty("qualification.proof"));
    }

    private void validate(String[] arguments) {
        boolean profile = arguments.length == 0
                || Arrays.equals(arguments, new String[] {"--runtime"})
                || Arrays.equals(arguments, new String[] {"--smoke"})
                || Arrays.equals(arguments, new String[] {"--orchestrator"})
                || Arrays.equals(arguments, new String[] {"--pin-smokes"})
                || Arrays.equals(arguments, new String[] {"--smoke-plan"})
                || Arrays.equals(arguments, new String[] {"--accept-legacy-smoke-baseline"})
                || arguments.length == 2 && ("--candidate".equals(arguments[0])
                        || "--milestone".equals(arguments[0]) || "--smoke-id".equals(arguments[0]));
        if (!profile) throw new IllegalArgumentException(
                "usage: java tools/harness/Gate.java "
                + "[--runtime|--smoke|--pin-smokes|--accept-legacy-smoke-baseline|--orchestrator|"
                + "--smoke-plan|--milestone ID|--candidate ID|--self-test]");
        if (arguments.length == 2 && !arguments[1].matches("[a-z0-9]+(?:-[a-z0-9]+)*"))
            throw new IllegalArgumentException("invalid milestone id: " + arguments[1]);
    }

    private Lease acquireSlot() throws Exception {
        int configured = integerEnvironment("WORLDLINE_VERIFY_SLOTS",
                Math.max(1, Math.min(4, Runtime.getRuntime().availableProcessors() / 2)));
        if (configured < 1 || configured > 32) throw new IllegalArgumentException(
                "WORLDLINE_VERIFY_SLOTS must be between 1 and 32");
        long deadline = System.currentTimeMillis() + waitMillis;
        boolean announced = false;
        while (System.currentTimeMillis() < deadline) {
            for (int index = 0; index < configured; index++) {
                Lease lease = tryAcquire(control.resolve("verify-slot-" + index + ".lock"), "slot-" + index);
                if (lease != null) return lease;
            }
            if (!announced) {
                System.out.println("  gate: waiting for one of " + configured + " verify slots");
                announced = true;
            }
            Thread.sleep(200L);
        }
        throw new IllegalStateException("timed out waiting for a verify slot in " + control);
    }

    private Lease acquire(Path path, String kind) throws Exception {
        long deadline = System.currentTimeMillis() + waitMillis;
        boolean announced = false;
        while (System.currentTimeMillis() < deadline) {
            Lease lease = tryAcquire(path, kind);
            if (lease != null) return lease;
            if (!announced) {
                System.out.println("  gate: waiting for " + kind + " lock " + path);
                announced = true;
            }
            Thread.sleep(200L);
        }
        throw new IllegalStateException("timed out waiting for " + kind + " lock " + path);
    }

    private Lease tryAcquire(Path path, String kind) throws IOException {
        Files.createDirectories(path.toAbsolutePath().normalize().getParent());
        FileChannel channel = FileChannel.open(path, StandardOpenOption.CREATE,
                StandardOpenOption.READ, StandardOpenOption.WRITE);
        try {
            FileLock lock = channel.tryLock();
            if (lock == null) { channel.close(); return null; }
            String metadata = "kind=" + kind + "\npid=" + ProcessHandle.current().pid()
                    + "\nstarted=" + Instant.now() + "\nroot=" + root + "\n";
            channel.truncate(0); channel.position(0);
            channel.write(ByteBuffer.wrap(metadata.getBytes(StandardCharsets.UTF_8))); channel.force(true);
            return new Lease(channel, lock, path);
        } catch (OverlappingFileLockException error) {
            channel.close(); return null;
        } catch (IOException | RuntimeException error) {
            try { channel.close(); } catch (IOException close) { error.addSuppressed(close); }
            throw error;
        }
    }

    private Path compileHarness() throws Exception {
        Path sourceRoot = root.resolve("tools/harness");
        List<Path> sources;
        try (Stream<Path> stream = Files.list(sourceRoot)) {
            sources = stream.filter(path -> path.toString().endsWith(".java"))
                    .sorted().collect(Collectors.toList());
        }
        Path output = root.resolve(".worldline/gate/classes");
        Path marker = output.getParent().resolve("sources.sha256");
        String digest = digest(sources);
        if (Files.isRegularFile(output.resolve("RepositoryVerify.class")) && Files.isRegularFile(marker)
                && Files.readString(marker, StandardCharsets.UTF_8).trim().equals(digest)) return output;
        recreate(output);
        List<String> command = new ArrayList<>(List.of(javaTool("javac"), "-encoding", "UTF-8",
                "--release", "21", "-Xlint:all,-options", "-Werror", "-d", output.toString()));
        sources.forEach(path -> command.add(path.toString()));
        run(command, root, 180);
        Files.writeString(marker, digest + "\n", StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        return output;
    }

    private int launchVerify(Path classes, String[] arguments, boolean runtime) throws Exception {
        List<String> command = new ArrayList<>(List.of(
                javaTool("java"), "-cp", classes.toString(), "RepositoryVerify"));
        command.addAll(Arrays.asList(arguments));
        ProcessBuilder builder = new ProcessBuilder(command).directory(root.toFile()).inheritIO();
        builder.environment().put(ACTIVE, "true");
        builder.environment().put("WORLDLINE_HARNESS_CP", classes.toString());
        builder.environment().put("WORLDLINE_GATE_CONTROL", control.toString());
        if (runtime) builder.environment().put(RUNTIME_LEASE, Long.toString(ProcessHandle.current().pid()));
        return waitFor(builder.start(), 0);
    }

    private static void selfTest() throws Exception {
        Path directory = Files.createTempDirectory("worldline-gate-");
        Path lock = directory.resolve("exclusive.lock");
        Path source = Path.of("tools/harness/Gate.java").toAbsolutePath().normalize();
        String java = javaTool("java");
        String classpath = System.getenv("WORLDLINE_HARNESS_CP");
        List<String> command = classpath == null || classpath.isBlank()
                ? List.of(java, source.toString(), "--hold", lock.toString(), "450")
                : List.of(java, "-cp", classpath, "Gate", "--hold", lock.toString(), "450");
        long started = System.nanoTime();
        Process first = new ProcessBuilder(command).start();
        Process second = new ProcessBuilder(command).start();
        int one = waitFor(first, 15), two = waitFor(second, 15);
        long elapsed = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started);
        if (one != 0 || two != 0 || elapsed < 800L)
            throw new IllegalStateException("exclusive lock self-test failed; elapsed=" + elapsed);
        Files.deleteIfExists(lock); Files.deleteIfExists(directory);
        System.out.println("Gate self-test passed; serialized elapsed=" + elapsed + "ms");
    }

    private static void hold(Path path, long millis) throws Exception {
        Files.createDirectories(path.toAbsolutePath().normalize().getParent());
        try (FileChannel channel = FileChannel.open(path, StandardOpenOption.CREATE,
                StandardOpenOption.WRITE); FileLock lock = channel.lock()) {
            if (!lock.isValid()) throw new IllegalStateException("self-test lock is invalid");
            Thread.sleep(millis);
        }
    }

    private static int waitFor(Process process, int timeoutSeconds) throws Exception {
        try {
            if (timeoutSeconds == 0) return process.waitFor();
            if (process.waitFor(timeoutSeconds, TimeUnit.SECONDS)) return process.exitValue();
            destroyTree(process); throw new IllegalStateException("process timed out after " + timeoutSeconds + "s");
        } catch (InterruptedException error) {
            destroyTree(process); Thread.currentThread().interrupt(); throw error;
        }
    }

    private static void run(List<String> command, Path directory, int timeout) throws Exception {
        Process process = new ProcessBuilder(command).directory(directory.toFile()).inheritIO().start();
        int exit = waitFor(process, timeout);
        if (exit != 0) throw new IllegalStateException(command.get(0) + " exited " + exit);
    }

    private static void destroyTree(Process process) {
        process.descendants().sorted(Comparator.comparingLong(ProcessHandle::pid).reversed())
                .forEach(ProcessHandle::destroyForcibly);
        process.destroyForcibly();
    }

    private String digest(List<Path> sources) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.update(System.getProperty("java.version").getBytes(StandardCharsets.UTF_8));
        for (Path source : sources) {
            digest.update(root.relativize(source).toString().replace('\\', '/')
                    .getBytes(StandardCharsets.UTF_8));
            digest.update(Files.readAllBytes(source));
        }
        return HexFormat.of().formatHex(digest.digest());
    }

    private static Path controlDirectory() {
        String override = System.getenv("WORLDLINE_CONTROL_DIR");
        if (override != null && !override.isBlank()) return Path.of(override).toAbsolutePath().normalize();
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        String base = os.contains("win") ? System.getenv("LOCALAPPDATA") : System.getenv("XDG_RUNTIME_DIR");
        if (base == null || base.isBlank()) base = System.getProperty("java.io.tmpdir");
        return Path.of(base).toAbsolutePath().normalize().resolve("worldline/locks");
    }

    private Path legacyRuntimeLock() {
        String override = System.getenv("WORLDLINE_RUNTIME_LOCK");
        if (override != null && !override.isBlank()) return Path.of(override).toAbsolutePath().normalize();
        for (Path ancestor = root; ancestor != null; ancestor = ancestor.getParent()) {
            Path candidate = ancestor.resolve("worldline-swarm-control/official-runtime.lock");
            if (Files.isRegularFile(candidate)) return candidate.toAbsolutePath().normalize();
        }
        return null;
    }

    private static long waitMillis() {
        return TimeUnit.SECONDS.toMillis(integerEnvironment("WORLDLINE_GATE_WAIT_SECONDS", 7200));
    }

    private static int integerEnvironment(String name, int fallback) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) return fallback;
        try { return Integer.parseInt(value); }
        catch (NumberFormatException error) { throw new IllegalArgumentException(name + " must be an integer"); }
    }

    private static String javaTool(String name) {
        boolean windows = System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win");
        return Path.of(System.getProperty("java.home"), "bin", name + (windows ? ".exe" : "")).toString();
    }

    private static void recreate(Path target) throws IOException {
        if (Files.exists(target)) try (Stream<Path> paths = Files.walk(target)) {
            for (Path path : paths.sorted(Comparator.reverseOrder()).collect(Collectors.toList())) Files.delete(path);
        }
        Files.createDirectories(target);
    }

    private static void close(Lease lease) {
        if (lease != null) lease.close();
    }

    private static final class Lease implements AutoCloseable {
        private final FileChannel channel; private final FileLock lock; private final Path path;
        Lease(FileChannel channel, FileLock lock, Path path) {
            this.channel = channel; this.lock = lock; this.path = path;
        }
        @Override public void close() {
            try { lock.release(); channel.close(); }
            catch (IOException error) { throw new IllegalStateException("could not release lock " + path, error); }
        }
    }
}
