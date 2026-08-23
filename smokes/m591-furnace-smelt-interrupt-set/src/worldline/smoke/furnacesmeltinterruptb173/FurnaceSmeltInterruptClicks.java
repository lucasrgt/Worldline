package worldline.smoke.furnacesmeltinterruptb173;

import worldline.api.BlockFace;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.RemoteContainerWindow;
import worldline.api.RemoteFurnaceLoad;
import worldline.api.RemoteFurnaceSmelt;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteItemStack;
import worldline.api.RemoteWindowClosure;
import worldline.api.RemoteWindowKind;
import worldline.api.RemoteWorldView;
import worldline.b173server.B173FurnaceInterrupt;
import worldline.b173server.B173WireClient;
import worldline.test.WorldlineSmokeAwait;

/** Control cobble smelt plus input-mid-cook and fuel-before-consume interrupts. */
final class FurnaceSmeltInterruptClicks {
  private static final RemoteItemStack COBBLE = new RemoteItemStack(4, 1, 0);
  private static final RemoteItemStack COAL = new RemoteItemStack(263, 1, 0);
  private static final RemoteItemStack STONE = new RemoteItemStack(1, 1, 0);
  private static final RemoteItemStack IRON = new RemoteItemStack(265, 1, 0);
  private static final BlockState IDLE = new BlockState(61, 2);

  private FurnaceSmeltInterruptClicks() {}

  static RemoteFurnaceSmelt control(B173WireClient actor, BlockPosition furnace, int inputSlot, int fuelSlot)
      throws Exception {
    open(actor, furnace, inputSlot, fuelSlot, COBBLE, COAL);
    RemoteFurnaceLoad load = actor.loadFurnace(inputSlot, fuelSlot);
    FurnaceSmeltInterruptSupport.require(load.inputTakeAction() == 1 && load.inputStoreAction() == 2
            && load.fuelTakeAction() == 3 && load.fuelStoreAction() == 4 && load.input().equals(COBBLE)
            && load.fuel().equals(COAL) && actor.inventory().slot(inputSlot).empty()
            && actor.inventory().slot(fuelSlot).empty(),
        "control furnace load drifted");
    WorldlineSmokeAwait.observe(actor, 5);
    RemoteFurnaceSmelt smelt = actor.awaitFurnaceSmelt();
    FurnaceSmeltInterruptSupport.require(smelt.output().equals(STONE) && !smelt.output().equals(IRON)
            && smelt.maximumCook() == 199 && smelt.maximumBurn() == 1600 && smelt.completionBurn() == 1401
            && smelt.window().inventory().slot(2).item().equals(STONE) && smelt.window().inventory().slot(0).empty()
            && smelt.window().inventory().slot(1).empty(),
        "control furnace smelt drifted");
    close(actor, smelt.window());
    return smelt;
  }

  static void input(B173WireClient actor, BlockPosition furnace, int inputSlot, int fuelSlot) throws Exception {
    open(actor, furnace, inputSlot, fuelSlot, COBBLE, COAL);
    RemoteFurnaceLoad load = actor.loadFurnace(inputSlot, fuelSlot);
    FurnaceSmeltInterruptSupport.require(
        load.input().equals(COBBLE) && load.fuel().equals(COAL), "input-interrupt load drifted");
    WorldlineSmokeAwait.awaitBlockMatching(actor, furnace, state -> state.legacyId() == 62, "burning furnace", 80);
    WorldlineSmokeAwait.awaitEntity(actor,
        ()
            -> B173FurnaceInterrupt.window(actor),
        view
        -> view.slot(1).empty() && !view.slot(0).empty() && view.slot(0).item().equals(COBBLE) && view.slot(2).empty(),
        "consumed fuel with remaining input", 40);
    B173FurnaceInterrupt.take(actor, 0, inputSlot, 5);
    WorldlineSmokeAwait.observe(actor, 220);
    RemoteInventoryView after = B173FurnaceInterrupt.window(actor);
    FurnaceSmeltInterruptSupport.require(
        after.slot(2).empty() && actor.inventory().slot(inputSlot).item().equals(COBBLE),
        "input interrupt produced a completed output");
    close(actor, B173FurnaceInterrupt.active(actor));
  }

  static void fuel(B173WireClient actor, BlockPosition furnace, int inputSlot, int fuelSlot) throws Exception {
    open(actor, furnace, inputSlot, fuelSlot, COBBLE, COAL);
    B173FurnaceInterrupt.store(actor, fuelSlot, 1, 1);
    RemoteWorldView primedWorld = WorldlineSmokeAwait.observe(actor, 40);
    RemoteInventoryView primed = B173FurnaceInterrupt.window(actor);
    FurnaceSmeltInterruptSupport.require(primed.slot(0).empty() && primed.slot(2).empty() && !primed.slot(1).empty()
            && primed.slot(1).item().equals(COAL)
            && primedWorld.blockAt(furnace.x(), furnace.y(), furnace.z()).equals(IDLE),
        "fuel-interrupt primed state drifted");
    B173FurnaceInterrupt.take(actor, 1, fuelSlot, 3);
    B173FurnaceInterrupt.store(actor, inputSlot, 0, 5);
    WorldlineSmokeAwait.observe(actor, 220);
    RemoteInventoryView after = B173FurnaceInterrupt.window(actor);
    FurnaceSmeltInterruptSupport.require(after.slot(2).empty() && after.slot(1).empty() && !after.slot(0).empty()
            && after.slot(0).item().equals(COBBLE) && actor.inventory().slot(fuelSlot).item().equals(COAL),
        "fuel interrupt produced a completed output");
    close(actor, B173FurnaceInterrupt.active(actor));
  }

  private static void open(B173WireClient actor, BlockPosition furnace, int inputSlot, int fuelSlot,
      RemoteItemStack input, RemoteItemStack fuel) throws Exception {
    RemoteContainerWindow opened = actor.openFurnace(furnace, BlockFace.UP);
    int inputCombined = inputSlot - 6, fuelCombined = fuelSlot - 6;
    FurnaceSmeltInterruptSupport.require(opened.descriptor().kind() == RemoteWindowKind.FURNACE
            && opened.descriptor().containerSlots() == 3 && "Furnace".equals(opened.descriptor().title())
            && opened.inventory().size() == 39 && opened.inventory().slot(inputCombined).item().equals(input)
            && opened.inventory().slot(fuelCombined).item().equals(fuel) && opened.inventory().slot(0).empty()
            && opened.inventory().slot(1).empty() && opened.inventory().slot(2).empty(),
        "furnace open mapping drifted");
  }

  private static void close(B173WireClient actor, RemoteContainerWindow window) {
    RemoteWindowClosure closure = actor.closeWindow();
    FurnaceSmeltInterruptSupport.require(
        closure.closedWindow().equals(window) && closure.proofAction() >= 1, "furnace close proof drifted");
  }
}
