package worldline.smoke.orepickbreaksb173;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Holds suitable picks and fully breaks cobble 4, coal ore 16, and diamond ore 56, freezing Packet21 drops. */
public final class OrePickBreaksSmoke {
  private static final RemoteItemStack COBBLE = new RemoteItemStack(4, 1, 0),
                                       COAL = new RemoteItemStack(263, 1, 0),
                                       GEM = new RemoteItemStack(264, 1, 0);
  private static final BlockState AIR = new BlockState(0, 0);
  private OrePickBreaksSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 7)
      throw new IllegalArgumentException(
          "usage: OrePickBreaksSmoke server.jar workspace port seed username chunkX chunkZ");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    int cx = Integer.parseInt(a[5]), cz = Integer.parseInt(a[6]);
    Duration timeout = Duration.ofSeconds(90);
    B173DedicatedServer server =
        new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout);
    BlockPosition top, cobble, coal, ore;
    int column;
    RemoteDroppedItem cobbleDrop, coalDrop, gemDrop;
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1, 2, 3, 4, 5},
          new int[] {1, 4, 16, 56, 257, 278}, new int[] {32, 1, 1, 1, 1, 1},
          new int[] {0, 0, 0, 0, 0, 0});
      actor.connect();
      actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() == 6, "ore-pick-breaks inventory drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      top = foundation(initial, cx, cz);
      column = 0;
      actor.selectHeldSlot(0);
      while (
          water(initial.blockAt(local(top.x(), cx), top.y() + 1, local(top.z(), cz)).legacyId())) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        require(++column <= 15, "water column exceeded ore-pick-breaks fixture");
      }
      for (int lift = 0; lift < 8; lift++) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        column++;
      }
      actor.selectHeldSlot(1);
      cobble = place(actor, top, BlockFace.UP, 4);
      actor.selectHeldSlot(2);
      coal = place(actor, top, BlockFace.EAST, 16);
      actor.selectHeldSlot(3);
      ore = place(actor, coal, BlockFace.UP, 56);
      RemoteWorldView live = worldline.test.WorldlineSmokeAwait.observe(actor, 5);
      require(live.blockAt(cobble.x(), cobble.y(), cobble.z()).equals(new BlockState(4, 0))
              && live.blockAt(coal.x(), coal.y(), coal.z()).equals(new BlockState(16, 0))
              && live.blockAt(ore.x(), ore.y(), ore.z()).equals(new BlockState(56, 0)),
          "live ore-pick family drift");
      actor.selectHeldSlot(4);
      cobbleDrop = harvest(actor, cobble, 15, COBBLE);
      coalDrop = harvest(actor, coal, 20, COAL);
      actor.selectHeldSlot(5);
      gemDrop = harvest(actor, ore, 20, GEM);
      require(cobbleDrop.item().legacyId() == 4 && coalDrop.item().legacyId() == 263
              && gemDrop.item().legacyId() == 264 && cobbleDrop.item().count() == 1
              && coalDrop.item().count() == 1 && gemDrop.item().count() == 1,
          "Packet21 ore-pick family drops absent");
      String evidence = "column=" + column + ",support=" + top.x() + ":" + top.y() + ":" + top.z()
          + ":1:0,cobble=" + cobble.x() + ":" + cobble.y() + ":" + cobble.z()
          + ":4:0->0:0,coal=" + coal.x() + ":" + coal.y() + ":" + coal.z()
          + ":16:0->0:0,ore=" + ore.x() + ":" + ore.y() + ":" + ore.z()
          + ":56:0->0:0,picks=257+278,drops=packet21-4+263+264,clients=1,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=raised-stone+cobble4+ore16+ore56|cause=packet14-ironpick257+diamondpick278|wire=packet53-air+packet21-id4+263+264|oracle=ore-pick-breaks-family|"
          + evidence;
      System.out.println("WORLDLINE_M300_BREAKS=" + evidence);
      System.out.println("WORLDLINE_M300_TRACE=" + trace);
      System.out.println("WORLDLINE_M300_SIGNATURE=" + sha(trace));
    } finally {
      actor.close();
      server.close();
    }
  }
  private static RemoteDroppedItem harvest(
      B173WireClient a, BlockPosition target, int ticks, RemoteItemStack item) throws Exception {
    a.beginBreak(target);
    worldline.test.WorldlineSmokeAwait.observe(a, ticks);
    a.finishBreak(target);
    a.awaitBlock(target, AIR);
    RemoteDroppedItem drop = a.awaitDroppedItem(item);
    require(drop.item().equals(item) && drop.item().legacyId() == item.legacyId()
            && drop.item().count() == 1,
        "Packet21 " + item.legacyId() + " drop absent");
    return drop;
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
    throw new IllegalStateException("no deterministic ore-pick-breaks foundation");
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
