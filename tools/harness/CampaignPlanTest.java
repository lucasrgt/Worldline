import java.util.ArrayList;
import java.util.List;

/** Partition, identity, tamper, and receipt-set invariants for campaign plans. */
final class CampaignPlanTest {
    private CampaignPlanTest() { }

    static void selfTest() throws Exception {
        CampaignPlan plan = CampaignPlan.create("fixture", 173L, 3, List.of(
                new CampaignPlan.Workload("mutation", "mutation", 8),
                new CampaignPlan.Workload("fuzz", "protocol-fuzz", 10)));
        require(plan.shard(0).assignments().get(0).start() == 0
                && plan.shard(0).assignments().get(0).endExclusive() == 4
                && plan.shard(1).assignments().get(0).start() == 4
                && plan.shard(2).assignments().get(0).endExclusive() == 10,
                "campaign remainder partition drifted");
        require(CampaignPlan.parse(plan.json()).equals(plan), "campaign plan round-trip drifted");
        CampaignPlan nightly = CampaignPlan.create("nightly-quality", 173L, 3, List.of(
                new CampaignPlan.Workload("fuzz", "protocol-fuzz", 4_096),
                new CampaignPlan.Workload("mutation", "mutation", 100_000)));
        require(nightly.sha256().equals(
                "ebc7330c479f65db7e45204078cc3e418e4f32000bd9267fcdcf2eb1aa34286d"),
                "campaign plan fixture identity drifted");
        expectFailure(() -> CampaignPlan.parse(plan.json().replace("\"java\"", "\"rust\"")));
        require(!CampaignPlan.create("fixture", 174L, 3, plan.workloads()).sha256().equals(plan.sha256()),
                "campaign root seed did not affect identity");
        receiptSet(plan);
        NightlyCampaignOptions.selfTest();
        CampaignArtifacts.selfTest();
        System.out.println("  campaign plan self-test: passed");
    }

    private static void receiptSet(CampaignPlan plan) throws Exception {
        List<CampaignShardReceipt> receipts = new ArrayList<>();
        for (int index = 0; index < plan.shardCount(); index++) {
            List<CampaignShardReceipt.Result> results = plan.shard(index).assignments().stream()
                    .map(value -> new CampaignShardReceipt.Result(value.workload(), value.start(),
                            value.endExclusive(), value.seed(), 5, 2L, 0, false,
                            "c".repeat(64))).toList();
            receipts.add(CampaignShardReceipt.create(plan, index, "a".repeat(40),
                    "b".repeat(40), null, results));
        }
        require(CampaignShardReceipt.parse(plan, receipts.get(0).json()).equals(receipts.get(0)),
                "campaign receipt round-trip drifted");
        expectFailure(() -> CampaignShardReceipt.parse(plan,
                receipts.get(0).json().replace("\"passed\"", "\"failed\"")));
        String forward = CampaignReceiptSet.aggregate(plan, receipts).json();
        List<CampaignShardReceipt> reversed = new ArrayList<>(receipts);
        java.util.Collections.reverse(reversed);
        require(forward.equals(CampaignReceiptSet.aggregate(plan, reversed).json()),
                "campaign receipt arrival order affected aggregate");
        expectFailure(() -> CampaignReceiptSet.aggregate(plan, receipts.subList(0, 2)));
        List<CampaignShardReceipt> duplicate = List.of(receipts.get(0), receipts.get(0), receipts.get(2));
        expectFailure(() -> CampaignReceiptSet.aggregate(plan, duplicate));
    }

    private static void expectFailure(Checked action) throws Exception { try { action.run();
        throw new IllegalStateException("invalid campaign evidence was accepted");
    } catch (IllegalArgumentException expected) { } }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalArgumentException(message);
    }
    @FunctionalInterface private interface Checked { void run() throws Exception; }
}
