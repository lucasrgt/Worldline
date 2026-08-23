package worldline.smoke.milkbucketb173;
import static worldline.b173server.B173FixtureSupport.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Uses official milk bucket 335 through Packet15 air-use and freezes empty bucket 325 plus health. */
public final class MilkBucketSmoke {
  private MilkBucketSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 7)
      throw new IllegalArgumentException(
          "usage: MilkBucketSmoke server.jar workspace port seed username chunkX chunkZ");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    int cx = Integer.parseInt(a[5]), cz = Integer.parseInt(a[6]);
    Duration timeout = Duration.ofSeconds(90);
    B173DedicatedServer server =
        new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout), reader = null;
    RemoteItemStack milk = new RemoteItemStack(335, 1, 0), empty = new RemoteItemStack(325, 1, 0);
    BlockPosition top, cell;
    int column;
    PlayerPose pose;
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1},
          new int[] {1, 335}, new int[] {48, 1}, new int[] {0, 0}, 20);
      actor.connect();
      pose = actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() == 2
              && actor.awaitInventory().slot(37).item().equals(milk),
          "milk inventory drift");
      require(actor.awaitHealth(20) == 20, "seeded milk health drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      top = foundation(initial, cx, cz);
      column = 0;
      actor.selectHeldSlot(0);
      while (
          water(initial.blockAt(local(top.x(), cx), top.y() + 1, local(top.z(), cz)).legacyId())) {
        top = place(actor, top, BlockFace.UP, 1);
        pose = actor.moveAndObserve(0D, 1D, 0D, 1).resulting();
        require(++column <= 15, "water column exceeded milk fixture");
      }
      for (int lift = 0; lift < 8; lift++) {
        top = place(actor, top, BlockFace.UP, 1);
        pose = actor.moveAndObserve(0D, 1D, 0D, 1).resulting();
        column++;
      }
      for (BlockFace wall :
          new BlockFace[] {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST})
        place(actor, place(actor, top, wall, 1), BlockFace.UP, 1);
      while (pose.y() > top.y() + 1.01D)
        pose = actor.moveAndObserve(0D, -1D, 0D, 1).resulting();
      pose = actor.moveAndObserve(0D, 1D, 0D, 1).resulting();
      pose = actor.moveAndObserve(0D, 0D, 1D, 1).resulting();
      cell = BlockFace.UP.adjacent(top);
      require(worldline.test.WorldlineSmokeAwait.observe(actor, 5)
                  .blockAt(cell.x(), cell.y(), cell.z())
                  .equals(new BlockState(0, 0))
              && actor.health() == 20,
          "empty basin drift");
      actor.selectHeldSlot(1);
      actor.look(180F, 70F);
      actor.useHeldItemOnBlock(top, BlockFace.UP);
      actor.useSelectedItemInAir();
      RemoteInventoryView consumed =
          worldline.test.WorldlineSmokeAwait.awaitSlot(actor, actor::inventory, 37, empty, 20);
      RemoteInventorySlot held = consumed.slot(37);
      require(actor.health() == 20
              && worldline.test.WorldlineSmokeAwait.observe(actor, 5)
                  .blockAt(cell.x(), cell.y(), cell.z())
                  .equals(new BlockState(0, 0)),
          "milk health or basin drift health=" + actor.health());
      require(!held.empty() && held.item().equals(empty),
          "milk empty-bucket drift held=" + (held.empty() ? "empty" : held.item()));
      actor.close();
      awaitPlayers(server, 0);
      server.save();
      reader = new B173WireClient("127.0.0.1", port, user, timeout);
      reader.connect();
      reader.synchronizePose();
      require(reader.awaitHealth(20) == 20 && reader.awaitInventory().slot(37).item().equals(empty),
          "persisted milk bucket drift");
      String evidence = "column=" + column + ",floor=" + top.x() + ":" + top.y() + ":" + top.z()
          + ":1:0,health=20->20,heal=0,held=335:1:0->325:1:0,persisted=true,clients=2,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=raised-stone-basin+milk335|cause=packet15-dir255-item335|wire=packet103-bucket325|oracle=itembucket-milk-empty-no-heal+fresh-login|"
          + evidence;
      System.out.println("WORLDLINE_M267_MILK=" + evidence);
      System.out.println("WORLDLINE_M267_TRACE=" + trace);
      System.out.println("WORLDLINE_M267_SIGNATURE=" + sha(trace));
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
    throw new IllegalStateException("no deterministic milk foundation");
  }
  private static void require(boolean v, String m) {
    if (!v)
      throw new IllegalStateException(m);
  }
}
