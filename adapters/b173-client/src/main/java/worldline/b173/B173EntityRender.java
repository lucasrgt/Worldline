package worldline.b173;

import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.Pbuffer;
import org.lwjgl.opengl.PixelFormat;

/** Offscreen native body rendering through the official EntityList and RenderManager. */
public final class B173EntityRender {
    private static final int SIZE = 128;
    private B173EntityRender() { }

    public static List<B173EntityRenderFrame> render(Path mappings, boolean official, int[] ids)
            throws Exception {
        require(ids != null && ids.length > 0, "empty entity render request");
        Pbuffer buffer = null;
        try {
            require((Pbuffer.getCapabilities() & Pbuffer.PBUFFER_SUPPORTED) != 0, "Pbuffer unsupported");
            buffer = new Pbuffer(SIZE, SIZE, new PixelFormat().withAlphaBits(8).withDepthBits(24), null);
            buffer.makeCurrent();
            require(buffer.isCurrent() && !Display.isCreated(), "native offscreen context absent");
            configure();
            B173EntityRenderRuntime runtime = new B173EntityRenderRuntime(new B173NativeNames(mappings, official));
            List<B173EntityRenderFrame> frames = new ArrayList<>();
            for (int id : ids) {
                try {
                    Object entity = runtime.entity(id), renderer = runtime.renderer(entity);
                    reset(runtime.width(entity), runtime.height(entity));
                    runtime.draw(renderer, entity);
                    GL11.glFinish();
                    ByteBuffer pixels = pixels();
                    int geometry = geometry(pixels);
                    require(geometry > 0, "native renderer emitted no geometry");
                    frames.add(new B173EntityRenderFrame(id, geometry, runtime.identity(entity),
                            runtime.identity(renderer), B173RgbaDigest.hash(pixels), origin(entity), origin(renderer)));
                } catch (Exception error) {
                    throw new IllegalStateException("native render entity " + id, error);
                }
            }
            return frames;
        } finally {
            if (buffer != null) buffer.destroy();
        }
    }

    private static void configure() {
        GL11.glViewport(0, 0, SIZE, SIZE);
        GL11.glEnable(GL11.GL_TEXTURE_2D); GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_DITHER);
        GL11.glClearColor(17 / 255.0F, 34 / 255.0F, 51 / 255.0F, 1.0F);
        GL11.glMatrixMode(GL11.GL_PROJECTION); GL11.glLoadIdentity();
        GL11.glOrtho(-1.5, 1.5, -1.5, 1.5, -30.0, 30.0);
    }

    private static void reset(float width, float height) {
        GL11.glDisable(GL11.GL_BLEND); GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_DEPTH_TEST); GL11.glDepthMask(true);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        double extent = 1.5D * Math.max(1.0F, Math.max(width, height)) / 1.6D;
        GL11.glMatrixMode(GL11.GL_PROJECTION); GL11.glLoadIdentity();
        GL11.glOrtho(-extent, extent, -extent, extent, -30.0D, 30.0D);
        GL11.glMatrixMode(GL11.GL_MODELVIEW); GL11.glLoadIdentity();
        GL11.glRotatef(15.0F, 1.0F, 0.0F, 0.0F); GL11.glRotatef(35.0F, 0.0F, 1.0F, 0.0F);
        GL11.glTranslatef(0.0F, -height / 2.0F, 0.0F);
    }

    private static ByteBuffer pixels() {
        ByteBuffer pixels = BufferUtils.createByteBuffer(SIZE * SIZE * 4);
        GL11.glReadBuffer(GL11.GL_FRONT);
        GL11.glReadPixels(0, 0, SIZE, SIZE, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, pixels);
        require(GL11.glGetError() == GL11.GL_NO_ERROR, "native renderer reported an OpenGL error");
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
    private static String origin(Object value) {
        return value.getClass().getProtectionDomain().getCodeSource().getLocation().toString();
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
