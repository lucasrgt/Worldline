package worldline.smoke.oreblockcraftsb173;
import static worldline.b173server.B173FixtureSupport.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Workbench-crafts gold 41, iron 42, diamond 57, and lapis 22 from nine ingredients. */
public final class OreBlockCraftsSmoke {
  private OreBlockCraftsSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 7)
      throw new IllegalArgumentException(
          "usage: OreBlockCraftsSmoke server.jar workspace port seed username chunkX chunkZ");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    int cx = Integer.parseInt(a[5]), cz = Integer.parseInt(a[6]);
    Duration timeout = Duration.ofSeconds(90);
    B173DedicatedServer server =
        new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout), reader = null;
    BlockPosition top, bench;
    int column;
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1, 2, 3, 4, 5},
          new int[] {1, 58, 266, 265, 264, 351}, new int[] {32, 1, 9, 9, 9, 9},
          new int[] {0, 0, 0, 0, 0, 4});
      actor.connect();
      actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() == 6, "ore-block inventory seed drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      top = foundation(initial, cx, cz);
      column = 0;
      actor.selectHeldSlot(0);
      while (
          water(initial.blockAt(local(top.x(), cx), top.y() + 1, local(top.z(), cz)).legacyId())) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        require(++column <= 15, "water column exceeded ore-block fixture");
      }
      for (int lift = 0; lift < 8; lift++) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        column++;
      }
      actor.selectHeldSlot(1);
      bench = BlockFace.UP.adjacent(top);
      actor.placeHeldBlock(top, BlockFace.UP);
      actor.awaitBlock(bench, new BlockState(58, 0));
      actor.sustainTicks(5);
      actor.selectHeldSlot(1);
      actor.openWorkbench(bench, BlockFace.UP);
      B173OreBlockCrafts.apply(actor);
      requireCrafts(actor.inventory());
      actor.closeWindow();
      actor.close();
      awaitPlayers(server, 0);
      server.save();
      require(server.player(user).inventoryItems() == 5, "ore-block persistence count drift");
      reader = new B173WireClient("127.0.0.1", port, user, timeout);
      reader.connect();
      reader.synchronizePose();
      requireCrafts(reader.awaitInventory());
      RemoteChunkSnapshot after = reader.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      require(after.blockAt(local(top.x(), cx), top.y(), local(top.z(), cz))
                  .equals(new BlockState(1, 0))
              && after.blockAt(local(bench.x(), cx), bench.y(), local(bench.z(), cz))
                  .equals(new BlockState(58, 0)),
          "persisted workbench 58:0 drift");
      String evidence = "column=" + column + ",support=" + top.x() + ":" + top.y() + ":" + top.z()
          + ":1:0,workbench=" + bench.x() + ":" + bench.y() + ":" + bench.z()
          + ":58:0,gold=41,iron=42,diamond=57,lapis=22,persisted=true,clients=2,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=workbench58+gold266x9+iron265x9+diamond264x9+lapis351x9:4|cause=packet102-workbench-ore-block-family|wire=packet106-accepted+packet200-craft-stat|oracle=craft-output-41-42-57-22+fresh-login+not-place|"
          + evidence;
      System.out.println("WORLDLINE_M345_CRAFTS=" + evidence);
      System.out.println("WORLDLINE_M345_TRACE=" + trace);
      System.out.println("WORLDLINE_M345_SIGNATURE=" + sha(trace));
    } finally {
      actor.close();
      if (reader != null)
        reader.close();
      server.close();
    }
  }
  private static void requireCrafts(RemoteInventoryView view) {
    require(view.slot(38).item().equals(B173OreBlockCrafts.GOLD)
            && view.slot(39).item().equals(B173OreBlockCrafts.IRON)
            && view.slot(40).item().equals(B173OreBlockCrafts.DIAMOND)
            && view.slot(41).item().equals(B173OreBlockCrafts.LAPIS) && view.occupiedSlots() == 5,
        "ore-block crafted inventory drift");
  }
  private static BlockPosition foundation(RemoteChunkSnapshot q, int cx, int cz) {
    for (int x = 4; x <= 11; x++)
      for (int z = 4; z <= 11; z++)
        for (int y = 126; y >= 1; y--)
          if (q.blockAt(x, y, z).legacyId() == 3 && water(q.blockAt(x, y + 1, z).legacyId()))
            return new BlockPosition(cx * 16 + x, y, cz * 16 + z);
    throw new IllegalStateException("no deterministic ore-block foundation");
  }
  private static void require(boolean v, String m) {
    if (!v)
      throw new IllegalStateException(m);
  }
}
