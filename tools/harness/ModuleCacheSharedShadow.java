import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/** End-to-end non-authoritative shadow of the normal shared-directory miss path. */
public final class ModuleCacheSharedShadow {
    private ModuleCacheSharedShadow() { }

    public static void main(String[] arguments) {
        try {
            require(arguments.length == 0, "usage: ModuleCacheSharedShadow"); execute();
        } catch (Exception error) {
            System.err.println("shared module cache shadow failed: " + error.getMessage()); System.exit(1);
        }
    }

    private static void execute() throws Exception {
        Path root = Path.of("").toAbsolutePath().normalize();
        Path linked = root.resolve(".worldline/build/classes/api");
        require(Files.isDirectory(linked), "run the canonical Gate before the shared module shadow");
        Path entry = linked.toRealPath();
        require(Files.isRegularFile(entry.resolve(".complete")), "api module cache entry is incomplete");
        Path staging = root.resolve(".worldline/cas-shadow/shared-directory-"
                + ProcessHandle.current().pid() + "-" + System.nanoTime()).toAbsolutePath().normalize();
        try {
            ModuleCachePublication.Prepared prepared = ModuleCachePublication.prepare(root, staging,
                    List.of(new ModuleCachePublication.Module("api", entry)), List.of());
            ModuleCachePublication.PublishedModule published = prepared.modules().get(0);
            Path shared = staging.resolve("store");
            ModuleCacheSharedDirectory.publish(shared, prepared);
            KeyPair keys = KeyPairGenerator.getInstance("Ed25519").generateKeyPair();
            Path trust = staging.resolve("modules.trust");
            ModuleCacheIndexTrust.sign(prepared.index().path(), "shared-shadow", keys, trust);
            Map<String, String> environment = Map.of(
                    "WORLDLINE_MODULE_CAS_MODE", "shared-directory",
                    "WORLDLINE_MODULE_CAS_DIRECTORY", shared.toString(),
                    "WORLDLINE_MODULE_CAS_INDEX", prepared.index().path().toString(),
                    "WORLDLINE_MODULE_CAS_INDEX_TRUST", trust.toString(),
                    "WORLDLINE_MODULE_CAS_PUBLIC_KEY",
                    Base64.getEncoder().encodeToString(keys.getPublic().getEncoded()),
                    "WORLDLINE_MODULE_CAS_CHANNEL", "shared-shadow");
            SharedCasInventory.Snapshot inventory = SharedCasInventory.configured(root, environment);
            require(inventory != null && inventory.totalObjects() == 2
                    && inventory.verifiedObjects() == 2 && inventory.moduleObjects() == 1
                    && inventory.indexObjects() == 1 && inventory.orphanObjects() == 0
                    && inventory.corruptObjects() == 0 && inventory.malformedEntries() == 0
                    && inventory.missingSignedReferences() == 0,
                    "shared module CAS inventory drifted");
            Path local = staging.resolve("local/modules"), target = local.resolve("api/")
                    .resolve(published.identity().input());
            require(ModuleCacheRemote.hydrate(root, local, "api", published.identity().input(),
                    target, environment), "normal module miss path did not hydrate shared object");
            Path repacked = staging.resolve("repacked-api.wmb");
            ModuleCacheBundle.Identity identity = ModuleCacheBundle.pack("api", target, repacked);
            require(identity.sha256().equals(published.identity().sha256())
                    && identity.size() == published.identity().size(),
                    "shared module extraction did not reproduce canonical bundle");
            require(!ModuleCacheRemote.hydrate(root, local, "api", "f".repeat(64),
                    staging.resolve("miss"), environment), "shared module miss was not preserved");
            System.out.println("shared module cache shadow passed: module=api, input="
                    + identity.input() + ", bundle=" + identity.sha256() + ", size=" + identity.size()
                    + ", inventory=" + inventory.verifiedObjects());
        } finally { SafeTreeDelete.delete(staging); }
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
