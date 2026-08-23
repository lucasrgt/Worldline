package worldline.smoke.placeableitemcraftsb173;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Crafts sticks in 2x2, then painting 321, sign 323, and bowl 281x4 on a workbench. */
public final class PlaceableItemCraftsSmoke {
  private PlaceableItemCraftsSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 7)
      throw new IllegalArgumentException(
          "usage: PlaceableItemCraftsSmoke server.jar workspace port seed username chunkX chunkZ");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    int cx = Integer.parseInt(a[5]), cz = Integer.parseInt(a[6]);
    Duration timeout = Duration.ofSeconds(90);
    require(user.length() <= 16 && B173PlaceableWorkbenchCrafts.PAINTING.legacyId() == 321
            && B173PlaceableWorkbenchCrafts.SIGN.legacyId() == 323
            && B173PlaceableWorkbenchCrafts.BOWL.legacyId() == 281
            && B173PlaceablePersonalCrafts.STICKS8.legacyId() == 280,
        "placeable craft identities drifted");
    B173DedicatedServer server =
        new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout), reader = null;
    BlockPosition top, bench;
    int column;
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1, 2, 3, 4},
          new int[] {1, 58, 5, 35, 280}, new int[] {32, 1, 13, 1, 1}, new int[] {0, 0, 0, 0, 0});
      actor.connect();
      actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() == 5, "placeable inventory seed drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      top = foundation(initial, cx, cz);
      column = 0;
      actor.selectHeldSlot(0);
      while (
          water(initial.blockAt(local(top.x(), cx), top.y() + 1, local(top.z(), cz)).legacyId())) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        require(++column <= 15, "water column exceeded placeable fixture");
      }
      for (int lift = 0; lift < 8; lift++) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        column++;
      }
      actor.selectHeldSlot(1);
      bench = BlockFace.UP.adjacent(top);
      actor.placeHeldBlock(top, BlockFace.UP);
      actor.awaitBlock(bench, new BlockState(58, 0));
      worldline.test.WorldlineSmokeAwait.awaitEntity(
          actor, actor::inventory, v -> v.slot(37).empty(), "workbench consumption", 5);
      actor.selectHeldSlot(1);
      B173PlaceablePersonalCrafts.apply(actor);
      require(
          B173PlaceablePersonalCrafts.prepared(actor.inventory()), "live 2x2 sticks 280 drifted");
      actor.selectHeldSlot(5);
      actor.openWorkbench(bench, BlockFace.UP);
      B173PlaceableWorkbenchCrafts.apply(actor);
      require(B173PlaceableWorkbenchCrafts.stored(actor.inventory()),
          "live painting 321 sign 323 bowl 281 drifted");
      actor.closeWindow();
      actor.close();
      awaitPlayers(server, 0);
      server.save();
      require(server.player(user).inventoryItems() == 4, "placeable persistence count drift");
      reader = new B173WireClient("127.0.0.1", port, user, timeout);
      reader.connect();
      reader.synchronizePose();
      require(B173PlaceableWorkbenchCrafts.stored(reader.awaitInventory()),
          "persisted placeable crafts drifted");
      RemoteChunkSnapshot after = reader.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      require(after.blockAt(local(top.x(), cx), top.y(), local(top.z(), cz))
                  .equals(new BlockState(1, 0))
              && after.blockAt(local(bench.x(), cx), bench.y(), local(bench.z(), cz))
                  .equals(new BlockState(58, 0)),
          "persisted workbench 58:0 drift");
      String evidence = "painting=321,sign=323,bowl=281x4,column=" + column + ",support=" + top.x()
          + ":" + top.y() + ":" + top.z() + ":1:0,workbench=" + bench.x() + ":" + bench.y() + ":"
          + bench.z() + ":58:0,grid=2x2+3x3,persisted=true,clients=2,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=personal-2x2-planks5-sticks280+workbench58-painting321-sign323-bowl281|cause=packet102-window0-vertical-planks-to-sticks+packet102-workbench-matrix+result-take|wire=packet106-accepted+packet200-craft-stat|oracle=placeable-family-321-323-281+fresh-login|"
          + evidence;
      System.out.println("WORLDLINE_M372_CRAFTS=" + evidence);
      System.out.println("WORLDLINE_M372_TRACE=" + trace);
      System.out.println("WORLDLINE_M372_SIGNATURE=" + sha(trace));
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
    throw new IllegalStateException("no deterministic placeable-craft foundation");
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
