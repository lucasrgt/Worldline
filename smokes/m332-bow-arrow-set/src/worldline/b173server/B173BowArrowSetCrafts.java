package worldline.b173server;

import java.util.ArrayList;
import java.util.List;
import worldline.api.RemoteInventorySlot;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteItemStack;

/** Workbench-crafts bow 261 and arrows 262 from sticks, string, flint, and feather. */
public final class B173BowArrowSetCrafts {
  public static final int BOW = 261, ARROW = 262, STICK = 280, STRING = 287, FLINT = 318,
                          FEATHER = 288;
  private B173BowArrowSetCrafts() {
  }

  public static int apply(B173WireClient actor) throws Exception {
    require(BOW == 261 && ARROW == 262 && STICK == 280 && STRING == 287 && FLINT == 318
            && FEATHER == 288,
        "bow-arrow craft identities drifted");
    int[] n = new int[] {0};
    take(actor, n, 38);
    put(actor, n, 2, 0, 0);
    put(actor, n, 4, 0, 0);
    put(actor, n, 8, 0, 0);
    store(actor, n, 38);
    take(actor, n, 39);
    put(actor, n, 3, 0, 0);
    put(actor, n, 6, 0, 0);
    put(actor, n, 9, BOW, 1);
    store(actor, n, 39);
    takeResult(actor, n, BOW, 1, new int[] {2, 4, 8, 3, 6, 9});
    store(actor, n, 37);
    take(actor, n, 40);
    put(actor, n, 2, 0, 0);
    store(actor, n, 40);
    take(actor, n, 38);
    put(actor, n, 5, 0, 0);
    store(actor, n, 38);
    take(actor, n, 41);
    put(actor, n, 8, ARROW, 4);
    store(actor, n, 41);
    takeResult(actor, n, ARROW, 4, new int[] {2, 5, 8});
    store(actor, n, 38);
    require(has(actor, 37, BOW, 1) && has(actor, 38, ARROW, 4),
        "crafted bow 261 and arrows 262 drifted");
    return n[0];
  }

  private static void take(B173WireClient a, int[] n, int personal) throws Exception {
    B173PlayInbound in = a.channel().inbound();
    RemoteInventoryView w = in.activeWindow().inventory(), p = in.inventory();
    int comb = personal + 1;
    require(!w.slot(comb).empty(), "take requires occupied personal material");
    RemoteItemStack stack = w.slot(comb).item();
    click(a, ++n[0], comb, 0, stack, set(w, comb, null), set(p, personal, null), stack);
  }

  private static void put(B173WireClient a, int[] n, int slot, int result, int count)
      throws Exception {
    B173PlayInbound in = a.channel().inbound();
    RemoteInventoryView w = in.activeWindow().inventory();
    RemoteItemStack cur = in.cursor();
    require(cur != null && w.slot(slot).empty(),
        "right-place requires cursor item and empty matrix slot");
    RemoteInventoryView after = set(w, slot, item(cur.legacyId(), 1, cur.damage()));
    if (result > 0)
      after = set(after, 0, item(result, count, 0));
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

  private static void takeResult(B173WireClient a, int[] n, int result, int count, int[] slots)
      throws Exception {
    B173PlayInbound in = a.channel().inbound();
    RemoteInventoryView w = in.activeWindow().inventory(), p = in.inventory();
    RemoteItemStack tool = item(result, count, 0);
    require(!w.slot(0).empty() && w.slot(0).item().equals(tool) && in.cursor() == null,
        "modeled craft result " + result + " absent");
    RemoteInventoryView after = set(w, 0, null);
    for (int s : slots)
      after = set(after, s, null);
    B173ContainerStep step = new B173ContainerStep(
        w.windowId(), ++n[0], 0, tool, w, after, p, p, null, tool, 16842752 + result, count);
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

  private static boolean has(B173WireClient a, int slot, int id, int count) {
    RemoteInventorySlot v = a.inventory().slot(slot);
    return !v.empty() && v.item().equals(item(id, count, 0));
  }

  private static void require(boolean v, String m) {
    if (!v)
      throw new IllegalStateException(m);
  }
}
