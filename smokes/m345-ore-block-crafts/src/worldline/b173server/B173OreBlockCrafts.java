package worldline.b173server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import worldline.api.RemoteContainerWindow;
import worldline.api.RemoteInventorySlot;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteItemStack;
import worldline.api.RemoteWindowKind;

/** Workbench crafts gold 41, iron 42, diamond 57, and lapis 22 from nine ingredients. */
public final class B173OreBlockCrafts {
  public static final RemoteItemStack GOLD = new RemoteItemStack(41, 1, 0);
  public static final RemoteItemStack IRON = new RemoteItemStack(42, 1, 0);
  public static final RemoteItemStack DIAMOND = new RemoteItemStack(57, 1, 0);
  public static final RemoteItemStack LAPIS = new RemoteItemStack(22, 1, 0);
  private static final int[] CELLS = {1, 2, 3, 4, 5, 6, 7, 8, 9};
  private final B173PlayChannel channel;
  private final B173PlayInbound inbound;
  private int windowId, action;
  private long epoch = -1L;

  private B173OreBlockCrafts(B173WireClient actor) {
    channel = actor.channel();
    inbound = channel.inbound();
  }

  public static void apply(B173WireClient actor) {
    try {
      new B173OreBlockCrafts(actor).run();
    } catch (IOException error) {
      throw new IllegalStateException("ore-block crafts failed", error);
    }
  }

  private void run() throws IOException {
    require(GOLD.legacyId() == 41 && IRON.legacyId() == 42 && DIAMOND.legacyId() == 57
            && LAPIS.legacyId() == 22,
        "ore-block result identity drifted");
    RemoteContainerWindow active = inbound.activeWindow();
    if (active.descriptor().kind() != RemoteWindowKind.WORKBENCH || active.inventory().size() != 46
        || !inbound.cursorObserved() || inbound.cursor() != null)
      throw new IllegalStateException("invalid ore-block workbench boundary");
    craft(38, item(266, 9, 0), GOLD);
    craft(39, item(265, 9, 0), IRON);
    craft(40, item(264, 9, 0), DIAMOND);
    craft(41, item(351, 9, 4), LAPIS);
  }

  private void craft(int personal, RemoteItemStack ingredient, RemoteItemStack result)
      throws IOException {
    int combined = personal + 1;
    pickup(combined, personal, ingredient);
    RemoteItemStack cursor = ingredient, one = item(ingredient.legacyId(), 1, ingredient.damage());
    for (int index = 0; index < CELLS.length; index++) {
      cursor = dec(cursor);
      RemoteInventoryView after = replace(window(), CELLS[index], one);
      if (index == CELLS.length - 1)
        after = replace(after, 0, result);
      step(CELLS[index], 1, null, after, personal(), personal(), cursor);
    }
    if (cursor != null || window().slot(0).empty() || !window().slot(0).item().equals(result))
      throw new IllegalStateException("ore-block result " + result.legacyId() + " absent");
    takeResult(result, clearOwned(window()), 16842752 + result.legacyId());
    step(combined, 0, null, replace(window(), combined, result), personal(),
        replace(personal(), personal, result), null);
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
      throw new IllegalStateException("ore-block ingredient preflight failed");
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
