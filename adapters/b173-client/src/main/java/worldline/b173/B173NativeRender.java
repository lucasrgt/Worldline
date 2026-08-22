package worldline.b173;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.Pbuffer;
import org.lwjgl.opengl.PixelFormat;

/** Runs a fixed Beta 1.7.3 Tessellator probe in a real offscreen OpenGL context. */
public final class B173NativeRender {
    private static final int WIDTH = 64;
    private static final int HEIGHT = 64;

    private B173NativeRender() {}

    public static B173NativeFrame render(String role, String rendererClass, String instance,
            String start, String color, String vertex, String draw) throws Exception {
        Pbuffer buffer = null;
        try {
            require((Pbuffer.getCapabilities() & Pbuffer.PBUFFER_SUPPORTED) != 0,
                    "OpenGL Pbuffer is unsupported");
            buffer = new Pbuffer(WIDTH, HEIGHT,
                    new PixelFormat().withAlphaBits(8).withDepthBits(24), null);
            buffer.makeCurrent();
            require(buffer.isCurrent(), "Pbuffer context is not current");
            require(!Display.isCreated(), "onscreen Display was created");
            configureOpenGl();
            Class<?> rendererType = Class.forName(rendererClass);
            Object renderer = rendererType.getField(instance).get(null);
            int colorChanges = 0, vertices = 0, drawCalls = 0;
            invoke(rendererType, renderer, start, new Class<?>[0]);
            invoke(rendererType, renderer, color,
                    new Class<?>[] {int.class, int.class, int.class, int.class},
                    204, 68, 102, 255);
            colorChanges++;
            Method vertexMethod = rendererType.getMethod(vertex,
                    double.class, double.class, double.class);
            vertexMethod.invoke(renderer, 16.0, 12.0, 0.0); vertices++;
            vertexMethod.invoke(renderer, 48.0, 12.0, 0.0); vertices++;
            vertexMethod.invoke(renderer, 48.0, 52.0, 0.0); vertices++;
            vertexMethod.invoke(renderer, 16.0, 52.0, 0.0); vertices++;
            invoke(rendererType, renderer, draw, new Class<?>[0]);
            drawCalls++;
            GL11.glFinish();
            ByteBuffer pixels = BufferUtils.createByteBuffer(WIDTH * HEIGHT * 4);
            GL11.glReadBuffer(GL11.GL_FRONT);
            GL11.glReadPixels(0, 0, WIDTH, HEIGHT, GL11.GL_RGBA,
                    GL11.GL_UNSIGNED_BYTE, pixels);
            require(GL11.glGetError() == GL11.GL_NO_ERROR, "OpenGL reported an error");
            require(pixel(pixels, 0, 0) == 0x112233ff, "background pixel mismatch");
            require(pixel(pixels, 32, 32) == 0xcc4466ff, "quad pixel mismatch");
            int geometryPixels = count(pixels, 0xcc4466ff);
            require(geometryPixels == 1280, "quad coverage mismatch: " + geometryPixels);
            return new B173NativeFrame(role, "Pbuffer", Display.isCreated(), geometryPixels,
                    "draw.calls=" + drawCalls + ",color.changes=" + colorChanges + ",vertices="
                            + vertices + ",texture.binds=0", sha256(pixels),
                    rendererType.getProtectionDomain().getCodeSource().getLocation().toString(),
                    GL11.glGetString(GL11.GL_VENDOR) + "|" + GL11.glGetString(GL11.GL_RENDERER)
                            + "|" + GL11.glGetString(GL11.GL_VERSION));
        } finally {
            if (buffer != null) buffer.destroy();
        }
    }

    private static void configureOpenGl() {
        GL11.glViewport(0, 0, WIDTH, HEIGHT);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_DITHER);
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GL11.glOrtho(0.0, WIDTH, 0.0, HEIGHT, -1.0, 1.0);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();
        GL11.glClearColor(17.0f / 255.0f, 34.0f / 255.0f, 51.0f / 255.0f, 1.0f);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
    }

    private static void invoke(Class<?> type, Object target, String name,
            Class<?>[] parameters, Object... arguments) throws Exception {
        type.getMethod(name, parameters).invoke(target, arguments);
    }

    private static int pixel(ByteBuffer pixels, int x, int y) {
        int offset = (y * WIDTH + x) * 4;
        return (pixels.get(offset) & 255) << 24 | (pixels.get(offset + 1) & 255) << 16
                | (pixels.get(offset + 2) & 255) << 8 | pixels.get(offset + 3) & 255;
    }

    private static int count(ByteBuffer pixels, int expected) {
        int result = 0;
        for (int y = 0; y < HEIGHT; y++) for (int x = 0; x < WIDTH; x++)
            if (pixel(pixels, x, y) == expected) result++;
        return result;
    }

    private static String sha256(ByteBuffer pixels) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        ByteBuffer copy = pixels.duplicate();
        copy.clear();
        digest.update(copy);
        StringBuilder result = new StringBuilder();
        for (byte value : digest.digest()) result.append(String.format("%02x", value & 255));
        return result.toString();
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
