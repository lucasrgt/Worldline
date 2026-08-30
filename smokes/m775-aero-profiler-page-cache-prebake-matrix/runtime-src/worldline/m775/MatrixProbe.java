package worldline.m775;

import aero.modellib.Aero_BECellRenderer;
import aero.modellib.Aero_ChunkCompileBudget;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

/** Allocation-light retained-frame, page-cache, and chunk-work census. */
public final class MatrixProbe {
    private static final List<Long> WALLS = new ArrayList<Long>(4096);
    private static final List<Long> ALLOCS = new ArrayList<Long>(4096);
    private static final List<Integer> PHASES = new ArrayList<Integer>(4096);
    private static final com.sun.management.ThreadMXBean ALLOC = allocationBean();
    private static long lastStart, lastAllocation, currentRebuildNanos;
    private static long totalRebuildNanos, maxRebuildNanos, allocatedBytes;
    private static int totalRebuilds, maxRebuilds, currentRebuilds;
    private static int chunkBuilt, chunkVisible, chunkPrebake, chunkUrgent;
    private static int pageCalls, pageRebuilds, directCalls, maxCachedPages;

    private MatrixProbe() {}

    public static void beginFrame(boolean retaining, int phase) {
        long now = System.nanoTime(), allocation = allocated();
        if (retaining && lastStart != 0L) {
            WALLS.add(Long.valueOf(now - lastStart));
            long delta = allocation >= lastAllocation ? allocation - lastAllocation : 0L;
            ALLOCS.add(Long.valueOf(delta));
            PHASES.add(Integer.valueOf(phase));
            allocatedBytes += delta;
            finishRebuildFrame();
            sampleAero();
        }
        lastStart = retaining ? now : 0L;
        lastAllocation = retaining ? allocation : 0L;
        currentRebuilds = 0;
        currentRebuildNanos = 0L;
    }

    public static long beginRebuild() {
        return MatrixState.retaining() ? System.nanoTime() : 0L;
    }

    public static void endRebuild(long started) {
        if (started == 0L) return;
        long elapsed = System.nanoTime() - started;
        currentRebuilds++;
        totalRebuilds++;
        currentRebuildNanos += elapsed;
        totalRebuildNanos += elapsed;
        if (elapsed > maxRebuildNanos) maxRebuildNanos = elapsed;
    }

    public static void write(File metrics, File frames, String arm,
                             int maxBacklog, int finalBacklog, int machines) throws Exception {
        finishRebuildFrame();
        sampleAero();
        try (PrintWriter out = new PrintWriter(new FileWriter(metrics))) {
            out.println("arm=" + arm);
            out.println("frames=" + WALLS.size());
            out.println("machines=" + machines);
            out.println("final.backlog=" + finalBacklog);
            out.println("max.backlog=" + maxBacklog);
            out.println("frame.allocated.bytes=" + allocatedBytes);
            out.println("chunk.rebuilds=" + totalRebuilds);
            out.println("chunk.rebuild.nanos=" + totalRebuildNanos);
            out.println("chunk.rebuild.max.nanos=" + maxRebuildNanos);
            out.println("chunk.rebuild.max.frame=" + maxRebuilds);
            out.println("chunk.work.built=" + chunkBuilt);
            out.println("chunk.work.visible=" + chunkVisible);
            out.println("chunk.work.prebake=" + chunkPrebake);
            out.println("chunk.work.urgent=" + chunkUrgent);
            out.println("page.calls=" + pageCalls);
            out.println("page.rebuilds=" + pageRebuilds);
            out.println("page.direct=" + directCalls);
            out.println("page.cached.max=" + maxCachedPages);
        }
        try (BufferedWriter out = new BufferedWriter(new FileWriter(frames))) {
            for (int i = 0; i < WALLS.size(); i++) {
                out.write(PHASES.get(i) + "," + WALLS.get(i) + "," + ALLOCS.get(i));
                out.newLine();
            }
        }
    }

    private static void sampleAero() {
        chunkBuilt += Aero_ChunkCompileBudget.builtLastFrame();
        chunkVisible += Aero_ChunkCompileBudget.visibleBuiltLastFrame();
        chunkPrebake += Aero_ChunkCompileBudget.prebakeBuiltLastFrame();
        chunkUrgent += Aero_ChunkCompileBudget.urgentBuiltLastFrame();
        pageCalls += Aero_BECellRenderer.pageCallsThisFrame();
        pageRebuilds += Aero_BECellRenderer.pageRebuildsThisFrame();
        directCalls += Aero_BECellRenderer.directFallbacksThisFrame();
        maxCachedPages = Math.max(maxCachedPages, Aero_BECellRenderer.cachedPageCount());
    }

    private static void finishRebuildFrame() {
        if (currentRebuilds > maxRebuilds) maxRebuilds = currentRebuilds;
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
