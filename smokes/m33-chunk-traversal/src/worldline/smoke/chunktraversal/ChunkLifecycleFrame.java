package worldline.smoke.chunktraversal;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import net.minecraft.src.Tessellator;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.Pbuffer;
import org.lwjgl.opengl.PixelFormat;
import worldline.api.RemoteWorldView;

/** Renders a fixed-grid native map of lifecycle-qualified cached chunks. */
final class ChunkLifecycleFrame {
  static final int BACKGROUND = 0x112233ff, LOADED = 0x55bb88ff;
  private static final int GRID = 12, CELL = 8, SIZE = GRID * CELL;

  private ChunkLifecycleFrame() {
  }

  static Frame render(RemoteWorldView world, int originX, int originZ) throws Exception {
    Pbuffer buffer = null;
    try {
      require((Pbuffer.getCapabilities() & Pbuffer.PBUFFER_SUPPORTED) != 0,
          "OpenGL Pbuffer is unsupported");
      buffer = new Pbuffer(SIZE, SIZE, new PixelFormat().withAlphaBits(8).withDepthBits(24), null);
      buffer.makeCurrent();
      require(buffer.isCurrent() && !Display.isCreated(), "render context is not offscreen");
      configure();
      Tessellator renderer = Tessellator.instance;
      renderer.startDrawingQuads();
      renderer.setColorRGBA(85, 187, 136, 255);
      int loaded = 0;
      for (int row = 0; row < GRID; row++)
        for (int column = 0; column < GRID; column++) {
          if (!world.containsChunk(originX + column, originZ + row))
            continue;
          quad(renderer, column, row);
          loaded++;
        }
      renderer.draw();
      GL11.glFinish();
      ByteBuffer pixels = BufferUtils.createByteBuffer(SIZE * SIZE * 4);
      GL11.glReadBuffer(GL11.GL_FRONT);
      GL11.glReadPixels(0, 0, SIZE, SIZE, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, pixels);
      require(GL11.glGetError() == GL11.GL_NO_ERROR, "OpenGL reported an error");
      byte[] rgba = new byte[pixels.remaining()];
      pixels.get(rgba);
      int coverage = count(rgba, LOADED);
      require(coverage == loaded * CELL * CELL,
          "chunk-map coverage mismatch: " + coverage + "/" + loaded);
      return new Frame(sha256(rgba), rgba, originX, originZ, coverage);
    } finally {
      if (buffer != null)
        buffer.destroy();
    }
  }

  static String provenance() {
    return Tessellator.class.getProtectionDomain().getCodeSource().getLocation().toString();
  }

  private static void quad(Tessellator renderer, int column, int row) {
    double x = column * CELL, y = row * CELL;
    renderer.addVertex(x, y, 0);
    renderer.addVertex(x + CELL, y, 0);
    renderer.addVertex(x + CELL, y + CELL, 0);
    renderer.addVertex(x, y + CELL, 0);
  }
  private static void configure() {
    GL11.glViewport(0, 0, SIZE, SIZE);
    GL11.glDisable(GL11.GL_TEXTURE_2D);
    GL11.glDisable(GL11.GL_DEPTH_TEST);
    GL11.glDisable(GL11.GL_BLEND);
    GL11.glDisable(GL11.GL_CULL_FACE);
    GL11.glDisable(GL11.GL_DITHER);
    GL11.glMatrixMode(GL11.GL_PROJECTION);
    GL11.glLoadIdentity();
    GL11.glOrtho(0, SIZE, 0, SIZE, -1, 1);
    GL11.glMatrixMode(GL11.GL_MODELVIEW);
    GL11.glLoadIdentity();
    GL11.glClearColor(17F / 255F, 34F / 255F, 51F / 255F, 1F);
    GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
  }
  private static int pixel(byte[] rgba, int x, int y) {
    int offset = (y * SIZE + x) * 4;
    return (rgba[offset] & 255) << 24 | (rgba[offset + 1] & 255) << 16
        | (rgba[offset + 2] & 255) << 8 | rgba[offset + 3] & 255;
  }
  private static int count(byte[] rgba, int expected) {
    int result = 0;
    for (int y = 0; y < SIZE; y++)
      for (int x = 0; x < SIZE; x++)
        if (pixel(rgba, x, y) == expected)
          result++;
    return result;
  }
  private static String sha256(byte[] bytes) throws Exception {
    byte[] hash = MessageDigest.getInstance("SHA-256").digest(bytes);
    StringBuilder result = new StringBuilder();
    for (byte value : hash)
      result.append(String.format("%02x", value & 255));
    return result.toString();
  }
  private static void require(boolean condition, String message) {
    if (!condition)
      throw new IllegalStateException(message);
  }
  static final class Frame {
    final String hash;
    final byte[] rgba;
    final int originX, originZ, coverage;
    Frame(String hash, byte[] rgba, int originX, int originZ, int coverage) {
      this.hash = hash;
      this.rgba = rgba;
      this.originX = originX;
      this.originZ = originZ;
      this.coverage = coverage;
    }
    int chunkPixel(int chunkX, int chunkZ) {
      int column = chunkX - originX, row = chunkZ - originZ;
      if (column < 0 || column >= GRID || row < 0 || row >= GRID)
        return BACKGROUND;
      return pixel(rgba, column * CELL + 4, row * CELL + 4);
    }
  }
}
