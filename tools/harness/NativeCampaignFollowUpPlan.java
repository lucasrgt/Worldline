import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Comparator;
import java.util.List;

/** Review-only follow-up suggestions derived from validated native campaign clusters. */
final class NativeCampaignFollowUpPlan {
    private NativeCampaignFollowUpPlan() { }

    public static void main(String[] arguments) {
        try {
            require(arguments.length == 0, "usage: NativeCampaignFollowUpPlan");
            Path root = Path.of("").toAbsolutePath().normalize();
            NativeCampaignFailureClusters.Snapshot failures = NativeCampaignFailureClusters.summarize(
                    EvidenceExplorerExecutionLinks.build(root));
            System.out.write(json(build(failures)).getBytes(StandardCharsets.UTF_8));
        } catch (Exception error) {
            System.err.println("native campaign follow-up planning failed: " + error.getMessage());
            System.exit(1);
        }
    }

    static Snapshot build(NativeCampaignFailureClusters.Snapshot failures) {
        List<Item> items = failures.clusters().stream().map(NativeCampaignFollowUpPlan::item)
                .sorted(Comparator.comparing(Item::id)).toList();
        return new Snapshot(items);
    }

    static String json(Snapshot value) {
        StringBuilder out = new StringBuilder(
                "{\"schema\":\"worldline.native-campaign-follow-up-plan.v1\"")
                .append(",\"verdict_authority\":false,\"admission_effect\":false")
                .append(",\"automatic_execution\":false,\"queue_mutation\":false")
                .append(",\"item_count\":").append(value.items().size()).append(",\"items\":[");
        for (int index = 0; index < value.items().size(); index++) {
            if (index > 0) out.append(','); out.append(value.items().get(index).json());
        }
        return out.append("]}\n").toString();
    }

    private static Item item(NativeCampaignFailureClusters.Cluster cluster) {
        require(!cluster.requests().isEmpty(), "follow-up cluster has no request");
        String action = cluster.domain().equals("unclassified-workload")
                ? "prepare-minimization-review" : "repeat-exact-request";
        String request = cluster.requests().get(0);
        String material = "schema=worldline.native-campaign-follow-up-item.v1\ncluster="
                + cluster.id() + "\naction=" + action + "\nrequest=" + request
                + "\nattempt-limit=1\nrequires-review=true\n";
        return new Item(sha256(material), cluster.id(), cluster.campaignId(), cluster.domain(),
                cluster.failureClass(), action, request, cluster.requests().size(), 1L, true);
    }

    static void selfTest() {
        NativeCampaignFailureClusters.Cluster workload = cluster(2, "unclassified-workload",
                "workload-nonzero-exit", List.of("2".repeat(64), "3".repeat(64)));
        NativeCampaignFailureClusters.Cluster timeout = cluster(1, "executor-infrastructure",
                "timeout", List.of("1".repeat(64)));
        NativeCampaignFailureClusters.Snapshot failures = new NativeCampaignFailureClusters.Snapshot(
                3L, 1L, 2L, List.of(workload, timeout));
        Snapshot value = build(failures); String json = json(value);
        require(value.items().size() == 2
                && json.equals(json(build(new NativeCampaignFailureClusters.Snapshot(
                        3L, 1L, 2L, List.of(timeout, workload)))))
                && json.contains("\"automatic_execution\":false")
                && json.contains("\"queue_mutation\":false")
                && json.contains("\"action\":\"prepare-minimization-review\"")
                && json.contains("\"action\":\"repeat-exact-request\"")
                && json.contains("\"attempt_limit\":1")
                && !json.contains("working_directory") && !json.contains("argv"),
                "native campaign follow-up authority or identity drifted");
        System.out.println("  native campaign follow-up plan self-test: passed");
    }

    private static NativeCampaignFailureClusters.Cluster cluster(int id, String domain,
            String failure, List<String> requests) {
        return new NativeCampaignFailureClusters.Cluster("a".repeat(63) + id, domain, failure,
                "fixture", "windows_job", "0.1.0", "exited", 1L, requests);
    }

    record Snapshot(List<Item> items) { }
    record Item(String id, String clusterId, String campaignId, String domain, String failureClass,
            String action, String requestSha256, int sourceRequestCount, long attemptLimit,
            boolean requiresReview) {
        String json() { return "{\"id\":" + q(id) + ",\"cluster_id\":" + q(clusterId)
                + ",\"campaign_id\":" + q(campaignId) + ",\"domain\":" + q(domain)
                + ",\"failure_class\":" + q(failureClass) + ",\"action\":" + q(action)
                + ",\"request_sha256\":" + q(requestSha256) + ",\"source_request_count\":"
                + sourceRequestCount + ",\"attempt_limit\":" + attemptLimit
                + ",\"requires_review\":" + requiresReview + "}"; }
    }
    private static String q(String value) { return EvidenceExplorerFormat.quote(value); }
    private static String sha256(String value) {
        return java.util.HexFormat.of().formatHex(HexDigest.sha256()
                .digest(value.getBytes(StandardCharsets.UTF_8)));
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalArgumentException(message);
    }
}
