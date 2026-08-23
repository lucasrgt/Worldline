package worldline.smoke.waterbucketb173;
import static worldline.b173server.B173FixtureSupport.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Places still water 9:0 on a raised stone basin and picks it up with empty bucket 325. */
public final class WaterBucketSmoke {
  private WaterBucketSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 7)
      throw new IllegalArgumentException(
          "usage: WaterBucketSmoke server.jar workspace port seed username chunkX chunkZ");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    int cx = Integer.parseInt(a[5]), cz = Integer.parseInt(a[6]);
    Duration timeout = Duration.ofSeconds(90);
    B173DedicatedServer server =
        new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout), reader = null;
    BlockPosition top, cell;
    int column;
    RemoteItemStack filled = new RemoteItemStack(326, 1, 0);
    PlayerPose pose;
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1, 2},
          new int[] {1, 9, 325}, new int[] {48, 1, 1}, new int[] {0, 0, 0});
      actor.connect();
      pose = actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() == 3, "bucket inventory drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      top = foundation(initial, cx, cz);
      column = 0;
      actor.selectHeldSlot(0);
      while (
          water(initial.blockAt(local(top.x(), cx), top.y() + 1, local(top.z(), cz)).legacyId())) {
        top = place(actor, top, BlockFace.UP, 1);
        pose = actor.moveAndObserve(0D, 1D, 0D, 1).resulting();
        require(++column <= 15, "water column exceeded bucket fixture");
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
      actor.selectHeldSlot(1);
      cell = place(actor, top, BlockFace.UP, 9);
      require(
          actor.sustainTicks(5).blockAt(cell.x(), cell.y(), cell.z()).equals(new BlockState(9, 0)),
          "still water 9:0 drift");
      actor.selectHeldSlot(2);
      actor.look(0F, 90F);
      actor.useHeldItemOnBlock(cell, BlockFace.UP);
      actor.useSelectedItemInAir();
      actor.awaitBlock(cell, new BlockState(0, 0));
      require(
          actor.sustainTicks(5).blockAt(cell.x(), cell.y(), cell.z()).equals(new BlockState(0, 0))
              && actor.inventory().slot(38).item().equals(filled),
          "live water-bucket pickup drift");
      actor.close();
      awaitPlayers(server, 0);
      server.save();
      reader = new B173WireClient("127.0.0.1", port, user, timeout);
      reader.connect();
      reader.synchronizePose();
      RemoteChunkSnapshot after = reader.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      require(after.blockAt(local(cell.x(), cx), cell.y(), local(cell.z(), cz))
                  .equals(new BlockState(0, 0))
              && reader.awaitInventory().slot(38).item().equals(filled),
          "persisted empty-basin water-bucket drift");
      String evidence = "column=" + column + ",floor=" + top.x() + ":" + top.y() + ":" + top.z()
          + ":1:0,water=" + cell.x() + ":" + cell.y() + ":" + cell.z()
          + ":9:0->0:0,held=325:1:0->326:1:0,persisted=true,clients=2,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=raised-stone-basin+still-water9|cause=packet15-water-cell+packet15-dir255-bucket325|wire=packet53-air0+packet103-bucket326|oracle=live-pickup+fresh-login-empty-basin+water-bucket|"
          + evidence;
      System.out.println("WORLDLINE_M168_BUCKET=" + evidence);
      System.out.println("WORLDLINE_M168_TRACE=" + trace);
      System.out.println("WORLDLINE_M168_SIGNATURE=" + sha(trace));
    } finally {
      actor.close();
      if (reader != null)
        reader.close();
      server.close();
    }
  }
  private static BlockPosition foundation(RemoteChunkSnapshot q, int cx, int cz) {
    for (int x = 4; x <= 11; x++)
      for (int z = 4; z <= 11; z++)
        for (int y = 126; y >= 1; y--)
          if (q.blockAt(x, y, z).legacyId() == 3 && water(q.blockAt(x, y + 1, z).legacyId()))
            return new BlockPosition(cx * 16 + x, y, cz * 16 + z);
    throw new IllegalStateException("no deterministic bucket foundation");
  }
  private static void require(boolean v, String m) {
    if (!v)
      throw new IllegalStateException(m);
  }
}
