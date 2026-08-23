import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/** Validates a pool capability before one Gate-owned runtime-only milestone execution. */
final class PooledSmokeCheck {
    private PooledSmokeCheck() {}

    static void execute(String id) throws Exception {
        Path root = Path.of("").toAbsolutePath().normalize(); Properties lease = verify(root);
        SmokeGitState state = SmokeGitState.read(root);
        require(state.clean() && state.head().equals(lease.getProperty("head"))
                && state.tree().equals(lease.getProperty("tree")),
                "pooled worker tree differs from its lease");
        SmokeDiscovery.Entry smoke = SmokeDiscovery.require(root, id);
        MilestoneContract contract = new MilestoneContract(root, id, root.resolve(".worldline/build"));
        contract.validate(); SmokeReceiptCache cache = new SmokeReceiptCache(root);
        require(cache.availablePin(smoke) == null, "pooled smoke already has a current proof: " + id);
        String fingerprint = cache.fingerprint(smoke); long duration = SmokeExecution.run(root, smoke);
        contract.validateEvidence(root.resolve(".worldline/smoke-logs").resolve(id + ".log"));
        cache.passed(smoke, fingerprint, duration);
        System.out.println("pooled milestone passed: " + id + " fingerprint=" + fingerprint);
    }

    private static Properties verify(Path root) throws Exception {
        String requested = requiredEnvironment("WORLDLINE_RUNTIME_POOL_FILE");
        String secret = requiredEnvironment("WORLDLINE_RUNTIME_POOL_TOKEN");
        Path directory = root.resolve(".worldline/runtime-fabric").normalize();
        Path file = Path.of(requested).toAbsolutePath().normalize();
        require(file.startsWith(directory) && Files.isRegularFile(file), "invalid pool lease file");
        Properties values = new Properties(); try (var reader = Files.newBufferedReader(
                file, StandardCharsets.UTF_8)) { values.load(reader); }
        require("1".equals(values.getProperty("schema")) && secret.equals(values.getProperty("secret"))
                && root.toString().equals(values.getProperty("root")), "pool lease identity mismatch");
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
                || command.contains("ContainerSmokePool")), "pool lease owner is not active");
        return values;
    }

    private static String requiredEnvironment(String name) {
        String value = System.getenv(name);
        require(value != null && !value.isBlank(), "missing " + name); return value;
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
