package worldline.m80;

import net.minecraft.entity.player.*;
import net.modificationstation.stationapi.api.network.packet.MessagePacket;
import worldline.m74.*;

/** Validates and applies one server-authored membership removal. */
public final class WorldlineMembershipServer {
  private static boolean done;
  private WorldlineMembershipServer() {
  }
  public static synchronized void remove(PlayerEntity player, int[] v) {
    int root = Integer.getInteger("worldline.census.nonce", 0);
    if (!(player instanceof ServerPlayerEntity server) || done || v == null || v.length != 4
        || v[3] != root || root <= 0)
      throw new IllegalStateException("invalid M80 request");
    int x = v[0], y = v[1], z = v[2];
    if (player.world.getBlockId(x, y, z) != WorldlineCensusMod.block.id
        || !(player.world.getBlockEntity(x, y, z) instanceof WorldlineCensusBlockEntity be)
        || be.nonce() != root * 100 + 1)
      throw new IllegalStateException("M80 target drift");
    if (!player.world.setBlock(x, y, z, 0) || player.world.getBlockId(x, y, z) != 0)
      throw new IllegalStateException("M80 removal rejected");
    done = true;
    MessagePacket ack = new MessagePacket(WorldlineMembershipMod.CHANGE);
    ack.ints = v.clone();
    server.networkHandler.sendPacket(ack);
    System.out.println(
        "[WorldlineMembership] removed x=" + x + " y=" + y + " z=" + z + " nonce=" + root);
  }
}
