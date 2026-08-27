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
            BlockStateObservation sentinel = step.observations().get(
                    step.observations().size() - 1);
            RemoteWorldView observed = await(driver, sentinel.position(), sentinel.state(), step.id());
            for (int index = 0; index < step.observations().size(); index++) {
                BlockStateObservation observation = step.observations().get(index);
                if (!matches(observed, observation.position(), observation.state())) {
                    observed = await(driver, observation.position(), observation.state(), step.id());
                }
                verifyBlock(observed, observation.position(), observation.state(), step.id());
                if (index > 0) row.append(',');
                row.append(token(observation.position())).append(':')
                        .append(token(observation.state()));
            }
            driver.sustainTicks(2);
            evidence.add(row.toString());
        }
        verifySlot(driver.inventory(), scenario.placementSlot(), true);
        driver.saveAndReload();
        ReloadBoundary boundary = driver.reloadBoundary();
        Map<BlockPosition, BlockState> finalStates = scenario.finalStates();
        Map.Entry<BlockPosition, BlockState> reloadSentinel = null;
        for (Map.Entry<BlockPosition, BlockState> state : finalStates.entrySet()) {
            reloadSentinel = state;
        }
        RemoteWorldView reloaded = await(driver, reloadSentinel.getKey(),
                reloadSentinel.getValue(), "reload");
        for (Map.Entry<BlockPosition, BlockState> state : finalStates.entrySet()) {
            if (!matches(reloaded, state.getKey(), state.getValue())) {
                reloaded = await(driver, state.getKey(), state.getValue(), "reload");
            }
            verifyBlock(reloaded, state.getKey(), state.getValue(), "reload");
        }
        return new BlockStateDomainEvidence(scenario, evidence, boundary);
    }

    private static RemoteWorldView await(BlockStateDomainDriver driver, BlockPosition position,
            BlockState state, String phase) {
        try { return driver.awaitBlock(position, state); }
        catch (RuntimeException error) {
            throw new IllegalStateException("state-domain " + phase + " awaits "
                    + token(position) + ":" + token(state), error);
        }
    }

    private static void verifyBlock(RemoteWorldView world, BlockPosition position,
            BlockState expected, String phase) {
        require(matches(world, position, expected), phase + " state-domain block drifted");
    }

    private static boolean matches(RemoteWorldView world, BlockPosition position,
            BlockState expected) {
        if (world == null) return false;
        try {
            return world.blockAt(position.x(), position.y(), position.z()).equals(expected);
        } catch (IllegalArgumentException absentChunk) { return false; }
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
