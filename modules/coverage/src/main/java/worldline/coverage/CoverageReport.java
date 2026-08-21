package worldline.coverage;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import worldline.minimization.Scenario;
import worldline.trace.CanonicalStateDocument;

/** Canonical checksum-protected coverage report for one scenario. */
public final class CoverageReport {
    private static final String HEADER = "WORLDLINE-COVERAGE/1";
    private final byte[] bytes;
    private final String bodyDigest;

    private CoverageReport(byte[] bytes, String bodyDigest) {
        this.bytes = bytes; this.bodyDigest = bodyDigest;
    }

    public static CoverageReport of(Scenario scenario, CanonicalStateDocument trace,
            ScenarioCoverage coverage) {
        if (scenario == null || coverage == null) throw new NullPointerException("report");
        StringBuilder body = new StringBuilder();
        line(body, HEADER);
        line(body, "scenario.sha256=" + scenario.sha256());
        line(body, "steps=" + scenario.size());
        line(body, trace == null ? "trace=none" : "trace.sha256=" + trace.signature());
        line(body, "categories.total=" + coverage.totalCategories());
        line(body, "categories.touched=" + join(coverage.categories()));
        line(body, "categories.percent=" + coverage.percentCategories());
        for (java.util.Map.Entry<String, Integer> entry : coverage.stepCounts().entrySet()) {
            line(body, "steps." + entry.getKey() + "=" + entry.getValue());
        }
        line(body, "roles.observed=" + (coverage.roles().isEmpty() ? "none"
                : join(coverage.roles())));
        String digest = sha256(utf8(body.toString()));
        byte[] bytes = utf8(body + "sha256=" + digest + "\n");
        return new CoverageReport(bytes, digest);
    }

    /** Digest embedded in the checksum line: the SHA-256 of the report body. */
    public String sha256() { return bodyDigest; }

    public byte[] bytes() { return bytes.clone(); }

    private static String join(List<String> values) {
        StringBuilder joined = new StringBuilder();
        for (String value : values) {
            if (!value.matches("[a-z0-9_]{1,64}") && !value.matches("[A-Z0-9_]{1,64}")) {
                throw new IllegalArgumentException("invalid report token: " + value);
            }
            if (joined.length() > 0) joined.append(',');
            joined.append(value);
        }
        return joined.toString();
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
