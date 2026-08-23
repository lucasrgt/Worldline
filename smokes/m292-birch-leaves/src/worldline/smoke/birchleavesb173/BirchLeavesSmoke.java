package worldline.smoke.birchleavesb173;
import static worldline.b173server.B173FixtureSupport.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Places birch leaves item 18:2 beside oak log 17 so official 18:10 persists. */
public final class BirchLeavesSmoke {
  private BirchLeavesSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 7)
      throw new IllegalArgumentException(
          "usage: BirchLeavesSmoke server.jar workspace port seed username chunkX chunkZ");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    int cx = Integer.parseInt(a[5]), cz = Integer.parseInt(a[6]);
    Duration timeout = Duration.ofSeconds(90);
    require(user.length() <= 16, "username exceeds 16");
    B173DedicatedServer server =
        new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout), reader = null;
    BlockPosition top, log, leaf;
    int column;
    BlockState placed;
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1, 2},
          new int[] {1, 17, 18}, new int[] {32, 1, 1}, new int[] {0, 0, 2});
      actor.connect();
      actor.synchronizePose();
      RemoteInventoryView inventory = actor.awaitInventory();
      require(inventory.occupiedSlots() == 3
              && inventory.slot(38).item().equals(new RemoteItemStack(18, 1, 2)),
          "birch leaves 18:2 inventory drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      top = foundation(initial, cx, cz);
      column = 0;
      actor.selectHeldSlot(0);
      while (
          water(initial.blockAt(local(top.x(), cx), top.y() + 1, local(top.z(), cz)).legacyId())) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        require(++column <= 15, "water column exceeded birch leaves fixture");
      }
      for (int lift = 0; lift < 8; lift++) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        column++;
      }
      actor.selectHeldSlot(1);
      log = place(actor, top, BlockFace.EAST, 17);
      actor.selectHeldSlot(2);
      leaf = BlockFace.UP.adjacent(top);
      actor.placeHeldBlock(top, BlockFace.UP);
      placed = new BlockState(18, 10);
      actor.awaitBlock(leaf, placed);
      require(placed.metadata() == 10 && !placed.equals(new BlockState(18, 8))
              && !placed.equals(new BlockState(18, 9))
              && actor.sustainTicks(5).blockAt(leaf.x(), leaf.y(), leaf.z()).equals(placed),
          "live birch leaves 18:10 drift");
      actor.close();
      awaitPlayers(server, 0);
      server.save();
      reader = new B173WireClient("127.0.0.1", port, user, timeout);
      reader.connect();
      reader.synchronizePose();
      RemoteChunkSnapshot after = reader.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      require(after.blockAt(local(top.x(), cx), top.y(), local(top.z(), cz))
                  .equals(new BlockState(1, 0))
              && after.blockAt(local(log.x(), cx), log.y(), local(log.z(), cz))
                  .equals(new BlockState(17, 0))
              && after.blockAt(local(leaf.x(), cx), leaf.y(), local(leaf.z(), cz)).equals(placed),
          "persisted birch leaves 18:10 drift");
      String evidence = "column=" + column + ",support=" + top.x() + ":" + top.y() + ":" + top.z()
          + ":1:0,log=" + log.x() + ":" + log.y() + ":" + log.z() + ":17:0,leaves=" + leaf.x() + ":"
          + leaf.y() + ":" + leaf.z() + ":18:" + placed.metadata()
          + ",persisted=true,clients=2,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=raised-stone+oak17+leaves18:2|cause=packet15-item18:2|wire=packet53-leaves18:"
          + placed.metadata() + "|oracle=live-block18:10+fresh-login|" + evidence;
      System.out.println("WORLDLINE_M292_LEAVES=" + evidence);
      System.out.println("WORLDLINE_M292_TRACE=" + trace);
      System.out.println("WORLDLINE_M292_SIGNATURE=" + sha(trace));
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
    throw new IllegalStateException("no deterministic birch leaves foundation");
  }
  private static void require(boolean v, String m) {
    if (!v)
      throw new IllegalStateException(m);
  }
}
