package worldline.m87;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.world.World;
import worldline.m74.WorldlineCensusBlockEntity;
import worldline.m74.WorldlineCensusMod;

/** Primitive remove/restore acknowledgement and buffered restore state. */
public final class WorldlineRecoveryState {
  private static final boolean[][] ack = new boolean[2][2];
  private static final boolean[] restoreReceived = new boolean[2], restoreApplied = new boolean[2];
  private static final int[] restoreNonce = new int[2];
  private static int baseX, baseY, baseZ, root;
  private WorldlineRecoveryState() {
  }
  public static synchronized void ack(int[] values) {
    if (values == null || values.length != 7 || values[4] < 1 || values[4] > 2 || values[5] < 1
        || values[5] > 2 || values[6] != values[4] - 1)
      throw new IllegalStateException("invalid M87 ack");
    int generation = values[4] - 1, operation = values[5] - 1,
        expected = generation * 2 + operation;
    if (ack[generation][operation] || expected > 0 && !ack[(expected - 1) / 2][(expected - 1) % 2])
      throw new IllegalStateException("out-of-order M87 ack");
    if (expected == 0) {
      baseX = values[0];
      baseY = values[1];
      baseZ = values[2];
      root = values[3];
    } else if (values[0] != baseX || values[1] != baseY + generation || values[2] != baseZ
        || values[3] != root)
      throw new IllegalStateException("conflicting M87 ack");
    ack[generation][operation] = true;
  }
  public static synchronized void restore(int[] values) {
    if (values == null || values.length != 6 || values[3] <= 0 || values[4] < 1 || values[4] > 2
        || values[5] != values[4] - 1)
      throw new IllegalStateException("invalid M87 restore state");
    int generation = values[4] - 1;
    if (restoreReceived[generation]) {
      if (values[0] != baseX || values[1] != baseY + generation || values[2] != baseZ
          || values[3] != restoreNonce[generation])
        throw new IllegalStateException("conflicting M87 restore state");
      return;
    }
    if (!ack[generation][0] || values[0] != baseX || values[1] != baseY + generation
        || values[2] != baseZ || values[3] != root * 100 + generation + 1)
      throw new IllegalStateException("unbound M87 restore state");
    restoreNonce[generation] = values[3];
    restoreReceived[generation] = true;
  }
  public static synchronized void apply(World world) {
    int generation = ack[1][0] ? 1 : 0, x = baseX, y = baseY + generation, z = baseZ;
    if (!restoreReceived[generation] || restoreApplied[generation] || world == null
        || world.getBlockId(x, y, z) != WorldlineCensusMod.block.id)
      return;
    BlockEntity raw = world.getBlockEntity(x, y, z);
    if (raw != null && !(raw instanceof WorldlineCensusBlockEntity))
      throw new IllegalStateException("M87 restored BE type drift");
    WorldlineCensusBlockEntity be =
        raw == null ? new WorldlineCensusBlockEntity() : (WorldlineCensusBlockEntity) raw;
    if (be.nonce() != 0 && be.nonce() != restoreNonce[generation])
      throw new IllegalStateException("M87 restored nonce drift");
    be.setNonce(restoreNonce[generation]);
    if (raw == null)
      world.setBlockEntity(x, y, z, be);
    restoreApplied[generation] = true;
  }
  public static synchronized boolean removed(int generation, int ex, int ey, int ez, int nonce) {
    int at = generation - 1;
    return ack[at][0] && baseX == ex && baseY + at == ey && baseZ == ez && root == nonce;
  }
  public static synchronized boolean restored(int generation, int ex, int ey, int ez, int nonce) {
    int at = generation - 1;
    return ack[at][1] && restoreReceived[at] && restoreApplied[at] && baseX == ex
        && baseY + at == ey && baseZ == ez && root == nonce;
  }
}
