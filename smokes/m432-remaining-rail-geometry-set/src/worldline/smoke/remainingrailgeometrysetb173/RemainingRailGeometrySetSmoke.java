package worldline.smoke.remainingrailgeometrysetb173;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Places remaining rail 66 slope and curve metadata as one SET. */
public final class RemainingRailGeometrySetSmoke {
  private RemainingRailGeometrySetSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 7)
      throw new IllegalArgumentException(
          "usage: RemainingRailGeometrySetSmoke server.jar workspace port seed username chunkX chunkZ");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    int cx = Integer.parseInt(a[5]), cz = Integer.parseInt(a[6]);
    require(seed == 17320110707L && user.equals("RailGeo432") && user.length() <= 16,
        "remaining-rail-geometry-set identity drift");
    Duration timeout = Duration.ofSeconds(90);
    B173DedicatedServer server =
        new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout), reader = null;
    BlockPosition top, east, high, south, corner, eastArm, southArm, slope, curve;
    int column;
    BlockState slopeS, curveS, stone = new BlockState(1, 0);
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1},
          new int[] {1, 66}, new int[] {48, 16}, new int[] {0, 0});
      actor.connect();
      actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() == 2,
          "remaining-rail-geometry-set inventory drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      top = foundation(initial, cx, cz);
      column = 0;
      actor.selectHeldSlot(0);
      while (
          water(initial.blockAt(local(top.x(), cx), top.y() + 1, local(top.z(), cz)).legacyId())) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        require(++column <= 15, "water column exceeded remaining-rail-geometry-set fixture");
      }
      for (int lift = 0; lift < 8; lift++) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        column++;
      }
      east = place(actor, top, BlockFace.EAST, 1);
      high = place(actor, east, BlockFace.UP, 1);
      south = place(actor, top, BlockFace.SOUTH, 1);
      corner = place(actor, south, BlockFace.SOUTH, 1);
      eastArm = place(actor, corner, BlockFace.EAST, 1);
      southArm = place(actor, corner, BlockFace.SOUTH, 1);
      actor.selectHeldSlot(1);
      actor.look(0F, 0F);
      slope = rail(actor, top);
      rail(actor, high);
      slopeS = awaitMeta(actor, slope, 2, 5);
      curve = rail(actor, corner);
      rail(actor, eastArm);
      rail(actor, southArm);
      curveS = awaitMeta(actor, curve, 6, 9);
      require(slopeS.legacyId() == 66 && curveS.legacyId() == 66 && slopeS.legacyId() != 27
              && curveS.legacyId() != 28 && slopeS.metadata() == 2 && curveS.metadata() == 6
              && !slopeS.equals(curveS),
          "live remaining-rail-geometry-set drift: " + slopeS + "/" + curveS);
      actor.close();
      awaitPlayers(server, 0);
      server.save();
      reader = new B173WireClient("127.0.0.1", port, user, timeout);
      reader.connect();
      reader.synchronizePose();
      RemoteChunkSnapshot after = reader.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      require(after.blockAt(local(top.x(), cx), top.y(), local(top.z(), cz)).equals(stone)
              && after.blockAt(local(slope.x(), cx), slope.y(), local(slope.z(), cz)).equals(slopeS)
              && after.blockAt(local(curve.x(), cx), curve.y(), local(curve.z(), cz))
                  .equals(curveS),
          "persisted remaining-rail-geometry-set drift");
      String evidence = "column=" + column + ",support=" + cell(top, 1, 0) + ",slope="
          + cell(slope, 66, slopeS.metadata()) + ",curve=" + cell(curve, 66, curveS.metadata())
          + ",persisted=true,clients=2,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=raised-stone+rail66-slope+rail66-curve|cause=packet15-item66-slope+curve|wire=packet53-rail66:"
          + slopeS.metadata() + "+rail66:" + curveS.metadata()
          + "|oracle=remaining-rail-geometry-set+fresh-login|" + evidence;
      System.out.println("WORLDLINE_M432_SET=" + evidence);
      System.out.println("WORLDLINE_M432_TRACE=" + trace);
      System.out.println("WORLDLINE_M432_SIGNATURE=" + sha(trace));
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
  private static BlockPosition rail(B173WireClient a, BlockPosition support) throws Exception {
    BlockPosition target = BlockFace.UP.adjacent(support);
    a.placeHeldBlock(support, BlockFace.UP);
    worldline.test.WorldlineSmokeAwait.awaitBlockMatching(
        a, target, s -> s.legacyId() == 66, "rail placement", 40);
    return target;
  }
  private static BlockState awaitMeta(B173WireClient a, BlockPosition p, int lo, int hi)
      throws Exception {
    return worldline.test.WorldlineSmokeAwait.awaitBlockMatching(a, p,
        s
        -> s.legacyId() == 66 && s.metadata() >= lo && s.metadata() <= hi,
        "rail meta " + lo + "-" + hi, 40);
  }
  private static BlockPosition foundation(RemoteChunkSnapshot q, int cx, int cz) {
    for (int x = 4; x <= 11; x++)
      for (int z = 4; z <= 11; z++)
        for (int y = 126; y >= 1; y--)
          if (q.blockAt(x, y, z).legacyId() == 3 && water(q.blockAt(x, y + 1, z).legacyId()))
            return new BlockPosition(cx * 16 + x, y, cz * 16 + z);
    throw new IllegalStateException("no deterministic remaining-rail-geometry-set foundation");
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
