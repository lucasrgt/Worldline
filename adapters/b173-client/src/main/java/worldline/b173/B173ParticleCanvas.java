package worldline.b173;

import java.nio.ByteBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.Pbuffer;
import org.lwjgl.opengl.PixelFormat;

/** Native offscreen particle scene, retaining empty frames to prove removal. */
final class B173ParticleCanvas implements AutoCloseable {
    private final Pbuffer buffer;
    B173ParticleCanvas() throws Exception {
        if ((Pbuffer.getCapabilities() & Pbuffer.PBUFFER_SUPPORTED) == 0)
            throw new IllegalStateException("Pbuffer unsupported");
        buffer = new Pbuffer(128, 128, new PixelFormat().withAlphaBits(8).withDepthBits(24), null);
        buffer.makeCurrent();
        GL11.glViewport(0, 0, 128, 128);
        GL11.glDisable(GL11.GL_DITHER); GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_TEXTURE_2D); GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glClearColor(17 / 255.0F, 34 / 255.0F, 51 / 255.0F, 1.0F);
        GL11.glMatrixMode(GL11.GL_PROJECTION); GL11.glLoadIdentity();
        GL11.glOrtho(-0.6, 0.6, -0.6, 0.6, -10.0, 10.0);
        GL11.glMatrixMode(GL11.GL_MODELVIEW); GL11.glLoadIdentity();
    }
    void clear() {
        GL11.glDepthMask(true);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        GL11.glColor4f(1, 1, 1, 1);
    }
    ByteBuffer pixels() {
        GL11.glFinish();
        ByteBuffer pixels = BufferUtils.createByteBuffer(128 * 128 * 4);
        GL11.glReadBuffer(GL11.GL_FRONT);
        GL11.glReadPixels(0, 0, 128, 128, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, pixels);
        if (GL11.glGetError() != GL11.GL_NO_ERROR) throw new IllegalStateException("particle OpenGL error");
        return pixels;
    }
    int geometry(ByteBuffer pixels) {
        int count = 0;
        for (int offset = 0; offset < pixels.capacity(); offset += 4)
            if ((pixels.get(offset) & 255) != 17 || (pixels.get(offset + 1) & 255) != 34
                    || (pixels.get(offset + 2) & 255) != 51) count++;
        return count;
    }
    @Override public void close() { buffer.destroy(); }
}
