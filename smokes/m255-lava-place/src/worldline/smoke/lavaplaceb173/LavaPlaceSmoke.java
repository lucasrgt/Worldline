package worldline.smoke.lavaplaceb173;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Holds lava bucket 327 on a raised stone basin and places still lava 11:0. */
public final class LavaPlaceSmoke {
  private LavaPlaceSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 7)
      throw new IllegalArgumentException(
          "usage: LavaPlaceSmoke server.jar workspace port seed username chunkX chunkZ");
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
    RemoteItemStack empty = new RemoteItemStack(325, 1, 0);
    PlayerPose pose;
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1},
          new int[] {1, 327}, new int[] {48, 1}, new int[] {0, 0});
      actor.connect();
      pose = actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() == 2, "lava-place inventory drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      top = foundation(initial, cx, cz);
      column = 0;
      actor.selectHeldSlot(0);
      while (
          water(initial.blockAt(local(top.x(), cx), top.y() + 1, local(top.z(), cz)).legacyId())) {
        top = place(actor, top, BlockFace.UP, 1);
        pose = actor.moveAndObserve(0D, 1D, 0D, 1).resulting();
        require(++column <= 15, "water column exceeded lava-place fixture");
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
      pose = actor.moveAndObserve(0D, 1D, 0D, 1).resulting();
      pose = actor.moveAndObserve(0D, 0D, 1D, 1).resulting();
      cell = BlockFace.UP.adjacent(top);
      require(
          actor.sustainTicks(5).blockAt(cell.x(), cell.y(), cell.z()).equals(new BlockState(0, 0))
              && actor.health() == 20,
          "empty basin drift");
      actor.selectHeldSlot(1);
      actor.look(180F, 70F);
      actor.useHeldItemOnBlock(top, BlockFace.UP);
      actor.useSelectedItemInAir();
      actor.awaitBlock(cell, new BlockState(11, 0));
      require(
          actor.sustainTicks(5).blockAt(cell.x(), cell.y(), cell.z()).equals(new BlockState(11, 0))
              && actor.inventory().slot(37).item().equals(empty) && actor.health() == 20,
          "live lava-bucket place drift");
      actor.close();
      awaitPlayers(server, 0);
      server.save();
      reader = new B173WireClient("127.0.0.1", port, user, timeout);
      reader.connect();
      reader.synchronizePose();
      RemoteChunkSnapshot after = reader.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      require(after.blockAt(local(cell.x(), cx), cell.y(), local(cell.z(), cz))
                  .equals(new BlockState(11, 0))
              && reader.awaitInventory().slot(37).item().equals(empty)
              && reader.awaitHealth(20) == 20,
          "persisted still-lava empty-bucket drift");
      String evidence = "column=" + column + ",floor=" + top.x() + ":" + top.y() + ":" + top.z()
          + ":1:0,lava=" + cell.x() + ":" + cell.y() + ":" + cell.z()
          + ":0:0->11:0,held=327:1:0->325:1:0,persisted=true,clients=2,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=raised-stone-basin+lava-bucket327|cause=packet15-basin-cell+packet15-dir255-bucket327|wire=packet53-lava11+packet103-bucket325|oracle=live-place+fresh-login-still-lava+empty-bucket|"
          + evidence;
      System.out.println("WORLDLINE_M255_PLACE=" + evidence);
      System.out.println("WORLDLINE_M255_TRACE=" + trace);
      System.out.println("WORLDLINE_M255_SIGNATURE=" + sha(trace));
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
    throw new IllegalStateException("no deterministic lava-place foundation");
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
