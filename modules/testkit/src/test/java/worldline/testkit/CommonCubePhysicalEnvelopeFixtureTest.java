package worldline.testkit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import worldline.api.BlockCollisionDriver;
import worldline.api.BlockFace;
import worldline.api.BlockLifecycleDriver.ReloadBoundary;
import worldline.api.BlockLightDriver;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.BlockStateDomainDriver;
import worldline.api.MovementDisposition;
import worldline.api.MovementOutcome;
import worldline.api.PlayerPose;
import worldline.api.RemoteChunkObservation;
import worldline.api.RemoteChunkSnapshot;
import worldline.api.RemoteInventorySlot;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteItemStack;
import worldline.api.RemoteWorldView;

/** Drives representative common-cube rows through public physical-envelope fixtures. */
public final class CommonCubePhysicalEnvelopeFixtureTest {
    private static final BlockPosition SUPPORT = new BlockPosition(4, 71, 4);
    private static final BlockPosition TARGET = new BlockPosition(4, 72, 4);
    private static final BlockPosition[] SUPPORTS = {
        SUPPORT, new BlockPosition(6, 71, 4),
        new BlockPosition(4, 71, 6), new BlockPosition(6, 71, 6)
    };
    private static final PlayerPose ORIGIN = new PlayerPose(4.5D, 72D, 3.5D, 0F, 0F);
    private static final BlockState DIRT = new BlockState(3, 0);
    private static final BlockState AIR = new BlockState(0, 0);

    private CommonCubePhysicalEnvelopeFixtureTest() { }

    public static void main(String[] arguments) {
        proveStateDomain();
        proveCollision();
        proveLight();
        System.out.println("CommonCubePhysicalEnvelopeFixtureTest passed");
    }

    private static void proveStateDomain() {
        BlockStateDomainScenario scenario = new BlockStateDomainScenario(
                "dirt-static-physical-envelope", claim("state-domain"),
                slot(4), List.of(DIRT), yawSteps(), 40);
        StateDriver driver = new StateDriver();
        BlockStateDomainEvidence evidence = BlockStateDomainFixture.execute(scenario, driver);
        require(evidence.layer() == ConformanceLayer.ARCHETYPE
                && evidence.boundary() == ReloadBoundary.FRESH_LOGIN
                && evidence.domain().equals(List.of(DIRT)), "state-domain evidence drifted");
        require(evidence.canonical().equals(
                "schema=worldline.block-state-domain-evidence.v1\n"
                + "scenario=dirt-static-physical-envelope\nsubject=b1.7.3:block/003\n"
                + "claim.state-domain=b1.7.3:block/003#state-domain|ARCHETYPE\n"
                + "domain=3:0\n"
                + "step.1=place-yaw-0|PLACE_HELD|4:72:4:3:0\n"
                + "step.2=place-yaw-1|PLACE_HELD|6:72:4:3:0\n"
                + "step.3=place-yaw-2|PLACE_HELD|4:72:6:3:0\n"
                + "step.4=place-yaw-3|PLACE_HELD|6:72:6:3:0\n"
                + "reload=FRESH_LOGIN\n"), "canonical state-domain evidence drifted");
        require(driver.remaining == 0 && driver.reloaded, "state-domain consumption drifted");
    }

    private static void proveCollision() {
        BlockCollisionScenario scenario = new BlockCollisionScenario(
                "dirt-static-physical-envelope", claim("collision-shape"),
                slot(1), 0F, 0F, List.of(new BlockCollisionPlacement(SUPPORT, BlockFace.UP, DIRT)),
                List.of(probe("level", 0D, BlockCollisionExpectation.BLOCKED),
                        probe("half-step", 0.5D, BlockCollisionExpectation.BLOCKED),
                        probe("full-step", 1D, BlockCollisionExpectation.PASSABLE)));
        CollisionDriver driver = new CollisionDriver();
        BlockCollisionEvidence evidence = BlockCollisionFixture.execute(scenario, driver);
        require(evidence.layer() == ConformanceLayer.ARCHETYPE
                && evidence.boundary() == ReloadBoundary.FRESH_LOGIN
                && evidence.controls().size() == 3 && evidence.treatments().size() == 3,
                "collision evidence drifted");
        require(evidence.canonical().equals(
                "schema=worldline.block-collision-evidence.v1\n"
                + "scenario=dirt-static-physical-envelope\nsubject=b1.7.3:block/003\n"
                + "claim.collision-shape=b1.7.3:block/003#collision-shape|ARCHETYPE\n"
                + "placement.1=4:72:4:3:0\n"
                + "control.1=level|PASSABLE|UNCHALLENGED|dx=0|dy=0|dz=1000\n"
                + "control.2=half-step|PASSABLE|UNCHALLENGED|dx=0|dy=500|dz=1000\n"
                + "control.3=full-step|PASSABLE|UNCHALLENGED|dx=0|dy=1000|dz=1000\n"
                + "treatment.1=level|BLOCKED|CORRECTED|dx=0|dy=0|dz=0\n"
                + "treatment.2=half-step|BLOCKED|CORRECTED|dx=0|dy=0|dz=0\n"
                + "treatment.3=full-step|PASSABLE|UNCHALLENGED|dx=0|dy=1000|dz=1000\n"
                + "reload=FRESH_LOGIN\n"), "canonical collision evidence drifted");
        require(driver.actions.indexOf("place") > driver.actions.indexOf("air-control")
                && driver.actions.indexOf("reload") > driver.actions.lastIndexOf("treatment"),
                "collision causal order drifted: " + driver.actions);
    }

    private static void proveLight() {
        BlockLightScenario scenario = new BlockLightScenario(
                "dirt-static-physical-envelope", claim("light-behavior"),
                slot(1), 0F, 0F, List.of(new BlockLightPlacement(SUPPORT, BlockFace.UP, DIRT)),
                List.of(new BlockLightProbe("source", TARGET,
                        new BlockLightExpectation(AIR, 0, 15),
                        new BlockLightExpectation(DIRT, 0, 0))));
        LightDriver driver = new LightDriver();
        BlockLightEvidence evidence = BlockLightFixture.execute(scenario, driver);
        require(evidence.layer() == ConformanceLayer.ARCHETYPE
                && evidence.boundary() == ReloadBoundary.FRESH_LOGIN
                && evidence.controls().size() == 1 && evidence.treatments().size() == 1,
                "light evidence drifted");
        require(evidence.canonical().equals(
                "schema=worldline.block-light-evidence.v1\n"
                + "scenario=dirt-static-physical-envelope\nsubject=b1.7.3:block/003\n"
                + "claim.light-behavior=b1.7.3:block/003#light-behavior|ARCHETYPE\n"
                + "placement.1=4:72:4:3:0\n"
                + "control.1=source|4:72:4|0:0|block=0|sky=15\n"
                + "treatment.1=source|4:72:4|3:0|block=0|sky=0\n"
                + "reload=FRESH_LOGIN\n"), "canonical light evidence drifted");
        require(driver.actions.indexOf("observe-control") < driver.actions.indexOf("place")
                && driver.actions.indexOf("reload") < driver.actions.indexOf("observe-treatment"),
                "light causal order drifted: " + driver.actions);
    }

    private static List<BlockStateDomainStep> yawSteps() {
        float[] yaws = {0F, 90F, 180F, -90F};
        List<BlockStateDomainStep> steps = new ArrayList<BlockStateDomainStep>();
        for (int index = 0; index < yaws.length; index++) {
            BlockPosition support = SUPPORTS[index];
            steps.add(BlockStateDomainStep.place("place-yaw-" + index, support, BlockFace.UP,
                    yaws[index], 0F, List.of(new BlockStateObservation(
                            BlockFace.UP.adjacent(support), DIRT))));
        }
        return steps;
    }

    private static BlockConformanceCase claim(String template) {
        return new BlockConformancePlan(List.of(new BlockConformanceProfile(
                "b1.7.3:block/003", List.of("simple-solid"), false, Map.of())),
                List.of(new BlockConformanceTemplate(template, ConformanceLayer.ARCHETYPE)))
                .caseFor("b1.7.3:block/003", template);
    }

    private static BlockLifecycleSlot slot(int count) {
        return new BlockLifecycleSlot(1, 37, new RemoteItemStack(3, count, 0), null);
    }

    private static BlockCollisionProbe probe(String id, double rise,
            BlockCollisionExpectation expectation) {
        return new BlockCollisionProbe(id, 0D, rise, 1D, 10, expectation);
    }

    private static RemoteInventoryView slots(int remaining) {
        List<RemoteInventorySlot> slots = new ArrayList<RemoteInventorySlot>();
        for (int index = 0; index < 45; index++) slots.add(new RemoteInventorySlot(index,
                index == 37 && remaining > 0 ? new RemoteItemStack(3, remaining, 0) : null));
        return new RemoteInventoryView(0, slots);
    }

    private static RemoteWorldView world(BlockPosition position, BlockState state) {
        RemoteChunkObservation region = new RemoteChunkObservation(0, 0, 0, 16, 128, 16, 81920);
        RemoteChunkSnapshot empty = new RemoteChunkSnapshot(region, new byte[32768],
                new byte[16384], new byte[16384], new byte[16384]);
        return new RemoteWorldView(List.of(empty.withBlock(
                position.x(), position.y(), position.z(), state)));
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static final class StateDriver implements BlockStateDomainDriver {
        int remaining = 4;
        boolean reloaded;

        @Override public RemoteInventoryView inventory() { return slots(remaining); }
        @Override public void selectHeldSlot(int slot) { }
        @Override public void look(float yaw, float pitch) { }
        @Override public void useHeldItemOnBlock(BlockPosition support, BlockFace face) {
            require(face == BlockFace.UP && remaining > 0, "state-domain placement drifted");
            remaining--;
        }
        @Override public void activateBlock(BlockPosition position, BlockFace face) {
            throw new AssertionError("common-cube state-domain does not activate");
        }
        @Override public RemoteWorldView awaitBlock(BlockPosition position, BlockState expected) {
            require(remaining < 4 && expected.equals(DIRT), "unexpected state-domain wait");
            return world(position, expected);
        }
        @Override public RemoteWorldView sustainTicks(int ticks) {
            return world(TARGET, AIR);
        }
        @Override public void saveAndReload() { reloaded = true; }
        @Override public ReloadBoundary reloadBoundary() {
            require(reloaded, "reload requested early"); return ReloadBoundary.FRESH_LOGIN;
        }
        @Override public void close() { }
    }

    private static final class CollisionDriver implements BlockCollisionDriver {
        final List<String> actions = new ArrayList<String>();
        PlayerPose current = ORIGIN;
        boolean consumed, reloaded;

        @Override public RemoteInventoryView inventory() { return slots(consumed ? 0 : 1); }
        @Override public void selectHeldSlot(int slot) { }
        @Override public void look(float yaw, float pitch) { }
        @Override public void useHeldItemOnBlock(BlockPosition support, BlockFace face) {
            require(support.equals(SUPPORT) && face == BlockFace.UP, "collision placement drifted");
            consumed = true; actions.add("place");
        }
        @Override public RemoteWorldView awaitBlock(BlockPosition position, BlockState expected) {
            require(consumed && position.equals(TARGET), "unexpected collision observation");
            return world(position, expected);
        }
        @Override public RemoteWorldView sustainTicks(int ticks) { return world(TARGET, AIR); }
        @Override public PlayerPose origin() { return ORIGIN; }
        @Override public MovementOutcome moveAndObserve(double dx, double dy, double dz, int ticks) {
            PlayerPose attempted = new PlayerPose(current.x() + dx, current.y() + dy,
                    current.z() + dz, 0F, 0F);
            boolean treatment = consumed && dz > 0D;
            boolean blocked = treatment && dy < 1D;
            actions.add(treatment ? "treatment" : "air-control");
            PlayerPose result = blocked ? current : attempted;
            MovementOutcome outcome = new MovementOutcome(attempted, result, blocked
                    ? MovementDisposition.CORRECTED : MovementDisposition.UNCHALLENGED);
            current = result; return outcome;
        }
        @Override public void saveAndReload() { reloaded = true; actions.add("reload"); }
        @Override public ReloadBoundary reloadBoundary() {
            require(reloaded, "reload requested early"); return ReloadBoundary.FRESH_LOGIN;
        }
        @Override public void close() { }
    }

    private static final class LightDriver implements BlockLightDriver {
        final List<String> actions = new ArrayList<String>();
        boolean consumed, reloaded;

        @Override public RemoteInventoryView inventory() { return slots(consumed ? 0 : 1); }
        @Override public void selectHeldSlot(int slot) { }
        @Override public void look(float yaw, float pitch) { }
        @Override public void useHeldItemOnBlock(BlockPosition support, BlockFace face) {
            require(support.equals(SUPPORT) && face == BlockFace.UP, "light placement drifted");
            consumed = true; actions.add("place");
        }
        @Override public RemoteWorldView awaitBlock(BlockPosition position, BlockState expected) {
            require(consumed && position.equals(TARGET), "unexpected light block wait");
            return lightView(true);
        }
        @Override public RemoteWorldView sustainTicks(int ticks) {
            require(ticks == 2, "light inventory barrier drifted");
            return lightView(true);
        }
        @Override public RemoteWorldView observe() {
            actions.add(reloaded ? "observe-treatment" : "observe-control");
            return lightView(reloaded);
        }
        @Override public void saveAndReload() { reloaded = true; actions.add("reload"); }
        @Override public ReloadBoundary reloadBoundary() {
            require(reloaded, "reload requested early"); return ReloadBoundary.FRESH_LOGIN;
        }
        @Override public void close() { }
    }

    private static RemoteWorldView lightView(boolean treatment) {
        RemoteChunkObservation region = new RemoteChunkObservation(0, 0, 0, 16, 128, 16, 81920);
        byte[] ids = new byte[32768], metadata = new byte[16384];
        byte[] block = new byte[16384], sky = new byte[16384];
        Arrays.fill(sky, (byte) 255);
        if (treatment) {
            ids[index(TARGET)] = 3;
            nibble(sky, TARGET, 0);
        }
        return new RemoteWorldView(List.of(new RemoteChunkSnapshot(region,
                ids, metadata, block, sky)));
    }

    private static int index(BlockPosition value) {
        return (value.x() * 16 + value.z()) * 128 + value.y();
    }

    private static void nibble(byte[] values, BlockPosition position, int light) {
        int index = index(position), pair = values[index >> 1] & 255;
        values[index >> 1] = (byte) ((index & 1) == 0
                ? pair & 240 | light : pair & 15 | light << 4);
    }
}
