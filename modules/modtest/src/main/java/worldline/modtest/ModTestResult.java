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
    public static final String EXECUTION = "controlled-runtime";
    private static final String HEADER_V1 = "WORLDLINE-MOD-TEST/1";
    private static final String HEADER_V2 = "WORLDLINE-MOD-TEST/2";
    public static final int MAX_TICKS = 100_000;
    private final String modId, modVersion, entrypoint, artifactSha256, runtime, worldlineApi;
    private final boolean executed;
    private final long seed;
    private final int ticks;
    private final CanonicalStateDocument trace;
    private final byte[] bytes;

    private ModTestResult(String modId, String modVersion, String entrypoint,
            String artifactSha256, String runtime, String worldlineApi,
            boolean executed, long seed, int ticks,
            CanonicalStateDocument trace, byte[] bytes) {
        this.modId = modId; this.modVersion = modVersion; this.entrypoint = entrypoint;
        this.artifactSha256 = artifactSha256; this.runtime = runtime;
        this.worldlineApi = worldlineApi; this.executed = executed;
        this.seed = seed; this.ticks = ticks; this.trace = trace; this.bytes = bytes;
    }

    /** Records a caller-supplied trace; execution is not attested. */
    public static ModTestResult create(ModArtifact artifact, CanonicalStateDocument trace) {
        if (artifact == null || trace == null) throw new NullPointerException("mod test input");
        require(artifact.compatible(), "mod test requires a compatible artifact");
        return encode(artifact.descriptor().id(), artifact.descriptor().version(),
                artifact.descriptor().entrypoint(), artifact.sha256(), artifact.descriptor().runtime(),
                artifact.descriptor().worldlineApi(), false, 0L, 0, trace);
    }

    /** Binds a trace produced by executing the artifact in the controlled runtime. */
    public static ModTestResult createExecuted(ModArtifact artifact, CanonicalStateDocument trace,
            long seed, int ticks) {
        if (artifact == null || trace == null) throw new NullPointerException("mod test input");
        require(artifact.compatible(), "mod test requires a compatible artifact");
        require(ticks >= 1 && ticks <= MAX_TICKS, "invalid executed tick count");
        return encode(artifact.descriptor().id(), artifact.descriptor().version(),
                artifact.descriptor().entrypoint(), artifact.sha256(), artifact.descriptor().runtime(),
                artifact.descriptor().worldlineApi(), true, seed, ticks, trace);
    }

    public static ModTestResult parse(byte[] input) {
        if (input == null) throw new NullPointerException("input");
        require(input.length > 0 && input.length <= MAX_BYTES, "invalid mod test result size");
        String document = new String(input, StandardCharsets.UTF_8);
        require(Arrays.equals(input, utf8(document)), "mod test result is not strict UTF-8");
        String[] lines = document.split("\n", -1);
        boolean second = HEADER_V2.equals(lines[0]);
        require(second || HEADER_V1.equals(lines[0]), "unsupported mod test result version");
        int bodyLines = second ? 12 : 9;
        require(lines.length == bodyLines + 2 && lines[bodyLines + 1].isEmpty(),
                "invalid mod test result framing");
        String id = value(lines[1], "mod.id"), version = value(lines[2], "mod.version");
        String entrypoint = value(lines[3], "mod.entrypoint");
        String artifact = value(lines[4], "artifact.sha256");
        String runtime = value(lines[5], "runtime"), api = value(lines[6], "worldline.api");
        String execution = second ? value(lines[7], "execution") : null;
        long seed = 0L; int ticks = 0;
        int traceLine = 7;
        if (second) {
            require(EXECUTION.equals(execution), "unsupported execution boundary");
            seed = seed(value(lines[8], "seed"));
            ticks = ticks(value(lines[9], "ticks"));
            traceLine = 10;
        }
        String traceHash = value(lines[traceLine], "trace.sha256");
        byte[] traceBytes;
        try { traceBytes = Base64.getUrlDecoder().decode(value(lines[traceLine + 1], "trace")); }
        catch (IllegalArgumentException error) { throw new IllegalArgumentException("invalid embedded trace", error); }
        require(traceBytes.length > 0 && traceBytes.length <= CanonicalStateDocument.MAX_CHARACTERS,
                "invalid embedded trace size");
        String traceText = new String(traceBytes, StandardCharsets.UTF_8);
        require(Arrays.equals(traceBytes, utf8(traceText)), "embedded trace is not strict UTF-8");
        CanonicalStateDocument trace = CanonicalStateDocument.parse(traceText);
        require(traceHash.equals(trace.signature()), "embedded trace SHA-256 mismatch");
        StringBuilder body = new StringBuilder();
        for (int index = 0; index < bodyLines; index++) line(body, lines[index]);
        require(value(lines[bodyLines], "sha256").equals(sha256(utf8(body.toString()))),
                "mod test result checksum mismatch");
        ModTestResult result = encode(id, version, entrypoint, artifact, runtime, api,
                second, seed, ticks, trace);
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

    /** True when the embedded trace was produced by an attested controlled run. */
    public boolean executed() { return executed; }

    /** Attested run seed; zero when not executed. */
    public long seed() { return seed; }

    /** Attested observed tick count; zero when not executed. */
    public int ticks() { return ticks; }

    public byte[] bytes() { return bytes.clone(); }
    public String sha256() { return sha256(bytes); }

    @Override public boolean equals(Object other) {
        return other instanceof ModTestResult && Arrays.equals(bytes, ((ModTestResult) other).bytes);
    }
    @Override public int hashCode() { return Arrays.hashCode(bytes); }

    private static ModTestResult encode(String id, String version, String entrypoint,
            String artifact, String runtime, String api, boolean executed, long seed,
            int ticks, CanonicalStateDocument trace) {
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
        StringBuilder body = new StringBuilder(); line(body, executed ? HEADER_V2 : HEADER_V1);
        line(body, "mod.id=" + id); line(body, "mod.version=" + version);
        line(body, "mod.entrypoint=" + entrypoint); line(body, "artifact.sha256=" + artifact);
        line(body, "runtime=" + runtime); line(body, "worldline.api=" + api);
        if (executed) {
            line(body, "execution=" + EXECUTION); line(body, "seed=" + seed);
            line(body, "ticks=" + ticks);
        }
        line(body, "trace.sha256=" + trace.signature());
        line(body, "trace=" + Base64.getUrlEncoder().withoutPadding().encodeToString(traceBytes));
        byte[] bytes = utf8(body + "sha256=" + sha256(utf8(body.toString())) + "\n");
        require(bytes.length <= MAX_BYTES, "mod test result exceeds maximum size");
        return new ModTestResult(id, version, entrypoint, artifact, runtime, api,
                executed, seed, ticks, trace, bytes);
    }

    private static long seed(String value) {
        require(value.matches("-?[0-9]{1,19}"), "invalid attested seed");
        return Long.parseLong(value);
    }

    private static int ticks(String value) {
        require(value.matches("[1-9][0-9]{0,5}") && Integer.parseInt(value) <= MAX_TICKS,
                "invalid attested tick count");
        return Integer.parseInt(value);
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
