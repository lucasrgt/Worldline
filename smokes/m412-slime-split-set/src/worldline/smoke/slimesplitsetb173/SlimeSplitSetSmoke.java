package worldline.smoke.slimesplitsetb173;
import static worldline.b173server.B173FixtureSupport.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Places a slime-chunk spawner below y=16, kills Packet24 type 55, and requires child type-55 Packet24. */
public final class SlimeSplitSetSmoke {
  private static final int[] SLOTS = {0, 1, 2, 3, 100, 101, 102, 103},
                             IDS = {1, 52, 276, 320, 313, 312, 311, 310},
                             COUNTS = {32, 1, 1, 8, 1, 1, 1, 1}, DMG = {0, 0, 0, 0, 0, 0, 0, 0};
  private SlimeSplitSetSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 7)
      throw new IllegalArgumentException(
          "usage: SlimeSplitSetSmoke server.jar workspace port seed username chunkX chunkZ");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    int cx = Integer.parseInt(a[5]), cz = Integer.parseInt(a[6]);
    require(user.length() <= 16 && user.equals("SlimeSplt412")
            && B173SlimeAccess.slimeChunk(seed, cx, cz),
        "slime-split identity or slime-chunk drift");
    Duration timeout = Duration.ofSeconds(360);
    B173DedicatedServer server =
        B173DedicatedServer.monsters(jar, workspace, port, seed, timeout, 5, true);
    B173WireClient scout = new B173WireClient("127.0.0.1", port, user, timeout),
                   actor = new B173WireClient("127.0.0.1", port, user, timeout);
    BlockPosition room, spawner;
    int children = 0, kills = 0;
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, SLOTS, IDS, COUNTS, DMG);
      scout.connect();
      scout.synchronizePose();
      require(scout.awaitInventory().occupiedSlots() == 8, "slime-split scout inventory drift");
      scout.awaitRemoteChunk(0, 0);
      RemoteChunkSnapshot chunk = B173SlimeAccess.waitChunk(scout, cx, cz).chunkAt(cx, cz);
      room = pocket(chunk, cx, cz);
      require(room != null && room.y() < 16, "slime-chunk cave below y=16 absent");
      scout.close();
      awaitPlayers(server, 0);
      B173PlayerSeed.writeInventory(
          workspace, user, room.x() + 1.5D, room.y(), room.z() + 0.5D, SLOTS, IDS, COUNTS, DMG);
      actor.connect();
      actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() == 8, "slime-split actor inventory drift");
      B173SlimeAccess.waitChunk(actor, cx, cz);
      B173SlimeAccess.go(actor, room.x() + 0.5D, room.y(), room.z() + 0.5D);
      worldline.test.WorldlineSmokeAwait.observe(actor, 5);
      actor.selectHeldSlot(1);
      spawner = place(actor, new BlockPosition(room.x(), room.y() - 1, room.z()), BlockFace.UP, 52);
      require(spawner.y() < 16, "slime spawner was not below y=16");
      worldline.test.WorldlineSmokeAwait.observe(actor, 5);
      actor.close();
      awaitPlayers(server, 0);
      server.save();
    } finally {
      scout.close();
      actor.close();
      server.close();
    }
    Thread.sleep(1000L);
    B173SpawnerSeed.slime(workspace, spawner);
    B173PlayerSeed.writeInventory(
        workspace, user, room.x() + 1.5D, room.y(), room.z() + 0.5D, SLOTS, IDS, COUNTS, DMG);
    server = B173DedicatedServer.monsters(jar, workspace, port, seed, timeout, 5, true);
    actor = new B173WireClient("127.0.0.1", port, user, timeout);
    try {
      server.boot();
      actor.connect();
      actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() >= 1, "slime-split reload inventory drift");
      B173SlimeAccess.waitChunk(actor, cx, cz);
      while (children < 1) {
        require(++kills <= 8, "official type-55 split absent after bounded kills");
        RemoteMobSpawn parent = B173SlimeAccess.huntNear(
            actor, 240, spawner.x() + 0.5D, spawner.y() + 1, spawner.z() + 0.5D);
        require(parent.legacyType() == 55 && parent.entityId() != actor.state().entityId()
                && parent.y() < 16D,
            "slime Packet24 type 55 identity drift");
        B173SlimeAccess.kill(actor, parent);
        children = B173SlimeAccess.children(actor, parent);
      }
      require(children >= 1, "slime split collapsed to a single Packet24 spawn");
      actor.close();
      awaitPlayers(server, 0);
      server.save();
      String evidence = "chunk=" + cx + ":" + cz + ",room=" + room.x() + ":" + room.y() + ":"
          + room.z() + ",spawner=" + spawner.x() + ":" + spawner.y() + ":" + spawner.z()
          + ":52:0,entityid=Slime,lowy=true,mobs=type55,split=parent-death+child-packet24-type55,sword=276,armor=diamond310+311+312+313,kills<=8,clients=3,disconnect=clean";
      String trace = "v2|server=official-b1.7.3|seed=" + seed
          + "|fixture=slime-chunk-y<16-spawner52|cause=nbt-entityid-slime+diamond-sword-packet7|safety=player-nbt-diamond-armor310+311+312+313|wire=packet24-type55+packet38-status3+packet29+child-packet24-type55|oracle=slime-parent-death-plus-child-spawns|"
          + evidence;
      System.out.println("WORLDLINE_M412_SPLIT=" + evidence);
      System.out.println("WORLDLINE_M412_TRACE=" + trace);
      System.out.println("WORLDLINE_M412_SIGNATURE=" + sha(trace));
    } finally {
      actor.close();
      server.close();
    }
  }
  private static BlockPosition pocket(RemoteChunkSnapshot q, int cx, int cz) {
    for (int y = 14; y >= 5; y--)
      for (int x = 2; x <= 13; x++)
        for (int z = 2; z <= 13; z++)
          if (open(q, x, y, z))
            return new BlockPosition(cx * 16 + x, y, cz * 16 + z);
    return null;
  }
  private static boolean open(RemoteChunkSnapshot q, int x, int y, int z) {
    if (!solid(q.blockAt(x, y - 1, z).legacyId()))
      return false;
    for (int dx = -1; dx <= 1; dx++)
      for (int dz = -1; dz <= 1; dz++)
        if (q.blockAt(x + dx, y, z + dz).legacyId() != 0
            || q.blockAt(x + dx, y + 1, z + dz).legacyId() != 0)
          return false;
    return true;
  }
  private static boolean solid(int id) {
    return id == 1 || id == 2 || id == 3 || id == 4 || id == 12 || id == 13 || id == 24;
  }
  private static void require(boolean v, String m) {
    if (!v)
      throw new IllegalStateException(m);
  }
}
