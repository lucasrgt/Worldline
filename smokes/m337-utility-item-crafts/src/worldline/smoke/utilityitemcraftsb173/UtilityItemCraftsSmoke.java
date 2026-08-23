package worldline.smoke.utilityitemcraftsb173;
import static worldline.b173server.B173FixtureSupport.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Crafts shears 359 and flint-and-steel 259 in 2x2, then empty bucket 325 on a workbench. */
public final class UtilityItemCraftsSmoke {
  private UtilityItemCraftsSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 7)
      throw new IllegalArgumentException(
          "usage: UtilityItemCraftsSmoke server.jar workspace port seed username chunkX chunkZ");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    int cx = Integer.parseInt(a[5]), cz = Integer.parseInt(a[6]);
    Duration timeout = Duration.ofSeconds(90);
    require(user.length() <= 16 && B173UtilityPersonalCrafts.SHEARS.legacyId() == 359
            && B173UtilityPersonalCrafts.FLINT_STEEL.legacyId() == 259
            && B173UtilityWorkbenchCrafts.BUCKET.legacyId() == 325
            && B173UtilityPersonalCrafts.FLINT.legacyId() == 318,
        "utility item identities drifted");
    B173DedicatedServer server =
        new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout), reader = null;
    BlockPosition top, bench;
    int column;
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1, 2, 3},
          new int[] {1, 58, 265, 318}, new int[] {32, 1, 6, 1}, new int[] {0, 0, 0, 0});
      actor.connect();
      actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() == 4, "utility inventory seed drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      top = foundation(initial, cx, cz);
      column = 0;
      actor.selectHeldSlot(0);
      while (
          water(initial.blockAt(local(top.x(), cx), top.y() + 1, local(top.z(), cz)).legacyId())) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        require(++column <= 15, "water column exceeded utility fixture");
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
      worldline.test.WorldlineSmokeAwait.awaitEntity(
          actor, actor::inventory, v -> v.slot(37).empty(), "workbench consumption", 5);
      actor.selectHeldSlot(1);
      B173UtilityPersonalCrafts.apply(actor);
      require(actor.inventory().slot(37).item().equals(B173UtilityPersonalCrafts.SHEARS)
              && actor.inventory().slot(39).item().equals(B173UtilityPersonalCrafts.FLINT_STEEL),
          "live 2x2 utility crafts drifted");
      actor.selectHeldSlot(4);
      actor.openWorkbench(bench, BlockFace.UP);
      B173UtilityWorkbenchCrafts.apply(actor);
      require(B173UtilityWorkbenchCrafts.stored(actor.inventory()), "live bucket 325 drifted");
      actor.closeWindow();
      actor.close();
      awaitPlayers(server, 0);
      server.save();
      require(server.player(user).inventoryItems() == 4, "utility persistence count drift");
      reader = new B173WireClient("127.0.0.1", port, user, timeout);
      reader.connect();
      reader.synchronizePose();
      require(B173UtilityWorkbenchCrafts.stored(reader.awaitInventory()),
          "persisted utility crafts drifted");
      RemoteChunkSnapshot after = reader.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      require(after.blockAt(local(top.x(), cx), top.y(), local(top.z(), cz))
                  .equals(new BlockState(1, 0))
              && after.blockAt(local(bench.x(), cx), bench.y(), local(bench.z(), cz))
                  .equals(new BlockState(58, 0)),
          "persisted workbench 58:0 drift");
      String evidence = "column=" + column + ",support=" + top.x() + ":" + top.y() + ":" + top.z()
          + ":1:0,workbench=" + bench.x() + ":" + bench.y() + ":" + bench.z()
          + ":58:0,shears=359,flintsteel=259,bucket=325,persisted=true,clients=2,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=personal-2x2+workbench58+ingot265x6+flint318|cause=packet102-window0-shears359+flintsteel259+workbench-bucket325|wire=packet106-accepted+packet200-craft-stat|oracle=result359+result259+result325+fresh-login|"
          + evidence;
      System.out.println("WORLDLINE_M337_CRAFTS=" + evidence);
      System.out.println("WORLDLINE_M337_TRACE=" + trace);
      System.out.println("WORLDLINE_M337_SIGNATURE=" + sha(trace));
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
    throw new IllegalStateException("no deterministic utility-craft foundation");
  }
  private static void require(boolean v, String m) {
    if (!v)
      throw new IllegalStateException(m);
  }
}
