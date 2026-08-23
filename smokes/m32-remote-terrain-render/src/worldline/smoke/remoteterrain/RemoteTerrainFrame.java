package worldline.smoke.remoteterrain;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import net.minecraft.src.Tessellator;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.Pbuffer;
import org.lwjgl.opengl.PixelFormat;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.RemoteWorldView;

/** Renders an exact cached 8x8 vertical block slice through mapped Minecraft geometry. */
final class RemoteTerrainFrame {
  static final int BACKGROUND = 0x112233ff;
  private static final int SIZE = 64, CELLS = 8, CELL = SIZE / CELLS;

  private RemoteTerrainFrame() {
  }

  static Frame render(RemoteWorldView world, BlockPosition target) throws Exception {
    int chunkX = Math.floorDiv(target.x(), 16), originX = chunkX * 16;
    require(world.containsChunk(chunkX, Math.floorDiv(target.z(), 16)), "target chunk absent");
    int startX = Math.max(originX, Math.min(target.x() - 4, originX + 8));
    int startY = Math.max(0, Math.min(target.y() - 4, 120));
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
      int nonAir = 0;
      for (int row = 0; row < CELLS; row++)
        for (int column = 0; column < CELLS; column++) {
          BlockState state = world.blockAt(startX + column, startY + row, target.z());
          if (state.legacyId() == 0)
            continue;
          int color = color(state);
          renderer.setColorRGBA(color >>> 24, color >>> 16 & 255, color >>> 8 & 255, 255);
          quad(renderer, column, row);
          nonAir++;
        }
      renderer.draw();
      GL11.glFinish();
      ByteBuffer pixels = BufferUtils.createByteBuffer(SIZE * SIZE * 4);
      GL11.glReadBuffer(GL11.GL_FRONT);
      GL11.glReadPixels(0, 0, SIZE, SIZE, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, pixels);
      require(GL11.glGetError() == GL11.GL_NO_ERROR, "OpenGL reported an error");
      BlockState targetState = world.blockAt(target.x(), target.y(), target.z());
      int targetPixel =
          pixel(pixels, (target.x() - startX) * CELL + 4, (target.y() - startY) * CELL + 4);
      require(targetPixel == (targetState.legacyId() == 0 ? BACKGROUND : color(targetState)),
          "target pixel did not represent cached state");
      int coverage = countChanged(pixels);
      require(
          coverage == nonAir * CELL * CELL, "slice coverage mismatch: " + coverage + "/" + nonAir);
      return new Frame(sha256(pixels), targetPixel, coverage, nonAir);
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
  private static int color(BlockState state) {
    int id = state.legacyId(), data = state.metadata();
    int red = 48 + (id * 37 + data * 11) % 192;
    int green = 48 + (id * 61 + data * 17) % 192;
    int blue = 48 + (id * 83 + data * 23) % 192;
    return red << 24 | green << 16 | blue << 8 | 255;
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
  private static int pixel(ByteBuffer pixels, int x, int y) {
    int offset = (y * SIZE + x) * 4;
    return (pixels.get(offset) & 255) << 24 | (pixels.get(offset + 1) & 255) << 16
        | (pixels.get(offset + 2) & 255) << 8 | pixels.get(offset + 3) & 255;
  }
  private static int countChanged(ByteBuffer pixels) {
    int result = 0;
    for (int y = 0; y < SIZE; y++)
      for (int x = 0; x < SIZE; x++)
        if (pixel(pixels, x, y) != BACKGROUND)
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
  static final class Frame {
    final String hash;
    final int targetPixel, coverage, nonAir;
    Frame(String hash, int targetPixel, int coverage, int nonAir) {
      this.hash = hash;
      this.targetPixel = targetPixel;
      this.coverage = coverage;
      this.nonAir = nonAir;
    }
  }
}
