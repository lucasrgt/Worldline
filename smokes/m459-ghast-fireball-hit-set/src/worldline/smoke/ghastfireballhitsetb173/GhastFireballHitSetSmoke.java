package worldline.smoke.ghastfireballhitsetb173;
import static worldline.b173server.B173FixtureSupport.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Nether ghast Packet24 56 plus Packet23 type 63, then Packet8 hurt and/or netherrack/cobble crater. */
public final class GhastFireballHitSetSmoke {
  private GhastFireballHitSetSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 7)
      throw new IllegalArgumentException(
          "usage: GhastFireballHitSetSmoke server.jar workspace port seed username chunkX chunkZ");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    int cx = Integer.parseInt(a[5]), cz = Integer.parseInt(a[6]);
    Duration timeout = Duration.ofSeconds(180);
    require(user.length() <= 16 && user.equals("GhastHit459") && seed == 17320110707L,
        "username or seed drift");
    B173DedicatedServer server =
        B173DedicatedServer.netherMonsters(jar, workspace, port, seed, timeout);
    B173WireClient scout = new B173WireClient("127.0.0.1", port, user, timeout),
                   actor = new B173WireClient("127.0.0.1", port, user, timeout);
    BlockPosition land, top, spawner;
    int pads, cobbles, clients = 2;
    try {
      server.boot();
      B173PlayerSeed.writeDimension(workspace, user, 4.5D, 64D, 4.5D, -1);
      scout.connect();
      scout.synchronizePose();
      require(scout.dimension() == -1 && scout.awaitDimension(-1) == -1,
          "nether scout dimension drift");
      RemoteChunkSnapshot initial = scout.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      require(count(initial, 87) > 0 && sky(initial) == 0, "nether terrain identity drift");
      land = landing(initial, cx, cz);
      scout.close();
      awaitPlayers(server, 0);
      B173PlayerSeed.writeInventory(workspace, user, land.x() + 0.5D, land.y() + 1.0D,
          land.z() + 0.5D, -1, new int[] {0, 1, 2}, new int[] {87, 4, 52}, new int[] {64, 64, 1},
          new int[] {0, 0, 0});
      actor.connect();
      require(actor.synchronizePose() != null && actor.dimension() == -1
              && actor.awaitInventory().occupiedSlots() == 3,
          "nether ghast-hit inventory or dimension drift");
      int cavernCx = cx + 2, cavernCz = cz - 1;
      top = cavern(load(actor, land, cavernCx, cavernCz), cavernCx, cavernCz);
      if (top.x() != land.x() || top.y() != land.y() || top.z() != land.z()) {
        actor.close();
        awaitPlayers(server, 0);
        B173PlayerSeed.writeInventory(workspace, user, top.x() + 0.5D, top.y() + 1.0D,
            top.z() + 0.5D, -1, new int[] {0, 1, 2}, new int[] {87, 4, 52}, new int[] {64, 64, 1},
            new int[] {0, 0, 0});
        actor = new B173WireClient("127.0.0.1", port, user, timeout);
        actor.connect();
        require(actor.synchronizePose() != null && actor.dimension() == -1
                && actor.awaitInventory().occupiedSlots() == 3,
            "nether cavern relog drift");
        clients++;
      }
      RemoteWorldView live =
          actor.awaitRemoteChunk(Math.floorDiv(top.x(), 16), Math.floorDiv(top.z(), 16));
      int[] placed = pad(actor, live, top);
      pads = placed[0];
      cobbles = placed[1];
      live = actor.sustainTicks(1);
      actor.look(0F, 60F);
      actor.selectHeldSlot(2);
      spawner = place(actor, site(live, top), BlockFace.UP, 52);
      actor.close();
      awaitPlayers(server, 0);
      server.save();
    } finally {
      scout.close();
      actor.close();
      server.close();
    }
    Thread.sleep(1000L);
    B173SpawnerSeed.ghast(workspace, spawner);
    server = B173DedicatedServer.netherMonsters(jar, workspace, port, seed, timeout);
    actor = new B173WireClient("127.0.0.1", port, user, timeout);
    try {
      server.boot();
      actor.connect();
      require(actor.synchronizePose() != null && actor.dimension() == -1,
          "nether ghast-hit reload dimension drift");
      RemoteWorldView before =
          actor.awaitRemoteChunk(Math.floorDiv(top.x(), 16), Math.floorDiv(top.z(), 16));
      require(actor.awaitHealth(20) == 20 && !lavaAt(before, top), "pre-hit Packet8 or lava drift");
      actor.look(0F, 0F);
      actor.moveAndObserve(0D, 0D, 0D, 2);
      RemoteMobSpawn ghast = actor.awaitMobSpawn(56);
      require(ghast.legacyType() == 56 && ghast.entityId() != actor.state().entityId(),
          "ghast Packet24 identity drift");
      RemoteObjectSpawn ball = B173GhastFireballHit.fireball(actor, ghast);
      B173GhastFireballHit.awaitImpact(actor, before, top);
      actor.close();
      awaitPlayers(server, 0);
      server.save();
      String evidence = "dimension=-1,support=" + top.x() + ":" + top.y() + ":" + top.z()
          + ":87,pads=" + pads + ",cobble-pads=" + cobbles + ",spawner=" + spawner.x() + ":"
          + spawner.y() + ":" + spawner.z() + ":52:0,entityid=Ghast,ghast=type56,fireball=type"
          + ball.type()
          + ",thrower=ghast,packet60=strength1,hit=packet8-or-crater,not-m410-spawn-only,not-m411-pork,packet23-known=absent,clients="
          + clients + ",disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|profile=allow-nether-true+spawn-monsters-true|entry=prelogin-player-nbt-dimension-minus-one+item87+item4+item52|fixture=nether-netherrack87-platform+cobble4-pad+spawner52-ghast|cause=nbt-entityid-ghast+ghast-los+fireball-impact|wire=packet24-type56+packet23-type"
          + ball.type()
          + "-thrower-ghast+packet60-strength1+packet8-or-crater|oracle=nether-ghast-fireball-hit-not-m410-spawn-only-not-m411-pork|"
          + evidence;
      System.out.println("WORLDLINE_M459_HIT=" + evidence);
      System.out.println("WORLDLINE_M459_TRACE=" + trace);
      System.out.println("WORLDLINE_M459_SIGNATURE=" + sha(trace));
    } finally {
      actor.close();
      server.close();
    }
  }
  private static BlockPosition site(RemoteWorldView v, BlockPosition t) {
    BlockFace[] faces = {BlockFace.EAST, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.WEST};
    for (int i = 0; i < faces.length; i++) {
      BlockPosition s = faces[i].adjacent(t);
      int ccx = Math.floorDiv(s.x(), 16), ccz = Math.floorDiv(s.z(), 16);
      if (!v.containsChunk(ccx, ccz))
        continue;
      if (!air(v.blockAt(s.x(), s.y(), s.z()).legacyId())
          && !lava(v.blockAt(s.x(), s.y(), s.z()).legacyId())
          && air(v.blockAt(s.x(), s.y() + 1, s.z()).legacyId())
          && !lava(v.blockAt(s.x(), s.y() + 1, s.z()).legacyId()))
        return s;
    }
    throw new IllegalStateException("no adjacent netherrack spawner support");
  }
  private static RemoteWorldView load(
      B173WireClient a, BlockPosition land, int targetCx, int targetCz) {
    int lcx = Math.floorDiv(land.x(), 16), lcz = Math.floorDiv(land.z(), 16);
    a.awaitRemoteChunk(lcx, lcz);
    a.moveAndObserve(0D, 0D, 0D, 20);
    return a.awaitRemoteChunk(targetCx, targetCz);
  }
  private static int[] pad(B173WireClient a, RemoteWorldView v, BlockPosition t) throws Exception {
    int n = 0, c = 0;
    BlockFace[] faces = {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};
    for (int i = 0; i < faces.length; i++) {
      int id = c == 0 ? 4 : 87;
      a.selectHeldSlot(c == 0 ? 1 : 0);
      int added = fill(a, v, t, faces[i], id);
      if (id == 4)
        c += added;
      else
        n += added;
    }
    return new int[] {n, c};
  }
  private static int fill(B173WireClient a, RemoteWorldView v, BlockPosition t, BlockFace f, int id)
      throws Exception {
    BlockPosition p = f.adjacent(t);
    int ccx = Math.floorDiv(p.x(), 16), ccz = Math.floorDiv(p.z(), 16);
    if (!v.containsChunk(ccx, ccz) || !air(v.blockAt(p.x(), p.y(), p.z()).legacyId())
        || lava(v.blockAt(p.x(), p.y(), p.z()).legacyId())
        || lava(v.blockAt(p.x(), p.y() + 1, p.z()).legacyId()))
      return 0;
    a.placeHeldBlock(t, f);
    a.awaitBlock(p, new BlockState(id, 0));
    return 1;
  }
  private static BlockPosition landing(RemoteChunkSnapshot q, int cx, int cz) {
    for (int x = 1; x <= 12; x++)
      for (int z = 1; z <= 12; z++)
        for (int y = 126; y >= 1; y--)
          if (q.blockAt(x, y, z).legacyId() == 87 && air(q.blockAt(x, y + 1, z).legacyId())
              && !lava(q.blockAt(x, y + 1, z).legacyId())
              && !lava(q.blockAt(x, y + 2, z).legacyId()))
            return new BlockPosition(cx * 16 + x, y, cz * 16 + z);
    throw new IllegalStateException("no deterministic nether landing");
  }
  private static BlockPosition cavern(RemoteWorldView view, int targetCx, int targetCz) {
    BlockPosition best = null;
    int bestRun = -1;
    for (RemoteChunkSnapshot q : view.chunks()) {
      int ccx = q.observation().x() >> 4, ccz = q.observation().z() >> 4;
      if (ccx != targetCx || ccz != targetCz)
        continue;
      for (int x = 2; x <= 13; x++)
        for (int z = 2; z <= 13; z++) {
          int y = 1;
          while (y < 128) {
            if (!air(q.blockAt(x, y, z).legacyId())) {
              y++;
              continue;
            }
            int start = y;
            while (y < 128 && air(q.blockAt(x, y, z).legacyId())
                && !lava(q.blockAt(x, y, z).legacyId()))
              y++;
            int run = y - start, floor = start - 1;
            if (floor >= 1 && q.blockAt(x, floor, z).legacyId() == 87
                && !lava(q.blockAt(x, floor, z).legacyId()) && run > bestRun) {
              bestRun = run;
              best = new BlockPosition(ccx * 16 + x, floor, ccz * 16 + z);
            }
          }
        }
    }
    if (best == null || bestRun < 6)
      throw new IllegalStateException("no deterministic nether ghast cavern run=" + bestRun
          + " chunk=" + targetCx + ":" + targetCz);
    return best;
  }
  private static int cratered(RemoteWorldView before, RemoteExplosion e, int id) {
    int n = 0;
    for (BlockPosition p : e.destroyed()) {
      if (!before.containsChunk(Math.floorDiv(p.x(), 16), Math.floorDiv(p.z(), 16)))
        continue;
      if (before.blockAt(p.x(), p.y(), p.z()).legacyId() == id)
        n++;
    }
    return n;
  }
  private static int nearby(RemoteExplosion e, BlockPosition t) {
    int n = 0;
    for (BlockPosition p : e.destroyed())
      if (Math.abs(p.x() - t.x()) <= 8 && Math.abs(p.y() - t.y()) <= 8
          && Math.abs(p.z() - t.z()) <= 8)
        n++;
    return n;
  }
  private static boolean lavaAt(RemoteWorldView v, BlockPosition t) {
    for (int dy = 0; dy <= 2; dy++) {
      if (!v.containsChunk(Math.floorDiv(t.x(), 16), Math.floorDiv(t.z(), 16)))
        continue;
      if (lava(v.blockAt(t.x(), t.y() + dy, t.z()).legacyId()))
        return true;
    }
    return false;
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
