package worldline.m783;

import aero.modellib.Aero_ChunkCompileBudget;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Frame and dirty-to-rebuild latency census for the long client route. */
public final class VisualProbe {
    private static final List<Long> WALLS = new ArrayList<Long>(4096);
    private static final List<Long> ALLOCS = new ArrayList<Long>(4096);
    private static final List<Integer> PHASES = new ArrayList<Integer>(4096);
    private static final List<Integer> LATENCIES = new ArrayList<Integer>(1024);
    private static final Map<Long, Integer> PENDING = new HashMap<Long, Integer>();
    private static final com.sun.management.ThreadMXBean ALLOC = allocationBean();
    private static long lastStart, lastAllocation, allocatedBytes, currentRebuildStarted;
    private static long rebuildNanos, maximumRebuildNanos;
    private static int frame, rebuilds, maxRebuilds, currentRebuilds;
    private static int chunkBuilt, chunkPrebake, chunkUrgent, chunkMaximumBuilt, worldResets;

    private VisualProbe() {}

    public static void beginFrame(boolean retaining, int phase, int currentFrame) {
        long now = System.nanoTime();
        long allocation = allocated();
        if (retaining && lastStart != 0L) {
            WALLS.add(Long.valueOf(now - lastStart));
            long delta = allocation >= lastAllocation ? allocation - lastAllocation : 0L;
            ALLOCS.add(Long.valueOf(delta));
            PHASES.add(Integer.valueOf(phase));
            allocatedBytes += delta;
            finishFrame();
            sampleAero();
        }
        frame = currentFrame;
        lastStart = retaining ? now : 0L;
        lastAllocation = retaining ? allocation : 0L;
        currentRebuilds = 0;
    }

    public static void dirtyVisible(int chunkX, int chunkZ, int dirtyFrame) {
        Long key = Long.valueOf(key(chunkX, chunkZ));
        if (!PENDING.containsKey(key)) PENDING.put(key, Integer.valueOf(dirtyFrame));
    }

    public static long beginRebuild() {
        currentRebuildStarted = VisualState.retaining() ? System.nanoTime() : 0L;
        return currentRebuildStarted;
    }

    public static void endRebuild(long started, int originX, int originZ) {
        if (started == 0L) return;
        long elapsed = System.nanoTime() - started;
        currentRebuilds++;
        rebuilds++;
        rebuildNanos += elapsed;
        maximumRebuildNanos = Math.max(maximumRebuildNanos, elapsed);
        Integer dirty = PENDING.remove(Long.valueOf(key(originX >> 4, originZ >> 4)));
        if (dirty != null) LATENCIES.add(Integer.valueOf(Math.max(0, frame - dirty.intValue())));
    }

    public static void worldReset() {
        PENDING.clear();
        worldResets++;
    }

    public static int pendingVisible() { return PENDING.size(); }

    public static void write(File metrics, File frames, String arm, int maxBacklog,
                             int finalBacklog, int maxVisibleBacklog,
                             int finalVisibleBacklog, int machines) throws Exception {
        finishFrame();
        sampleAero();
        try (PrintWriter out = new PrintWriter(new FileWriter(metrics))) {
            out.println("arm=" + arm);
            out.println("frames=" + WALLS.size());
            out.println("machines=" + machines);
            out.println("world.resets=" + worldResets);
            out.println("final.backlog=" + finalBacklog);
            out.println("max.backlog=" + maxBacklog);
            out.println("final.visible.backlog=" + finalVisibleBacklog);
            out.println("max.visible.backlog=" + maxVisibleBacklog);
            out.println("frame.allocated.bytes=" + allocatedBytes);
            out.println("chunk.rebuilds=" + rebuilds);
            out.println("chunk.rebuild.nanos=" + rebuildNanos);
            out.println("chunk.rebuild.max.nanos=" + maximumRebuildNanos);
            out.println("chunk.rebuild.max.frame=" + maxRebuilds);
            out.println("chunk.work.built=" + chunkBuilt);
            out.println("chunk.work.prebake=" + chunkPrebake);
            out.println("chunk.work.urgent=" + chunkUrgent);
            out.println("chunk.work.max.frame=" + chunkMaximumBuilt);
            out.println("visible.latency.samples=" + LATENCIES.size());
            out.println("visible.latency.maximum.frames=" + maximumLatency());
            out.println("visible.latency.p99.frames=" + latencyP99());
            out.println("visible.latency.pending=" + PENDING.size());
        }
        try (BufferedWriter out = new BufferedWriter(new FileWriter(frames))) {
            for (int index = 0; index < WALLS.size(); index++) {
                out.write(PHASES.get(index) + "," + WALLS.get(index)
                        + "," + ALLOCS.get(index));
                out.newLine();
            }
        }
    }

    private static int maximumLatency() {
        return LATENCIES.isEmpty() ? 0 : Collections.max(LATENCIES).intValue();
    }

    private static int latencyP99() {
        if (LATENCIES.isEmpty()) return 0;
        List<Integer> sorted = new ArrayList<Integer>(LATENCIES);
        Collections.sort(sorted);
        return sorted.get((int) Math.ceil(sorted.size() * 0.99D) - 1).intValue();
    }

    private static void sampleAero() {
        int built = Aero_ChunkCompileBudget.builtLastFrame();
        chunkBuilt += built;
        chunkMaximumBuilt = Math.max(chunkMaximumBuilt, built);
        chunkPrebake += Aero_ChunkCompileBudget.prebakeBuiltLastFrame();
        chunkUrgent += Aero_ChunkCompileBudget.urgentBuiltLastFrame();
    }

    private static void finishFrame() {
        maxRebuilds = Math.max(maxRebuilds, currentRebuilds);
    }

    private static long key(int x, int z) {
        return ((long) x << 32) ^ (z & 0xffffffffL);
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
