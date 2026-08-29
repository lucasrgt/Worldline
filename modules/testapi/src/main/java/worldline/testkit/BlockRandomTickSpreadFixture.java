package worldline.testkit;

import java.util.List;
import worldline.api.BlockFace;
import worldline.api.BlockLifecycleDriver.ReloadBoundary;
import worldline.api.BlockPosition;
import worldline.api.BlockRandomTickSpreadDriver;
import worldline.api.BlockState;
import worldline.api.MovementOutcome;
import worldline.api.PlayerPose;
import worldline.api.RemoteInventorySlot;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteItemStack;
import worldline.api.RemoteWorldView;

/** Executes physical controls, bounded native spread, support loss, and reload. */
public final class BlockRandomTickSpreadFixture {
    private static final BlockState AIR = new BlockState(0, 0);
    private BlockRandomTickSpreadFixture() { }

    public static BlockRandomTickSpreadEvidence execute(BlockRandomTickSpreadScenario scenario,
            BlockRandomTickSpreadDriver driver) {
        if (scenario == null || driver == null) throw new NullPointerException("random tick spread");
        verifySlot(driver.inventory(), scenario.placementSlot(), false);
        for (BlockPosition source : scenario.sources()) verify(driver, source, AIR, "source control");
        for (BlockPosition target : scenario.targets()) verify(driver, target, AIR, "target control");
        verify(driver, scenario.control(), AIR, "invalid control");
        verifyLight(driver.observe(), scenario, AIR, "air control");
        probePassable(driver, scenario.collision(), driver.origin(), "air control");

        driver.selectHeldSlot(scenario.placementSlot().hotbarSlot());
        for (BlockPosition support : scenario.sourceSupports()) {
            driver.placeHeldBlock(support, BlockFace.UP);
            verify(driver, BlockFace.UP.adjacent(support), scenario.state(), "source placement");
        }
        verifySlot(driver.inventory(), scenario.placementSlot(), true);
        verifyLight(driver.observe(), scenario, scenario.state(), "treatment");
        probePassable(driver, scenario.collision(), driver.origin(), "treatment");
        awaitSpread(driver, scenario);
        verify(driver, scenario.control(), AIR, "invalid random-tick control");

        verifySlot(driver.inventory(), scenario.breakSlot(), false);
        driver.selectHeldSlot(scenario.breakSlot().hotbarSlot());
        driver.beginBreak(scenario.supportToBreak());
        if (scenario.breakTicks() > 0) driver.sustainTicks(scenario.breakTicks());
        driver.finishBreak(scenario.supportToBreak());
        verify(driver, scenario.supportToBreak(), AIR, "support removal");
        driver.sustainTicks(scenario.observationTicks());
        verify(driver, scenario.sourceToRemove(), AIR, "neighbor response");
        verifySlot(driver.inventory(), scenario.breakSlot(), true);

        driver.saveAndReload(); ReloadBoundary boundary = driver.reloadBoundary();
        require(any(driver.observe(), scenario.targets(), scenario.state()),
                "spread did not persist across reload");
        verify(driver, scenario.control(), AIR, "reload invalid control");
        verify(driver, scenario.sourceToRemove(), AIR, "reload removed source");
        return new BlockRandomTickSpreadEvidence(scenario, boundary);
    }

    private static void awaitSpread(BlockRandomTickSpreadDriver driver,
            BlockRandomTickSpreadScenario scenario) {
        for (int window = 0; window < scenario.maxWindows(); window++) {
            RemoteWorldView world = driver.sustainTicks(scenario.windowTicks());
            if (any(world, scenario.targets(), scenario.state())) return;
        }
        throw new IllegalStateException("bounded random-tick spread was not observed");
    }
    private static boolean any(RemoteWorldView world, List<BlockPosition> positions,
            BlockState expected) {
        for (BlockPosition position : positions) if (world.blockAt(
                position.x(), position.y(), position.z()).equals(expected)) return true;
        return false;
    }
    private static void verifyLight(RemoteWorldView world,
            BlockRandomTickSpreadScenario scenario, BlockState state, String phase) {
        BlockPosition probe = scenario.lightProbe();
        require(world.blockAt(probe.x(), probe.y(), probe.z()).equals(state),
                phase + " light state drifted");
        int block = world.blockLightAt(probe.x(), probe.y(), probe.z());
        int sky = world.skyLightAt(probe.x(), probe.y(), probe.z());
        require(block == scenario.blockLight() && sky == scenario.skyLight(),
                phase + " light plane drifted: expected block=" + scenario.blockLight()
                        + ",sky=" + scenario.skyLight() + " observed block=" + block
                        + ",sky=" + sky);
    }
    private static void probePassable(BlockRandomTickSpreadDriver driver,
            BlockCollisionProbe probe, PlayerPose origin, String phase) {
        MovementOutcome outcome = driver.moveAndObserve(probe.deltaX(), probe.deltaY(),
                probe.deltaZ(), probe.ticks());
        require(!outcome.corrected(), phase + " collision trajectory was corrected");
        PlayerPose current = outcome.resulting();
        for (int attempt = 0; attempt < 8; attempt++) {
            current = driver.moveAndObserve(origin.x() - current.x(), origin.y() - current.y(),
                    origin.z() - current.z(), probe.ticks()).resulting();
            if (close(current, origin)) return;
        }
        require(close(current, origin), phase + " collision origin did not reset");
    }
    private static boolean close(PlayerPose left, PlayerPose right) {
        return Math.abs(left.x() - right.x()) <= 0.002D
                && Math.abs(left.y() - right.y()) <= 0.002D
                && Math.abs(left.z() - right.z()) <= 0.002D;
    }
    private static void verify(BlockRandomTickSpreadDriver driver, BlockPosition position,
            BlockState state, String phase) {
        RemoteWorldView world;
        try {
            world = driver.awaitBlock(position, state);
        } catch (RuntimeException error) {
            throw new IllegalStateException(phase + " await failed at " + position
                    + " for " + state, error);
        }
        require(world.blockAt(position.x(), position.y(), position.z()).equals(state),
                phase + " state drifted");
    }
    private static void verifySlot(RemoteInventoryView inventory, BlockLifecycleSlot slot,
            boolean after) {
        RemoteInventorySlot observed = inventory.slot(slot.inventorySlot());
        RemoteItemStack expected = after ? slot.after() : slot.before();
        require(expected == null ? observed.empty()
                : !observed.empty() && observed.item().equals(expected),
                "random-tick spread inventory effect drifted");
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
