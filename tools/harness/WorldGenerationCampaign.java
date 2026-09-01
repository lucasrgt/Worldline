import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/** Replays one sealed official world-generation matrix through the canonical Gate. */
public final class WorldGenerationCampaign {
    private static final String TARGET = "m634-cross-lane-seed-matrix";
    private final Path root = Path.of("").toAbsolutePath().normalize();
    private final Path reports = root.resolve(".worldline/reports/worldgen-matrix");

    public static void main(String[] arguments) {
        try {
            if (List.of(arguments).equals(List.of("--self-test"))) { selfTest(); return; }
            new WorldGenerationCampaign().execute(arguments);
        } catch (Exception error) {
            System.err.println("world-generation campaign failed: " + error.getMessage());
            System.exit(1);
        }
    }

    private void execute(String[] arguments) throws Exception {
        CampaignOptions options = CampaignOptions.parse(arguments);
        CampaignSpec spec = CampaignSpec.load(root, "worldgen-matrix");
        CampaignPlan plan = options.shardCount() == null ? spec.plan(options.seed())
                : spec.plan(options.seed(), options.shardCount());
        require(options.shardIndex() < plan.shardCount(), "shard index is outside the campaign plan");
        if (options.planOnly()) { System.out.print(plan.envelope()); return; }
        Contract contract = contract(); CampaignArtifacts.publishPlan(reports, plan);
        CampaignPlan.Assignment assignment = plan.shard(options.shardIndex()).assignments().stream()
                .filter(value -> value.workload().equals(TARGET)).findFirst().orElseThrow();
        Run run = replay(plan, assignment, contract, options);
        Path log = CampaignArtifacts.publishLog(reports, plan, options.shardIndex(), TARGET, run.log);
        CampaignShardReceipt.Result result = new CampaignShardReceipt.Result(TARGET,
                assignment.start(), assignment.endExclusive(), assignment.seed(),
                options.budgetSeconds(), run.elapsedMs, run.exit, run.timedOut,
                EvidenceExplorerSnapshot.sha256(log));
        CampaignShardReceipt receipt = CampaignShardReceipt.create(plan, options.shardIndex(),
                git("HEAD"), git("HEAD^{tree}"), executorRequest(options.executorPlan()), List.of(result));
        Path receiptPath = CampaignArtifacts.publishReceipt(reports, plan, receipt);
        CampaignReceiptSet.Aggregate aggregate = CampaignArtifacts.aggregateIfComplete(reports, plan);
        require(run.exit == 0, "world-generation replay failed; inspect "
                + root.relativize(receiptPath) + "\n" + run.failureTail);
        System.out.println("world-generation shard passed: index=" + options.shardIndex()
                + "/" + plan.shardCount() + ", replays=" + assignment.cases()
                + ", contract=" + contract.signature + ", plan=" + plan.sha256()
                + ", aggregate=" + (aggregate != null));
    }

    private Run replay(CampaignPlan plan, CampaignPlan.Assignment assignment,
            Contract contract, CampaignOptions options) throws Exception {
        long started = System.nanoTime(), deadline = started
                + TimeUnit.SECONDS.toNanos(options.budgetSeconds());
        MessageDigest normalized = MessageDigest.getInstance("SHA-256");
        long executed = 0L, passed = 0L; boolean timedOut = false; int exit = 0;
        String failureDigest = "-", failureTail = "";
        for (long index = assignment.start(); index < assignment.endExclusive(); index++) {
            long remaining = TimeUnit.NANOSECONDS.toSeconds(deadline - System.nanoTime());
            if (remaining <= 0L) { timedOut = true; exit = 1; break; }
            ProcessCapture.Result result = ProcessCapture.run(root, List.of(javaTool(),
                    root.resolve("tools/harness/Gate.java").toString(), "--milestone", TARGET),
                    Math.toIntExact(Math.min(Integer.MAX_VALUE, remaining)));
            executed++; boolean accepted = result.exit() == 0 && !result.timedOut();
            update(normalized, Long.toString(index)); update(normalized, Long.toString(assignment.seed()));
            update(normalized, TARGET); update(normalized, contract.signature);
            failureDigest = accepted ? "-" : sha256(result.output());
            update(normalized, accepted ? "gate-milestone-pass" : "gate-milestone-fail-" + failureDigest);
            if (accepted) passed++;
            else { timedOut |= result.timedOut(); exit = result.exit() == 0 ? 1 : result.exit();
                failureTail = ProcessCapture.tail(result.output(), 8_000); break; }
        }
        String rootHash = HexFormat.of().formatHex(normalized.digest());
        long elapsed = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started);
        String log = "schema=worldline.worldgen-matrix-result.v1\nplan.sha256=" + plan.sha256()
                + "\nshard.index=" + options.shardIndex() + "\nworkload=" + TARGET
                + "\nrange=" + assignment.start() + ":" + assignment.endExclusive()
                + "\nseed=" + assignment.seed() + "\nexecuted=" + executed + "\npassed=" + passed
                + "\ncontract.signature=" + contract.signature
                + "\nfailure.output.sha256=" + failureDigest
                + "\nverdict.authority=false\nnormalized.root.sha256=" + rootHash + "\n";
        return new Run(exit, timedOut, elapsed, rootHash, log, failureTail);
    }

    private Contract contract() throws Exception {
        Properties values = new Properties(); Path descriptor = root.resolve("smokes").resolve(TARGET)
                .resolve("smoke.properties");
        try (Reader reader = Files.newBufferedReader(descriptor, StandardCharsets.UTF_8)) {
            values.load(reader);
        }
        String signature = values.getProperty("expected.signature", "");
        require(TARGET.equals(values.getProperty("id"))
                && "official-cycle".equals(values.getProperty("qualification.proof"))
                && List.of(values.getProperty("atlas.subsystems", "").split(",")).contains("worldgen")
                && signature.matches("[0-9a-f]{64}"), "world-generation contract drifted");
        return new Contract(signature);
    }

    static void selfTest() throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        update(digest, "0"); update(digest, "7"); update(digest, TARGET);
        update(digest, "a".repeat(64)); update(digest, "gate-milestone-pass");
        require(HexFormat.of().formatHex(digest.digest()).matches("[0-9a-f]{64}"),
                "world-generation normalized root drifted");
        System.out.println("world-generation campaign self-test passed");
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
    private static void update(MessageDigest digest, String value) {
        digest.update(value.getBytes(StandardCharsets.UTF_8)); digest.update((byte) 0);
    }
    private static String sha256(String value) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(value.getBytes(StandardCharsets.UTF_8)));
    }
    private static String javaTool() { boolean windows = System.getProperty("os.name", "")
            .toLowerCase(java.util.Locale.ROOT).contains("win"); return Path.of(
            System.getProperty("java.home"), "bin", "java" + (windows ? ".exe" : "")).toString(); }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
    private record Contract(String signature) { }
    private record Run(int exit, boolean timedOut, long elapsedMs,
            String normalizedRoot, String log, String failureTail) { }
}
