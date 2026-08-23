package worldline.m92;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.world.World;
import worldline.m74.WorldlineCensusBlockEntity;
import worldline.m74.WorldlineCensusMod;

/** Primitive acknowledgement and buffered state for one six-step page sequence. */
public final class WorldlineRecoveryState {
  private static final int[] INDICES = {1, 2, 3, 3, 2, 1}, OPERATIONS = {1, 1, 1, 2, 2, 2};
  private static final boolean[] ack = new boolean[6], removed = new boolean[4],
                                 restoreReceived = new boolean[4], restoreApplied = new boolean[4];
  private static final int[] restoreNonce = new int[4];
  private static int baseX, baseY, baseZ, root;
  private WorldlineRecoveryState() {
  }
  public static synchronized void ack(int[] values) {
    if (values == null || values.length != 7 || values[4] < 1 || values[4] > 6)
      throw new IllegalStateException("invalid M92 ack");
    int at = values[4] - 1, index = INDICES[at], operation = OPERATIONS[at];
    if (values[5] != operation || values[6] != index || ack[at] || at > 0 && !ack[at - 1])
      throw new IllegalStateException("out-of-order M92 ack");
    if (at == 0) {
      baseX = values[0];
      baseY = values[1] - index;
      baseZ = values[2];
      root = values[3];
    }
    if (values[0] != baseX || values[1] != baseY + index || values[2] != baseZ || values[3] != root
        || operation == 2 && !restoreReceived[index])
      throw new IllegalStateException("conflicting M92 ack");
    ack[at] = true;
    if (operation == 1)
      removed[index] = true;
  }
  public static synchronized void restore(int[] values) {
    if (values == null || values.length != 6 || values[4] < 4 || values[4] > 6)
      throw new IllegalStateException("invalid M92 restore state");
    int at = values[4] - 1, index = INDICES[at], removeAt = index - 1;
    if (values[5] != index || !ack[removeAt] || values[0] != baseX || values[1] != baseY + index
        || values[2] != baseZ || values[3] != root * 100 + index + 1)
      throw new IllegalStateException("unbound M92 restore state");
    if (restoreReceived[index]) {
      if (restoreNonce[index] != values[3])
        throw new IllegalStateException("conflicting M92 restore state");
      return;
    }
    restoreNonce[index] = values[3];
    restoreReceived[index] = true;
  }
  public static synchronized void apply(World world) {
    if (world == null)
      return;
    for (int index = 1; index <= 3; index++) {
      if (!restoreReceived[index] || restoreApplied[index])
        continue;
      int x = baseX, y = baseY + index, z = baseZ;
      if (world.getBlockId(x, y, z) != WorldlineCensusMod.block.id)
        continue;
      BlockEntity raw = world.getBlockEntity(x, y, z);
      if (raw != null && !(raw instanceof WorldlineCensusBlockEntity))
        throw new IllegalStateException("M92 restored BE type drift");
      WorldlineCensusBlockEntity be =
          raw == null ? new WorldlineCensusBlockEntity() : (WorldlineCensusBlockEntity) raw;
      if (be.nonce() != 0 && be.nonce() != restoreNonce[index])
        throw new IllegalStateException("M92 restored nonce drift");
      be.setNonce(restoreNonce[index]);
      if (raw == null)
        world.setBlockEntity(x, y, z, be);
      restoreApplied[index] = true;
    }
  }
  public static synchronized boolean removed(int ordinal, int x, int y, int z, int nonce) {
    int at = ordinal - 1, index = at >= 0 && at < 6 ? INDICES[at] : -1;
    return at >= 0 && at < 6 && OPERATIONS[at] == 1 && ack[at] && removed[index] && baseX == x
        && baseY + index == y && baseZ == z && root == nonce;
  }
  public static synchronized boolean restored(int ordinal, int x, int y, int z, int nonce) {
    int at = ordinal - 1, index = at >= 0 && at < 6 ? INDICES[at] : -1;
    return at >= 0 && at < 6 && OPERATIONS[at] == 2 && ack[at] && restoreReceived[index]
        && restoreApplied[index] && baseX == x && baseY + index == y && baseZ == z && root == nonce;
  }
}
