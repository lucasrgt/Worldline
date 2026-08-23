package worldline.smoke.spiderclimbsetb173;
import static worldline.b173server.B173FixtureSupport.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Observes Packet24 type 52 climb Packet31/33/34 +Y on cobble 4 and oak plank 5 walls. */
public final class SpiderClimbSetSmoke {
  private SpiderClimbSetSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 7)
      throw new IllegalArgumentException(
          "usage: SpiderClimbSetSmoke server.jar workspace port seed username chunkX chunkZ");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    int cx = Integer.parseInt(a[5]), cz = Integer.parseInt(a[6]);
    require(seed == 17320110707L && user.equals("SpidClimb447") && user.length() <= 16,
        "spider-climb identity drift");
    Duration timeout = Duration.ofSeconds(240);
    B173DedicatedServer server =
        B173DedicatedServer.monsters(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout);
    BlockPosition top, cobble, planks, spawner;
    int column;
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1, 2, 3, 4, 5},
          new int[] {1, 2, 4, 5, 52, 85}, new int[] {32, 48, 32, 32, 1, 24},
          new int[] {0, 0, 0, 0, 0, 0});
      actor.connect();
      actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() == 6, "spider-climb inventory drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      top = foundation(initial, cx, cz);
      column = 0;
      actor.selectHeldSlot(0);
      while (
          water(initial.blockAt(local(top.x(), cx), top.y() + 1, local(top.z(), cz)).legacyId())) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        require(++column <= 15, "water column exceeded spider-climb fixture");
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
      arena(actor, top, 5);
      actor.selectHeldSlot(2);
      cobble = wall(actor, new BlockPosition(top.x() + 1, top.y(), top.z()), 4);
      actor.selectHeldSlot(3);
      planks = wall(actor, new BlockPosition(top.x() - 1, top.y(), top.z()), 5);
      actor.selectHeldSlot(4);
      spawner = place(actor, top, BlockFace.UP, 52);
      actor.sustainTicks(5);
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
      require(actor.awaitInventory().occupiedSlots() >= 1, "spider-climb reload inventory drift");
      server.setTime(14000L);
      RemoteMobSpawn first = arm(actor, spawner, top);
      cross(actor, cobble, spawner.x() + 0.5D, cobble.x() + 1.5D, spawner.y());
      RemoteMobMovement cobbleClimb = climb(actor, first.entityId(), cobble.x(), cobble.y() - 5.0D);
      require(cobbleClimb != null, "cobble 4 Packet31/33/34 +Y climb absent");
      cross(actor, planks, cobble.x() + 1.5D, planks.x() - 0.5D, spawner.y());
      RemoteMobMovement plankClimb = climb(actor, first.entityId(), planks.x(), planks.y() - 5.0D);
      if (plankClimb == null) {
        cross(actor, planks, planks.x() - 0.5D, spawner.x() + 0.5D, spawner.y());
        RemoteMobSpawn second = arm(actor, spawner, top);
        cross(actor, planks, spawner.x() + 0.5D, planks.x() - 0.5D, spawner.y());
        plankClimb = climb(actor, second.entityId(), planks.x(), planks.y() - 5.0D);
      }
      require(plankClimb != null, "oak plank 5 Packet31/33/34 +Y climb absent");
      require(cobbleClimb.toFixedY() > cobbleClimb.fromFixedY() && nearWall(cobbleClimb, cobble.x())
              && plankClimb.toFixedY() > plankClimb.fromFixedY()
              && nearWall(plankClimb, planks.x()),
          "spider climb wall adjacency drift");
      require((cobbleClimb.packetId() == 31 || cobbleClimb.packetId() == 33
                  || cobbleClimb.packetId() == 34)
              && (plankClimb.packetId() == 31 || plankClimb.packetId() == 33
                  || plankClimb.packetId() == 34),
          "spider climb packet drift");
      actor.close();
      awaitPlayers(server, 0);
      server.save();
      String evidence = "column=" + column + ",platform=7x7-48grass,arena=fence85-24,spawner="
          + cell(spawner) + ",entityid=Spider,mob=type52,night=14000,cobble=" + cell(cobble, 4)
          + ",planks=" + cell(planks, 5)
          + ",climb=packet31|33|34+positive-y+cobble4+planks5,clients=1,disconnect=clean";
      String trace = "v2|server=official-b1.7.3|seed=" + seed
          + "|fixture=raised-7x7-grass-platform+fence85-arena+cobble4-wall+oak-plank5-wall+spawner52|cause=nbt-entityid-spider+time-14000+bounded-mob-movement-poll|wire=packet24-type52+packet31-or33-or34-positive-y|oracle=spider-climb-cobble4-and-planks5-not-m409-string|"
          + evidence;
      System.out.println("WORLDLINE_M447_SET=" + evidence);
      System.out.println("WORLDLINE_M447_TRACE=" + trace);
      System.out.println("WORLDLINE_M447_SIGNATURE=" + sha(trace));
    } finally {
      actor.close();
      server.close();
    }
  }
  private static RemoteMobSpawn arm(B173WireClient a, BlockPosition spawner, BlockPosition top) {
    step(a, spawner.x() + 0.5D, spawner.y() + 1.0D, spawner.z() + 0.5D);
    RemoteMobSpawn spider = near(a, 52, spawner, top);
    require(spider.legacyType() == 52 && spider.entityId() != a.state().entityId()
            && spider.legacyType() != 90,
        "spider Packet24 identity drift");
    a.sustainTicks(20);
    return spider;
  }
  private static RemoteMobMovement climb(B173WireClient a, int entity, int wallX, double minY) {
    for (int window = 0; window < 120; window++) {
      a.sustainTicks(5);
      RemoteMobMovement m;
      while ((m = B173SpiderClimbAccess.poll(a, entity)) != null)
        if (m.toFixedY() > m.fromFixedY() && nearWall(m, wallX) && m.toY() >= minY)
          return m;
    }
    return null;
  }
  private static RemoteMobSpawn near(
      B173WireClient a, int type, BlockPosition p, BlockPosition top) {
    for (int n = 0; n < 32; n++) {
      RemoteMobSpawn s = a.awaitMobSpawn(type);
      double dx = s.x() - (top.x() + 0.5D), dz = s.z() - (top.z() + 0.5D);
      if (Math.abs(dx) <= 2.5D && Math.abs(dz) <= 2.5D && Math.abs(s.y() - p.y()) <= 2D)
        return s;
    }
    throw new IllegalStateException("arena-contained spider type " + type + " absent");
  }
  private static boolean nearWall(RemoteMobMovement m, int wallX) {
    return Math.abs(m.toX() - (wallX + 0.5D)) <= 1.5D;
  }
  private static BlockPosition wall(B173WireClient a, BlockPosition grass, int id)
      throws Exception {
    BlockPosition cell = place(a, grass, BlockFace.UP, id);
    place(a, cell, BlockFace.NORTH, id);
    place(a, cell, BlockFace.SOUTH, id);
    for (int h = 1; h < 8; h++) {
      a.moveAndObserve(0D, 1D, 0D, 1);
      cell = place(a, cell, BlockFace.UP, id);
    }
    down(a, 7);
    return cell;
  }
  private static void down(B173WireClient a, int n) {
    for (int i = 0; i < n; i++)
      a.moveAndObserve(0D, -1D, 0D, 1);
  }
  private static void cross(
      B173WireClient a, BlockPosition wall, double fromX, double toX, double y) {
    step(a, fromX, y, wall.z() + 2.5D);
    step(a, toX, y, wall.z() + 2.5D);
    step(a, toX, y, wall.z() + 0.5D);
  }
  private static void step(B173WireClient a, double x, double y, double z) {
    for (int n = 0; n < 32; n++) {
      PlayerPose here = a.moveAndObserve(0D, 0D, 0D, 1).resulting();
      double dx = x - here.x(), dz = z - here.z(), dist = Math.sqrt(dx * dx + dz * dz);
      if (dist <= 0.75D)
        return;
      double s = Math.min(1D, 9.0D / dist);
      a.moveAndObserve(dx * s, 0D, dz * s, 1);
    }
    throw new IllegalStateException("movement cap missed spider-climb target");
  }
  private static void arena(B173WireClient a, BlockPosition top, int slot) throws Exception {
    a.selectHeldSlot(slot);
    for (int x = -3; x <= 3; x++) {
      place(a, new BlockPosition(top.x() + x, top.y(), top.z() - 3), BlockFace.UP, 85);
      place(a, new BlockPosition(top.x() + x, top.y(), top.z() + 3), BlockFace.UP, 85);
    }
    for (int z = -2; z <= 2; z++) {
      place(a, new BlockPosition(top.x() - 3, top.y(), top.z() + z), BlockFace.UP, 85);
      place(a, new BlockPosition(top.x() + 3, top.y(), top.z() + z), BlockFace.UP, 85);
    }
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
    throw new IllegalStateException("no deterministic spider-climb foundation");
  }
  private static String cell(BlockPosition p) {
    return cell(p, 52);
  }
  private static String cell(BlockPosition p, int id) {
    return p.x() + ":" + p.y() + ":" + p.z() + ":" + id + ":0";
  }
  private static void require(boolean v, String m) {
    if (!v)
      throw new IllegalStateException(m);
  }
}
