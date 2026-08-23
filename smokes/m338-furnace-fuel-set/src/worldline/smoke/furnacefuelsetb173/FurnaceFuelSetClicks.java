package worldline.smoke.furnacefuelsetb173;

import worldline.api.BlockFace;
import worldline.api.BlockPosition;
import worldline.api.RemoteContainerWindow;
import worldline.api.RemoteFurnaceLoad;
import worldline.api.RemoteFurnaceSmelt;
import worldline.api.RemoteItemStack;
import worldline.api.RemoteWindowClosure;
import worldline.api.RemoteWindowKind;
import worldline.b173server.B173WireClient;

/** One official furnace open, four-action load, Packet103/105 cobble smelt, and close. */
final class FurnaceFuelSetClicks {
    private static final RemoteItemStack COBBLE = new RemoteItemStack(4, 1, 0);
    private static final RemoteItemStack STONE = new RemoteItemStack(1, 1, 0);
    private static final RemoteItemStack IRON = new RemoteItemStack(265, 1, 0);

    private FurnaceFuelSetClicks() {}

    static RemoteFurnaceSmelt smelt(B173WireClient actor, BlockPosition furnace, int inputSlot,
            int fuelSlot, RemoteItemStack fuel, int burn) throws Exception {
        RemoteContainerWindow opened = actor.openFurnace(furnace, BlockFace.UP);
        int inputCombined = inputSlot - 6, fuelCombined = fuelSlot - 6;
        FurnaceFuelSetSupport.require(opened.descriptor().kind() == RemoteWindowKind.FURNACE
                && opened.descriptor().containerSlots() == 3
                && "Furnace".equals(opened.descriptor().title())
                && opened.inventory().size() == 39
                && opened.inventory().slot(inputCombined).item().equals(COBBLE)
                && opened.inventory().slot(fuelCombined).item().equals(fuel)
                && opened.inventory().slot(0).empty() && opened.inventory().slot(1).empty()
                && opened.inventory().slot(2).empty(), "furnace open mapping drifted");
        RemoteFurnaceLoad load = actor.loadFurnace(inputSlot, fuelSlot);
        FurnaceFuelSetSupport.require(load.inputTakeAction() == 1 && load.inputStoreAction() == 2
                && load.fuelTakeAction() == 3 && load.fuelStoreAction() == 4
                && load.input().equals(COBBLE) && load.fuel().equals(fuel)
                && actor.inventory().slot(inputSlot).empty()
                && actor.inventory().slot(fuelSlot).empty(), "accepted furnace load drifted");
        worldline.test.WorldlineSmokeAwait.observe(actor,5);
        RemoteFurnaceSmelt smelt = actor.awaitFurnaceSmelt();
        FurnaceFuelSetSupport.require(smelt.output().equals(STONE) && !smelt.output().equals(IRON)
                && smelt.maximumCook() == 199 && smelt.maximumBurn() == burn
                && smelt.totalBurn() == burn && smelt.completionBurn() == burn - 199
                && smelt.window().inventory().slot(2).item().equals(STONE)
                && smelt.window().inventory().slot(0).empty()
                && smelt.window().inventory().slot(1).empty(), "completed furnace fuel smelt drifted");
        RemoteWindowClosure closure = actor.closeWindow();
        FurnaceFuelSetSupport.require(closure.closedWindow().equals(smelt.window())
                && closure.proofAction() >= 1, "furnace close proof drifted");
        return smelt;
    }
}
