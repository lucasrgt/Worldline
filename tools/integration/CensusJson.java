import java.time.Instant;
import java.util.*;

/** Writes the canonical census envelope and its head-bound telemetry. */
final class CensusJson {
    private CensusJson() {
    }

    static String json(List<SwarmCensus.Item> items, Map<String, Integer> counts,
            List<SwarmCensus.Wave> waves, String baselineSha) {
        StringBuilder text = new StringBuilder("{\n  \"schema\":2,\n  \"created\":\"")
                .append(Instant.now()).append("\",\n  \"waves\":").append(waves.size())
                .append(",\n  \"baseline_census_sha256\":\"").append(baselineSha)
                .append("\",\n  \"summary\":{");
        for (String state : List.of("QUALIFIED", "REJECTED", "RETRYABLE", "STRANDED",
                "FAILED_GATE", "DIRTY_SUSPENDED", "NOT_STARTED")) {
            text.append('"').append(state).append("\":")
                    .append(counts.getOrDefault(state, 0)).append(',');
        }
        text.setLength(text.length() - 1);
        text.append("},\n  \"candidates\":[\n");
        for (int index = 0; index < items.size(); index++) {
            append(text, items.get(index));
            text.append(index + 1 == items.size() ? "\n" : ",\n");
        }
        return text.append("  ]\n}\n").toString();
    }

    private static void append(StringBuilder text, SwarmCensus.Item item) {
        text.append("    {\"id\":\"").append(item.id()).append("\",\"state\":\"")
                .append(item.state()).append("\",\"path\":\"").append(escape(item.path().toString()))
                .append("\",\"branch\":\"").append(item.branch()).append("\",\"base\":\"")
                .append(item.base()).append("\",\"head\":\"").append(item.head())
                .append("\",\"tree\":\"").append(item.tree()).append("\",\"dirty\":")
                .append(item.dirty()).append(",\"commits\":").append(item.commits())
                .append(",\"scaffold\":").append(item.scaffold()).append(",\"receipt_present\":")
                .append(item.receiptPresent()).append(",\"receipt_exact\":")
                .append(item.receiptExact()).append(",\"handoff_exact\":")
                .append(item.handoffExact()).append(",\"integrated\":").append(item.integrated())
                .append(",\"retries\":").append(item.retries());
        metrics(text, item.metrics());
        disposition(text, item.disposition());
        text.append(",\"cause\":\"").append(escape(item.cause())).append("\",\"archive\":\"")
                .append(escape(item.archive().path())).append("\",\"archive_sha256\":\"")
                .append(item.archive().sha256()).append("\"}");
    }

    private static void disposition(StringBuilder text, CensusDisposition.Decision decision) {
        if (decision == null || !"RETRYABLE".equals(decision.state())) {
            return;
        }
        text.append(",\"owner\":\"").append(escape(decision.owner()))
                .append("\",\"session\":\"").append(escape(decision.session()))
                .append("\",\"attempt\":").append(decision.attempt())
                .append(",\"max_attempts\":").append(decision.maximum());
    }

    static void selfTest() {
        CensusDisposition.Decision retry = new CensusDisposition.Decision("m1-retry", "RETRYABLE",
                "codex/milestone-m1-retry", java.nio.file.Path.of("retry"), "1".repeat(40),
                "2".repeat(40), "3".repeat(40), "self-test", "worldline-orchestrator",
                "ses_exact", 1, 2, SwarmEvidenceArchive.Result.empty());
        StringBuilder text = new StringBuilder();
        disposition(text, retry);
        String value = text.toString();
        require(value.contains("\"owner\":\"worldline-orchestrator\"")
                && value.contains("\"session\":\"ses_exact\"")
                && value.contains("\"attempt\":1,\"max_attempts\":2"),
                "RETRYABLE ownership fields were omitted from census JSON");
    }

    private static void metrics(StringBuilder text, CensusMetrics.Entry metrics) {
        if (metrics.firstPass() != null) {
            text.append(",\"first_pass\":").append(metrics.firstPass());
        }
        if (metrics.recurrence() != null) {
            text.append(",\"recurrence\":").append(metrics.recurrence());
        }
        if (metrics.candidateAttempts() != null) {
            text.append(",\"candidate_attempts\":").append(metrics.candidateAttempts());
        }
        if (metrics.officialAttempts() != null) {
            text.append(",\"official_attempts\":").append(metrics.officialAttempts());
        }
        if (metrics.prevented() != null) {
            text.append(",\"prevented_same_scar_failures\":").append(metrics.prevented());
        }
        if (metrics.recurrence() != null) {
            text.append(",\"recurrence_scars\":[");
            for (int index = 0; index < metrics.scars().size(); index++) {
                text.append('"').append(escape(metrics.scars().get(index))).append('"')
                        .append(index + 1 == metrics.scars().size() ? "" : ",");
            }
            text.append(']');
        }
        if (metrics.receiptSeconds() != null) {
            text.append(",\"time_to_receipt_seconds\":").append(metrics.receiptSeconds());
        }
        if (!metrics.interlock().isBlank()) {
            text.append(",\"prevention_interlock\":\"").append(escape(metrics.interlock()))
                    .append('"');
        }
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\r", "\\r").replace("\n", "\\n");
    }
    private static void require(boolean value, String message) {
        if (!value) {
            throw new IllegalStateException(message);
        }
    }
}
