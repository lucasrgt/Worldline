package worldline.m793;
import java.nio.ByteBuffer;
import java.util.Arrays;
import net.minecraft.client.Minecraft;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
/** Observed terrain readiness; model submission stays canceled throughout. */
public final class ProductionTerrain {
    private static int view, frames, stable, dirty;
    private static byte[] previous;
    private static ByteBuffer pixels;
    private ProductionTerrain() {}
    public static void dirty(int value) { dirty = value; }
    static boolean ready() { return view == 5; }
    static void place(Minecraft game) {
        if (view >= 5) return;
        double x = view == 0 ? 15.5D : view == 1 ? 17.5D : view == 2 ? 48.5D : 16.5D;
        double y = view == 3 ? 90D : 76D;
        float yaw = view < 3 ? 90F : view == 3 ? 180F : 0F;
        game.player.velocityX = game.player.velocityY = game.player.velocityZ = 0D;
        game.player.setPositionAndAngles(x, y, 16.5D, yaw, view == 3 ? 20F : -20F);
    }
    public static void sample(Minecraft game) {
        if (!ProductionState.retaining() || ProductionState.acquiring() || ready()) return;
        if (++frames > 20000) throw new IllegalStateException("M793 terrain did not settle");
        if (frames % 15 != 0) return;
        int length = game.displayWidth * game.displayHeight * 4;
        if (pixels == null) pixels = BufferUtils.createByteBuffer(length);
        pixels.clear();
        GL11.glReadPixels(0, 0, game.displayWidth, game.displayHeight, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, pixels);
        if (GL11.glGetError() != 0) throw new IllegalStateException("terrain readback failed");
        byte[] current = new byte[length]; pixels.get(current);
        boolean valid = current[3] == (byte) 255;
        stable = dirty == 0 && valid && Arrays.equals(previous, current) ? stable + 1 : 0;
        previous = current;
        if (stable < 8) return;
        System.out.println("[M793 terrain] view=" + view + " frames=" + frames + " dirty=" + dirty);
        view++; frames = stable = 0; previous = null;
    }
}
