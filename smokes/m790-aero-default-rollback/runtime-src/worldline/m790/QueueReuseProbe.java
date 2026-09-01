package worldline.m790;

import aero.modellib.Aero_BECellRenderer;
import aero.modellib.Aero_DisplayListBudget;
import aero.modellib.Aero_Prewarm;
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
public final class QueueReuseProbe {
    private static final List<Long> WALLS = new ArrayList<Long>(2048);
    private static final List<Long> ALLOCS = new ArrayList<Long>(2048);
    private static final String[] HASHES = new String[QueueReuseScene.CHECKPOINTS];
    private static final boolean[] CAPTURED = new boolean[QueueReuseScene.CHECKPOINTS];
    private static final com.sun.management.ThreadMXBean ALLOC = allocationBean();
    private static long started, allocationStarted, allocatedBytes, heapPeak;
    private static long explicitRenderCalls, explicitRenderNanos, submittedMachines;
    private static int captures, blankCaptures, width, height;
    private static String arm;
    private static boolean sceneCleared, controlledFlush;

    private QueueReuseProbe() {}

    public static void beginArm(String name) {
        arm = name;
        explicitRenderCalls = explicitRenderNanos = submittedMachines = 0L;
        QueueReusePageMetrics.beginArm();
    }

    public static void beginFrame() {
        sceneCleared = false;
        if (!QueueReuseState.retaining()) {
            started = allocationStarted = 0L;
            return;
        }
        started = System.nanoTime();
        allocationStarted = allocated();
    }

    /** Removes unrelated terrain after it renders and before the first fixture mesh. */
    public static void beginScene() {
        if (sceneCleared || !QueueReuseState.fixtureActive()) return;
        GL11.glClearColor(0.06F, 0.08F, 0.12F, 1.0F);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        sceneCleared = true;
    }

    public static void sample(Minecraft game) {
        if (!QueueReuseState.retaining() || started == 0L) return;
        long wall = System.nanoTime() - started;
        long allocation = allocated();
        long delta = allocation >= allocationStarted ? allocation - allocationStarted : 0L;
        WALLS.add(Long.valueOf(wall));
        ALLOCS.add(Long.valueOf(delta));
        allocatedBytes += delta;
        Runtime runtime = Runtime.getRuntime();
        heapPeak = Math.max(heapPeak, runtime.totalMemory() - runtime.freeMemory());
        int checkpoint = QueueReuseState.checkpoint();
        if (QueueReuseState.captureFrame() && !CAPTURED[checkpoint]) capture(game, checkpoint);
    }

    public static int frames() { return WALLS.size(); }
    public static int captures() { return captures; }
    public static int blankCaptures() { return blankCaptures; }

    public static long beginRender() { return System.nanoTime(); }

    /** Executes the one controlled production Cell Page flush and closes its render timer. */
    public static void flush(double cameraX, double cameraY, double cameraZ, long startedAt) {
        int compiledBefore = Aero_BECellRenderer.compiledCachedPages();
        controlledFlush = true;
        try {
            Aero_BECellRenderer.flush(cameraX, cameraY, cameraZ);
        } finally {
            controlledFlush = false;
            finishRender(startedAt);
        }
        if (QueueReuseState.retaining()) {
            QueueReusePageMetrics.recordFlush(compiledBefore);
        }
    }

    public static void recordSubmission(int submitted) {
        if (QueueReuseState.retaining()) submittedMachines += submitted;
    }

    public static boolean controlledFlush() { return controlledFlush; }

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
            QueueReusePageMetrics.write(out);
            out.println("render.calls=" + explicitRenderCalls);
            out.println("render.nanos=" + explicitRenderNanos);
            out.println("submitted.machines=" + submittedMachines);
            out.println("prewarm.enabled=" + Aero_Prewarm.ENABLED);
            out.println("prewarm.queued.total=" + Aero_Prewarm.queuedModelsTotal());
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
            int previousReadBuffer = GL11.glGetInteger(GL11.GL_READ_BUFFER);
            try {
                GL11.glReadBuffer(GL11.GL_BACK);
                GL11.glReadPixels(0, 0, width, height,
                    GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, pixels);
            } finally {
                GL11.glReadBuffer(previousReadBuffer);
            }
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
            File directory = new File(System.getProperty("worldline.m790.framesDir"), arm);
            if (!directory.isDirectory() && !directory.mkdirs()) {
                throw new IllegalStateException("cannot create M790 frame directory");
            }
            File output = new File(directory, String.format("checkpoint-%02d.rgba", checkpoint));
            try (FileOutputStream stream = new FileOutputStream(output)) { stream.write(bytes); }
            HASHES[checkpoint] = hash(bytes);
            CAPTURED[checkpoint] = true;
            captures++;
        } catch (Exception error) {
            throw new IllegalStateException("M790 framebuffer capture failed", error);
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

    private static void finishRender(long startedAt) {
        if (!QueueReuseState.retaining()) return;
        explicitRenderNanos += System.nanoTime() - startedAt;
        explicitRenderCalls++;
    }

    private static com.sun.management.ThreadMXBean allocationBean() {
        java.lang.management.ThreadMXBean value = ManagementFactory.getThreadMXBean();
        if (!(value instanceof com.sun.management.ThreadMXBean)) return null;
        com.sun.management.ThreadMXBean bean = (com.sun.management.ThreadMXBean) value;
        if (!bean.isThreadAllocatedMemoryEnabled()) bean.setThreadAllocatedMemoryEnabled(true);
        return bean;
    }
}
