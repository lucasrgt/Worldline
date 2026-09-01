package worldline.smoke.wallsignsupportbreakb173;
import static worldline.b173server.B173FixtureSupport.*;

import java.nio.file.*;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;
import worldline.testkit.WallSignSupportBreakFixture;

/** Places an official wall sign on a stone side, digs the support, and freezes the pop state. */
public final class WallSignSupportBreakSmoke {
  private static final BlockState STONE = new BlockState(1, 0);
  private static final BlockState WALL_SIGN = new BlockState(68, 5);
  private static final BlockState AIR = new BlockState(0, 0);
  private static final RemoteItemStack SIGN_DROP = new RemoteItemStack(323, 1, 0);
  private WallSignSupportBreakSmoke() {}
  public static void main(String[] a) throws Exception {
    if (a.length != 7)
      throw new IllegalArgumentException(
          "usage: WallSignSupportBreakSmoke server.jar workspace port seed username chunkX chunkZ");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    int cx = Integer.parseInt(a[5]), cz = Integer.parseInt(a[6]);
    Duration timeout = Duration.ofSeconds(90);
    require(seed == 17320110707L && user.equals("SignSup762") && user.length() <= 16,
        "wall-sign-support-break identity drift");
    B173DedicatedServer server = new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout), reader = null;
    BlockPosition top, cell, support, sign;
    int column;
    RemoteDroppedItem drop;
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1},
          new int[] {1, 323}, new int[] {32, 1}, new int[] {0, 0});
      actor.connect();
      actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() == 2, "wall-sign-support-break inventory drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      top = foundation(initial, cx, cz);
      column = 0;
      actor.selectHeldSlot(0);
      while (water(initial.blockAt(local(top.x(), cx), top.y() + 1, local(top.z(), cz)).legacyId())) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        require(++column <= 15, "water column exceeded wall-sign-support-break fixture");
      }
      for (int lift = 0; lift < 8; lift++) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        column++;
      }
      support = top;
      cell = BlockFace.EAST.adjacent(top);
      require(initial.blockAt(local(cell.x(), cx), cell.y(), local(cell.z(), cz)).legacyId() == 0,
          "wall sign target was not initial air");
      actor.selectHeldSlot(1);
      actor.look(-90F, 0F);
      actor.useHeldItemOnBlock(support, BlockFace.EAST);
      worldline.test.WorldlineSmokeAwait.awaitBlockMatching(
          actor, cell, s -> s.legacyId() == 68, "wall sign placement", 40);
      require(worldline.test.WorldlineSmokeAwait.observe(actor, 20)
                  .blockAt(cell.x(), cell.y(), cell.z()).equals(WALL_SIGN)
              && worldline.test.WorldlineSmokeAwait.observe(actor, 2)
                  .blockAt(support.x(), support.y(), support.z()).equals(STONE),
          "supported wall sign drifted before invalidation");
      actor.selectHeldSlot(0);
      actor.beginBreak(support);
      actor.sustainTicks(20);
      actor.finishBreak(support);
      actor.awaitBlock(support, AIR);
      worldline.test.WorldlineSmokeAwait.awaitBlock(actor, cell, AIR, 20);
      drop = actor.awaitDroppedItem(SIGN_DROP);
      require(drop.item().equals(SIGN_DROP) && drop.item().legacyId() == 323
              && drop.item().count() == 1,
          "Packet21 sign item 323 drop absent");
      RemoteWorldView live = worldline.test.WorldlineSmokeAwait.observe(actor, 2);
      actor.close();
      awaitPlayers(server, 0);
      server.save();
      reader = new B173WireClient("127.0.0.1", port, user, timeout);
      reader.connect();
      reader.synchronizePose();
      RemoteChunkSnapshot after = reader.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      BlockState persistedSign = after.blockAt(local(cell.x(), cx), cell.y(), local(cell.z(), cz));
      require(persistedSign.equals(AIR), "popped wall sign did not stay air after login");
      WallSignSupportBreakFixture.Evidence contract =
          WallSignSupportBreakFixture.observe(STONE, WALL_SIGN,
              live.blockAt(support.x(), support.y(), support.z()),
              live.blockAt(cell.x(), cell.y(), cell.z()), drop.item(), persistedSign);
      require(contract.drop().legacyId() == 323 && contract.persistedSign().equals(AIR),
          "reusable wall sign support break evidence drift");
      String evidence = "column=" + column + ",support=" + support.x() + ":" + support.y() + ":"
          + support.z() + ":1:0->0:0,sign=" + cell.x() + ":" + cell.y() + ":" + cell.z()
          + ":68:5->0:0,drops=packet21-323,persisted=true,clients=2,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + ("|fixture=raised-stone-column+wall-sign68:5|cause=packet15-item323-east+packet14-dig-support"
              + "|wire=packet53-sign68:5->0+packet21-323")
          + "|oracle=supported-sign-persistent+support-break-pop-air+fresh-login-air|" + evidence;
      System.out.println("WORLDLINE_M762_SIGNAL=" + evidence);
      System.out.println("WORLDLINE_M762_TRACE=" + trace);
      System.out.println("WORLDLINE_M762_SIGNATURE=" + sha(trace));
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
    throw new IllegalStateException("no deterministic wall-sign-support-break foundation");
  }
  private static void require(boolean v, String m) {
    if (!v)
      throw new IllegalStateException(m);
  }
}
