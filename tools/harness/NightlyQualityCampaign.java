import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/** Runs bounded exploratory fuzz and mutation campaigns after the canonical Gate. */
public final class NightlyQualityCampaign {
    private final Path root = Path.of("").toAbsolutePath().normalize();
    private final Path reports = root.resolve(".worldline/reports/nightly-quality");

    public static void main(String[] arguments) {
        try {
            if (Arrays.equals(arguments, new String[] {"--self-test"})) {
                selfTest();
                return;
            }
            new NightlyQualityCampaign().execute(arguments);
        } catch (Exception error) {
            System.err.println("nightly quality campaign failed: " + error.getMessage());
            System.exit(1);
        }
    }

    private void execute(String[] arguments) throws Exception {
        NightlyCampaignOptions options = NightlyCampaignOptions.parse(arguments);
        CampaignSpec spec = CampaignSpec.load(root, "nightly-quality");
        CampaignPlan plan = options.shardCount() == null ? spec.plan(options.seed())
                : spec.plan(options.seed(), options.shardCount());
        require(options.shardIndex() < plan.shardCount(), "shard index is outside the campaign plan");
        if (options.planOnly()) { System.out.print(plan.envelope()); return; }
        CampaignArtifacts.publishPlan(reports, plan);
        CampaignPlan.Assignment fuzzAssignment = assignment(plan, options.shardIndex(), "fuzz");
        CampaignPlan.Assignment mutationAssignment = assignment(plan, options.shardIndex(), "mutation");
        int total = options.budgetSeconds();
        int fuzzBudget = Math.max(1, total / 2), mutationBudget = total - fuzzBudget;
        require(mutationBudget > 0, "budget must cover both campaigns");
        String classpath = classpath();
        int fuzzCases = Math.toIntExact(fuzzAssignment.cases());
        int mutationCases = Math.toIntExact(mutationAssignment.cases());
        Run fuzz = run("fuzz", List.of(javaTool(), "-ea", "-cp", classpath,
                "worldline.fuzz.FuzzTest", "--explore", Long.toString(fuzzAssignment.seed()),
                Integer.toString(fuzzCases), "32"), fuzzBudget, plan, options.shardIndex());
        Run mutation = run("mutation", List.of(javaTool(), "-ea", "-cp", classpath,
                "worldline.testapi.MutationCoverageTest", "--explore",
                Long.toString(mutationAssignment.seed()), Integer.toString(mutationCases)),
                mutationBudget, plan, options.shardIndex());
        String head = gitIdentity("HEAD"), tree = gitIdentity("HEAD^{tree}");
        List<CampaignShardReceipt.Result> results = List.of(
                result(fuzzAssignment, fuzz), result(mutationAssignment, mutation));
        CampaignShardReceipt receipt = CampaignShardReceipt.create(plan, options.shardIndex(),
                head, tree, executorRequest(), results);
        Path receiptPath = CampaignArtifacts.publishReceipt(reports, plan, receipt);
        CampaignReceiptSet.Aggregate aggregate = CampaignArtifacts.aggregateIfComplete(reports, plan);
        require(fuzz.exit == 0 && mutation.exit == 0,
                "campaign failure; inspect " + root.relativize(receiptPath));
        System.out.println("nightly quality campaign shard passed: index=" + options.shardIndex()
                + "/" + plan.shardCount() + ", fuzz=" + fuzzCases + ", mutation=" + mutationCases
                + ", plan=" + plan.sha256() + ", aggregate=" + (aggregate != null));
    }

    private Run run(String name, List<String> command, int budget, CampaignPlan plan,
            int shard) throws Exception {
        long started = System.nanoTime();
        ProcessCapture.Result result = ProcessCapture.run(root, command, budget);
        long elapsed = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started);
        CampaignArtifacts.publishLog(reports, plan, shard, name, result.output());
        return new Run(result.exit(), result.timedOut(), budget, elapsed, sha256(result.output()));
    }

    private String classpath() throws Exception {
        Path tests = root.resolve(".worldline/build/test-classes");
        require(Files.isDirectory(tests), "run the canonical Gate before the nightly campaign");
        Properties config = new Properties();
        try (var reader = Files.newBufferedReader(root.resolve("harness.properties"),
                StandardCharsets.UTF_8)) { config.load(reader); }
        List<String> paths = new ArrayList<>();
        paths.add(tests.toString());
        for (String module : config.getProperty("modules", "").split(",")) {
            Path output = root.resolve(".worldline/build/classes").resolve(module.trim());
            require(Files.isDirectory(output), "missing Gate module output: " + module);
            paths.add(output.toString());
        }
        return String.join(File.pathSeparator, paths);
    }

    private String gitIdentity(String reference) throws Exception {
        String value = ProcessCapture.require(root, List.of("git", "rev-parse", reference), 10).trim();
        require(value.matches("[0-9a-f]{40}"), "invalid Git identity for " + reference);
        return value;
    }

    private static CampaignPlan.Assignment assignment(CampaignPlan plan, int shard, String workload) {
        return plan.shard(shard).assignments().stream()
                .filter(value -> value.workload().equals(workload)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("campaign workload is missing: " + workload));
    }
    private static CampaignShardReceipt.Result result(CampaignPlan.Assignment assignment, Run run) {
        return new CampaignShardReceipt.Result(assignment.workload(), assignment.start(),
                assignment.endExclusive(), assignment.seed(), run.budget, run.elapsed,
                run.exit, run.timedOut, run.outputSha256);
    }

    static void selfTest() {
        NightlyCampaignOptions.selfTest();
        Run run = new Run(124, true, 5, 2L, "c".repeat(64));
        CampaignPlan.Assignment assignment = new CampaignPlan.Assignment("fuzz", 3, 7, 11);
        CampaignShardReceipt.Result result = result(assignment, run);
        require(result.start() == 3 && result.endExclusive() == 7 && result.seed() == 11
                && result.timedOut() && result.resultSha256().equals("c".repeat(64)),
                "nightly shard result drifted");
        System.out.println("nightly quality campaign self-test passed");
    }

    private static String executorRequest() {
        String value = System.getenv("WORLDLINE_CAMPAIGN_EXECUTOR_REQUEST_SHA256");
        if (value == null || value.isBlank()) return null;
        require(value.matches("[0-9a-f]{64}"), "invalid campaign executor request digest");
        return value;
    }
    private static String sha256(String value) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(value.getBytes(StandardCharsets.UTF_8)));
    }
    private static String javaTool() {
        boolean windows = System.getProperty("os.name", "").toLowerCase().contains("win");
        return Path.of(System.getProperty("java.home"), "bin", "java" + (windows ? ".exe" : "")).toString();
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
    private record Run(int exit, boolean timedOut, int budget, long elapsed,
            String outputSha256) { }
}
