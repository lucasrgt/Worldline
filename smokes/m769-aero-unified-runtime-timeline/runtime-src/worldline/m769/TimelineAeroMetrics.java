package worldline.m769;

import aero.modellib.Aero_BECellRenderer;
import aero.modellib.Aero_FrameSpikeLogger;
import worldline.profiling.ClientProfiler;
import worldline.profiling.WorldlineProfilerMetrics;

/** Registers and publishes the Aero-owned portion of the generic frame schema. */
public final class TimelineAeroMetrics {
    private static ClientProfiler.Metric save;
    private static ClientProfiler.Metric enqueue;
    private static ClientProfiler.Metric flush;
    private static ClientProfiler.Metric queued;
    private static ClientProfiler.Metric pageCalls;
    private static ClientProfiler.Metric pageRebuilds;
    private static boolean initialized;

    private TimelineAeroMetrics() {}

    public static void initialize() {
        if (initialized) return;
        save = ClientProfiler.register(WorldlineProfilerMetrics.extensionDuration(
                "mod.aero.worldsave.nanos", "aero"));
        enqueue = ClientProfiler.register(WorldlineProfilerMetrics.extensionDuration(
                "mod.aero.enqueue.nanos", "aero"));
        flush = ClientProfiler.register(WorldlineProfilerMetrics.extensionDuration(
                "mod.aero.flush.nanos", "aero"));
        queued = ClientProfiler.register(WorldlineProfilerMetrics.extensionCounter(
                "mod.aero.pages.queued", "aero"));
        pageCalls = ClientProfiler.register(WorldlineProfilerMetrics.extensionCounter(
                "mod.aero.pages.calls", "aero"));
        pageRebuilds = ClientProfiler.register(WorldlineProfilerMetrics.extensionCounter(
                "mod.aero.pages.rebuilds", "aero"));
        initialized = true;
    }

    public static void snapshotPriorFrame() {
        if (!ClientProfiler.active()) return;
        ClientProfiler.add(save, nonnegative(Aero_FrameSpikeLogger.worldSaveNanos()));
        ClientProfiler.add(queued, nonnegative(Aero_BECellRenderer.queuedLastFrame()));
        ClientProfiler.add(pageCalls, nonnegative(Aero_BECellRenderer.pageCallsThisFrame()));
        ClientProfiler.add(pageRebuilds,
                nonnegative(Aero_BECellRenderer.pageRebuildsThisFrame()));
    }

    public static long begin() {
        return ClientProfiler.startTimer();
    }

    public static void endEnqueue(long started) {
        ClientProfiler.addElapsed(enqueue, started);
    }

    public static void endFlush(long started) {
        ClientProfiler.addElapsed(flush, started);
    }

    private static long nonnegative(long value) {
        return value < 0L ? 0L : value;
    }
}
