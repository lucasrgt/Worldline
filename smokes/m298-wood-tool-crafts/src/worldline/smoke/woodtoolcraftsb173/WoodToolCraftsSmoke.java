package worldline.smoke.woodtoolcraftsb173;
import static worldline.b173server.B173FixtureSupport.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Places workbench 58, opens the 3x3 grid, and crafts the wooden tool family. */
public final class WoodToolCraftsSmoke {
  private WoodToolCraftsSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 7)
      throw new IllegalArgumentException(
          "usage: WoodToolCraftsSmoke server.jar workspace port seed username chunkX chunkZ");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    int cx = Integer.parseInt(a[5]), cz = Integer.parseInt(a[6]);
    Duration timeout = Duration.ofSeconds(90);
    B173WoodToolPacketFixture.verify();
    require(user.length() <= 16 && B173WoodToolClick.SWORD == 268 && B173WoodToolClick.PICK == 270
            && B173WoodToolClick.AXE == 271 && B173WoodToolClick.SHOVEL == 269
            && B173WoodToolClick.HOE == 290 && B173WoodToolClick.SWORD != 5,
        "wood-tool identity drifted");
    B173DedicatedServer server =
        new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout), reader = null;
    BlockPosition top, bench;
    int column;
    BlockState placed;
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1, 2, 3},
          new int[] {1, 58, 5, 280}, new int[] {32, 1, 11, 9}, new int[] {0, 0, 0, 0});
      actor.connect();
      actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() == 4, "wood-tool inventory drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      top = foundation(initial, cx, cz);
      column = 0;
      actor.selectHeldSlot(0);
      while (
          water(initial.blockAt(local(top.x(), cx), top.y() + 1, local(top.z(), cz)).legacyId())) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        require(++column <= 15, "water column exceeded wood-tool fixture");
      }
      for (int lift = 0; lift < 8; lift++) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        column++;
      }
      actor.selectHeldSlot(1);
      bench = BlockFace.UP.adjacent(top);
      actor.placeHeldBlock(top, BlockFace.UP);
      placed = new BlockState(58, 0);
      actor.awaitBlock(bench, placed);
      require(actor.sustainTicks(5).blockAt(bench.x(), bench.y(), bench.z()).equals(placed),
          "live workbench 58:0 drift");
      actor.selectHeldSlot(1);
      RemoteContainerWindow opened = actor.openWorkbench(bench, BlockFace.UP);
      require(opened.descriptor().kind() == RemoteWindowKind.WORKBENCH
              && opened.descriptor().containerSlots() == 9 && opened.inventory().size() == 46,
          "workbench window drift");
      int[] ids = new B173WoodToolClick().family(actor);
      require(ids.length == 5 && ids[0] == 268 && ids[1] == 270 && ids[2] == 271 && ids[3] == 269
              && ids[4] == 290,
          "wood-tool family result drift");
      RemoteWindowClosure closure = actor.closeWindow();
      require(closure.proofAction() >= 1 && actor.inventory().occupiedSlots() == 6,
          "wood-tool close drift");
      actor.close();
      awaitPlayers(server, 0);
      server.save();
      reader = new B173WireClient("127.0.0.1", port, user, timeout);
      reader.connect();
      reader.synchronizePose();
      RemoteInventoryView restored = reader.awaitInventory();
      RemoteChunkSnapshot after = reader.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      require(after.blockAt(local(top.x(), cx), top.y(), local(top.z(), cz))
                  .equals(new BlockState(1, 0))
              && after.blockAt(local(bench.x(), cx), bench.y(), local(bench.z(), cz)).equals(placed)
              && hasFamily(restored, ids),
          "persisted wood-tool family drift");
      String evidence = "column=" + column + ",support=" + top.x() + ":" + top.y() + ":" + top.z()
          + ":1:0,workbench=" + bench.x() + ":" + bench.y() + ":" + bench.z()
          + ":58:0,results=268+270+271+269+290,taken=true,persisted=true,clients=2,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=raised-stone+workbench58+planks5x11+sticks280x9|cause=packet15-open+packet102-wood-tool-family|wire=packet100-crafting+packet106-accepted|oracle=result-items268+270+271+269+290+take+fresh-login|"
          + evidence;
      System.out.println("WORLDLINE_M298_TOOLS=" + evidence);
      System.out.println("WORLDLINE_M298_TRACE=" + trace);
      System.out.println("WORLDLINE_M298_SIGNATURE=" + sha(trace));
    } finally {
      actor.close();
      if (reader != null)
        reader.close();
      server.close();
    }
  }
  private static boolean hasFamily(RemoteInventoryView view, int[] ids) {
    boolean[] found = new boolean[ids.length];
    for (int slot = 9; slot <= 44; slot++) {
      if (view.slot(slot).empty())
        continue;
      int id = view.slot(slot).item().legacyId();
      for (int i = 0; i < ids.length; i++)
        if (id == ids[i] && view.slot(slot).item().count() == 1)
          found[i] = true;
    }
    for (boolean v : found)
      if (!v)
        return false;
    return true;
  }
  private static BlockPosition foundation(RemoteChunkSnapshot q, int cx, int cz) {
    for (int x = 4; x <= 11; x++)
      for (int z = 4; z <= 11; z++)
        for (int y = 126; y >= 1; y--)
          if (q.blockAt(x, y, z).legacyId() == 3 && water(q.blockAt(x, y + 1, z).legacyId()))
            return new BlockPosition(cx * 16 + x, y, cz * 16 + z);
    throw new IllegalStateException("no deterministic wood-tool foundation");
  }
  private static void require(boolean v, String m) {
    if (!v)
      throw new IllegalStateException(m);
  }
}
