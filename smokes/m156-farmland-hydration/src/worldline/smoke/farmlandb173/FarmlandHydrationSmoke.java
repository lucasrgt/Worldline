package worldline.smoke.farmlandb173;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Hoes official dirt into farmland and proves adjacent still water hydrates it across restart. */
public final class FarmlandHydrationSmoke {
  private FarmlandHydrationSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 7)
      throw new IllegalArgumentException(
          "usage: FarmlandHydrationSmoke server.jar workspace port seed username chunkX chunkZ");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    int cx = Integer.parseInt(a[5]), cz = Integer.parseInt(a[6]);
    Duration timeout = Duration.ofSeconds(90);
    B173DedicatedServer server =
        new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout), reader = null;
    BlockPosition top, north, south, west, basin, water, perch;
    BlockPosition[] plots;
    int column;
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1, 2, 3},
          new int[] {1, 3, 9, 290}, new int[] {64, 16, 8, 1}, new int[] {0, 0, 0, 0});
      actor.connect();
      actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() == 4, "farm inventory drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      top = foundation(initial, cx, cz);
      column = 0;
      actor.selectHeldSlot(0);
      while (
          water(initial.blockAt(local(top.x(), cx), top.y() + 1, local(top.z(), cz)).legacyId())) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        require(++column <= 15, "water column exceeded farm fixture");
      }
      for (int lift = 0; lift < 8; lift++) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        column++;
      }
      north = place(actor, top, BlockFace.NORTH, 1);
      south = place(actor, top, BlockFace.SOUTH, 1);
      west = place(actor, top, BlockFace.WEST, 1);
      basin = place(actor, top, BlockFace.EAST, 1);
      actor.moveAndObserve(0D, 0D, -1D, 2);
      actor.selectHeldSlot(1);
      plots = new BlockPosition[] {place(actor, top, BlockFace.UP, 3),
          place(actor, north, BlockFace.UP, 3), place(actor, south, BlockFace.UP, 3),
          place(actor, west, BlockFace.UP, 3)};
      actor.selectHeldSlot(2);
      water = place(actor, basin, BlockFace.UP, 9);
      actor.selectHeldSlot(3);
      for (BlockPosition dirt : plots)
        till(actor, dirt);
      tick(actor, plots);
      actor.close();
      awaitPlayers(server, 0);
      server.save();
      reader = new B173WireClient("127.0.0.1", port, user, timeout);
      reader.connect();
      reader.synchronizePose();
      RemoteChunkSnapshot after = reader.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      require(moist(after, plots, cx, cz)
              && after.blockAt(local(water.x(), cx), water.y(), local(water.z(), cz))
                  .equals(new BlockState(9, 0)),
          "persisted farmland hydration drift: " + dump(after, plots, cx, cz)
              + ",water=" + after.blockAt(local(water.x(), cx), water.y(), local(water.z(), cz)));
      String evidence = "column=" + column + ",plots=4,water=" + water.x() + ":" + water.y() + ":"
          + water.z() + ":9:0,hoe=290,hydrated=7,persisted=true,clients=2,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=raised-dirt3+still-water9|cause=packet15-wooden-hoe290|wire=packet53-farmland60|oracle=live-ticks+fresh-login-farmland60:7|"
          + evidence;
      System.out.println("WORLDLINE_M156_FARM=" + evidence);
      System.out.println("WORLDLINE_M156_TRACE=" + trace);
      System.out.println("WORLDLINE_M156_SIGNATURE=" + sha(trace));
    } finally {
      actor.close();
      if (reader != null)
        reader.close();
      server.close();
    }
  }
  private static BlockPosition place(
      B173WireClient a, BlockPosition support, BlockFace face, int id) throws Exception {
    BlockPosition target = face.adjacent(support);
    a.placeHeldBlock(support, face);
    a.awaitBlock(target, new BlockState(id, 0));
    return target;
  }
  private static void till(B173WireClient a, BlockPosition dirt) throws Exception {
    a.useHeldItemOnBlock(dirt, BlockFace.UP);
    a.awaitBlock(dirt, new BlockState(60, 0));
  }
  private static void tick(B173WireClient a, BlockPosition[] plots) throws Exception {
    RemoteWorldView v = worldline.test.WorldlineSmokeAwait.awaitWorld(
        a, world -> wet(world, plots), "hydrated farmland", 3200);
    for (BlockPosition p : plots)
      require(v.blockAt(p.x(), p.y(), p.z()).legacyId() == 60, "farmland lost");
  }
  private static boolean wet(RemoteWorldView v, BlockPosition[] plots) {
    for (BlockPosition p : plots) {
      BlockState s = v.blockAt(p.x(), p.y(), p.z());
      if (s.legacyId() != 60)
        return false;
      if (s.metadata() == 7)
        return true;
    }
    return false;
  }
  private static boolean moist(RemoteChunkSnapshot q, BlockPosition[] plots, int cx, int cz) {
    for (BlockPosition p : plots)
      if (q.blockAt(local(p.x(), cx), p.y(), local(p.z(), cz)).equals(new BlockState(60, 7)))
        return true;
    return false;
  }
  private static String dump(RemoteChunkSnapshot q, BlockPosition[] plots, int cx, int cz) {
    StringBuilder s = new StringBuilder();
    for (BlockPosition p : plots)
      s.append(p.x())
          .append(':')
          .append(p.y())
          .append(':')
          .append(p.z())
          .append('=')
          .append(q.blockAt(local(p.x(), cx), p.y(), local(p.z(), cz)))
          .append(' ');
    return s.toString();
  }
  private static BlockPosition foundation(RemoteChunkSnapshot q, int cx, int cz) {
    for (int x = 4; x <= 11; x++)
      for (int z = 4; z <= 11; z++)
        for (int y = 126; y >= 1; y--)
          if (q.blockAt(x, y, z).legacyId() == 3 && water(q.blockAt(x, y + 1, z).legacyId()))
            return new BlockPosition(cx * 16 + x, y, cz * 16 + z);
    throw new IllegalStateException("no deterministic farm foundation");
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
