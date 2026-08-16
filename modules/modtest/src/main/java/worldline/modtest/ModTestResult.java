package worldline.modtest;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import worldline.mods.ModArtifact;
import worldline.trace.CanonicalStateDocument;

/** Canonical provenance-bound result of one mod scenario execution. */
public final class ModTestResult {
    public static final int MAX_BYTES = 6_291_456;
    private static final String HEADER = "WORLDLINE-MOD-TEST/1";
    private final String modId, modVersion, entrypoint, artifactSha256, runtime, worldlineApi;
    private final CanonicalStateDocument trace;
    private final byte[] bytes;

    private ModTestResult(String modId, String modVersion, String entrypoint,
            String artifactSha256, String runtime, String worldlineApi,
            CanonicalStateDocument trace, byte[] bytes) {
        this.modId = modId; this.modVersion = modVersion; this.entrypoint = entrypoint;
        this.artifactSha256 = artifactSha256; this.runtime = runtime;
        this.worldlineApi = worldlineApi; this.trace = trace; this.bytes = bytes;
    }

    public static ModTestResult create(ModArtifact artifact, CanonicalStateDocument trace) {
        if (artifact == null || trace == null) throw new NullPointerException("mod test input");
        require(artifact.compatible(), "mod test requires a compatible artifact");
        return encode(artifact.descriptor().id(), artifact.descriptor().version(),
                artifact.descriptor().entrypoint(), artifact.sha256(), artifact.descriptor().runtime(),
                artifact.descriptor().worldlineApi(), trace);
    }

    public static ModTestResult parse(byte[] input) {
        if (input == null) throw new NullPointerException("input");
        require(input.length > 0 && input.length <= MAX_BYTES, "invalid mod test result size");
        String document = new String(input, StandardCharsets.UTF_8);
        require(Arrays.equals(input, utf8(document)), "mod test result is not strict UTF-8");
        String[] lines = document.split("\n", -1);
        require(lines.length == 11 && lines[10].isEmpty(), "invalid mod test result framing");
        require(HEADER.equals(lines[0]), "unsupported mod test result version");
        String id = value(lines[1], "mod.id"), version = value(lines[2], "mod.version");
        String entrypoint = value(lines[3], "mod.entrypoint");
        String artifact = value(lines[4], "artifact.sha256");
        String runtime = value(lines[5], "runtime"), api = value(lines[6], "worldline.api");
        String traceHash = value(lines[7], "trace.sha256");
        byte[] traceBytes;
        try { traceBytes = Base64.getUrlDecoder().decode(value(lines[8], "trace")); }
        catch (IllegalArgumentException error) { throw new IllegalArgumentException("invalid embedded trace", error); }
        require(traceBytes.length > 0 && traceBytes.length <= CanonicalStateDocument.MAX_CHARACTERS,
                "invalid embedded trace size");
        String traceText = new String(traceBytes, StandardCharsets.UTF_8);
        require(Arrays.equals(traceBytes, utf8(traceText)), "embedded trace is not strict UTF-8");
        CanonicalStateDocument trace = CanonicalStateDocument.parse(traceText);
        require(traceHash.equals(trace.signature()), "embedded trace SHA-256 mismatch");
        StringBuilder body = new StringBuilder();
        for (int index = 0; index < 9; index++) line(body, lines[index]);
        require(value(lines[9], "sha256").equals(sha256(utf8(body.toString()))),
                "mod test result checksum mismatch");
        ModTestResult result = encode(id, version, entrypoint, artifact, runtime, api, trace);
        require(Arrays.equals(input, result.bytes), "mod test result is not canonical");
        return result;
    }

    public String modId() { return modId; }
    public String modVersion() { return modVersion; }
    public String entrypoint() { return entrypoint; }
    public String artifactSha256() { return artifactSha256; }
    public String runtime() { return runtime; }
    public String worldlineApi() { return worldlineApi; }
    public CanonicalStateDocument trace() { return trace; }
    public byte[] bytes() { return bytes.clone(); }
    public String sha256() { return sha256(bytes); }

    @Override public boolean equals(Object other) {
        return other instanceof ModTestResult && Arrays.equals(bytes, ((ModTestResult) other).bytes);
    }
    @Override public int hashCode() { return Arrays.hashCode(bytes); }

    private static ModTestResult encode(String id, String version, String entrypoint,
            String artifact, String runtime, String api, CanonicalStateDocument trace) {
        require(id != null && id.matches("[a-z][a-z0-9.-]{0,63}"), "invalid result mod id");
        require(version != null && version.matches("[0-9]+\\.[0-9]+\\.[0-9]+(?:-[a-z0-9][a-z0-9.-]*)?"),
                "invalid result mod version");
        require(entrypoint != null && entrypoint.matches(
                "[A-Za-z_$][A-Za-z0-9_$]*(?:\\.[A-Za-z_$][A-Za-z0-9_$]*)+"),
                "invalid result entrypoint");
        require(hex(artifact, 64), "invalid result artifact SHA-256");
        require(runtime != null && runtime.matches("[a-z0-9][a-z0-9._-]{0,63}"),
                "invalid result runtime");
        require(api != null && api.matches("[1-9][0-9]*"), "invalid result Worldline API");
        byte[] traceBytes = utf8(trace.canonical());
        require(traceBytes.length <= CanonicalStateDocument.MAX_CHARACTERS, "embedded trace is too large");
        StringBuilder body = new StringBuilder(); line(body, HEADER); line(body, "mod.id=" + id);
        line(body, "mod.version=" + version); line(body, "mod.entrypoint=" + entrypoint);
        line(body, "artifact.sha256=" + artifact); line(body, "runtime=" + runtime);
        line(body, "worldline.api=" + api); line(body, "trace.sha256=" + trace.signature());
        line(body, "trace=" + Base64.getUrlEncoder().withoutPadding().encodeToString(traceBytes));
        byte[] bytes = utf8(body + "sha256=" + sha256(utf8(body.toString())) + "\n");
        require(bytes.length <= MAX_BYTES, "mod test result exceeds maximum size");
        return new ModTestResult(id, version, entrypoint, artifact, runtime, api, trace, bytes);
    }

    private static String value(String line, String key) {
        String prefix = key + "="; require(line.startsWith(prefix), "missing result field " + key);
        return line.substring(prefix.length());
    }
    private static boolean hex(String value, int length) {
        return value != null && value.length() == length && value.matches("[0-9a-f]+");
    }
    private static byte[] utf8(String value) { return value.getBytes(StandardCharsets.UTF_8); }
    private static void line(StringBuilder target, String value) { target.append(value).append('\n'); }
    private static String sha256(byte[] value) { try {
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(value); StringBuilder result = new StringBuilder();
        for (byte item : digest) result.append(String.format("%02x", item & 255)); return result.toString();
    } catch (NoSuchAlgorithmException error) { throw new IllegalStateException(error); } }
    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalArgumentException(message);
    }
}
