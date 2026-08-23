package worldline.smoke.bedspawnsetb173;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import worldline.api.BlockFace;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.RemoteChunkSnapshot;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173WireClient;

/** Smoke-local raised 3x3 stone pad, bed halves, and SHA helpers. */
final class BedSpawnSupport {
  private BedSpawnSupport() {
  }

  static BlockPosition place(B173WireClient actor, BlockPosition support, BlockFace face, int id)
      throws Exception {
    BlockPosition target = face.adjacent(support);
    actor.placeHeldBlock(support, face);
    actor.awaitBlock(target, new BlockState(id, 0));
    return target;
  }

  static void pad(B173WireClient actor, BlockPosition center) throws Exception {
    BlockPosition east = place(actor, center, BlockFace.EAST, 1);
    BlockPosition west = place(actor, center, BlockFace.WEST, 1);
    place(actor, center, BlockFace.NORTH, 1);
    place(actor, center, BlockFace.SOUTH, 1);
    place(actor, east, BlockFace.NORTH, 1);
    place(actor, east, BlockFace.SOUTH, 1);
    place(actor, west, BlockFace.NORTH, 1);
    place(actor, west, BlockFace.SOUTH, 1);
  }

  static BlockPosition foundation(RemoteChunkSnapshot chunk, int chunkX, int chunkZ) {
    for (int x = 4; x <= 11; x++)
      for (int z = 4; z <= 11; z++)
        for (int y = 126; y >= 1; y--)
          if (chunk.blockAt(x, y, z).legacyId() == 3
              && water(chunk.blockAt(x, y + 1, z).legacyId()))
            return new BlockPosition(chunkX * 16 + x, y, chunkZ * 16 + z);
    throw new IllegalStateException("no deterministic bed spawn set foundation");
  }

  static boolean water(int id) {
    return id == 8 || id == 9;
  }
  static int local(int value, int chunk) {
    return value - chunk * 16;
  }

  static void awaitPlayers(B173DedicatedServer server, int count) throws Exception {
    long end = System.currentTimeMillis() + 5000;
    while (System.currentTimeMillis() < end) {
      if (server.players().size() == count)
        return;
      Thread.sleep(100);
    }
    throw new IllegalStateException("player count drift");
  }

  static String sha(String value) throws Exception {
    byte[] bytes =
        MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
    StringBuilder result = new StringBuilder();
    for (byte item : bytes)
      result.append(String.format("%02x", item & 255));
    return result.toString();
  }

  static void require(boolean value, String message) {
    if (!value)
      throw new IllegalStateException(message);
  }
}
