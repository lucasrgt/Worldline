package worldline.smoke.mushroomplacesetb173;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Places official brown mushroom 39 on dark dirt and red mushroom 40 on dark netherrack as one set. */
public final class MushroomPlaceSetSmoke {
  private MushroomPlaceSetSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 7)
      throw new IllegalArgumentException(
          "usage: MushroomPlaceSetSmoke server.jar workspace port seed username chunkX chunkZ");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    int cx = Integer.parseInt(a[5]), cz = Integer.parseInt(a[6]);
    require(seed == 17320110707L && user.equals("MushSet383") && user.length() <= 16,
        "mushroom-place-set identity drift");
    Duration timeout = Duration.ofSeconds(90);
    B173DedicatedServer server =
        new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout), reader = null;
    BlockPosition top, dirt, rack, roof, brown, red;
    int column;
    BlockState brownPlaced = new BlockState(39, 0), redPlaced = new BlockState(40, 0);
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1, 2, 3, 4},
          new int[] {1, 3, 87, 39, 40}, new int[] {48, 1, 1, 1, 1}, new int[] {0, 0, 0, 0, 0});
      actor.connect();
      actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() == 5, "mushroom-place-set inventory drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      top = foundation(initial, cx, cz);
      column = 0;
      actor.selectHeldSlot(0);
      while (
          water(initial.blockAt(local(top.x(), cx), top.y() + 1, local(top.z(), cz)).legacyId())) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        require(++column <= 15, "water column exceeded mushroom-place-set fixture");
      }
      for (int lift = 0; lift < 8; lift++) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        column++;
      }
      actor.selectHeldSlot(1);
      dirt = place(actor, top, BlockFace.UP, 3);
      actor.selectHeldSlot(2);
      rack = place(actor, dirt, BlockFace.EAST, 87);
      actor.selectHeldSlot(0);
      roof = shade(actor, dirt, rack);
      actor.selectHeldSlot(3);
      brown = BlockFace.UP.adjacent(dirt);
      actor.placeHeldBlock(dirt, BlockFace.UP);
      actor.awaitBlock(brown, brownPlaced);
      actor.selectHeldSlot(4);
      red = BlockFace.UP.adjacent(rack);
      actor.placeHeldBlock(rack, BlockFace.UP);
      actor.awaitBlock(red, redPlaced);
      require(actor.sustainTicks(5).blockAt(brown.x(), brown.y(), brown.z()).equals(brownPlaced)
              && actor.sustainTicks(1).blockAt(red.x(), red.y(), red.z()).equals(redPlaced),
          "live mushroom-place-set drift");
      actor.close();
      awaitPlayers(server, 0);
      server.save();
      reader = new B173WireClient("127.0.0.1", port, user, timeout);
      reader.connect();
      reader.synchronizePose();
      RemoteChunkSnapshot after = reader.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      require(after.blockAt(local(dirt.x(), cx), dirt.y(), local(dirt.z(), cz))
                  .equals(new BlockState(3, 0))
              && after.blockAt(local(rack.x(), cx), rack.y(), local(rack.z(), cz))
                  .equals(new BlockState(87, 0))
              && after.blockAt(local(brown.x(), cx), brown.y(), local(brown.z(), cz))
                  .equals(brownPlaced)
              && after.blockAt(local(red.x(), cx), red.y(), local(red.z(), cz)).equals(redPlaced)
              && after.blockAt(local(roof.x(), cx), roof.y(), local(roof.z(), cz))
                  .equals(new BlockState(1, 0)),
          "persisted mushroom-place-set drift");
      String evidence = "column=" + column + ",dirt=" + dirt.x() + ":" + dirt.y() + ":" + dirt.z()
          + ":3:0,brown=" + brown.x() + ":" + brown.y() + ":" + brown.z()
          + ":39:0,netherrack=" + rack.x() + ":" + rack.y() + ":" + rack.z()
          + ":87:0,red=" + red.x() + ":" + red.y() + ":" + red.z() + ":40:0,roof=" + roof.x() + ":"
          + roof.y() + ":" + roof.z() + ":1:0,persisted=true,clients=2,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=dark-dirt3+dark-netherrack87+mushroom39+mushroom40|cause=packet15-item39+packet15-item40|wire=packet53-brown-mushroom39:0+packet53-red-mushroom40:0|oracle=live-block39:0+live-block40:0+fresh-login|"
          + evidence;
      System.out.println("WORLDLINE_M383_SET=" + evidence);
      System.out.println("WORLDLINE_M383_TRACE=" + trace);
      System.out.println("WORLDLINE_M383_SIGNATURE=" + sha(trace));
    } finally {
      actor.close();
      if (reader != null)
        reader.close();
      server.close();
    }
  }
  private static BlockPosition shade(B173WireClient a, BlockPosition dirt, BlockPosition rack)
      throws Exception {
    BlockPosition w = place(a, dirt, BlockFace.WEST, 1), n = place(a, dirt, BlockFace.NORTH, 1),
                  s = place(a, dirt, BlockFace.SOUTH, 1), e = place(a, rack, BlockFace.EAST, 1);
    place(a, rack, BlockFace.NORTH, 1);
    place(a, rack, BlockFace.SOUTH, 1);
    place(a, w, BlockFace.UP, 1);
    place(a, n, BlockFace.UP, 1);
    place(a, s, BlockFace.UP, 1);
    place(a, BlockFace.NORTH.adjacent(rack), BlockFace.UP, 1);
    place(a, BlockFace.SOUTH.adjacent(rack), BlockFace.UP, 1);
    BlockPosition wall = place(a, e, BlockFace.UP, 1);
    return place(
        a, place(a, place(a, wall, BlockFace.UP, 1), BlockFace.WEST, 1), BlockFace.WEST, 1);
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
    throw new IllegalStateException("no deterministic mushroom-place-set foundation");
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
