package worldline.smoke.remainingbedorientsetb173;
import static worldline.b173server.B173FixtureSupport.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;
import worldline.testkit.*;

/** Places remaining Overworld bed 26 west, north, and east foot/head halves as one SET. */
public final class RemainingBedOrientSetSmoke {
  private RemainingBedOrientSetSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 7)
      throw new IllegalArgumentException(
          "usage: RemainingBedOrientSetSmoke server.jar workspace port seed username chunkX chunkZ");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    int cx = Integer.parseInt(a[5]), cz = Integer.parseInt(a[6]);
    require(seed == 17320110707L && user.equals("BedOrnt431") && user.length() <= 16,
        "remaining-bed-orient-set identity drift");
    Duration timeout = Duration.ofSeconds(90);
    B173DedicatedServer server =
        new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout), reader = null;
    BlockPosition top, westPad, northPad, eastPad, westFoot, westHead, northFoot, northHead,
        eastFoot, eastHead;
    int column;
    PlayerPose pose;
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1},
          new int[] {1, 355}, new int[] {48, 3}, new int[] {0, 0});
      actor.connect();
      pose = actor.synchronizePose();
      require(
          actor.awaitInventory().occupiedSlots() == 2, "remaining-bed-orient-set inventory drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      top = foundation(initial, cx, cz);
      column = 0;
      actor.selectHeldSlot(0);
      while (
          water(initial.blockAt(local(top.x(), cx), top.y() + 1, local(top.z(), cz)).legacyId())) {
        top = place(actor, top, BlockFace.UP, 1);
        pose = actor.moveAndObserve(0D, 1D, 0D, 1).resulting();
        require(++column <= 15, "water column exceeded remaining-bed-orient-set fixture");
      }
      for (int lift = 0; lift < 8; lift++) {
        top = place(actor, top, BlockFace.UP, 1);
        pose = actor.moveAndObserve(0D, 1D, 0D, 1).resulting();
        column++;
      }
      westPad = place(actor, top, BlockFace.WEST, 1);
      place(actor, westPad, BlockFace.WEST, 1);
      northPad = place(actor, top, BlockFace.NORTH, 1);
      place(actor, northPad, BlockFace.NORTH, 1);
      eastPad = place(actor, top, BlockFace.EAST, 1);
      place(actor, eastPad, BlockFace.EAST, 1);
      pose = actor
                 .moveAndObserve(top.x() + 0.5D - pose.x(), top.y() + 1.0D - pose.y(),
                     top.z() + 0.5D - pose.z(), 8)
                 .resulting();
      actor.selectHeldSlot(1);
      westFoot = bed(actor, westPad, 90F, 1, BlockFace.WEST);
      westHead = BlockFace.WEST.adjacent(westFoot);
      northFoot = bed(actor, northPad, 180F, 2, BlockFace.NORTH);
      northHead = BlockFace.NORTH.adjacent(northFoot);
      eastFoot = bed(actor, eastPad, -90F, 3, BlockFace.EAST);
      eastHead = BlockFace.EAST.adjacent(eastFoot);
      require(live(actor, westFoot, 1, westHead, 9) && live(actor, northFoot, 2, northHead, 10)
              && live(actor, eastFoot, 3, eastHead, 11),
          "live remaining-bed-orient-set drift");
      actor.close();
      awaitPlayers(server, 0);
      server.save();
      reader = new B173WireClient("127.0.0.1", port, user, timeout);
      reader.connect();
      reader.synchronizePose();
      RemoteChunkSnapshot after = reader.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      require(cell(after, westFoot, cx, cz, 1) && cell(after, westHead, cx, cz, 9)
              && cell(after, northFoot, cx, cz, 2) && cell(after, northHead, cx, cz, 10)
              && cell(after, eastFoot, cx, cz, 3) && cell(after, eastHead, cx, cz, 11),
          "persisted remaining-bed-orient-set drift");
      java.util.List<BlockStateCell> cells = java.util.Arrays.asList(
          new BlockStateCell(westFoot, new BlockState(26, 1)),
          new BlockStateCell(westHead, new BlockState(26, 9)),
          new BlockStateCell(northFoot, new BlockState(26, 2)),
          new BlockStateCell(northHead, new BlockState(26, 10)),
          new BlockStateCell(eastFoot, new BlockState(26, 3)),
          new BlockStateCell(eastHead, new BlockState(26, 11)));
      require(BlockPlacementPersistenceFixture.execute("b1.7.3:block/026", "oriented-bed",
              true, 355, 3, 0, 3, cells, cells, cells,
              BlockLifecycleDriver.ReloadBoundary.FRESH_LOGIN).subject()
                  .equals("b1.7.3:block/026"),
          "public bed placement/persistence evidence drift");
      String evidence = "dimension=0,column=" + column + ",support=" + token(top, 1, 0)
          + ",west=" + token(westFoot, 26, 1) + "+" + token(westHead, 26, 9)
          + ",north=" + token(northFoot, 26, 2) + "+" + token(northHead, 26, 10)
          + ",east=" + token(eastFoot, 26, 3) + "+" + token(eastHead, 26, 11)
          + ",look=90+180+-90,persisted=true,clients=2,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=raised-stone+item355-block26-west+north+east|cause=packet15-item355+look90+look180+look-90|wire=packet53-bed26:1/9+26:2/10+26:3/11|oracle=remaining-foot-head-facings+fresh-login|"
          + evidence;
      System.out.println("WORLDLINE_M431_SET=" + evidence);
      System.out.println("WORLDLINE_M431_TRACE=" + trace);
      System.out.println("WORLDLINE_M431_SIGNATURE=" + sha(trace));
    } finally {
      actor.close();
      if (reader != null)
        reader.close();
      server.close();
    }
  }
  private static BlockPosition bed(B173WireClient a, BlockPosition support, float yaw, int footMeta,
      BlockFace headFace) throws Exception {
    BlockPosition foot = BlockFace.UP.adjacent(support), head = headFace.adjacent(foot);
    a.look(yaw, 0F);
    a.moveAndObserve(0D, 0D, 0D, 2);
    a.useHeldItemOnBlock(support, BlockFace.UP);
    a.awaitBlock(foot, new BlockState(26, footMeta));
    a.awaitBlock(head, new BlockState(26, footMeta + 8));
    return foot;
  }
  private static boolean live(B173WireClient a, BlockPosition foot, int footMeta,
      BlockPosition head, int headMeta) throws Exception {
    RemoteWorldView v = a.sustainTicks(1);
    return v.blockAt(foot.x(), foot.y(), foot.z()).equals(new BlockState(26, footMeta))
        && v.blockAt(head.x(), head.y(), head.z()).equals(new BlockState(26, headMeta));
  }
  private static boolean cell(RemoteChunkSnapshot q, BlockPosition p, int cx, int cz, int meta) {
    return q.blockAt(local(p.x(), cx), p.y(), local(p.z(), cz)).equals(new BlockState(26, meta));
  }
  private static String token(BlockPosition p, int id, int meta) {
    return p.x() + ":" + p.y() + ":" + p.z() + ":" + id + ":" + meta;
  }
  private static BlockPosition foundation(RemoteChunkSnapshot q, int cx, int cz) {
    for (int x = 4; x <= 11; x++)
      for (int z = 4; z <= 11; z++)
        for (int y = 126; y >= 1; y--)
          if (q.blockAt(x, y, z).legacyId() == 3 && water(q.blockAt(x, y + 1, z).legacyId()))
            return new BlockPosition(cx * 16 + x, y, cz * 16 + z);
    throw new IllegalStateException("no deterministic remaining-bed-orient-set foundation");
  }
  private static void require(boolean v, String m) {
    if (!v)
      throw new IllegalStateException(m);
  }
}
