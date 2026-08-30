package worldline.m776;

import aero.modellib.Aero_AnimatedBatcher;
import aero.modellib.Aero_DisplayListBudget;
import aero.modellib.Aero_MeshRenderer;
import aero.modellib.Aero_Prewarm;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

/** Allocation-light frame and Aero GL-work census for the M776 matrix. */
public final class MatrixProbe {
    private static final List<Long> WALLS = new ArrayList<Long>(1024);
    private static final List<Long> ALLOCS = new ArrayList<Long>(1024);
    private static final List<Integer> PHASES = new ArrayList<Integer>(1024);
    private static final com.sun.management.ThreadMXBean ALLOC = allocationBean();
    private static long lastStart, lastAllocation, allocatedBytes;
    private static int atRestRenders, listCalls, fallbacks, animatedInstances;
    private static int prewarmDrained, prewarmUrgent, maxQueued, renderSamples;

    private MatrixProbe() {}

    public static void beginFrame(boolean retaining, int phase) {
        long now = System.nanoTime(), allocation = allocated();
        if (retaining && lastStart != 0L) {
            WALLS.add(Long.valueOf(now - lastStart));
            long delta = allocation >= lastAllocation ? allocation - lastAllocation : 0L;
            ALLOCS.add(Long.valueOf(delta));
            PHASES.add(Integer.valueOf(phase));
            allocatedBytes += delta;
        }
        lastStart = retaining ? now : 0L;
        lastAllocation = retaining ? allocation : 0L;
    }

    public static void write(File metrics, File frames, String arm, int machines) throws Exception {
        try (PrintWriter out = new PrintWriter(new FileWriter(metrics))) {
            out.println("arm=" + arm);
            out.println("frames=" + WALLS.size());
            out.println("machines=" + machines);
            out.println("frame.allocated.bytes=" + allocatedBytes);
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
            out.println("render.samples=" + renderSamples);
        }
        try (BufferedWriter out = new BufferedWriter(new FileWriter(frames))) {
            for (int i = 0; i < WALLS.size(); i++) {
                out.write(PHASES.get(i) + "," + WALLS.get(i) + "," + ALLOCS.get(i));
                out.newLine();
            }
        }
    }

    public static void sampleRenderWorld() {
        if (!MatrixState.retaining()) return;
        renderSamples++;
        atRestRenders += Aero_MeshRenderer.atRestRendersThisFrame();
        listCalls += Aero_MeshRenderer.atRestListCallsThisFrame();
        fallbacks += Aero_MeshRenderer.atRestTessFallbacksThisFrame();
        animatedInstances += Aero_AnimatedBatcher.flushedInstancesThisFrame();
        prewarmDrained += Aero_Prewarm.drainedThisFrame();
        prewarmUrgent += Aero_Prewarm.urgentDrainedThisFrame();
        maxQueued = Math.max(maxQueued, Aero_Prewarm.queuedModelCount());
    }

    private static long allocated() {
        return ALLOC == null ? 0L : ALLOC.getThreadAllocatedBytes(Thread.currentThread().getId());
    }

    private static com.sun.management.ThreadMXBean allocationBean() {
        java.lang.management.ThreadMXBean value = ManagementFactory.getThreadMXBean();
        if (!(value instanceof com.sun.management.ThreadMXBean)) return null;
        com.sun.management.ThreadMXBean bean = (com.sun.management.ThreadMXBean) value;
        if (!bean.isThreadAllocatedMemoryEnabled()) bean.setThreadAllocatedMemoryEnabled(true);
        return bean;
    }
}
