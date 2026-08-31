package worldline.m778;

import aero.modellib.Aero_MeshRenderer;
import aero.modellib.render.Aero_FrustumCull;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import net.minecraft.client.Minecraft;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

/** Captures complete RGBA frames and per-checkpoint Aero draw work. */
public final class VisualProbe {
    private static final String[][] HASHES = new String[2][VisualScene.CHECKPOINTS];
    private static final int[][] AT_REST = new int[2][VisualScene.CHECKPOINTS];
    private static final int[][] LIST_CALLS = new int[2][VisualScene.CHECKPOINTS];
    private static final int[][] VIEW_CULLS = new int[2][VisualScene.CHECKPOINTS];
    private static final boolean[][] CAPTURED = new boolean[2][VisualScene.CHECKPOINTS];
    private static int captures, width, height;

    private VisualProbe() {}

    public static void sample(Minecraft game) {
        if (!VisualState.retaining()) return;
        int checkpoint = VisualState.checkpoint();
        if (checkpoint < 0 || checkpoint >= VisualScene.CHECKPOINTS) return;
        int arm = VisualState.cullingEnabled() ? 1 : 0;
        AT_REST[arm][checkpoint] += Aero_MeshRenderer.atRestRendersThisFrame();
        LIST_CALLS[arm][checkpoint] += Aero_MeshRenderer.atRestListCallsThisFrame();
        VIEW_CULLS[arm][checkpoint] += Aero_FrustumCull.beViewCulledThisFrame();
        if (VisualState.captureFrame() && !CAPTURED[arm][checkpoint]) {
            capture(game, arm, checkpoint);
        }
    }

    public static int captures() { return captures; }

    public static void write(File metrics, String order, int machines) throws Exception {
        writeArm(artifact(metrics, "cull-off"), "cull-off", 0, order, machines);
        writeArm(artifact(metrics, "cull-on"), "cull-on", 1, order, machines);
    }

    private static void writeArm(File metrics, String name, int arm,
            String order, int machines) throws Exception {
        try (PrintWriter out = new PrintWriter(new FileWriter(metrics))) {
            out.println("arm=" + name);
            out.println("order=" + order);
            out.println("machines=" + machines);
            out.println("captures=" + VisualScene.CHECKPOINTS);
            out.println("width=" + width);
            out.println("height=" + height);
            for (int i = 0; i < VisualScene.CHECKPOINTS; i++) {
                out.println("checkpoint." + i + ".sha256=" + HASHES[arm][i]);
                out.println("checkpoint." + i + ".atrest=" + AT_REST[arm][i]);
                out.println("checkpoint." + i + ".listcalls=" + LIST_CALLS[arm][i]);
                out.println("checkpoint." + i + ".viewculls=" + VIEW_CULLS[arm][i]);
            }
        }
    }

    private static void capture(Minecraft game, int arm, int checkpoint) {
        try {
            width = game.displayWidth;
            height = game.displayHeight;
            ByteBuffer pixels = BufferUtils.createByteBuffer(width * height * 4);
            GL11.glReadPixels(0, 0, width, height, GL11.GL_RGBA,
                GL11.GL_UNSIGNED_BYTE, pixels);
            byte[] bytes = new byte[pixels.remaining()];
            pixels.get(bytes);
            String armName = arm == 0 ? "cull-off" : "cull-on";
            File directory = new File(System.getProperty("worldline.m778.framesDir"), armName);
            if (!directory.isDirectory() && !directory.mkdirs()) {
                throw new IllegalStateException("cannot create M778 frame directory");
            }
            File output = new File(directory, String.format("checkpoint-%02d.rgba", checkpoint));
            try (FileOutputStream stream = new FileOutputStream(output)) {
                stream.write(bytes);
            }
            HASHES[arm][checkpoint] = hash(bytes);
            CAPTURED[arm][checkpoint] = true;
            captures++;
        } catch (Exception error) {
            throw new IllegalStateException("M778 framebuffer capture failed", error);
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
