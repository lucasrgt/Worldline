package worldline.testkit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import worldline.api.BlockLifecycleDriver.ReloadBoundary;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.BlockStateDomainDriver;
import worldline.api.RemoteInventorySlot;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteItemStack;
import worldline.api.RemoteWorldView;

/** Public causal executor for a declared reachable metadata domain. */
public final class BlockStateDomainFixture {
    private BlockStateDomainFixture() { }

    public static BlockStateDomainEvidence execute(BlockStateDomainScenario scenario,
            BlockStateDomainDriver driver) {
        if (scenario == null || driver == null) throw new NullPointerException("block state domain");
        verifySlot(driver.inventory(), scenario.placementSlot(), false);
        driver.selectHeldSlot(scenario.placementSlot().hotbarSlot());
        List<String> evidence = new ArrayList<String>();
        for (BlockStateDomainStep step : scenario.steps()) {
            if (step.action() == BlockStateDomainStep.Action.PLACE_HELD) {
                driver.look(step.yaw(), step.pitch());
                driver.sustainTicks(2);
                driver.useHeldItemOnBlock(step.position(), step.face());
            } else {
                verifySlot(driver.inventory(), scenario.placementSlot(), true);
                driver.activateBlock(step.position(), step.face());
            }
            StringBuilder row = new StringBuilder(step.id()).append('|')
                    .append(step.action()).append('|');
            for (int index = 0; index < step.observations().size(); index++) {
                BlockStateObservation observation = step.observations().get(index);
                verifyBlock(driver.awaitBlock(observation.position(), observation.state()),
                        observation.position(), observation.state(), step.id());
                if (index > 0) row.append(',');
                row.append(token(observation.position())).append(':')
                        .append(token(observation.state()));
            }
            evidence.add(row.toString());
        }
        verifySlot(driver.inventory(), scenario.placementSlot(), true);
        driver.saveAndReload();
        ReloadBoundary boundary = driver.reloadBoundary();
        for (Map.Entry<BlockPosition, BlockState> state : scenario.finalStates().entrySet()) {
            verifyBlock(driver.awaitBlock(state.getKey(), state.getValue()),
                    state.getKey(), state.getValue(), "reload");
        }
        return new BlockStateDomainEvidence(scenario, evidence, boundary);
    }

    private static void verifyBlock(RemoteWorldView world, BlockPosition position,
            BlockState expected, String phase) {
        require(world != null && world.blockAt(position.x(), position.y(), position.z())
                .equals(expected), phase + " state-domain block drifted");
    }

    private static void verifySlot(RemoteInventoryView inventory,
            BlockLifecycleSlot expected, boolean after) {
        if (inventory == null || expected.inventorySlot() >= inventory.size()) {
            throw new IllegalStateException("state-domain inventory slot is absent");
        }
        RemoteInventorySlot observed = inventory.slot(expected.inventorySlot());
        RemoteItemStack stack = after ? expected.after() : expected.before();
        require(stack == null ? observed.empty()
                : !observed.empty() && observed.item().equals(stack),
                "state-domain inventory effect drifted at slot " + expected.inventorySlot());
    }

    private static String token(BlockPosition position) {
        return position.x() + ":" + position.y() + ":" + position.z();
    }

    private static String token(BlockState state) {
        return state.legacyId() + ":" + state.metadata();
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
