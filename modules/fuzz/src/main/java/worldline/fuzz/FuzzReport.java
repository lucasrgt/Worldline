package worldline.fuzz;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import worldline.minimization.Scenario;

/** Canonical checksum-protected text report of one fuzzing campaign. */
public final class FuzzReport {
    private static final String HEADER = "WORLDLINE-FUZZ/1";
    private final long seed;
    private final int cases, maxSteps, evaluations;
    private final String subjects;
    private final DifferentialFuzzer.Result result;
    private final byte[] bytes;
    private final String bodyDigest;

    private FuzzReport(long seed, int cases, int maxSteps, int evaluations,
            String subjects, DifferentialFuzzer.Result result, byte[] bytes,
            String bodyDigest) {
        this.seed = seed; this.cases = cases; this.maxSteps = maxSteps;
        this.evaluations = evaluations; this.subjects = subjects;
        this.result = result; this.bytes = bytes; this.bodyDigest = bodyDigest;
    }

    public static FuzzReport of(long seed, int cases, int maxSteps,
            List<String> subjectLabels, DifferentialFuzzer.Result result) {
        if (subjectLabels == null || subjectLabels.isEmpty()) {
            throw new IllegalArgumentException("report requires subjects");
        }
        StringBuilder body = new StringBuilder();
        line(body, HEADER);
        line(body, "seed=" + seed);
        line(body, "cases=" + cases);
        line(body, "max-steps=" + maxSteps);
        line(body, "subjects=" + join(subjectLabels));
        line(body, "evaluations=" + result.evaluations());
        line(body, "findings=" + result.findings().size());
        for (int index = 0; index < result.findings().size(); index++) {
            appendFinding(body, index, result.findings().get(index));
        }
        String digest = sha256(utf8(body.toString()));
        byte[] bytes = utf8(body + "sha256=" + digest + "\n");
        return new FuzzReport(seed, cases, maxSteps, result.evaluations(),
                join(subjectLabels), result, bytes, digest);
    }

    private static void appendFinding(StringBuilder body, int index, FuzzFinding finding) {
        String prefix = "finding." + index + ".";
        line(body, prefix + "kind=" + finding.kind().name().toLowerCase(java.util.Locale.ROOT));
        line(body, prefix + "subjects=" + finding.leftSubject() + "," + finding.rightSubject());
        line(body, prefix + "original.steps=" + finding.original().size());
        line(body, prefix + "original=" + DifferentialFuzzer.embed(finding.original()));
        if (finding.minimized() != null) {
            line(body, prefix + "minimized.steps=" + finding.minimized().size());
            line(body, prefix + "minimized=" + DifferentialFuzzer.embed(finding.minimized()));
        } else {
            line(body, prefix + "minimized=none");
        }
    }

    /** Canonical summary lines for terminal output after the report hash. */
    public void print(java.io.PrintStream output) {
        output.println("WORLDLINE_FUZZ=" + (result.findings().isEmpty() ? "CLEAN" : "FINDINGS"));
        output.println("subjects=" + subjects);
        output.println("cases=" + cases);
        output.println("evaluations=" + evaluations);
        output.println("findings=" + result.findings().size());
        output.println("report.sha256=" + sha256());
    }

    public DifferentialFuzzer.Result result() { return result; }
    public byte[] bytes() { return bytes.clone(); }

    /** Digest embedded in the checksum line: the SHA-256 of the report body. */
    public String sha256() { return bodyDigest; }

    private static String join(List<String> labels) {
        StringBuilder joined = new StringBuilder();
        for (String label : labels) {
            if (label == null || !label.matches("[a-z0-9@._:-]{1,64}")) {
                throw new IllegalArgumentException("invalid subject label: " + label);
            }
            if (joined.length() > 0) joined.append(',');
            joined.append(label);
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
