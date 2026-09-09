package worldline.b173;

import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.Pbuffer;
import org.lwjgl.opengl.PixelFormat;

/** Native body, animation, shadow and fire captures with retained raw RGBA for GPU comparison. */
public final class B173EntityStateRender {
    private B173EntityStateRender() { }
    public static Map<String, String> observe(Path mappings, boolean official, Path output) throws Exception {
        Pbuffer buffer = new Pbuffer(128, 128, new PixelFormat().withAlphaBits(8).withDepthBits(24), null);
        try {
            buffer.makeCurrent();
            B173NativeNames n = new B173NativeNames(mappings, official);
            B173EntityRenderRuntime runtime = new B173EntityRenderRuntime(n);
            Map<String, String> values = new TreeMap<>();
            String device = GL11.glGetString(GL11.GL_VENDOR) + "|" + GL11.glGetString(GL11.GL_RENDERER)
                    + "|" + GL11.glGetString(GL11.GL_VERSION);
            Files.createDirectories(output);
            Files.write(output.resolve("device.txt"), device.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            for (int id : new int[] {90, 54}) for (String state : new String[] {
                    "idle", "hurt", "walk", "bare", "shadow", "fire", "combined"}) {
                Object entity = runtime.entity(id), renderer = runtime.renderer(entity);
                prepare(n, entity, state);
                reset();
                if (!state.equals("bare") && !state.equals("shadow") && !state.equals("fire")) {
                    runtime.draw(renderer, entity);
                }
                if (state.equals("bare") || state.equals("shadow") || state.equals("fire")
                        || state.equals("combined")) n.method("Render", "doRenderShadowAndFire",
                        n.type("Entity"), double.class, double.class, double.class, float.class, float.class)
                        .invoke(renderer, entity, 0D, 0D, 0D, 0F, 0F);
                GL11.glFinish();
                ByteBuffer pixels = BufferUtils.createByteBuffer(128 * 128 * 4);
                GL11.glReadBuffer(GL11.GL_FRONT);
                GL11.glReadPixels(0, 0, 128, 128, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, pixels);
                if (GL11.glGetError() != GL11.GL_NO_ERROR) throw new IllegalStateException("native GL error");
                byte[] rgba = new byte[pixels.capacity()]; pixels.get(rgba); pixels.rewind();
                String key = id + "-" + state;
                Files.write(output.resolve(key + ".rgba"), rgba);
                values.put(key + "-sha256", B173RgbaDigest.hash(pixels));
                int count = 0;
                for (int i = 0; i < rgba.length; i += 4) {
                    if ((rgba[i] & 255) != 17 || (rgba[i + 1] & 255) != 34 || (rgba[i + 2] & 255) != 51) count++;
                }
                values.put(key + "-pixels", Integer.toString(count));
                String origin = entity.getClass().getProtectionDomain().getCodeSource().getLocation().toString();
                if (!origin.contains(official ? "minecraft.jar" : "minecraft/bin")) {
                    throw new IllegalStateException("native render origin differs: " + origin);
                }
            }
            return values;
        } finally { buffer.destroy(); }
    }
    private static void prepare(B173NativeNames n, Object entity, String state) throws Exception {
        n.set(entity, "lastTickPosX", 8D); n.set(entity, "lastTickPosY", 64D); n.set(entity, "lastTickPosZ", 8D);
        n.set(entity, "hurtTime", state.equals("hurt") ? 8 : 0);
        n.set(entity, "fire", state.equals("fire") || state.equals("combined") ? 40 : 0);
        n.set(entity, "field_705_Q", state.equals("walk") ? 1F : 0F);
        n.set(entity, "field_704_R", state.equals("walk") ? 1F : 0F);
        n.set(entity, "field_703_S", state.equals("walk") ? 2.3F : 0F);
        Object manager = n.singleton("RenderManager", "instance");
        n.set(manager, "field_1222_l", 8D); n.set(manager, "field_1221_m", 64D);
        n.set(manager, "field_1220_n", 8D);
        n.set(n.get(manager, "options"), "fancyGraphics", state.equals("shadow") || state.equals("combined"));
        Object world = n.get(entity, "worldObj");
        Object chunk = n.method("World", "getChunkFromChunkCoords", int.class, int.class).invoke(world, 0, 0);
        byte[] blocks = (byte[]) n.get(chunk, "blocks");
        for (int x = 0; x < 16; x++) for (int z = 0; z < 16; z++) blocks[x << 11 | z << 7 | 63] = 1;
        Arrays.fill((byte[]) n.get(n.get(chunk, "skylightMap"), "data"), (byte) 255);
    }
    private static void reset() {
        GL11.glViewport(0, 0, 128, 128); GL11.glDisable(GL11.GL_DITHER);
        GL11.glDisable(GL11.GL_BLEND); GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_TEXTURE_2D); GL11.glEnable(GL11.GL_DEPTH_TEST); GL11.glDepthMask(true);
        GL11.glClearColor(17 / 255F, 34 / 255F, 51 / 255F, 1F);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        GL11.glColor4f(1F, 1F, 1F, 1F);
        GL11.glMatrixMode(GL11.GL_PROJECTION); GL11.glLoadIdentity();
        GL11.glOrtho(-1.8D, 1.8D, -1.8D, 1.8D, -30D, 30D);
        GL11.glMatrixMode(GL11.GL_MODELVIEW); GL11.glLoadIdentity();
        GL11.glRotatef(30F, 1F, 0F, 0F); GL11.glRotatef(35F, 0F, 1F, 0F);
        GL11.glTranslatef(0F, -0.6F, 0F);
    }
}
