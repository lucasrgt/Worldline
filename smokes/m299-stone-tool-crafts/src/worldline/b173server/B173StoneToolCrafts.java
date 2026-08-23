package worldline.b173server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import worldline.api.RemoteInventorySlot;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteItemStack;
import worldline.api.RemoteWindowKind;

/** ACK-correlated workbench crafts for the official stone-tool family. */
public final class B173StoneToolCrafts {
  public static final int COBBLE = 4, STICK = 280, SWORD = 272, SHOVEL = 273, PICK = 274, AXE = 275,
                          HOE = 291;
  static final int STAT = 16842752;
  static final int[] RESULTS = {SWORD, SHOVEL, PICK, AXE, HOE};
  static final int[][] RECIPES = {{0, 1, 0, 0, 1, 0, 0, 2, 0}, {0, 1, 0, 0, 2, 0, 0, 2, 0},
      {1, 1, 1, 0, 2, 0, 0, 2, 0}, {1, 1, 0, 1, 2, 0, 0, 2, 0}, {1, 1, 0, 0, 2, 0, 0, 2, 0}};
  private final B173PlayChannel channel;
  private final B173PlayInbound inbound;
  private final int windowId;
  private int action;

  private B173StoneToolCrafts(B173PlayChannel channel) {
    this.channel = channel;
    this.inbound = channel.inbound();
    if (inbound.activeWindow().descriptor().kind() != RemoteWindowKind.WORKBENCH
        || inbound.activeWindow().inventory().size() != 46 || inbound.cursor() != null)
      throw new IllegalStateException("stone-tool workbench preflight failed");
    this.windowId = inbound.activeWindow().descriptor().windowId();
    for (int slot = 0; slot < 10; slot++)
      if (!inbound.activeWindow().inventory().slot(slot).empty())
        throw new IllegalStateException("stone-tool workbench matrix was not empty");
  }

  public static int[] apply(B173WireClient actor) {
    try {
      B173StoneToolCrafts crafts = new B173StoneToolCrafts(actor.channel());
      int cobble = crafts.find(COBBLE), stick = crafts.find(STICK);
      for (int index = 0; index < RESULTS.length; index++)
        crafts.recipe(RECIPES[index], cobble, stick, RESULTS[index], 9 + index);
      return RESULTS.clone();
    } catch (IOException error) {
      throw new IllegalStateException("stone-tool workbench craft failed", error);
    }
  }

  private void recipe(int[] grid, int cobble, int stick, int result, int store) throws IOException {
    int lastStick = -1;
    for (int cell = 0; cell < 9; cell++)
      if (grid[cell] == 2)
        lastStick = cell + 1;
    pick(cobble);
    for (int cell = 0; cell < 9; cell++)
      if (grid[cell] == 1)
        place(cell + 1, null);
    put(cobble);
    pick(stick);
    for (int cell = 0; cell < 9; cell++)
      if (grid[cell] == 2)
        place(cell + 1, cell + 1 == lastStick ? item(result, 1) : null);
    put(stick);
    take(result);
    store(store);
  }

  private void pick(int combined) throws IOException {
    RemoteInventoryView before = window(), personal = inbound.inventory();
    if (inbound.cursor() != null || before.slot(combined).empty())
      throw new IllegalStateException("stone-tool pick requires occupied source");
    RemoteItemStack stack = before.slot(combined).item();
    step(combined, 0, stack, replace(before, combined, null), replace(personal, combined - 1, null),
        stack, -1, 0);
  }

  private void put(int combined) throws IOException {
    RemoteItemStack cursor = inbound.cursor();
    if (cursor == null)
      return;
    RemoteInventoryView before = window(), personal = inbound.inventory();
    if (!before.slot(combined).empty())
      throw new IllegalStateException("stone-tool put target occupied");
    step(combined, 0, null, replace(before, combined, cursor),
        replace(personal, combined - 1, cursor), null, -1, 0);
  }

  private void place(int grid, RemoteItemStack result) throws IOException {
    RemoteItemStack cursor = inbound.cursor();
    if (cursor == null || !window().slot(grid).empty())
      throw new IllegalStateException("stone-tool place requires cursor and empty grid");
    RemoteInventoryView after = replace(window(), grid, item(cursor.legacyId(), 1));
    if (result != null)
      after = replace(after, 0, result);
    step(grid, 1, null, after, inbound.inventory(), dec(cursor), -1, 0);
  }

  private void take(int result) throws IOException {
    RemoteInventoryView before = window();
    if (before.slot(0).empty() || before.slot(0).item().legacyId() != result
        || before.slot(0).item().count() != 1 || inbound.cursor() != null)
      throw new IllegalStateException("stone-tool result " + result + " absent");
    RemoteItemStack tool = before.slot(0).item();
    RemoteInventoryView after = before;
    for (int slot = 0; slot < 10; slot++)
      after = replace(after, slot, null);
    step(0, 0, tool, after, inbound.inventory(), tool, STAT + result, 1);
  }

  private void store(int personal) throws IOException {
    RemoteItemStack tool = inbound.cursor();
    int combined = personal + 1;
    RemoteInventoryView before = window(), owned = inbound.inventory();
    if (tool == null || !before.slot(combined).empty() || !owned.slot(personal).empty())
      throw new IllegalStateException("stone-tool store target occupied");
    step(combined, 0, null, replace(before, combined, tool), replace(owned, personal, tool), null,
        -1, 0);
  }

  private void step(int slot, int button, RemoteItemStack predicted, RemoteInventoryView after,
      RemoteInventoryView personalAfter, RemoteItemStack cursorAfter, int stat, int increment)
      throws IOException {
    RemoteInventoryView before = window(), personalBefore = inbound.inventory();
    RemoteItemStack cursorBefore = inbound.cursor();
    int next = action + 1;
    B173ContainerStep value = stat >= 0
        ? new B173ContainerStep(windowId, next, slot, predicted, before, after, personalBefore,
              personalAfter, cursorBefore, cursorAfter, stat, increment)
        : new B173ContainerStep(windowId, next, slot, button, predicted, before, after,
              personalBefore, personalAfter, cursorBefore, cursorAfter);
    inbound.beginContainerTransaction(value);
    B173ContainerPacket.write(channel.output, windowId, slot, button, next, predicted);
    channel.output.flush();
    action = next;
    inbound.awaitContainerTransaction();
  }

  private int find(int id) {
    RemoteInventoryView view = window();
    for (int slot = 10; slot < 46; slot++)
      if (!view.slot(slot).empty() && view.slot(slot).item().legacyId() == id)
        return slot;
    throw new IllegalStateException("stone-tool ingredient " + id + " absent");
  }

  private RemoteInventoryView window() {
    return inbound.activeWindow().inventory();
  }
  private static RemoteItemStack item(int id, int count) {
    return new RemoteItemStack(id, count, 0);
  }
  private static RemoteItemStack dec(RemoteItemStack stack) {
    return stack.count() == 1
        ? null
        : new RemoteItemStack(stack.legacyId(), stack.count() - 1, stack.damage());
  }
  private static RemoteInventoryView replace(
      RemoteInventoryView source, int slot, RemoteItemStack item) {
    List<RemoteInventorySlot> slots = new ArrayList<RemoteInventorySlot>(source.slots());
    slots.set(slot, new RemoteInventorySlot(slot, item));
    return new RemoteInventoryView(source.windowId(), slots);
  }
}
