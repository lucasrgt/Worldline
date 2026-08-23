package worldline.smoke.spiderstringsetb173;
import static worldline.b173server.B173FixtureSupport.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Kills spawned spider Packet24 type 52, collects Packet21 string 287, and places cobweb 30. */
public final class SpiderStringSetSmoke {
  private static final RemoteItemStack STRING = new RemoteItemStack(287, 1, 0);
  private static final BlockState WEB = new BlockState(30, 0);
  private SpiderStringSetSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 7)
      throw new IllegalArgumentException(
          "usage: SpiderStringSetSmoke server.jar workspace port seed username chunkX chunkZ");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    int cx = Integer.parseInt(a[5]), cz = Integer.parseInt(a[6]);
    require(seed == 17320110707L && user.equals("SpidrStr409") && user.length() <= 16,
        "spider-string identity drift");
    Duration timeout = Duration.ofSeconds(180);
    B173DedicatedServer server =
        B173DedicatedServer.monsters(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout);
    BlockPosition top, spawner, web;
    int column;
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1, 2, 3, 4, 5},
          new int[] {1, 2, 30, 52, 276, 320}, new int[] {32, 48, 1, 1, 1, 8},
          new int[] {0, 0, 0, 0, 0, 0});
      actor.connect();
      actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() == 6, "spider-string inventory drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      top = foundation(initial, cx, cz);
      column = 0;
      actor.selectHeldSlot(0);
      while (
          water(initial.blockAt(local(top.x(), cx), top.y() + 1, local(top.z(), cz)).legacyId())) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        require(++column <= 15, "water column exceeded spider-string fixture");
      }
      for (int lift = 0; lift < 8; lift++) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        column++;
      }
      actor.selectHeldSlot(1);
      for (int r = 1; r <= 3; r++) {
        for (int z = -r + 1; z < r; z++) {
          grass(actor, new BlockPosition(top.x() - r + 1, top.y(), top.z() + z), BlockFace.WEST);
          grass(actor, new BlockPosition(top.x() + r - 1, top.y(), top.z() + z), BlockFace.EAST);
        }
        for (int x = -r + 1; x < r; x++) {
          grass(actor, new BlockPosition(top.x() + x, top.y(), top.z() - r + 1), BlockFace.NORTH);
          grass(actor, new BlockPosition(top.x() + x, top.y(), top.z() + r - 1), BlockFace.SOUTH);
        }
        grass(actor, new BlockPosition(top.x() - r, top.y(), top.z() - r + 1), BlockFace.NORTH);
        grass(actor, new BlockPosition(top.x() - r, top.y(), top.z() + r - 1), BlockFace.SOUTH);
        grass(actor, new BlockPosition(top.x() + r, top.y(), top.z() - r + 1), BlockFace.NORTH);
        grass(actor, new BlockPosition(top.x() + r, top.y(), top.z() + r - 1), BlockFace.SOUTH);
      }
      actor.selectHeldSlot(3);
      spawner = place(actor, top, BlockFace.UP, 52);
      worldline.test.WorldlineSmokeAwait.observe(actor, 5);
      actor.close();
      awaitPlayers(server, 0);
      server.save();
    } finally {
      actor.close();
      server.close();
    }
    Thread.sleep(1000L);
    B173SpawnerSeed.entity(workspace, spawner, "Spider");
    server = B173DedicatedServer.monsters(jar, workspace, port, seed, timeout, 3, true);
    actor = new B173WireClient("127.0.0.1", port, user, timeout);
    try {
      server.boot();
      actor.connect();
      actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() >= 1, "spider-string reload inventory drift");
      RemoteDroppedItem silk = null;
      int kills = 0;
      go(actor, spawner);
      server.setTime(14000L);
      while (silk == null) {
        require(++kills <= 8, "official spider string drop absent after bounded kills");
        RemoteMobSpawn spider = near(actor, 52, spawner);
        require(spider.legacyType() == 52 && spider.entityId() != actor.state().entityId()
                && spider.legacyType() != 90,
            "spider Packet24 identity drift");
        kill(actor, spider);
        silk = worldline.test.WorldlineSmokeAwait.awaitEntityOrNull(actor,
            () -> actor.peekDroppedItem(STRING), value -> value != null, "spider string drop", 20);
      }
      require(silk.item().legacyId() == 287, "spider Packet21 string id drift");
      step(actor, top.x() + 0.5D, top.y() + 1.0D, top.z() + 0.5D);
      int webSlot = find(actor.inventory(), 30);
      require(webSlot >= 36, "cobweb 30 lost");
      actor.selectHeldSlot(webSlot - 36);
      web = place(actor, new BlockPosition(top.x() - 1, top.y(), top.z()), BlockFace.UP, 30);
      require(worldline.test.WorldlineSmokeAwait.observe(actor, 5)
                  .blockAt(web.x(), web.y(), web.z())
                  .equals(WEB),
          "live cobweb 30 drift");
      actor.close();
      awaitPlayers(server, 0);
      server.save();
      String evidence = "column=" + column + ",platform=7x7-48grass,spawner=" + cell(spawner)
          + ",entityid=Spider,mob=type52,night=14000,sword=276,drop=packet21-287,cobweb=" + web.x()
          + ":" + web.y() + ":" + web.z() + ":30:0,kills<=8,clients=1,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=raised-7x7-grass-platform+spawner52+cobweb30|cause=nbt-entityid-spider+time-14000+diamond-sword-packet7+packet15-item30|wire=packet24-type52+packet38-status3+packet29+packet21-287+packet53-cobweb30|oracle=spider-string-drop-and-cobweb-place|"
          + evidence;
      System.out.println("WORLDLINE_M409_STRING=" + evidence);
      System.out.println("WORLDLINE_M409_TRACE=" + trace);
      System.out.println("WORLDLINE_M409_SIGNATURE=" + sha(trace));
    } finally {
      actor.close();
      server.close();
    }
  }
  private static RemoteMobSpawn near(B173WireClient a, int type, BlockPosition p) {
    for (int n = 0; n < 32; n++) {
      RemoteMobSpawn s = a.awaitMobSpawn(type);
      double dx = s.x() - (p.x() + 0.5D), dz = s.z() - (p.z() + 0.5D);
      if (dx * dx + dz * dz <= 100D && Math.abs(s.y() - p.y()) <= 6D)
        return s;
    }
    throw new IllegalStateException("nearby spider type " + type + " absent");
  }
  private static void go(B173WireClient a, BlockPosition p) {
    step(a, p.x() + 0.5D, p.y() + 1.0D, p.z() - 1.5D);
  }
  private static void kill(B173WireClient a, RemoteMobSpawn spawn) {
    int entity = spawn.entityId();
    double x = spawn.x(), y = spawn.y(), z = spawn.z();
    for (int hit = 0; hit < 16; hit++) {
      step(a, x, y + 1.0D, z);
      if (B173ShearsAccess.peekDeath(a, entity) != null)
        break;
      strike(a, entity);
      if (B173ShearsAccess.peekDeath(a, entity) != null)
        break;
      RemoteMobMovement m = a.awaitMobMovement(entity);
      x = m.toX();
      y = m.toY();
      z = m.toZ();
    }
    RemoteMobDeath death = a.awaitMobDeath(entity);
    require(death.entityId() == entity && death.hurtObserved(), "spider death drift");
  }
  private static void step(B173WireClient a, double x, double y, double z) {
    for (int n = 0; n < 16; n++) {
      heal(a);
      PlayerPose here = a.moveAndObserve(0D, 0D, 0D, 1).resulting();
      double dx = x - here.x(), dy = y - here.y(), dz = z - here.z(),
             dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
      if (dist <= 2.5D)
        return;
      double s = Math.min(1D, 9.0D / dist);
      a.moveAndObserve(dx * s, dy * s, dz * s, 2);
    }
  }
  private static void strike(B173WireClient a, int entity) {
    heal(a);
    int sword = find(a.inventory(), 276);
    require(sword >= 36, "diamond sword lost");
    a.selectHeldSlot(sword - 36);
    a.attackMob(entity);
    worldline.test.WorldlineSmokeAwait.observe(a, 20);
    heal(a);
  }
  private static void heal(B173WireClient a) {
    int h = a.health();
    if (h == 0)
      throw new IllegalStateException("actor died during spider string");
    if (h >= 20)
      return;
    int food = find(a.inventory(), 320);
    if (food < 36)
      return;
    a.selectHeldSlot(food - 36);
    a.useSelectedItemInAir();
    worldline.test.WorldlineSmokeAwait.observe(a, 5);
  }
  private static int find(RemoteInventoryView view, int id) {
    for (int slot = 36; slot <= 44; slot++)
      if (!view.slot(slot).empty() && view.slot(slot).item().legacyId() == id)
        return slot;
    return -1;
  }
  private static void grass(B173WireClient a, BlockPosition support, BlockFace face)
      throws Exception {
    place(a, support, face, 2);
  }
  private static BlockPosition foundation(RemoteChunkSnapshot q, int cx, int cz) {
    for (int x = 4; x <= 11; x++)
      for (int z = 4; z <= 11; z++)
        for (int y = 126; y >= 1; y--)
          if (q.blockAt(x, y, z).legacyId() == 3 && water(q.blockAt(x, y + 1, z).legacyId()))
            return new BlockPosition(cx * 16 + x, y, cz * 16 + z);
    throw new IllegalStateException("no deterministic spider-string foundation");
  }
  private static String cell(BlockPosition p) {
    return p.x() + ":" + p.y() + ":" + p.z() + ":52:0";
  }
  private static void require(boolean v, String m) {
    if (!v)
      throw new IllegalStateException(m);
  }
}
