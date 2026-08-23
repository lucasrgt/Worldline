package worldline.m93;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.modificationstation.stationapi.api.network.packet.MessagePacket;
import worldline.m74.WorldlineCensusBlockEntity;
import worldline.m74.WorldlineCensusMod;

/** Validates complete depletion and reverse restoration of the exact page. */
public final class WorldlineRecoveryServer {
  private static final int[] INDICES = {1, 2, 3, 5, 6, 7, 7, 6, 5, 3, 2, 1},
                             OPERATIONS = {1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2};
  private static int step, baseX, baseY, baseZ, root;
  private WorldlineRecoveryServer() {
  }
  public static synchronized void change(PlayerEntity player, int[] values) {
    int expected = Integer.getInteger("worldline.census.nonce", 0);
    if (!(player instanceof ServerPlayerEntity server) || values == null || values.length != 7
        || step >= 12 || values[3] != expected || expected <= 0 || values[4] != step + 1
        || values[5] != OPERATIONS[step] || values[6] != INDICES[step])
      throw new IllegalStateException("invalid M93 request");
    int index = INDICES[step], dy = index & 3, dz = index >> 2;
    if (step == 0) {
      baseX = values[0];
      baseY = values[1] - dy;
      baseZ = values[2] - dz;
      root = values[3];
    }
    if (values[0] != baseX || values[1] != baseY + dy || values[2] != baseZ + dz
        || values[3] != root)
      throw new IllegalStateException("M93 target drift");
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
    int x = baseX, y = baseY + (index & 3), z = baseZ + (index >> 2);
    if (player.world.getBlockId(x, y, z) != WorldlineCensusMod.block.id
        || !(player.world.getBlockEntity(x, y, z) instanceof WorldlineCensusBlockEntity be)
        || be.nonce() != root * 100 + index + 1)
      throw new IllegalStateException("M93 remove target drift");
    if (!player.world.setBlock(x, y, z, 0) || player.world.getBlockId(x, y, z) != 0
        || player.world.getBlockEntity(x, y, z) != null)
      throw new IllegalStateException("M93 removal rejected");
    System.out.println("[WorldlineRecovery] removed ordinal=" + values[4]
        + " operation=1 index=" + index + " x=" + x + " y=" + y + " z=" + z + " nonce=" + root);
  }
  private static void restore(
      PlayerEntity player, ServerPlayerEntity server, int[] values, int index) {
    int x = baseX, y = baseY + (index & 3), z = baseZ + (index >> 2);
    if (player.world.getBlockId(x, y, z) != 0 || player.world.getBlockEntity(x, y, z) != null)
      throw new IllegalStateException("M93 restore target drift");
    if (!player.world.setBlock(x, y, z, WorldlineCensusMod.block.id)
        || !(player.world.getBlockEntity(x, y, z) instanceof WorldlineCensusBlockEntity be))
      throw new IllegalStateException("M93 restoration rejected");
    int nonce = root * 100 + index + 1;
    be.setNonce(nonce);
    MessagePacket state = new MessagePacket(WorldlineRecoveryMod.RESTORE);
    state.ints = new int[] {x, y, z, nonce, step + 1, index};
    server.networkHandler.sendPacket(state);
    System.out.println("[WorldlineRecovery] restored ordinal=" + values[4]
        + " operation=2 index=" + index + " x=" + x + " y=" + y + " z=" + z + " nonce=" + root);
  }
}
