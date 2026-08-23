package worldline.smoke.remainingbookshelfplaceb173;
import static worldline.b173server.B173FixtureSupport.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Crafts bookshelf 47 twice on workbench 58, places two 47 cells, harvests one to air with no Packet21 340. */
public final class RemainingBookshelfPlaceSmoke {
  private static final RemoteItemStack BOOK = new RemoteItemStack(340, 1, 0);
  private static final BlockState AIR = new BlockState(0, 0), STONE = new BlockState(1, 0),
                                  BENCH = new BlockState(58, 0), SHELF = new BlockState(47, 0);
  private RemainingBookshelfPlaceSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 7)
      throw new IllegalArgumentException(
          "usage: RemainingBookshelfPlaceSmoke server.jar workspace port seed username chunkX chunkZ");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    int cx = Integer.parseInt(a[5]), cz = Integer.parseInt(a[6]);
    require(seed == 17320110707L && user.equals("Bookshelf416") && user.length() <= 16
            && B173RemainingBookshelfCrafts.BOOKSHELF.legacyId() == 47 && BOOK.legacyId() == 340,
        "remaining-bookshelf identity drift");
    Duration timeout = Duration.ofSeconds(90);
    B173DedicatedServer server =
        new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout), reader = null;
    BlockPosition top, east, west, bench, first, second;
    int column;
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D,
          new int[] {0, 1, 2, 3, 4, 5, 6}, new int[] {1, 58, 340, 5, 340, 5, 286},
          new int[] {32, 1, 3, 6, 3, 6, 1}, new int[] {0, 0, 0, 0, 0, 0, 0});
      actor.connect();
      actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() == 7
              && actor.awaitInventory().slot(38).item().equals(new RemoteItemStack(340, 3, 0))
              && actor.awaitInventory().slot(39).item().equals(new RemoteItemStack(5, 6, 0))
              && actor.awaitInventory().slot(42).item().equals(new RemoteItemStack(286, 1, 0)),
          "remaining-bookshelf inventory seed drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      top = foundation(initial, cx, cz);
      column = 0;
      actor.selectHeldSlot(0);
      while (
          water(initial.blockAt(local(top.x(), cx), top.y() + 1, local(top.z(), cz)).legacyId())) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        require(++column <= 15, "water column exceeded remaining-bookshelf fixture");
      }
      for (int lift = 0; lift < 8; lift++) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        column++;
      }
      east = place(actor, top, BlockFace.EAST, 1);
      west = place(actor, top, BlockFace.WEST, 1);
      actor.selectHeldSlot(1);
      bench = place(actor, top, BlockFace.UP, 58);
      worldline.test.WorldlineSmokeAwait.observe(actor, 5);
      actor.selectHeldSlot(1);
      actor.openWorkbench(bench, BlockFace.UP);
      B173RemainingBookshelfCrafts.apply(actor);
      requireCrafts(actor.inventory());
      actor.closeWindow();
      actor.selectHeldSlot(3);
      first = place(actor, east, BlockFace.UP, 47);
      actor.selectHeldSlot(5);
      second = place(actor, west, BlockFace.UP, 47);
      require(worldline.test.WorldlineSmokeAwait.observe(actor, 5)
                  .blockAt(first.x(), first.y(), first.z())
                  .equals(SHELF)
              && worldline.test.WorldlineSmokeAwait.observe(actor, 1)
                  .blockAt(second.x(), second.y(), second.z())
                  .equals(SHELF),
          "live remaining-bookshelf two-cell drift");
      harvest(actor, first, 6, 20);
      require(actor.peekDroppedItem(BOOK) == null
              && actor.peekDroppedItem(B173RemainingBookshelfCrafts.BOOKSHELF) == null,
          "Packet21 340 or 47 after remaining-bookshelf harvest");
      RemoteWorldView live = worldline.test.WorldlineSmokeAwait.observe(actor, 5);
      require(live.blockAt(first.x(), first.y(), first.z()).equals(AIR)
              && live.blockAt(second.x(), second.y(), second.z()).equals(SHELF)
              && live.blockAt(bench.x(), bench.y(), bench.z()).equals(BENCH)
              && actor.peekDroppedItem(BOOK) == null,
          "live remaining-bookshelf harvest drift");
      actor.close();
      awaitPlayers(server, 0);
      server.save();
      require(
          server.player(user).inventoryItems() == 2, "remaining-bookshelf persistence count drift");
      reader = new B173WireClient("127.0.0.1", port, user, timeout);
      reader.connect();
      reader.synchronizePose();
      RemoteChunkSnapshot after = reader.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      require(after.blockAt(local(top.x(), cx), top.y(), local(top.z(), cz)).equals(STONE)
              && after.blockAt(local(bench.x(), cx), bench.y(), local(bench.z(), cz)).equals(BENCH)
              && after.blockAt(local(first.x(), cx), first.y(), local(first.z(), cz)).equals(AIR)
              && after.blockAt(local(second.x(), cx), second.y(), local(second.z(), cz))
                  .equals(SHELF),
          "persisted remaining-bookshelf family drift");
      String evidence = "column=" + column + ",support=" + cell(top, 1, 0) + ",workbench="
          + cell(bench, 58, 0) + ",craft=47-from-5x6+340x3,places=" + cell(first, 47, 0) + "+"
          + cell(second, 47, 0) + ",harvest=" + cell(first, 47, 0)
          + "->0:0,drop=no-packet21-340,axe=286,persisted=true,clients=2,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=workbench58+planks5x6+book340x3+two-47|cause=packet102-craft-47+packet15-item47+packet14-goldaxe286|wire=packet53-47-to-air+no-packet21-id340|oracle=craft-47+two-places+no-drop-340+fresh-login-not-m189|"
          + evidence;
      System.out.println("WORLDLINE_M416_SET=" + evidence);
      System.out.println("WORLDLINE_M416_TRACE=" + trace);
      System.out.println("WORLDLINE_M416_SIGNATURE=" + sha(trace));
    } finally {
      actor.close();
      if (reader != null)
        reader.close();
      server.close();
    }
  }
  private static void requireCrafts(RemoteInventoryView view) {
    require(view.slot(39).item().equals(B173RemainingBookshelfCrafts.BOOKSHELF)
            && view.slot(41).item().equals(B173RemainingBookshelfCrafts.BOOKSHELF)
            && view.slot(42).item().equals(new RemoteItemStack(286, 1, 0))
            && view.occupiedSlots() == 4,
        "remaining-bookshelf crafted inventory drift");
  }
  private static void harvest(B173WireClient a, BlockPosition target, int axeSlot, int ticks)
      throws Exception {
    a.selectHeldSlot(axeSlot);
    a.beginBreak(target);
    worldline.test.WorldlineSmokeAwait.observe(a, ticks);
    a.finishBreak(target);
    a.awaitBlock(target, AIR);
  }
  private static BlockPosition foundation(RemoteChunkSnapshot q, int cx, int cz) {
    for (int x = 4; x <= 11; x++)
      for (int z = 4; z <= 11; z++)
        for (int y = 126; y >= 1; y--)
          if (q.blockAt(x, y, z).legacyId() == 3 && water(q.blockAt(x, y + 1, z).legacyId()))
            return new BlockPosition(cx * 16 + x, y, cz * 16 + z);
    throw new IllegalStateException("no deterministic remaining-bookshelf foundation");
  }
  private static String cell(BlockPosition p, int id, int meta) {
    return p.x() + ":" + p.y() + ":" + p.z() + ":" + id + ":" + meta;
  }
  private static void require(boolean v, String m) {
    if (!v)
      throw new IllegalStateException(m);
  }
}
