import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;

/** Arrival-order-independent validation and aggregation of a complete campaign plan. */
final class CampaignReceiptSet {
    private CampaignReceiptSet() { }

    static Aggregate aggregate(CampaignPlan plan, List<CampaignShardReceipt> input) throws Exception {
        require(input.size() == plan.shardCount(), "campaign receipt set is incomplete");
        List<CampaignShardReceipt> receipts = input.stream()
                .sorted(Comparator.comparingInt(CampaignShardReceipt::shardIndex)).toList();
        Set<Integer> seen = new HashSet<>(); String head = null, tree = null;
        long cases = 0L; int passed = 0;
        for (CampaignShardReceipt supplied : receipts) {
            CampaignShardReceipt receipt = CampaignShardReceipt.parse(plan, supplied.json());
            require(seen.add(receipt.shardIndex()), "duplicate campaign shard receipt");
            require(receipt.shardIndex() >= 0 && receipt.shardIndex() < plan.shardCount()
                    && receipt.shardCount() == plan.shardCount()
                    && receipt.planSha256().equals(plan.sha256())
                    && receipt.campaignId().equals(plan.id()), "campaign receipt plan drifted");
            CampaignPlan.Shard shard = plan.shard(receipt.shardIndex());
            require(receipt.results().size() == shard.assignments().size(),
                    "campaign receipt assignment count drifted");
            for (int index = 0; index < receipt.results().size(); index++) {
                CampaignShardReceipt.Result result = receipt.results().get(index);
                CampaignPlan.Assignment assignment = shard.assignments().get(index);
                require(result.workload().equals(assignment.workload())
                        && result.start() == assignment.start()
                        && result.endExclusive() == assignment.endExclusive()
                        && result.seed() == assignment.seed(), "campaign receipt assignment drifted");
                cases = Math.addExact(cases, result.cases());
            }
            if (head == null) { head = receipt.head(); tree = receipt.tree(); }
            require(head.equals(receipt.head()) && tree.equals(receipt.tree()),
                    "campaign receipts belong to different Git trees");
            if (receipt.status().equals("passed")) passed++;
            else require(receipt.status().equals("failed"), "unknown campaign receipt status");
        }
        long expected = plan.workloads().stream().mapToLong(CampaignPlan.Workload::cases).sum();
        require(cases == expected, "campaign receipt case coverage drifted");
        return new Aggregate(plan.sha256(), head, tree, receipts.size(), passed,
                receipts.size() - passed, cases, List.copyOf(receipts));
    }

    record Aggregate(String planSha256, String head, String tree, int shards,
            int passed, int failed, long cases, List<CampaignShardReceipt> receipts) {
        String json() throws Exception {
            StringBuilder out = new StringBuilder("{\"schema\":\"worldline.campaign-aggregate.v1\",")
                    .append("\"verdict_authority\":false,\"plan_sha256\":").append(q(planSha256))
                    .append(",\"head\":").append(q(head)).append(",\"tree\":").append(q(tree))
                    .append(",\"shards\":").append(shards).append(",\"passed\":").append(passed)
                    .append(",\"failed\":").append(failed).append(",\"cases\":").append(cases)
                    .append(",\"receipt_sha256\":[");
            for (int index = 0; index < receipts.size(); index++) {
                if (index > 0) out.append(','); out.append(q(receipts.get(index).sha256()));
            }
            return out.append("]}").toString();
        }
        String sha256() throws Exception { return HexFormat.of().formatHex(MessageDigest.getInstance(
                "SHA-256").digest(json().getBytes(StandardCharsets.UTF_8))); }
    }

    private static String q(String value) { return EvidenceExplorerFormat.quote(value); }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalArgumentException(message);
    }
}
