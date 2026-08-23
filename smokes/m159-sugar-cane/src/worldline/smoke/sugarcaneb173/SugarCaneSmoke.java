package worldline.smoke.sugarcaneb173;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Places official sugar cane beside still water and waits for random-tick growth to height 2+. */
public final class SugarCaneSmoke {
  private SugarCaneSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 9)
      throw new IllegalArgumentException(
          "usage: SugarCaneSmoke server.jar workspace port seed username chunkX chunkZ windowTicks growthWindows");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    int cx = Integer.parseInt(a[5]), cz = Integer.parseInt(a[6]), window = Integer.parseInt(a[7]),
        windows = Integer.parseInt(a[8]);
    Duration timeout = Duration.ofMinutes(20);
    B173DedicatedServer server =
        new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout), reader = null;
    BlockPosition top, dirt, waterCell, cane, above;
    int column, height;
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1, 2, 3},
          new int[] {1, 3, 9, 338}, new int[] {32, 1, 1, 8}, new int[] {0, 0, 0, 0});
      actor.connect();
      actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() == 4, "cane inventory drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      top = foundation(initial, cx, cz);
      column = 0;
      actor.selectHeldSlot(0);
      while (
          water(initial.blockAt(local(top.x(), cx), top.y() + 1, local(top.z(), cz)).legacyId())) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        require(++column <= 15, "water column exceeded cane fixture");
      }
      for (int lift = 0; lift < 8; lift++) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        column++;
      }
      BlockPosition east = place(actor, top, BlockFace.EAST, 1);
      actor.selectHeldSlot(1);
      dirt = place(actor, top, BlockFace.UP, 3);
      actor.selectHeldSlot(2);
      waterCell = place(actor, east, BlockFace.UP, 9);
      require(worldline.test.WorldlineSmokeAwait.observe(actor, 5)
                  .blockAt(waterCell.x(), waterCell.y(), waterCell.z())
                  .equals(new BlockState(9, 0)),
          "still water 9 drift");
      actor.selectHeldSlot(3);
      actor.useHeldItemOnBlock(dirt, BlockFace.UP);
      cane = BlockFace.UP.adjacent(dirt);
      above = BlockFace.UP.adjacent(cane);
      actor.awaitBlock(cane, new BlockState(83, 0));
      require(worldline.test.WorldlineSmokeAwait.observe(actor, 5)
                  .blockAt(cane.x(), cane.y(), cane.z())
                  .equals(new BlockState(83, 0)),
          "planted cane popped");
      RemoteWorldView grown = worldline.test.WorldlineSmokeAwait.awaitBlockOrNull(
          actor, above, new BlockState(83, 0), windows * window);
      height = grown == null ? 1 : 2;
      require(height >= 2 && grown.blockAt(cane.x(), cane.y(), cane.z()).legacyId() == 83,
          "sugar cane did not grow to height 2 within bound");
      actor.close();
      awaitPlayers(server, 0);
      server.save();
      reader = new B173WireClient("127.0.0.1", port, user, timeout);
      reader.connect();
      reader.synchronizePose();
      RemoteChunkSnapshot after = reader.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      require(after.blockAt(local(dirt.x(), cx), dirt.y(), local(dirt.z(), cz))
                  .equals(new BlockState(3, 0))
              && after.blockAt(local(waterCell.x(), cx), waterCell.y(), local(waterCell.z(), cz))
                  .equals(new BlockState(9, 0))
              && after.blockAt(local(cane.x(), cx), cane.y(), local(cane.z(), cz))
                  .equals(new BlockState(83, 0))
              && after.blockAt(local(above.x(), cx), above.y(), local(above.z(), cz)).legacyId()
                  == 83,
          "persisted sugar cane drift");
      String evidence = "column=" + column + ",dirt=" + dirt.x() + ":" + dirt.y() + ":" + dirt.z()
          + ":3:0,water=" + waterCell.x() + ":" + waterCell.y() + ":" + waterCell.z()
          + ":9:0,base=" + cane.x() + ":" + cane.y() + ":" + cane.z()
          + ":83:0,height>=2,persisted=true,clients=2,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=raised-dirt3+still-water9|cause=packet15-item338-reed|wire=packet53-reed83|oracle=official-random-tick-height>=2+fresh-login|"
          + evidence;
      System.out.println("WORLDLINE_M159_CANE=" + evidence);
      System.out.println("WORLDLINE_M159_TRACE=" + trace);
      System.out.println("WORLDLINE_M159_SIGNATURE=" + sha(trace));
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
  private static BlockPosition foundation(RemoteChunkSnapshot q, int cx, int cz) {
    for (int x = 4; x <= 11; x++)
      for (int z = 4; z <= 11; z++)
        for (int y = 126; y >= 1; y--)
          if (q.blockAt(x, y, z).legacyId() == 3 && water(q.blockAt(x, y + 1, z).legacyId()))
            return new BlockPosition(cx * 16 + x, y, cz * 16 + z);
    throw new IllegalStateException("no deterministic cane foundation");
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
