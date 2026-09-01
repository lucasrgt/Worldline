import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

/** Immutable plan/receipt publication and complete-set aggregation. */
final class CampaignArtifacts {
    private CampaignArtifacts() { }

    static Path planRoot(Path reports, CampaignPlan plan) throws Exception {
        return reports.resolve("plans").resolve(plan.sha256());
    }

    static Path publishPlan(Path reports, CampaignPlan plan) throws Exception {
        Path target = planRoot(reports, plan).resolve("plan.json");
        publish(target, plan.json() + "\n");
        require(CampaignPlan.parse(Files.readString(target, StandardCharsets.UTF_8).trim()).equals(plan),
                "published campaign plan drifted");
        return target;
    }

    static Path publishReceipt(Path reports, CampaignPlan plan,
            CampaignShardReceipt receipt) throws Exception {
        require(receipt.planSha256().equals(plan.sha256()), "campaign receipt plan drifted");
        Path target = planRoot(reports, plan).resolve("shards")
                .resolve("%04d.json".formatted(receipt.shardIndex()));
        publish(target, receipt.json() + "\n");
        require(CampaignShardReceipt.parse(plan, Files.readString(target,
                StandardCharsets.UTF_8).trim()).equals(receipt), "published campaign receipt drifted");
        return target;
    }

    static Path publishLog(Path reports, CampaignPlan plan, int shard, String name,
            String output) throws Exception {
        require(name.matches("[a-z0-9-]+"), "invalid campaign log name");
        Path target = planRoot(reports, plan).resolve("logs").resolve("%04d".formatted(shard))
                .resolve(name + ".log");
        publish(target, output); return target;
    }

    static CampaignReceiptSet.Aggregate aggregateIfComplete(Path reports, CampaignPlan plan)
            throws Exception {
        List<CampaignShardReceipt> receipts = new ArrayList<>();
        Path shards = planRoot(reports, plan).resolve("shards");
        for (int index = 0; index < plan.shardCount(); index++) {
            Path path = shards.resolve("%04d.json".formatted(index));
            if (!Files.exists(path, LinkOption.NOFOLLOW_LINKS)) return null;
            require(Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)
                    && !Files.isSymbolicLink(path) && Files.size(path) <= 1_048_576L,
                    "invalid campaign receipt artifact");
            receipts.add(CampaignShardReceipt.parse(plan,
                    Files.readString(path, StandardCharsets.UTF_8).trim()));
        }
        CampaignReceiptSet.Aggregate aggregate = CampaignReceiptSet.aggregate(plan, receipts);
        publish(planRoot(reports, plan).resolve("aggregate.json"), aggregate.json() + "\n");
        return aggregate;
    }

    static void selfTest() throws Exception {
        Path root = Files.createTempDirectory("worldline-campaign-artifacts-");
        try {
            CampaignPlan plan = CampaignPlan.create("fixture", 7L, 1, List.of(
                    new CampaignPlan.Workload("fuzz", "protocol-fuzz", 2)));
            CampaignPlan.Assignment assignment = plan.shard(0).assignments().get(0);
            CampaignShardReceipt receipt = CampaignShardReceipt.create(plan, 0, "a".repeat(40),
                    "b".repeat(40), null, List.of(new CampaignShardReceipt.Result("fuzz",
                            assignment.start(), assignment.endExclusive(), assignment.seed(),
                            5, 3L, 0, false, "c".repeat(64))));
            publishPlan(root, plan); publishPlan(root, plan);
            publishReceipt(root, plan, receipt); publishReceipt(root, plan, receipt);
            CampaignReceiptSet.Aggregate aggregate = aggregateIfComplete(root, plan);
            require(aggregate != null && aggregate.passed() == 1 && aggregate.cases() == 2,
                    "campaign artifact aggregate drifted");
            expectFailure(() -> publish(planRoot(root, plan).resolve("plan.json"), "changed\n"));
        } finally { SafeTreeDelete.delete(root); }
    }

    private static void publish(Path target, String text) throws Exception {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8); Files.createDirectories(target.getParent());
        if (Files.exists(target, LinkOption.NOFOLLOW_LINKS)) {
            require(Files.isRegularFile(target, LinkOption.NOFOLLOW_LINKS)
                    && !Files.isSymbolicLink(target)
                    && java.util.Arrays.equals(bytes, Files.readAllBytes(target)),
                    "immutable campaign artifact collision: " + target.getFileName()); return; }
        Path temporary = target.resolveSibling(target.getFileName() + ".tmp-"
                + ProcessHandle.current().pid() + "-" + System.nanoTime());
        try {
            Files.write(temporary, bytes, StandardOpenOption.CREATE_NEW);
            try { Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE); }
            catch (AtomicMoveNotSupportedException error) { Files.move(temporary, target); }
        } catch (java.io.IOException error) {
            require(Files.isRegularFile(target, LinkOption.NOFOLLOW_LINKS)
                    && !Files.isSymbolicLink(target)
                    && java.util.Arrays.equals(bytes, Files.readAllBytes(target)),
                    "immutable campaign artifact collision: " + target.getFileName());
        } finally { Files.deleteIfExists(temporary); }
    }

    private static void expectFailure(Action action) throws Exception { try { action.run();
        throw new IllegalStateException("campaign artifact collision was accepted");
    } catch (IllegalArgumentException expected) { } }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalArgumentException(message);
    }
    @FunctionalInterface private interface Action { void run() throws Exception; }
}
