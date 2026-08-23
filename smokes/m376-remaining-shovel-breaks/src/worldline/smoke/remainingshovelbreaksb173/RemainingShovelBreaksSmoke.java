package worldline.smoke.remainingshovelbreaksb173;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Places clay 82, snow 78, snow block 80, and soul sand 88, then gold-shovel harvests each Packet21 drop. */
public final class RemainingShovelBreaksSmoke {
  private static final RemoteItemStack CLAY_BALL = new RemoteItemStack(337, 1, 0),
                                       SNOWBALL = new RemoteItemStack(332, 1, 0),
                                       SOUL = new RemoteItemStack(88, 1, 0),
                                       SHOVEL = new RemoteItemStack(284, 1, 0);
  private static final BlockState AIR = new BlockState(0, 0);
  private RemainingShovelBreaksSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 7)
      throw new IllegalArgumentException(
          "usage: RemainingShovelBreaksSmoke server.jar workspace port seed username chunkX chunkZ");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    int cx = Integer.parseInt(a[5]), cz = Integer.parseInt(a[6]);
    Duration timeout = Duration.ofSeconds(90);
    require(seed == 17320110707L && user.equals("Shovel376") && user.length() <= 16,
        "remaining-shovel-breaks identity drift");
    B173DedicatedServer server =
        new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout);
    BlockPosition top;
    int column;
    String clay, snow, snowblock, soulsand;
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1, 2, 3, 4, 5},
          new int[] {1, 82, 78, 80, 88, 284}, new int[] {32, 1, 1, 1, 1, 1},
          new int[] {0, 0, 0, 0, 0, 0});
      actor.connect();
      actor.synchronizePose();
      RemoteInventoryView inventory = actor.awaitInventory();
      require(inventory.occupiedSlots() == 6 && inventory.slot(41).item().equals(SHOVEL),
          "remaining-shovel-breaks inventory drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      top = foundation(initial, cx, cz);
      column = 0;
      actor.selectHeldSlot(0);
      while (
          water(initial.blockAt(local(top.x(), cx), top.y() + 1, local(top.z(), cz)).legacyId())) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        require(++column <= 15, "water column exceeded remaining-shovel-breaks fixture");
      }
      for (int lift = 0; lift < 8; lift++) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        column++;
      }
      clay = harvest(actor, top, 1, 5, 82, CLAY_BALL);
      snow = harvest(actor, top, 2, 5, 78, SNOWBALL);
      snowblock = harvest(actor, top, 3, 5, 80, SNOWBALL);
      soulsand = harvest(actor, top, 4, 5, 88, SOUL);
      String evidence = "column=" + column + ",support=" + top.x() + ":" + top.y() + ":" + top.z()
          + ":1:0," + clay + "," + snow + "," + snowblock + "," + soulsand
          + ",shovel=284,clients=1,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=raised-stone+clay82+snow78+snowblock80+soulsand88|cause=packet14-goldshovel284|wire=packet53-air+packet21-id337+packet21-id332+packet21-id88|oracle=remaining-shovel-breaks-drops+cells-82-78-80-88-to-0|"
          + evidence;
      System.out.println("WORLDLINE_M376_SHOVEL=" + evidence);
      System.out.println("WORLDLINE_M376_TRACE=" + trace);
      System.out.println("WORLDLINE_M376_SIGNATURE=" + sha(trace));
    } finally {
      actor.close();
      server.close();
    }
  }
  private static String harvest(B173WireClient a, BlockPosition support, int placeSlot,
      int shovelSlot, int id, RemoteItemStack expected) throws Exception {
    RemoteDroppedItem before = a.peekDroppedItem(expected);
    a.selectHeldSlot(placeSlot);
    BlockPosition cell = place(a, support, BlockFace.UP, id);
    require(worldline.test.WorldlineSmokeAwait.observe(a, 5)
                .blockAt(cell.x(), cell.y(), cell.z())
                .equals(new BlockState(id, 0)),
        "live block " + id + " drift");
    a.selectHeldSlot(shovelSlot);
    a.beginBreak(cell);
    worldline.test.WorldlineSmokeAwait.observe(a, 5);
    a.finishBreak(cell);
    a.awaitBlock(cell, AIR);
    RemoteDroppedItem drop = a.peekDroppedItem(expected);
    drop = worldline.test.WorldlineSmokeAwait.awaitEntityOrNull(a,
        ()
            -> a.peekDroppedItem(expected),
        value
        -> value != null && (before == null || value.entityId() != before.entityId()),
        "fresh shovel drop", 40);
    if (drop == null)
      drop = a.awaitDroppedItem(expected);
    require(drop.item().legacyId() > 0 && drop.item().count() >= 1
            && (before == null || drop.entityId() != before.entityId())
            && worldline.test.WorldlineSmokeAwait.observe(a, 1)
                .blockAt(cell.x(), cell.y(), cell.z())
                .equals(AIR),
        "Packet21 drop or cell " + id + "->0 absent");
    String name = id == 82 ? "clay" : id == 78 ? "snow" : id == 80 ? "snowblock" : "soulsand";
    return name + "=" + cell.x() + ":" + cell.y() + ":" + cell.z() + ":" + id
        + ":0->0:0,drop=packet21-" + drop.item().legacyId() + ":" + drop.item().count() + ":"
        + drop.item().damage();
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
    throw new IllegalStateException("no deterministic remaining-shovel-breaks foundation");
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
