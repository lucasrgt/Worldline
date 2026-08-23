package worldline.smoke.slimetouchsetb173;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Places a slime-chunk spawner below y=16 and requires Packet24 type 55 contact Packet8. */
public final class SlimeTouchSetSmoke {
  private static final int[] SLOTS = {0, 1, 2}, IDS = {1, 52, 320}, COUNTS = {32, 1, 16},
                             DMG = {0, 0, 0};
  private SlimeTouchSetSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 7)
      throw new IllegalArgumentException(
          "usage: SlimeTouchSetSmoke server.jar workspace port seed username chunkX chunkZ");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    int cx = Integer.parseInt(a[5]), cz = Integer.parseInt(a[6]);
    require(user.length() <= 16 && user.equals("SlimeTouch458")
            && B173SlimeTouchAccess.slimeChunk(seed, cx, cz),
        "slime-touch identity or slime-chunk drift");
    Duration timeout = Duration.ofSeconds(360);
    B173DedicatedServer server =
        B173DedicatedServer.monsters(jar, workspace, port, seed, timeout, 5, true);
    B173WireClient scout = new B173WireClient("127.0.0.1", port, user, timeout),
                   actor = new B173WireClient("127.0.0.1", port, user, timeout);
    BlockPosition room, spawner;
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, SLOTS, IDS, COUNTS, DMG);
      scout.connect();
      scout.synchronizePose();
      require(scout.awaitInventory().occupiedSlots() == 3, "slime-touch scout inventory drift");
      scout.awaitRemoteChunk(0, 0);
      RemoteChunkSnapshot chunk = B173SlimeTouchAccess.waitChunk(scout, cx, cz).chunkAt(cx, cz);
      room = pocket(chunk, cx, cz);
      require(room != null && room.y() < 16, "slime-chunk cave below y=16 absent");
      scout.close();
      awaitPlayers(server, 0);
      B173PlayerSeed.writeInventory(
          workspace, user, room.x() + 1.5D, room.y(), room.z() + 0.5D, SLOTS, IDS, COUNTS, DMG);
      actor.connect();
      actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() == 3, "slime-touch actor inventory drift");
      B173SlimeTouchAccess.waitChunk(actor, cx, cz);
      B173SlimeTouchAccess.go(actor, room.x() + 0.5D, room.y(), room.z() + 0.5D);
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
      require(actor.awaitInventory().occupiedSlots() >= 1, "slime-touch reload inventory drift");
      B173SlimeTouchAccess.waitChunk(actor, cx, cz);
      B173SlimeTouchAccess.go(actor, room.x() + 0.5D, room.y(), room.z() + 0.5D);
      boolean tiny = false, large = false, visible = false;
      RemoteIncomingHit hit = null;
      int hunts = 0;
      while (hit == null || (visible && !(tiny && large))) {
        require(++hunts <= 16, "official type-55 Packet8 contact absent after bounded hunts");
        RemoteMobSpawn slime = B173SlimeTouchAccess.huntNear(
            actor, 240, spawner.x() + 0.5D, spawner.y() + 1, spawner.z() + 0.5D);
        require(slime.legacyType() == 55 && slime.entityId() != actor.state().entityId()
                && slime.y() < 16D,
            "slime Packet24 type 55 identity drift");
        int size = B173SlimeTouchAccess.size(actor, slime);
        if (size == 1) {
          tiny = true;
          visible = true;
        }
        if (size >= 2) {
          large = true;
          visible = true;
        }
        if (hit == null)
          hit = B173SlimeTouchAccess.touch(actor, slime);
      }
      require(hit != null && hit.damage() >= 1 && hit.healthAfter() < hit.healthBefore(),
          "slime Packet8 contact damage absent");
      require(!visible || (tiny && large), "visible slime size family collapsed");
      String family = visible ? "size=1+larger" : "size=hidden";
      actor.close();
      awaitPlayers(server, 0);
      server.save();
      String evidence = "chunk=" + cx + ":" + cz + ",room=" + room.x() + ":" + room.y() + ":"
          + room.z() + ",spawner=" + spawner.x() + ":" + spawner.y() + ":" + spawner.z()
          + ":52:0,entityid=Slime,lowy=true,mobs=type55," + family
          + ",touch=packet8,status=2,hunts<=16,clients=3,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=slime-chunk-y<16-spawner52|cause=nbt-entityid-slime+move-into-type55-aabb|wire=packet24-type55+packet38-status2-before-packet8|oracle=slime-contact-damage-not-split-not-slimeball|"
          + evidence;
      System.out.println("WORLDLINE_M458_SET=" + evidence);
      System.out.println("WORLDLINE_M458_TRACE=" + trace);
      System.out.println("WORLDLINE_M458_SIGNATURE=" + sha(trace));
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
  private static BlockPosition place(
      B173WireClient a, BlockPosition support, BlockFace face, int id) {
    BlockPosition target = face.adjacent(support);
    BlockState expected = new BlockState(id, 0);
    for (int attempt = 0; attempt < 4; attempt++) {
      a.placeHeldBlock(support, face);
      if (worldline.test.WorldlineSmokeAwait.awaitBlockOrNull(a, target, expected, 20) != null)
        return target;
    }
    throw new IllegalStateException("bounded authoritative spawner placement absent");
  }
  private static boolean solid(int id) {
    return id == 1 || id == 2 || id == 3 || id == 4 || id == 12 || id == 13 || id == 24;
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
