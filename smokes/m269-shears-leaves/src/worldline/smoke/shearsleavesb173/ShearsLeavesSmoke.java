package worldline.smoke.shearsleavesb173;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Places oak leaves 18 beside log 17, shears them with item 359, and contrasts Packet21 id 18 versus bare-hand. */
public final class ShearsLeavesSmoke {
  private static final RemoteItemStack LEAF = new RemoteItemStack(18, 1, 0);
  private static final BlockState AIR = new BlockState(0, 0), PLACED = new BlockState(18, 8);
  private ShearsLeavesSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 7)
      throw new IllegalArgumentException(
          "usage: ShearsLeavesSmoke server.jar workspace port seed username chunkX chunkZ");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    int cx = Integer.parseInt(a[5]), cz = Integer.parseInt(a[6]);
    Duration timeout = Duration.ofSeconds(90);
    B173DedicatedServer server =
        new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout);
    BlockPosition top, log, sheared, bare;
    int column;
    RemoteDroppedItem drop, after;
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1, 2, 3},
          new int[] {1, 17, 18, 359}, new int[] {32, 1, 2, 1}, new int[] {0, 0, 0, 0});
      actor.connect();
      actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() == 4, "shears-leaves inventory drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      top = foundation(initial, cx, cz);
      column = 0;
      actor.selectHeldSlot(0);
      while (
          water(initial.blockAt(local(top.x(), cx), top.y() + 1, local(top.z(), cz)).legacyId())) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        require(++column <= 15, "water column exceeded shears-leaves fixture");
      }
      for (int lift = 0; lift < 8; lift++) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        column++;
      }
      actor.selectHeldSlot(1);
      log = place(actor, top, BlockFace.EAST, 17);
      actor.selectHeldSlot(2);
      sheared = BlockFace.UP.adjacent(top);
      actor.placeHeldBlock(top, BlockFace.UP);
      actor.awaitBlock(sheared, PLACED);
      bare = BlockFace.UP.adjacent(log);
      actor.placeHeldBlock(log, BlockFace.UP);
      actor.awaitBlock(bare, PLACED);
      RemoteWorldView live = worldline.test.WorldlineSmokeAwait.observe(actor, 5);
      require(live.blockAt(sheared.x(), sheared.y(), sheared.z()).equals(PLACED)
              && live.blockAt(bare.x(), bare.y(), bare.z()).equals(PLACED),
          "live leaves drift");
      actor.selectHeldSlot(3);
      harvest(actor, sheared, 5);
      actor.awaitBlock(sheared, AIR);
      drop = actor.awaitDroppedItem(LEAF);
      require(drop.item().equals(LEAF) && drop.item().legacyId() == 18 && drop.item().count() == 1,
          "shears Packet21 leaf drop absent");
      actor.selectHeldSlot(4);
      harvest(actor, bare, 20);
      actor.awaitBlock(bare, AIR);
      after = actor.peekDroppedItem(LEAF);
      require(after != null && after.entityId() == drop.entityId(), "bare-hand Packet21 leaf drop");
      String evidence = "column=" + column + ",support=" + top.x() + ":" + top.y() + ":" + top.z()
          + ":1:0,log=" + log.x() + ":" + log.y() + ":" + log.z() + ":17:0,sheared=" + sheared.x()
          + ":" + sheared.y() + ":" + sheared.z() + ":18:8->0:0,bare=" + bare.x() + ":" + bare.y()
          + ":" + bare.z()
          + ":18:8->0:0,shears=359,drop=packet21-18:1:0,bare-hand=no-new-packet21-18,clients=1,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=raised-stone+oak17+leaves18x2|cause=packet14-shears359|wire=packet53-air+packet21-id18|oracle=shears-leaf-drop-versus-bare-hand|"
          + evidence;
      System.out.println("WORLDLINE_M269_SHEARS=" + evidence);
      System.out.println("WORLDLINE_M269_TRACE=" + trace);
      System.out.println("WORLDLINE_M269_SIGNATURE=" + sha(trace));
    } finally {
      actor.close();
      server.close();
    }
  }
  private static void harvest(B173WireClient a, BlockPosition target, int ticks) {
    a.beginBreak(target);
    worldline.test.WorldlineSmokeAwait.observe(a, ticks);
    a.finishBreak(target);
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
    throw new IllegalStateException("no deterministic shears-leaves foundation");
  }
  private static boolean water(int id) {
    return id == 8 || id == 9;
  }
  private static int local(int v, int c) {
    return v - c * 16;
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
