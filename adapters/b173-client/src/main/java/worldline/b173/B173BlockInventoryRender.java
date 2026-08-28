package worldline.b173;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
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

/** Executes Beta 1.7.3 RenderBlocks inventory geometry in an offscreen native context. */
public final class B173BlockInventoryRender {
    private static final int WIDTH = 96, HEIGHT = 96;

    private B173BlockInventoryRender() { }

    public static List<B173BlockInventoryFrame> render(Path clientJar,
            String rendererClass, String blockClass, String blocksField,
            String renderMethod, String renderTypeMethod, String render3dMethod,
            int[] legacyIds, int[] metadata) throws Exception {
        require(legacyIds != null && metadata != null && legacyIds.length == metadata.length
                && legacyIds.length > 0, "invalid native render rows");
        Pbuffer buffer = null;
        int texture = 0;
        try {
            require((Pbuffer.getCapabilities() & Pbuffer.PBUFFER_SUPPORTED) != 0,
                    "OpenGL Pbuffer is unsupported");
            buffer = new Pbuffer(WIDTH, HEIGHT,
                    new PixelFormat().withAlphaBits(8).withDepthBits(24), null);
            buffer.makeCurrent();
            require(buffer.isCurrent() && !Display.isCreated(), "offscreen context is absent");
            configure();
            texture = B173TerrainTexture.load(clientJar);
            Class<?> rendererType = Class.forName(rendererClass);
            Class<?> blockType = Class.forName(blockClass);
            Object renderer = rendererType.getConstructor().newInstance();
            Field registry = blockType.getField(blocksField);
            Object blocks = registry.get(null);
            Method render = rendererType.getMethod(renderMethod,
                    blockType, int.class, float.class);
            Method type = blockType.getMethod(renderTypeMethod);
            Method render3d = rendererType.getMethod(render3dMethod, int.class);
            List<B173BlockInventoryFrame> frames = new ArrayList<B173BlockInventoryFrame>();
            for (int index = 0; index < legacyIds.length; index++) {
                Object block = Array.get(blocks, legacyIds[index]);
                require(block != null, "unregistered block: " + legacyIds[index]);
                int observedType = ((Integer) type.invoke(block)).intValue();
                require(((Boolean) render3d.invoke(null, observedType)).booleanValue(),
                        "block is not on the native 3D inventory route: " + legacyIds[index]);
                reset(texture);
                render.invoke(renderer, block, metadata[index], 1.0f);
                GL11.glFinish();
                ByteBuffer pixels = BufferUtils.createByteBuffer(WIDTH * HEIGHT * 4);
                GL11.glReadBuffer(GL11.GL_FRONT);
                GL11.glReadPixels(0, 0, WIDTH, HEIGHT, GL11.GL_RGBA,
                        GL11.GL_UNSIGNED_BYTE, pixels);
                require(GL11.glGetError() == GL11.GL_NO_ERROR, "OpenGL reported an error");
                int geometry = geometry(pixels);
                require(geometry > 0, "renderer emitted no pixels: " + legacyIds[index]);
                frames.add(new B173BlockInventoryFrame(legacyIds[index], metadata[index],
                        observedType, geometry, drawCalls(observedType), sha256(pixels),
                        rendererType.getProtectionDomain().getCodeSource().getLocation().toString()));
            }
            return frames;
        } finally {
            if (texture != 0) {
                IntBuffer textures = BufferUtils.createIntBuffer(1);
                textures.put(0, texture);
                GL11.glDeleteTextures(textures);
            }
            if (buffer != null) buffer.destroy();
        }
    }

    private static void configure() {
        GL11.glViewport(0, 0, WIDTH, HEIGHT);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_DITHER);
        GL11.glClearColor(17.0f / 255.0f, 34.0f / 255.0f, 51.0f / 255.0f, 1.0f);
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GL11.glOrtho(-1.35, 1.35, -1.35, 1.35, -10.0, 10.0);
    }

    private static void reset(int texture) {
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();
        GL11.glRotatef(30.0f, 1.0f, 0.0f, 0.0f);
        GL11.glRotatef(45.0f, 0.0f, 1.0f, 0.0f);
    }

    private static int geometry(ByteBuffer pixels) {
        int count = 0;
        for (int index = 0; index < WIDTH * HEIGHT; index++) {
            int offset = index * 4;
            if ((pixels.get(offset) & 255) != 17 || (pixels.get(offset + 1) & 255) != 34
                    || (pixels.get(offset + 2) & 255) != 51) count++;
        }
        return count;
    }

    private static int drawCalls(int renderType) {
        if (renderType == 10) return 12;
        if (renderType == 11) return 24;
        return 6;
    }

    private static String sha256(ByteBuffer pixels) throws Exception {
        ByteBuffer copy = pixels.duplicate(); copy.clear();
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.update(copy);
        byte[] hash = digest.digest();
        StringBuilder value = new StringBuilder();
        for (byte item : hash) value.append(String.format("%02x", item & 255));
        return value.toString();
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
