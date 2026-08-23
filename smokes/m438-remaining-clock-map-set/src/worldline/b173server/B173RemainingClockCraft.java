package worldline.b173server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import worldline.api.RemoteContainerWindow;
import worldline.api.RemoteInventorySlot;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteItemStack;
import worldline.api.RemoteWindowKind;

/** Workbench crafts clock 347 from gold 266 x4 plus redstone 331. */
public final class B173RemainingClockCraft {
  public static final RemoteItemStack CLOCK = new RemoteItemStack(347, 1, 0);
  private static final int[] PLUS = {2, 4, 6, 8};
  private final B173PlayChannel channel;
  private final B173PlayInbound inbound;
  private int windowId, action;
  private long epoch = -1L;

  private B173RemainingClockCraft(B173WireClient actor) {
    channel = actor.channel();
    inbound = channel.inbound();
  }

  public static void apply(B173WireClient actor) {
    try {
      new B173RemainingClockCraft(actor).run();
    } catch (IOException error) {
      throw new IllegalStateException("remaining-clock craft failed", error);
    }
  }

  private void run() throws IOException {
    require(CLOCK.legacyId() == 347, "remaining-clock result identity drifted");
    RemoteContainerWindow active = inbound.activeWindow();
    if (active.descriptor().kind() != RemoteWindowKind.WORKBENCH || active.inventory().size() != 46
        || !inbound.cursorObserved() || inbound.cursor() != null)
      throw new IllegalStateException("invalid remaining-clock workbench boundary");
    pickup(39, 331, 1);
    placeOne(5, null);
    storeLeftover(39);
    pickup(38, 266, 4);
    for (int index = 0; index < PLUS.length; index++)
      placeOne(PLUS[index], index == PLUS.length - 1 ? CLOCK : null);
    storeLeftover(38);
    takeResult(CLOCK, clearOwned(window()), 16842752 + CLOCK.legacyId());
    int combined = 38;
    step(combined, 0, null, replace(window(), combined, CLOCK), personal(),
        replace(personal(), 37, CLOCK), null);
    require(!personal().slot(37).empty() && personal().slot(37).item().equals(CLOCK)
            && inbound.cursor() == null,
        "remaining-clock crafted inventory drifted");
  }

  private void pickup(int personal, int expectedId, int needed) throws IOException {
    int combined = personal + 1;
    RemoteInventoryView before = window();
    if (before.slot(combined).empty() || inbound.cursor() != null)
      throw new IllegalStateException("remaining-clock ingredient preflight failed");
    RemoteItemStack stack = before.slot(combined).item();
    if (stack.legacyId() != expectedId || stack.count() < needed || stack.damage() != 0
        || !personal().slot(personal).item().equals(stack))
      throw new IllegalStateException("remaining-clock ingredient seed drifted");
    step(combined, 0, stack, replace(before, combined, null), personal(),
        replace(personal(), personal, null), stack);
  }

  private void placeOne(int cell, RemoteItemStack result) throws IOException {
    RemoteItemStack cursor = inbound.cursor();
    if (cursor == null || !window().slot(cell).empty())
      throw new IllegalStateException("remaining-clock matrix place drifted");
    RemoteItemStack one = item(cursor.legacyId(), 1, cursor.damage());
    RemoteInventoryView after = replace(window(), cell, one);
    if (result != null)
      after = replace(after, 0, result);
    step(cell, 1, null, after, personal(), personal(), dec(cursor));
  }

  private void storeLeftover(int personal) throws IOException {
    RemoteItemStack cursor = inbound.cursor();
    if (cursor == null)
      return;
    int combined = personal + 1;
    if (!window().slot(combined).empty() || !personal().slot(personal).empty())
      throw new IllegalStateException("remaining-clock leftover store drifted");
    step(combined, 0, null, replace(window(), combined, cursor), personal(),
        replace(personal(), personal, cursor), null);
  }

  private void takeResult(RemoteItemStack result, RemoteInventoryView taken, int statisticId)
      throws IOException {
    if (window().slot(0).empty() || !window().slot(0).item().equals(result)
        || inbound.cursor() != null)
      throw new IllegalStateException("remaining-clock result absent");
    send(new B173ContainerStep(windowId(), action + 1, 0, result, window(), taken, personal(),
        personal(), inbound.cursor(), result, statisticId, result.count()));
    action++;
    inbound.awaitContainerTransaction();
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
