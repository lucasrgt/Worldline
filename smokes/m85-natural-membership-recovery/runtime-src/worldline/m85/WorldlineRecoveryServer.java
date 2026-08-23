package worldline.m85;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.modificationstation.stationapi.api.network.packet.MessagePacket;
import worldline.m74.WorldlineCensusBlockEntity;
import worldline.m74.WorldlineCensusMod;

/** Validates one exact natural removal followed by restoration of the same cell. */
public final class WorldlineRecoveryServer {
  private static int phase;
  private static int x, y, z, root;
  private WorldlineRecoveryServer() {
  }
  public static synchronized void change(PlayerEntity player, int[] values) {
    int expected = Integer.getInteger("worldline.census.nonce", 0);
    if (!(player instanceof ServerPlayerEntity server) || values == null || values.length != 5
        || values[3] != expected || expected <= 0 || values[4] != phase + 1)
      throw new IllegalStateException("invalid M85 request");
    if (phase == 0)
      remove(player, values);
    else
      restore(player, server, values);
    phase++;
    MessagePacket ack = new MessagePacket(WorldlineRecoveryMod.CHANGE);
    ack.ints = values.clone();
    server.networkHandler.sendPacket(ack);
  }
  private static void remove(PlayerEntity player, int[] values) {
    x = values[0];
    y = values[1];
    z = values[2];
    root = values[3];
    if (player.world.getBlockId(x, y, z) != WorldlineCensusMod.block.id
        || !(player.world.getBlockEntity(x, y, z) instanceof WorldlineCensusBlockEntity be)
        || be.nonce() != root * 100 + 1)
      throw new IllegalStateException("M85 remove target drift");
    if (!player.world.setBlock(x, y, z, 0) || player.world.getBlockId(x, y, z) != 0
        || player.world.getBlockEntity(x, y, z) != null)
      throw new IllegalStateException("M85 removal rejected");
    System.out.println(
        "[WorldlineRecovery] removed x=" + x + " y=" + y + " z=" + z + " nonce=" + root);
  }
  private static void restore(PlayerEntity player, ServerPlayerEntity server, int[] values) {
    if (values[0] != x || values[1] != y || values[2] != z || values[3] != root
        || player.world.getBlockId(x, y, z) != 0 || player.world.getBlockEntity(x, y, z) != null)
      throw new IllegalStateException("M85 restore target drift");
    if (!player.world.setBlock(x, y, z, WorldlineCensusMod.block.id)
        || !(player.world.getBlockEntity(x, y, z) instanceof WorldlineCensusBlockEntity be))
      throw new IllegalStateException("M85 restoration rejected");
    int nonce = root * 100 + 1;
    be.setNonce(nonce);
    MessagePacket state = new MessagePacket(WorldlineRecoveryMod.RESTORE);
    state.ints = new int[] {x, y, z, nonce};
    server.networkHandler.sendPacket(state);
    System.out.println(
        "[WorldlineRecovery] restored x=" + x + " y=" + y + " z=" + z + " nonce=" + root);
  }
}
