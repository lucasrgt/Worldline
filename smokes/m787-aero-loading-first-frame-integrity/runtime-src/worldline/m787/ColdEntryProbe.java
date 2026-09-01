package worldline.m787;

import aero.modellib.Aero_BECellRenderer;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import net.minecraft.client.Minecraft;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

/** Captures loading, page convergence, complete submission, and RGBA evidence. */
public final class ColdEntryProbe {
    private static final int[] SUBMITTED = new int[ColdEntryScene.FRAMES];
    private static final int[] REBUILDS = new int[ColdEntryScene.FRAMES];
    private static final int[] DIRECT = new int[ColdEntryScene.FRAMES];
    private static final int[] CACHED = new int[ColdEntryScene.FRAMES];
    private static final int[] PAGE_CALLS = new int[ColdEntryScene.FRAMES];
    private static final String[] PAGE_HASHES = new String[ColdEntryScene.CAPTURE_FRAMES.length];
    private static final String[] DIRECT_HASHES = new String[ColdEntryScene.CAPTURE_FRAMES.length];
    private static boolean controlledFlush;
    private static int pageCaptures, directCaptures, width, height;

    private ColdEntryProbe() {}

    public static void beginScene(Minecraft game) {
        if (!ColdEntryState.fixtureActive()) return;
        int viewportWidth = game.displayWidth;
        int viewportHeight = game.displayHeight;
        GL11.glViewport(0, 0, viewportWidth, viewportHeight);
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GLU.gluPerspective(70.0F, (float) viewportWidth / viewportHeight, 0.05F, 256.0F);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();
        GL11.glRotatef(game.player.pitch, 1.0F, 0.0F, 0.0F);
        GL11.glRotatef(game.player.yaw + 180.0F, 0.0F, 1.0F, 0.0F);
        GL11.glColorMask(true, true, true, true);
        GL11.glDepthMask(true);
        GL11.glClearDepth(1.0D);
        GL11.glClearColor(0.06F, 0.08F, 0.12F, 1.0F);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
    }

    public static void flush(double cameraX, double cameraY, double cameraZ) {
        controlledFlush = true;
        try { Aero_BECellRenderer.flush(cameraX, cameraY, cameraZ); }
        finally { controlledFlush = false; }
    }

    public static boolean controlledFlush() { return controlledFlush; }

    public static void finishPageFrame(Minecraft game, int frame, int submitted) {
        SUBMITTED[frame] = submitted;
        REBUILDS[frame] = Aero_BECellRenderer.pageRebuildsThisFrame();
        DIRECT[frame] = Aero_BECellRenderer.directFallbacksThisFrame();
        CACHED[frame] = Aero_BECellRenderer.cachedPageCount();
        PAGE_CALLS[frame] = Aero_BECellRenderer.pageCallsThisFrame();
        int capture = ColdEntryScene.captureIndex(frame);
        if (capture >= 0) {
            capture(game, capture, "pages", PAGE_HASHES);
            pageCaptures++;
        }
    }

    public static void captureDirect(Minecraft game, int frame) {
        int capture = ColdEntryScene.captureIndex(frame);
        if (capture < 0) return;
        capture(game, capture, "direct", DIRECT_HASHES);
        directCaptures++;
    }

    public static int captures() { return Math.min(pageCaptures, directCaptures); }

    public static void write(File metrics, String arm, int machines) throws Exception {
        require(pageCaptures == PAGE_HASHES.length && directCaptures == DIRECT_HASHES.length,
            "M787 incomplete paired framebuffer set: " + pageCaptures + "/" + directCaptures);
        try (PrintWriter out = new PrintWriter(new FileWriter(metrics))) {
            out.println("arm=" + arm);
            out.println("pages.enabled=" + Aero_BECellRenderer.ENABLED);
            out.println("pages.flattened=" + Aero_BECellRenderer.flattenedPagesEnabled());
            out.println("machines=" + machines);
            out.println("frames=" + ColdEntryScene.FRAMES);
            out.println("captures=" + PAGE_HASHES.length);
            out.println("width=" + width);
            out.println("height=" + height);
            out.println("loading.starts=" + ColdEntryLoadTrace.starts());
            out.println("loading.building.stages=" + ColdEntryLoadTrace.building());
            out.println("loading.simulating.stages=" + ColdEntryLoadTrace.simulating());
            out.println("loading.renderworld.calls=" + ColdEntryLoadTrace.renderWorldCalls());
            out.println("loading.sequence=" + ColdEntryLoadTrace.sequence());
            for (int frame = 0; frame < ColdEntryScene.FRAMES; frame++) {
                out.println("frame." + frame + ".submitted=" + SUBMITTED[frame]);
                out.println("frame." + frame + ".rebuilds=" + REBUILDS[frame]);
                out.println("frame." + frame + ".direct=" + DIRECT[frame]);
                out.println("frame." + frame + ".cached=" + CACHED[frame]);
                out.println("frame." + frame + ".pageCalls=" + PAGE_CALLS[frame]);
            }
            for (int capture = 0; capture < PAGE_HASHES.length; capture++) {
                out.println("capture." + capture + ".pages.sha256=" + PAGE_HASHES[capture]);
                out.println("capture." + capture + ".direct.sha256=" + DIRECT_HASHES[capture]);
            }
        }
    }

    private static void capture(Minecraft game, int capture, String mode, String[] hashes) {
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
            require(nonblank(bytes), "M787 blank framebuffer at capture " + capture);
            File directory = new File(System.getProperty("worldline.m787.framesDir"));
            require(directory.isDirectory() || directory.mkdirs(), "cannot create M787 frame directory");
            File output = new File(directory, String.format("%s-%02d.rgba", mode, capture));
            try (FileOutputStream stream = new FileOutputStream(output)) { stream.write(bytes); }
            hashes[capture] = hash(bytes);
        } catch (Exception error) {
            throw new IllegalStateException("M787 framebuffer capture failed", error);
        }
    }

    private static boolean nonblank(byte[] bytes) {
        for (byte value : bytes) if (value != 0) return true;
        return false;
    }

    private static String hash(byte[] bytes) throws Exception {
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(bytes);
        StringBuilder result = new StringBuilder(digest.length * 2);
        for (byte value : digest) result.append(String.format("%02x", value & 255));
        return result.toString();
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
