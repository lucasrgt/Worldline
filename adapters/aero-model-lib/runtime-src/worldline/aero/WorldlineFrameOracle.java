package worldline.aero;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

/** Freezes one game tick, waits for visible readiness, and hashes the framebuffer. */
public final class WorldlineFrameOracle {
    private static final boolean ENABLED = Boolean.getBoolean("worldline.frameOracle.enabled");
    private static final boolean FIXED_DELTA = Boolean.getBoolean("worldline.frameOracle.fixedDelta");
    private static final int TARGET_TICK = Integer.getInteger("worldline.frameOracle.tick", 20);
    private static final int STABLE_FRAMES = Integer.getInteger("worldline.frameOracle.stableFrames", 5);
    private static final int MAX_FRAMES = Integer.getInteger("worldline.frameOracle.maxFrames", 2500);
    private static boolean frozen, complete;
    private static int tick, frames, stable, lastVisible = -1;
    private static String path = "unknown";
    private static int view = -1;
    private static double x, y, z;
    private static float yaw;

    private WorldlineFrameOracle() {}

    public static boolean freeze(int measuredTick) {
        if (!ENABLED || measuredTick < TARGET_TICK) return false;
        if (!frozen) { frozen = true; tick = measuredTick; }
        return true;
    }

    public static boolean fixedDelta() { return FIXED_DELTA || ENABLED && frozen; }

    public static void pose(String value, int distance, double px, double py, double pz, float angle) {
        path = value; view = distance; x = px; y = py; z = pz; yaw = angle;
    }

    public static void prepare(Minecraft client) {
        if (!ENABLED || !frozen) return;
        client.currentScreen = null;
        client.paused = false;
        client.skipGameRender = false;
        client.options.hideHud = true;
        client.options.bobView = false;
    }

    public static void readiness(int dirty, int visible, int visibleDirty, int visibleReady) {
        if (!frozen || complete) return;
        frames++;
        boolean ready = dirty == 0 && visible > 0 && visibleDirty == 0 && visibleReady == visible;
        stable = ready && visible == lastVisible ? stable + 1 : 0;
        lastVisible = visible;
    }

    public static void capture(Minecraft client) {
        if (!frozen || complete || (stable < STABLE_FRAMES && frames < MAX_FRAMES)) return;
        if (stable < STABLE_FRAMES) {
            complete = true;
            System.out.println("[WorldlineFrameOracle] timeout tick=" + tick + " frames=" + frames);
        } else {
            Snapshot snapshot = framebuffer(client.displayWidth, client.displayHeight);
            if (snapshot.nonBlack == 0 && frames < MAX_FRAMES) return;
            complete = true;
            writeImage(snapshot.bytes, client.displayWidth, client.displayHeight);
            System.out.println("[WorldlineFrameOracle] tick=" + tick + " frames=" + frames
                    + " stable=" + stable + " globalReady=true visibleReady=true width=" + client.displayWidth
                    + " height=" + client.displayHeight + " path=" + path + " view=" + view
                    + " x=" + x + " y=" + y + " z=" + z + " yaw=" + yaw
                    + " sha256=" + snapshot.hash);
        }
        System.out.println("[WorldlineCapture] complete frozenTick=" + tick + " renderFrames=" + frames);
        client.scheduleStop();
    }

    private static Snapshot framebuffer(int width, int height) {
        try {
            ByteBuffer pixels = BufferUtils.createByteBuffer(width * height * 4);
            GL11.glReadPixels(0, 0, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, pixels);
            byte[] bytes = new byte[pixels.remaining()]; pixels.get(bytes);
            int nonBlack = 0;
            for (int index = 0; index < bytes.length; index += 4)
                if (bytes[index] != 0 || bytes[index + 1] != 0 || bytes[index + 2] != 0) nonBlack++;
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(bytes);
            StringBuilder result = new StringBuilder();
            for (byte item : digest) result.append(String.format("%02x", item & 255));
            return new Snapshot(bytes, result.toString(), nonBlack);
        } catch (Exception error) {
            throw new IllegalStateException("could not hash framebuffer", error);
        }
    }

    private static void writeImage(byte[] bytes, int width, int height) {
        String output = System.getProperty("worldline.frameOracle.output", "");
        if (output.isEmpty()) return;
        try {
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            for (int y = 0; y < height; y++) for (int x = 0; x < width; x++) {
                int index = (y * width + x) * 4;
                int rgba = (bytes[index] & 255) << 16 | (bytes[index + 1] & 255) << 8
                        | bytes[index + 2] & 255 | (bytes[index + 3] & 255) << 24;
                image.setRGB(x, height - 1 - y, rgba);
            }
            ImageIO.write(image, "png", new File(output));
        } catch (Exception error) {
            throw new IllegalStateException("could not write framebuffer", error);
        }
    }

    private static final class Snapshot {
        final byte[] bytes; final String hash; final int nonBlack;
        Snapshot(byte[] bytes, String hash, int nonBlack) {
            this.bytes = bytes; this.hash = hash; this.nonBlack = nonBlack;
        }
    }
}
