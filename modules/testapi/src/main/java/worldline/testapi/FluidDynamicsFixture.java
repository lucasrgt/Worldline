package worldline.testapi;

import worldline.api.BlockLifecycleDriver;
import worldline.api.BlockState;
import worldline.api.RemoteInventorySlot;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteItemStack;
import worldline.api.RemoteWorldView;

/** Executes one source-fluid placement and causal horizontal-flow lifecycle. */
public final class FluidDynamicsFixture {
    private static final BlockState AIR = new BlockState(0, 0);

    private FluidDynamicsFixture() { }

    public static FluidDynamicsEvidence execute(FluidDynamicsScenario scenario,
            BlockLifecycleDriver driver) {
        if (scenario == null || driver == null) throw new NullPointerException("fluid dynamics");
        verify(driver.awaitBlock(scenario.support(), scenario.supportState()),
                scenario.supportState(), scenario.support().x(), scenario.support().y(),
                scenario.support().z(), "source support");
        verify(driver.awaitBlock(scenario.flowSupport(), scenario.supportState()),
                scenario.supportState(), scenario.flowSupport().x(), scenario.flowSupport().y(),
                scenario.flowSupport().z(), "flow support");
        verify(driver.awaitBlock(scenario.source(), AIR), AIR, scenario.source().x(),
                scenario.source().y(), scenario.source().z(), "source baseline");
        verify(driver.awaitBlock(scenario.flow(), scenario.gateState()), scenario.gateState(),
                scenario.flow().x(), scenario.flow().y(), scenario.flow().z(), "closed gate");
        verifySlot(driver.inventory(), scenario.sourceSlot(), false);
        driver.selectHeldSlot(scenario.sourceSlot().hotbarSlot());
        driver.useHeldPlacementItem(scenario.support(), worldline.api.BlockFace.UP);
        verify(driver.awaitBlock(scenario.source(), scenario.sourceState()),
                scenario.sourceState(), scenario.source().x(), scenario.source().y(),
                scenario.source().z(), "source placement");
        awaitSlot(driver, scenario.sourceSlot(), true, scenario.settleTicks());
        driver.sustainTicks(scenario.settleTicks());
        verify(driver.awaitBlock(scenario.source(), scenario.sourceState()),
                scenario.sourceState(), scenario.source().x(), scenario.source().y(),
                scenario.source().z(), "settled source");
        verify(driver.awaitBlock(scenario.flow(), scenario.gateState()), scenario.gateState(),
                scenario.flow().x(), scenario.flow().y(), scenario.flow().z(), "settled gate");

        driver.selectHeldSlot(scenario.gateToolSlot().hotbarSlot());
        driver.beginBreak(scenario.flow());
        if (scenario.breakTicks() > 0) driver.sustainTicks(scenario.breakTicks());
        driver.finishBreak(scenario.flow());
        verify(driver.awaitBlock(scenario.flow(), AIR), AIR, scenario.flow().x(),
                scenario.flow().y(), scenario.flow().z(), "gate release");
        driver.sustainTicks(scenario.flowTicks());
        verify(driver.awaitBlock(scenario.flow(), scenario.flowState()), scenario.flowState(),
                scenario.flow().x(), scenario.flow().y(), scenario.flow().z(), "horizontal flow");
        verify(driver.awaitBlock(scenario.source(), scenario.sourceState()),
                scenario.sourceState(), scenario.source().x(), scenario.source().y(),
                scenario.source().z(), "post-flow source");

        driver.saveAndReload();
        BlockLifecycleDriver.ReloadBoundary boundary = driver.reloadBoundary();
        verify(driver.awaitBlock(scenario.source(), scenario.sourceState()),
                scenario.sourceState(), scenario.source().x(), scenario.source().y(),
                scenario.source().z(), "reloaded source");
        verify(driver.awaitBlock(scenario.flow(), scenario.flowState()), scenario.flowState(),
                scenario.flow().x(), scenario.flow().y(), scenario.flow().z(), "reloaded flow");
        return new FluidDynamicsEvidence(scenario, boundary);
    }

    private static void awaitSlot(BlockLifecycleDriver driver, BlockLifecycleSlot expected,
            boolean after, int ticks) {
        for (int elapsed = 0; elapsed <= ticks; elapsed++) {
            RemoteInventoryView inventory = driver.inventory();
            if (inventory != null && expected.inventorySlot() < inventory.size()
                    && matches(inventory.slot(expected.inventorySlot()), expected, after)) return;
            if (elapsed < ticks) driver.sustainTicks(1);
        }
        throw new IllegalStateException("fluid inventory effect did not settle");
    }

    private static void verifySlot(RemoteInventoryView inventory,
            BlockLifecycleSlot expected, boolean after) {
        if (inventory == null || expected.inventorySlot() >= inventory.size()
                || !matches(inventory.slot(expected.inventorySlot()), expected, after)) {
            throw new IllegalStateException("fluid inventory baseline drifted");
        }
    }

    private static boolean matches(RemoteInventorySlot observed,
            BlockLifecycleSlot expected, boolean after) {
        RemoteItemStack stack = after ? expected.after() : expected.before();
        return stack == null ? observed.empty()
                : !observed.empty() && observed.item().equals(stack);
    }

    private static void verify(RemoteWorldView world, BlockState expected,
            int x, int y, int z, String phase) {
        if (world == null || !world.blockAt(x, y, z).equals(expected)) {
            throw new IllegalStateException(phase + " block state drifted");
        }
    }
}
