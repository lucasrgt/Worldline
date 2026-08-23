package worldline.smoke.remainingtorchfacesb173;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Places remaining wall-torch attachments 50:1, 50:2, 50:3, 50:4 on one raised stone as a SET. */
public final class RemainingTorchFacesSmoke {
  private RemainingTorchFacesSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 7)
      throw new IllegalArgumentException(
          "usage: RemainingTorchFacesSmoke server.jar workspace port seed username chunkX chunkZ");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    int cx = Integer.parseInt(a[5]), cz = Integer.parseInt(a[6]);
    require(seed == 17320110707L && user.equals("TorchFace400") && user.length() <= 16,
        "remaining-torch-faces identity drift");
    Duration timeout = Duration.ofSeconds(90);
    B173DedicatedServer server =
        new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout), reader = null;
    BlockPosition top, east, west, south, north;
    int column;
    BlockState e = new BlockState(50, 1), w = new BlockState(50, 2), s = new BlockState(50, 3),
               n = new BlockState(50, 4), stone = new BlockState(1, 0),
               floor = new BlockState(50, 5);
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1},
          new int[] {1, 50}, new int[] {32, 4}, new int[] {0, 0});
      actor.connect();
      actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() == 2, "remaining-torch-faces inventory drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      top = foundation(initial, cx, cz);
      column = 0;
      actor.selectHeldSlot(0);
      while (
          water(initial.blockAt(local(top.x(), cx), top.y() + 1, local(top.z(), cz)).legacyId())) {
        top = place(actor, top, BlockFace.UP, 1, 0);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        require(++column <= 15, "water column exceeded remaining-torch-faces fixture");
      }
      for (int lift = 0; lift < 8; lift++) {
        top = place(actor, top, BlockFace.UP, 1, 0);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        column++;
      }
      actor.selectHeldSlot(1);
      east = place(actor, top, BlockFace.EAST, 50, 1);
      west = place(actor, top, BlockFace.WEST, 50, 2);
      south = place(actor, top, BlockFace.SOUTH, 50, 3);
      north = place(actor, top, BlockFace.NORTH, 50, 4);
      RemoteWorldView live = worldline.test.WorldlineSmokeAwait.observe(actor, 5);
      require(live.blockAt(east.x(), east.y(), east.z()).equals(e)
              && live.blockAt(west.x(), west.y(), west.z()).equals(w)
              && live.blockAt(south.x(), south.y(), south.z()).equals(s)
              && live.blockAt(north.x(), north.y(), north.z()).equals(n)
              && !live.blockAt(east.x(), east.y(), east.z()).equals(floor),
          "live remaining-torch-faces drift");
      actor.close();
      awaitPlayers(server, 0);
      server.save();
      reader = new B173WireClient("127.0.0.1", port, user, timeout);
      reader.connect();
      reader.synchronizePose();
      RemoteChunkSnapshot after = reader.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      require(after.blockAt(local(top.x(), cx), top.y(), local(top.z(), cz)).equals(stone)
              && after.blockAt(local(east.x(), cx), east.y(), local(east.z(), cz)).equals(e)
              && after.blockAt(local(west.x(), cx), west.y(), local(west.z(), cz)).equals(w)
              && after.blockAt(local(south.x(), cx), south.y(), local(south.z(), cz)).equals(s)
              && after.blockAt(local(north.x(), cx), north.y(), local(north.z(), cz)).equals(n),
          "persisted remaining-torch-faces drift");
      String evidence = "column=" + column + ",support=" + cell(top, 1, 0) + ",east="
          + cell(east, 50, 1) + ",west=" + cell(west, 50, 2) + ",south=" + cell(south, 50, 3)
          + ",north=" + cell(north, 50, 4) + ",persisted=true,clients=2,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=raised-stone+torch50:1+50:2+50:3+50:4|cause=packet15-item50-east+west+south+north|wire=packet53-torch50:1+50:2+50:3+50:4|oracle=remaining-wall-torch-faces+fresh-login|"
          + evidence;
      System.out.println("WORLDLINE_M400_SET=" + evidence);
      System.out.println("WORLDLINE_M400_TRACE=" + trace);
      System.out.println("WORLDLINE_M400_SIGNATURE=" + sha(trace));
    } finally {
      actor.close();
      if (reader != null)
        reader.close();
      server.close();
    }
  }
  private static BlockPosition place(
      B173WireClient a, BlockPosition support, BlockFace face, int id, int meta) throws Exception {
    BlockPosition target = face.adjacent(support);
    a.placeHeldBlock(support, face);
    a.awaitBlock(target, new BlockState(id, meta));
    return target;
  }
  private static BlockPosition foundation(RemoteChunkSnapshot q, int cx, int cz) {
    for (int x = 4; x <= 11; x++)
      for (int z = 4; z <= 11; z++)
        for (int y = 126; y >= 1; y--)
          if (q.blockAt(x, y, z).legacyId() == 3 && water(q.blockAt(x, y + 1, z).legacyId()))
            return new BlockPosition(cx * 16 + x, y, cz * 16 + z);
    throw new IllegalStateException("no deterministic remaining-torch-faces foundation");
  }
  private static String cell(BlockPosition p, int id, int meta) {
    return p.x() + ":" + p.y() + ":" + p.z() + ":" + id + ":" + meta;
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
