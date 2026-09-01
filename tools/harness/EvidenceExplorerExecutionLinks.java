import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Validated local links between campaign receipts, logs, and native executor evidence. */
final class EvidenceExplorerExecutionLinks {
    private static final List<String> RECEIPT_FIELDS = List.of("schema", "request_sha256",
            "executor_version", "backend", "exit_code", "termination", "stdout_sha256",
            "stderr_sha256", "metrics_sha256", "output_manifest_sha256", "stdout_observed_bytes",
            "stderr_observed_bytes", "stdout_truncated", "stderr_truncated",
            "descendants_remaining", "error");
    private static final List<String> METRIC_FIELDS = List.of("schema", "elapsed_ms", "bootstrap_ms",
            "runtime_ms", "peak_memory_bytes", "memory_measurement", "peak_processes", "peak_tasks",
            "cpu_user_100ns", "cpu_kernel_100ns", "io_read_bytes", "io_write_bytes",
            "memory_limit_events", "oom_kill_events", "task_limit_events", "cpu_throttled_100ns",
            "stdout_observed_bytes", "stderr_observed_bytes", "stdout_truncated", "stderr_truncated",
            "telemetry_qualification");

    private EvidenceExplorerExecutionLinks() { }

    public static void main(String[] arguments) {
        try {
            require(List.of(arguments).equals(List.of("--self-test")),
                    "usage: EvidenceExplorerExecutionLinks --self-test");
            selfTest();
        } catch (Exception error) {
            System.err.println("execution link self-test failed: " + error.getMessage()); System.exit(1);
        }
    }

    static Snapshot build(Path root) throws Exception {
        Campaigns campaigns = campaigns(root.resolve(".worldline/reports/nightly-quality/plans"));
        Path runs = root.resolve(".worldline/reports/nightly-quality/executor-runs");
        List<Link> links = new ArrayList<>(); Set<String> nativeRequests = new HashSet<>();
        int partial = 0;
        for (Path run : directories(runs)) {
            Path plan = run.resolve("plan.json"), output = run.resolve("output");
            if (!complete(plan, output)) { partial++; continue; }
            Native value = nativeEvidence(run, plan, output);
            require(nativeRequests.add(value.request), "duplicate native executor request");
            Binding binding = campaigns.bindings.get(value.request);
            links.add(new Link(value.request, binding == null ? null : binding.campaign,
                    binding == null ? null : binding.plan, binding == null ? null : binding.shard,
                    value.backend, value.version, value.termination, value.elapsed,
                    value.peakMemory, value.peakProcesses));
        }
        links.sort(Comparator.comparing(Link::requestSha256));
        int linked = (int) links.stream().filter(value -> value.campaignId != null).count();
        int dangling = (int) campaigns.bindings.keySet().stream().filter(value ->
                !nativeRequests.contains(value)).count();
        return new Snapshot(links.size(), linked, links.size() - linked, partial,
                campaigns.bindings.size(), dangling, campaigns.referencedLogs,
                campaigns.orphanLogs, campaigns.missingLogs, List.copyOf(links));
    }

    static void selfTest() throws Exception {
        Path root = Files.createTempDirectory("worldline-execution-links-").toAbsolutePath().normalize();
        try {
            Path run = root.resolve(".worldline/reports/nightly-quality/executor-runs/run-fixture");
            Path output = run.resolve("output"), planPath = run.resolve("plan.json");
            Files.createDirectories(output); Files.writeString(output.resolve("stdout.log"), "ok\n");
            Files.writeString(output.resolve("stderr.log"), "");
            String metrics = fixtureMetrics(); Files.writeString(output.resolve("metrics.json"), metrics);
            Files.writeString(output.resolve("manifest.json"), "{}\n");
            String plan = fixturePlan(root, output); Files.writeString(planPath, plan);
            String request = EvidenceExplorerSnapshot.sha256(planPath);
            Files.writeString(output.resolve("receipt.json"), fixtureReceipt(output, request));
            CampaignPlan campaign = CampaignPlan.create("fixture", 7L, 1, List.of(
                    new CampaignPlan.Workload("fuzz", "protocol-fuzz", 2)));
            Path reports = root.resolve(".worldline/reports/nightly-quality");
            CampaignArtifacts.publishPlan(reports, campaign);
            CampaignPlan.Assignment assignment = campaign.shard(0).assignments().get(0);
            Path log = CampaignArtifacts.publishLog(reports, campaign, 0, "fuzz", "ok\n");
            CampaignShardReceipt receipt = CampaignShardReceipt.create(campaign, 0, "a".repeat(40),
                    "b".repeat(40), request, List.of(new CampaignShardReceipt.Result("fuzz",
                            assignment.start(), assignment.endExclusive(), assignment.seed(), 5, 3L, 0,
                            false, EvidenceExplorerSnapshot.sha256(log))));
            CampaignArtifacts.publishReceipt(reports, campaign, receipt);
            Path extra = log.resolveSibling("orphan.log"); Files.writeString(extra, "orphan\n");
            Files.createDirectories(run.resolveSibling("run-partial"));
            Snapshot value = build(root);
            require(value.nativeRuns == 1 && value.linkedRuns == 1 && value.partialNativeRuns == 1
                    && value.referencedLogs == 1 && value.orphanLogs == 1 && value.missingLogs == 0,
                    "execution link classification drifted");
            Files.writeString(output.resolve("stdout.log"), "changed\n");
            expectFailure(() -> build(root));
        } finally { SafeTreeDelete.delete(root); }
    }

    private static String fixturePlan(Path root, Path output) {
        return "{\"schema\":\"worldline.execution-plan.v1\",\"job_id\":\"fixture\","
                + "\"working_directory\":" + q(root.toString()) + ",\"argv\":[\"java\"],"
                + "\"environment\":{},\"limits\":{},\"isolation\":{"
                + "\"backend\":\"windows_job\",\"network\":\"inherit\",\"owner_pid\":1},"
                + "\"filesystem\":{},\"outputs\":{\"root\":" + q(output.toString())
                + ",\"stdout\":" + q(output.resolve("stdout.log").toString())
                + ",\"stderr\":" + q(output.resolve("stderr.log").toString())
                + ",\"metrics\":" + q(output.resolve("metrics.json").toString())
                + ",\"manifest\":" + q(output.resolve("manifest.json").toString())
                + ",\"receipt\":" + q(output.resolve("receipt.json").toString()) + "}}\n";
    }
    private static String fixtureMetrics() {
        return "{\"schema\":\"worldline.execution-metrics.v2\",\"elapsed_ms\":3,"
                + "\"bootstrap_ms\":1,\"runtime_ms\":2,\"peak_memory_bytes\":4,"
                + "\"memory_measurement\":\"job_commit\",\"peak_processes\":1,\"peak_tasks\":null,"
                + "\"cpu_user_100ns\":1,\"cpu_kernel_100ns\":1,\"io_read_bytes\":0,"
                + "\"io_write_bytes\":0,\"memory_limit_events\":null,\"oom_kill_events\":null,"
                + "\"task_limit_events\":null,\"cpu_throttled_100ns\":null,"
                + "\"stdout_observed_bytes\":3,\"stderr_observed_bytes\":0,"
                + "\"stdout_truncated\":false,\"stderr_truncated\":false,"
                + "\"telemetry_qualification\":\"observational\"}\n";
    }
    private static String fixtureReceipt(Path output, String request) throws Exception {
        return "{\"schema\":\"worldline.execution-receipt.v1\",\"request_sha256\":" + q(request)
                + ",\"executor_version\":\"fixture\",\"backend\":\"windows_job\",\"exit_code\":0,"
                + "\"termination\":\"exited\",\"stdout_sha256\":" + digest(output, "stdout.log")
                + ",\"stderr_sha256\":" + digest(output, "stderr.log") + ",\"metrics_sha256\":"
                + digest(output, "metrics.json") + ",\"output_manifest_sha256\":"
                + digest(output, "manifest.json") + ",\"stdout_observed_bytes\":3,"
                + "\"stderr_observed_bytes\":0,\"stdout_truncated\":false,"
                + "\"stderr_truncated\":false,\"descendants_remaining\":0,\"error\":null}\n";
    }
    private static String digest(Path root, String name) throws Exception {
        return q(EvidenceExplorerSnapshot.sha256(root.resolve(name)));
    }
    private static String q(String value) { return EvidenceExplorerFormat.quote(value); }

    private static Campaigns campaigns(Path plans) throws Exception {
        Map<String, Binding> bindings = new HashMap<>(); Set<Path> referenced = new HashSet<>();
        int missing = 0;
        for (Path directory : directories(plans)) {
            Path planPath = directory.resolve("plan.json");
            if (!Files.exists(planPath, LinkOption.NOFOLLOW_LINKS)) continue;
            CampaignPlan plan = CampaignPlan.parse(read(planPath, 1L << 20).trim());
            require(directory.getFileName().toString().equals(plan.sha256()),
                    "campaign plan directory identity drifted");
            Path shards = directory.resolve("shards");
            for (Path receiptPath : files(shards)) {
                require(receiptPath.getFileName().toString().matches("[0-9]{4}[.]json"),
                        "invalid campaign receipt filename");
                CampaignShardReceipt receipt = CampaignShardReceipt.parse(plan,
                        read(receiptPath, 1L << 20).trim());
                require(receiptPath.getFileName().toString().equals(
                        "%04d.json".formatted(receipt.shardIndex())), "campaign receipt path drifted");
                if (receipt.executorRequestSha256() != null) require(bindings.put(
                        receipt.executorRequestSha256(), new Binding(receipt.campaignId(),
                                receipt.planSha256(), receipt.shardIndex())) == null,
                        "duplicate campaign executor binding");
                for (CampaignShardReceipt.Result result : receipt.results()) {
                    Path log = directory.resolve("logs").resolve("%04d".formatted(receipt.shardIndex()))
                            .resolve(result.workload() + ".log").toAbsolutePath().normalize();
                    if (!Files.exists(log, LinkOption.NOFOLLOW_LINKS)) { missing++; continue; }
                    require(regular(log) && EvidenceExplorerSnapshot.sha256(log).equals(
                            result.resultSha256()), "campaign workload log digest drifted");
                    referenced.add(log);
                }
            }
        }
        int allLogs = 0;
        for (Path directory : directories(plans)) for (Path shard : directories(directory.resolve("logs"))) {
            for (Path log : files(shard)) {
                require(log.getFileName().toString().matches("[a-z0-9-]+[.]log") && regular(log),
                        "invalid campaign workload log");
                allLogs++;
            }
        }
        return new Campaigns(bindings, referenced.size(), allLogs - referenced.size(), missing);
    }

    private static Native nativeEvidence(Path run, Path planPath, Path output) throws Exception {
        Map<String, Object> plan = MiniJson.object(read(planPath, 1L << 20));
        exact(plan, "schema", "job_id", "working_directory", "argv", "environment", "limits",
                "isolation", "filesystem", "outputs");
        require("worldline.execution-plan.v1".equals(MiniJson.string(plan, "schema")),
                "native plan schema drifted");
        Map<String, Object> outputs = MiniJson.asObject(plan.get("outputs"), "native outputs");
        exact(outputs, "root", "stdout", "stderr", "metrics", "manifest", "receipt");
        require(Path.of(MiniJson.string(outputs, "root")).toAbsolutePath().normalize().equals(output),
                "native output root drifted");
        Map<String, Object> isolation = MiniJson.asObject(plan.get("isolation"), "native isolation");
        exact(isolation, "backend", "network", "owner_pid"); String backend = MiniJson.string(isolation, "backend");
        String request = EvidenceExplorerSnapshot.sha256(planPath);
        Path receiptPath = output.resolve("receipt.json"), metricsPath = output.resolve("metrics.json");
        Map<String, Object> receipt = MiniJson.object(read(receiptPath, 1L << 20)); exact(receipt, RECEIPT_FIELDS);
        require("worldline.execution-receipt.v1".equals(MiniJson.string(receipt, "schema"))
                && request.equals(MiniJson.string(receipt, "request_sha256"))
                && backend.equals(MiniJson.string(receipt, "backend")), "native receipt identity drifted");
        verifyDigest(output.resolve("stdout.log"), receipt, "stdout_sha256");
        verifyDigest(output.resolve("stderr.log"), receipt, "stderr_sha256");
        verifyDigest(metricsPath, receipt, "metrics_sha256");
        verifyDigest(output.resolve("manifest.json"), receipt, "output_manifest_sha256");
        Map<String, Object> metrics = MiniJson.object(read(metricsPath, 1L << 20)); exact(metrics, METRIC_FIELDS);
        require("worldline.execution-metrics.v2".equals(MiniJson.string(metrics, "schema"))
                && "observational".equals(MiniJson.string(metrics, "telemetry_qualification")),
                "native metrics authority drifted");
        return new Native(request, backend, MiniJson.string(receipt, "executor_version"),
                MiniJson.string(receipt, "termination"), MiniJson.integer(metrics, "elapsed_ms"),
                nullableInteger(metrics, "peak_memory_bytes"), nullableInteger(metrics, "peak_processes"));
    }

    private static boolean complete(Path plan, Path output) {
        return regular(plan) && regular(output.resolve("receipt.json")) && regular(output.resolve("metrics.json"))
                && regular(output.resolve("manifest.json")) && regular(output.resolve("stdout.log"))
                && regular(output.resolve("stderr.log"));
    }
    private static void verifyDigest(Path path, Map<String, Object> receipt, String field) throws Exception {
        require(regular(path) && EvidenceExplorerSnapshot.sha256(path).equals(MiniJson.string(receipt, field)),
                "native output digest drifted: " + field);
    }
    private static Long nullableInteger(Map<String, Object> value, String key) {
        Object item = value.get(key); require(item == null || item instanceof Long, key + " is not nullable integer");
        return (Long) item;
    }
    private static String read(Path path, long maximum) throws Exception {
        require(regular(path) && Files.size(path) <= maximum, "invalid or oversized local evidence file");
        return Files.readString(path, StandardCharsets.UTF_8);
    }
    private static boolean regular(Path path) { return Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)
            && !Files.isSymbolicLink(path); }
    private static List<Path> directories(Path root) throws Exception { return entries(root, true); }
    private static List<Path> files(Path root) throws Exception { return entries(root, false); }
    private static List<Path> entries(Path root, boolean directories) throws Exception {
        if (!Files.exists(root, LinkOption.NOFOLLOW_LINKS)) return List.of();
        require(Files.isDirectory(root, LinkOption.NOFOLLOW_LINKS) && !Files.isSymbolicLink(root),
                "invalid local evidence directory");
        List<Path> values = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(root)) {
            for (Path path : stream) if (directories == Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
                require(!Files.isSymbolicLink(path), "linked local evidence entry");
                values.add(path.toAbsolutePath().normalize());
            }
        }
        values.sort(Comparator.comparing(path -> path.getFileName().toString())); return values;
    }
    private static void exact(Map<String, Object> value, String... fields) {
        exact(value, List.of(fields));
    }
    private static void exact(Map<String, Object> value, List<String> fields) {
        require(new ArrayList<>(value.keySet()).equals(fields), "local evidence fields drifted");
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalArgumentException(message);
    }
    private static void expectFailure(Action action) throws Exception { try { action.run();
        throw new IllegalStateException("corrupt execution link evidence was accepted");
    } catch (IllegalArgumentException expected) { } }
    @FunctionalInterface private interface Action { void run() throws Exception; }

    record Snapshot(int nativeRuns, int linkedRuns, int orphanNativeRuns, int partialNativeRuns,
            int campaignBindings, int danglingCampaignBindings, int referencedLogs,
            int orphanLogs, int missingLogs, List<Link> links) { }
    record Link(String requestSha256, String campaignId, String campaignPlanSha256, Integer shardIndex,
            String backend, String executorVersion, String termination, long elapsedMs,
            Long peakMemoryBytes, Long peakProcesses) { }
    private record Binding(String campaign, String plan, int shard) { }
    private record Campaigns(Map<String, Binding> bindings, int referencedLogs, int orphanLogs, int missingLogs) { }
    private record Native(String request, String backend, String version, String termination,
            long elapsed, Long peakMemory, Long peakProcesses) { }
}
