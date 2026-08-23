package worldline.b173server;
import static worldline.b173server.B173FixtureSupport.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.*;
import worldline.api.*;

/** Workbench-crafts gold and diamond tool families against one official server JVM. */
public final class GoldDiamondToolCraftsSmoke {
  private GoldDiamondToolCraftsSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 7)
      throw new IllegalArgumentException(
          "usage: GoldDiamondToolCraftsSmoke server.jar workspace port seed username chunkX chunkZ");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    int cx = Integer.parseInt(a[5]), cz = Integer.parseInt(a[6]);
    Duration timeout = Duration.ofSeconds(90);
    B173DedicatedServer server =
        new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout);
    BlockPosition top, bench;
    int column;
    int[] n = new int[] {0};
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1, 2, 3, 4},
          new int[] {1, 58, 266, 264, 280}, new int[] {32, 1, 9, 9, 14}, new int[] {0, 0, 0, 0, 0});
      actor.connect();
      actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() == 5, "tool-craft inventory seed drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      top = foundation(initial, cx, cz);
      column = 0;
      actor.selectHeldSlot(0);
      while (
          water(initial.blockAt(local(top.x(), cx), top.y() + 1, local(top.z(), cz)).legacyId())) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        require(++column <= 15, "water column exceeded tool-craft fixture");
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
      worldline.test.WorldlineSmokeAwait.observe(actor, 5);
      actor.selectHeldSlot(1);
      require(actor.inventory().slot(37).empty(), "workbench hand was not consumed");
      actor.openWorkbench(bench, BlockFace.UP);
      craft(actor, n, 38, 40, 37, new int[] {2, 5}, new int[] {8}, 283);
      craft(actor, n, 38, 40, 41, new int[] {1, 2, 3}, new int[] {5, 8}, 285);
      craft(actor, n, 38, 40, 42, new int[] {1, 2, 4}, new int[] {5, 8}, 286);
      craft(actor, n, 38, 40, 43, new int[] {2}, new int[] {5, 8}, 284);
      craft(actor, n, 39, 40, 44, new int[] {2, 5}, new int[] {8}, 276);
      craft(actor, n, 39, 40, 9, new int[] {1, 2, 3}, new int[] {5, 8}, 278);
      craft(actor, n, 39, 40, 10, new int[] {1, 2, 4}, new int[] {5, 8}, 279);
      craft(actor, n, 39, 40, 11, new int[] {2}, new int[] {5, 8}, 277);
      require(has(actor, 37, 283) && has(actor, 41, 285) && has(actor, 42, 286)
              && has(actor, 43, 284) && has(actor, 44, 276) && has(actor, 9, 278)
              && has(actor, 10, 279) && has(actor, 11, 277),
          "gold/diamond tool results drifted");
      actor.closeWindow();
      actor.close();
      awaitPlayers(server, 0);
      server.save();
      require(server.player(user).inventoryItems() == 9, "crafted tool persistence drift");
      String evidence = "gold=283+285+286+284,diamond=276+278+279+277,column=" + column
          + ",support=" + top.x() + ":" + top.y() + ":" + top.z() + ":1:0,workbench=" + bench.x()
          + ":" + bench.y() + ":" + bench.z() + ":58:0,actions=" + n[0]
          + ",persisted=true,clients=1,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=workbench58+gold266x9+diamond264x9+stick280x14|cause=packet102-workbench-matrix+result-take|wire=packet106-accepted+packet200-craft-stat|oracle=gold-family-283-284-285-286+diamond-family-276-277-278-279|"
          + evidence;
      System.out.println("WORLDLINE_M318_CRAFT=" + evidence);
      System.out.println("WORLDLINE_M318_TRACE=" + trace);
      System.out.println("WORLDLINE_M318_SIGNATURE=" + sha(trace));
    } finally {
      actor.close();
      server.close();
    }
  }
  private static void craft(B173WireClient a, int[] n, int mat, int stick, int out, int[] mats,
      int[] stks, int result) throws Exception {
    take(a, n, mat);
    for (int i = 0; i < mats.length; i++)
      put(a, n, mats[i], 0);
    store(a, n, mat);
    take(a, n, stick);
    for (int i = 0; i < stks.length; i++)
      put(a, n, stks[i], i == stks.length - 1 ? result : 0);
    store(a, n, stick);
    takeResult(a, n, result, mats, stks);
    store(a, n, out);
  }
  private static void take(B173WireClient a, int[] n, int personal) throws Exception {
    B173PlayInbound in = a.channel().inbound();
    RemoteInventoryView w = in.activeWindow().inventory(), p = in.inventory();
    int comb = personal + 1;
    require(!w.slot(comb).empty(), "take requires occupied personal material");
    RemoteItemStack stack = w.slot(comb).item();
    click(a, ++n[0], comb, 0, stack, set(w, comb, null), set(p, personal, null), stack);
  }
  private static void put(B173WireClient a, int[] n, int slot, int result) throws Exception {
    B173PlayInbound in = a.channel().inbound();
    RemoteInventoryView w = in.activeWindow().inventory();
    RemoteItemStack cur = in.cursor();
    require(cur != null && w.slot(slot).empty(),
        "right-place requires cursor item and empty matrix slot");
    RemoteInventoryView after = set(w, slot, item(cur.legacyId(), 1, cur.damage()));
    if (result > 0)
      after = set(after, 0, item(result, 1, 0));
    click(a, ++n[0], slot, 1, null, after, in.inventory(), dec(cur));
  }
  private static void store(B173WireClient a, int[] n, int personal) throws Exception {
    B173PlayInbound in = a.channel().inbound();
    RemoteItemStack cur = in.cursor();
    if (cur == null)
      return;
    RemoteInventoryView w = in.activeWindow().inventory(), p = in.inventory();
    int comb = personal + 1;
    require(w.slot(comb).empty() && p.slot(personal).empty(),
        "store requires empty personal destination");
    click(a, ++n[0], comb, 0, null, set(w, comb, cur), set(p, personal, cur), null);
  }
  private static void takeResult(B173WireClient a, int[] n, int result, int[] mats, int[] stks)
      throws Exception {
    B173PlayInbound in = a.channel().inbound();
    RemoteInventoryView w = in.activeWindow().inventory(), p = in.inventory();
    RemoteItemStack tool = item(result, 1, 0);
    require(!w.slot(0).empty() && w.slot(0).item().equals(tool) && in.cursor() == null,
        "modeled tool result absent");
    RemoteInventoryView after = set(w, 0, null);
    for (int s : mats)
      after = set(after, s, null);
    for (int s : stks)
      after = set(after, s, null);
    B173ContainerStep step = new B173ContainerStep(
        w.windowId(), ++n[0], 0, tool, w, after, p, p, null, tool, 16842752 + result, 1);
    in.beginContainerTransaction(step);
    B173ContainerPacket.write(a.channel().output, w.windowId(), 0, 0, n[0], tool);
    a.channel().output.flush();
    in.awaitContainerTransaction();
  }
  private static void click(B173WireClient a, int action, int slot, int button,
      RemoteItemStack predicted, RemoteInventoryView after, RemoteInventoryView personalAfter,
      RemoteItemStack cursorAfter) throws Exception {
    B173PlayInbound in = a.channel().inbound();
    RemoteInventoryView w = in.activeWindow().inventory(), p = in.inventory();
    B173ContainerStep step = new B173ContainerStep(w.windowId(), action, slot, button, predicted, w,
        after, p, personalAfter, in.cursor(), cursorAfter);
    in.beginContainerTransaction(step);
    B173ContainerPacket.write(a.channel().output, w.windowId(), slot, button, action, predicted);
    a.channel().output.flush();
    in.awaitContainerTransaction();
  }
  private static RemoteInventoryView set(RemoteInventoryView v, int slot, RemoteItemStack item) {
    List<RemoteInventorySlot> s = new ArrayList<RemoteInventorySlot>(v.slots());
    s.set(slot, new RemoteInventorySlot(slot, item));
    return new RemoteInventoryView(v.windowId(), s);
  }
  private static RemoteItemStack item(int id, int count, int damage) {
    return new RemoteItemStack(id, count, damage);
  }
  private static RemoteItemStack dec(RemoteItemStack s) {
    return s.count() == 1 ? null : item(s.legacyId(), s.count() - 1, s.damage());
  }
  private static boolean has(B173WireClient a, int slot, int id) {
    RemoteInventorySlot v = a.inventory().slot(slot);
    return !v.empty() && v.item().equals(item(id, 1, 0));
  }
  private static BlockPosition foundation(RemoteChunkSnapshot q, int cx, int cz) {
    for (int x = 4; x <= 11; x++)
      for (int z = 4; z <= 11; z++)
        for (int y = 126; y >= 1; y--)
          if (q.blockAt(x, y, z).legacyId() == 3 && water(q.blockAt(x, y + 1, z).legacyId()))
            return new BlockPosition(cx * 16 + x, y, cz * 16 + z);
    throw new IllegalStateException("no deterministic tool-craft foundation");
  }
  private static void require(boolean v, String m) {
    if (!v)
      throw new IllegalStateException(m);
  }
}
