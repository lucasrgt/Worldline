package worldline.b173server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import worldline.api.RemoteContainerWindow;
import worldline.api.RemoteInventorySlot;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteItemStack;
import worldline.api.RemoteWindowKind;

/** Workbench crafts stew 282, bread 297, cookies 357x8, and cake 354. */
public final class B173FoodWorkbenchClick {
  public static final RemoteItemStack STEW = new RemoteItemStack(282, 1, 0);
  public static final RemoteItemStack BREAD = new RemoteItemStack(297, 1, 0);
  public static final RemoteItemStack COOKIE = new RemoteItemStack(357, 8, 0);
  public static final RemoteItemStack CAKE = new RemoteItemStack(354, 1, 0);
  public static final RemoteItemStack BUCKET = new RemoteItemStack(325, 1, 0);
  private static final int STAT = 16842752;
  private final B173PlayChannel channel;
  private final B173PlayInbound inbound;
  private int windowId, action;
  private long epoch = -1L;

  private B173FoodWorkbenchClick(B173WireClient actor) {
    channel = actor.channel();
    inbound = channel.inbound();
  }

  public static void apply(B173WireClient actor) {
    try {
      new B173FoodWorkbenchClick(actor).run();
    } catch (IOException error) {
      throw new IllegalStateException("food workbench crafts failed", error);
    }
  }

  private void run() throws IOException {
    require(STEW.legacyId() == 282 && BREAD.legacyId() == 297 && COOKIE.legacyId() == 357
            && CAKE.legacyId() == 354 && BUCKET.legacyId() == 325 && COOKIE.count() == 8,
        "food workbench identities drifted");
    RemoteContainerWindow active = inbound.activeWindow();
    if (active.descriptor().kind() != RemoteWindowKind.WORKBENCH || active.inventory().size() != 46
        || !inbound.cursorObserved() || inbound.cursor() != null)
      throw new IllegalStateException("invalid food workbench boundary");
    shaped(42, new int[] {2}, null, null, null, null, -1);
    shaped(43, new int[] {5}, null, null, null, null, -1);
    shaped(44, new int[] {8}, STEW, new int[] {2, 5, 8}, new int[0], null, 42);
    shaped(38, new int[] {1, 2, 3}, BREAD, new int[] {1, 2, 3}, new int[0], null, 43);
    shaped(38, new int[] {1}, null, null, null, null, -1);
    shaped(39, new int[] {2}, null, null, null, null, -1);
    shaped(38, new int[] {3}, COOKIE, new int[] {1, 2, 3}, new int[0], null, 44);
    shaped(9, new int[] {1}, null, null, null, null, -1);
    shaped(10, new int[] {2}, null, null, null, null, -1);
    shaped(11, new int[] {3}, null, null, null, null, -1);
    shaped(41, new int[] {5}, null, null, null, null, -1);
    shaped(12, new int[] {4}, null, null, null, null, -1);
    shaped(13, new int[] {6}, null, null, null, null, -1);
    shaped(38, new int[] {7, 8, 9}, CAKE, new int[] {4, 5, 6, 7, 8, 9}, new int[] {1, 2, 3}, BUCKET,
        40);
    leftover(1, 9);
    leftover(2, 10);
    leftover(3, 11);
    if (!same(personal(), 42, STEW) || !same(personal(), 43, BREAD) || !same(personal(), 44, COOKIE)
        || !same(personal(), 40, CAKE) || !same(personal(), 9, BUCKET) || inbound.cursor() != null
        || !emptyOwned(window()))
      throw new IllegalStateException("food workbench results drifted");
  }

  private void shaped(int personal, int[] cells, RemoteItemStack result, int[] consumed, int[] keep,
      RemoteItemStack leftover, int dest) throws IOException {
    int combined = personal + 1;
    RemoteItemStack ingredient = window().slot(combined).item();
    pickup(combined, personal, ingredient);
    RemoteItemStack cursor = ingredient, one = item(ingredient.legacyId(), 1, ingredient.damage());
    for (int index = 0; index < cells.length; index++) {
      cursor = dec(cursor);
      RemoteInventoryView after = replace(window(), cells[index], one);
      if (index == cells.length - 1 && result != null)
        after = replace(after, 0, result);
      step(cells[index], 1, null, after, personal(), personal(), cursor);
    }
    if (cursor != null)
      step(combined, 0, null, replace(window(), combined, cursor), personal(),
          replace(personal(), personal, cursor), null);
    if (result == null)
      return;
    if (!same(window(), 0, result))
      throw new IllegalStateException("food result " + result.legacyId() + " absent");
    RemoteInventoryView taken = replace(window(), 0, null);
    for (int slot : consumed)
      taken = replace(taken, slot, null);
    for (int slot : keep)
      taken = replace(taken, slot, leftover);
    takeResult(result, taken);
    int stored = dest + 1;
    step(stored, 0, null, replace(window(), stored, result), personal(),
        replace(personal(), dest, result), null);
  }

  private void leftover(int matrix, int personal) throws IOException {
    if (!same(window(), matrix, BUCKET) || inbound.cursor() != null)
      throw new IllegalStateException("cake leftover bucket absent");
    step(matrix, 0, BUCKET, replace(window(), matrix, null), personal(), personal(), BUCKET);
    int combined = personal + 1;
    step(combined, 0, null, replace(window(), combined, BUCKET), personal(),
        replace(personal(), personal, BUCKET), null);
  }

  private void takeResult(RemoteItemStack result, RemoteInventoryView taken) throws IOException {
    send(new B173ContainerStep(windowId(), action + 1, 0, result, window(), taken, personal(),
        personal(), inbound.cursor(), result, STAT + result.legacyId(), result.count()));
    action++;
    inbound.awaitContainerTransaction();
  }

  private void pickup(int combined, int personal, RemoteItemStack ingredient) throws IOException {
    if (ingredient == null || inbound.cursor() != null || window().slot(combined).empty()
        || !window().slot(combined).item().equals(ingredient)
        || !personal().slot(personal).item().equals(ingredient))
      throw new IllegalStateException("food ingredient preflight failed");
    step(combined, 0, ingredient, replace(window(), combined, null), personal(),
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

  private static boolean same(RemoteInventoryView view, int slot, RemoteItemStack item) {
    return !view.slot(slot).empty() && view.slot(slot).item().equals(item);
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
