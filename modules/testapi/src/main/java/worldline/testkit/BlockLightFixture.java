package worldline.testkit;

import java.util.ArrayList;
import java.util.List;
import worldline.api.BlockLightDriver;
import worldline.api.BlockLifecycleDriver.ReloadBoundary;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.RemoteInventorySlot;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteItemStack;
import worldline.api.RemoteWorldView;

/** Public causal executor for air controls versus gameplay-authored light treatments. */
public final class BlockLightFixture {
    private BlockLightFixture() { }

    public static BlockLightEvidence execute(BlockLightScenario scenario, BlockLightDriver driver) {
        if (scenario == null || driver == null) throw new NullPointerException("block light");
        verifySlot(driver.inventory(), scenario.placementSlot(), false);
        List<String> controls = observe(driver.observe(), scenario.probes(), true);
        driver.selectHeldSlot(scenario.placementSlot().hotbarSlot());
        driver.look(scenario.yaw(), scenario.pitch());
        List<String> placements = new ArrayList<String>();
        for (BlockLightPlacement placement : scenario.placements()) {
            driver.useHeldItemOnBlock(placement.support(), placement.face());
            verifyBlock(driver.awaitBlock(placement.position(), placement.expected()), placement);
            placements.add(token(placement.position()) + ':' + token(placement.expected()));
        }
        verifySlot(driver.inventory(), scenario.placementSlot(), true);
        driver.saveAndReload(); ReloadBoundary boundary = driver.reloadBoundary();
        for (BlockLightPlacement placement : scenario.placements()) {
            verifyBlock(driver.awaitBlock(placement.position(), placement.expected()), placement);
        }
        List<String> treatments = observe(driver.observe(), scenario.probes(), false);
        return new BlockLightEvidence(scenario, controls, treatments, placements, boundary);
    }

    private static List<String> observe(RemoteWorldView world, List<BlockLightProbe> probes,
            boolean control) {
        if (world == null) throw new IllegalStateException("light world observation is absent");
        List<String> values = new ArrayList<String>();
        for (BlockLightProbe probe : probes) {
            BlockPosition position = probe.position();
            BlockLightExpectation expected = control ? probe.control() : probe.treatment();
            BlockState state = world.blockAt(position.x(), position.y(), position.z());
            int block = world.blockLightAt(position.x(), position.y(), position.z());
            int sky = world.skyLightAt(position.x(), position.y(), position.z());
            require(state.equals(expected.state()), phase(control) + ' ' + probe.id()
                    + " block state drifted: " + token(state));
            require(expected.blockLight() == BlockLightExpectation.ANY_LIGHT
                    || block == expected.blockLight(), phase(control) + ' ' + probe.id()
                    + " block light drifted: " + block);
            require(expected.skyLight() == BlockLightExpectation.ANY_LIGHT
                    || sky == expected.skyLight(), phase(control) + ' ' + probe.id()
                    + " sky light drifted: " + sky);
            values.add(probe.id() + '|' + token(position) + '|' + token(state)
                    + "|block=" + block + "|sky=" + sky);
        }
        return values;
    }

    private static void verifyBlock(RemoteWorldView world, BlockLightPlacement placement) {
        BlockPosition position = placement.position();
        require(world != null && world.blockAt(position.x(), position.y(), position.z())
                .equals(placement.expected()), "light treatment block drifted at " + token(position));
    }
    private static void verifySlot(RemoteInventoryView inventory,
            BlockLifecycleSlot expected, boolean after) {
        if (inventory == null || expected.inventorySlot() >= inventory.size()) {
            throw new IllegalStateException("light inventory slot is absent");
        }
        RemoteInventorySlot observed = inventory.slot(expected.inventorySlot());
        RemoteItemStack stack = after ? expected.after() : expected.before();
        require(stack == null ? observed.empty() : !observed.empty() && observed.item().equals(stack),
                "light inventory effect drifted at slot " + expected.inventorySlot());
    }
    private static String phase(boolean control) { return control ? "air control" : "treatment"; }
    private static String token(BlockPosition value) {
        return value.x() + ":" + value.y() + ":" + value.z();
    }
    private static String token(BlockState value) {
        return value.legacyId() + ":" + value.metadata();
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
