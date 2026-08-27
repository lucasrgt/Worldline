package worldline.testkit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import worldline.api.BlockLifecycleDriver;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.RemoteDroppedItem;
import worldline.api.RemoteInventorySlot;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteItemStack;
import worldline.api.RemoteWorldView;

/** Public execution of a complete server-authoritative block lifecycle. */
public final class BlockLifecycleFixture {
    private static final BlockState AIR = new BlockState(0, 0);
    private static final Comparator<RemoteItemStack> STACK_ORDER = Comparator
            .comparingInt(RemoteItemStack::legacyId)
            .thenComparingInt(RemoteItemStack::damage)
            .thenComparingInt(RemoteItemStack::count);

    private BlockLifecycleFixture() {
    }

    public static BlockLifecycleEvidence execute(BlockLifecycleScenario scenario,
            BlockLifecycleDriver driver) {
        if (scenario == null || driver == null) throw new NullPointerException("block lifecycle");
        BlockPosition target = scenario.target();
        if (scenario.supportState() != null) {
            verifyBlock(driver.awaitBlock(scenario.support(), scenario.supportState()),
                    scenario.support(), scenario.supportState(), "placement support");
        }
        verifyBlock(driver.awaitBlock(target, AIR), target, AIR, "placement baseline");
        verifySlot(driver.inventory(), scenario.placementSlot(), false);
        driver.selectHeldSlot(scenario.placementSlot().hotbarSlot());
        driver.placeHeldBlock(scenario.support(), scenario.face());
        verifyBlock(driver.awaitBlock(target, scenario.placedState()),
                target, scenario.placedState(), "placement");
        verifySlot(driver.inventory(), scenario.placementSlot(), true);

        driver.saveAndReload();
        BlockLifecycleDriver.ReloadBoundary boundary = driver.reloadBoundary();
        verifyBlock(driver.awaitBlock(target, scenario.placedState()),
                target, scenario.placedState(), "placed-state reload");

        verifySlot(driver.inventory(), scenario.breakSlot(), false);
        driver.selectHeldSlot(scenario.breakSlot().hotbarSlot());
        Set<Integer> priorDrops = ids(driver.droppedItems());
        driver.beginBreak(target);
        if (scenario.breakTicks() > 0) driver.sustainTicks(scenario.breakTicks());
        driver.finishBreak(target);
        verifyBlock(driver.awaitBlock(target, AIR), target, AIR, "break transition");
        driver.sustainTicks(scenario.observationTicks());
        List<RemoteItemStack> drops = newDrops(driver.droppedItems(), priorDrops);
        require(drops.equals(sorted(scenario.expectedDrops())),
                "drop matrix drifted: expected=" + scenario.expectedDrops() + ",actual=" + drops);
        verifySlot(driver.inventory(), scenario.breakSlot(), true);

        driver.saveAndReload();
        require(driver.reloadBoundary() == boundary, "reload boundary changed within lifecycle");
        verifyBlock(driver.awaitBlock(target, AIR), target, AIR, "removed-state reload");
        return new BlockLifecycleEvidence(scenario, drops, boundary);
    }

    private static void verifyBlock(RemoteWorldView world, BlockPosition position,
            BlockState expected, String phase) {
        require(world != null && world.blockAt(position.x(), position.y(), position.z()).equals(expected),
                phase + " block state drifted");
    }

    private static void verifySlot(RemoteInventoryView inventory,
            BlockLifecycleSlot expected, boolean after) {
        if (inventory == null || expected.inventorySlot() >= inventory.size()) {
            throw new IllegalStateException("lifecycle inventory slot is absent");
        }
        RemoteInventorySlot observed = inventory.slot(expected.inventorySlot());
        RemoteItemStack stack = after ? expected.after() : expected.before();
        require(stack == null ? observed.empty() : !observed.empty() && observed.item().equals(stack),
                "lifecycle inventory effect drifted at slot " + expected.inventorySlot());
    }

    private static Set<Integer> ids(List<RemoteDroppedItem> drops) {
        if (drops == null) throw new IllegalStateException("null dropped-item snapshot");
        Set<Integer> result = new HashSet<>();
        for (RemoteDroppedItem drop : drops) {
            require(drop != null && result.add(drop.entityId()), "invalid dropped-item snapshot");
        }
        return result;
    }

    private static List<RemoteItemStack> newDrops(List<RemoteDroppedItem> observed,
            Set<Integer> prior) {
        List<RemoteItemStack> result = new ArrayList<>();
        Set<Integer> current = new HashSet<>();
        if (observed == null) throw new IllegalStateException("null dropped-item snapshot");
        for (RemoteDroppedItem drop : observed) {
            require(drop != null && current.add(drop.entityId()), "invalid dropped-item snapshot");
            if (!prior.contains(drop.entityId())) result.add(drop.item());
        }
        result.sort(STACK_ORDER);
        return Collections.unmodifiableList(result);
    }

    private static List<RemoteItemStack> sorted(List<RemoteItemStack> values) {
        List<RemoteItemStack> result = new ArrayList<>(values);
        result.sort(STACK_ORDER);
        return result;
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
