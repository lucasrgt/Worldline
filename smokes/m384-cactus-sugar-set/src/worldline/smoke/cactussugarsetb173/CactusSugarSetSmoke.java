package worldline.smoke.cactussugarsetb173;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Places cactus 81 on sand and sugar cane 83 on grass beside water, then waits both to height 2. */
public final class CactusSugarSetSmoke {
  private CactusSugarSetSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 9)
      throw new IllegalArgumentException(
          "usage: CactusSugarSetSmoke server.jar workspace port seed username chunkX chunkZ windowTicks growthWindows");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    int cx = Integer.parseInt(a[5]), cz = Integer.parseInt(a[6]), window = Integer.parseInt(a[7]),
        windows = Integer.parseInt(a[8]);
    require(seed == 17320110707L && user.equals("CactSugar384") && user.length() <= 16
            && window >= 1 && windows >= 1,
        "cactus-sugar-set identity drift");
    Duration timeout = Duration.ofMinutes(25);
    B173DedicatedServer server =
        new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout), reader = null;
    BlockPosition top, west, west2, south, south2, east, se, se2, west2s2, sand1, sand2, cactus,
        cactus2, cactusTop, cactus2Top, grass1, grass2, grass3, water1, cane, cane2, cane3, caneTop,
        cane2Top, cane3Top;
    int column, caneH, cactH;
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1, 2, 3, 4, 5},
          new int[] {1, 2, 9, 12, 81, 338}, new int[] {64, 16, 8, 8, 8, 8},
          new int[] {0, 0, 0, 0, 0, 0});
      actor.connect();
      actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() == 6, "cactus-sugar-set inventory drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      top = foundation(initial, cx, cz);
      column = 0;
      actor.selectHeldSlot(0);
      while (
          water(initial.blockAt(local(top.x(), cx), top.y() + 1, local(top.z(), cz)).legacyId())) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        require(++column <= 15, "water column exceeded cactus-sugar-set fixture");
      }
      for (int lift = 0; lift < 8; lift++) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        column++;
      }
      west = place(actor, top, BlockFace.WEST, 1);
      west2 = place(actor, west, BlockFace.WEST, 1);
      south = place(actor, top, BlockFace.SOUTH, 1);
      south2 = place(actor, south, BlockFace.SOUTH, 1);
      east = place(actor, top, BlockFace.EAST, 1);
      se = place(actor, east, BlockFace.SOUTH, 1);
      se2 = place(actor, se, BlockFace.SOUTH, 1);
      west2s2 = place(actor, place(actor, west2, BlockFace.SOUTH, 1), BlockFace.SOUTH, 1);
      actor.selectHeldSlot(3);
      sand1 = place(actor, west2, BlockFace.UP, 12);
      sand2 = place(actor, west2s2, BlockFace.UP, 12);
      actor.selectHeldSlot(4);
      cactus = place(actor, sand1, BlockFace.UP, 81);
      cactus2 = place(actor, sand2, BlockFace.UP, 81);
      cactusTop = BlockFace.UP.adjacent(cactus);
      cactus2Top = BlockFace.UP.adjacent(cactus2);
      require(worldline.test.WorldlineSmokeAwait.observe(actor, 5)
                      .blockAt(cactus.x(), cactus.y(), cactus.z())
                      .legacyId()
                  == 81
              && worldline.test.WorldlineSmokeAwait.observe(actor, 1)
                      .blockAt(cactus2.x(), cactus2.y(), cactus2.z())
                      .legacyId()
                  == 81,
          "isolated cactus popped");
      actor.selectHeldSlot(1);
      grass1 = place(actor, top, BlockFace.UP, 2);
      grass2 = place(actor, south, BlockFace.UP, 2);
      grass3 = place(actor, south2, BlockFace.UP, 2);
      actor.selectHeldSlot(2);
      water1 = place(actor, east, BlockFace.UP, 9);
      place(actor, se, BlockFace.UP, 9);
      place(actor, se2, BlockFace.UP, 9);
      require(worldline.test.WorldlineSmokeAwait.observe(actor, 5)
                  .blockAt(water1.x(), water1.y(), water1.z())
                  .equals(new BlockState(9, 0)),
          "still water 9 drift");
      actor.selectHeldSlot(5);
      cane = reed(actor, grass1);
      cane2 = reed(actor, grass2);
      cane3 = reed(actor, grass3);
      caneTop = BlockFace.UP.adjacent(cane);
      cane2Top = BlockFace.UP.adjacent(cane2);
      cane3Top = BlockFace.UP.adjacent(cane3);
      RemoteWorldView grown = worldline.test.WorldlineSmokeAwait.awaitWorld(actor,
          v
          -> (id(v, caneTop) == 83 || id(v, cane2Top) == 83 || id(v, cane3Top) == 83)
              && (id(v, cactusTop) == 81 || id(v, cactus2Top) == 81),
          "cactus and sugar cane growth", windows * window);
      caneH = 2;
      cactH = 2;
      require(id(grown, cane) == 83 && id(grown, cactus) == 81,
          "live cactus-sugar-set vanished during wait");
      actor.close();
      awaitPlayers(server, 0);
      server.save();
      reader = new B173WireClient("127.0.0.1", port, user, timeout);
      reader.connect();
      reader.synchronizePose();
      reader.awaitInventory();
      reader.awaitRemoteChunk(cx, cz);
      RemoteChunkSnapshot after =
          worldline.test.WorldlineSmokeAwait.observe(reader, 5).chunkAt(cx, cz);
      require(at(after, sand1, cx, cz).equals(new BlockState(12, 0))
              && at(after, grass1, cx, cz).equals(new BlockState(2, 0))
              && water(at(after, water1, cx, cz).legacyId())
              && at(after, cactus, cx, cz).legacyId() == 81
              && (at(after, cactusTop, cx, cz).legacyId() == 81
                  || at(after, cactus2Top, cx, cz).legacyId() == 81)
              && at(after, cane, cx, cz).legacyId() == 83
              && (at(after, caneTop, cx, cz).legacyId() == 83
                  || at(after, cane2Top, cx, cz).legacyId() == 83
                  || at(after, cane3Top, cx, cz).legacyId() == 83),
          "persisted cactus-sugar-set drift: "
              + dump(after,
                  new BlockPosition[] {sand1, grass1, water1, cactus, cactusTop, cactus2Top, cane,
                      caneTop, cane2Top, cane3Top},
                  cx, cz));
      String evidence = "column=" + column + ",sand=" + cell(sand1) + ":12:0,cactus=" + cell(cactus)
          + ":81,cactus-height>=2,grass=" + cell(grass1) + ":2:0,water=" + cell(water1)
          + ":9:0,cane=" + cell(cane)
          + ":83,cane-height>=2,plants=81+83,persisted=true,clients=2,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=raised-sand12+grass2+still-water9|cause=packet15-item81-cactus+item338-reed|wire=packet53-cactus81+reed83|oracle=official-random-tick-height>=2+fresh-login|"
          + evidence;
      System.out.println("WORLDLINE_M384_SET=" + evidence);
      System.out.println("WORLDLINE_M384_TRACE=" + trace);
      System.out.println("WORLDLINE_M384_SIGNATURE=" + sha(trace));
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
  private static BlockPosition reed(B173WireClient a, BlockPosition soil) throws Exception {
    BlockPosition crop = BlockFace.UP.adjacent(soil);
    a.useHeldItemOnBlock(soil, BlockFace.UP);
    a.awaitBlock(crop, new BlockState(83, 0));
    return crop;
  }
  private static BlockPosition foundation(RemoteChunkSnapshot q, int cx, int cz) {
    for (int x = 4; x <= 11; x++)
      for (int z = 4; z <= 11; z++)
        for (int y = 126; y >= 1; y--)
          if (q.blockAt(x, y, z).legacyId() == 3 && water(q.blockAt(x, y + 1, z).legacyId()))
            return new BlockPosition(cx * 16 + x, y, cz * 16 + z);
    throw new IllegalStateException("no deterministic cactus-sugar-set foundation");
  }
  private static BlockState at(RemoteChunkSnapshot q, BlockPosition p, int cx, int cz) {
    return q.blockAt(local(p.x(), cx), p.y(), local(p.z(), cz));
  }
  private static int id(RemoteWorldView v, BlockPosition p) {
    return v.blockAt(p.x(), p.y(), p.z()).legacyId();
  }
  private static String cell(BlockPosition p) {
    return p.x() + ":" + p.y() + ":" + p.z();
  }
  private static String dump(RemoteChunkSnapshot q, BlockPosition[] cells, int cx, int cz) {
    StringBuilder s = new StringBuilder();
    for (BlockPosition p : cells)
      s.append(cell(p)).append('=').append(at(q, p, cx, cz)).append(' ');
    return s.toString();
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
