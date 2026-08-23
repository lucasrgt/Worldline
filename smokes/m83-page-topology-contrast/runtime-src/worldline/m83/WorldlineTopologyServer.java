package worldline.m83;

import net.minecraft.entity.player.*;
import net.modificationstation.stationapi.api.network.packet.MessagePacket;
import worldline.m74.*;

/** Applies exactly two removals within one page or across two pages. */
public final class WorldlineTopologyServer {
  private static boolean done;
  private WorldlineTopologyServer() {
  }
  private static int[] indices(int t) {
    if (t == 1)
      return new int[] {0, 1};
    if (t == 2)
      return new int[] {0, 4};
    throw new IllegalStateException("invalid M83 topology");
  }
  public static synchronized void remove(PlayerEntity player, int[] v) {
    int root = Integer.getInteger("worldline.census.nonce", 0),
        expected = Integer.getInteger("worldline.topology.code", 0);
    if (!(player instanceof ServerPlayerEntity server) || done || v == null || v.length != 5
        || v[3] != root || v[4] != expected || root <= 0)
      throw new IllegalStateException("invalid M83 request");
    int x = v[0], y = v[1], z = v[2], topology = v[4];
    int[] ids = indices(topology);
    for (int i : ids) {
      int cy = y + i % 4, cz = z + i / 4;
      if (player.world.getBlockId(x, cy, cz) != WorldlineCensusMod.block.id
          || !(player.world.getBlockEntity(x, cy, cz) instanceof WorldlineCensusBlockEntity be)
          || be.nonce() != root * 100 + i + 1)
        throw new IllegalStateException("M83 target drift");
    }
    for (int i : ids)
      if (!player.world.setBlock(x, y + i % 4, z + i / 4, 0))
        throw new IllegalStateException("M83 removal rejected");
    for (int i : ids)
      if (player.world.getBlockId(x, y + i % 4, z + i / 4) != 0)
        throw new IllegalStateException("M83 removal absent");
    done = true;
    MessagePacket ack = new MessagePacket(WorldlineTopologyMod.CHANGE);
    ack.ints = v.clone();
    server.networkHandler.sendPacket(ack);
    System.out.println("[WorldlineTopology] removed topology=" + topology + " targets=2 x=" + x
        + " y=" + y + " z=" + z + " nonce=" + root);
  }
}
