package worldline.reproduction;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import worldline.api.RuntimeSnapshot;

/** Canonical portable envelope for a snapshot and its required runtime inputs. */
public final class ReproductionBundle {
    public static final int MAX_BYTES = 2_097_152;
    private static final String HEADER = "WORLDLINE-REPRODUCTION/1";
    private final String runtimeId, worldlineVersion, clientSha256, toolchainRevision;
    private final RuntimeSnapshot snapshot;
    private final byte[] bytes;

    private ReproductionBundle(String runtimeId, String worldlineVersion, String clientSha256,
            String toolchainRevision, RuntimeSnapshot snapshot, byte[] bytes) {
        this.runtimeId = runtimeId; this.worldlineVersion = worldlineVersion;
        this.clientSha256 = clientSha256; this.toolchainRevision = toolchainRevision;
        this.snapshot = snapshot; this.bytes = bytes;
    }

    public static ReproductionBundle create(String runtimeId, String worldlineVersion,
            String clientSha256, String toolchainRevision, RuntimeSnapshot snapshot) {
        require(token(runtimeId), "invalid bundle runtime ID");
        require(worldlineVersion != null && worldlineVersion.matches("[0-9]+\\.[0-9]+\\.[0-9]+"),
                "invalid Worldline version");
        require(hex(clientSha256, 64), "invalid client SHA-256");
        require(hex(toolchainRevision, 40), "invalid toolchain revision");
        if (snapshot == null) throw new NullPointerException("snapshot");
        StringBuilder body = new StringBuilder();
        line(body, HEADER); line(body, "runtime=" + runtimeId);
        line(body, "worldline=" + worldlineVersion); line(body, "client.sha256=" + clientSha256);
        line(body, "toolchain.revision=" + toolchainRevision);
        line(body, "snapshot.sha256=" + snapshot.sha256());
        line(body, "snapshot=" + Base64.getUrlEncoder().withoutPadding()
                .encodeToString(snapshot.bytes()));
        byte[] bytes = utf8(body + "sha256=" + sha256(utf8(body.toString())) + "\n");
        require(bytes.length <= MAX_BYTES, "reproduction bundle exceeds " + MAX_BYTES + " bytes");
        return new ReproductionBundle(runtimeId, worldlineVersion, clientSha256,
                toolchainRevision, snapshot, bytes);
    }

    public static ReproductionBundle parse(byte[] input) {
        if (input == null) throw new NullPointerException("input");
        require(input.length > 0 && input.length <= MAX_BYTES, "invalid reproduction bundle size");
        String document = new String(input, StandardCharsets.UTF_8);
        require(Arrays.equals(input, utf8(document)), "bundle is not strict UTF-8");
        String[] lines = document.split("\n", -1);
        require(lines.length == 9 && lines[8].isEmpty(), "invalid bundle framing");
        require(lines[0].equals(HEADER), "unsupported bundle version");
        String runtime = value(lines[1], "runtime");
        String version = value(lines[2], "worldline");
        String client = value(lines[3], "client.sha256");
        String toolchain = value(lines[4], "toolchain.revision");
        String snapshotHash = value(lines[5], "snapshot.sha256");
        RuntimeSnapshot snapshot;
        try { snapshot = RuntimeSnapshot.of(Base64.getUrlDecoder().decode(value(lines[6], "snapshot"))); }
        catch (IllegalArgumentException error) { throw new IllegalArgumentException("invalid bundle snapshot", error); }
        require(snapshotHash.equals(snapshot.sha256()), "bundle snapshot SHA-256 mismatch");
        StringBuilder body = new StringBuilder();
        for (int index = 0; index < 7; index++) line(body, lines[index]);
        require(value(lines[7], "sha256").equals(sha256(utf8(body.toString()))),
                "bundle checksum mismatch");
        ReproductionBundle result = create(runtime, version, client, toolchain, snapshot);
        require(Arrays.equals(input, result.bytes), "bundle is not canonical");
        return result;
    }

    public String runtimeId() { return runtimeId; }
    public String worldlineVersion() { return worldlineVersion; }
    public String clientSha256() { return clientSha256; }
    public String toolchainRevision() { return toolchainRevision; }
    public RuntimeSnapshot snapshot() { return snapshot; }
    public byte[] bytes() { return bytes.clone(); }
    public int size() { return bytes.length; }
    public String sha256() { return sha256(bytes); }

    @Override public boolean equals(Object other) {
        return other instanceof ReproductionBundle
                && Arrays.equals(bytes, ((ReproductionBundle) other).bytes);
    }
    @Override public int hashCode() { return Arrays.hashCode(bytes); }
    @Override public String toString() {
        return "ReproductionBundle[runtime=" + runtimeId + ",size=" + size()
                + ",sha256=" + sha256() + "]";
    }

    private static String value(String line, String key) {
        String prefix = key + "="; require(line.startsWith(prefix), "missing bundle field " + key);
        return line.substring(prefix.length());
    }
    private static boolean token(String value) {
        return value != null && value.matches("[a-z0-9][a-z0-9._-]{0,63}");
    }
    private static boolean hex(String value, int length) {
        return value != null && value.length() == length && value.matches("[0-9a-f]+");
    }
    private static String sha256(byte[] value) {
        try { byte[] digest = MessageDigest.getInstance("SHA-256").digest(value);
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte item : digest) hex.append(String.format("%02x", item & 0xff)); return hex.toString(); }
        catch (NoSuchAlgorithmException error) { throw new IllegalStateException(error); }
    }
    private static byte[] utf8(String value) { return value.getBytes(StandardCharsets.UTF_8); }
    private static void line(StringBuilder target, String value) { target.append(value).append('\n'); }
    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalArgumentException(message);
    }
}
