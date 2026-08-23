package worldline.smoke.bedplaceb173;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Places official bed item 355 as two-block 26 halves from look yaw and persists them. */
public final class BedPlaceSmoke {
  private BedPlaceSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 7)
      throw new IllegalArgumentException(
          "usage: BedPlaceSmoke server.jar workspace port seed username chunkX chunkZ");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    int cx = Integer.parseInt(a[5]), cz = Integer.parseInt(a[6]);
    Duration timeout = Duration.ofSeconds(90);
    B173DedicatedServer server =
        new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout), reader = null;
    BlockPosition top, foot, head;
    int column;
    PlayerPose pose;
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1},
          new int[] {1, 355}, new int[] {32, 1}, new int[] {0, 0});
      actor.connect();
      pose = actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() == 2, "bed inventory drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      top = foundation(initial, cx, cz);
      column = 0;
      actor.selectHeldSlot(0);
      while (
          water(initial.blockAt(local(top.x(), cx), top.y() + 1, local(top.z(), cz)).legacyId())) {
        top = place(actor, top, BlockFace.UP, 1);
        pose = actor.moveAndObserve(0D, 1D, 0D, 1).resulting();
        require(++column <= 15, "water column exceeded bed fixture");
      }
      for (int lift = 0; lift < 8; lift++) {
        top = place(actor, top, BlockFace.UP, 1);
        pose = actor.moveAndObserve(0D, 1D, 0D, 1).resulting();
        column++;
      }
      foot = BlockFace.UP.adjacent(top);
      head = BlockFace.SOUTH.adjacent(foot);
      require(initial.blockAt(local(foot.x(), cx), foot.y(), local(foot.z(), cz)).legacyId() == 0
              && initial.blockAt(local(head.x(), cx), head.y(), local(head.z(), cz)).legacyId()
                  == 0,
          "bed cells were not initial air");
      pose = actor
                 .moveAndObserve(top.x() + 0.5D - pose.x(), top.y() + 1.0D - pose.y(),
                     top.z() + 0.5D - pose.z(), 8)
                 .resulting();
      place(actor, top, BlockFace.SOUTH, 1);
      actor.look(0F, 0F);
      pose = actor.moveAndObserve(0D, 0D, 0D, 2).resulting();
      actor.selectHeldSlot(1);
      actor.useHeldItemOnBlock(top, BlockFace.UP);
      awaitBed(actor, foot, head, 0, 8);
      RemoteWorldView live = worldline.test.WorldlineSmokeAwait.observe(actor, 5);
      require(live.blockAt(foot.x(), foot.y(), foot.z()).equals(new BlockState(26, 0))
              && live.blockAt(head.x(), head.y(), head.z()).equals(new BlockState(26, 8)),
          "live bed halves drift");
      actor.close();
      awaitPlayers(server, 0);
      server.save();
      reader = new B173WireClient("127.0.0.1", port, user, timeout);
      reader.connect();
      reader.synchronizePose();
      RemoteChunkSnapshot after = reader.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      require(after.blockAt(local(foot.x(), cx), foot.y(), local(foot.z(), cz))
                  .equals(new BlockState(26, 0))
              && after.blockAt(local(head.x(), cx), head.y(), local(head.z(), cz))
                  .equals(new BlockState(26, 8)),
          "persisted bed halves drift");
      String evidence = "column=" + column + ",support=" + top.x() + ":" + top.y() + ":" + top.z()
          + ":1:0,foot=" + foot.x() + ":" + foot.y() + ":" + foot.z() + ":26:0,head=" + head.x()
          + ":" + head.y() + ":" + head.z()
          + ":26:8,look=0:0,persisted=true,clients=2,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=raised-stone+item355-block26|cause=packet15-item355+look-0|wire=packet53-bed26:0/8|oracle=place-halves+fresh-login|"
          + evidence;
      System.out.println("WORLDLINE_M240_BED=" + evidence);
      System.out.println("WORLDLINE_M240_TRACE=" + trace);
      System.out.println("WORLDLINE_M240_SIGNATURE=" + sha(trace));
    } finally {
      actor.close();
      if (reader != null)
        reader.close();
      server.close();
    }
  }
  private static void awaitBed(B173WireClient a, BlockPosition foot, BlockPosition head,
      int footMeta, int headMeta) throws Exception {
    RemoteWorldView v = a.awaitBlock(foot, new BlockState(26, footMeta));
    if (!v.blockAt(head.x(), head.y(), head.z()).equals(new BlockState(26, headMeta)))
      v = a.awaitBlock(head, new BlockState(26, headMeta));
    BlockState f = v.blockAt(foot.x(), foot.y(), foot.z()),
               h = v.blockAt(head.x(), head.y(), head.z());
    require(f.equals(new BlockState(26, footMeta)) && h.equals(new BlockState(26, headMeta)),
        "bed halves " + f + " / " + h);
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
    throw new IllegalStateException("no deterministic bed foundation");
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
