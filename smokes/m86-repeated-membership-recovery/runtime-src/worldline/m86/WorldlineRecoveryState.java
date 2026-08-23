package worldline.m86;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.world.World;
import worldline.m74.WorldlineCensusBlockEntity;
import worldline.m74.WorldlineCensusMod;

/** Primitive remove/restore acknowledgement and buffered restore state. */
public final class WorldlineRecoveryState {
  private static final boolean[][] ack = new boolean[2][2];
  private static final boolean[] restoreReceived = new boolean[2], restoreApplied = new boolean[2];
  private static int x, y, z, root, restoreNonce;
  private WorldlineRecoveryState() {
  }
  public static synchronized void ack(int[] values) {
    if (values == null || values.length != 6 || values[4] < 1 || values[4] > 2 || values[5] < 1
        || values[5] > 2)
      throw new IllegalStateException("invalid M86 ack");
    int generation = values[4] - 1, operation = values[5] - 1,
        expected = generation * 2 + operation;
    if (ack[generation][operation] || expected > 0 && !ack[(expected - 1) / 2][(expected - 1) % 2])
      throw new IllegalStateException("out-of-order M86 ack");
    if (expected == 0) {
      x = values[0];
      y = values[1];
      z = values[2];
      root = values[3];
    } else if (values[0] != x || values[1] != y || values[2] != z || values[3] != root)
      throw new IllegalStateException("conflicting M86 ack");
    ack[generation][operation] = true;
  }
  public static synchronized void restore(int[] values) {
    if (values == null || values.length != 5 || values[3] <= 0 || values[4] < 1 || values[4] > 2)
      throw new IllegalStateException("invalid M86 restore state");
    int generation = values[4] - 1;
    if (restoreReceived[generation]) {
      if (values[0] != x || values[1] != y || values[2] != z || values[3] != restoreNonce)
        throw new IllegalStateException("conflicting M86 restore state");
      return;
    }
    if (!ack[generation][0] || values[0] != x || values[1] != y || values[2] != z
        || values[3] != root * 100 + 1)
      throw new IllegalStateException("unbound M86 restore state");
    restoreNonce = values[3];
    restoreReceived[generation] = true;
  }
  public static synchronized void apply(World world) {
    int generation = ack[1][0] ? 1 : 0;
    if (!restoreReceived[generation] || restoreApplied[generation] || world == null
        || world.getBlockId(x, y, z) != WorldlineCensusMod.block.id)
      return;
    BlockEntity raw = world.getBlockEntity(x, y, z);
    if (raw != null && !(raw instanceof WorldlineCensusBlockEntity))
      throw new IllegalStateException("M86 restored BE type drift");
    WorldlineCensusBlockEntity be =
        raw == null ? new WorldlineCensusBlockEntity() : (WorldlineCensusBlockEntity) raw;
    if (be.nonce() != 0 && be.nonce() != restoreNonce)
      throw new IllegalStateException("M86 restored nonce drift");
    be.setNonce(restoreNonce);
    if (raw == null)
      world.setBlockEntity(x, y, z, be);
    restoreApplied[generation] = true;
  }
  public static synchronized boolean removed(int generation, int ex, int ey, int ez, int nonce) {
    return ack[generation - 1][0] && x == ex && y == ey && z == ez && root == nonce;
  }
  public static synchronized boolean restored(int generation, int ex, int ey, int ez, int nonce) {
    int at = generation - 1;
    return ack[at][1] && restoreReceived[at] && restoreApplied[at] && x == ex && y == ey && z == ez
        && root == nonce;
  }
}
