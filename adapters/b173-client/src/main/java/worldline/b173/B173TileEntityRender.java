package worldline.b173;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.Pbuffer;
import org.lwjgl.opengl.PixelFormat;

/** Executes Beta 1.7.3 sign and moving-piston tile renderers in native OpenGL. */
public final class B173TileEntityRender {
    private static final int SIZE = 128;

    private B173TileEntityRender() { }

    public static List<B173TileEntityFrame> render(Path clientJar, String[] names,
            int[] legacyIds, int[] metadata, String[] routes) throws Exception {
        require(names.length == 44 && legacyIds.length == metadata.length
                && legacyIds.length == routes.length && legacyIds.length > 0, "invalid rows");
        Pbuffer buffer = null;
        int terrain = 0, sign = 0;
        try {
            require((Pbuffer.getCapabilities() & Pbuffer.PBUFFER_SUPPORTED) != 0,
                    "OpenGL Pbuffer is unsupported");
            buffer = new Pbuffer(SIZE, SIZE,
                    new PixelFormat().withAlphaBits(8).withDepthBits(24), null);
            buffer.makeCurrent();
            require(buffer.isCurrent() && !Display.isCreated(), "offscreen context is absent");
            configure();
            terrain = B173TerrainTexture.load(clientJar);
            sign = B173TerrainTexture.load(clientJar, "item/sign.png", 64, 32);
            B173TileEntityRuntime runtime = new B173TileEntityRuntime(names, terrain, sign);
            List<B173TileEntityFrame> frames = new ArrayList<>();
            for (int index = 0; index < legacyIds.length; index++) {
                reset(terrain);
                Object renderer = runtime.renderer(legacyIds[index], metadata[index], routes[index]);
                Object entity = runtime.entity(legacyIds[index], metadata[index], routes[index],
                        renderer);
                String provenance = runtime.invoke(renderer, entity, routes[index]);
                GL11.glFinish();
                ByteBuffer pixels = pixels();
                int geometry = geometry(pixels);
                require(geometry > 0, "tile renderer emitted no geometry: " + legacyIds[index]);
                frames.add(new B173TileEntityFrame(legacyIds[index], metadata[index],
                        routes[index], geometry, sha256(pixels), provenance));
            }
            return frames;
        } finally {
            deleteTexture(terrain); deleteTexture(sign);
            if (buffer != null) buffer.destroy();
        }
    }

    private static void configure() {
        GL11.glViewport(0, 0, SIZE, SIZE);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_DITHER);
        GL11.glClearColor(17 / 255.0f, 34 / 255.0f, 51 / 255.0f, 1.0f);
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GL11.glOrtho(-1.4, 1.4, -1.4, 1.4, -10.0, 10.0);
    }

    private static void reset(int texture) {
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glDepthMask(true);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();
        GL11.glRotatef(30.0f, 1.0f, 0.0f, 0.0f);
        GL11.glRotatef(45.0f, 0.0f, 1.0f, 0.0f);
        GL11.glTranslatef(-0.5f, -0.5f, -0.5f);
    }

    private static ByteBuffer pixels() {
        ByteBuffer pixels = BufferUtils.createByteBuffer(SIZE * SIZE * 4);
        GL11.glReadBuffer(GL11.GL_FRONT);
        GL11.glReadPixels(0, 0, SIZE, SIZE, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, pixels);
        require(GL11.glGetError() == GL11.GL_NO_ERROR, "OpenGL reported an error");
        return pixels;
    }

    private static int geometry(ByteBuffer pixels) {
        int count = 0;
        for (int index = 0; index < SIZE * SIZE; index++) {
            int offset = index * 4;
            if ((pixels.get(offset) & 255) != 17 || (pixels.get(offset + 1) & 255) != 34
                    || (pixels.get(offset + 2) & 255) != 51) count++;
        }
        return count;
    }

    private static String sha256(ByteBuffer pixels) throws Exception {
        ByteBuffer copy = pixels.duplicate(); copy.clear();
        MessageDigest digest = MessageDigest.getInstance("SHA-256"); digest.update(copy);
        StringBuilder value = new StringBuilder();
        for (byte item : digest.digest()) value.append(String.format("%02x", item & 255));
        return value.toString();
    }

    private static void deleteTexture(int texture) {
        if (texture == 0) return;
        IntBuffer textures = BufferUtils.createIntBuffer(1); textures.put(0, texture);
        GL11.glDeleteTextures(textures);
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
