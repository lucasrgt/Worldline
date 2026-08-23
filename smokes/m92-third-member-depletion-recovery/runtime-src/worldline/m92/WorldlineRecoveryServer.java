package worldline.m92;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.modificationstation.stationapi.api.network.packet.MessagePacket;
import worldline.m74.WorldlineCensusBlockEntity;
import worldline.m74.WorldlineCensusMod;

/** Validates three removals and reverse restorations in the exact larger page. */
public final class WorldlineRecoveryServer {
  private static final int[] INDICES = {1, 2, 3, 3, 2, 1}, OPERATIONS = {1, 1, 1, 2, 2, 2};
  private static int step, baseX, baseY, baseZ, root;
  private WorldlineRecoveryServer() {
  }
  public static synchronized void change(PlayerEntity player, int[] values) {
    int expected = Integer.getInteger("worldline.census.nonce", 0);
    if (!(player instanceof ServerPlayerEntity server) || values == null || values.length != 7
        || step >= 6 || values[3] != expected || expected <= 0 || values[4] != step + 1
        || values[5] != OPERATIONS[step] || values[6] != INDICES[step])
      throw new IllegalStateException("invalid M92 request");
    int index = INDICES[step];
    if (step == 0) {
      baseX = values[0];
      baseY = values[1] - index;
      baseZ = values[2];
      root = values[3];
    }
    if (values[0] != baseX || values[1] != baseY + index || values[2] != baseZ || values[3] != root)
      throw new IllegalStateException("M92 target drift");
    if (OPERATIONS[step] == 1)
      remove(player, values, index);
    else
      restore(player, server, values, index);
    step++;
    MessagePacket ack = new MessagePacket(WorldlineRecoveryMod.CHANGE);
    ack.ints = values.clone();
    server.networkHandler.sendPacket(ack);
  }
  private static void remove(PlayerEntity player, int[] values, int index) {
    int x = baseX, y = baseY + index, z = baseZ;
    if (player.world.getBlockId(x, y, z) != WorldlineCensusMod.block.id
        || !(player.world.getBlockEntity(x, y, z) instanceof WorldlineCensusBlockEntity be)
        || be.nonce() != root * 100 + index + 1)
      throw new IllegalStateException("M92 remove target drift");
    if (!player.world.setBlock(x, y, z, 0) || player.world.getBlockId(x, y, z) != 0
        || player.world.getBlockEntity(x, y, z) != null)
      throw new IllegalStateException("M92 removal rejected");
    System.out.println("[WorldlineRecovery] removed ordinal=" + values[4]
        + " operation=1 index=" + index + " x=" + x + " y=" + y + " z=" + z + " nonce=" + root);
  }
  private static void restore(
      PlayerEntity player, ServerPlayerEntity server, int[] values, int index) {
    int x = baseX, y = baseY + index, z = baseZ;
    if (player.world.getBlockId(x, y, z) != 0 || player.world.getBlockEntity(x, y, z) != null)
      throw new IllegalStateException("M92 restore target drift");
    if (!player.world.setBlock(x, y, z, WorldlineCensusMod.block.id)
        || !(player.world.getBlockEntity(x, y, z) instanceof WorldlineCensusBlockEntity be))
      throw new IllegalStateException("M92 restoration rejected");
    int nonce = root * 100 + index + 1;
    be.setNonce(nonce);
    MessagePacket state = new MessagePacket(WorldlineRecoveryMod.RESTORE);
    state.ints = new int[] {x, y, z, nonce, step + 1, index};
    server.networkHandler.sendPacket(state);
    System.out.println("[WorldlineRecovery] restored ordinal=" + values[4]
        + " operation=2 index=" + index + " x=" + x + " y=" + y + " z=" + z + " nonce=" + root);
  }
}
