package worldline.m784;

import aero.modellib.Aero_BECellRenderer;
import aero.modellib.Aero_DisplayListBudget;
import aero.modellib.Aero_Prewarm;
import aero.modellib.util.Aero_Profiler;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

/** Captures full-frame, allocation, heap, Cell Page, prewarm, and GL evidence. */
public final class HighMemoryProbe {
    private static final List<Long> WALLS = new ArrayList<Long>(2048);
    private static final List<Long> ALLOCS = new ArrayList<Long>(2048);
    private static final String[] HASHES = new String[HighMemoryScene.CHECKPOINTS];
    private static final boolean[] CAPTURED = new boolean[HighMemoryScene.CHECKPOINTS];
    private static final com.sun.management.ThreadMXBean ALLOC = allocationBean();
    private static long started, allocationStarted, allocatedBytes, heapPeak;
    private static long explicitFlushCalls, explicitFlushNanos;
    private static int captures, blankCaptures, width, height, pageCalls, pageRebuilds, directCalls;
    private static int maxCachedPages, prewarmDrained, compiledStart, expiredStart, evictedStart;
    private static String arm;
    private static boolean sceneCleared;

    private HighMemoryProbe() {}

    public static void beginArm(String name) {
        arm = name;
        explicitFlushCalls = explicitFlushNanos = 0L;
        Aero_Profiler.reset();
        compiledStart = Aero_BECellRenderer.compiledCachedPages();
        expiredStart = Aero_BECellRenderer.expiredCachedPages();
        evictedStart = Aero_BECellRenderer.evictedCachedPages();
    }

    public static void beginFrame() {
        sceneCleared = false;
        if (!HighMemoryState.retaining()) {
            started = allocationStarted = 0L;
            return;
        }
        started = System.nanoTime();
        allocationStarted = allocated();
    }

    /** Removes unrelated terrain after it renders and before the first fixture mesh. */
    public static void beginScene() {
        if (sceneCleared || !HighMemoryState.retaining()) return;
        GL11.glClearColor(0.06F, 0.08F, 0.12F, 1.0F);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        sceneCleared = true;
    }

    public static void sample(Minecraft game) {
        if (!HighMemoryState.retaining() || started == 0L) return;
        long wall = System.nanoTime() - started;
        long allocation = allocated();
        long delta = allocation >= allocationStarted ? allocation - allocationStarted : 0L;
        WALLS.add(Long.valueOf(wall));
        ALLOCS.add(Long.valueOf(delta));
        allocatedBytes += delta;
        Runtime runtime = Runtime.getRuntime();
        heapPeak = Math.max(heapPeak, runtime.totalMemory() - runtime.freeMemory());
        pageCalls += Aero_BECellRenderer.pageCallsThisFrame();
        pageRebuilds += Aero_BECellRenderer.pageRebuildsThisFrame();
        directCalls += Aero_BECellRenderer.directFallbacksThisFrame();
        maxCachedPages = Math.max(maxCachedPages, Aero_BECellRenderer.cachedPageCount());
        prewarmDrained += Aero_Prewarm.drainedThisFrame();
        int checkpoint = HighMemoryState.checkpoint();
        if (HighMemoryState.captureFrame() && !CAPTURED[checkpoint]) capture(game, checkpoint);
    }

    public static int frames() { return WALLS.size(); }
    public static int captures() { return captures; }

    /** Executes and times the one controlled production Cell Page flush for this frame. */
    public static void flush(double cameraX, double cameraY, double cameraZ) {
        long start = System.nanoTime();
        Aero_BECellRenderer.flush(cameraX, cameraY, cameraZ);
        explicitFlushNanos += System.nanoTime() - start;
        explicitFlushCalls++;
    }

    public static void write(File metrics, File frames, String name, int machines) throws Exception {
        Runtime runtime = Runtime.getRuntime();
        try (PrintWriter out = new PrintWriter(new FileWriter(metrics))) {
            out.println("arm=" + name);
            out.println("machines=" + machines);
            out.println("frames=" + WALLS.size());
            out.println("captures=" + captures);
            out.println("captures.blank.rejected=" + blankCaptures);
            out.println("width=" + width);
            out.println("height=" + height);
            out.println("frame.allocated.bytes=" + allocatedBytes);
            out.println("heap.peak.bytes=" + heapPeak);
            out.println("heap.final.bytes=" + (runtime.totalMemory() - runtime.freeMemory()));
            out.println("page.calls=" + pageCalls);
            out.println("page.rebuilds=" + pageRebuilds);
            out.println("page.direct=" + directCalls);
            out.println("page.cached.max=" + maxCachedPages);
            out.println("page.compiled=" + (Aero_BECellRenderer.compiledCachedPages() - compiledStart));
            out.println("page.expired=" + (Aero_BECellRenderer.expiredCachedPages() - expiredStart));
            out.println("page.evicted=" + (Aero_BECellRenderer.evictedCachedPages() - evictedStart));
            out.println("page.flattened=" + Aero_BECellRenderer.flattenedPagesEnabled());
            out.println("page.ttl.frames=" + Aero_BECellRenderer.pageTtlFrames());
            out.println("page.rebuild.budget=" + Aero_BECellRenderer.rebuildsPerFrame());
            out.println("page.cache.max=" + Aero_BECellRenderer.maxCachedPages());
            out.println("flush.calls=" + explicitFlushCalls);
            out.println("flush.nanos=" + explicitFlushNanos);
            out.println("profiler.flush.calls=" + Aero_Profiler.callCount("aero.becell.flush"));
            out.println("profiler.flush.nanos=" + Aero_Profiler.totalNanos("aero.becell.flush"));
            out.println("prewarm.enabled=" + Aero_Prewarm.ENABLED);
            out.println("prewarm.queued.total=" + Aero_Prewarm.queuedModelsTotal());
            out.println("prewarm.drained=" + prewarmDrained);
            out.println("prewarm.pending=" + Aero_Prewarm.queuedModelCount());
            out.println("display.live=" + Aero_DisplayListBudget.liveLists());
            out.println("display.peak=" + Aero_DisplayListBudget.peakLiveLists());
            out.println("display.allocated=" + Aero_DisplayListBudget.totalAllocatedLists());
            out.println("display.denied=" + Aero_DisplayListBudget.deniedAllocations());
            out.println("display.failed=" + Aero_DisplayListBudget.failedAllocations());
            out.println("display.max=" + Aero_DisplayListBudget.maxLiveLists());
            for (int index = 0; index < HASHES.length; index++) {
                out.println("checkpoint." + index + ".sha256=" + HASHES[index]);
            }
        }
        try (BufferedWriter out = new BufferedWriter(new FileWriter(frames))) {
            for (int i = 0; i < WALLS.size(); i++) {
                out.write(WALLS.get(i) + "," + ALLOCS.get(i));
                out.newLine();
            }
        }
    }

    private static void capture(Minecraft game, int checkpoint) {
        try {
            width = game.displayWidth;
            height = game.displayHeight;
            ByteBuffer pixels = BufferUtils.createByteBuffer(width * height * 4);
            GL11.glReadPixels(0, 0, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, pixels);
            byte[] bytes = new byte[pixels.remaining()];
            pixels.get(bytes);
            boolean nonzero = false;
            for (byte value : bytes) {
                if (value != 0) {
                    nonzero = true;
                    break;
                }
            }
            if (!nonzero) {
                blankCaptures++;
                return;
            }
            File directory = new File(System.getProperty("worldline.m784.framesDir"), arm);
            if (!directory.isDirectory() && !directory.mkdirs()) {
                throw new IllegalStateException("cannot create M784 frame directory");
            }
            File output = new File(directory, String.format("checkpoint-%02d.rgba", checkpoint));
            try (FileOutputStream stream = new FileOutputStream(output)) { stream.write(bytes); }
            HASHES[checkpoint] = hash(bytes);
            CAPTURED[checkpoint] = true;
            captures++;
        } catch (Exception error) {
            throw new IllegalStateException("M784 framebuffer capture failed", error);
        }
    }

    private static String hash(byte[] bytes) throws Exception {
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(bytes);
        StringBuilder result = new StringBuilder(digest.length * 2);
        for (byte value : digest) result.append(String.format("%02x", value & 255));
        return result.toString();
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
