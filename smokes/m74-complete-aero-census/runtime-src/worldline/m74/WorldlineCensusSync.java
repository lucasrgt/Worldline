package worldline.m74;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.world.World;

/** Allocation-free fixed-capacity client reconciliation and identity map. */
public final class WorldlineCensusSync {
  private static final int[] xs = new int[16], ys = new int[16], zs = new int[16],
                             nonces = new int[16];
  private static final boolean[] applied = new boolean[16];
  private static final int ROOT = Integer.getInteger("worldline.census.nonce", 0);
  private static int received, appliedCount, x, y, z, scenePlaced;
  private static boolean planned, scene, trackedDiagnostic;
  private WorldlineCensusSync() {
  }
  public static synchronized void plan(int[] values) {
    if (values == null || values.length != 3 || values[1] < 1 || values[1] > 124 || planned)
      throw new IllegalStateException("invalid M74 plan");
    x = values[0];
    y = values[1];
    z = values[2];
    planned = true;
  }
  public static synchronized boolean tracked(
      World world, double px, double py, double pz, float yaw, float pitch) {
    float error = (yaw + 90F) % 360F;
    if (error < -180F)
      error += 360F;
    if (error > 180F)
      error -= 360F;
    if (!planned || world == null)
      return false;
    if (Math.abs(px - (x - 1.5D)) > .05D || Math.abs(py - (y + 1.62D)) > .1D
        || Math.abs(pz - (z + 2.5D)) > .05D || Math.abs(error) > .01F || Math.abs(pitch) > .01F) {
      diagnostic("pose actual=" + px + "/" + py + "/" + pz + "/" + yaw + "/" + pitch);
      return false;
    }
    for (int dz = 0; dz < 4; dz++) {
      if (!world.getChunkSource().isChunkLoaded(x >> 4, z + dz >> 4)) {
        diagnostic("chunk dz=" + dz);
        return false;
      }
      for (int dy = 0; dy < 4; dy++)
        if (world.getBlockId(x, y + dy, z + dz) != 0) {
          diagnostic("occupied dy/dz=" + dy + "/" + dz);
          return false;
        }
    }
    return true;
  }
  public static synchronized int[] plan() {
    if (!planned)
      throw new IllegalStateException("M74 plan absent");
    return new int[] {x, y, z};
  }
  public static synchronized void scene(int[] values) {
    int expected = Integer.getInteger("worldline.census.nonce", 0),
        placed = mode().equals("present") ? 16 : 0;
    if (!planned || scene || values == null || values.length != 5 || values[0] != x
        || values[1] != y || values[2] != z || values[3] != expected || values[4] != placed)
      throw new IllegalStateException("invalid M74 scene");
    scenePlaced = placed;
    scene = true;
  }
  public static synchronized void receive(World world, int cx, int cy, int cz, int nonce) {
    if (!planned || nonce <= 0)
      throw new IllegalStateException("invalid M74 cell");
    for (int i = 0; i < received; i++) {
      if (nonces[i] == nonce && (xs[i] != cx || ys[i] != cy || zs[i] != cz))
        throw new IllegalStateException("duplicate M74 nonce");
      if (xs[i] == cx && ys[i] == cy && zs[i] == cz) {
        if (nonces[i] != nonce)
          throw new IllegalStateException("conflicting M74 cell");
        return;
      }
    }
    if (received == 16 || index(cx, cy, cz, nonce) < 0)
      throw new IllegalStateException("excess M74 cell");
    xs[received] = cx;
    ys[received] = cy;
    zs[received] = cz;
    nonces[received] = nonce;
    received++;
    apply(world);
  }
  public static synchronized void apply(World world) {
    if (world == null)
      return;
    for (int i = 0; i < received; i++)
      if (!applied[i]) {
        if (world.getBlockId(xs[i], ys[i], zs[i]) != WorldlineCensusMod.block.id)
          continue;
        BlockEntity raw = world.getBlockEntity(xs[i], ys[i], zs[i]);
        if (raw != null && !(raw instanceof WorldlineCensusBlockEntity))
          throw new IllegalStateException("M74 BE type drift");
        WorldlineCensusBlockEntity be =
            raw == null ? new WorldlineCensusBlockEntity() : (WorldlineCensusBlockEntity) raw;
        if (be.nonce() != 0 && be.nonce() != nonces[i])
          throw new IllegalStateException("M74 state drift");
        be.setNonce(nonces[i]);
        if (raw == null)
          world.setBlockEntity(xs[i], ys[i], zs[i], be);
        applied[i] = true;
        appliedCount++;
      }
  }
  public static synchronized int index(int cx, int cy, int cz, int nonce) {
    int dz = cz - z, dy = cy - y, at = dz * 4 + dy;
    return planned && cx == x && dz >= 0 && dz < 4 && dy >= 0 && dy < 4
            && nonce == ROOT * 100 + at + 1
        ? at
        : -1;
  }
  public static synchronized boolean ready(int mask) {
    int expected = mode().equals("present") ? 16 : 0;
    return scene && scenePlaced == expected && received == expected && appliedCount == expected
        && mask == (expected == 16 ? 0xffff : 0);
  }
  public static synchronized int packed() {
    return received << 8 | appliedCount;
  }
  public static synchronized int x() {
    return x;
  }
  public static synchronized int y() {
    return y;
  }
  public static synchronized int z() {
    return z;
  }
  private static void diagnostic(String reason) {
    if (!trackedDiagnostic) {
      trackedDiagnostic = true;
      System.out.println("[WorldlineCensus] tracked-wait " + reason + " expected=" + (x - 1.5D)
          + "/" + (y + 1.62D) + "/" + (z + 2.5D) + "/-90/0");
    }
  }
  private static String mode() {
    return System.getProperty("worldline.census.mode", "");
  }
}
