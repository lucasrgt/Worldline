package worldline.smoke.stonetoolcraftsb173;
import static worldline.b173server.B173FixtureSupport.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Workbench-crafts official stone tools from cobble 4 and sticks 280. */
public final class StoneToolCraftsSmoke {
  private StoneToolCraftsSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 7)
      throw new IllegalArgumentException(
          "usage: StoneToolCraftsSmoke server.jar workspace port seed username chunkX chunkZ");
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
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1, 2, 3},
          new int[] {1, 58, 4, 280}, new int[] {32, 1, 64, 64}, new int[] {0, 0, 0, 0});
      actor.connect();
      actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() == 4, "stone-tool inventory seed drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      top = foundation(initial, cx, cz);
      column = 0;
      actor.selectHeldSlot(0);
      while (
          water(initial.blockAt(local(top.x(), cx), top.y() + 1, local(top.z(), cz)).legacyId())) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        require(++column <= 15, "water column exceeded stone-tool fixture");
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
      actor.openWorkbench(bench, BlockFace.UP);
      int[] crafted = B173StoneToolCrafts.apply(actor);
      require(family(actor.inventory()) && count(actor.inventory(), 4) == 53
              && count(actor.inventory(), 280) == 55 && woodless(actor.inventory()),
          "live stone-tool family drift");
      RemoteWindowClosure closed = actor.closeWindow();
      require(closed != null, "workbench close drift");
      actor.close();
      awaitPlayers(server, 0);
      server.save();
      reader = new B173WireClient("127.0.0.1", port, user, timeout);
      reader.connect();
      reader.synchronizePose();
      RemoteInventoryView after = reader.awaitInventory();
      require(family(after) && count(after, 4) == 53 && count(after, 280) == 55 && woodless(after),
          "persisted stone-tool family drift");
      String evidence = "results=" + crafted[0] + "," + crafted[1] + "," + crafted[2] + ","
          + crafted[3] + "," + crafted[4] + ",left=4:53+280:55,column=" + column
          + ",workbench=" + bench.x() + ":" + bench.y() + ":" + bench.z()
          + ":58:0,persisted=true,clients=2,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=workbench58+cobble4+stick280|cause=packet102-craft-stone-tools|wire=packet104-results-272,273,274,275,291|oracle=workbench-family+fresh-login|"
          + evidence;
      System.out.println("WORLDLINE_M299_CRAFT=" + evidence);
      System.out.println("WORLDLINE_M299_TRACE=" + trace);
      System.out.println("WORLDLINE_M299_SIGNATURE=" + sha(trace));
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
    throw new IllegalStateException("no deterministic stone-tool foundation");
  }
  private static boolean family(RemoteInventoryView v) {
    return has(v, 272) && has(v, 273) && has(v, 274) && has(v, 275) && has(v, 291);
  }
  private static boolean woodless(RemoteInventoryView v) {
    return !has(v, 268) && !has(v, 269) && !has(v, 270) && !has(v, 271) && !has(v, 290);
  }
  private static boolean has(RemoteInventoryView v, int id) {
    for (int s = 9; s <= 44; s++)
      if (!v.slot(s).empty() && v.slot(s).item().legacyId() == id)
        return true;
    return false;
  }
  private static int count(RemoteInventoryView v, int id) {
    int n = 0;
    for (int s = 9; s <= 44; s++)
      if (!v.slot(s).empty() && v.slot(s).item().legacyId() == id)
        n += v.slot(s).item().count();
    return n;
  }
  private static void require(boolean v, String m) {
    if (!v)
      throw new IllegalStateException(m);
  }
}
