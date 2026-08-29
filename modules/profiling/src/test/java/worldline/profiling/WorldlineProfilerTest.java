package worldline.profiling;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Proves the typed schema, hot-path recorder, artifact, summary, and attribution. */
public final class WorldlineProfilerTest {
    private WorldlineProfilerTest() {}

    public static void main(String[] arguments) throws Exception {
        typedSchemaAndExtensions();
        completeHotPathRows();
        capabilityRegistryAndSession();
        jvmSampling();
        sealedArtifactAndAggregates();
        causalAttribution();
        budgetsComparisonsAndExports();
        atomicArtifactPersistence();
        loaderNeutralClientRuntime();
        System.out.println("WorldlineProfilerTest passed");
    }

    private static void typedSchemaAndExtensions() {
        ProfilerSchema core = WorldlineProfilerMetrics.standardSchema();
        require(core.size() >= 40 && core.contains(WorldlineProfilerMetrics.CHUNK_GENERATE)
                && core.contains(WorldlineProfilerMetrics.JIT_COMPILATION),
                "standard profiler schema is incomplete");
        ProfilerMetric duration = WorldlineProfilerMetrics.extensionDuration(
                "mod.example.renderer.nanos", "example-mod");
        ProfilerMetric counter = WorldlineProfilerMetrics.extensionCounter(
                "mod.example.models.count", "example-mod");
        ProfilerSchema extended = core.extend(Arrays.asList(duration, counter));
        require(extended.size() == core.size() + 2 && duration.extensionOwned()
                && duration.category() == ProfilerMetric.Category.MOD
                && core.metric(core.index(WorldlineProfilerMetrics.CHUNK_LOAD)).category()
                        == ProfilerMetric.Category.CHUNK
                && extended.metric(extended.index(duration.name())).equals(duration),
                "profiler extension ownership drifted");
        rejects(() -> core.extend(Collections.singletonList(core.metric(0))));
        rejects(() -> WorldlineProfilerMetrics.extensionDuration("renderer.nanos", "example"));
        rejects(() -> ProfilerMetric.of("Bad", "worldline", ProfilerMetric.Unit.COUNT,
                ProfilerMetric.Kind.DELTA, ProfilerMetric.Causality.DIAGNOSTIC));
        rejects(() -> ProfilerMetric.of("bad.duration", "worldline", ProfilerMetric.Unit.COUNT,
                ProfilerMetric.Kind.DURATION, ProfilerMetric.Causality.NESTED));
        rejects(() -> ProfilerMetric.of("renderer.draws", "example-mod",
                ProfilerMetric.Unit.COUNT, ProfilerMetric.Kind.DELTA,
                ProfilerMetric.Causality.DIAGNOSTIC));
    }

    private static void capabilityRegistryAndSession() {
        ProfilerMetric draws = WorldlineProfilerMetrics.extensionCounter(
                "mod.example.draws.count", "example-mod");
        ProfilerRegistry registry = ProfilerRegistry.builder()
                .support(WorldlineProfilerMetrics.FRAME_WALL,
                        WorldlineProfilerMetrics.CLIENT_TICK)
                .extension(draws).build();
        require(registry.supports(WorldlineProfilerMetrics.FRAME_WALL)
                && !registry.supports(WorldlineProfilerMetrics.CHUNK_LOAD),
                "unsupported capability leaked into profiler schema");
        ProfilerSession session = new ProfilerSession(registry, 2);
        ProfilerRegistry.Handle tick = registry.require(WorldlineProfilerMetrics.CLIENT_TICK);
        ProfilerRegistry.Handle draw = registry.require(draws.name());
        session.beginFrame(4L, 100L);
        session.addElapsed(tick, 102L, 112L); session.add(draw, 2L); session.add(draw, 3L);
        session.endFrame(120L);
        session.beginFrame(5L, 130L); session.endFrame(145L);
        FrameCensus census = session.snapshot();
        require(census.metrics() == 3 && census.value(0, WorldlineProfilerMetrics.FRAME_WALL) == 20L
                && census.value(0, WorldlineProfilerMetrics.CLIENT_TICK) == 10L
                && census.value(0, draws.name()) == 5L
                && census.value(1, draws.name()) == 0L,
                "profiler session capture drifted");
        ProfilerRegistry other = ProfilerRegistry.builder()
                .support(WorldlineProfilerMetrics.FRAME_WALL).build();
        rejects(() -> session.set(other.require(WorldlineProfilerMetrics.FRAME_WALL), 1L));
        rejects(() -> ProfilerRegistry.builder().support(WorldlineProfilerMetrics.FRAME_WALL)
                .support(WorldlineProfilerMetrics.FRAME_WALL));
        ProfilerRun sealed = session.seal(ProfilerRun.Mode.MIXED, 1L, 2L,
                Collections.<String, String>emptyMap());
        require(ProfilerAttribution.standard(sealed).classify(0, 1L, 1, 4)
                .causes().equals(Collections.singletonList("client-tick")),
                "partial-capability attribution drifted");
        rejects(() -> session.beginFrame(6L, 150L));
    }

    private static void jvmSampling() {
        ProfilerRegistry.Builder builder = ProfilerRegistry.builder()
                .support(WorldlineProfilerMetrics.FRAME_WALL);
        JvmProfilerSampler.registerCapabilities(builder);
        ProfilerRegistry registry = builder.build();
        ProfilerSession session = new ProfilerSession(registry, 1,
                new JvmProfilerSampler(registry));
        session.beginFrame(0L, 1L);
        long end = 2L;
        for (int index = 0; index < 10_000; index++) end += index & 1;
        session.endFrame(end);
        FrameCensus census = session.snapshot();
        require(census.value(0, WorldlineProfilerMetrics.FRAME_WALL) == end - 1L
                && registry.supports("jvm.heap.used.bytes")
                && census.value(0, "jvm.heap.used.bytes") >= 0L,
                "JVM profiler sampler produced invalid telemetry");
    }

    private static void completeHotPathRows() {
        ProfilerSchema schema = ProfilerSchema.of(Arrays.asList(
                ProfilerMetric.of("frame.wall.nanos", "worldline",
                        ProfilerMetric.Unit.NANOSECONDS, ProfilerMetric.Kind.DURATION,
                        ProfilerMetric.Causality.ROOT),
                WorldlineProfilerMetrics.extensionCounter("mod.example.draws.count", "example")));
        ProfilerRecorder recorder = new ProfilerRecorder(schema, 2);
        recorder.beginFrame(7L, 100L);
        recorder.set(0, 20L); recorder.add(1, 2L); recorder.add(1, 3L); recorder.endFrame();
        recorder.beginFrame(8L, 120L);
        recorder.maximum(0, 15L); recorder.maximum(0, 25L); recorder.set(1, 1L);
        recorder.endFrame();
        FrameCensus census = recorder.snapshot();
        require(census.frames() == 2 && census.value(0, "mod.example.draws.count") == 5L
                && census.value(1, "frame.wall.nanos") == 25L,
                "profiler recorder values drifted");
        rejects(() -> recorder.beginFrame(10L, 130L));
        ProfilerRecorder missing = new ProfilerRecorder(schema, 1);
        missing.beginFrame(0L, 1L); missing.set(0, 1L);
        rejects(missing::endFrame);
        ProfilerRecorder duplicate = new ProfilerRecorder(schema, 1);
        duplicate.beginFrame(0L, 1L); duplicate.set(0, 1L);
        rejects(() -> duplicate.set(0, 2L));
    }

    private static void sealedArtifactAndAggregates() {
        ProfilerRun run = run(ProfilerRun.Mode.STEADY);
        byte[] encoded = ProfilerRunCodec.encode(run);
        ProfilerRun decoded = ProfilerRunCodec.decode(encoded);
        require(Arrays.equals(encoded, ProfilerRunCodec.encode(decoded))
                && decoded.schema().metric(WorldlineProfilerMetrics.standardSchema().size())
                        .owner().equals("example-mod")
                && "b1.7.3".equals(decoded.tag("runtime.version")),
                "profiler artifact round trip drifted");
        ProfilerSummary summary = new ProfilerSummary(decoded);
        require(summary.frames() == 3
                && summary.total(WorldlineProfilerMetrics.FRAME_WALL) == 160L
                && summary.mean(WorldlineProfilerMetrics.FRAME_WALL) == 53L
                && summary.maximum(WorldlineProfilerMetrics.FRAME_WALL) == 100L
                && summary.percentile(WorldlineProfilerMetrics.FRAME_WALL, 95, 100) == 100L
                && summary.worstFrame(WorldlineProfilerMetrics.FRAME_WALL) == 1
                && summary.countAtLeast(WorldlineProfilerMetrics.FRAME_WALL, 40L) == 2L
                && summary.unattributedWall(1) == 5L
                && summary.steadyQualified(WorldlineProfilerMetrics.STREAMING_ACTIVITY),
                "profiler aggregate drifted");
        require(!new ProfilerSummary(run(ProfilerRun.Mode.STREAMING))
                .steadyQualified(WorldlineProfilerMetrics.STREAMING_ACTIVITY),
                "streaming run qualified as steady");
        byte[] corrupt = encoded.clone(); corrupt[12] ^= 1;
        rejects(() -> ProfilerRunCodec.decode(corrupt));
        rejects(() -> ProfilerRunCodec.decode(Arrays.copyOf(encoded, encoded.length - 1)));
    }

    private static void causalAttribution() {
        ProfilerRun run = run(ProfilerRun.Mode.STEADY);
        ProfilerAttribution.Result result = ProfilerAttribution.standard(run)
                .classify(1, 1L, 1, 4);
        require(result.mixed() && !result.unknown()
                && result.causes().equals(Arrays.asList("client-tick", "world", "chunk"))
                && result.valueNanos(2) == 60L && result.thresholdNanos() == 25L,
                "profiler causal attribution drifted: " + result.causes());
        ProfilerAttribution.Result quiet = ProfilerAttribution.standard(run)
                .classify(0, 20L, 1, 2);
        require(quiet.unknown(), "quiet frame received a causal label");
    }

    private static void budgetsComparisonsAndExports() {
        ProfilerRun baseline = run(ProfilerRun.Mode.STEADY);
        List<ProfilerBudgetPolicy.Rule> rules = Arrays.asList(
                ProfilerBudgetPolicy.Rule.of(WorldlineProfilerMetrics.FRAME_WALL,
                        ProfilerBudgetPolicy.Statistic.P95, 80L,
                        ProfilerBudgetPolicy.Severity.CRITICAL),
                ProfilerBudgetPolicy.Rule.of(WorldlineProfilerMetrics.GC_PAUSE,
                        ProfilerBudgetPolicy.Statistic.MAX, 0L,
                        ProfilerBudgetPolicy.Severity.WARNING));
        List<ProfilerBudgetPolicy.Finding> findings =
                new ProfilerBudgetPolicy(rules).evaluate(baseline);
        require(findings.size() == 1 && findings.get(0).actual() == 100L
                && findings.get(0).excess() == 20L
                && "budget.critical".equals(findings.get(0).code()),
                "profiler budget classification drifted");
        ProfilerComparison.Result same = new ProfilerComparison(baseline, baseline).compare(
                WorldlineProfilerMetrics.FRAME_WALL, ProfilerBudgetPolicy.Statistic.P95,
                1L, 10_000);
        require(same.verdict() == ProfilerComparison.Verdict.EQUIVALENT
                && same.delta() == 0L && same.relativePpm() == 0L,
                "profiler A/B equivalence drifted");
        String json = ProfilerExport.json(baseline);
        String metrics = ProfilerExport.openMetrics(baseline);
        require(json.startsWith("{\"schema\":1") && json.contains("\"category\":\"chunk\"")
                && metrics.contains("worldline_profiler_frame_wall_nanos")
                && metrics.endsWith("# EOF\n"), "profiler exports drifted");
    }

    private static void atomicArtifactPersistence() throws Exception {
        java.nio.file.Path directory = java.nio.file.Files.createTempDirectory("worldline-profiler");
        java.nio.file.Path artifact = directory.resolve("capture.wlpr");
        try {
            ProfilerArtifacts.write(artifact, run(ProfilerRun.Mode.MIXED));
            require(java.nio.file.Files.isRegularFile(artifact)
                    && ProfilerArtifacts.read(artifact).mode() == ProfilerRun.Mode.MIXED,
                    "atomic profiler artifact persistence drifted");
            java.nio.file.Files.write(artifact, new byte[] {1, 2, 3});
            rejectsChecked(() -> ProfilerArtifacts.read(artifact));
        } finally {
            java.nio.file.Files.deleteIfExists(artifact);
            java.nio.file.Files.deleteIfExists(directory);
        }
    }

    private static void loaderNeutralClientRuntime() throws Exception {
        java.nio.file.Path directory = java.nio.file.Files.createTempDirectory("worldline-client");
        java.nio.file.Path artifact = directory.resolve("legacy.wlpr");
        try {
            System.setProperty("worldline.profiler.enabled", "true");
            System.setProperty("worldline.profiler.output", artifact.toString());
            System.setProperty("worldline.profiler.scenario", "client-runtime-test");
            ClientProfiler.Metric metric = ClientProfiler.register(
                    WorldlineProfilerMetrics.extensionCounter(
                            "mod.example.client.count", "example-mod"));
            ClientProfilerRuntime.configure("modloader-forge", "forge");
            ClientProfilerRuntime.tick(7L); ClientProfilerRuntime.display(3L);
            ClientProfilerRuntime.beginFrame();
            require(ClientProfiler.active(), "client profiler frame did not open");
            ClientProfiler.add(metric, 2L); ClientProfilerRuntime.endFrame();
            ClientProfilerRuntime.finish("test-close");
            ProfilerRun run = ProfilerArtifacts.read(artifact);
            require("modloader-forge".equals(run.tag("driver.id"))
                    && "forge".equals(run.tag("loader.id"))
                    && run.census().value(0, metric.name()) == 2L
                    && run.census().value(0, WorldlineProfilerMetrics.CLIENT_TICK) == 7L,
                    "loader-neutral client runtime drifted");
        } finally {
            java.nio.file.Files.deleteIfExists(artifact);
            java.nio.file.Files.deleteIfExists(directory);
        }
    }

    private static ProfilerRun run(ProfilerRun.Mode mode) {
        ProfilerSchema schema = WorldlineProfilerMetrics.standardSchema().extend(
                Collections.singletonList(WorldlineProfilerMetrics.extensionDuration(
                        "mod.example.renderer.nanos", "example-mod")));
        long[][] rows = new long[3][schema.size() + 2];
        for (int frame = 0; frame < rows.length; frame++) {
            rows[frame][0] = 30L + frame; rows[frame][1] = 1_000L + frame * 100L;
        }
        set(schema, rows[0], WorldlineProfilerMetrics.FRAME_WALL, 20L);
        set(schema, rows[1], WorldlineProfilerMetrics.FRAME_WALL, 100L);
        set(schema, rows[2], WorldlineProfilerMetrics.FRAME_WALL, 40L);
        set(schema, rows[1], WorldlineProfilerMetrics.CLIENT_TICK, 80L);
        set(schema, rows[1], WorldlineProfilerMetrics.WORLD_TICK, 70L);
        set(schema, rows[1], WorldlineProfilerMetrics.CHUNK_GENERATE, 60L);
        set(schema, rows[1], WorldlineProfilerMetrics.RENDER_CAMERA, 10L);
        set(schema, rows[1], WorldlineProfilerMetrics.DISPLAY_PRESENT, 5L);
        Map<String, String> tags = new LinkedHashMap<String, String>();
        tags.put("runtime.version", "b1.7.3"); tags.put("driver.id", "stationapi");
        return ProfilerRun.of(schema, FrameCensus.of(schema.metricNames(), rows), mode,
                2_000L, 3_000L, tags);
    }

    private static void set(ProfilerSchema schema, long[] row, String metric, long value) {
        row[schema.index(metric) + 2] = value;
    }
    private static void rejects(Runnable action) {
        try { action.run(); throw new AssertionError("invalid profiler input was accepted"); }
        catch (RuntimeException expected) { }
    }
    private static void rejectsChecked(CheckedAction action) {
        try { action.run(); throw new AssertionError("invalid profiler input was accepted"); }
        catch (Exception expected) { }
    }
    private interface CheckedAction { void run() throws Exception; }
    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
