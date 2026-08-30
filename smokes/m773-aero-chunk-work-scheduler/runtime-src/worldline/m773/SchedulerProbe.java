package worldline.m773;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.render.chunk.ChunkBuilder;

/** Allocation-light retained-window census for frame and rebuild work. */
public final class SchedulerProbe {
    private static final List<Long> FRAMES = new ArrayList<Long>(4096);
    private static ChunkBuilder hiddenTarget;
    private static int frame, currentBuilds, currentVisible;
    private static int totalBuilds, visibleBuilds, maxBuilds, hiddenBuildFrame;
    private static long currentNanos, maxNanos, lastFrameStart;

    private SchedulerProbe() {}

    public static void beginFrame(boolean retaining) {
        if (retaining) finishRebuildFrame();
        currentBuilds = currentVisible = 0;
        currentNanos = 0L;
        frame++;
        long now = System.nanoTime();
        if (retaining && lastFrameStart != 0L) FRAMES.add(Long.valueOf(now - lastFrameStart));
        lastFrameStart = retaining ? now : 0L;
    }

    public static long beginRebuild() {
        return SchedulerState.retaining() ? System.nanoTime() : 0L;
    }

    public static void endRebuild(ChunkBuilder chunk, long started) {
        if (started == 0L) return;
        currentBuilds++;
        totalBuilds++;
        if (chunk.inFrustum) {
            currentVisible++;
            visibleBuilds++;
        }
        currentNanos += System.nanoTime() - started;
        if (chunk == hiddenTarget && hiddenBuildFrame == 0)
            hiddenBuildFrame = SchedulerState.retainedFrame();
    }

    public static void target(ChunkBuilder chunk) { hiddenTarget = chunk; }

    public static void write(File metrics, File frames, String arm,
                             int maxBacklog, int finalBacklog, int machines)
            throws Exception {
        finishRebuildFrame();
        try (PrintWriter out = new PrintWriter(new FileWriter(metrics))) {
            out.println("arm=" + arm);
            out.println("frames=" + FRAMES.size());
            out.println("total.rebuilds=" + totalBuilds);
            out.println("visible.rebuilds=" + visibleBuilds);
            out.println("max.rebuilds.frame=" + maxBuilds);
            out.println("max.rebuild.nanos.frame=" + maxNanos);
            out.println("hidden.target.frame=" + hiddenBuildFrame);
            out.println("max.backlog=" + maxBacklog);
            out.println("final.backlog=" + finalBacklog);
            out.println("machines=" + machines);
        }
        try (BufferedWriter out = new BufferedWriter(new FileWriter(frames))) {
            for (Long value : FRAMES) {
                out.write(Long.toString(value.longValue()));
                out.newLine();
            }
        }
    }

    private static void finishRebuildFrame() {
        if (currentBuilds > maxBuilds) maxBuilds = currentBuilds;
        if (currentNanos > maxNanos) maxNanos = currentNanos;
    }
}
