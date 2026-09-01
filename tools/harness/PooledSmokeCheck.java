import java.nio.channels.FileChannel;
import java.nio.channels.OverlappingFileLockException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Properties;

/** Validates a pool capability before one Gate-owned runtime-only milestone execution. */
final class PooledSmokeCheck {
    private PooledSmokeCheck() {}

    static void selfTest() throws Exception {
        Path path = Files.createTempFile("worldline-pooled-lock-", ".lock");
        try (FileChannel channel = FileChannel.open(path, StandardOpenOption.READ,
                StandardOpenOption.WRITE); var lock = channel.lock()) {
            require(lock.isValid() && lockHeld(path), "held runtime lock was not observed");
        }
        require(!lockHeld(path), "released runtime lock remained held");
        require(descendsFrom(ProcessHandle.current(), ProcessHandle.current().pid()),
                "process ancestry identity drift");
        Files.delete(path);
    }

    static void execute(String id) throws Exception {
        Path root = Path.of("").toAbsolutePath().normalize(); Properties lease = verify(root);
        boolean differential = "executor-differential".equals(lease.getProperty("purpose"));
        SmokeGitState state = SmokeGitState.read(root);
        require(state.clean() && state.head().equals(lease.getProperty("head"))
                && state.tree().equals(lease.getProperty("tree")),
                "pooled worker tree differs from its lease");
        SmokeDiscovery.Entry smoke = SmokeDiscovery.require(root, id);
        MilestoneContract contract = new MilestoneContract(root, id, root.resolve(".worldline/build"));
        contract.validate(); SmokeReceiptCache cache = new SmokeReceiptCache(root);
        require(differential || cache.availablePin(smoke) == null,
                "pooled smoke already has a current proof: " + id);
        String fingerprint = cache.fingerprint(smoke);
        SmokeObservationCache observations = new SmokeObservationCache(root);
        String observationFingerprint = observations.fingerprint(smoke);
        SmokeObservationCache.Observation restored = observations.restore(smoke,
                observationFingerprint);
        long duration;
        if (restored == null) {
            duration = SmokeExecution.run(root, smoke);
            observations.observed(smoke, observationFingerprint, duration);
        } else {
            duration = restored.duration();
            System.out.println("  runtime observation restored: " + id);
        }
        contract.validateEvidence(root.resolve(".worldline/smoke-logs").resolve(id + ".log"));
        cache.passed(smoke, fingerprint, duration);
        System.out.println("pooled milestone passed: " + id + " fingerprint=" + fingerprint);
    }

    private static Properties verify(Path root) throws Exception {
        String requested = requiredEnvironment("WORLDLINE_RUNTIME_POOL_FILE");
        Path directory = root.resolve(".worldline/runtime-fabric").normalize();
        Path file = Path.of(requested).toAbsolutePath().normalize();
        require(file.startsWith(directory) && Files.isRegularFile(file), "invalid pool lease file");
        Properties values = new Properties(); try (var reader = Files.newBufferedReader(
                file, StandardCharsets.UTF_8)) { values.load(reader); }
        require("1".equals(values.getProperty("schema"))
                && root.toString().equals(values.getProperty("root")), "pool lease identity mismatch");
        boolean differential = "executor-differential".equals(values.getProperty("purpose"));
        if (differential) verifyDifferential(values);
        else require(requiredEnvironment("WORLDLINE_RUNTIME_POOL_TOKEN")
                .equals(values.getProperty("secret")), "pool lease secret mismatch");
        if ("docker".equals(values.getProperty("backend"))) {
            require("1".equals(System.getenv("WORLDLINE_CONTAINER_ISOLATED"))
                    && Files.isRegularFile(Path.of("/.dockerenv")),
                    "Docker pool token used outside an isolated container");
            require(requiredEnvironment("WORLDLINE_CONTAINER_IMAGE_ID").equals(
                    values.getProperty("image.id")), "container image identity mismatch");
            require(requiredEnvironment("WORLDLINE_CONTAINER_HEAD").equals(values.getProperty("head"))
                    && requiredEnvironment("WORLDLINE_CONTAINER_TREE").equals(values.getProperty("tree")),
                    "container commit identity mismatch");
            return values;
        }
        String parent = values.getProperty("parent.pid", ""); require(parent.matches("[0-9]+"),
                "invalid pool parent");
        ProcessHandle owner = ProcessHandle.of(Long.parseLong(parent)).orElseThrow();
        String command = owner.info().commandLine().orElse("").replace('\\', '/');
        require(owner.isAlive() && (command.contains("HostSmokePool")
                || command.contains("ContainerSmokePool")
                || differential && descendsFrom(owner,
                        Long.parseLong(values.getProperty("lease.pid", "-1")))),
                "pool lease owner is not active");
        return values;
    }

    private static void verifyDifferential(Properties values) {
        require("1".equals(requiredEnvironment("WORLDLINE_RUNTIME_DIFFERENTIAL")),
                "executor differential capability is absent");
        String lease = requiredEnvironment("WORLDLINE_RUNTIME_PARENT_LEASE");
        require(lease.matches("[0-9]+") && lease.equals(values.getProperty("lease.pid")),
                "executor differential runtime lease mismatch");
        ProcessHandle owner = ProcessHandle.of(Long.parseLong(lease)).orElseThrow();
        String started = requiredEnvironment("WORLDLINE_RUNTIME_PARENT_STARTED");
        require(started.matches("[0-9]+")
                && started.equals(values.getProperty("lease.started"))
                && owner.info().startInstant().orElseThrow().toEpochMilli() == Long.parseLong(started),
                "executor differential runtime lease start mismatch");
        Path lock = Path.of(values.getProperty("lock.path", "")).toAbsolutePath().normalize();
        require(owner.isAlive() && "official-b173.lock".equals(lock.getFileName().toString())
                        && lockHeld(lock),
                "canonical runtime lease owner is not active");
    }

    private static boolean lockHeld(Path path) {
        if (!Files.isRegularFile(path)) return false;
        try (FileChannel channel = FileChannel.open(path, StandardOpenOption.READ,
                StandardOpenOption.WRITE)) {
            try (var lock = channel.tryLock()) { return lock == null; }
        } catch (OverlappingFileLockException error) { return true; }
        catch (java.io.IOException error) { return false; }
    }

    private static boolean descendsFrom(ProcessHandle process, long ancestorPid) {
        ProcessHandle current = process;
        while (true) {
            if (current.pid() == ancestorPid) return true;
            current = current.parent().orElse(null);
            if (current == null) return false;
        }
    }

    private static String requiredEnvironment(String name) {
        String value = System.getenv(name);
        require(value != null && !value.isBlank(), "missing " + name); return value;
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
