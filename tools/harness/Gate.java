import java.io.IOException;
import java.io.Reader;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
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
    private static final String INTERNAL = "WORLDLINE_GATE_INTERNAL";
    private static final String RUNTIME_LEASE = "WORLDLINE_RUNTIME_LEASE";
    private final Path root = Paths.get("").toAbsolutePath().normalize();
    private final Path control = controlDirectory();
    private final long waitMillis = waitMillis();

    private Gate() {}

    public static void main(String[] arguments) {
        try {
            if (Arrays.equals(arguments, new String[] {"--self-test"})) { selfTest(); return; }
            if (arguments.length > 0 && "--internal".equals(arguments[0])) {
                if (!"true".equals(System.getenv(INTERNAL)))
                    throw new IllegalStateException("internal Gate phase is not authorized");
                new Gate().executeInside(Arrays.copyOfRange(arguments, 1, arguments.length)); return;
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
        if (arguments.length == 2 && "--new-milestone".equals(arguments[0])) {
            Path classes = compileHarness();
            int exit = waitFor(new ProcessBuilder(javaTool("java"), "-cp", classes.toString(),
                    "MilestoneScaffold", arguments[1]).directory(root.toFile()).inheritIO().start(), 60);
            if (exit != 0) System.exit(exit);
            return;
        }
        if (arguments.length == 1 && maintenance(arguments[0]) != null) {
            String[] spec = maintenance(arguments[0]); Path classes = compileHarness();
            ProcessBuilder builder = new ProcessBuilder(javaTool("java"), "-cp", classes.toString(),
                    spec[0], spec[1]).directory(root.toFile()).inheritIO();
            if (spec[0].equals("SharedCacheMaintenance"))
                builder.environment().put("WORLDLINE_GATE_CONTROL", control.toString());
            int exit = waitFor(builder.start(), Integer.parseInt(spec[2]));
            if (exit != 0) System.exit(exit); return;
        }
        Files.createDirectories(control);
        if (arguments.length == 2 && ("--milestone".equals(arguments[0])
                || "--smoke-id".equals(arguments[0]))) {
            executeMilestone(arguments[1]);
            return;
        }
        if (arguments.length == 2 && "--lane-differential".equals(arguments[0])) {
            executePhase(arguments, true, false, true); return;
        }
        boolean smoke = arguments.length > 0 && "--smoke".equals(arguments[0]);
        boolean pinnedSmoke = arguments.length > 0 && "--pinned-smoke".equals(arguments[0]);
        executePhase(arguments, smoke, true, smoke && !pinnedSmoke);
    }

    private void executeMilestone(String id) throws Exception {
        if (System.getenv("WORLDLINE_RUNTIME_POOL_FILE") != null) { Path classes = compileHarness();
            int exit = launchVerify(classes, new String[] {"--pooled-smoke", id}, true);
            if (exit != 0) System.exit(exit); return; }
        executePhase(new String[] {"--milestone-static", id}, false, true, false);
        executePhase(new String[] {"--milestone-runtime", id}, milestoneUsesOfficialRuntime(id), false, true);
    }

    private static String[] maintenance(String value) { return switch (value) {
        case "--migrate-data-cycles" -> m("DataDrivenCycleMigration", "--apply", 300);
        case "--refresh-data-cycle-pins" -> m("DataDrivenCycleMigration", "--refresh", 300);
        case "--migrate-composite-cycles" -> m("CompositeCycleMigration", "--apply", 300);
        case "--migrate-telemetry-pins" -> m("TelemetryPinMigration", "--apply", 300);
        case "--migrate-repository-schemas" -> m("RepositorySchemaMigration", "--apply", 600);
        case "--migrate-formatting-pins" -> m("FormattingPinMigration", "--apply", 600);
        case "--migrate-shared-helper-pins" -> m("SharedHelperPinMigration", "--apply", 600);
        case "--migrate-adapter-split-pins" -> m("AdapterSplitPinMigration", "--apply", 600);
        case "--migrate-provider-discovery-pins" -> m("ProviderDiscoveryPinMigration", "--apply", 600);
        case "--migrate-gui-workbench-pins" -> m("GuiWorkbenchPinMigration", "--apply", 600);
        case "--seal-lane-portability" -> m("LaneDifferential", "--seal", 60);
        case "--module-cache-doctor", "--cache-doctor" -> m("SharedCacheMaintenance", "doctor", 600);
        case "--module-cache-gc", "--cache-gc" -> m("SharedCacheMaintenance", "gc", 600);
        default -> null; }; }
    private static String[] m(String t, String a, int s) {
        return new String[] {t, a, Integer.toString(s)}; }

    private void executePhase(String[] arguments, boolean runtime, boolean useSlot, boolean runtimeLease)
            throws Exception {
        int slots = useSlot ? verifySlots() : 0;
        Path legacy = runtime ? legacyRuntimeLock() : null;
        List<String> command = new ArrayList<>(List.of(javaTool("java"),
                root.resolve("tools/harness/FairLeaseCommand.java").toString(), root.toString(),
                control.toString(), Long.toString(waitMillis), Integer.toString(slots),
                Boolean.toString(runtime), Boolean.toString(runtimeLease),
                legacy == null ? "-" : legacy.toString()));
        command.addAll(Arrays.asList(arguments));
        int exit = waitFor(new ProcessBuilder(command).directory(root.toFile()).inheritIO().start(), 0);
        if (exit != 0) System.exit(exit);
    }

    private void executeInside(String[] arguments) throws Exception {
        Path classes = compileHarness();
        int exit = launchVerify(classes, arguments, false);
        if (exit != 0) System.exit(exit);
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
                || Arrays.equals(arguments, new String[] {"--pinned-smoke"})
                || Arrays.equals(arguments, new String[] {"--accept-legacy-smoke-baseline"})
                || arguments.length == 1 && maintenance(arguments[0]) != null
                || arguments.length == 2 && ("--new-milestone".equals(arguments[0])
                        || "--candidate".equals(arguments[0])
                        || "--lane-differential".equals(arguments[0])
                        || "--milestone".equals(arguments[0]) || "--smoke-id".equals(arguments[0]));
        if (!profile) throw new IllegalArgumentException(
                "usage: java tools/harness/Gate.java "
                + "[--runtime|--smoke|--pin-smokes|--accept-legacy-smoke-baseline|--orchestrator|"
                + "--smoke-plan|--pinned-smoke|--migrate-data-cycles|--refresh-data-cycle-pins|"
                + "--migrate-composite-cycles|"
                + "--migrate-telemetry-pins|"
                + "--migrate-repository-schemas|"
                + "--migrate-formatting-pins|"
                + "--migrate-shared-helper-pins|"
                + "--migrate-adapter-split-pins|"
                + "--migrate-provider-discovery-pins|"
                + "--migrate-gui-workbench-pins|"
                + "--seal-lane-portability|"
                + "--module-cache-doctor|--module-cache-gc|--cache-doctor|--cache-gc|"
                + "--new-milestone ID|--milestone ID|--lane-differential ID|"
                + "--candidate ID|--self-test]");
        if (arguments.length == 2 && !arguments[1].matches("[a-z0-9]+(?:-[a-z0-9]+)*"))
            throw new IllegalArgumentException("invalid milestone id: " + arguments[1]);
    }

    private int verifySlots() {
        int configured = integerEnvironment("WORLDLINE_VERIFY_SLOTS",
                Math.max(1, Math.min(4, Runtime.getRuntime().availableProcessors() / 2)));
        if (configured < 1 || configured > 32) throw new IllegalArgumentException(
                "WORLDLINE_VERIFY_SLOTS must be between 1 and 32");
        return configured;
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

}
