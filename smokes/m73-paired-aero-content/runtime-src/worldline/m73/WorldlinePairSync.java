package worldline.m73;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.world.World;

/** Fixed-capacity client reconciliation for the 16 planned cells. */
public final class WorldlinePairSync {
  private static final int CAPACITY = 16;
  private static final int[] xs = new int[CAPACITY], ys = new int[CAPACITY], zs = new int[CAPACITY],
                             nonces = new int[CAPACITY];
  private static final boolean[] applied = new boolean[CAPACITY];
  private static int received, appliedCount, planX, planY, planZ;
  private static boolean planned;
  private WorldlinePairSync() {
  }
  public static synchronized void plan(int[] values) {
    if (values == null || values.length != 3 || values[1] < 1 || values[1] > 124 || planned)
      throw new IllegalStateException("invalid M73 plan");
    planX = values[0];
    planY = values[1];
    planZ = values[2];
    planned = true;
  }
  public static synchronized boolean tracked(
      World world, double playerX, double playerY, double playerZ, float yaw, float pitch) {
    float yawError = (yaw + 90.0F) % 360.0F;
    if (yawError < -180.0F)
      yawError += 360.0F;
    if (yawError > 180.0F)
      yawError -= 360.0F;
    if (!planned || world == null || Math.abs(playerX - (planX - 1.5D)) > 0.05D
        || Math.abs(playerY - planY) > 0.1D || Math.abs(playerZ - (planZ + 2.5D)) > 0.05D
        || Math.abs(yawError) > 0.01F || Math.abs(pitch) > 0.01F)
      return false;
    for (int dz = 0; dz < 4; dz++) {
      if (!world.getChunkSource().isChunkLoaded(planX >> 4, planZ + dz >> 4))
        return false;
      for (int dy = 0; dy < 4; dy++)
        if (world.getBlockId(planX, planY + dy, planZ + dz) != 0)
          return false;
    }
    return true;
  }
  public static synchronized int[] plan() {
    if (!planned)
      throw new IllegalStateException("M73 plan absent");
    return new int[] {planX, planY, planZ};
  }
  public static synchronized void receive(World world, int x, int y, int z, int nonce) {
    if (y < 0 || y > 127 || nonce <= 0)
      throw new IllegalStateException("invalid M73 cell values");
    for (int i = 0; i < received; i++) {
      if (nonces[i] == nonce && (xs[i] != x || ys[i] != y || zs[i] != z))
        throw new IllegalStateException("duplicate M73 nonce");
      if (xs[i] == x && ys[i] == y && zs[i] == z) {
        if (nonces[i] != nonce)
          throw new IllegalStateException("conflicting M73 cell");
        return;
      }
    }
    if (received == CAPACITY)
      throw new IllegalStateException("excess M73 cell");
    xs[received] = x;
    ys[received] = y;
    zs[received] = z;
    nonces[received] = nonce;
    received++;
    apply(world);
  }
  public static synchronized void apply(World world) {
    if (world == null)
      return;
    for (int i = 0; i < received; i++)
      if (!applied[i]) {
        if (world.getBlockId(xs[i], ys[i], zs[i]) != WorldlinePairMod.block.id)
          continue;
        BlockEntity raw = world.getBlockEntity(xs[i], ys[i], zs[i]);
        if (raw != null && !(raw instanceof WorldlinePairBlockEntity))
          throw new IllegalStateException("M73 BE type drift");
        WorldlinePairBlockEntity be =
            raw == null ? new WorldlinePairBlockEntity() : (WorldlinePairBlockEntity) raw;
        if (be.nonce() != 0 && be.nonce() != nonces[i])
          throw new IllegalStateException("M73 BE state drift");
        be.setNonce(nonces[i]);
        if (raw == null)
          world.setBlockEntity(xs[i], ys[i], zs[i], be);
        applied[i] = true;
        appliedCount++;
        System.out.println("[WorldlinePairContent] applied index=" + appliedCount + " x=" + xs[i]
            + " y=" + ys[i] + " z=" + zs[i] + " raw=" + WorldlinePairMod.block.id
            + " nonce=" + nonces[i]);
      }
  }
  public static synchronized int received() {
    return received;
  }
  public static synchronized int applied() {
    return appliedCount;
  }
}
