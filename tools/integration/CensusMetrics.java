import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/** Carries wave telemetry only across exact milestone-head continuity. */
final class CensusMetrics {
    private final Map<String, WaveCensus.Row> baseline;
    private final Map<String, Properties> dispositions;
    private final String baselineSha;

    private CensusMetrics(Map<String, WaveCensus.Row> baseline,
            Map<String, Properties> dispositions, String baselineSha) {
        this.baseline = baseline;
        this.dispositions = dispositions;
        this.baselineSha = baselineSha;
    }

    static CensusMetrics load(Path root, Path baselineValue) throws Exception {
        Map<String, WaveCensus.Row> baseline = new HashMap<>();
        String sha = "";
        if (baselineValue != null) {
            WaveCensus.Snapshot snapshot = WaveCensus.read(baselineValue.toAbsolutePath().normalize());
            snapshot.rows().forEach(row -> baseline.put(row.id(), row));
            sha = SwarmEvidenceArchive.sha256(snapshot.path());
        }
        Map<String, Properties> dispositions = new HashMap<>();
        Path directory = root.resolve("coordination/swarm/dispositions");
        if (Files.isDirectory(directory)) {
            try (var paths = Files.list(directory)) {
                for (Path path : paths.filter(Files::isRegularFile).toList()) {
                    Properties values = new Properties();
                    try (var reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                        values.load(reader);
                    }
                    String id = values.getProperty("id", "").trim();
                    if (!id.isBlank()) {
                        dispositions.put(id, values);
                    }
                }
            }
        }
        return new CensusMetrics(Map.copyOf(baseline), Map.copyOf(dispositions), sha);
    }

    Entry entry(String id, String head) {
        WaveCensus.Row prior = baseline.get(id);
        if (prior != null && !head.equals(WaveCensus.string(prior.body(), "head", ""))) {
            prior = null;
        }
        Properties disposition = dispositions.get(id);
        if (disposition != null && !head.equals(first(disposition, "head", "failure.head"))) {
            disposition = null;
        }
        Boolean firstPass = bool(disposition, prior, "first_pass", "first.pass");
        Boolean recurrence = bool(disposition, prior, "recurrence",
                "recurrence", "known.scar.recurrence");
        Integer candidates = integer(disposition, "candidate.attempts", "attempt", prior,
                "candidate_attempts", null);
        Integer official = integer(disposition, "official.attempts", prior,
                "official_attempts", null);
        Integer prevented = integer(disposition, "prevented.same.scar.failures", prior,
                "prevented_same_scar_failures", null);
        String interlock = disposition == null ? ""
                : disposition.getProperty("prevention.stage", "").trim();
        Double seconds = decimal(disposition, "receipt.seconds", prior,
                "time_to_receipt_seconds");
        List<String> scars = prior == null ? List.of()
                : WaveCensus.strings(prior.body(), "recurrence_scars");
        String dispositionScar = disposition == null ? "" : disposition.getProperty("scar", "");
        if (recurrence != null && recurrence && !dispositionScar.isBlank()) {
            List<String> merged = new ArrayList<>(scars);
            if (!merged.contains(dispositionScar)) {
                merged.add(dispositionScar);
            }
            scars = List.copyOf(merged);
        }
        scars = scars.stream().filter(value -> !value.isBlank()).toList();
        return new Entry(firstPass, recurrence, candidates, official, prevented, seconds, scars,
                interlock);
    }

    String baselineSha() {
        return baselineSha;
    }

    static void selfTest() {
        Properties values = new Properties();
        values.setProperty("head", "abc");
        values.setProperty("first.pass", "false");
        values.setProperty("candidate.attempts", "3");
        WaveCensus.Row prior = new WaveCensus.Row("m1-fixture",
                ",\"head\":\"abc\",\"first_pass\":true,\"candidate_attempts\":2");
        CensusMetrics metrics = new CensusMetrics(Map.of(prior.id(), prior),
                Map.of(prior.id(), values), "sha");
        Entry exact = metrics.entry(prior.id(), "abc");
        Entry drift = metrics.entry(prior.id(), "def");
        require(Boolean.FALSE.equals(exact.firstPass) && exact.candidateAttempts == 3,
                "disposition telemetry did not override an exact baseline");
        require(drift.firstPass == null && drift.candidateAttempts == null,
                "telemetry crossed a changed milestone head");
    }

    private static String first(Properties values, String... keys) {
        for (String key : keys) {
            String value = values.getProperty(key, "").trim();
            if (!value.isBlank()) {
                return value;
            }
        }
        return "";
    }
    private static Boolean bool(Properties values, WaveCensus.Row prior, String jsonKey,
            String... keys) {
        if (values != null) {
            for (String key : keys) {
                if (values.containsKey(key)) {
                    return Boolean.parseBoolean(values.getProperty(key));
                }
            }
        }
        return prior != null && WaveCensus.has(prior.body(), jsonKey)
                ? WaveCensus.bool(prior.body(), jsonKey, false) : null;
    }
    private static Integer integer(Properties values, String key, WaveCensus.Row prior,
            String jsonKey, Integer fallback) {
        if (values != null && values.containsKey(key)) {
            return Integer.parseInt(values.getProperty(key));
        }
        if (prior != null && WaveCensus.has(prior.body(), jsonKey)) {
            return WaveCensus.integer(prior.body(), jsonKey, 0);
        }
        return fallback;
    }
    private static Integer integer(Properties values, String key, String secondary,
            WaveCensus.Row prior, String jsonKey, Integer fallback) {
        if (values != null && values.containsKey(key)) {
            return Integer.parseInt(values.getProperty(key));
        }
        if (values != null && values.containsKey(secondary)) {
            return Integer.parseInt(values.getProperty(secondary));
        }
        if (prior != null && WaveCensus.has(prior.body(), jsonKey)) {
            return WaveCensus.integer(prior.body(), jsonKey, 0);
        }
        return fallback;
    }
    private static Double decimal(Properties values, String key, WaveCensus.Row prior,
            String jsonKey) {
        if (values != null && values.containsKey(key)) {
            return Double.parseDouble(values.getProperty(key));
        }
        return prior != null && WaveCensus.has(prior.body(), jsonKey)
                ? WaveCensus.decimal(prior.body(), jsonKey, -1) : null;
    }
    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }

    record Entry(Boolean firstPass, Boolean recurrence, Integer candidateAttempts,
            Integer officialAttempts, Integer prevented, Double receiptSeconds, List<String> scars,
            String interlock) { }
}
