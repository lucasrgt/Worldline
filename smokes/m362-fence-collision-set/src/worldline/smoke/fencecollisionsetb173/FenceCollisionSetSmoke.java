package worldline.smoke.fencecollisionsetb173;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Places two official fence 85:0 cells and proves the same Packet13 walk is blocked versus air. */
public final class FenceCollisionSetSmoke {
  private FenceCollisionSetSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 7)
      throw new IllegalArgumentException(
          "usage: FenceCollisionSetSmoke server.jar workspace port seed username chunkX chunkZ");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    int cx = Integer.parseInt(a[5]), cz = Integer.parseInt(a[6]);
    Duration timeout = Duration.ofSeconds(90);
    require(user.equals("FenceCol362") && user.length() <= 16, "actor username drift");
    B173DedicatedServer server =
        new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout), reader = null;
    BlockPosition top, north, east, westFence, eastFence;
    int column, ticks = 10;
    PlayerPose pose;
    MovementOutcome airMove, fenceMove;
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1},
          new int[] {1, 85}, new int[] {32, 2}, new int[] {0, 0});
      actor.connect();
      pose = actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() == 2, "fence-collision inventory drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      top = foundation(initial, cx, cz);
      column = 0;
      actor.selectHeldSlot(0);
      while (
          water(initial.blockAt(local(top.x(), cx), top.y() + 1, local(top.z(), cz)).legacyId())) {
        top = place(actor, top, BlockFace.UP, 1);
        pose = actor.moveAndObserve(0D, 1D, 0D, 1).resulting();
        require(++column <= 15, "water column exceeded fence-collision fixture");
      }
      for (int lift = 0; lift < 8; lift++) {
        top = place(actor, top, BlockFace.UP, 1);
        pose = actor.moveAndObserve(0D, 1D, 0D, 1).resulting();
        column++;
      }
      north = place(actor, top, BlockFace.NORTH, 1);
      east = place(actor, top, BlockFace.EAST, 1);
      pose = actor
                 .moveAndObserve((north.x() + 0.5D) - pose.x(), (north.y() + 1D) - pose.y(),
                     (north.z() + 0.5D) - pose.z(), 4)
                 .resulting();
      RemoteWorldView live = worldline.test.WorldlineSmokeAwait.observe(actor, 1);
      require(live.blockAt(top.x(), top.y() + 1, top.z()).legacyId() == 0
              && live.blockAt(east.x(), east.y() + 1, east.z()).legacyId() == 0,
          "pre-fence air path occupied");
      PlayerPose airStart = pose;
      airMove = actor.moveAndObserve(0D, 0D, 1D, ticks);
      pose = airMove.resulting();
      int air = milli(Math.abs(pose.z() - airStart.z()));
      require(!airMove.corrected() && air > 0,
          "air Packet13 step was not free " + air + " " + disp(airMove));
      pose = actor
                 .moveAndObserve(airStart.x() - pose.x(), airStart.y() - pose.y(),
                     airStart.z() - pose.z(), ticks)
                 .resulting();
      actor.selectHeldSlot(1);
      westFence = place(actor, top, BlockFace.UP, 85);
      eastFence = place(actor, east, BlockFace.UP, 85);
      live = worldline.test.WorldlineSmokeAwait.observe(actor, 5);
      require(
          live.blockAt(westFence.x(), westFence.y(), westFence.z()).equals(new BlockState(85, 0))
              && live.blockAt(eastFence.x(), eastFence.y(), eastFence.z())
                  .equals(new BlockState(85, 0)),
          "live adjacent fence drift");
      PlayerPose fenceStart = pose;
      fenceMove = actor.moveAndObserve(0D, 0D, 1D, ticks);
      pose = fenceMove.resulting();
      int fence = milli(Math.abs(pose.z() - fenceStart.z()));
      require(fenceMove.corrected() && fence < air,
          "fence Packet13 walk was not blocked vs air " + air + "/" + fence + " "
              + disp(fenceMove));
      actor.close();
      awaitPlayers(server, 0);
      server.save();
      reader = new B173WireClient("127.0.0.1", port, user, timeout);
      reader.connect();
      reader.synchronizePose();
      RemoteChunkSnapshot after = reader.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      require(after.blockAt(local(top.x(), cx), top.y(), local(top.z(), cz))
                  .equals(new BlockState(1, 0))
              && after.blockAt(local(north.x(), cx), north.y(), local(north.z(), cz))
                  .equals(new BlockState(1, 0))
              && after.blockAt(local(westFence.x(), cx), westFence.y(), local(westFence.z(), cz))
                  .equals(new BlockState(85, 0))
              && after.blockAt(local(eastFence.x(), cx), eastFence.y(), local(eastFence.z(), cz))
                  .equals(new BlockState(85, 0)),
          "persisted fence-collision drift");
      String evidence = "column=" + column + ",support=" + cell(top, 1, 0)
          + ",north=" + cell(north, 1, 0) + ",west=" + cell(westFence, 85, 0)
          + ",east=" + cell(eastFence, 85, 0) + ",ticks=" + ticks + ",air=" + air
          + ",fence=" + fence + ",air-disp=" + disp(airMove) + ",fence-disp=" + disp(fenceMove)
          + ",blocked=true,persisted=true,clients=2,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=raised-stone+adjacent-fence85-path|cause=packet15-item85+packet13-walk|wire=packet13-air-vs-fence-collision|oracle=fence-walk-blocked-vs-air|"
          + evidence;
      System.out.println("WORLDLINE_M362_SET=" + evidence);
      System.out.println("WORLDLINE_M362_TRACE=" + trace);
      System.out.println("WORLDLINE_M362_SIGNATURE=" + sha(trace));
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
    throw new IllegalStateException("no deterministic fence-collision foundation");
  }
  private static String cell(BlockPosition p, int id, int meta) {
    return p.x() + ":" + p.y() + ":" + p.z() + ":" + id + ":" + meta;
  }
  private static String disp(MovementOutcome o) {
    return o.corrected() ? "corrected" : "unchallenged";
  }
  private static boolean water(int id) {
    return id == 8 || id == 9;
  }
  private static int local(int v, int c) {
    return v - c * 16;
  }
  private static int milli(double v) {
    return (int) Math.round(v * 1000D);
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
