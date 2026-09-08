import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** End-to-end non-authoritative shadow of the normal S3-compatible miss path. */
public final class ModuleCacheS3Shadow {
    private ModuleCacheS3Shadow() { }

    public static void main(String[] arguments) {
        try {
            require(arguments.length == 0, "usage: ModuleCacheS3Shadow");
            execute(Path.of("").toAbsolutePath().normalize());
        } catch (Exception error) {
            System.err.println("S3 module cache shadow failed: " + error.getMessage()); System.exit(1);
        }
    }

    private static void execute(Path root) throws Exception {
        Path linked = root.resolve(".worldline/build/classes/api");
        require(Files.isDirectory(linked), "run the canonical Gate before the S3 module shadow");
        Path entry = linked.toRealPath();
        require(Files.isRegularFile(entry.resolve(".complete")), "api module cache entry is incomplete");
        Path staging = root.resolve(".worldline/cas-shadow/s3-"
                + ProcessHandle.current().pid() + "-" + System.nanoTime()).toAbsolutePath().normalize();
        try (ModuleCacheS3Fixture fixture = new ModuleCacheS3Fixture(
                "us-east-1", "SHADOWACCESS", "shadow-secret")) {
            ModuleCachePublication.Prepared prepared = ModuleCachePublication.prepare(root, staging,
                    List.of(new ModuleCachePublication.Module("api", entry)), List.of());
            ModuleCachePublication.PublishedModule published = prepared.modules().get(0);
            KeyPair keys = KeyPairGenerator.getInstance("Ed25519").generateKeyPair();
            Path trust = staging.resolve("modules.trust"); ModuleCacheIndexTrust.sign(
                    prepared.index().path(), "s3-shadow", keys, trust);
            Map<String, String> environment = new HashMap<>(fixture.environment());
            environment.put("WORLDLINE_MODULE_CAS_MODE", "s3");
            environment.put("WORLDLINE_MODULE_CAS_INDEX", prepared.index().path().toString());
            environment.put("WORLDLINE_MODULE_CAS_INDEX_TRUST", trust.toString());
            environment.put("WORLDLINE_MODULE_CAS_PUBLIC_KEY",
                    Base64.getEncoder().encodeToString(keys.getPublic().getEncoded()));
            environment.put("WORLDLINE_MODULE_CAS_CHANNEL", "s3-shadow");
            ModuleCacheS3 backend = ModuleCacheS3.connect(ModuleCacheS3Config.from(environment),
                    prepared.index().path(), trust, environment.get("WORLDLINE_MODULE_CAS_PUBLIC_KEY"),
                    "s3-shadow", staging.resolve("publisher-downloads"));
            backend.publish(prepared); backend.publish(prepared);
            require(fixture.objectCount() == 2, "S3 shadow object cardinality drifted");
            Path local = staging.resolve("local/modules"), target = local.resolve("api")
                    .resolve(published.identity().input());
            require(ModuleCacheRemote.hydrate(root, local, "api", published.identity().input(),
                    target, environment), "normal module miss path did not hydrate S3 object");
            Path repacked = staging.resolve("repacked-api.wmb");
            ModuleCacheBundle.Identity identity = ModuleCacheBundle.pack("api", target, repacked);
            require(identity.sha256().equals(published.identity().sha256())
                    && identity.size() == published.identity().size(),
                    "S3 module extraction did not reproduce canonical bundle");
            require(!ModuleCacheRemote.hydrate(root, local, "api", "f".repeat(64),
                    staging.resolve("miss"), environment), "S3 module miss was not preserved");
            System.out.println("S3 module cache shadow passed: module=api, input="
                    + identity.input() + ", bundle=" + identity.sha256() + ", size="
                    + identity.size() + ", objects=" + fixture.objectCount());
        } finally { SafeTreeDelete.delete(staging); }
    }

    static void selfTest() throws Exception {
        Path source = Path.of("").toAbsolutePath().normalize();
        Path root = Files.createTempDirectory("worldline-s3-shadow-").toAbsolutePath().normalize();
        try {
            rejected(() -> execute(root), "S3 shadow accepted an absent api module");
            Files.createDirectories(root.resolve("quality"));
            Files.copy(source.resolve("quality/artifact-transport.properties"),
                    root.resolve("quality/artifact-transport.properties"));
            Path entry = root.resolve(".worldline/build/classes/api");
            Files.createDirectories(entry.resolve("worldline/fixture"));
            Files.writeString(entry.resolve(".complete"), "a".repeat(64) + "\n");
            Files.write(entry.resolve("worldline/fixture/Api.class"),
                    new byte[] {-54, -2, -70, -66, 0, 1});
            execute(root);
        }
        finally { SafeTreeDelete.delete(root); }
    }

    private static void rejected(Action action, String message) throws Exception {
        boolean rejected = false; try { action.run(); }
        catch (IllegalStateException expected) { rejected = true; }
        require(rejected, message);
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
    @FunctionalInterface private interface Action { void run() throws Exception; }
}
