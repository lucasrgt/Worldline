package worldline.smoke.slimeballsetb173;
import static worldline.b173server.B173FixtureSupport.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Kills size-1 Packet24 type 55 for Packet21 slimeball 341 and crafts sticky piston 29 from 33+341. */
public final class SlimeballSetSmoke {
  private static final int[] SLOTS = {0, 1, 2, 3, 4, 5, 6}, IDS = {1, 52, 276, 320, 58, 33, 341},
                             COUNTS = {32, 1, 1, 16, 1, 1, 1}, DMG = {0, 0, 0, 0, 0, 0, 0};
  private SlimeballSetSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 7)
      throw new IllegalArgumentException(
          "usage: SlimeballSetSmoke server.jar workspace port seed username chunkX chunkZ");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    int cx = Integer.parseInt(a[5]), cz = Integer.parseInt(a[6]);
    require(user.length() <= 16 && user.equals("SlimeBall423")
            && B173SlimeballAccess.slimeChunk(seed, cx, cz),
        "slimeball identity or slime-chunk drift");
    Duration timeout = Duration.ofSeconds(360);
    B173DedicatedServer server =
        B173DedicatedServer.monsters(jar, workspace, port, seed, timeout, 5, true);
    B173WireClient scout = new B173WireClient("127.0.0.1", port, user, timeout),
                   actor = new B173WireClient("127.0.0.1", port, user, timeout);
    BlockPosition room, spawner, bench;
    int kills = 0;
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, SLOTS, IDS, COUNTS, DMG);
      scout.connect();
      scout.synchronizePose();
      require(scout.awaitInventory().occupiedSlots() == 7, "slimeball scout inventory drift");
      scout.awaitRemoteChunk(0, 0);
      RemoteChunkSnapshot chunk = B173SlimeballAccess.waitChunk(scout, cx, cz).chunkAt(cx, cz);
      room = pocket(chunk, cx, cz);
      require(room != null && room.y() < 16, "slime-chunk cave below y=16 absent");
      scout.close();
      awaitPlayers(server, 0);
      B173PlayerSeed.writeInventory(
          workspace, user, room.x() + 1.5D, room.y(), room.z() + 0.5D, SLOTS, IDS, COUNTS, DMG);
      actor.connect();
      actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() == 7, "slimeball actor inventory drift");
      B173SlimeballAccess.waitChunk(actor, cx, cz);
      B173SlimeballAccess.go(actor, room.x() + 0.5D, room.y(), room.z() + 0.5D);
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
      require(actor.awaitInventory().occupiedSlots() >= 1, "slimeball reload inventory drift");
      B173SlimeballAccess.waitChunk(actor, cx, cz);
      RemoteDroppedItem drop = null;
      while (drop == null) {
        require(++kills <= 12, "official size-1 slimeball 341 drop absent after bounded kills");
        RemoteMobSpawn slime = B173SlimeballAccess.huntNear(
            actor, 240, spawner.x() + 0.5D, spawner.y() + 1, spawner.z() + 0.5D);
        require(slime.legacyType() == 55 && slime.entityId() != actor.state().entityId()
                && slime.y() < 16D,
            "slime Packet24 type 55 identity drift");
        B173SlimeballAccess.kill(actor, slime);
        B173SlimeballAccess.go(actor, room.x() + 1.5D, room.y(), room.z() + 0.5D);
        drop = worldline.test.WorldlineSmokeAwait.awaitEntityOrNull(actor,
            () -> B173SlimeballAccess.ball(actor), value -> value != null, "slimeball drop", 20);
      }
      require(drop.item().legacyId() == 341, "slime Packet21 slimeball id drift");
      int benchSlot = B173SlimeballAccess.find(actor.inventory(), 58);
      require(benchSlot >= 36, "workbench 58 lost");
      actor.selectHeldSlot(benchSlot - 36);
      bench =
          place(actor, new BlockPosition(room.x() - 1, room.y() - 1, room.z()), BlockFace.UP, 58);
      worldline.test.WorldlineSmokeAwait.observe(actor, 5);
      actor.openWorkbench(bench, BlockFace.UP);
      require(B173SlimeballCrafts.apply(actor) == 29 && has(actor.inventory(), 29)
              && !has(actor.inventory(), 33),
          "sticky piston 29 from 33+341 drifted");
      actor.closeWindow();
      actor.close();
      awaitPlayers(server, 0);
      server.save();
      String evidence = "chunk=" + cx + ":" + cz + ",room=" + room.x() + ":" + room.y() + ":"
          + room.z() + ",spawner=" + spawner.x() + ":" + spawner.y() + ":" + spawner.z()
          + ":52:0,entityid=Slime,lowy=true,mobs=type55,size=1,drop=packet21-341,craft=sticky29-from-33+341,workbench="
          + bench.x() + ":" + bench.y() + ":" + bench.z()
          + ":58:0,sword=276,kills<=12,clients=3,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=slime-chunk-y<16-spawner52+workbench58+piston33+slimeball341|cause=nbt-entityid-slime+diamond-sword-packet7+packet102-33+341|wire=packet24-type55+packet38-status3+packet29+packet21-341+packet106-sticky29|oracle=size-1-slimeball-drop-and-sticky-piston-craft|"
          + evidence;
      System.out.println("WORLDLINE_M423_SET=" + evidence);
      System.out.println("WORLDLINE_M423_TRACE=" + trace);
      System.out.println("WORLDLINE_M423_SIGNATURE=" + sha(trace));
    } finally {
      actor.close();
      server.close();
    }
  }
  private static boolean has(RemoteInventoryView view, int id) {
    for (int s = 9; s <= 44; s++)
      if (!view.slot(s).empty() && view.slot(s).item().legacyId() == id)
        return true;
    return false;
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
