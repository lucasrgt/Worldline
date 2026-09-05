package worldline.m793;

import aero.modellib.Aero_AnimatedBatcher;
import aero.modellib.Aero_BECellOcclusion;
import aero.modellib.Aero_BECellRenderer;
import aero.modellib.Aero_MeshRenderer;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import com.google.common.hash.Hashing;
import net.minecraft.client.Minecraft;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

/** Captures exact frames plus candidate activation and renderer work. */
public final class ProductionProbe {
    private static final int N = ProductionScene.CHECKPOINTS;
    private static final String[][] HASHES = new String[2][N];
    private static final long[][] WORK = new long[2][N];
    private static final long[][] PAGE_CALLS = new long[2][N];
    private static final long[][] ANIMATED = new long[2][N];
    private static final long[][] CULLED = new long[2][N];
    private static final int[][] SLICES = new int[2][N];
    private static final boolean[][] CAPTURED = new boolean[2][N];
    private static int captures, width, height;

    private ProductionProbe() {}

    public static void sample(Minecraft game) {
        if (!ProductionState.acquiring()) return;
        int checkpoint = ProductionState.checkpoint();
        if (checkpoint < 0 || checkpoint >= N) return;
        int arm = ProductionState.candidateEnabled() ? 1 : 0;
        WORK[arm][checkpoint] += Aero_MeshRenderer.atRestRendersThisFrame();
        PAGE_CALLS[arm][checkpoint] += Aero_BECellRenderer.pageCallsThisFrame();
        ANIMATED[arm][checkpoint] += Aero_AnimatedBatcher.queuedThisFrame();
        CULLED[arm][checkpoint] += Aero_BECellOcclusion.culledThisFrame();
        SLICES[arm][checkpoint] = Math.max(SLICES[arm][checkpoint],
            Aero_BECellOcclusion.stableSliceCount());
        if (ProductionState.captureFrame() && !CAPTURED[arm][checkpoint]) {
            capture(game, arm, checkpoint);
        }
    }

    public static int captures() { return captures; }

    public static void write(File metrics, String order, int machines) throws Exception {
        writeArm(artifact(metrics, "replay-off"), "replay-off", 0, order, machines);
        writeArm(artifact(metrics, "replay-on"), "replay-on", 1, order, machines);
    }

    private static void writeArm(File metrics, String name, int arm,
            String order, int machines) throws Exception {
        try (PrintWriter out = new PrintWriter(new FileWriter(metrics))) {
            out.println("arm=" + name);
            out.println("order=" + order);
            out.println("machines=" + machines);
            out.println("captures=" + N);
            out.println("width=" + width);
            out.println("height=" + height);
            for (int i = 0; i < N; i++) {
                String prefix = "checkpoint." + i + ".";
                out.println(prefix + "sha256=" + HASHES[arm][i]);
                out.println(prefix + "work=" + WORK[arm][i]);
                out.println(prefix + "pagecalls=" + PAGE_CALLS[arm][i]);
                out.println(prefix + "animated=" + ANIMATED[arm][i]);
                out.println(prefix + "culled=" + CULLED[arm][i]);
                out.println(prefix + "slices=" + SLICES[arm][i]);
            }
        }
    }

    private static void capture(Minecraft game, int arm, int checkpoint) {
        try {
            if (game.currentScreen != null || !game.options.hideHud) {
                throw new IllegalStateException("menu or HUD contaminated M793 capture");
            }
            width = game.displayWidth;
            height = game.displayHeight;
            ByteBuffer pixels = BufferUtils.createByteBuffer(width * height * 4);
            int before = GL11.glGetError();
            GL11.glReadPixels(0, 0, width, height, GL11.GL_RGBA,
                GL11.GL_UNSIGNED_BYTE, pixels);
            int after = GL11.glGetError();
            byte[] bytes = new byte[pixels.remaining()];
            pixels.get(bytes);
            System.out.println("[M793 capture] arm=" + arm + " checkpoint=" + checkpoint
                + " gl.before=" + before + " gl.after=" + after
                + " read.buffer=" + GL11.glGetInteger(GL11.GL_READ_BUFFER)
                + " visible=" + org.lwjgl.opengl.Display.isVisible()
                + " first.rgba=" + (bytes[0] & 255) + "," + (bytes[1] & 255)
                + "," + (bytes[2] & 255) + "," + (bytes[3] & 255));
            if (before != 0 || after != 0) throw new IllegalStateException("M793 OpenGL capture error");
            String name = arm == 0 ? "replay-off" : "replay-on";
            File directory = new File(System.getProperty("worldline.m793.framesDir"), name);
            if (!directory.isDirectory() && !directory.mkdirs()) {
                throw new IllegalStateException("cannot create M793 frame directory");
            }
            File output = new File(directory, String.format("checkpoint-%02d.rgba", checkpoint));
            try (FileOutputStream stream = new FileOutputStream(output)) { stream.write(bytes); }
            HASHES[arm][checkpoint] = hash(bytes);
            CAPTURED[arm][checkpoint] = true;
            captures++;
        } catch (Exception error) {
            throw new IllegalStateException("M793 framebuffer capture failed", error);
        }
    }

    private static File artifact(File metrics, String arm) {
        return new File(metrics.getParentFile(), "metrics-" + arm + ".properties");
    }

    private static String hash(byte[] bytes) throws Exception {
        return Hashing.sha256().hashBytes(bytes).toString();
    }
}
