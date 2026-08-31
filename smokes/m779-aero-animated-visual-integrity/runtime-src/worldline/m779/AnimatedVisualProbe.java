package worldline.m779;

import aero.modellib.Aero_AnimatedBatcher;
import aero.modellib.Aero_MeshRenderer;
import aero.modellib.render.Aero_FrustumCull;
import aero.modellib.test.WorldlineM779Rehydrator;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import net.minecraft.client.Minecraft;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

/** Captures full RGBA frames, animated work, and render-frame hitch evidence. */
public final class AnimatedVisualProbe {
    private static final long HITCH_NS = 50_000_000L;
    private static final String[][] HASHES = new String[2][AnimatedVisualScene.CHECKPOINTS];
    private static final long[][] ANIMATED = new long[2][AnimatedVisualScene.CHECKPOINTS];
    private static final long[][] AT_REST = new long[2][AnimatedVisualScene.CHECKPOINTS];
    private static final long[][] LIST_CALLS = new long[2][AnimatedVisualScene.CHECKPOINTS];
    private static final long[][] VIEW_CULLS = new long[2][AnimatedVisualScene.CHECKPOINTS];
    private static final long[][] VISIBILITY = new long[2][AnimatedVisualScene.CHECKPOINTS];
    private static final boolean[][] CAPTURED = new boolean[2][AnimatedVisualScene.CHECKPOINTS];
    private static final long[] RENDER_NS = new long[2];
    private static final long[] MAX_RENDER_NS = new long[2];
    private static final int[] HITCHES = new int[2];
    private static final int[] FRAMES = new int[2];
    private static long started;
    private static int activeArm = -1, captures, width, height;

    private AnimatedVisualProbe() {}

    public static void beginArm(boolean culling) {
        activeArm = culling ? 1 : 0;
        started = 0L;
    }

    public static void beginFrame() {
        started = AnimatedVisualState.retaining() ? System.nanoTime() : 0L;
    }

    public static void sample(Minecraft game) {
        if (!AnimatedVisualState.retaining() || activeArm < 0 || started == 0L) return;
        long duration = System.nanoTime() - started;
        RENDER_NS[activeArm] += duration;
        MAX_RENDER_NS[activeArm] = Math.max(MAX_RENDER_NS[activeArm], duration);
        FRAMES[activeArm]++;
        if (duration >= HITCH_NS) HITCHES[activeArm]++;
        int checkpoint = AnimatedVisualState.checkpoint();
        ANIMATED[activeArm][checkpoint] += Aero_AnimatedBatcher.flushedInstancesThisFrame();
        AT_REST[activeArm][checkpoint] += Aero_MeshRenderer.atRestRendersThisFrame();
        LIST_CALLS[activeArm][checkpoint] += Aero_MeshRenderer.atRestListCallsThisFrame();
        VIEW_CULLS[activeArm][checkpoint] += Aero_FrustumCull.beViewCulledThisFrame();
        if (AnimatedVisualState.captureFrame() && !CAPTURED[activeArm][checkpoint]) {
            VISIBILITY[activeArm][checkpoint] =
                WorldlineM779Rehydrator.visibilitySignature(game.world, game.player);
            capture(game, activeArm, checkpoint);
        }
    }

    public static int captures() { return captures; }

    public static void write(File metrics, String name, int[] counts) throws Exception {
        int arm = name.equals("cull-on") ? 1 : 0;
        writeArm(artifact(metrics, name), name, arm, counts);
    }

    private static void writeArm(File metrics, String name, int arm,
            int[] counts) throws Exception {
        try (PrintWriter out = new PrintWriter(new FileWriter(metrics))) {
            out.println("arm=" + name);
            out.println("machines=" + (counts[0] + counts[1] + counts[2]));
            out.println("animated.mega=" + counts[0]);
            out.println("morph.crystal=" + counts[1]);
            out.println("turret.ik=" + counts[2]);
            out.println("frames=" + FRAMES[arm]);
            out.println("captures=" + AnimatedVisualScene.CHECKPOINTS);
            out.println("width=" + width);
            out.println("height=" + height);
            out.println("render.ns=" + RENDER_NS[arm]);
            out.println("max.render.ns=" + MAX_RENDER_NS[arm]);
            out.println("hitches.50ms=" + HITCHES[arm]);
            for (int i = 0; i < AnimatedVisualScene.CHECKPOINTS; i++) {
                out.println("checkpoint." + i + ".sha256=" + HASHES[arm][i]);
                out.println("checkpoint." + i + ".animated=" + ANIMATED[arm][i]);
                out.println("checkpoint." + i + ".atrest=" + AT_REST[arm][i]);
                out.println("checkpoint." + i + ".listcalls=" + LIST_CALLS[arm][i]);
                out.println("checkpoint." + i + ".viewculls=" + VIEW_CULLS[arm][i]);
                out.println("checkpoint." + i + ".visibility=" + VISIBILITY[arm][i]);
            }
        }
    }

    private static void capture(Minecraft game, int arm, int checkpoint) {
        try {
            width = game.displayWidth;
            height = game.displayHeight;
            ByteBuffer pixels = BufferUtils.createByteBuffer(width * height * 4);
            GL11.glReadPixels(0, 0, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, pixels);
            byte[] bytes = new byte[pixels.remaining()];
            pixels.get(bytes);
            String armName = arm == 0 ? "cull-off" : "cull-on";
            File directory = new File(System.getProperty("worldline.m779.framesDir"), armName);
            if (!directory.isDirectory() && !directory.mkdirs()) {
                throw new IllegalStateException("cannot create M779 frame directory");
            }
            File output = new File(directory, String.format("checkpoint-%02d.rgba", checkpoint));
            try (FileOutputStream stream = new FileOutputStream(output)) { stream.write(bytes); }
            HASHES[arm][checkpoint] = hash(bytes);
            CAPTURED[arm][checkpoint] = true;
            captures++;
        } catch (Exception error) {
            throw new IllegalStateException("M779 framebuffer capture failed", error);
        }
    }

    private static File artifact(File metrics, String arm) {
        return new File(metrics.getParentFile(), "metrics-" + arm + ".properties");
    }

    private static String hash(byte[] bytes) throws Exception {
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(bytes);
        StringBuilder result = new StringBuilder(digest.length * 2);
        for (byte value : digest) result.append(String.format("%02x", value & 255));
        return result.toString();
    }
}
