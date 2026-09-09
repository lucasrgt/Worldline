import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.time.Clock;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Signed-index module hydration over an immutable S3-compatible object store. */
final class ModuleCacheS3 {
    private final ModuleCacheIndex index;
    private final S3ObjectTransport transport;

    private ModuleCacheS3(ModuleCacheIndex index, S3ObjectTransport transport) {
        this.index = index; this.transport = transport;
    }

    static ModuleCacheS3 connect(ModuleCacheS3Config config, Path indexPath, Path trustPath,
            String publicKey, String channel, Path downloads) throws Exception {
        return connect(config, indexPath, trustPath, publicKey, channel, downloads,
                Clock.systemUTC());
    }

    static ModuleCacheS3 connect(ModuleCacheS3Config config, Path indexPath, Path trustPath,
            String publicKey, String channel, Path downloads, Clock clock) throws Exception {
        ModuleCacheIndexTrust.verify(indexPath, trustPath, publicKey, channel);
        return new ModuleCacheS3(ModuleCacheIndex.read(indexPath),
                new S3ObjectTransport(config, clock, downloads));
    }

    boolean hydrate(String module, String inputSha256, Path target) throws Exception {
        ModuleCacheIndex.Entry entry = index.lookup(module, inputSha256,
                System.getProperty("java.version"));
        if (entry == null) return false;
        Path bundle = transport.temporary(module);
        try {
            require(transport.get(entry.blobSha256(), entry.blobSize(), bundle),
                    "indexed S3 module object is absent");
            ModuleCacheBundle.Identity identity = ModuleCacheBundle.validate(bundle);
            require(identity.module().equals(module) && identity.input().equals(inputSha256)
                    && identity.javaVersion().equals(System.getProperty("java.version"))
                    && identity.sha256().equals(entry.blobSha256())
                    && identity.size() == entry.blobSize(), "S3 module bundle identity drifted");
            ModuleCacheBundle.extract(bundle, module, inputSha256, target); return true;
        } finally { Files.deleteIfExists(bundle); }
    }

    void publish(ModuleCachePublication.Prepared prepared) throws Exception {
        String permits = ArtifactTransportPermitQuery.manifest(prepared.candidates());
        require(Files.readString(prepared.permits(), StandardCharsets.UTF_8).equals(permits),
                "S3 module publication permits drifted");
        for (ArtifactTransportPolicy.Candidate candidate : prepared.candidates())
            transport.put(candidate);
    }

    static void selfTest() throws Exception {
        ModuleCacheS3Config.selfTest(); S3SigV4.selfTest();
        Path source = Path.of("").toAbsolutePath().normalize();
        Path root = Files.createTempDirectory("worldline-s3-module-cache-")
                .toAbsolutePath().normalize();
        try (ModuleCacheS3Fixture fixture = new ModuleCacheS3Fixture(
                "us-east-1", "TESTACCESS", "test-secret")) {
            Files.createDirectories(root.resolve("quality"));
            Files.copy(source.resolve("quality/artifact-transport.properties"),
                    root.resolve("quality/artifact-transport.properties"),
                    StandardCopyOption.COPY_ATTRIBUTES);
            Path entry = root.resolve("entry"), classFile = entry.resolve("fixture/Example.class");
            Files.createDirectories(classFile.getParent()); String input = "a".repeat(64);
            Files.writeString(entry.resolve(".complete"), input + "\n");
            Files.write(classFile, new byte[] {-54, -2, -70, -66, 0, 1});
            ModuleCachePublication.Prepared prepared = ModuleCachePublication.prepare(root,
                    root.resolve(".worldline/cas-shadow/s3"),
                    List.of(new ModuleCachePublication.Module("fixture", entry)), List.of());
            KeyPair keys = KeyPairGenerator.getInstance("Ed25519").generateKeyPair();
            Path trust = root.resolve("index.trust"); ModuleCacheIndexTrust.sign(
                    prepared.index().path(), "s3", keys, trust);
            String publicKey = Base64.getEncoder().encodeToString(keys.getPublic().getEncoded());
            Map<String, String> environment = fixture.environment();
            ModuleCacheS3 backend = connect(ModuleCacheS3Config.from(environment),
                    prepared.index().path(), trust, publicKey, "s3", root.resolve("downloads"));
            backend.publish(prepared); backend.publish(prepared);
            Path target = root.resolve("hydrated");
            require(backend.hydrate("fixture", input, target)
                    && Files.isRegularFile(target.resolve("fixture/Example.class")),
                    "S3 module hydration failed");
            require(!backend.hydrate("fixture", "b".repeat(64), root.resolve("miss")),
                    "S3 module miss was accepted");
            Map<String, String> wrong = new HashMap<>(environment);
            wrong.put("WORLDLINE_MODULE_S3_SECRET_KEY", "wrong-secret");
            ModuleCacheS3 unauthenticated = connect(ModuleCacheS3Config.from(wrong),
                    prepared.index().path(), trust, publicKey, "s3", root.resolve("wrong-downloads"));
            rejected(() -> unauthenticated.hydrate("fixture", input, root.resolve("wrong")),
                    "invalid S3 credentials were accepted");
            String digest = prepared.modules().get(0).candidate().sha256(); fixture.corrupt(digest);
            rejected(() -> backend.hydrate("fixture", input, root.resolve("corrupt")),
                    "corrupt S3 module object was accepted");
        } finally { SafeTreeDelete.delete(root); }
        System.out.println("  S3-compatible module cache self-test: passed");
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
