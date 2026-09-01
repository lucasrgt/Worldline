import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import java.util.concurrent.TimeUnit;

/** Repeats the structural Gate self-test in deterministic campaign shards. */
public final class StructuralInstabilityCampaign {
    private static final String CAMPAIGN_JAVA_OPTIONS = "-Dfile.encoding=UTF-8 "
            + "-Dstdout.encoding=UTF-8 -Dstderr.encoding=UTF-8 -XX:-UsePerfData";
    private final Path root = Path.of("").toAbsolutePath().normalize();
    private final Path reports = root.resolve(".worldline/reports/structural-instability");

    public static void main(String[] arguments) {
        try {
            if (List.of(arguments).equals(List.of("--self-test"))) { selfTest(); return; }
            new StructuralInstabilityCampaign().execute(arguments);
        } catch (Exception error) {
            System.err.println("structural instability campaign failed: " + error.getMessage());
            System.exit(1);
        }
    }

    private void execute(String[] arguments) throws Exception {
        CampaignOptions options = CampaignOptions.parse(arguments);
        CampaignSpec spec = CampaignSpec.load(root, "structural-instability");
        CampaignPlan plan = options.shardCount() == null ? spec.plan(options.seed())
                : spec.plan(options.seed(), options.shardCount());
        require(options.shardIndex() < plan.shardCount(), "shard index is outside the campaign plan");
        if (options.planOnly()) { System.out.print(plan.envelope()); return; }
        CampaignArtifacts.publishPlan(reports, plan);
        CampaignPlan.Assignment assignment = plan.shard(options.shardIndex()).assignments().stream()
                .filter(value -> value.workload().equals("gate-self-test")).findFirst().orElseThrow();
        Run run = repeat(plan, assignment, options);
        Path log = CampaignArtifacts.publishLog(reports, plan, options.shardIndex(),
                "gate-self-test", run.log);
        CampaignShardReceipt.Result result = new CampaignShardReceipt.Result(
                assignment.workload(), assignment.start(), assignment.endExclusive(), assignment.seed(),
                options.budgetSeconds(), run.elapsedMs, run.exit, run.timedOut,
                EvidenceExplorerSnapshot.sha256(log));
        CampaignShardReceipt receipt = CampaignShardReceipt.create(plan, options.shardIndex(),
                git("HEAD"), git("HEAD^{tree}"), executorRequest(options.executorPlan()), List.of(result));
        Path receiptPath = CampaignArtifacts.publishReceipt(reports, plan, receipt);
        CampaignReceiptSet.Aggregate aggregate = CampaignArtifacts.aggregateIfComplete(reports, plan);
        require(run.exit == 0, "instability detected; inspect " + root.relativize(receiptPath));
        System.out.println("structural instability shard passed: index=" + options.shardIndex()
                + "/" + plan.shardCount() + ", cases=" + assignment.cases()
                + ", normalized=" + run.normalizedRoot + ", plan=" + plan.sha256()
                + ", aggregate=" + (aggregate != null));
    }

    private Run repeat(CampaignPlan plan, CampaignPlan.Assignment assignment,
            CampaignOptions options) throws Exception {
        long started = System.nanoTime(), deadline = started
                + TimeUnit.SECONDS.toNanos(options.budgetSeconds());
        MessageDigest normalized = MessageDigest.getInstance("SHA-256");
        long executed = 0L, passed = 0L; boolean timedOut = false; int exit = 0;
        for (long index = assignment.start(); index < assignment.endExclusive(); index++) {
            long remaining = TimeUnit.NANOSECONDS.toSeconds(deadline - System.nanoTime());
            if (remaining <= 0L) { timedOut = true; exit = 1; break; }
            ProcessCapture.Result result = ProcessCapture.run(root, List.of(javaTool(),
                    root.resolve("tools/harness/Gate.java").toString(), "--self-test"),
                    Math.toIntExact(Math.min(Integer.MAX_VALUE, remaining)));
            executed++; String observation = normalize(result.output());
            update(normalized, Long.toString(index)); update(normalized, Long.toString(assignment.seed()));
            update(normalized, observation); boolean accepted = result.exit() == 0
                    && !result.timedOut() && observation.equals("gate-self-test-pass");
            if (accepted) passed++;
            else { timedOut |= result.timedOut(); exit = result.exit() == 0 ? 1 : result.exit(); break; }
        }
        String rootHash = HexFormat.of().formatHex(normalized.digest());
        long elapsed = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started);
        String log = "schema=worldline.structural-instability-result.v1\nplan.sha256="
                + plan.sha256() + "\nshard.index=" + options.shardIndex() + "\nworkload="
                + assignment.workload() + "\nrange=" + assignment.start() + ":"
                + assignment.endExclusive() + "\nseed=" + assignment.seed() + "\nexecuted="
                + executed + "\npassed=" + passed + "\nnormalized.root.sha256=" + rootHash + "\n";
        return new Run(exit, timedOut, elapsed, rootHash, log);
    }

    static void selfTest() throws Exception {
        require(normalize("Gate self-test passed; serialized elapsed=17ms\r\n")
                .equals("gate-self-test-pass"), "instability observation normalization drifted");
        require(normalize("Picked up JAVA_TOOL_OPTIONS: " + CAMPAIGN_JAVA_OPTIONS
                + "\nGate self-test passed; serialized elapsed=17ms\n")
                .equals("gate-self-test-pass"), "campaign JVM banner normalization drifted");
        require(!normalize("Gate self-test failed\n").equals("gate-self-test-pass"),
                "failed instability observation was accepted");
        System.out.println("structural instability campaign self-test passed");
    }

    private String git(String reference) throws Exception {
        String value = ProcessCapture.require(root, List.of("git", "rev-parse", reference), 10).trim();
        require(value.matches("[0-9a-f]{40}"), "invalid Git identity"); return value;
    }
    private static String executorRequest(Path plan) throws Exception {
        String marker = System.getenv("WORLDLINE_CAMPAIGN_EXECUTOR_PLAN");
        if (plan == null) { require(marker == null || marker.isBlank(),
                "unexpected campaign executor plan marker"); return null; }
        require(plan.toString().equals(marker) && Files.isRegularFile(plan)
                && !Files.isSymbolicLink(plan), "campaign executor plan marker drifted");
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(Files.readAllBytes(plan)));
    }
    private static String normalize(String output) {
        String value = output.replace("\r\n", "\n").strip();
        String banner = "Picked up JAVA_TOOL_OPTIONS: " + CAMPAIGN_JAVA_OPTIONS + "\n";
        if (value.startsWith(banner)) value = value.substring(banner.length());
        return value.matches("Gate self-test passed; serialized elapsed=[0-9]+ms")
                ? "gate-self-test-pass" : "unexpected-" + digest(value);
    }
    private static String digest(String value) { try { return HexFormat.of().formatHex(
            MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
    } catch (java.security.NoSuchAlgorithmException error) { throw new IllegalStateException(error); } }
    private static void update(MessageDigest digest, String value) {
        digest.update(value.getBytes(StandardCharsets.UTF_8)); digest.update((byte) 0);
    }
    private static String javaTool() { boolean windows = System.getProperty("os.name", "")
            .toLowerCase(java.util.Locale.ROOT).contains("win"); return Path.of(
            System.getProperty("java.home"), "bin", "java" + (windows ? ".exe" : "")).toString(); }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
    private record Run(int exit, boolean timedOut, long elapsedMs,
            String normalizedRoot, String log) { }
}
