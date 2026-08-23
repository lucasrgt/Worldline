package worldline.smoke.bedsleepsetb173;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Places one official bed, Packet15-occupies it at night, then leaves/wakes standing. */
public final class BedSleepSetSmoke {
  private BedSleepSetSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 7)
      throw new IllegalArgumentException(
          "usage: BedSleepSetSmoke server.jar workspace port seed username chunkX chunkZ");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    int cx = Integer.parseInt(a[5]), cz = Integer.parseInt(a[6]);
    Duration timeout = Duration.ofSeconds(180);
    require(user.length() <= 16, "username exceeds 16");
    B173DedicatedServer server =
        new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout), reader = null;
    BlockPosition top, foot, head;
    int column;
    RemoteBedUse sleep;
    PlayerPose pose, wake;
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
        require(++column <= 15, "water column exceeded bed sleep set fixture");
      }
      for (int lift = 0; lift < 8; lift++) {
        top = place(actor, top, BlockFace.UP, 1);
        pose = actor.moveAndObserve(0D, 1D, 0D, 1).resulting();
        column++;
      }
      pose = actor
                 .moveAndObserve(top.x() + 0.5D - pose.x(), top.y() + 1.0D - pose.y(),
                     top.z() + 0.5D - pose.z(), 8)
                 .resulting();
      place(actor, top, BlockFace.SOUTH, 1);
      actor.look(0F, 0F);
      pose = actor.moveAndObserve(0D, 0D, 0D, 2).resulting();
      actor.selectHeldSlot(1);
      actor.useHeldItemOnBlock(top, BlockFace.UP);
      foot = BlockFace.UP.adjacent(top);
      head = BlockFace.SOUTH.adjacent(foot);
      actor.awaitBlock(foot, new BlockState(26, 0));
      actor.awaitBlock(head, new BlockState(26, 8));
      actor.selectHeldSlot(2);
      server.setTime(18000L);
      worldline.test.WorldlineSmokeAwait.observe(actor, 20);
      actor.activateBlock(foot, BlockFace.UP);
      sleep = B173BedAccess.await(actor);
      require(sleep.entityId() == actor.state().entityId() && sleep.unused() == 0
              && sleep.x() == head.x() && sleep.y() == head.y() && sleep.z() == head.z()
              && sleep.sleepPacket() == 17 && sleep.bedPacket() == 70
              && sleep.packet70() == RemoteBedUse.NO_PACKET70,
          "Packet17 sleep enter drift");
      actor.awaitBlock(head, new BlockState(26, 12));
      require(worldline.test.WorldlineSmokeAwait.observe(actor, 240)
                  .blockAt(head.x(), head.y(), head.z())
                  .equals(new BlockState(26, 8)),
          "SMP bed skip did not leave occupied head");
      wake = actor.moveAndObserve(0D, 0D, 0D, 8).resulting();
      require(wake.y() >= foot.y() - 0.5D && wake.y() <= foot.y() + 2.0D,
          "actor is not standing after bed leave");
      actor.close();
      awaitPlayers(server, 0);
      server.save();
      reader = new B173WireClient("127.0.0.1", port, user, timeout);
      reader.connect();
      pose = reader.synchronizePose();
      RemoteChunkSnapshot after = reader.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      require(after.blockAt(local(foot.x(), cx), foot.y(), local(foot.z(), cz))
                  .equals(new BlockState(26, 0))
              && after.blockAt(local(head.x(), cx), head.y(), local(head.z(), cz))
                  .equals(new BlockState(26, 8)),
          "post-sleep bed halves drift");
      require(pose.y() >= foot.y() - 0.5D && pose.y() <= foot.y() + 2.0D,
          "persisted wake is not standing");
      String evidence = "column=" + column + ",foot=" + foot.x() + ":" + foot.y() + ":" + foot.z()
          + ":26:0,head=" + head.x() + ":" + head.y() + ":" + head.z()
          + ":26:8,enter=26:8->26:12,packet17=head,packet70=-1,leave=26:12->26:8,wake=standing,skip=true,persisted=wake,clients=2,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=raised-stone+item355-block26|cause=packet15-item355-place+empty-hand-night-use|wire=packet17-sleep+packet70=-1+packet53-occupied|oracle=sleep-enter+leave-standing+persisted-wake|"
          + evidence;
      System.out.println("WORLDLINE_M330_SET=" + evidence);
      System.out.println("WORLDLINE_M330_TRACE=" + trace);
      System.out.println("WORLDLINE_M330_SIGNATURE=" + sha(trace));
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
    throw new IllegalStateException("no deterministic bed sleep set foundation");
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
