package worldline.smoke.furnacerestsmeltsb173;

import worldline.api.BlockFace;
import worldline.api.BlockPosition;
import worldline.api.RemoteContainerWindow;
import worldline.api.RemoteFurnaceLoad;
import worldline.api.RemoteFurnaceSmelt;
import worldline.api.RemoteItemStack;
import worldline.api.RemoteWindowClosure;
import worldline.api.RemoteWindowKind;
import worldline.b173server.B173WireClient;

/** One official furnace open, four-action load, Packet103/105 rest smelt, and close. */
final class FurnaceRestSmeltsClicks {
    private static final RemoteItemStack COAL = new RemoteItemStack(263, 1, 0);
    private static final RemoteItemStack IRON = new RemoteItemStack(15, 1, 0);
    private static final RemoteItemStack INGOT = new RemoteItemStack(265, 1, 0);

    private FurnaceRestSmeltsClicks() {}

    static RemoteFurnaceSmelt smelt(B173WireClient actor, BlockPosition furnace, int inputSlot,
            int fuelSlot, RemoteItemStack input, RemoteItemStack output) throws Exception {
        RemoteContainerWindow opened = actor.openFurnace(furnace, BlockFace.UP);
        int inputCombined = inputSlot - 6, fuelCombined = fuelSlot - 6;
        FurnaceRestSmeltsSupport.require(opened.descriptor().kind() == RemoteWindowKind.FURNACE
                && opened.descriptor().containerSlots() == 3
                && "Furnace".equals(opened.descriptor().title())
                && opened.inventory().size() == 39
                && opened.inventory().slot(inputCombined).item().equals(input)
                && opened.inventory().slot(fuelCombined).item().equals(COAL)
                && opened.inventory().slot(0).empty() && opened.inventory().slot(1).empty()
                && opened.inventory().slot(2).empty(), "furnace open mapping drifted");
        RemoteFurnaceLoad load = actor.loadFurnace(inputSlot, fuelSlot);
        FurnaceRestSmeltsSupport.require(load.inputTakeAction() == 1 && load.inputStoreAction() == 2
                && load.fuelTakeAction() == 3 && load.fuelStoreAction() == 4
                && load.input().equals(input) && load.fuel().equals(COAL)
                && !load.input().equals(IRON)
                && actor.inventory().slot(inputSlot).empty()
                && actor.inventory().slot(fuelSlot).empty(), "accepted furnace load drifted");
        worldline.test.WorldlineSmokeAwait.observe(actor,5);
        RemoteFurnaceSmelt smelt = actor.awaitFurnaceSmelt();
        FurnaceRestSmeltsSupport.require(smelt.output().equals(output) && !smelt.output().equals(INGOT)
                && smelt.maximumCook() == 199 && smelt.maximumBurn() == 1600
                && smelt.totalBurn() == 1600 && smelt.completionBurn() == 1401
                && smelt.window().inventory().slot(2).item().equals(output)
                && smelt.window().inventory().slot(0).empty()
                && smelt.window().inventory().slot(1).empty(), "completed rest furnace smelt drifted");
        RemoteWindowClosure closure = actor.closeWindow();
        FurnaceRestSmeltsSupport.require(closure.closedWindow().equals(smelt.window())
                && closure.proofAction() >= 1, "furnace close proof drifted");
        return smelt;
    }
}
