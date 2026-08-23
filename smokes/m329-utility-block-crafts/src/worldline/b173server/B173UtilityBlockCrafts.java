package worldline.b173server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import worldline.api.RemoteContainerWindow;
import worldline.api.RemoteInventorySlot;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteItemStack;
import worldline.api.RemoteWindowKind;

/** Workbench crafts fence 85, ladder 65, and bookshelf 47. */
public final class B173UtilityBlockCrafts {
  public static final RemoteItemStack FENCE = new RemoteItemStack(85, 2, 0);
  public static final RemoteItemStack LADDER = new RemoteItemStack(65, 2, 0);
  public static final RemoteItemStack BOOKSHELF = new RemoteItemStack(47, 1, 0);
  private static final int[] FENCES = {1, 2, 3, 4, 5, 6}, LADDERS = {1, 3, 4, 5, 6, 7, 9};
  private static final int[] BOOKS = {4, 5, 6}, PLANKS = {1, 2, 3, 7, 8, 9};
  private final B173PlayChannel channel;
  private final B173PlayInbound inbound;
  private int windowId, action;
  private long epoch = -1L;

  private B173UtilityBlockCrafts(B173WireClient actor) {
    channel = actor.channel();
    inbound = channel.inbound();
  }

  public static void apply(B173WireClient actor) {
    try {
      new B173UtilityBlockCrafts(actor).run();
    } catch (IOException error) {
      throw new IllegalStateException("utility-block crafts failed", error);
    }
  }

  private void run() throws IOException {
    require(FENCE.legacyId() == 85 && LADDER.legacyId() == 65 && BOOKSHELF.legacyId() == 47,
        "utility-block result identity drifted");
    RemoteContainerWindow active = inbound.activeWindow();
    if (active.descriptor().kind() != RemoteWindowKind.WORKBENCH || active.inventory().size() != 46
        || !inbound.cursorObserved() || inbound.cursor() != null)
      throw new IllegalStateException("invalid utility-block workbench boundary");
    craft(38, item(280, 6, 0), FENCES, new RemoteItemStack[] {null, null, null, null, null, FENCE},
        FENCE);
    craft(39, item(280, 7, 0), LADDERS,
        new RemoteItemStack[] {null, null, null, null, null, null, LADDER}, LADDER);
    fill(41, item(340, 3, 0), BOOKS, new RemoteItemStack[] {null, null, null}, true);
    fill(40, item(5, 6, 0), PLANKS, new RemoteItemStack[] {null, null, null, null, null, BOOKSHELF},
        false);
    finish(40, BOOKSHELF);
  }

  private void craft(int personal, RemoteItemStack ingredient, int[] cells, RemoteItemStack[] mid,
      RemoteItemStack result) throws IOException {
    fill(personal, ingredient, cells, mid, true);
    finish(personal, result);
  }

  private void fill(int personal, RemoteItemStack ingredient, int[] cells, RemoteItemStack[] mid,
      boolean emptyMatrix) throws IOException {
    int combined = personal + 1;
    pickup(combined, personal, ingredient, emptyMatrix);
    RemoteItemStack cursor = ingredient, one = item(ingredient.legacyId(), 1, ingredient.damage());
    for (int index = 0; index < cells.length; index++) {
      cursor = dec(cursor);
      RemoteInventoryView after = replace(window(), cells[index], one);
      if (mid[index] != null)
        after = replace(after, 0, mid[index]);
      step(cells[index], 1, null, after, personal(), personal(), cursor);
    }
  }

  private void finish(int personal, RemoteItemStack result) throws IOException {
    RemoteInventoryView taken = clearOwned(window());
    takeResult(result, taken, 16842752 + result.legacyId());
    int combined = personal + 1;
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

  private void pickup(int combined, int personal, RemoteItemStack ingredient, boolean emptyMatrix)
      throws IOException {
    RemoteInventoryView before = window();
    if ((emptyMatrix && !emptyOwned(before)) || before.slot(combined).empty()
        || !before.slot(combined).item().equals(ingredient)
        || !personal().slot(personal).item().equals(ingredient) || inbound.cursor() != null)
      throw new IllegalStateException("utility-block ingredient preflight failed");
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
