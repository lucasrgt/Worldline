package worldline.testapi;

import java.util.ArrayList;
import java.util.List;
import worldline.api.BlockCollisionDriver;
import worldline.api.BlockLifecycleDriver.ReloadBoundary;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.MovementOutcome;
import worldline.api.PlayerPose;
import worldline.api.RemoteInventorySlot;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteItemStack;
import worldline.api.RemoteWorldView;

/** Public causal executor for air-control versus placed-block collision envelopes. */
public final class BlockCollisionFixture {
    private static final double POSITION_EPSILON = 0.002D;

    private BlockCollisionFixture() { }

    public static BlockCollisionEvidence execute(BlockCollisionScenario scenario,
            BlockCollisionDriver driver) {
        if (scenario == null || driver == null) throw new NullPointerException("block collision");
        verifySlot(driver.inventory(), scenario.placementSlot(), false);
        PlayerPose origin = driver.origin();
        List<String> controls = probeAll(driver, origin, scenario.probes(), true);
        driver.selectHeldSlot(scenario.placementSlot().hotbarSlot());
        driver.look(scenario.yaw(), scenario.pitch());
        driver.sustainTicks(2);
        List<String> placements = new ArrayList<String>();
        for (BlockCollisionPlacement placement : scenario.placements()) {
            driver.useHeldItemOnBlock(placement.support(), placement.face());
            verifyBlock(driver.awaitBlock(placement.position(), placement.expected()), placement);
            placements.add(token(placement.position()) + ':' + token(placement.expected()));
        }
        driver.sustainTicks(2);
        verifySlot(driver.inventory(), scenario.placementSlot(), true);
        List<String> treatments = probeAll(driver, origin, scenario.probes(), false);
        driver.saveAndReload();
        ReloadBoundary boundary = driver.reloadBoundary();
        for (BlockCollisionPlacement placement : scenario.placements()) {
            verifyBlock(driver.awaitBlock(placement.position(), placement.expected()), placement);
        }
        return new BlockCollisionEvidence(scenario, controls, treatments, placements, boundary);
    }

    private static List<String> probeAll(BlockCollisionDriver driver, PlayerPose origin,
            List<BlockCollisionProbe> probes, boolean control) {
        List<String> evidence = new ArrayList<String>();
        for (BlockCollisionProbe probe : probes) {
            MovementOutcome outcome = driver.moveAndObserve(probe.deltaX(), probe.deltaY(),
                    probe.deltaZ(), probe.ticks());
            BlockCollisionExpectation expected = control
                    ? BlockCollisionExpectation.PASSABLE : probe.expected();
            if (expected == BlockCollisionExpectation.PASSABLE) {
                require(!outcome.corrected(), phase(control) + ' ' + probe.id()
                        + " trajectory was corrected");
            } else {
                require(outcome.corrected(), phase(control) + ' ' + probe.id()
                        + " trajectory was not blocked");
                require(progress(origin, outcome.resulting(), probe) < 250,
                        phase(control) + ' ' + probe.id() + " correction crossed obstacle");
            }
            evidence.add(probe.id() + '|' + expected + '|' + disposition(outcome)
                    + "|dx=" + milli(outcome.resulting().x() - origin.x())
                    + "|dy=" + milli(outcome.resulting().y() - origin.y())
                    + "|dz=" + milli(outcome.resulting().z() - origin.z()));
            reset(driver, origin, outcome.resulting(), probe.ticks(), probe.id());
        }
        return evidence;
    }

    private static void reset(BlockCollisionDriver driver, PlayerPose origin, PlayerPose current,
            int ticks, String probe) {
        PlayerPose result = current;
        for (int attempt = 0; attempt < 8; attempt++) {
            double dx = origin.x() - result.x();
            double dy = origin.y() - result.y();
            double dz = origin.z() - result.z();
            result = driver.moveAndObserve(dx, dy, dz, ticks).resulting();
            if (close(result.x(), origin.x()) && close(result.y(), origin.y())
                    && close(result.z(), origin.z())) {
                return;
            }
        }
        require(close(result.x(), origin.x()) && close(result.y(), origin.y())
                && close(result.z(), origin.z()), probe + " did not reset to collision origin");
    }

    private static int progress(PlayerPose origin, PlayerPose result, BlockCollisionProbe probe) {
        double length = probe.deltaX() * probe.deltaX() + probe.deltaY() * probe.deltaY()
                + probe.deltaZ() * probe.deltaZ();
        double dot = (result.x() - origin.x()) * probe.deltaX()
                + (result.y() - origin.y()) * probe.deltaY()
                + (result.z() - origin.z()) * probe.deltaZ();
        return milli(dot / length);
    }

    private static void verifyBlock(RemoteWorldView world, BlockCollisionPlacement placement) {
        BlockPosition position = placement.position(); BlockState expected = placement.expected();
        require(world != null && world.blockAt(position.x(), position.y(), position.z())
                .equals(expected), "collision treatment block drifted at " + token(position));
    }

    private static void verifySlot(RemoteInventoryView inventory,
            BlockLifecycleSlot expected, boolean after) {
        if (inventory == null || expected.inventorySlot() >= inventory.size()) {
            throw new IllegalStateException("collision inventory slot is absent");
        }
        RemoteInventorySlot observed = inventory.slot(expected.inventorySlot());
        RemoteItemStack stack = after ? expected.after() : expected.before();
        require(stack == null ? observed.empty()
                : !observed.empty() && observed.item().equals(stack),
                "collision inventory effect drifted at slot " + expected.inventorySlot());
    }

    private static String phase(boolean control) { return control ? "air control" : "treatment"; }
    private static String disposition(MovementOutcome value) {
        return value.corrected() ? "CORRECTED" : "UNCHALLENGED";
    }
    private static boolean close(double left, double right) {
        return Math.abs(left - right) <= POSITION_EPSILON;
    }
    private static int milli(double value) { return (int) Math.round(value * 1000D); }
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
