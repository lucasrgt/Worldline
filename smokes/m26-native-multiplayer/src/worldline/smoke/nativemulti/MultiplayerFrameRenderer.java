package worldline.smoke.nativemulti;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import net.minecraft.src.Tessellator;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.Pbuffer;
import org.lwjgl.opengl.PixelFormat;

/** Renders one connection-selected frame through Minecraft's native renderer. */
final class MultiplayerFrameRenderer {
  private static final int WIDTH = 64, HEIGHT = 64;

  private MultiplayerFrameRenderer() {
  }

  static String render(boolean connected) throws Exception {
    require(connected, "multiplayer session is not connected");
    Pbuffer buffer = null;
    try {
      require((Pbuffer.getCapabilities() & Pbuffer.PBUFFER_SUPPORTED) != 0,
          "OpenGL Pbuffer is unsupported");
      buffer =
          new Pbuffer(WIDTH, HEIGHT, new PixelFormat().withAlphaBits(8).withDepthBits(24), null);
      buffer.makeCurrent();
      require(buffer.isCurrent() && !Display.isCreated(), "render context is not offscreen");
      configure();
      Tessellator renderer = Tessellator.instance;
      renderer.startDrawingQuads();
      renderer.setColorRGBA(204, 68, 102, 255);
      renderer.addVertex(16.0D, 12.0D, 0.0D);
      renderer.addVertex(48.0D, 12.0D, 0.0D);
      renderer.addVertex(48.0D, 52.0D, 0.0D);
      renderer.addVertex(16.0D, 52.0D, 0.0D);
      renderer.draw();
      GL11.glFinish();
      ByteBuffer pixels = BufferUtils.createByteBuffer(WIDTH * HEIGHT * 4);
      GL11.glReadBuffer(GL11.GL_FRONT);
      GL11.glReadPixels(0, 0, WIDTH, HEIGHT, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, pixels);
      require(GL11.glGetError() == GL11.GL_NO_ERROR, "OpenGL reported an error");
      require(pixel(pixels, 0, 0) == 0x112233ff, "background pixel mismatch");
      require(pixel(pixels, 32, 32) == 0xcc4466ff, "connected pixel mismatch");
      int coverage = count(pixels, 0xcc4466ff);
      require(coverage == 1280, "connected quad coverage mismatch: " + coverage);
      System.out.println("WORLDLINE_M26_CONTEXT=Pbuffer");
      System.out.println("WORLDLINE_M26_DISPLAY_CREATED=" + Display.isCreated());
      System.out.println("WORLDLINE_M26_GEOMETRY_PIXELS=" + coverage);
      System.out.println("WORLDLINE_M26_RENDERER="
          + Tessellator.class.getProtectionDomain().getCodeSource().getLocation());
      return sha256(pixels);
    } finally {
      if (buffer != null)
        buffer.destroy();
    }
  }

  private static void configure() {
    GL11.glViewport(0, 0, WIDTH, HEIGHT);
    GL11.glDisable(GL11.GL_TEXTURE_2D);
    GL11.glDisable(GL11.GL_DEPTH_TEST);
    GL11.glDisable(GL11.GL_BLEND);
    GL11.glDisable(GL11.GL_CULL_FACE);
    GL11.glDisable(GL11.GL_DITHER);
    GL11.glMatrixMode(GL11.GL_PROJECTION);
    GL11.glLoadIdentity();
    GL11.glOrtho(0.0D, WIDTH, 0.0D, HEIGHT, -1.0D, 1.0D);
    GL11.glMatrixMode(GL11.GL_MODELVIEW);
    GL11.glLoadIdentity();
    GL11.glClearColor(17.0F / 255.0F, 34.0F / 255.0F, 51.0F / 255.0F, 1.0F);
    GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
  }

  private static int pixel(ByteBuffer pixels, int x, int y) {
    int offset = (y * WIDTH + x) * 4;
    return (pixels.get(offset) & 255) << 24 | (pixels.get(offset + 1) & 255) << 16
        | (pixels.get(offset + 2) & 255) << 8 | pixels.get(offset + 3) & 255;
  }
  private static int count(ByteBuffer pixels, int expected) {
    int result = 0;
    for (int y = 0; y < HEIGHT; y++)
      for (int x = 0; x < WIDTH; x++)
        if (pixel(pixels, x, y) == expected)
          result++;
    return result;
  }
  private static String sha256(ByteBuffer pixels) throws Exception {
    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    ByteBuffer copy = pixels.duplicate();
    copy.clear();
    digest.update(copy);
    StringBuilder result = new StringBuilder();
    for (byte value : digest.digest())
      result.append(String.format("%02x", value & 255));
    return result.toString();
  }
  private static void require(boolean condition, String message) {
    if (!condition)
      throw new IllegalStateException(message);
  }
}
