package worldline.m81;

/** Primitive cross-side acknowledgement state; contains no client/Aero types. */
public final class WorldlineWaveState {
  private static boolean ack;
  private static int x, y, z, nonce;
  private WorldlineWaveState() {
  }
  public static synchronized void ack(int[] v) {
    if (ack || v == null || v.length != 4)
      throw new IllegalStateException("invalid M81 ack");
    x = v[0];
    y = v[1];
    z = v[2];
    nonce = v[3];
    ack = true;
  }
  public static synchronized boolean matches(int ex, int ey, int ez, int en) {
    return ack && x == ex && y == ey && z == ez && nonce == en;
  }
}
