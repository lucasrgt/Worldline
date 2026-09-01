import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Map;
import java.util.Set;

/** Canonical Ed25519 trust envelope for a Java-owned module cache index. */
final class ModuleCacheIndexTrust {
    private static final String SCHEMA = "worldline.module-cache-index-trust.v1";
    private static final String ALGORITHM = "Ed25519";
    private static final Set<String> KEYS = Set.of("schema", "channel", "algorithm",
            "publisher_key_sha256", "index_sha256", "index_size", "signature");

    private ModuleCacheIndexTrust() { }

    static Envelope sign(Path index, String channel, KeyPair keys, Path target) throws Exception {
        requireChannel(channel); ModuleCacheIndex.validate(index);
        require(keys != null && keys.getPublic() != null && keys.getPrivate() != null,
                "module index signing key pair is absent");
        String keySha256 = digest(keys.getPublic().getEncoded());
        String indexSha256 = digest(index); long indexSize = Files.size(index);
        byte[] payload = payload(channel, keySha256, indexSha256, indexSize);
        Signature signer = Signature.getInstance(ALGORITHM); signer.initSign(keys.getPrivate());
        signer.update(payload); byte[] signature = signer.sign();
        Signature verifier = Signature.getInstance(ALGORITHM); verifier.initVerify(keys.getPublic());
        verifier.update(payload); require(verifier.verify(signature),
                "module index signing key pair does not match");
        Envelope envelope = new Envelope(channel, keySha256, indexSha256, indexSize,
                Base64.getEncoder().encodeToString(signature));
        write(target, encode(envelope)); return envelope;
    }

    static Envelope verify(Path index, Path trust, String publicKeyBase64,
            String expectedChannel) throws Exception {
        requireChannel(expectedChannel);
        PublicKey publicKey = publicKey(publicKeyBase64);
        require(Files.isRegularFile(trust, LinkOption.NOFOLLOW_LINKS)
                && !Files.isSymbolicLink(trust) && Files.size(trust) <= 4096L,
                "invalid module index trust envelope");
        String text = Files.readString(trust, StandardCharsets.UTF_8);
        Map<String, Object> root = MiniJson.object(text);
        require(root.keySet().equals(KEYS) && SCHEMA.equals(MiniJson.string(root, "schema"))
                && ALGORITHM.equals(MiniJson.string(root, "algorithm")),
                "invalid module index trust schema");
        Envelope envelope = new Envelope(MiniJson.string(root, "channel"),
                MiniJson.string(root, "publisher_key_sha256"),
                MiniJson.string(root, "index_sha256"), MiniJson.integer(root, "index_size"),
                MiniJson.string(root, "signature"));
        validate(envelope); require(text.equals(encode(envelope)),
                "noncanonical module index trust envelope");
        require(envelope.channel().equals(expectedChannel), "module index trust channel drifted");
        require(envelope.publisherKeySha256().equals(digest(publicKey.getEncoded())),
                "module index publisher key drifted");
        require(Files.isRegularFile(index, LinkOption.NOFOLLOW_LINKS)
                && !Files.isSymbolicLink(index) && Files.size(index) == envelope.indexSize()
                && digest(index).equals(envelope.indexSha256()), "trusted module index drifted");
        Signature verifier = Signature.getInstance(ALGORITHM); verifier.initVerify(publicKey);
        verifier.update(payload(envelope.channel(), envelope.publisherKeySha256(),
                envelope.indexSha256(), envelope.indexSize()));
        require(verifier.verify(signature(envelope.signature())),
                "module index trust signature drifted");
        ModuleCacheIndex.validate(index);
        return envelope;
    }

    static void selfTest() throws Exception {
        Path root = Files.createTempDirectory("worldline-module-index-trust-");
        try {
            Path index = root.resolve("modules.wmi");
            ModuleCacheIndex.write(index, java.util.List.of(new ModuleCacheIndex.Entry("api",
                    "1".repeat(64), "21.0.1", "2".repeat(64), 4096L)));
            KeyPair keys = KeyPairGenerator.getInstance(ALGORITHM).generateKeyPair();
            String publicKey = Base64.getEncoder().encodeToString(keys.getPublic().getEncoded());
            Path first = root.resolve("first.trust"), second = root.resolve("second.trust");
            Envelope envelope = sign(index, "shadow", keys, first);
            sign(index, "shadow", keys, second);
            require(Files.mismatch(first, second) == -1L
                    && verify(index, first, publicKey, "shadow").equals(envelope),
                    "module index trust envelope was not deterministic");
            KeyPair wrong = KeyPairGenerator.getInstance(ALGORITHM).generateKeyPair();
            rejected(() -> verify(index, first, Base64.getEncoder().encodeToString(
                    wrong.getPublic().getEncoded()), "shadow"), "wrong publisher key was accepted");
            rejected(() -> verify(index, first, publicKey, "release"),
                    "wrong module index channel was accepted");
            String text = Files.readString(first); String signature = envelope.signature();
            Path tampered = root.resolve("tampered.trust");
            Files.writeString(tampered, text.replace(signature,
                    (signature.startsWith("A") ? "B" : "A") + signature.substring(1)));
            rejected(() -> verify(index, tampered, publicKey, "shadow"),
                    "corrupt module index signature was accepted");
            Path unknown = root.resolve("unknown.trust");
            Files.writeString(unknown, text.replace("  \"channel\":",
                    "  \"unknown\":true,\n  \"channel\":"));
            rejected(() -> verify(index, unknown, publicKey, "shadow"),
                    "unknown module index trust field was accepted");
            byte[] bytes = Files.readAllBytes(index); bytes[bytes.length - 2] ^= 1;
            Files.write(index, bytes);
            rejected(() -> verify(index, first, publicKey, "shadow"),
                    "corrupt signed module index was accepted");
        } finally { SafeTreeDelete.delete(root); }
        System.out.println("  module cache index trust self-test: passed");
    }

    private static PublicKey publicKey(String encoded) throws Exception {
        try {
            byte[] bytes = Base64.getDecoder().decode(encoded);
            require(bytes.length > 0 && bytes.length <= 256, "invalid module index public key");
            require(Base64.getEncoder().encodeToString(bytes).equals(encoded),
                    "noncanonical module index public key");
            return KeyFactory.getInstance(ALGORITHM).generatePublic(new X509EncodedKeySpec(bytes));
        } catch (IllegalArgumentException error) {
            throw new IllegalStateException("invalid module index public key encoding", error);
        }
    }
    private static byte[] signature(String encoded) {
        try {
            byte[] bytes = Base64.getDecoder().decode(encoded);
            require(bytes.length == 64, "invalid module index trust signature"); return bytes;
        } catch (IllegalArgumentException error) {
            throw new IllegalStateException("invalid module index trust signature encoding", error);
        }
    }
    private static byte[] payload(String channel, String key, String index, long size) {
        return (SCHEMA + "\nchannel=" + channel + "\npublisher_key_sha256=" + key
                + "\nindex_sha256=" + index + "\nindex_size=" + size + "\n")
                .getBytes(StandardCharsets.UTF_8);
    }
    private static String encode(Envelope value) {
        return "{\n  \"schema\":\"" + SCHEMA + "\",\n  \"channel\":\"" + value.channel()
                + "\",\n  \"algorithm\":\"" + ALGORITHM
                + "\",\n  \"publisher_key_sha256\":\"" + value.publisherKeySha256()
                + "\",\n  \"index_sha256\":\"" + value.indexSha256()
                + "\",\n  \"index_size\":" + value.indexSize()
                + ",\n  \"signature\":\"" + value.signature() + "\"\n}\n";
    }
    private static void validate(Envelope value) {
        requireChannel(value.channel());
        require(value.publisherKeySha256().matches("[0-9a-f]{64}")
                && value.indexSha256().matches("[0-9a-f]{64}")
                && value.indexSize() > 0L && value.indexSize() <= 1024L * 1024L,
                "invalid module index trust identity");
        signature(value.signature());
    }
    private static void requireChannel(String channel) {
        require(channel != null && channel.matches("[a-z0-9]+(?:-[a-z0-9]+)*"),
                "invalid module index trust channel");
    }
    private static String digest(Path path) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream input = Files.newInputStream(path)) {
            byte[] buffer = new byte[16 * 1024]; int count;
            while ((count = input.read(buffer)) >= 0) if (count > 0) digest.update(buffer, 0, count);
        }
        return HexFormat.of().formatHex(digest.digest());
    }
    private static String digest(byte[] bytes) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
    }
    private static void write(Path target, String text) throws Exception {
        require(!Files.exists(target), "module index trust target already exists");
        Files.createDirectories(target.getParent()); Path temporary = target.resolveSibling(
                target.getFileName() + ".tmp-" + ProcessHandle.current().pid() + "-" + System.nanoTime());
        try {
            Files.writeString(temporary, text, StandardCharsets.UTF_8);
            try { Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE); }
            catch (AtomicMoveNotSupportedException error) { Files.move(temporary, target); }
        } finally { Files.deleteIfExists(temporary); }
    }
    private static void rejected(Action action, String message) throws Exception {
        boolean rejected = false;
        try { action.run(); } catch (Exception expected) { rejected = true; }
        require(rejected, message);
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }

    record Envelope(String channel, String publisherKeySha256, String indexSha256,
            long indexSize, String signature) { }
    @FunctionalInterface private interface Action { void run() throws Exception; }
}
