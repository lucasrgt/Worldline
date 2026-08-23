package worldline.m72.probe;

/** Cross-mixin state for the test-only real-client M72 boundary. */
public final class WorldlineContentProbe {
  private static boolean hello, play, rendered;
  private static int frames, x, y, z, raw, nonce;
  private WorldlineContentProbe() {
  }
  public static void hello() {
    hello = true;
    System.out.println("[WorldlineContent] packet1");
  }
  public static void play() {
    if (!hello)
      throw new IllegalStateException("play before login");
    play = true;
    System.out.println("[WorldlineContent] packet13");
  }
  public static void rendered(int px, int py, int pz, int blockId, int serverNonce) {
    if (!rendered) {
      x = px;
      y = py;
      z = pz;
      raw = blockId;
      nonce = serverNonce;
      rendered = true;
      System.out.println(
          "[WorldlineContent] rendered identifier=worldline-m72-content:server_probe x=" + x
          + " y=" + y + " z=" + z + " raw=" + raw + " nonce=" + nonce);
    }
  }
  public static boolean ready() {
    return hello && play && rendered;
  }
  public static int frame() {
    return ++frames;
  }
  public static int frames() {
    return frames;
  }
}
