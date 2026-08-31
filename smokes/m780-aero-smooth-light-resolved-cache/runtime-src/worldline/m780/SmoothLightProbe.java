package worldline.m780;

import aero.modellib.render.Aero_SmoothLightCache;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import net.minecraft.client.Minecraft;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

/** Captures full frames, resolved-light work, cache telemetry, and hitches. */
public final class SmoothLightProbe {
    private static final long HITCH_NS = 50_000_000L;
    private static final String[][] HASHES = new String[2][SmoothLightScene.CHECKPOINTS];
    private static final String[][] LIGHT_HASHES = new String[2][2];
    private static final long[][] RENDERS = new long[2][SmoothLightScene.CHECKPOINTS];
    private static final long[][] SAMPLES = new long[2][SmoothLightScene.CHECKPOINTS];
    private static final long[][] RESOLVED = new long[2][SmoothLightScene.CHECKPOINTS];
    private static final long[][] RESOLVED_VALUES = new long[2][SmoothLightScene.CHECKPOINTS];
    private static final long[][] LIGHT_RESOLVED = new long[2][2];
    private static final long[][] LIGHT_RESOLVED_VALUES = new long[2][2];
    private static final boolean[][] CAPTURED = new boolean[2][SmoothLightScene.CHECKPOINTS];
    private static final long[] RENDER_NS = new long[2];
    private static final long[] MAX_RENDER_NS = new long[2];
    private static final int[] HITCHES = new int[2];
    private static final int[] FRAMES = new int[2];
    private static long started;
    private static int activeArm = -1, captures, width, height;
    private static boolean sceneCleared;

    private SmoothLightProbe() {}

    public static void beginArm(boolean cache) {
        activeArm = cache ? 1 : 0;
        started = 0L;
    }

    public static void beginFrame() {
        sceneCleared = false;
        started = SmoothLightState.retaining() ? System.nanoTime() : 0L;
    }

    /** Removes unrelated terrain after it renders and before the first fixture mesh. */
    public static void beginScene() {
        if (sceneCleared) return;
        GL11.glClearColor(0.06F, 0.08F, 0.12F, 1.0F);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        sceneCleared = true;
    }

    public static void renderCall() {
        if (SmoothLightState.retaining() && activeArm >= 0) {
            RENDERS[activeArm][SmoothLightState.checkpoint()]++;
        }
    }

    public static void lightSample() {
        if (SmoothLightState.retaining() && activeArm >= 0) {
            SAMPLES[activeArm][SmoothLightState.checkpoint()]++;
        }
    }

    /** Hashes the exact brightness values consumed by tess.color at checkpoints. */
    public static void resolved(float[] values) {
        if (activeArm < 0) return;
        int diagnostic = SmoothLightState.lightDiagnostic();
        int slot = diagnostic != 0 ? diagnostic - 1 : SmoothLightState.checkpoint();
        if (diagnostic == 0 && !SmoothLightState.captureFrame()) return;
        long current = diagnostic != 0 ? LIGHT_RESOLVED[activeArm][slot]
            : RESOLVED[activeArm][slot];
        if (current == 0L) current = 0xcbf29ce484222325L;
        for (float value : values) {
            current ^= Float.floatToIntBits(value) & 0xffffffffL;
            current *= 0x100000001b3L;
        }
        if (diagnostic != 0) {
            LIGHT_RESOLVED[activeArm][slot] = current;
            LIGHT_RESOLVED_VALUES[activeArm][slot] += values.length;
        } else {
            RESOLVED[activeArm][slot] = current;
            RESOLVED_VALUES[activeArm][slot] += values.length;
        }
    }

    public static void sample(Minecraft game) {
        int diagnostic = SmoothLightState.lightDiagnostic();
        if (diagnostic != 0 && activeArm >= 0) {
            capture(game, activeArm, diagnostic - 1, true);
            SmoothLightState.consumedLightDiagnostic();
        }
        if (!SmoothLightState.retaining() || activeArm < 0 || started == 0L) return;
        long duration = System.nanoTime() - started;
        RENDER_NS[activeArm] += duration;
        MAX_RENDER_NS[activeArm] = Math.max(MAX_RENDER_NS[activeArm], duration);
        FRAMES[activeArm]++;
        if (duration >= HITCH_NS) HITCHES[activeArm]++;
        int checkpoint = SmoothLightState.checkpoint();
        if (SmoothLightState.captureFrame() && !CAPTURED[activeArm][checkpoint]) {
            capture(game, activeArm, checkpoint);
        }
    }

    public static int captures() { return captures; }

    public static void write(File metrics, String name, int machines) throws Exception {
        int arm = name.equals("cache-on") ? 1 : 0;
        File output = new File(metrics.getParentFile(), "metrics-" + name + ".properties");
        try (PrintWriter out = new PrintWriter(new FileWriter(output))) {
            out.println("arm=" + name);
            out.println("machines=" + machines);
            out.println("frames=" + FRAMES[arm]);
            out.println("captures=" + SmoothLightScene.CHECKPOINTS);
            out.println("width=" + width);
            out.println("height=" + height);
            out.println("render.ns=" + RENDER_NS[arm]);
            out.println("max.render.ns=" + MAX_RENDER_NS[arm]);
            out.println("hitches.50ms=" + HITCHES[arm]);
            out.println("cache.entries=" + Aero_SmoothLightCache.entryCount());
            out.println("cache.hits=" + Aero_SmoothLightCache.hitCount());
            out.println("cache.misses=" + Aero_SmoothLightCache.missCount());
            out.println("cache.cold.misses=" + Aero_SmoothLightCache.coldMissCount());
            out.println("cache.stale.misses=" + Aero_SmoothLightCache.staleMissCount());
            out.println("cache.size.misses=" + Aero_SmoothLightCache.sizeMismatchMissCount());
            out.println("cache.evictions=" + Aero_SmoothLightCache.evictionCount());
            out.println("light.before.sha256=" + LIGHT_HASHES[arm][0]);
            out.println("light.after.sha256=" + LIGHT_HASHES[arm][1]);
            out.println("light.before.resolved=" + LIGHT_RESOLVED[arm][0]);
            out.println("light.after.resolved=" + LIGHT_RESOLVED[arm][1]);
            out.println("light.before.values=" + LIGHT_RESOLVED_VALUES[arm][0]);
            out.println("light.after.values=" + LIGHT_RESOLVED_VALUES[arm][1]);
            for (int i = 0; i < SmoothLightScene.CHECKPOINTS; i++) {
                out.println("checkpoint." + i + ".sha256=" + HASHES[arm][i]);
                out.println("checkpoint." + i + ".renders=" + RENDERS[arm][i]);
                out.println("checkpoint." + i + ".samples=" + SAMPLES[arm][i]);
                out.println("checkpoint." + i + ".resolved=" + RESOLVED[arm][i]);
                out.println("checkpoint." + i + ".values=" + RESOLVED_VALUES[arm][i]);
            }
        }
    }

    private static void capture(Minecraft game, int arm, int checkpoint) {
        capture(game, arm, checkpoint, false);
    }

    private static void capture(Minecraft game, int arm, int checkpoint, boolean diagnostic) {
        try {
            width = game.displayWidth;
            height = game.displayHeight;
            ByteBuffer pixels = BufferUtils.createByteBuffer(width * height * 4);
            GL11.glReadPixels(0, 0, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, pixels);
            byte[] bytes = new byte[pixels.remaining()];
            pixels.get(bytes);
            String armName = arm == 0 ? "cache-off" : "cache-on";
            File directory = new File(System.getProperty("worldline.m780.framesDir"), armName);
            if (!directory.isDirectory() && !directory.mkdirs()) {
                throw new IllegalStateException("cannot create M780 frame directory");
            }
            String file = diagnostic ? (checkpoint == 0 ? "light-before.rgba" : "light-after.rgba")
                : String.format("checkpoint-%02d.rgba", checkpoint);
            File output = new File(directory, file);
            try (FileOutputStream stream = new FileOutputStream(output)) { stream.write(bytes); }
            if (diagnostic) LIGHT_HASHES[arm][checkpoint] = hash(bytes);
            else {
                HASHES[arm][checkpoint] = hash(bytes);
                CAPTURED[arm][checkpoint] = true;
                captures++;
            }
        } catch (Exception error) {
            throw new IllegalStateException("M780 framebuffer capture failed", error);
        }
    }

    private static String hash(byte[] bytes) throws Exception {
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(bytes);
        StringBuilder result = new StringBuilder(digest.length * 2);
        for (byte value : digest) result.append(String.format("%02x", value & 255));
        return result.toString();
    }
}
