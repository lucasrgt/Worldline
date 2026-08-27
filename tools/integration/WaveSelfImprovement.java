import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Closes a wave with comparable metrics, causal controls, and an adaptive release decision. */
final class WaveSelfImprovement {
    private WaveSelfImprovement() { }

    static void close(Path censusValue, Path previousValue, Path outputValue, Path evidenceRoot,
            String baseValue, String correctionValue) throws Exception {
        Path root = Path.of("").toAbsolutePath().normalize();
        String head = git(root, "rev-parse", "HEAD").trim();
        String tree = git(root, "show", "-s", "--format=%T", "HEAD").trim();
        String base = git(root, "rev-parse", "--verify", baseValue + "^{commit}").trim();
        require(head.equals(base), "wave closure must bind the exact current HEAD");
        require(git(root, "status", "--porcelain=v1", "--untracked-files=all").isBlank(),
                "wave closure requires a clean committed tree");
        WaveCensus.Snapshot current = WaveCensus.read(censusValue.toAbsolutePath().normalize());
        WaveCensus.Snapshot previous = previousValue == null ? null
                : WaveCensus.read(previousValue.toAbsolutePath().normalize());
        boolean correction = correction(root, head, correctionValue, previous);
        List<RejectionRegistry.Entry> rejections = RejectionRegistry.load(root,
                evidenceRoot.toAbsolutePath().normalize());
        Map<String, ScarControlRegistry.Control> controls = ScarControlRegistry.load(root, rejections);
        RejectionRegistry.selfTest();
        Metrics now = Metrics.of(current.rows(), rejections);
        Metrics prior = previous == null ? Metrics.empty() : Metrics.of(previous.rows(), rejections);
        WaveUtility utility = WaveUtility.of(root, current.rows());
        WaveUtility priorUtility = WaveUtility.previous(root, previous);
        boolean improved = previous == null || now.firstPassProcessedRate() > prior.firstPassProcessedRate()
                || now.recurrenceRate() < prior.recurrenceRate()
                || utility.improves(priorUtility);
        boolean release = now.hardBlockers == 0 && now.unownedRetryable == 0
                && now.falsePromotions == 0 && now.inexactQualified == 0
                && now.rejected == now.registeredRejected && utility.substantial()
                && (improved || correction);
        AdaptiveParallelism.Decision parallelism = AdaptiveParallelism.decide(root, correction,
                now.newSystemic(prior), now.cleanDelta(prior));
        boolean nextWave = release && now.total == 25 && now.terminal == 25;
        Path output = outputValue.toAbsolutePath().normalize();
        require(!Files.exists(output), "immutable wave closure already exists: " + output);
        Files.createDirectories(output.getParent());
        String json = json(root, current, previous, now, prior, utility, priorUtility,
                controls, head, tree, base, correctionValue, correction, improved,
                release, nextWave, parallelism);
        Files.writeString(output, json, StandardCharsets.UTF_8);
        System.out.println("wave self-improvement: processed=" + now.processed + ", qualified="
                + now.qualified + ", rejected=" + now.rejected + ", recurrence="
                + WaveReportFormat.rate(now.recurrenceRate()) + ", next-candidate=" + release
                + utility.summary() + ", next-wave=" + nextWave
                + ", parallelism=" + parallelism.width());
        System.out.println("  report: " + output);
    }

    static void selfTest() {
        List<WaveCensus.Row> rows = List.of(
                new WaveCensus.Row("m1-one", ",\"state\":\"QUALIFIED\",\"first_pass\":true,"
                        + "\"integrated\":true,\"receipt_exact\":true,"
                        + "\"time_to_receipt_seconds\":10"),
                new WaveCensus.Row("m2-two", ",\"state\":\"QUALIFIED\",\"first_pass\":false,"
                        + "\"integrated\":true,\"receipt_exact\":true,"
                        + "\"recurrence\":true,\"time_to_receipt_seconds\":30"));
        Metrics metrics = Metrics.of(rows, List.of());
        require(metrics.processed == 2 && metrics.firstPass == 1
                && metrics.medianReceipt == 20 && metrics.p95Receipt == 30,
                "wave metric calculation drifted");
        RejectionRegistry.selfTest();
        AdaptiveParallelism.selfTest();
        WaveUtility.selfTest();
    }

    private static String json(Path root, WaveCensus.Snapshot current, WaveCensus.Snapshot previous,
            Metrics now, Metrics prior, WaveUtility utility, WaveUtility priorUtility,
            Map<String, ScarControlRegistry.Control> controls,
            String head, String tree, String base, String correctionSha, boolean correction,
            boolean improved, boolean release, boolean nextWave,
            AdaptiveParallelism.Decision parallelism) throws Exception {
        StringBuilder text = new StringBuilder("{\n  \"schema\":2,\n  \"created\":\"")
                .append(Instant.now()).append("\",\n  \"base\":\"").append(base)
                .append("\",\n  \"head\":\"").append(head).append("\",\n  \"tree\":\"")
                .append(tree).append("\",\n  \"census_sha256\":\"")
                .append(SwarmEvidenceArchive.sha256(current.path())).append("\",")
                .append("\n  \"previous_census_sha256\":\"")
                .append(previous == null ? "" : SwarmEvidenceArchive.sha256(previous.path()))
                .append("\",\n  \"dispositions\":").append(now.dispositionsJson()).append(',')
                .append("\n  \"first_pass\":{\"count\":").append(now.firstPass)
                .append(",\"processed_rate\":").append(WaveReportFormat.rate(now.firstPassProcessedRate()))
                .append(",\"qualified_rate\":").append(WaveReportFormat.rate(now.firstPassQualifiedRate()))
                .append(",\"unknown\":").append(now.firstPassUnknown).append("},")
                .append("\n  \"known_scar\":{\"recurrences\":").append(now.recurrences)
                .append(",\"assessed\":").append(now.recurrenceAssessed)
                .append(",\"rate\":").append(WaveReportFormat.rate(now.recurrenceRate())).append("},")
                .append("\n  \"prevention\":{\"pre_candidate_milestones\":")
                .append(now.preCandidateMilestones).append(",\"pre_candidate_events\":")
                .append(now.preCandidateEvents).append(",\"pre_runtime_milestones\":")
                .append(now.preRuntimeMilestones).append("},")
                .append("\n  \"rejections\":").append(now.rejectionsJson()).append(',')
                .append("\n  \"recovery\":{\"revalidation_attempted\":")
                .append(now.revalidationAttempted).append(",\"revalidated\":")
                .append(now.revalidated).append(",\"rate\":").append(WaveReportFormat.rate(now.recoveryRate()))
                .append(",\"correctly_anticipated\":").append(now.correctlyAnticipated)
                .append(",\"historical_equivalent_relaunches\":").append(now.historicalEquivalentRelaunches)
                .append(",\"equivalent_blocked_by_new_check\":").append(now.equivalentBlockedByCheck).append("},")
                .append("\n  \"cohort\":{\"total\":").append(now.total)
                .append(",\"terminal\":").append(now.terminal)
                .append(",\"qualified_integrated\":").append(now.integrated)
                .append(",\"rejected_registered\":").append(now.registeredRejected).append("},")
                .append("\n  \"receipt_time_seconds\":{\"count\":").append(now.receiptCount)
                .append(",\"median\":").append(WaveReportFormat.decimal(now.medianReceipt))
                .append(",\"p95\":").append(WaveReportFormat.decimal(now.p95Receipt)).append("},")
                .append("\n  \"safety\":{\"dirty\":").append(now.dirty)
                .append(",\"stranded\":").append(now.stranded)
                .append(",\"retryable\":").append(now.retryable).append(",\"owned_retryable\":")
                .append(now.ownedRetryable).append(",\"unowned_retryable\":").append(now.unownedRetryable)
                .append(",\"inexact_qualified_receipts\":").append(now.inexactQualified)
                .append(",\"false_promotions\":").append(now.falsePromotions).append("},")
                .append("\n  \"delta\":{\"qualified\":").append(now.qualified - prior.qualified)
                .append(",\"first_pass_processed_rate\":")
                .append(WaveReportFormat.rate(now.firstPassProcessedRate() - prior.firstPassProcessedRate()))
                .append(",\"recurrence_rate\":")
                .append(WaveReportFormat.rate(now.recurrenceRate() - prior.recurrenceRate()))
                .append(",\"median_receipt_seconds\":")
                .append(WaveReportFormat.decimal(now.medianReceipt - prior.medianReceipt)).append("},")
                .append("\n  \"moving_window\":{\"waves\":")
                .append(previous == null ? 1 : 2).append(",\"first_pass_processed_rate\":")
                .append(WaveReportFormat.rate((now.firstPassProcessedRate() + prior.firstPassProcessedRate())
                        / (previous == null ? 1 : 2))).append(",\"recurrence_rate\":")
                .append(WaveReportFormat.rate((now.recurrenceRate() + prior.recurrenceRate())
                        / (previous == null ? 1 : 2))).append("},")
                .append(utility.report(priorUtility))
                .append("\n  \"pareto\":").append(now.paretoJson()).append(',')
                .append("\n  \"milestones\":").append(now.milestonesJson()).append(',')
                .append("\n  \"scar_controls\":").append(controlsJson(controls)).append(',')
                .append("\n  \"historical_archive_proof\":{\"verified\":true,\"entries\":")
                .append(now.registryEntries)
                .append(",\"m660_m674_equivalence_rejected\":")
                .append(now.equivalentBlockedByCheck > 0).append("},")
                .append("\n  \"process_correction\":{\"required\":").append(!improved)
                .append(",\"implemented\":").append(correction).append(",\"sha\":\"")
                .append(correctionSha).append("\"},")
                .append("\n  \"release\":{\"next_candidate_allowed\":").append(release)
                .append(",\"next_wave_allowed\":").append(nextWave)
                .append(",\"recommended_candidate_parallelism\":").append(parallelism.width())
                .append(",\"safe_candidate_capacity\":").append(parallelism.capacity())
                .append(",\"logical_processors\":").append(parallelism.processors())
                .append(",\"free_memory_bytes\":").append(parallelism.freeBytes())
                .append(",\"candidate_memory_bytes\":").append(parallelism.workerBytes())
                .append(",\"official_runtime_parallelism\":")
                .append(parallelism.runtimeParallelism())
                .append(",\"official_runtime_serialized_by_lease\":true},")
                .append("\n  \"status\":\"").append(release ? "PASS" : "BLOCKED").append("\"\n}\n");
        return text.toString();
    }

    private static String controlsJson(Map<String, ScarControlRegistry.Control> controls) {
        StringBuilder text = new StringBuilder("[");
        controls.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> {
            ScarControlRegistry.Control control = entry.getValue();
            if (text.length() > 1) text.append(',');
            text.append("{\"scar\":\"").append(entry.getKey()).append("\",\"check_id\":\"")
                    .append(control.id()).append("\",\"version\":").append(control.version())
                    .append(",\"type\":\"").append(control.type()).append("\",\"evidence\":\"")
                    .append(WaveReportFormat.escape(control.evidence())).append("\"}");
        });
        return text.append(']').toString();
    }

    private static boolean correction(Path root, String head, String value,
            WaveCensus.Snapshot previous) throws Exception {
        if (value == null || value.isBlank()) return false;
        String sha = git(root, "rev-parse", "--verify", value + "^{commit}").trim();
        require(SwarmProcess.status(root, List.of("merge-base", "--is-ancestor", sha, head), 60) == 0,
                "process correction is not contained in HEAD");
        String paths = git(root, "show", "--format=", "--name-only", sha);
        require(paths.contains("WaveSelfImprovement.java")
                && paths.contains("RejectedContractCheck.java"),
                "process correction lacks executable wave controls");
        if (previous == null) return true;
        String prior = WaveCensus.string(previous.text(), "authorized_base",
                WaveCensus.string(previous.text(), "head", ""));
        return prior.isBlank() || SwarmProcess.status(root,
                List.of("merge-base", "--is-ancestor", sha, prior), 60) != 0;
    }
    private static String git(Path root, String... arguments) throws Exception {
        return SwarmProcess.output(root, List.of(arguments), 120);
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }

    private static final class Metrics {
        final List<WaveCensus.Row> rows;
        final Map<String, Integer> dispositions = new LinkedHashMap<>(), rejectionClasses = new HashMap<>();
        final Map<String, Integer> scars = new HashMap<>();
        int total, processed, terminal, qualified, rejected, integrated, firstPass, firstPassUnknown;
        int recurrences, recurrenceAssessed, preCandidateMilestones, preCandidateEvents;
        int preRuntimeMilestones, dirty, stranded, hardBlockers, retryable, ownedRetryable;
        int unownedRetryable, inexactQualified, falsePromotions;
        int registeredRejected, revalidationAttempted, revalidated, correctlyAnticipated, receiptCount;
        int registryEntries, historicalEquivalentRelaunches, equivalentBlockedByCheck;
        double medianReceipt = -1, p95Receipt = -1;

        private Metrics(List<WaveCensus.Row> rows) { this.rows = rows; }
        static Metrics empty() { return new Metrics(List.of()); }
        static Metrics of(List<WaveCensus.Row> rows, List<RejectionRegistry.Entry> registry) {
            Metrics value = new Metrics(rows); value.total = rows.size();
            value.registryEntries = registry.size();
            value.historicalEquivalentRelaunches = (int) registry.stream()
                    .filter(entry -> !entry.duplicateOf().isBlank()).count();
            value.equivalentBlockedByCheck = value.historicalEquivalentRelaunches;
            Map<String, RejectionRegistry.Entry> rejectedById = new HashMap<>();
            registry.forEach(entry -> rejectedById.put(entry.id(), entry));
            List<Double> times = new ArrayList<>();
            for (WaveCensus.Row row : rows) {
                value.dispositions.merge(row.state(), 1, Integer::sum);
                if (row.processed()) value.processed++;
                if (row.retryable()) {
                    value.retryable++;
                    if (row.ownedRetryable()) value.ownedRetryable++;
                    else value.unownedRetryable++;
                }
                if (row.qualified()) {
                    value.qualified++; if (row.integrated()) value.integrated++;
                    if (!row.receiptExact()) value.inexactQualified++;
                    if (row.integrated() && row.receiptExact()) value.terminal++;
                }
                if (row.firstPass()) value.firstPass++;
                else if (row.qualified() && !WaveCensus.has(row.body(), "first_pass")) value.firstPassUnknown++;
                if (row.recurrenceAssessed()) value.recurrenceAssessed++;
                if (row.recurrence()) value.recurrences++;
                if (row.candidateAttempts() > 1 || row.objectiveInterlock()) value.preCandidateMilestones++;
                value.preCandidateEvents += row.preCandidatePreventions();
                if (row.candidateAttempts() > 1 && row.officialAttempts() <= 1) value.preRuntimeMilestones++;
                if (row.receiptSeconds() >= 0) times.add(row.receiptSeconds());
                if (row.integrated() && (!row.qualified() || !row.receiptExact())) value.falsePromotions++;
                row.recurrenceScars().forEach(scar -> value.scars.merge(scar, 1, Integer::sum));
                if (row.rejected()) {
                    value.rejected++; RejectionRegistry.Entry entry = rejectedById.get(row.id());
                    if (entry != null) {
                        value.registeredRejected++; value.rejectionClasses.merge(entry.classification(), 1,
                                Integer::sum); value.scars.merge(entry.scar(), 1, Integer::sum);
                        value.terminal++;
                        if (entry.revalidationApproved()) value.revalidationAttempted++;
                    }
                }
            }
            value.dirty = value.dispositions.getOrDefault("DIRTY_SUSPENDED", 0);
            value.stranded = value.dispositions.getOrDefault("STRANDED", 0);
            value.hardBlockers = value.dirty + value.stranded
                    + value.dispositions.getOrDefault("FAILED_GATE", 0);
            Collections.sort(times); value.receiptCount = times.size();
            if (!times.isEmpty()) {
                int middle = times.size() / 2;
                value.medianReceipt = times.size() % 2 == 0 ? (times.get(middle - 1) + times.get(middle)) / 2
                        : times.get(middle);
                value.p95Receipt = times.get(Math.max(0, (int) Math.ceil(times.size() * .95) - 1));
            }
            return value;
        }
        double firstPassProcessedRate() { return processed == 0 ? 0 : (double) firstPass / processed; }
        double firstPassQualifiedRate() { return qualified == 0 ? 0 : (double) firstPass / qualified; }
        double recurrenceRate() { return recurrenceAssessed == 0 ? 0 : (double) recurrences / recurrenceAssessed; }
        double recoveryRate() {
            return revalidationAttempted == 0 ? 0 : (double) revalidated / revalidationAttempted;
        }
        int cleanDelta(Metrics prior) { return processed - prior.processed - (recurrences - prior.recurrences); }
        boolean newSystemic(Metrics prior) {
            return hardBlockers > 0 || unownedRetryable > 0 || retryable > prior.retryable
                    || rejectionClasses.getOrDefault("harness-process-defect", 0)
                    > prior.rejectionClasses.getOrDefault("harness-process-defect", 0);
        }
        String dispositionsJson() { return mapJson(dispositions); }
        String rejectionsJson() { return mapJson(rejectionClasses); }
        String paretoJson() {
            StringBuilder text = new StringBuilder("[");
            scars.entrySet().stream().sorted(Comparator.<Map.Entry<String, Integer>>comparingInt(
                    Map.Entry::getValue).reversed().thenComparing(Map.Entry::getKey)).forEach(entry -> {
                if (text.length() > 1) text.append(',');
                text.append("{\"scar\":\"").append(entry.getKey()).append("\",\"count\":")
                        .append(entry.getValue()).append('}');
            }); return text.append(']').toString();
        }
        String milestonesJson() {
            StringBuilder text = new StringBuilder("[");
            for (WaveCensus.Row row : rows) {
                if (!row.processed()) continue; if (text.length() > 1) text.append(',');
                text.append("{\"id\":\"").append(row.id()).append("\",\"state\":\"")
                        .append(row.state()).append("\",\"corrections\":").append(row.corrections())
                        .append(",\"candidate_attempts\":").append(row.candidateAttempts())
                        .append(",\"official_attempts\":").append(row.officialAttempts()).append('}');
            } return text.append(']').toString();
        }
        private static String mapJson(Map<String, Integer> values) {
            StringBuilder text = new StringBuilder("{");
            values.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> {
                if (text.length() > 1) text.append(',');
                text.append('\"').append(entry.getKey()).append("\":").append(entry.getValue());
            }); return text.append('}').toString();
        }
    }
}
