import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Conservative clusters over validated native campaign outcomes. */
final class NativeCampaignFailureClusters {
    private NativeCampaignFailureClusters() { }

    public static void main(String[] arguments) {
        try {
            require(arguments.length == 0, "usage: NativeCampaignFailureClusters");
            Path root = Path.of("").toAbsolutePath().normalize();
            System.out.write(json(summarize(EvidenceExplorerExecutionLinks.build(root))).getBytes(
                    java.nio.charset.StandardCharsets.UTF_8));
        } catch (Exception error) {
            System.err.println("native campaign failure clustering failed: " + error.getMessage());
            System.exit(1);
        }
    }

    static Snapshot summarize(EvidenceExplorerExecutionLinks.Snapshot links) {
        Map<String, Mutable> grouped = new LinkedHashMap<>();
        for (EvidenceExplorerExecutionLinks.Link link : links.links()) {
            String failure = classify(link); if (failure == null || link.campaignId() == null) continue;
            String domain = failure.equals("workload-nonzero-exit")
                    ? "unclassified-workload" : "executor-infrastructure";
            String key = String.join("\u0000", domain, failure, link.campaignId(), link.backend(),
                    link.executorVersion(), link.termination(), String.valueOf(link.exitCode()));
            grouped.computeIfAbsent(key, ignored -> new Mutable(domain, failure, link)).requests
                    .add(link.requestSha256());
        }
        List<Cluster> clusters = grouped.values().stream().map(Mutable::cluster)
                .sorted(Comparator.comparing(Cluster::id)).toList();
        long failures = clusters.stream().mapToLong(value -> value.requests().size()).sum();
        long unclassified = clusters.stream().filter(value -> value.domain().equals(
                "unclassified-workload")).mapToLong(value -> value.requests().size()).sum();
        return new Snapshot(failures, failures - unclassified, unclassified, clusters);
    }

    static String json(Snapshot value) {
        StringBuilder out = new StringBuilder("{\"schema\":\"worldline.native-campaign-failure-clusters.v1\"")
                .append(",\"verdict_authority\":false,\"admission_effect\":false")
                .append(",\"automatic_execution\":false")
                .append(",\"failure_count\":").append(value.failureCount())
                .append(",\"infrastructure_count\":").append(value.infrastructureCount())
                .append(",\"unclassified_workload_count\":").append(value.unclassifiedWorkloadCount())
                .append(",\"cluster_count\":").append(value.clusters().size()).append(",\"clusters\":[");
        for (int index = 0; index < value.clusters().size(); index++) {
            if (index > 0) out.append(','); out.append(value.clusters().get(index).json());
        }
        return out.append("]}\n").toString();
    }

    private static String classify(EvidenceExplorerExecutionLinks.Link value) {
        if (value.descendantsRemaining() != null && value.descendantsRemaining() > 0L)
            return "escaped-descendant";
        if (value.stdoutTruncated() || value.stderrTruncated()) return "output-truncated";
        return switch (value.termination()) {
            case "exited" -> Long.valueOf(0L).equals(value.exitCode())
                    ? null : "workload-nonzero-exit";
            case "crashed" -> "process-crash";
            case "timed_out" -> "timeout";
            case "spawn_failed" -> "spawn-failure";
            case "wait_failed" -> "wait-failure";
            case "owner_exited" -> "owner-exit";
            default -> throw new IllegalArgumentException("unknown native termination");
        };
    }

    static void selfTest() {
        List<EvidenceExplorerExecutionLinks.Link> links = List.of(
                link(1, "fixture", "exited", 0L, false, 0L),
                link(2, "fixture", "exited", 1L, false, 0L),
                link(3, "fixture", "exited", 1L, false, 0L),
                link(4, "fixture", "timed_out", null, false, 0L),
                link(5, "fixture", "exited", 0L, true, 0L),
                link(6, "fixture", "exited", 0L, false, 1L),
                link(7, null, "exited", 1L, false, 0L));
        Snapshot value = summarize(new EvidenceExplorerExecutionLinks.Snapshot(
                7, 6, 5, 1, 0, 6, 0, 0, 0, 0, links));
        String json = json(value);
        require(value.failureCount() == 5L && value.infrastructureCount() == 3L
                && value.unclassifiedWorkloadCount() == 2L && value.clusters().size() == 4,
                "native campaign failure grouping drifted");
        require(json.contains("\"verdict_authority\":false")
                && json.contains("\"admission_effect\":false")
                && json.contains("\"automatic_execution\":false")
                && json.contains("\"failure_class\":\"workload-nonzero-exit\"")
                && json.contains("\"id\":")
                && !json.contains("oracle-mismatch") && !json.contains("working_directory"),
                "native campaign failure authority or classification drifted");
        System.out.println("  native campaign failure clustering self-test: passed");
    }

    private static EvidenceExplorerExecutionLinks.Link link(int id, String campaign,
            String termination, Long exit, boolean truncated, Long descendants) {
        return new EvidenceExplorerExecutionLinks.Link("%064x".formatted(id), campaign,
                campaign == null ? null : "%064x".formatted(100 + id), campaign == null ? null : 0,
                "windows_job", "0.1.0", termination, exit, 1L, 2L, 3L,
                truncated, false, descendants);
    }

    record Snapshot(long failureCount, long infrastructureCount, long unclassifiedWorkloadCount,
            List<Cluster> clusters) { }
    record Cluster(String id, String domain, String failureClass, String campaignId, String backend,
            String executorVersion, String termination, Long exitCode, List<String> requests) {
        String json() { return "{\"id\":" + q(id) + ",\"domain\":" + q(domain)
                + ",\"failure_class\":" + q(failureClass)
                + ",\"campaign_id\":" + q(campaignId) + ",\"backend\":" + q(backend)
                + ",\"executor_version\":" + q(executorVersion) + ",\"termination\":"
                + q(termination) + ",\"exit_code\":" + (exitCode == null ? "null" : exitCode)
                + ",\"count\":" + requests.size() + ",\"request_sha256\":"
                + strings(requests) + "}"; }
    }
    private static final class Mutable {
        final String domain, failure; final EvidenceExplorerExecutionLinks.Link sample;
        final List<String> requests = new ArrayList<>();
        Mutable(String domain, String failure, EvidenceExplorerExecutionLinks.Link sample) {
            this.domain = domain; this.failure = failure; this.sample = sample;
        }
        Cluster cluster() {
            requests.sort(String::compareTo); List<String> stable = List.copyOf(requests);
            String material = "schema=worldline.native-campaign-failure-cluster.v1\n"
                    + "domain=" + domain + "\nclass=" + failure + "\ncampaign="
                    + sample.campaignId() + "\nbackend=" + sample.backend() + "\nexecutor="
                    + sample.executorVersion() + "\ntermination=" + sample.termination()
                    + "\nexit=" + sample.exitCode() + "\nrequests=" + String.join(",", stable) + "\n";
            return new Cluster(sha256(material), domain, failure, sample.campaignId(), sample.backend(),
                    sample.executorVersion(), sample.termination(), sample.exitCode(), stable);
        }
    }
    private static String q(String value) { return EvidenceExplorerFormat.quote(value); }
    private static String strings(List<String> values) {
        return "[\"" + String.join("\",\"", values) + "\"]";
    }
    private static String sha256(String value) {
        return java.util.HexFormat.of().formatHex(HexDigest.sha256()
                .digest(value.getBytes(StandardCharsets.UTF_8)));
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalArgumentException(message);
    }
}
