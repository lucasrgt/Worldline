package worldline.smoke.pigmanangersetb173;
import static worldline.b173server.B173FixtureSupport.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Nether Packet24 type 57 pair: Packet7 diamond sword 276 angers nearby pigmen. Not M411 pork or M437 lightning. */
public final class PigmanAngerSetSmoke {
  private PigmanAngerSetSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 7)
      throw new IllegalArgumentException(
          "usage: PigmanAngerSetSmoke server.jar workspace port seed username chunkX chunkZ");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    int cx = Integer.parseInt(a[5]), cz = Integer.parseInt(a[6]);
    Duration timeout = Duration.ofSeconds(180);
    require(seed == 17320110707L && user.equals("PigAnger450") && user.length() <= 16,
        "pigman-anger-set identity drift");
    B173DedicatedServer server = B173PigmanAngerAccess.server(jar, workspace, port, seed, timeout);
    B173WireClient scout = new B173WireClient("127.0.0.1", port, user, timeout),
                   actor = new B173WireClient("127.0.0.1", port, user, timeout);
    BlockPosition top, first, second;
    try {
      server.boot();
      B173PlayerSeed.writeDimension(workspace, user, 4.5D, 64D, 4.5D, -1);
      scout.connect();
      scout.synchronizePose();
      require(scout.dimension() == -1 && scout.awaitDimension(-1) == -1,
          "nether scout dimension drift");
      RemoteChunkSnapshot initial = scout.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      require(count(initial, 87) > 0 && sky(initial) == 0, "nether terrain identity drift");
      top = foundation(initial, cx, cz);
      scout.close();
      awaitPlayers(server, 0);
      B173PlayerSeed.writeInventory(workspace, user, top.x() + 0.5D, top.y() + 1.0D, top.z() + 0.5D,
          -1, new int[] {0, 1, 2, 3}, new int[] {87, 52, 276, 297}, new int[] {64, 2, 1, 8},
          new int[] {0, 0, 0, 0});
      actor.connect();
      require(actor.synchronizePose() != null && actor.dimension() == -1
              && actor.awaitInventory().occupiedSlots() == 4,
          "nether pigman anger inventory or dimension drift");
      actor.awaitRemoteChunk(cx, cz);
      actor.look(0F, 0F);
      actor.selectHeldSlot(0);
      B173PigmanAngerAccess.pad(actor, top);
      PlayerPose pose = actor.moveAndObserve(0D, 0D, -1.5D, 4).resulting();
      require(
          Math.abs(pose.x() - (top.x() + 0.5D)) < 3D && Math.abs(pose.y() - (top.y() + 1.0D)) < 4D,
          "actor missed nether pigman anger fixture");
      actor.selectHeldSlot(1);
      first = B173PigmanAngerAccess.place(actor, top, BlockFace.UP, 52);
      second = B173PigmanAngerAccess.second(actor, first);
      actor.sustainTicks(5);
      actor.close();
      awaitPlayers(server, 0);
      server.save();
    } finally {
      scout.close();
      actor.close();
      server.close();
    }
    Thread.sleep(1000L);
    B173PigmanAngerAccess.retarget(workspace, first);
    server = B173PigmanAngerAccess.server(jar, workspace, port, seed, timeout);
    actor = new B173WireClient("127.0.0.1", port, user, timeout);
    try {
      server.boot();
      actor.connect();
      actor.synchronizePose();
      require(actor.dimension() == -1 && actor.awaitInventory().occupiedSlots() >= 1,
          "nether pigman anger reload drift");
      RemoteMobSpawn struck = B173PigmanAngerAccess.near(actor, 57, first),
                     neighbor = B173PigmanAngerAccess.near(actor, 57, second);
      require(struck.legacyType() == 57 && neighbor.legacyType() == 57
              && struck.entityId() != neighbor.entityId()
              && struck.entityId() != actor.state().entityId()
              && neighbor.entityId() != actor.state().entityId() && struck.legacyType() != 90
              && neighbor.legacyType() != 90,
          "pigman Packet24 type 57 pair identity drift");
      B173PigmanAngerAccess.provoke(actor, struck, neighbor);
      require(B173PigmanAngerAccess.peekDeath(actor, struck.entityId()) == null
              && B173PigmanAngerAccess.peekDeath(actor, neighbor.entityId()) == null,
          "Packet38 status 3 death during anger");
      actor.close();
      awaitPlayers(server, 0);
      server.save();
      String evidence = "dimension=-1,netherrack=positive,first=" + cell(first)
          + ",second=" + cell(second)
          + ",entityid=PigZombie,mobs=type57+type57,sword=276,hit=packet7,hurt=packet38-status2,aggro=neighbor,not-m411-pork,not-m437-lightning,clients=2,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|profile=allow-nether-true+spawn-monsters-true|entry=prelogin-player-nbt-dimension-minus-one+item52+item276|fixture=nether-netherrack87+two-spawner52|cause=nbt-entityid-pigzombie+diamond-sword-packet7|wire=packet24-type57+packet24-type57+packet38-status2|oracle=nether-pigzombie57-neighbor-anger-not-m411-pork-not-m437-lightning|"
          + evidence;
      System.out.println("WORLDLINE_M450_SET=" + evidence);
      System.out.println("WORLDLINE_M450_TRACE=" + trace);
      System.out.println("WORLDLINE_M450_SIGNATURE=" + sha(trace));
    } finally {
      actor.close();
      server.close();
    }
  }
  private static BlockPosition foundation(RemoteChunkSnapshot q, int cx, int cz) {
    for (int x = 2; x <= 13; x++)
      for (int z = 2; z <= 13; z++)
        for (int y = 110; y >= 8; y--) {
          int id = q.blockAt(x, y, z).legacyId(), up = q.blockAt(x, y + 1, z).legacyId(),
              up2 = q.blockAt(x, y + 2, z).legacyId(), up3 = q.blockAt(x, y + 3, z).legacyId();
          if (id == 87 && air(up) && air(up2) && air(up3) && !lava(up) && !lava(up2))
            return new BlockPosition(cx * 16 + x, y, cz * 16 + z);
        }
    throw new IllegalStateException("no deterministic nether pigman anger foundation");
  }
  private static String cell(BlockPosition p) {
    return p.x() + ":" + p.y() + ":" + p.z() + ":52:0";
  }
  private static boolean air(int id) {
    return id == 0;
  }
  private static boolean lava(int id) {
    return id == 10 || id == 11;
  }
  private static int count(RemoteChunkSnapshot q, int id) {
    int n = 0;
    for (int x = 0; x < 16; x++)
      for (int z = 0; z < 16; z++)
        for (int y = 0; y < 128; y++)
          if (q.blockAt(x, y, z).legacyId() == id)
            n++;
    return n;
  }
  private static int sky(RemoteChunkSnapshot q) {
    int n = 0;
    for (int x = 0; x < 16; x++)
      for (int z = 0; z < 16; z++)
        for (int y = 0; y < 128; y++)
          if (q.skyLightAt(x, y, z) > 0)
            n++;
    return n;
  }
  private static void require(boolean v, String m) {
    if (!v)
      throw new IllegalStateException(m);
  }
}
