import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Strict live-Explorer parser for non-authoritative regression triage. */
final class EvidenceExplorerRegressionTriage {
    private EvidenceExplorerRegressionTriage() { }

    static Snapshot parse(Map<String, Object> value) {
        exact(value, "schema", "verdict_authority", "admission_effect", "automatic_execution",
                "metric", "change_threshold_percent", "scope_termination", "case_count",
                "performance_increase_count", "data_quality_count", "cases");
        require("worldline.execution-regression-triage.v1".equals(MiniJson.string(value, "schema"))
                && !MiniJson.bool(value, "verdict_authority")
                && !MiniJson.bool(value, "admission_effect")
                && !MiniJson.bool(value, "automatic_execution"),
                "regression triage gained authority or execution");
        String metric = MiniJson.string(value, "metric");
        long threshold = MiniJson.integer(value, "change_threshold_percent");
        String termination = MiniJson.string(value, "scope_termination");
        require("elapsed_ms".equals(metric) && threshold >= 110L && threshold <= 1_000L
                && "exited".equals(termination), "regression triage policy drifted");
        List<Case> cases = new ArrayList<>();
        for (Object item : MiniJson.array(value, "cases"))
            cases.add(aCase(MiniJson.asObject(item, "regression triage case"), metric,
                    threshold, termination));
        long count = MiniJson.integer(value, "case_count");
        long increases = MiniJson.integer(value, "performance_increase_count");
        long quality = MiniJson.integer(value, "data_quality_count");
        require(count == cases.size() && increases == count(cases, "performance-increase")
                && quality == count(cases, "baseline-data-quality")
                && count == increases + quality, "regression triage counts drifted");
        for (int index = 1; index < cases.size(); index++)
            require(cases.get(index - 1).id().compareTo(cases.get(index).id()) < 0,
                    "regression triage cases are not uniquely sorted");
        return new Snapshot(metric, threshold, termination, count, increases, quality,
                List.copyOf(cases));
    }

    static void requireMatches(EvidenceExplorerLiveSnapshot.Performance performance,
            Snapshot triage) {
        List<EvidenceExplorerLiveSnapshot.Signal> eligible = performance.signals().stream()
                .filter(value -> Set.of("increase", "zero-baseline").contains(
                        value.classification())).toList();
        require(performance.metric().equals(triage.metric())
                && performance.changeThresholdPercent() == triage.changeThresholdPercent()
                && eligible.size() == triage.caseCount(),
                "regression triage does not cover current eligible signals");
        for (Case item : triage.cases()) require(eligible.stream().anyMatch(signal ->
                matches(item, signal)), "regression triage case does not match its signal");
    }

    private static boolean matches(Case item, EvidenceExplorerLiveSnapshot.Signal signal) {
        String expectedKind = signal.classification().equals("increase")
                ? "performance-increase" : "baseline-data-quality";
        return expectedKind.equals(item.kind()) && item.workloadId().equals(signal.workloadId())
                && item.executorVersion().equals(signal.executorVersion())
                && item.backend().equals(signal.backend()) && item.os().equals(signal.os())
                && item.architecture().equals(signal.architecture())
                && item.sampleCount() == signal.sampleCount()
                && java.util.Objects.equals(item.baselineMedianMs(), signal.baselineMedianMs())
                && java.util.Objects.equals(item.recentMedianMs(), signal.recentMedianMs())
                && java.util.Objects.equals(item.ratioPercent(), signal.ratioPercent());
    }

    private static Case aCase(Map<String, Object> value, String metric, long threshold,
            String termination) {
        exact(value, "id", "lane", "workload_id", "executor_version", "backend", "os",
                "architecture", "runtime_capability", "kind", "disposition", "sample_count",
                "baseline_median_ms", "recent_median_ms", "ratio_percent",
                "baseline_sample_sha256", "recent_sample_sha256", "suggested_next_step");
        String id = MiniJson.string(value, "id"), lane = token(value, "lane");
        String workload = token(value, "workload_id"), executor = token(value, "executor_version");
        String backend = token(value, "backend"), os = token(value, "os");
        String architecture = token(value, "architecture"), capability = token(value,
                "runtime_capability"), kind = MiniJson.string(value, "kind");
        String disposition = MiniJson.string(value, "disposition");
        String next = MiniJson.string(value, "suggested_next_step");
        require(id.matches("[0-9a-f]{64}") && "campaign".equals(lane)
                && "campaign-worker".equals(capability)
                && Set.of("performance-increase", "baseline-data-quality").contains(kind)
                && "investigation-candidate".equals(disposition)
                && next.equals(kind.equals("performance-increase")
                        ? "repeat-exact-profile" : "inspect-profile-history"),
                "invalid regression triage identity or disposition");
        long samples = nonnegative(value, "sample_count");
        Long baseline = nullable(value, "baseline_median_ms");
        Long recent = nullable(value, "recent_median_ms");
        Long ratio = nullable(value, "ratio_percent");
        List<String> baselineIds = digests(value, "baseline_sample_sha256");
        List<String> recentIds = digests(value, "recent_sample_sha256");
        require(!baselineIds.isEmpty() && !recentIds.isEmpty()
                && baselineIds.size() <= 31 && recentIds.size() <= 31
                && baselineIds.size() + recentIds.size() <= samples
                && baseline != null && recent != null
                && (kind.equals("performance-increase") ? ratio != null : ratio == null),
                "invalid regression triage sample window");
        String material = "schema=worldline.execution-regression-triage-case.v1\nmetric="
                + metric + "\nthreshold=" + threshold + "\nlane=" + lane + "\nworkload="
                + workload + "\nexecutor=" + executor + "\nbackend=" + backend + "\nos=" + os
                + "\narchitecture=" + architecture + "\nruntime_capability=" + capability
                + "\ntermination=" + termination + "\nkind=" + kind + "\nbaseline="
                + String.join(",", baselineIds) + "\nrecent=" + String.join(",", recentIds) + "\n";
        require(id.equals(sha256(material)), "regression triage case ID drifted");
        return new Case(id, workload, executor, backend, os, architecture, kind, samples,
                baseline, recent, ratio, next);
    }

    private static List<String> digests(Map<String, Object> value, String key) {
        List<String> result = new ArrayList<>();
        for (Object item : MiniJson.array(value, key)) {
            require(item instanceof String, key + " contains a non-string");
            String digest = (String) item;
            require(digest.matches("[0-9a-f]{64}"), "invalid sample digest");
            result.add(digest);
        }
        require(result.stream().distinct().count() == result.size(), "duplicate sample digest");
        return List.copyOf(result);
    }

    private static long count(List<Case> cases, String kind) {
        return cases.stream().filter(value -> kind.equals(value.kind())).count();
    }
    private static String token(Map<String, Object> value, String key) {
        String item = MiniJson.string(value, key);
        require(item.matches("[a-z0-9][a-z0-9._-]{0,62}"), "invalid triage token: " + key);
        return item;
    }
    private static long nonnegative(Map<String, Object> value, String key) {
        long item = MiniJson.integer(value, key); require(item >= 0L, key + " is negative"); return item;
    }
    private static Long nullable(Map<String, Object> value, String key) {
        Object item = value.get(key); require(item == null || item instanceof Long, key + " is invalid");
        require(item == null || (Long) item >= 0L, key + " is negative"); return (Long) item;
    }
    private static void exact(Map<String, Object> value, String... fields) {
        require(new ArrayList<>(value.keySet()).equals(List.of(fields)),
                "regression triage fields drifted");
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalArgumentException(message);
    }
    private static String sha256(String value) {
        return HexFormat.of().formatHex(HexDigest.sha256().digest(
                value.getBytes(StandardCharsets.UTF_8)));
    }

    record Snapshot(String metric, long changeThresholdPercent, String scopeTermination,
            long caseCount, long performanceIncreaseCount, long dataQualityCount, List<Case> cases) { }
    record Case(String id, String workloadId, String executorVersion, String backend,
            String os, String architecture, String kind, long sampleCount,
            Long baselineMedianMs, Long recentMedianMs, Long ratioPercent,
            String suggestedNextStep) { }
}
