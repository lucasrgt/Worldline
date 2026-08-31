package worldline.m777;

import aero.modellib.Aero_AnimatedBatcher;
import aero.modellib.Aero_DisplayListBudget;
import aero.modellib.Aero_MeshRenderer;
import aero.modellib.Aero_Prewarm;
import aero.modellib.model.Aero_ObjLoader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

/** Allocation-light frame and Aero GL-work census for the M777 matrix. */
public final class MatrixProbe {
    private static final List<Long> WALLS = new ArrayList<Long>(1024);
    private static final List<Long> ALLOCS = new ArrayList<Long>(1024);
    private static final List<Long> CPUS = new ArrayList<Long>(1024);
    private static final List<Integer> PHASES = new ArrayList<Integer>(1024);
    private static final List<Integer> FRAME_AT_REST = new ArrayList<Integer>(1024);
    private static final List<Integer> FRAME_LIST_CALLS = new ArrayList<Integer>(1024);
    private static final com.sun.management.ThreadMXBean ALLOC = allocationBean();
    private static final java.lang.management.ThreadMXBean CPU =
        ManagementFactory.getThreadMXBean();
    private static long lastStart, lastAllocation, lastCpu, allocatedBytes, cpuNanos;
    private static int atRestRenders, listCalls, fallbacks, animatedInstances;
    private static int prewarmDrained, prewarmUrgent, maxQueued, renderSamples;

    private MatrixProbe() {}

    public static void beginFrame(boolean retaining, int phase) {
        long now = System.nanoTime(), allocation = allocated(), cpu = cpu();
        if (retaining && lastStart != 0L) {
            WALLS.add(Long.valueOf(now - lastStart));
            long delta = allocation >= lastAllocation ? allocation - lastAllocation : 0L;
            long cpuDelta = cpu >= lastCpu ? cpu - lastCpu : 0L;
            ALLOCS.add(Long.valueOf(delta));
            CPUS.add(Long.valueOf(cpuDelta));
            PHASES.add(Integer.valueOf(phase));
            allocatedBytes += delta;
            cpuNanos += cpuDelta;
        }
        lastStart = retaining ? now : 0L;
        lastAllocation = retaining ? allocation : 0L;
        lastCpu = retaining ? cpu : 0L;
    }

    public static void write(File metrics, File frames, String arm, int machines) throws Exception {
        try (PrintWriter out = new PrintWriter(new FileWriter(metrics))) {
            out.println("arm=" + arm);
            out.println("frames=" + WALLS.size());
            out.println("machines=" + machines);
            out.println("frame.allocated.bytes=" + allocatedBytes);
            out.println("frame.cpu.nanos=" + cpuNanos);
            out.println("atrest.renders=" + atRestRenders);
            out.println("atrest.list.calls=" + listCalls);
            out.println("atrest.fallbacks=" + fallbacks);
            out.println("animated.instances=" + animatedInstances);
            out.println("display.allocated=" + Aero_DisplayListBudget.totalAllocatedLists());
            out.println("display.live=" + Aero_DisplayListBudget.liveLists());
            out.println("display.peak=" + Aero_DisplayListBudget.peakLiveLists());
            out.println("display.denied=" + Aero_DisplayListBudget.deniedAllocations());
            out.println("display.failed=" + Aero_DisplayListBudget.failedAllocations());
            out.println("prewarm.drained=" + prewarmDrained);
            out.println("prewarm.urgent.drained=" + prewarmUrgent);
            out.println("prewarm.queued.total=" + Aero_Prewarm.queuedModelsTotal());
            out.println("prewarm.promoted=" + Aero_Prewarm.promotedModelsTotal());
            out.println("prewarm.dropped=" + Aero_Prewarm.droppedModelsTotal());
            out.println("prewarm.max.queued=" + maxQueued);
            out.println("prewarm.final.queued=" + Aero_Prewarm.queuedModelCount());
            out.println("prewarm.admission.tracked=" + Aero_Prewarm.admissionTracked());
            out.println("prewarm.admission.accepted=" + Aero_Prewarm.admissionAcceptedTotal());
            out.println("prewarm.admission.rejected=" + Aero_Prewarm.admissionRejectedTotal());
            out.println("prewarm.admission.expired=" + Aero_Prewarm.admissionExpiredTotal());
            out.println("prewarm.pressure.skips=" + Aero_Prewarm.pressureSkipsTotal());
            out.println("prewarm.firstuse.misses=" + Aero_Prewarm.firstUseMissesTotal());
            out.println("obj.cache.size=" + Aero_ObjLoader.cacheSize());
            out.println("render.samples=" + renderSamples);
        }
        if (FRAME_AT_REST.size() < WALLS.size() || FRAME_LIST_CALLS.size() < WALLS.size())
            throw new IllegalStateException("M777 per-frame render census incomplete");
        try (BufferedWriter out = new BufferedWriter(new FileWriter(frames))) {
            for (int i = 0; i < WALLS.size(); i++) {
                out.write(PHASES.get(i) + "," + WALLS.get(i) + "," + ALLOCS.get(i)
                    + "," + CPUS.get(i) + "," + FRAME_AT_REST.get(i)
                    + "," + FRAME_LIST_CALLS.get(i));
                out.newLine();
            }
        }
    }

    public static void sampleRenderWorld() {
        if (!MatrixState.retaining()) return;
        renderSamples++;
        int frameAtRest = Aero_MeshRenderer.atRestRendersThisFrame();
        int frameListCalls = Aero_MeshRenderer.atRestListCallsThisFrame();
        FRAME_AT_REST.add(Integer.valueOf(frameAtRest));
        FRAME_LIST_CALLS.add(Integer.valueOf(frameListCalls));
        atRestRenders += frameAtRest;
        listCalls += frameListCalls;
        fallbacks += Aero_MeshRenderer.atRestTessFallbacksThisFrame();
        animatedInstances += Aero_AnimatedBatcher.flushedInstancesThisFrame();
        prewarmDrained += Aero_Prewarm.drainedThisFrame();
        prewarmUrgent += Aero_Prewarm.urgentDrainedThisFrame();
        maxQueued = Math.max(maxQueued, Aero_Prewarm.queuedModelCount());
    }

    public static boolean hasCompleteFrames(int required) { return WALLS.size() >= required; }

    private static long allocated() {
        return ALLOC == null ? 0L : ALLOC.getThreadAllocatedBytes(Thread.currentThread().getId());
    }

    private static long cpu() {
        if (!CPU.isCurrentThreadCpuTimeSupported()) return 0L;
        if (!CPU.isThreadCpuTimeEnabled()) CPU.setThreadCpuTimeEnabled(true);
        return CPU.getCurrentThreadCpuTime();
    }

    private static com.sun.management.ThreadMXBean allocationBean() {
        java.lang.management.ThreadMXBean value = ManagementFactory.getThreadMXBean();
        if (!(value instanceof com.sun.management.ThreadMXBean)) return null;
        com.sun.management.ThreadMXBean bean = (com.sun.management.ThreadMXBean) value;
        if (!bean.isThreadAllocatedMemoryEnabled()) bean.setThreadAllocatedMemoryEnabled(true);
        return bean;
    }
}
