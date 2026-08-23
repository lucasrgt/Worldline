package worldline.m81;

import net.minecraft.entity.player.*;
import net.modificationstation.stationapi.api.network.packet.MessagePacket;
import worldline.m74.*;

/** Validates and applies one server-authored multipage removal. */
public final class WorldlineWaveServer {
  private static boolean done;
  private WorldlineWaveServer() {
  }
  public static synchronized void remove(PlayerEntity player, int[] v) {
    int root = Integer.getInteger("worldline.census.nonce", 0);
    if (!(player instanceof ServerPlayerEntity server) || done || v == null || v.length != 4
        || v[3] != root || root <= 0)
      throw new IllegalStateException("invalid M81 request");
    int x = v[0], y = v[1], z = v[2], z2 = z + 2;
    if (player.world.getBlockId(x, y, z) != WorldlineCensusMod.block.id
        || player.world.getBlockId(x, y, z2) != WorldlineCensusMod.block.id
        || !(player.world.getBlockEntity(x, y, z) instanceof WorldlineCensusBlockEntity first)
        || !(player.world.getBlockEntity(x, y, z2) instanceof WorldlineCensusBlockEntity second)
        || first.nonce() != root * 100 + 1 || second.nonce() != root * 100 + 9)
      throw new IllegalStateException("M81 target drift");
    if (!player.world.setBlock(x, y, z, 0) || !player.world.setBlock(x, y, z2, 0)
        || player.world.getBlockId(x, y, z) != 0 || player.world.getBlockId(x, y, z2) != 0)
      throw new IllegalStateException("M81 removal rejected");
    done = true;
    MessagePacket ack = new MessagePacket(WorldlineWaveMod.CHANGE);
    ack.ints = v.clone();
    server.networkHandler.sendPacket(ack);
    System.out.println("[WorldlineWave] removed targets=2 x=" + x + " y=" + y + " z=" + z
        + " z2=" + z2 + " nonce=" + root);
  }
}
