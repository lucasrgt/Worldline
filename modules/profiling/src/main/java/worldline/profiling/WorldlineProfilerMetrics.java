package worldline.profiling;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import worldline.profiling.ProfilerMetric.Causality;
import worldline.profiling.ProfilerMetric.Kind;
import worldline.profiling.ProfilerMetric.Unit;

/** Stable neutral names for metrics that runtime drivers may support. */
public final class WorldlineProfilerMetrics {
    public static final String FRAME_WALL = "frame.wall.nanos";
    public static final String FRAME_CPU = "frame.cpu.nanos";
    public static final String FRAME_ALLOCATED = "frame.allocated.bytes";
    public static final String CLIENT_TICK = "client.tick.total.nanos";
    public static final String CLIENT_TICK_MAX = "client.tick.max.nanos";
    public static final String CLIENT_TICK_CALLS = "client.tick.calls";
    public static final String WORLD_TICK = "world.tick.nanos";
    public static final String WORLD_ENTITIES = "world.entities.nanos";
    public static final String WORLD_BLOCKS = "world.blocks.nanos";
    public static final String WORLD_WEATHER = "world.weather.nanos";
    public static final String CHUNK_LOOKUP = "chunk.lookup.nanos";
    public static final String CHUNK_LOAD = "chunk.load.nanos";
    public static final String CHUNK_GENERATE = "chunk.generate.nanos";
    public static final String CHUNK_POPULATE = "chunk.populate.nanos";
    public static final String CHUNK_SAVE = "chunk.save.nanos";
    public static final String CHUNK_FLUSH = "chunk.flush.nanos";
    public static final String CHUNK_COMPILE = "chunk.compile.nanos";
    public static final String CHUNK_REBUILD = "chunk.rebuild.nanos";
    public static final String RENDER_CAMERA = "render.camera.nanos";
    public static final String RENDER_WORLD = "render.world.nanos";
    public static final String RENDER_TERRAIN = "render.terrain.nanos";
    public static final String RENDER_ENTITIES = "render.entities.nanos";
    public static final String RENDER_GUI = "render.gui.nanos";
    public static final String DISPLAY_PRESENT = "display.present.nanos";
    public static final String GPU_FRAME = "gpu.frame.nanos";
    public static final String GPU_WAIT = "gpu.wait.nanos";
    public static final String GC_PAUSE = "jvm.gc.pause.nanos";
    public static final String JIT_COMPILATION = "jvm.jit.compilation.nanos";
    public static final String STREAMING_ACTIVITY = "streaming.activity.count";

    private static final List<ProfilerMetric> STANDARD = standardMetrics();
    private WorldlineProfilerMetrics() {}

    public static ProfilerSchema standardSchema() { return ProfilerSchema.of(STANDARD); }
    public static List<ProfilerMetric> standardMetricsList() { return STANDARD; }

    public static ProfilerMetric extensionDuration(String name, String modId) {
        require(name.startsWith("mod."), "extension metric must use the mod namespace");
        return metric(name, modId, Unit.NANOSECONDS, Kind.DURATION, Causality.NESTED);
    }

    public static ProfilerMetric extensionCounter(String name, String modId) {
        require(name.startsWith("mod."), "extension metric must use the mod namespace");
        return metric(name, modId, Unit.COUNT, Kind.DELTA, Causality.DIAGNOSTIC);
    }

    private static List<ProfilerMetric> standardMetrics() {
        List<ProfilerMetric> metrics = new ArrayList<ProfilerMetric>();
        duration(metrics, FRAME_WALL, Causality.ROOT);
        duration(metrics, FRAME_CPU, Causality.DIAGNOSTIC);
        value(metrics, FRAME_ALLOCATED, Unit.BYTES, Kind.DELTA);
        duration(metrics, CLIENT_TICK, Causality.TOP_LEVEL);
        duration(metrics, CLIENT_TICK_MAX, Causality.NESTED);
        value(metrics, CLIENT_TICK_CALLS, Unit.COUNT, Kind.DELTA);
        duration(metrics, WORLD_TICK, Causality.NESTED);
        duration(metrics, WORLD_ENTITIES, Causality.NESTED);
        duration(metrics, WORLD_BLOCKS, Causality.NESTED);
        duration(metrics, WORLD_WEATHER, Causality.NESTED);
        duration(metrics, CHUNK_LOOKUP, Causality.NESTED);
        duration(metrics, CHUNK_LOAD, Causality.NESTED);
        duration(metrics, CHUNK_GENERATE, Causality.NESTED);
        duration(metrics, CHUNK_POPULATE, Causality.NESTED);
        duration(metrics, CHUNK_SAVE, Causality.NESTED);
        duration(metrics, CHUNK_FLUSH, Causality.NESTED);
        duration(metrics, CHUNK_COMPILE, Causality.NESTED);
        duration(metrics, CHUNK_REBUILD, Causality.NESTED);
        count(metrics, "chunk.lookup.calls"); count(metrics, "chunk.load.calls");
        count(metrics, "chunk.generate.calls"); count(metrics, "chunk.populate.calls");
        count(metrics, "chunk.save.calls"); count(metrics, "chunk.rebuild.calls");
        value(metrics, "chunk.loaded.count", Unit.COUNT, Kind.GAUGE);
        value(metrics, "chunk.backlog.count", Unit.COUNT, Kind.GAUGE);
        duration(metrics, RENDER_CAMERA, Causality.TOP_LEVEL);
        duration(metrics, RENDER_WORLD, Causality.NESTED);
        duration(metrics, RENDER_TERRAIN, Causality.NESTED);
        duration(metrics, RENDER_ENTITIES, Causality.NESTED);
        duration(metrics, RENDER_GUI, Causality.NESTED);
        duration(metrics, DISPLAY_PRESENT, Causality.TOP_LEVEL);
        duration(metrics, "frame.sleep.nanos", Causality.TOP_LEVEL);
        duration(metrics, GPU_FRAME, Causality.DIAGNOSTIC);
        duration(metrics, GPU_WAIT, Causality.NESTED);
        value(metrics, "gpu.upload.bytes", Unit.BYTES, Kind.DELTA);
        duration(metrics, "input.poll.nanos", Causality.NESTED);
        duration(metrics, "audio.update.nanos", Causality.NESTED);
        duration(metrics, "task.execute.nanos", Causality.NESTED);
        count(metrics, "task.execute.calls");
        value(metrics, "task.queue.count", Unit.COUNT, Kind.GAUGE);
        duration(metrics, "render.blockentities.nanos", Causality.NESTED);
        duration(metrics, "render.particles.nanos", Causality.NESTED);
        duration(metrics, "render.weather.nanos", Causality.NESTED);
        duration(metrics, "render.hand.nanos", Causality.NESTED);
        count(metrics, "render.drawcalls.count");
        count(metrics, "render.vertices.count");
        count(metrics, "render.triangles.count");
        count(metrics, "render.texturebinds.count");
        count(metrics, "render.statechanges.count");
        value(metrics, "render.visiblechunks.count", Unit.COUNT, Kind.GAUGE);
        value(metrics, "render.blockentities.count", Unit.COUNT, Kind.GAUGE);
        value(metrics, "render.entities.count", Unit.COUNT, Kind.GAUGE);
        value(metrics, "render.particles.count", Unit.COUNT, Kind.GAUGE);
        duration(metrics, "chunk.unload.nanos", Causality.NESTED);
        duration(metrics, "chunk.upload.nanos", Causality.NESTED);
        count(metrics, "chunk.unload.calls"); count(metrics, "chunk.upload.calls");
        value(metrics, "chunk.dirty.count", Unit.COUNT, Kind.GAUGE);
        duration(metrics, GC_PAUSE, Causality.NESTED);
        count(metrics, "jvm.gc.collections");
        value(metrics, "jvm.heap.used.bytes", Unit.BYTES, Kind.GAUGE);
        value(metrics, "jvm.nonheap.used.bytes", Unit.BYTES, Kind.GAUGE);
        value(metrics, "jvm.threads.live.count", Unit.COUNT, Kind.GAUGE);
        value(metrics, "jvm.classes.loaded.count", Unit.COUNT, Kind.GAUGE);
        value(metrics, "jvm.allocation.bytes", Unit.BYTES, Kind.DELTA);
        duration(metrics, JIT_COMPILATION, Causality.NESTED);
        value(metrics, "io.read.bytes", Unit.BYTES, Kind.DELTA);
        value(metrics, "io.write.bytes", Unit.BYTES, Kind.DELTA);
        count(metrics, "io.read.calls"); count(metrics, "io.write.calls");
        value(metrics, "network.read.bytes", Unit.BYTES, Kind.DELTA);
        value(metrics, "network.write.bytes", Unit.BYTES, Kind.DELTA);
        count(metrics, "network.read.packets"); count(metrics, "network.write.packets");
        duration(metrics, "thread.blocked.nanos", Causality.NESTED);
        duration(metrics, "thread.waited.nanos", Causality.NESTED);
        count(metrics, STREAMING_ACTIVITY);
        return Collections.unmodifiableList(metrics);
    }

    private static void duration(List<ProfilerMetric> target, String name, Causality causality) {
        target.add(metric(name, "worldline", Unit.NANOSECONDS, Kind.DURATION, causality));
    }
    private static void count(List<ProfilerMetric> target, String name) {
        value(target, name, Unit.COUNT, Kind.DELTA);
    }
    private static void value(List<ProfilerMetric> target, String name, Unit unit, Kind kind) {
        target.add(metric(name, "worldline", unit, kind, Causality.DIAGNOSTIC));
    }
    private static ProfilerMetric metric(String name, String owner, Unit unit, Kind kind,
            Causality causality) {
        return ProfilerMetric.of(name, owner, unit, kind, causality);
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalArgumentException(message);
    }
}
