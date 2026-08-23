package worldline.smoke.bucketfluidsetb173;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Places still water 9 from bucket 326 and still lava 11 from bucket 327, then picks both up. */
public final class BucketFluidSetSmoke {
  private BucketFluidSetSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 7)
      throw new IllegalArgumentException(
          "usage: BucketFluidSetSmoke server.jar workspace port seed username chunkX chunkZ");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    int cx = Integer.parseInt(a[5]), cz = Integer.parseInt(a[6]);
    require(seed == 17320110707L && user.equals("BucketSet344") && user.length() <= 16,
        "bucket-fluid-set identity drift");
    Duration timeout = Duration.ofSeconds(90);
    B173DedicatedServer server =
        new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout), reader = null;
    BlockPosition top, cell;
    int column;
    RemoteItemStack empty = new RemoteItemStack(325, 1, 0),
                    waterBucket = new RemoteItemStack(326, 1, 0),
                    lavaBucket = new RemoteItemStack(327, 1, 0);
    PlayerPose pose;
    BlockState air = new BlockState(0, 0), stillWater = new BlockState(9, 0),
               stillLava = new BlockState(11, 0);
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1, 2},
          new int[] {1, 326, 327}, new int[] {48, 1, 1}, new int[] {0, 0, 0});
      actor.connect();
      pose = actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() == 3, "bucket-fluid-set inventory drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      top = foundation(initial, cx, cz);
      column = 0;
      actor.selectHeldSlot(0);
      while (
          water(initial.blockAt(local(top.x(), cx), top.y() + 1, local(top.z(), cz)).legacyId())) {
        top = place(actor, top, BlockFace.UP, 1);
        pose = actor.moveAndObserve(0D, 1D, 0D, 1).resulting();
        require(++column <= 15, "water column exceeded bucket-fluid-set fixture");
      }
      for (int lift = 0; lift < 8; lift++) {
        top = place(actor, top, BlockFace.UP, 1);
        pose = actor.moveAndObserve(0D, 1D, 0D, 1).resulting();
        column++;
      }
      for (BlockFace wall :
          new BlockFace[] {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST})
        place(actor, place(actor, top, wall, 1), BlockFace.UP, 1);
      while (pose.y() > top.y() + 1.01D)
        pose = actor.moveAndObserve(0D, -1D, 0D, 1).resulting();
      cell = BlockFace.UP.adjacent(top);
      require(actor.sustainTicks(5).blockAt(cell.x(), cell.y(), cell.z()).equals(air),
          "empty basin air drift");
      actor.selectHeldSlot(1);
      actor.look(0F, 90F);
      actor.useHeldItemOnBlock(top, BlockFace.UP);
      actor.useSelectedItemInAir();
      actor.awaitBlock(cell, stillWater);
      require(actor.sustainTicks(5).blockAt(cell.x(), cell.y(), cell.z()).equals(stillWater)
              && actor.inventory().slot(37).item().equals(empty),
          "live water-bucket place drift");
      actor.useHeldItemOnBlock(cell, BlockFace.UP);
      actor.useSelectedItemInAir();
      actor.awaitBlock(cell, air);
      require(actor.sustainTicks(5).blockAt(cell.x(), cell.y(), cell.z()).equals(air)
              && actor.inventory().slot(37).item().equals(waterBucket),
          "live water-bucket pickup drift");
      pose = actor.moveAndObserve(0D, 1D, 0D, 1).resulting();
      pose = actor.moveAndObserve(0D, 0D, 1D, 1).resulting();
      actor.selectHeldSlot(2);
      actor.look(180F, 70F);
      actor.useHeldItemOnBlock(top, BlockFace.UP);
      actor.useSelectedItemInAir();
      actor.awaitBlock(cell, stillLava);
      require(actor.sustainTicks(5).blockAt(cell.x(), cell.y(), cell.z()).equals(stillLava)
              && actor.inventory().slot(38).item().equals(empty) && actor.health() == 20,
          "live lava-bucket place drift");
      actor.useHeldItemOnBlock(cell, BlockFace.UP);
      actor.useSelectedItemInAir();
      actor.awaitBlock(cell, air);
      require(actor.sustainTicks(5).blockAt(cell.x(), cell.y(), cell.z()).equals(air)
              && actor.inventory().slot(38).item().equals(lavaBucket) && actor.health() == 20,
          "live lava-bucket pickup drift");
      actor.close();
      awaitPlayers(server, 0);
      server.save();
      reader = new B173WireClient("127.0.0.1", port, user, timeout);
      reader.connect();
      reader.synchronizePose();
      RemoteChunkSnapshot after = reader.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      RemoteInventoryView persisted = reader.awaitInventory();
      require(after.blockAt(local(cell.x(), cx), cell.y(), local(cell.z(), cz)).equals(air)
              && persisted.slot(37).item().equals(waterBucket)
              && persisted.slot(38).item().equals(lavaBucket) && reader.awaitHealth(20) == 20,
          "persisted empty-basin bucket-fluid-set drift");
      String evidence = "column=" + column + ",floor=" + top.x() + ":" + top.y() + ":" + top.z()
          + ":1:0,water=" + cell.x() + ":" + cell.y() + ":" + cell.z()
          + ":0:0->9:0->0:0,held-water=326:1:0->325:1:0->326:1:0,lava=" + cell.x() + ":" + cell.y()
          + ":" + cell.z()
          + ":0:0->11:0->0:0,held-lava=327:1:0->325:1:0->327:1:0,persisted=true,clients=2,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=raised-stone-basin+water-bucket326+lava-bucket327|cause=packet15-support+packet15-dir255-bucket326+packet15-water-cell+packet15-dir255-bucket325+packet15-basin-cell+packet15-dir255-bucket327+packet15-lava-cell+packet15-dir255-bucket325|wire=packet53-water9+packet103-bucket325+packet53-air0+packet103-bucket326+packet53-lava11+packet103-bucket325+packet53-air0+packet103-bucket327|oracle=live-place-pickup-326/9+327/11+fresh-login-empty-basin|"
          + evidence;
      System.out.println("WORLDLINE_M344_SET=" + evidence);
      System.out.println("WORLDLINE_M344_TRACE=" + trace);
      System.out.println("WORLDLINE_M344_SIGNATURE=" + sha(trace));
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
    throw new IllegalStateException("no deterministic bucket-fluid-set foundation");
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
