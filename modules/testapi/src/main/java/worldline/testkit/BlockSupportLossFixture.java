package worldline.testkit;

import worldline.api.BlockFace;
import worldline.api.BlockLifecycleDriver;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.RemoteInventorySlot;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteItemStack;
import worldline.api.RemoteWorldView;

/** Executes placement, a bounded stable window, causal support loss, and reload. */
public final class BlockSupportLossFixture {
    private static final BlockState AIR = new BlockState(0, 0);
    private BlockSupportLossFixture() {
    }

    public static BlockSupportLossEvidence execute(BlockSupportLossScenario scenario,
            BlockLifecycleDriver driver) {
        if (scenario == null || driver == null) throw new NullPointerException("block support loss");
        verify(driver, scenario.support(), scenario.supportState(), "support baseline");
        verify(driver, scenario.target(), AIR, "target baseline");
        verifySlot(driver.inventory(), scenario.placementSlot(), false);
        driver.selectHeldSlot(scenario.placementSlot().hotbarSlot());
        driver.placeHeldBlock(scenario.support(), BlockFace.UP);
        verify(driver, scenario.target(), scenario.targetState(), "placement");
        awaitSlot(driver, scenario.placementSlot(), true, scenario.observationTicks());

        driver.sustainTicks(scenario.tickWindow());
        verify(driver, scenario.support(), scenario.supportState(), "stable support");
        verify(driver, scenario.target(), scenario.targetState(), "tick window");

        verifySlot(driver.inventory(), scenario.breakSlot(), false);
        driver.selectHeldSlot(scenario.breakSlot().hotbarSlot());
        driver.beginBreak(scenario.support());
        if (scenario.breakTicks() > 0) driver.sustainTicks(scenario.breakTicks());
        driver.finishBreak(scenario.support());
        verify(driver, scenario.support(), AIR, "support removal");
        driver.sustainTicks(scenario.observationTicks());
        verify(driver, scenario.target(), AIR, "support-loss response");
        awaitSlot(driver, scenario.breakSlot(), true, scenario.observationTicks());

        driver.saveAndReload();
        BlockLifecycleDriver.ReloadBoundary boundary = driver.reloadBoundary();
        verify(driver, scenario.support(), AIR, "reload support");
        verify(driver, scenario.target(), AIR, "reload target");
        return new BlockSupportLossEvidence(scenario, boundary);
    }

    private static void verify(BlockLifecycleDriver driver, BlockPosition position,
            BlockState expected, String phase) {
        RemoteWorldView world = driver.awaitBlock(position, expected);
        require(world != null && world.blockAt(position.x(), position.y(), position.z())
                .equals(expected), phase + " state drifted");
    }

    private static void verifySlot(RemoteInventoryView inventory,
            BlockLifecycleSlot expected, boolean after) {
        require(inventory != null && expected.inventorySlot() < inventory.size()
                && matches(inventory.slot(expected.inventorySlot()), expected, after),
                "support-loss inventory effect drifted at slot " + expected.inventorySlot());
    }

    private static void awaitSlot(BlockLifecycleDriver driver, BlockLifecycleSlot expected,
            boolean after, int ticks) {
        for (int elapsed = 0; elapsed <= ticks; elapsed++) {
            RemoteInventoryView inventory = driver.inventory();
            if (inventory != null && expected.inventorySlot() < inventory.size()
                    && matches(inventory.slot(expected.inventorySlot()), expected, after)) return;
            if (elapsed < ticks) driver.sustainTicks(1);
        }
        throw new IllegalStateException("support-loss inventory effect did not settle at slot "
                + expected.inventorySlot());
    }

    private static boolean matches(RemoteInventorySlot observed,
            BlockLifecycleSlot expected, boolean after) {
        RemoteItemStack stack = after ? expected.after() : expected.before();
        return stack == null ? observed.empty() : !observed.empty() && observed.item().equals(stack);
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
