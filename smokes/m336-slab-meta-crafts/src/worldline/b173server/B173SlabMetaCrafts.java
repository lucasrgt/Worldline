package worldline.b173server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import worldline.api.RemoteContainerWindow;
import worldline.api.RemoteInventorySlot;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteItemStack;
import worldline.api.RemoteWindowKind;

/** Workbench crafts sandstone slab 44:1, wood slab 44:2, and cobble slab 44:3. */
public final class B173SlabMetaCrafts {
  public static final RemoteItemStack SANDSTONE = new RemoteItemStack(44, 3, 1);
  public static final RemoteItemStack WOOD = new RemoteItemStack(44, 3, 2);
  public static final RemoteItemStack COBBLE = new RemoteItemStack(44, 3, 3);
  private static final RemoteItemStack WOOD_PLATE = new RemoteItemStack(72, 1, 0);
  private static final int[] SLABS = {1, 2, 3};
  private final B173PlayChannel channel;
  private final B173PlayInbound inbound;
  private int windowId, action;
  private long epoch = -1L;

  private B173SlabMetaCrafts(B173WireClient actor) {
    channel = actor.channel();
    inbound = channel.inbound();
  }

  public static void apply(B173WireClient actor) {
    try {
      new B173SlabMetaCrafts(actor).run();
    } catch (IOException error) {
      throw new IllegalStateException("slab-meta crafts failed", error);
    }
  }

  private void run() throws IOException {
    require(SANDSTONE.legacyId() == 44 && SANDSTONE.damage() == 1 && WOOD.legacyId() == 44
            && WOOD.damage() == 2 && COBBLE.legacyId() == 44 && COBBLE.damage() == 3,
        "slab metadata result identity drifted");
    RemoteContainerWindow active = inbound.activeWindow();
    if (active.descriptor().kind() != RemoteWindowKind.WORKBENCH || active.inventory().size() != 46
        || !inbound.cursorObserved() || inbound.cursor() != null)
      throw new IllegalStateException("invalid slab-meta workbench boundary");
    craft(38, item(24, 3, 0), SLABS, new RemoteItemStack[] {null, null, SANDSTONE}, SANDSTONE);
    craft(39, item(5, 3, 0), SLABS, new RemoteItemStack[] {null, WOOD_PLATE, WOOD}, WOOD);
    craft(40, item(4, 3, 0), SLABS, new RemoteItemStack[] {null, null, COBBLE}, COBBLE);
  }

  private void craft(int personal, RemoteItemStack ingredient, int[] cells, RemoteItemStack[] mid,
      RemoteItemStack result) throws IOException {
    int combined = personal + 1;
    pickup(combined, personal, ingredient);
    RemoteItemStack cursor = ingredient, one = item(ingredient.legacyId(), 1, ingredient.damage());
    for (int index = 0; index < cells.length; index++) {
      cursor = dec(cursor);
      RemoteInventoryView after = replace(window(), cells[index], one);
      if (mid[index] != null)
        after = replace(after, 0, mid[index]);
      step(cells[index], 1, null, after, personal(), personal(), cursor);
    }
    RemoteInventoryView taken = clearOwned(window());
    takeResult(result, taken, 16842752 + result.legacyId());
    RemoteInventoryView stored = replace(window(), combined, result);
    step(combined, 0, null, stored, personal(), replace(personal(), personal, result), null);
  }

  private void takeResult(RemoteItemStack result, RemoteInventoryView taken, int statisticId)
      throws IOException {
    send(new B173ContainerStep(windowId(), action + 1, 0, result, window(), taken, personal(),
        personal(), inbound.cursor(), result, statisticId, result.count()));
    action++;
    inbound.awaitContainerTransaction();
  }

  private void pickup(int combined, int personal, RemoteItemStack ingredient) throws IOException {
    RemoteInventoryView before = window();
    if (!emptyOwned(before) || before.slot(combined).empty()
        || !before.slot(combined).item().equals(ingredient)
        || !personal().slot(personal).item().equals(ingredient) || inbound.cursor() != null)
      throw new IllegalStateException("slab-meta ingredient preflight failed");
    step(combined, 0, ingredient, replace(before, combined, null), personal(),
        replace(personal(), personal, null), ingredient);
  }

  private void step(int slot, int button, RemoteItemStack predicted, RemoteInventoryView after,
      RemoteInventoryView personalBefore, RemoteInventoryView personalAfter,
      RemoteItemStack cursorAfter) throws IOException {
    send(new B173ContainerStep(windowId(), action + 1, slot, button, predicted, window(), after,
        personalBefore, personalAfter, inbound.cursor(), cursorAfter));
    action++;
    inbound.awaitContainerTransaction();
  }

  private void send(B173ContainerStep value) throws IOException {
    inbound.beginContainerTransaction(value);
    B173ContainerPacket.write(
        channel.output, windowId(), value.slot, value.button, value.action, value.predicted);
    channel.output.flush();
  }

  private int windowId() {
    long current = inbound.activeWindowEpoch();
    if (epoch != current) {
      epoch = current;
      windowId = window().windowId();
      action = 0;
    }
    return windowId;
  }

  private RemoteInventoryView window() {
    return inbound.activeWindow().inventory();
  }
  private RemoteInventoryView personal() {
    return inbound.inventory();
  }

  private static RemoteInventoryView clearOwned(RemoteInventoryView view) {
    RemoteInventoryView next = view;
    for (int slot = 0; slot < 10; slot++)
      next = replace(next, slot, null);
    return next;
  }

  private static RemoteInventoryView replace(
      RemoteInventoryView source, int slot, RemoteItemStack item) {
    List<RemoteInventorySlot> slots = new ArrayList<RemoteInventorySlot>(source.slots());
    slots.set(slot, new RemoteInventorySlot(slot, item));
    return new RemoteInventoryView(source.windowId(), slots);
  }

  private static boolean emptyOwned(RemoteInventoryView view) {
    for (int slot = 0; slot < 10; slot++)
      if (!view.slot(slot).empty())
        return false;
    return true;
  }

  private static RemoteItemStack item(int id, int count, int damage) {
    return new RemoteItemStack(id, count, damage);
  }

  private static RemoteItemStack dec(RemoteItemStack stack) {
    return stack.count() == 1
        ? null
        : new RemoteItemStack(stack.legacyId(), stack.count() - 1, stack.damage());
  }

  private static void require(boolean value, String message) {
    if (!value)
      throw new IllegalStateException(message);
  }
}
