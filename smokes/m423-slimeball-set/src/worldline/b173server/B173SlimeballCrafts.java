package worldline.b173server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import worldline.api.RemoteContainerWindow;
import worldline.api.RemoteInventorySlot;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteItemStack;
import worldline.api.RemoteWindowKind;

/** Workbench crafts sticky piston 29 from slimeball 341 above piston 33. */
public final class B173SlimeballCrafts {
  public static final int STICKY = 29, PISTON = 33, SLIME = 341;
  private static final int STAT = 16842752;
  private final B173PlayChannel channel;
  private final B173PlayInbound inbound;
  private int windowId, action;

  private B173SlimeballCrafts(B173WireClient actor) {
    channel = actor.channel();
    inbound = channel.inbound();
  }

  public static int apply(B173WireClient actor) {
    try {
      return new B173SlimeballCrafts(actor).run();
    } catch (IOException error) {
      throw new IllegalStateException("slimeball sticky craft failed", error);
    }
  }

  private int run() throws IOException {
    RemoteContainerWindow active = inbound.activeWindow();
    if (active.descriptor().kind() != RemoteWindowKind.WORKBENCH || active.inventory().size() != 46
        || !inbound.cursorObserved() || inbound.cursor() != null)
      throw new IllegalStateException("invalid slimeball workbench boundary");
    for (int slot = 0; slot < 10; slot++)
      if (!active.inventory().slot(slot).empty())
        throw new IllegalStateException("slimeball matrix was not empty");
    windowId = active.inventory().windowId();
    pickup(find(SLIME));
    placeAll(2, null);
    pickup(find(PISTON));
    placeAll(5, item(STICKY, 1));
    finish(item(STICKY, 1));
    if (!has(STICKY))
      throw new IllegalStateException("sticky piston 29 craft result drifted");
    return STICKY;
  }

  private void pickup(int combined) throws IOException {
    RemoteItemStack stack = window().slot(combined).item();
    step(combined, 0, stack, replace(window(), combined, null),
        replace(personal(), combined - 1, null), stack);
  }

  private void placeAll(int slot, RemoteItemStack result) throws IOException {
    RemoteItemStack cursor = inbound.cursor();
    step(slot, 0, null, replace(replace(window(), slot, cursor), 0, result), personal(), null);
  }

  private void finish(RemoteItemStack result) throws IOException {
    if (window().slot(0).empty() || !window().slot(0).item().equals(result)
        || inbound.cursor() != null)
      throw new IllegalStateException("modeled sticky 29 result absent");
    RemoteInventoryView taken = window();
    for (int slot = 0; slot < 10; slot++)
      taken = replace(taken, slot, null);
    send(new B173ContainerStep(windowId, action + 1, 0, result, window(), taken, personal(),
        personal(), inbound.cursor(), result, STAT + result.legacyId(), 1));
    action++;
    inbound.awaitContainerTransaction();
    int dest = empty();
    step(dest, 0, null, replace(window(), dest, result), replace(personal(), dest - 1, result),
        null);
  }

  private void step(int slot, int button, RemoteItemStack predicted, RemoteInventoryView after,
      RemoteInventoryView personalAfter, RemoteItemStack cursorAfter) throws IOException {
    send(new B173ContainerStep(windowId, action + 1, slot, button, predicted, window(), after,
        personal(), personalAfter, inbound.cursor(), cursorAfter));
    action++;
    inbound.awaitContainerTransaction();
  }

  private void send(B173ContainerStep value) throws IOException {
    inbound.beginContainerTransaction(value);
    B173ContainerPacket.write(
        channel.output, windowId, value.slot, value.button, value.action, value.predicted);
    channel.output.flush();
  }

  private RemoteInventoryView window() {
    return inbound.activeWindow().inventory();
  }
  private RemoteInventoryView personal() {
    return inbound.inventory();
  }

  private int find(int id) {
    RemoteInventoryView view = window();
    for (int slot = 10; slot < view.size(); slot++)
      if (!view.slot(slot).empty() && view.slot(slot).item().legacyId() == id)
        return slot;
    throw new IllegalStateException("slimeball ingredient " + id + " absent");
  }

  private int empty() {
    RemoteInventoryView view = window();
    for (int slot = 10; slot < view.size(); slot++)
      if (view.slot(slot).empty())
        return slot;
    throw new IllegalStateException("slimeball destination absent");
  }

  private boolean has(int id) {
    RemoteInventoryView view = personal();
    for (int slot = 9; slot <= 44; slot++)
      if (!view.slot(slot).empty() && view.slot(slot).item().legacyId() == id)
        return true;
    return false;
  }

  private static RemoteInventoryView replace(
      RemoteInventoryView source, int slot, RemoteItemStack item) {
    List<RemoteInventorySlot> slots = new ArrayList<RemoteInventorySlot>(source.slots());
    slots.set(slot, new RemoteInventorySlot(slot, item));
    return new RemoteInventoryView(source.windowId(), slots);
  }

  private static RemoteItemStack item(int id, int count) {
    return new RemoteItemStack(id, count, 0);
  }
}
