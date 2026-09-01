import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/** Sanitized local-state projection kept separate from canonical evidence. */
record EvidenceExplorerLiveSnapshot(long observedAtEpochMs, String canonicalSha256,
        Execution execution, EvidenceExplorerExecutionLinks.Snapshot executionLinks,
        NativeCampaignFailureClusters.Snapshot campaignFailures,
        SharedCasInventory.Snapshot sharedCas, Admission admission,
        List<Queue> queues, List<CacheDecision> cache) {
    static EvidenceExplorerLiveSnapshot build(Path root, Path control, long observedAt) throws Exception {
        Path canonical = EvidenceExplorer.output(root).resolve("evidence.json");
        require(Files.isRegularFile(canonical), "canonical explorer snapshot is missing");
        SmokeReceiptCache receipts = new SmokeReceiptCache(root);
        List<CacheDecision> cache = new ArrayList<>();
        for (SmokeDiscovery.Entry smoke : SmokeDiscovery.discover(root)) {
            SmokeCacheExplanation item = receipts.explain(smoke);
            cache.add(new CacheDecision(item.smoke(), item.fingerprint(), item.cacheEnabled(),
                    item.localProof(), item.trackedPin(), item.decision(), item.reason(),
                    item.currentTreeMode()));
        }
        cache.sort(Comparator.comparing(CacheDecision::smoke));
        List<Queue> queues = new ArrayList<>();
        queues.add(queue(root.resolve(".worldline/verify.lock.queue"), "worktree", observedAt));
        queues.add(queue(control.resolve("verify-slots.queue"), "verify-slot", observedAt));
        queues.add(queue(control.resolve("official-b173.lock.queue"), "official-runtime", observedAt));
        String legacy = System.getenv("WORLDLINE_RUNTIME_LOCK");
        if (legacy != null && !legacy.isBlank()) {
            Path path = Path.of(legacy).toAbsolutePath().normalize();
            queues.add(queue(path.resolveSibling(path.getFileName() + ".queue"),
                    "legacy-runtime", observedAt));
        }
        EvidenceExplorerExecutionLinks.Snapshot links = EvidenceExplorerExecutionLinks.build(root);
        return new EvidenceExplorerLiveSnapshot(observedAt,
                EvidenceExplorerSnapshot.sha256(canonical), execution(root, control), links,
                NativeCampaignFailureClusters.summarize(links),
                SharedCasInventory.configured(root, System.getenv()),
                admission(root.resolve(".worldline/executor-telemetry/admission.properties")),
                List.copyOf(queues), List.copyOf(cache));
    }

    static Queue queue(Path root, String label, long observedAt) throws Exception {
        if (!Files.exists(root, LinkOption.NOFOLLOW_LINKS)) return new Queue(label, 0, 0, 0, 0, null);
        require(Files.isDirectory(root, LinkOption.NOFOLLOW_LINKS) && !Files.isSymbolicLink(root),
                "invalid live queue: " + label);
        List<Path> paths = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(root)) {
            for (Path path : stream) paths.add(path);
        }
        paths.sort(Comparator.comparing(path -> path.getFileName().toString()));
        int waiting = 0, live = 0, abandoned = 0, partial = 0; Long oldest = null;
        for (Path path : paths) {
            String name = path.getFileName().toString();
            if (name.contains(".tmp-")) { partial++; continue; }
            if (name.equals("sequence.lock")) continue;
            if (!name.matches("[0-9]{20}-[0-9]+-[0-9a-f-]{36}[.]ticket")) { partial++; continue; }
            waiting++;
            try {
                Ticket ticket = ticket(path); long age = Math.max(0L, observedAt - ticket.created);
                oldest = oldest == null ? age : Math.max(oldest, age);
                ProcessHandle process = ProcessHandle.of(ticket.pid).orElse(null);
                Instant started = process == null ? null : process.info().startInstant().orElse(null);
                if (process != null && process.isAlive()
                        && (started == null || started.toEpochMilli() == ticket.started)) live++;
                else abandoned++;
            } catch (RuntimeException error) { partial++; }
        }
        return new Queue(label, waiting, live, abandoned, partial, oldest);
    }

    static Admission admission(Path path) throws Exception {
        if (!Files.exists(path, LinkOption.NOFOLLOW_LINKS)) return null;
        require(Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)
                && !Files.isSymbolicLink(path) && Files.size(path) <= 2_048L,
                "invalid telemetry admission file");
        Properties values = properties(path);
        Set<String> keys = Set.of("schema", "os", "architecture", "runtime.capability",
                "sample.count", "observed.peak.memory.bytes", "minimum.worker.memory.bytes",
                "direction");
        require(values.stringPropertyNames().equals(keys), "telemetry admission fields drifted");
        require("worldline.telemetry-admission.v1".equals(values.getProperty("schema"))
                && "candidate-worker".equals(values.getProperty("runtime.capability"))
                && "lower-only".equals(values.getProperty("direction")),
                "telemetry admission authority drifted");
        require(values.getProperty("os").matches("[a-z0-9][a-z0-9._-]{0,62}")
                && values.getProperty("architecture").matches("[a-z0-9][a-z0-9._-]{0,62}"),
                "invalid telemetry admission host identity");
        long samples = number(values, "sample.count"), peak = number(values,
                "observed.peak.memory.bytes"), minimum = number(values, "minimum.worker.memory.bytes");
        require(samples > 0 && minimum >= peak, "invalid telemetry admission values");
        return new Admission(values.getProperty("os"), values.getProperty("architecture"),
                samples, peak, minimum);
    }

    private static Execution execution(Path root, Path control) throws Exception {
        Path output = Files.createTempFile(EvidenceExplorer.output(root), "execution-", ".json");
        List<String> command = List.of(javaTool(), root.resolve(
                "tools/containers/RuntimeFabricLauncher.java").toString(), "ExecutionEvidenceSummary");
        try {
            ProcessBuilder builder = new ProcessBuilder(command).directory(root.toFile())
                    .redirectErrorStream(true).redirectOutput(output.toFile());
            builder.environment().put("WORLDLINE_GATE_CONTROL", control.toString());
            Process process = builder.start();
            if (!process.waitFor(60, TimeUnit.SECONDS)) {
                process.descendants().sorted(Comparator.comparingLong(ProcessHandle::pid).reversed())
                        .forEach(ProcessHandle::destroyForcibly);
                process.destroyForcibly(); process.waitFor(5, TimeUnit.SECONDS);
                throw new IllegalArgumentException("execution evidence summary timed out");
            }
            String json = Files.readString(output, StandardCharsets.UTF_8).trim();
            require(process.exitValue() == 0, "execution evidence summary failed: " + json);
            Map<String, Object> value = MiniJson.object(json);
            require(new ArrayList<>(value.keySet()).equals(List.of("schema", "telemetry",
                    "failure_clusters", "performance_signals", "regression_triage")),
                    "execution overlay fields drifted");
            require("worldline.execution-evidence-overlay.v3".equals(MiniJson.string(value, "schema")),
                    "execution overlay schema drifted");
            Map<String, Object> telemetry = MiniJson.asObject(value.get("telemetry"), "telemetry");
            Map<String, Object> failures = MiniJson.asObject(value.get("failure_clusters"),
                    "failure_clusters");
            Map<String, Object> performance = MiniJson.asObject(value.get("performance_signals"),
                    "performance_signals");
            Map<String, Object> triage = MiniJson.asObject(value.get("regression_triage"),
                    "regression_triage");
            require("worldline.execution-telemetry-profiles.v1".equals(
                    MiniJson.string(telemetry, "schema")), "telemetry profile schema drifted");
            require("worldline.execution-failure-clusters.v1".equals(
                    MiniJson.string(failures, "schema")), "failure cluster schema drifted");
            long samples = MiniJson.integer(telemetry, "sample_count");
            long failureCount = MiniJson.integer(failures, "failure_count");
            require(samples >= 0L && failureCount >= 0L && failureCount <= samples,
                    "execution evidence counts drifted");
            Performance signals = performance(performance);
            EvidenceExplorerRegressionTriage.Snapshot triageSnapshot =
                    EvidenceExplorerRegressionTriage.parse(triage);
            EvidenceExplorerRegressionTriage.requireMatches(signals, triageSnapshot);
            return new Execution(json, samples, failureCount, signals, triageSnapshot);
        } finally { Files.deleteIfExists(output); }
    }

    private static Performance performance(Map<String, Object> value) {
        exact(value, "schema", "verdict_authority", "admission_effect", "metric",
                "baseline_window_samples", "recent_window_samples", "change_threshold_percent",
                "signal_count", "increase_count", "insufficient_history_count", "signals");
        require("worldline.execution-performance-signals.v1".equals(MiniJson.string(value, "schema"))
                && !MiniJson.bool(value, "verdict_authority")
                && !MiniJson.bool(value, "admission_effect")
                && "elapsed_ms".equals(MiniJson.string(value, "metric")),
                "performance signals gained authority or changed metric");
        long baselineWindow = MiniJson.integer(value, "baseline_window_samples");
        long recentWindow = MiniJson.integer(value, "recent_window_samples");
        long threshold = MiniJson.integer(value, "change_threshold_percent");
        require(baselineWindow >= 3L && baselineWindow <= 31L && (baselineWindow & 1L) == 1L
                && recentWindow >= 3L && recentWindow <= 31L && (recentWindow & 1L) == 1L
                && threshold >= 110L && threshold <= 1_000L,
                "invalid performance signal policy projection");
        List<Signal> signals = new ArrayList<>();
        for (Object item : MiniJson.array(value, "signals")) signals.add(signal(
                MiniJson.asObject(item, "performance signal")));
        long expected = MiniJson.integer(value, "signal_count");
        long increases = MiniJson.integer(value, "increase_count");
        long insufficient = MiniJson.integer(value, "insufficient_history_count");
        require(expected == signals.size()
                && increases == signals.stream().filter(item -> item.classification().equals("increase")).count()
                && insufficient == signals.stream().filter(item -> item.classification()
                        .equals("insufficient-history")).count(), "performance signal counts drifted");
        return new Performance("elapsed_ms", threshold, expected, increases, insufficient,
                List.copyOf(signals));
    }

    private static Signal signal(Map<String, Object> value) {
        exact(value, "lane", "workload_id", "executor_version", "backend", "os",
                "architecture", "runtime_capability", "sample_count", "baseline_count",
                "recent_count", "baseline_median_ms", "recent_median_ms", "ratio_percent",
                "classification");
        for (String field : List.of("lane", "workload_id", "executor_version", "backend", "os",
                "architecture", "runtime_capability")) require(MiniJson.string(value, field)
                        .matches("[a-z0-9][a-z0-9._-]{0,62}"), "invalid performance signal identity");
        require("campaign".equals(MiniJson.string(value, "lane"))
                && "campaign-worker".equals(MiniJson.string(value, "runtime_capability")),
                "performance signal escaped campaign scope");
        long samples = MiniJson.integer(value, "sample_count");
        long baseline = MiniJson.integer(value, "baseline_count");
        long recent = MiniJson.integer(value, "recent_count");
        String classification = MiniJson.string(value, "classification");
        require(Set.of("insufficient-history", "zero-baseline", "increase", "decrease",
                "within-band").contains(classification) && samples >= 0L && baseline >= 0L
                && recent >= 0L && baseline + recent <= samples,
                "invalid performance signal classification or counts");
        return new Signal(MiniJson.string(value, "workload_id"),
                MiniJson.string(value, "executor_version"), MiniJson.string(value, "backend"),
                MiniJson.string(value, "os"), MiniJson.string(value, "architecture"), samples,
                baseline, recent, nullable(value, "baseline_median_ms"),
                nullable(value, "recent_median_ms"), nullable(value, "ratio_percent"), classification);
    }

    private static Long nullable(Map<String, Object> value, String key) {
        Object item = value.get(key); require(item == null || item instanceof Long, key + " is not nullable integer");
        require(item == null || (Long) item >= 0L, key + " is negative"); return (Long) item;
    }
    private static void exact(Map<String, Object> value, String... fields) {
        require(new ArrayList<>(value.keySet()).equals(List.of(fields)),
                "performance signal fields drifted");
    }

    private static Ticket ticket(Path path) throws Exception {
        require(Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)
                && !Files.isSymbolicLink(path) && Files.size(path) <= 1_024L, "invalid queue ticket");
        Properties values = properties(path);
        require(values.stringPropertyNames().equals(Set.of("pid", "process-start", "created")),
                "queue ticket fields drifted");
        long pid = number(values, "pid"), started = number(values, "process-start");
        long created = Instant.parse(values.getProperty("created")).toEpochMilli();
        require(pid > 0L && started > 0L, "invalid queue ticket identity");
        return new Ticket(pid, started, created);
    }

    private static Properties properties(Path path) throws Exception {
        Properties values = new Properties();
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            values.load(reader);
        }
        return values;
    }
    private static long number(Properties values, String key) {
        try { long value = Long.parseLong(values.getProperty(key)); require(value >= 0L, key + " is negative");
            return value; } catch (RuntimeException error) {
            throw new IllegalArgumentException(key + " is not a nonnegative integer", error); }
    }
    private static String javaTool() { boolean windows = System.getProperty("os.name", "")
            .toLowerCase(java.util.Locale.ROOT).contains("win"); return Path.of(
            System.getProperty("java.home"), "bin", "java" + (windows ? ".exe" : "")).toString(); }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalArgumentException(message);
    }

    record Execution(String json, long sampleCount, long failureCount, Performance performance,
            EvidenceExplorerRegressionTriage.Snapshot triage) { }
    record Performance(String metric, long changeThresholdPercent, long signalCount,
            long increaseCount, long insufficientHistoryCount, List<Signal> signals) { }
    record Signal(String workloadId, String executorVersion, String backend, String os, String architecture,
            long sampleCount, long baselineCount, long recentCount, Long baselineMedianMs,
            Long recentMedianMs, Long ratioPercent, String classification) { }
    record Admission(String os, String architecture, long sampleCount,
            long observedPeakMemoryBytes, long minimumWorkerMemoryBytes) { }
    record Queue(String label, int waiting, int live, int abandoned, int partial,
            Long oldestWaitMs) { }
    record CacheDecision(String smoke, String fingerprint, boolean cacheEnabled,
            boolean localProof, boolean trackedPin, String decision, String reason,
            String currentTreeMode) { }
    private record Ticket(long pid, long started, long created) { }
}
