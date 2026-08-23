package worldline.smoke.brownwoolb173;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Places official brown wool 35:12 on raised stone and freezes live plus persisted block 35:12. */
public final class BrownWoolSmoke {
  private BrownWoolSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 7)
      throw new IllegalArgumentException(
          "usage: BrownWoolSmoke server.jar workspace port seed username chunkX chunkZ");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    int cx = Integer.parseInt(a[5]), cz = Integer.parseInt(a[6]);
    Duration timeout = Duration.ofSeconds(90);
    require(user.length() <= 16, "username exceeds 16");
    B173DedicatedServer server =
        new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout), reader = null;
    BlockPosition top, wool;
    int column;
    BlockState placed;
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1},
          new int[] {1, 35}, new int[] {32, 1}, new int[] {0, 12});
      actor.connect();
      actor.synchronizePose();
      RemoteInventoryView inventory = actor.awaitInventory();
      require(inventory.occupiedSlots() == 2
              && inventory.slot(37).item().equals(new RemoteItemStack(35, 1, 12)),
          "brown wool 35:12 inventory drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      top = foundation(initial, cx, cz);
      column = 0;
      actor.selectHeldSlot(0);
      while (
          water(initial.blockAt(local(top.x(), cx), top.y() + 1, local(top.z(), cz)).legacyId())) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        require(++column <= 15, "water column exceeded brown wool fixture");
      }
      for (int lift = 0; lift < 8; lift++) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        column++;
      }
      actor.selectHeldSlot(1);
      wool = BlockFace.UP.adjacent(top);
      actor.placeHeldBlock(top, BlockFace.UP);
      placed = new BlockState(35, 12);
      actor.awaitBlock(wool, placed);
      require(placed.metadata() == 12 && !placed.equals(new BlockState(35, 0))
              && !placed.equals(new BlockState(35, 1)) && !placed.equals(new BlockState(35, 13))
              && actor.sustainTicks(5).blockAt(wool.x(), wool.y(), wool.z()).equals(placed),
          "live brown wool 35:12 drift");
      actor.close();
      awaitPlayers(server, 0);
      server.save();
      reader = new B173WireClient("127.0.0.1", port, user, timeout);
      reader.connect();
      reader.synchronizePose();
      RemoteChunkSnapshot after = reader.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      require(after.blockAt(local(top.x(), cx), top.y(), local(top.z(), cz))
                  .equals(new BlockState(1, 0))
              && after.blockAt(local(wool.x(), cx), wool.y(), local(wool.z(), cz)).equals(placed)
              && !placed.equals(new BlockState(35, 0)) && !placed.equals(new BlockState(35, 1))
              && !placed.equals(new BlockState(35, 13)),
          "persisted brown wool 35:12 drift");
      String evidence = "column=" + column + ",support=" + top.x() + ":" + top.y() + ":" + top.z()
          + ":1:0,wool=" + wool.x() + ":" + wool.y() + ":" + wool.z() + ":35:" + placed.metadata()
          + ",persisted=true,clients=2,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=raised-stone+wool35:12|cause=packet15-item35:12|wire=packet53-wool35:"
          + placed.metadata() + "|oracle=live-block35:12+fresh-login|" + evidence;
      System.out.println("WORLDLINE_M288_WOOL=" + evidence);
      System.out.println("WORLDLINE_M288_TRACE=" + trace);
      System.out.println("WORLDLINE_M288_SIGNATURE=" + sha(trace));
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
    throw new IllegalStateException("no deterministic brown wool foundation");
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
