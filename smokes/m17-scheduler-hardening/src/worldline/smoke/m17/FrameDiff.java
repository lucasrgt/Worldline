package worldline.smoke.m17;

import java.awt.image.BufferedImage;
import java.nio.file.Path;
import javax.imageio.ImageIO;

/** Complete pixel comparison for one M17 framebuffer checkpoint. */
final class FrameDiff {
  private FrameDiff() {
  }

  static Result compare(Path left, Path right) throws Exception {
    BufferedImage first = ImageIO.read(left.toFile()), second = ImageIO.read(right.toFile());
    if (first == null || second == null || first.getWidth() != second.getWidth()
        || first.getHeight() != second.getHeight())
      throw new IllegalArgumentException("incompatible framebuffer images");
    long changed = 0;
    int maximum = 0;
    for (int y = 0; y < first.getHeight(); y++)
      for (int x = 0; x < first.getWidth(); x++) {
        int a = first.getRGB(x, y), b = second.getRGB(x, y);
        boolean different = false;
        for (int shift : new int[] {0, 8, 16, 24}) {
          int delta = Math.abs((a >> shift & 255) - (b >> shift & 255));
          maximum = Math.max(maximum, delta);
          different |= delta != 0;
        }
        if (different)
          changed++;
      }
    return new Result(changed, maximum);
  }

  static final class Result {
    final long changedPixels;
    final int maxChannelDelta;
    Result(long changedPixels, int maxChannelDelta) {
      this.changedPixels = changedPixels;
      this.maxChannelDelta = maxChannelDelta;
    }
  }
}
