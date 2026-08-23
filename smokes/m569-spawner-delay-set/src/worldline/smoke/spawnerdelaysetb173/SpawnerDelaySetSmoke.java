package worldline.smoke.spawnerdelaysetb173;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Places one official spawner, retargets Zombie Delay=1, and proves far-range Packet24 absence versus in-range spawn. */
public final class SpawnerDelaySetSmoke {
  private SpawnerDelaySetSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 7)
      throw new IllegalArgumentException(
          "usage: SpawnerDelaySetSmoke server.jar workspace port seed username chunkX chunkZ");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    int cx = Integer.parseInt(a[5]), cz = Integer.parseInt(a[6]);
    Duration timeout = Duration.ofSeconds(180);
    require(seed == 17320110707L && user.equals("SpawnDly569") && user.length() <= 16,
        "spawner-delay-set identity drift");
    B173DedicatedServer server =
        B173DedicatedServer.monsters(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout);
    BlockPosition top, spawner;
    int column;
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1, 2},
          new int[] {1, 2, 52}, new int[] {32, 48, 1}, new int[] {0, 0, 0});
      actor.connect();
      actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() == 3, "spawner-delay inventory drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      top = foundation(initial, cx, cz);
      column = 0;
      actor.selectHeldSlot(0);
      while (
          water(initial.blockAt(local(top.x(), cx), top.y() + 1, local(top.z(), cz)).legacyId())) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        require(++column <= 15, "water column exceeded spawner-delay fixture");
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
      actor.selectHeldSlot(2);
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
    B173SpawnerSeed.entity(workspace, spawner, "Zombie");
    B173SpawnerDelay.delay(workspace, spawner, (short) 1);
    B173PlayerSeed.writeInventory(workspace, user, top.x() + 0.5D, top.y() + 24.1D, top.z() + 0.5D,
        new int[] {0, 1, 2}, new int[] {1, 2, 52}, new int[] {32, 48, 1}, new int[] {0, 0, 0});
    server = B173DedicatedServer.monsters(jar, workspace, port, seed, timeout, 3, true);
    actor = new B173WireClient("127.0.0.1", port, user, timeout);
    try {
      server.boot();
      actor.connect();
      PlayerPose far = actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() >= 1, "spawner-delay reload inventory drift");
      require(range(far, spawner) >= 20D, "far pose drift dist=" + range(far, spawner));
      server.setTime(14000L);
      station(actor, top.x() + 0.5D, top.y() + 24.1D, top.z() + 0.5D);
      worldline.test.WorldlineSmokeAwait.observe(actor, 40);
      require(drainNear(actor, spawner) == null,
          "Packet24 present while player far outside 16-block activation");
      station(actor, top.x() + 0.5D, top.y() + 1.1D, top.z() - 1.5D);
      require(range(actor.moveAndObserve(0D, 0D, 0D, 1).resulting(), spawner) < 16D,
          "near pose still outside activation");
      RemoteMobSpawn zombie = awaitNear(actor, spawner);
      require(zombie.legacyType() == 54 && zombie.entityId() != actor.state().entityId()
              && zombie.legacyType() != 90 && zombie.legacyType() != 50
              && zombie.legacyType() != 52,
          "spawner-delay Packet24 identity drift");
      actor.close();
      awaitPlayers(server, 0);
      server.save();
      String evidence = "column=" + column + ",platform=7x7-48grass,spawner=" + cell(spawner)
          + ",entityid=Zombie,delay=1,far=24,wait=40,absent=true,near=type54,night=14000,clients=2,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=raised-7x7-grass-platform+spawner52|cause=nbt-entityid-zombie+nbt-delay-1+time-14000+range-24-above-then-near|wire=packet24-type54-absent-far+packet24-type54-near|oracle=spawner-delay-range-not-pig-not-creeper-spider-not-light|"
          + evidence;
      System.out.println("WORLDLINE_M569_DELAY=" + evidence);
      System.out.println("WORLDLINE_M569_TRACE=" + trace);
      System.out.println("WORLDLINE_M569_SIGNATURE=" + sha(trace));
    } finally {
      actor.close();
      server.close();
    }
  }
  private static RemoteMobSpawn drainNear(B173WireClient a, BlockPosition p) {
    RemoteMobSpawn hit = null;
    for (int n = 0; n < 32; n++) {
      RemoteMobSpawn s = B173SpawnerDelay.peek(a, 54);
      if (s == null)
        break;
      s = a.awaitMobSpawn(54);
      if (inside(s, p))
        hit = s;
    }
    return hit;
  }
  private static RemoteMobSpawn awaitNear(B173WireClient a, BlockPosition p) {
    RemoteMobSpawn hit = drainNear(a, p);
    if (hit != null)
      return hit;
    for (int n = 0; n < 32; n++) {
      RemoteMobSpawn s = a.awaitMobSpawn(54);
      if (inside(s, p))
        return s;
    }
    throw new IllegalStateException("nearby type54 absent after entering range");
  }
  private static boolean inside(RemoteMobSpawn s, BlockPosition p) {
    double dx = s.x() - (p.x() + 0.5D), dz = s.z() - (p.z() + 0.5D);
    return dx * dx + dz * dz <= 20.25D && Math.abs(s.y() - p.y()) <= 1.1D;
  }
  private static double range(PlayerPose p, BlockPosition b) {
    double dx = p.x() - (b.x() + 0.5D), dy = p.y() - (b.y() + 0.5D), dz = p.z() - (b.z() + 0.5D);
    return Math.sqrt(dx * dx + dy * dy + dz * dz);
  }
  private static void station(B173WireClient a, double x, double y, double z) {
    for (int n = 0; n < 16; n++) {
      PlayerPose here = a.moveAndObserve(0D, 0D, 0D, 1).resulting();
      double dx = x - here.x(), dy = y - here.y(), dz = z - here.z();
      double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
      if (dist <= 1D)
        return;
      double s = Math.min(1D, 8D / dist);
      a.moveAndObserve(dx * s, dy * s, dz * s, 4);
    }
    PlayerPose here = a.moveAndObserve(0D, 0D, 0D, 1).resulting();
    require((x - here.x()) * (x - here.x()) + (z - here.z()) * (z - here.z()) <= 4D
            && Math.abs(y - here.y()) <= 3D,
        "station pose drift x=" + here.x() + " y=" + here.y() + " z=" + here.z());
  }
  private static BlockPosition place(
      B173WireClient a, BlockPosition support, BlockFace face, int id) throws Exception {
    BlockPosition target = face.adjacent(support);
    a.placeHeldBlock(support, face);
    a.awaitBlock(target, new BlockState(id, 0));
    return target;
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
    throw new IllegalStateException("no deterministic spawner-delay foundation");
  }
  private static String cell(BlockPosition p) {
    return p.x() + ":" + p.y() + ":" + p.z() + ":52:0";
  }
  private static boolean water(int id) {
    return id == 8 || id == 9;
  }
  private static int local(int v, int c) {
    return v - c * 16;
  }
  private static void awaitPlayers(B173DedicatedServer s, int n) throws Exception {
    long e = System.currentTimeMillis() + 5000;
    while (System.currentTimeMillis() < e) {
      if (s.players().size() == n)
        return;
      Thread.sleep(100);
    }
    throw new IllegalStateException("player count drift");
  }
  private static String sha(String s) throws Exception {
    byte[] b = MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));
    StringBuilder v = new StringBuilder();
    for (byte x : b)
      v.append(String.format("%02x", x & 255));
    return v.toString();
  }
  private static void require(boolean v, String m) {
    if (!v)
      throw new IllegalStateException(m);
  }
}
