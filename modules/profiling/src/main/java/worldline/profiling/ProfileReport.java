package worldline.profiling;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import worldline.minimization.Scenario;

/** Canonical checksum-protected text report of one profiled execution. */
public final class ProfileReport {
    private static final String HEADER = "WORLDLINE-PROFILE/1";
    private final byte[] bytes;
    private final String bodyDigest;

    private ProfileReport(byte[] bytes, String bodyDigest) {
        this.bytes = bytes; this.bodyDigest = bodyDigest;
    }

    public static ProfileReport of(Scenario scenario, long seed, TickProfiledRun run) {
        if (scenario == null || run == null) throw new NullPointerException("profile report");
        TickProfile profile = run.profile();
        StringBuilder body = new StringBuilder();
        line(body, HEADER);
        line(body, "seed=" + seed);
        line(body, "scenario.sha256=" + scenario.sha256());
        line(body, "steps=" + scenario.size());
        line(body, "ticks=" + profile.ticks());
        for (int index = 0; index < profile.ticks(); index++) {
            line(body, "sample." + index + "=" + profile.tickNanos(index)
                    + "," + profile.modNanos(index));
        }
        aggregate(body, "tick.total.nanos", profile.total());
        aggregate(body, "tick.mean.nanos", profile.mean());
        aggregate(body, "tick.median.nanos", profile.median());
        aggregate(body, "tick.p95.nanos", profile.p95());
        aggregate(body, "tick.max.nanos", profile.max());
        aggregate(body, "mod.total.nanos", profile.modTotal());
        line(body, String.format(Locale.ROOT, "mod.share.percent=%d", profile.modSharePercent()));
        String digest = sha256(utf8(body.toString()));
        byte[] bytes = utf8(body + "sha256=" + digest + "\n");
        return new ProfileReport(bytes, digest);
    }

    /** Digest embedded in the checksum line: the SHA-256 of the report body. */
    public String sha256() { return bodyDigest; }

    public byte[] bytes() { return bytes.clone(); }

    private static void aggregate(StringBuilder body, String name, long value) {
        line(body, name + "=" + value);
    }

    private static void line(StringBuilder target, String value) {
        target.append(value).append('\n');
    }
    private static byte[] utf8(String value) { return value.getBytes(StandardCharsets.UTF_8); }
    private static String sha256(byte[] value) { try {
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(value);
        StringBuilder result = new StringBuilder();
        for (byte item : digest) result.append(String.format("%02x", item & 255));
        return result.toString();
    } catch (NoSuchAlgorithmException error) { throw new IllegalStateException(error); } }
}
