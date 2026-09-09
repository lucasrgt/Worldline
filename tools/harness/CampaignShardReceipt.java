import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Canonical non-authoritative result from one exact campaign shard. */
record CampaignShardReceipt(String planSha256, String campaignId, int shardIndex,
        int shardCount, String head, String tree, String status, String executorRequestSha256,
        List<Result> results) {
    private static final String SCHEMA = "worldline.campaign-shard-receipt.v1";

    static CampaignShardReceipt create(CampaignPlan plan, int shardIndex, String head, String tree,
            String executorRequestSha256, List<Result> input) throws Exception {
        require(head.matches("[0-9a-f]{40,64}") && tree.matches("[0-9a-f]{40,64}"),
                "invalid campaign Git identity");
        require(executorRequestSha256 == null || executorRequestSha256.matches("[0-9a-f]{64}"),
                "invalid campaign executor request digest");
        CampaignPlan.Shard shard = plan.shard(shardIndex);
        List<Result> results = input.stream().sorted(Comparator.comparing(Result::workload)).toList();
        require(results.size() == shard.assignments().size(), "campaign shard result count drifted");
        Set<String> seen = new HashSet<>();
        for (int index = 0; index < results.size(); index++) {
            Result result = results.get(index); CampaignPlan.Assignment expected = shard.assignments().get(index);
            require(result.workload.equals(expected.workload()) && result.start == expected.start()
                    && result.endExclusive == expected.endExclusive() && result.seed == expected.seed(),
                    "campaign shard assignment drifted");
            require(seen.add(result.workload), "duplicate campaign workload result");
            require(result.budgetSeconds > 0 && result.elapsedMs >= 0L
                    && result.resultSha256.matches("[0-9a-f]{64}"),
                    "invalid campaign workload result");
        }
        String status = results.stream().allMatch(result -> result.exit == 0 && !result.timedOut)
                ? "passed" : "failed";
        return new CampaignShardReceipt(plan.sha256(), plan.id(), shardIndex, plan.shardCount(),
                head, tree, status, executorRequestSha256, List.copyOf(results));
    }

    static CampaignShardReceipt parse(CampaignPlan plan, String json) throws Exception {
        Map<String, Object> value = MiniJson.object(json); exact(value, "schema", "verdict_authority",
                "plan_sha256", "campaign_id", "shard_index", "shard_count", "head", "tree",
                "status", "executor_request_sha256", "results");
        require(SCHEMA.equals(MiniJson.string(value, "schema"))
                && !MiniJson.bool(value, "verdict_authority"), "campaign receipt authority drifted");
        List<Result> results = new ArrayList<>();
        for (Object item : MiniJson.array(value, "results")) {
            Map<String, Object> row = MiniJson.asObject(item, "campaign result");
            exact(row, "workload", "start", "end_exclusive", "seed", "budget_seconds",
                    "elapsed_ms", "exit", "timed_out", "result_sha256");
            results.add(new Result(MiniJson.string(row, "workload"), MiniJson.integer(row, "start"),
                    MiniJson.integer(row, "end_exclusive"), MiniJson.integer(row, "seed"),
                    Math.toIntExact(MiniJson.integer(row, "budget_seconds")),
                    MiniJson.integer(row, "elapsed_ms"), Math.toIntExact(MiniJson.integer(row, "exit")),
                    MiniJson.bool(row, "timed_out"),
                    MiniJson.string(row, "result_sha256")));
        }
        CampaignShardReceipt receipt = create(plan, Math.toIntExact(MiniJson.integer(value,
                "shard_index")), MiniJson.string(value, "head"), MiniJson.string(value, "tree"),
                nullable(value, "executor_request_sha256"), results);
        require(receipt.planSha256.equals(MiniJson.string(value, "plan_sha256"))
                && receipt.campaignId.equals(MiniJson.string(value, "campaign_id"))
                && receipt.shardCount == Math.toIntExact(MiniJson.integer(value, "shard_count"))
                && receipt.status.equals(MiniJson.string(value, "status")),
                "campaign receipt identity drifted");
        require(receipt.json().equals(json), "campaign receipt is not canonical");
        return receipt;
    }

    String json() {
        StringBuilder out = new StringBuilder("{\"schema\":").append(q(SCHEMA))
                .append(",\"verdict_authority\":false,\"plan_sha256\":").append(q(planSha256))
                .append(",\"campaign_id\":").append(q(campaignId)).append(",\"shard_index\":")
                .append(shardIndex).append(",\"shard_count\":").append(shardCount)
                .append(",\"head\":").append(q(head)).append(",\"tree\":").append(q(tree))
                .append(",\"status\":").append(q(status)).append(",\"executor_request_sha256\":")
                .append(executorRequestSha256 == null ? "null" : q(executorRequestSha256))
                .append(",\"results\":[");
        for (int index = 0; index < results.size(); index++) {
            if (index > 0) out.append(','); out.append(results.get(index).json());
        }
        return out.append("]}").toString();
    }
    String sha256() throws Exception { return HexFormat.of().formatHex(MessageDigest.getInstance(
            "SHA-256").digest(json().getBytes(StandardCharsets.UTF_8))); }

    private static String nullable(Map<String, Object> value, String key) {
        Object item = value.get(key);
        require(item == null || item instanceof String, key + " is not nullable text");
        return (String) item;
    }
    private static void exact(Map<String, Object> value, String... fields) {
        require(new ArrayList<>(value.keySet()).equals(List.of(fields)),
                "campaign receipt fields or order drifted: " + value.keySet());
    }
    private static String q(String value) { return EvidenceExplorerFormat.quote(value); }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalArgumentException(message);
    }

    record Result(String workload, long start, long endExclusive, long seed,
            int budgetSeconds, long elapsedMs, int exit, boolean timedOut, String resultSha256) {
        String json() { return "{\"workload\":" + q(workload) + ",\"start\":" + start
                + ",\"end_exclusive\":" + endExclusive + ",\"seed\":" + seed
                + ",\"budget_seconds\":" + budgetSeconds + ",\"elapsed_ms\":" + elapsedMs
                + ",\"exit\":" + exit + ",\"timed_out\":" + timedOut
                + ",\"result_sha256\":" + q(resultSha256) + "}"; }
        long cases() { return endExclusive - start; }
    }
}
