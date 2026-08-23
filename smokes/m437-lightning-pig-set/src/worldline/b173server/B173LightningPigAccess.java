package worldline.b173server;

import java.nio.file.Path;
import java.time.Duration;
import worldline.api.BlockFace;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.RemoteChunkSnapshot;
import worldline.api.RemoteMobSpawn;

/** Overworld pig and Nether pigman contrast helpers. Lightning is Packet71, not Packet23. */
public final class B173LightningPigAccess {
  private B173LightningPigAccess() {
  }

  public static B173DedicatedServer overworld(
      Path jar, Path directory, int port, long seed, Duration timeout) {
    return B173DedicatedServer.animals(jar, directory, port, seed, timeout, 3, true);
  }

  public static B173DedicatedServer nether(
      Path jar, Path directory, int port, long seed, Duration timeout) {
    return B173DedicatedServer.netherMonsters(jar, directory, port, seed, timeout);
  }

  public static void retargetPigman(Path directory, BlockPosition spawner) {
    B173SpawnerSeed.nether(directory, spawner, "PigZombie");
  }

  public static RemoteMobSpawn near(B173WireClient actor, int type, BlockPosition p) {
    for (int n = 0; n < 32; n++) {
      RemoteMobSpawn spawn = actor.awaitMobSpawn(type);
      double dx = spawn.x() - (p.x() + 0.5D), dz = spawn.z() - (p.z() + 0.5D);
      if (dx * dx + dz * dz <= 100D && Math.abs(spawn.y() - p.y()) <= 6D)
        return spawn;
    }
    throw new IllegalStateException("nearby type " + type + " absent");
  }

  public static void pad(B173WireClient actor, BlockPosition center) throws Exception {
    BlockFace[] faces = {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.WEST, BlockFace.EAST};
    for (int i = 0; i < faces.length; i++) {
      BlockPosition next = faces[i].adjacent(center);
      if (air(actor.awaitRemoteChunk(next.x() >> 4, next.z() >> 4)
                  .blockAt(next.x(), next.y(), next.z())
                  .legacyId()))
        place(actor, center, faces[i], 87);
    }
  }

  public static BlockPosition place(
      B173WireClient actor, BlockPosition support, BlockFace face, int id) throws Exception {
    BlockPosition target = face.adjacent(support);
    actor.placeHeldBlock(support, face);
    actor.awaitBlock(target, new BlockState(id, 0));
    return target;
  }

  public static void grassPlatform(B173WireClient actor, BlockPosition top) throws Exception {
    for (int r = 1; r <= 3; r++) {
      for (int z = -r + 1; z < r; z++) {
        place(actor, new BlockPosition(top.x() - r + 1, top.y(), top.z() + z), BlockFace.WEST, 2);
        place(actor, new BlockPosition(top.x() + r - 1, top.y(), top.z() + z), BlockFace.EAST, 2);
      }
      for (int x = -r + 1; x < r; x++) {
        place(actor, new BlockPosition(top.x() + x, top.y(), top.z() - r + 1), BlockFace.NORTH, 2);
        place(actor, new BlockPosition(top.x() + x, top.y(), top.z() + r - 1), BlockFace.SOUTH, 2);
      }
      place(actor, new BlockPosition(top.x() - r, top.y(), top.z() - r + 1), BlockFace.NORTH, 2);
      place(actor, new BlockPosition(top.x() - r, top.y(), top.z() + r - 1), BlockFace.SOUTH, 2);
      place(actor, new BlockPosition(top.x() + r, top.y(), top.z() - r + 1), BlockFace.NORTH, 2);
      place(actor, new BlockPosition(top.x() + r, top.y(), top.z() + r - 1), BlockFace.SOUTH, 2);
    }
  }

  public static BlockPosition overworldFoundation(RemoteChunkSnapshot q, int cx, int cz) {
    for (int x = 4; x <= 11; x++)
      for (int z = 4; z <= 11; z++)
        for (int y = 126; y >= 1; y--)
          if (q.blockAt(x, y, z).legacyId() == 3 && water(q.blockAt(x, y + 1, z).legacyId()))
            return new BlockPosition(cx * 16 + x, y, cz * 16 + z);
    throw new IllegalStateException("no deterministic overworld pig foundation");
  }

  public static BlockPosition netherFoundation(RemoteChunkSnapshot q, int cx, int cz) {
    for (int x = 2; x <= 13; x++)
      for (int z = 2; z <= 13; z++)
        for (int y = 110; y >= 8; y--) {
          int id = q.blockAt(x, y, z).legacyId(), up = q.blockAt(x, y + 1, z).legacyId();
          int up2 = q.blockAt(x, y + 2, z).legacyId(), up3 = q.blockAt(x, y + 3, z).legacyId();
          if (id == 87 && air(up) && air(up2) && air(up3) && !lava(up) && !lava(up2))
            return new BlockPosition(cx * 16 + x, y, cz * 16 + z);
        }
    throw new IllegalStateException("no deterministic nether pigman foundation");
  }

  public static String cell(BlockPosition p) {
    return p.x() + ":" + p.y() + ":" + p.z() + ":52:0";
  }

  public static boolean air(int id) {
    return id == 0;
  }

  public static boolean lava(int id) {
    return id == 10 || id == 11;
  }

  public static boolean water(int id) {
    return id == 8 || id == 9;
  }

  public static int local(int v, int c) {
    return v - c * 16;
  }

  public static int count(RemoteChunkSnapshot q, int id) {
    int n = 0;
    for (int x = 0; x < 16; x++)
      for (int z = 0; z < 16; z++)
        for (int y = 0; y < 128; y++)
          if (q.blockAt(x, y, z).legacyId() == id)
            n++;
    return n;
  }

  public static int sky(RemoteChunkSnapshot q) {
    int n = 0;
    for (int x = 0; x < 16; x++)
      for (int z = 0; z < 16; z++)
        for (int y = 0; y < 128; y++)
          if (q.skyLightAt(x, y, z) > 0)
            n++;
    return n;
  }

  public static void awaitPlayers(B173DedicatedServer server, int n) throws Exception {
    long end = System.currentTimeMillis() + 5000;
    while (System.currentTimeMillis() < end) {
      if (server.players().size() == n)
        return;
      Thread.sleep(100);
    }
    throw new IllegalStateException("player count drift");
  }
}
