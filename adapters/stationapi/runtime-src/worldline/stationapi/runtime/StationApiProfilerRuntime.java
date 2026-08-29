package worldline.stationapi.runtime;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import worldline.profiling.JvmProfilerSampler;
import worldline.profiling.ProfilerArtifacts;
import worldline.profiling.ProfilerMetric;
import worldline.profiling.ProfilerRegistry;
import worldline.profiling.ProfilerRun;
import worldline.profiling.ProfilerSession;
import worldline.profiling.WorldlineProfilerMetrics;

/** Owns one bounded, allocation-free-on-frame-path StationAPI capture. */
final class StationApiProfilerRuntime {
    private static final boolean ENABLED = Boolean.getBoolean("worldline.profiler.enabled");
    private static final List<StationApiProfiler.Metric> EXTENSIONS = new ArrayList<>();
    private static ProfilerSession session;
    private static ProfilerRegistry registry;
    private static boolean initialized, open, sealed;
    private static boolean armed = Boolean.parseBoolean(
            System.getProperty("worldline.profiler.autoStart", "true"));
    private static int frames, capacity;
    private static long sequence, frameStarted, startedEpoch;
    private static long pendingTick, pendingTickMax, pendingTickCalls, pendingDisplay;
    private static ProfilerRegistry.Handle tick, tickMax, tickCalls, display, camera;

    private StationApiProfilerRuntime() {}

    static synchronized StationApiProfiler.Metric register(ProfilerMetric metric) {
        if (metric == null || !metric.extensionOwned())
            throw new IllegalArgumentException("StationAPI profiler extensions require mod ownership");
        if (initialized) throw new IllegalStateException("profiler schema is already closed");
        for (StationApiProfiler.Metric present : EXTENSIONS)
            if (present.name().equals(metric.name()))
                throw new IllegalArgumentException("duplicate profiler extension: " + metric.name());
        StationApiProfiler.Metric token = new StationApiProfiler.Metric(metric);
        EXTENSIONS.add(token); return token;
    }

    static void beginFrame() {
        if (!ENABLED || !armed || sealed) return;
        initialize();
        if (frames >= capacity) { finish("capacity"); return; }
        frameStarted = System.nanoTime();
        session.beginFrame(sequence++, frameStarted); open = true;
        session.set(tick, pendingTick); session.set(tickMax, pendingTickMax);
        session.set(tickCalls, pendingTickCalls); session.set(display, pendingDisplay);
        pendingTick = pendingTickMax = pendingTickCalls = pendingDisplay = 0L;
    }

    static void endFrame() {
        if (!open) return;
        long now = System.nanoTime(); session.set(camera, now - frameStarted);
        session.endFrame(now); open = false; frames++;
        if (frames >= capacity) finish("capacity");
    }

    static boolean frameOpen() { return open; }
    static void startCapture() { if (ENABLED && !sealed) armed = true; }
    static long timer() { return ENABLED && armed && !sealed ? System.nanoTime() : 0L; }
    static void tick(long elapsed) {
        if (!ENABLED || !armed || sealed || elapsed < 0L) return;
        pendingTick = Math.addExact(pendingTick, elapsed);
        pendingTickMax = Math.max(pendingTickMax, elapsed); pendingTickCalls++;
    }
    static void display(long elapsed) {
        if (ENABLED && armed && !sealed && elapsed >= 0L)
            pendingDisplay = Math.addExact(pendingDisplay, elapsed);
    }
    static void elapsed(String name, long start) {
        if (open && start != 0L) session.addElapsed(registry.require(name), start, System.nanoTime());
    }
    static void count(String name) { if (open) session.add(registry.require(name), 1L); }
    static void gauge(String name, long value) {
        if (open) session.maximum(registry.require(name), value);
    }
    static void add(StationApiProfiler.Metric metric, long value) {
        if (open) session.add(metric.handle, value);
    }
    static void maximum(StationApiProfiler.Metric metric, long value) {
        if (open) session.maximum(metric.handle, value);
    }

    static void finish(String reason) {
        if (!ENABLED || sealed || !initialized || frames == 0 || open) return;
        try {
            Map<String, String> tags = new LinkedHashMap<>();
            tags.put("runtime.version", "b1.7.3"); tags.put("driver.id", "stationapi");
            tags.put("loader.id", "babric"); tags.put("capture.reason", reason);
            tags.put("scenario.id", System.getProperty("worldline.profiler.scenario", "unspecified"));
            ProfilerRun run = session.seal(mode(), startedEpoch, System.currentTimeMillis(), tags);
            Path output = Paths.get(System.getProperty("worldline.profiler.output",
                    ".worldline/profiler/stationapi.wlpr"));
            ProfilerArtifacts.write(output, run); sealed = true;
            System.out.println("WORLDLINE_PROFILER_ARTIFACT=" + output.toAbsolutePath().normalize()
                    + " frames=" + frames + " metrics=" + registry.schema().size());
        } catch (Exception error) { throw new IllegalStateException("profiler seal failed", error); }
    }

    private static synchronized void initialize() {
        if (initialized) return;
        ProfilerRegistry.Builder builder = ProfilerRegistry.builder().support(
                WorldlineProfilerMetrics.FRAME_WALL, WorldlineProfilerMetrics.CLIENT_TICK,
                WorldlineProfilerMetrics.CLIENT_TICK_MAX, WorldlineProfilerMetrics.CLIENT_TICK_CALLS,
                WorldlineProfilerMetrics.RENDER_CAMERA, WorldlineProfilerMetrics.RENDER_WORLD,
                WorldlineProfilerMetrics.DISPLAY_PRESENT, WorldlineProfilerMetrics.CHUNK_COMPILE,
                WorldlineProfilerMetrics.CHUNK_REBUILD, "chunk.rebuild.calls", "chunk.backlog.count");
        JvmProfilerSampler.registerCapabilities(builder);
        for (StationApiProfiler.Metric extension : EXTENSIONS) builder.extension(extension.definition);
        registry = builder.build();
        for (StationApiProfiler.Metric extension : EXTENSIONS)
            extension.handle = registry.require(extension.name());
        tick = registry.require(WorldlineProfilerMetrics.CLIENT_TICK);
        tickMax = registry.require(WorldlineProfilerMetrics.CLIENT_TICK_MAX);
        tickCalls = registry.require(WorldlineProfilerMetrics.CLIENT_TICK_CALLS);
        display = registry.require(WorldlineProfilerMetrics.DISPLAY_PRESENT);
        camera = registry.require(WorldlineProfilerMetrics.RENDER_CAMERA);
        capacity = Math.max(1, Math.min(5_000_000,
                Integer.getInteger("worldline.profiler.capacity", 36_000)));
        session = new ProfilerSession(registry, capacity, new JvmProfilerSampler(registry));
        startedEpoch = System.currentTimeMillis(); initialized = true;
    }

    private static ProfilerRun.Mode mode() {
        String value = System.getProperty("worldline.profiler.mode", "mixed").toUpperCase();
        try { return ProfilerRun.Mode.valueOf(value); }
        catch (IllegalArgumentException invalid) { return ProfilerRun.Mode.MIXED; }
    }
}
