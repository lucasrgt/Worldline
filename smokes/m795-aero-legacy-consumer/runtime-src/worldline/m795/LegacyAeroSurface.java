package worldline.m795;

import java.nio.ByteBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.EXTFramebufferObject;
import org.lwjgl.opengl.GL11;

/** An exact owned render target keeps native GUI/window composition outside the oracle. */
final class LegacyAeroSurface implements AutoCloseable {
  private final int previous, framebuffer, color, depth;
  private final ByteBuffer pixels = BufferUtils.createByteBuffer(128 * 128 * 4);
  LegacyAeroSurface() {
    previous = GL11.glGetInteger(EXTFramebufferObject.GL_FRAMEBUFFER_BINDING_EXT);
    GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
    GL11.glMatrixMode(GL11.GL_PROJECTION);
    GL11.glPushMatrix();
    GL11.glLoadIdentity();
    GL11.glOrtho(-1, 1, -1, 1, -10, 10);
    GL11.glMatrixMode(GL11.GL_MODELVIEW);
    GL11.glPushMatrix();
    GL11.glLoadIdentity();
    framebuffer = EXTFramebufferObject.glGenFramebuffersEXT();
    EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, framebuffer);
    color = attach(GL11.GL_RGBA8, EXTFramebufferObject.GL_COLOR_ATTACHMENT0_EXT);
    depth = attach(33190, EXTFramebufferObject.GL_DEPTH_ATTACHMENT_EXT);
    require(EXTFramebufferObject.glCheckFramebufferStatusEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT)
            == EXTFramebufferObject.GL_FRAMEBUFFER_COMPLETE_EXT,
        "incomplete native consumer framebuffer");
    GL11.glDrawBuffer(EXTFramebufferObject.GL_COLOR_ATTACHMENT0_EXT);
    GL11.glReadBuffer(EXTFramebufferObject.GL_COLOR_ATTACHMENT0_EXT);
    GL11.glViewport(0, 0, 128, 128);
    GL11.glDisable(GL11.GL_TEXTURE_2D);
    GL11.glDisable(GL11.GL_FOG);
    GL11.glDisable(GL11.GL_SCISSOR_TEST);
    GL11.glDisable(GL11.GL_BLEND);
    GL11.glColorMask(true, true, true, true);
  }
  void clear() {
    GL11.glDepthMask(true);
    GL11.glClearColor(0, 0, 0, 1);
    GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
  }
  byte[] capture() {
    pixels.clear();
    GL11.glReadPixels(0, 0, 128, 128, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, pixels);
    require(GL11.glGetError() == GL11.GL_NO_ERROR, "native Aero OpenGL error");
    byte[] result = new byte[pixels.remaining()];
    pixels.get(result);
    int visible = 0;
    for (int i = 0; i < result.length; i += 4) {
      require(result[i + 3] == (byte) 255, "unexpected consumer alpha");
      if (result[i] != 0 || result[i + 1] != 0 || result[i + 2] != 0)
        visible++;
    }
    require(visible > 1000 && visible < 10000, "empty or constant native consumer capture");
    return result;
  }
  public void close() {
    EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, previous);
    EXTFramebufferObject.glDeleteRenderbuffersEXT(color);
    EXTFramebufferObject.glDeleteRenderbuffersEXT(depth);
    EXTFramebufferObject.glDeleteFramebuffersEXT(framebuffer);
    GL11.glMatrixMode(GL11.GL_MODELVIEW);
    GL11.glPopMatrix();
    GL11.glMatrixMode(GL11.GL_PROJECTION);
    GL11.glPopMatrix();
    GL11.glPopAttrib();
  }
  private static int attach(int format, int attachment) {
    int id = EXTFramebufferObject.glGenRenderbuffersEXT();
    EXTFramebufferObject.glBindRenderbufferEXT(EXTFramebufferObject.GL_RENDERBUFFER_EXT, id);
    EXTFramebufferObject.glRenderbufferStorageEXT(EXTFramebufferObject.GL_RENDERBUFFER_EXT, format, 128, 128);
    EXTFramebufferObject.glFramebufferRenderbufferEXT(
        EXTFramebufferObject.GL_FRAMEBUFFER_EXT, attachment, EXTFramebufferObject.GL_RENDERBUFFER_EXT, id);
    return id;
  }
  static void require(boolean value, String message) {
    if (!value)
      throw new IllegalStateException(message);
  }
}
