package worldline.smoke.m10;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.Pbuffer;
import org.lwjgl.opengl.PixelFormat;

/** Draws a fixed quad through a real Minecraft renderer into an offscreen OpenGL buffer. */
public final class NativeRenderSmoke {
    private static final int WIDTH = 64;
    private static final int HEIGHT = 64;

    private NativeRenderSmoke() {}

    public static void main(String[] arguments) throws Exception {
        if (arguments.length != 7) throw new IllegalArgumentException("expected renderer mapping");
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
            Class<?> rendererType = Class.forName(arguments[1]);
            Object renderer = rendererType.getField(arguments[2]).get(null);
            invoke(rendererType, renderer, arguments[3], new Class<?>[0]);
            invoke(rendererType, renderer, arguments[4],
                    new Class<?>[] {int.class, int.class, int.class, int.class},
                    204, 68, 102, 255);
            Method vertex = rendererType.getMethod(arguments[5],
                    double.class, double.class, double.class);
            vertex.invoke(renderer, 16.0, 12.0, 0.0);
            vertex.invoke(renderer, 48.0, 12.0, 0.0);
            vertex.invoke(renderer, 48.0, 52.0, 0.0);
            vertex.invoke(renderer, 16.0, 52.0, 0.0);
            invoke(rendererType, renderer, arguments[6], new Class<?>[0]);
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
            System.out.println("WORLDLINE_RENDER_ROLE=" + arguments[0]);
            System.out.println("WORLDLINE_RENDER_CONTEXT=Pbuffer");
            System.out.println("WORLDLINE_RENDER_DISPLAY_CREATED=" + Display.isCreated());
            System.out.println("WORLDLINE_RENDER_GEOMETRY_PIXELS=" + geometryPixels);
            System.out.println("WORLDLINE_RENDER_SHA256=" + sha256(pixels));
            System.out.println("WORLDLINE_RENDER_PROVENANCE="
                    + rendererType.getProtectionDomain().getCodeSource().getLocation());
            System.out.println("WORLDLINE_RENDER_GPU=" + GL11.glGetString(GL11.GL_VENDOR)
                    + "|" + GL11.glGetString(GL11.GL_RENDERER)
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
