package worldline.smoke.difficultydamagesetb173;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Same type-54 zombie melee on Easy then Hard, hashing distinct Packet8 health deltas. */
public final class DifficultyDamageSetSmoke {
  private DifficultyDamageSetSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 7)
      throw new IllegalArgumentException(
          "usage: DifficultyDamageSetSmoke server.jar workspace port seed username chunkX chunkZ");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    int cx = Integer.parseInt(a[5]), cz = Integer.parseInt(a[6]);
    require(seed == 17320110707L && user.equals("DiffDmg467") && user.length() <= 16,
        "difficulty-damage-set identity drift");
    Hit easy = melee(jar, workspace.resolve("easy"), port, seed, user, cx, cz, 1);
    Hit hard = melee(jar, workspace.resolve("hard"), port, seed, user, cx, cz, 3);
    require(easy.hit.healthBefore() == 20 && hard.hit.healthBefore() == 20
            && easy.hit.healthAfter() == 18 && hard.hit.healthAfter() == 18
            && easy.hit.damage() == 2 && hard.hit.damage() == 2 && easy.column == hard.column,
        "easy/hard Packet8 health delta drift easy=" + easy.hit.damage()
            + " hard=" + hard.hit.damage());
    require(easy.mob == 54 && hard.mob == 54, "type54 melee Packet8 absent");
    String evidence = "column=" + easy.column + ",support=" + easy.cell
        + ",platform=7x7-48grass,mob=type54,difficulty=1+3,easy=20->18,hard=20->18,delta=2+2,armor=none,night=14000,heal=health20,clients=2,disconnect=clean";
    String trace = "v1|server=official-b1.7.3|seed=" + seed
        + "|fixture=raised-7x7-grass-platform+spawner52-zombie|cause=nbt-entityid-zombie+time-14000+difficulty-1-then-3|wire=packet24-type54+packet38-status2+packet8-easy-then-hard|oracle=difficulty-property-zombie-melee-not-armor-not-peaceful-not-door|"
        + evidence;
    System.out.println("WORLDLINE_M467_SET=" + evidence);
    System.out.println("WORLDLINE_M467_TRACE=" + trace);
    System.out.println("WORLDLINE_M467_SIGNATURE=" + sha(trace));
  }
  private static Hit melee(Path jar, Path workspace, int port, long seed, String user, int cx,
      int cz, int difficulty) throws Exception {
    Duration timeout = Duration.ofSeconds(180);
    B173DedicatedServer server =
        B173DedicatedServer.difficulty(jar, workspace, port, seed, timeout, difficulty);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout);
    BlockPosition top, spawner;
    int column;
    RemoteIncomingHit hit;
    int mob;
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1, 2, 3},
          new int[] {1, 2, 52, 322}, new int[] {32, 48, 1, 4}, new int[] {0, 0, 0, 0}, 20);
      actor.connect();
      actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() == 4 && actor.awaitHealth(20) == 20,
          "difficulty-damage inventory or health drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      top = foundation(initial, cx, cz);
      column = 0;
      actor.selectHeldSlot(0);
      while (
          water(initial.blockAt(local(top.x(), cx), top.y() + 1, local(top.z(), cz)).legacyId())) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        require(++column <= 15, "water column exceeded difficulty-damage fixture");
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
      actor.close();
      awaitPlayers(server, 0);
      server.save();
    } finally {
      actor.close();
      server.close();
    }
    Thread.sleep(1000L);
    B173SpawnerSeed.entity(workspace, spawner, "Zombie");
    B173PlayerSeed.writeInventory(workspace, user, top.x() + 0.5D, top.y() + 1.1D, top.z() - 1.5D,
        new int[] {0, 1, 2, 3}, new int[] {1, 2, 52, 322}, new int[] {32, 48, 1, 4},
        new int[] {0, 0, 0, 0}, 20);
    server = B173DedicatedServer.difficulty(jar, workspace, port, seed, timeout, difficulty);
    actor = new B173WireClient("127.0.0.1", port, user, timeout);
    try {
      server.boot();
      actor.connect();
      actor.synchronizePose();
      B173DifficultyDamage.station(actor, top);
      require(actor.awaitInventory().occupiedSlots() >= 1 && actor.awaitHealth(20) == 20,
          "difficulty-damage reload health drift");
      server.setTime(14000L);
      RemoteMobSpawn zombie = B173DifficultyDamage.near(actor, spawner);
      require(zombie.legacyType() == 54 && zombie.entityId() != actor.state().entityId()
              && zombie.legacyType() != 90,
          "zombie Packet24 type54 identity drift");
      mob = zombie.legacyType();
      hit = B173DifficultyDamage.strike(actor, zombie, top);
      require(hit.healthBefore() == 20 && hit.healthAfter() < 20
              && hit.damage() == 20 - hit.healthAfter(),
          "type54 Packet8 melee drift difficulty=" + difficulty + " after=" + hit.healthAfter());
      actor.close();
      awaitPlayers(server, 0);
      server.save();
    } finally {
      actor.close();
      server.close();
    }
    return new Hit(column, cell(top), hit, mob);
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
    throw new IllegalStateException("no deterministic difficulty-damage foundation");
  }
  private static String cell(BlockPosition p) {
    return p.x() + ":" + p.y() + ":" + p.z() + ":1:0";
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
  private static final class Hit {
    final int column, mob;
    final String cell;
    final RemoteIncomingHit hit;
    Hit(int c, String s, RemoteIncomingHit h, int m) {
      column = c;
      cell = s;
      hit = h;
      mob = m;
    }
  }
}
