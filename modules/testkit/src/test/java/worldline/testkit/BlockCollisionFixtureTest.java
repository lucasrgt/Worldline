package worldline.testkit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import worldline.api.BlockCollisionDriver;
import worldline.api.BlockFace;
import worldline.api.BlockLifecycleDriver.ReloadBoundary;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.MovementDisposition;
import worldline.api.MovementOutcome;
import worldline.api.PlayerPose;
import worldline.api.RemoteChunkObservation;
import worldline.api.RemoteChunkSnapshot;
import worldline.api.RemoteInventorySlot;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteItemStack;
import worldline.api.RemoteWorldView;

/** Proves air controls, collision classification, exact consumption, and reload order. */
public final class BlockCollisionFixtureTest {
    private static final BlockPosition SUPPORT = new BlockPosition(4, 71, 4);
    private static final BlockPosition TARGET = new BlockPosition(4, 72, 4);
    private static final PlayerPose ORIGIN = new PlayerPose(4.5D, 72D, 3.5D, 0F, 0F);

    private BlockCollisionFixtureTest() { }

    public static void main(String[] arguments) {
        BlockConformancePlan plan = new BlockConformancePlan(List.of(
                new BlockConformanceProfile("b1.7.3:block/044",
                        List.of("special-collision"), false, Map.of())),
                List.of(new BlockConformanceTemplate(
                        "collision-shape", ConformanceLayer.ARCHETYPE)));
        BlockCollisionScenario scenario = new BlockCollisionScenario("slab-envelope",
                plan.caseFor("b1.7.3:block/044", "collision-shape"),
                new BlockLifecycleSlot(1, 37, new RemoteItemStack(44, 1, 0), null), 0F, 0F,
                List.of(new BlockCollisionPlacement(SUPPORT, BlockFace.UP,
                        new BlockState(44, 0))),
                List.of(new BlockCollisionProbe("level", 0D, 0D, 1D, 10,
                                BlockCollisionExpectation.BLOCKED),
                        new BlockCollisionProbe("half-step", 0D, 0.5D, 1D, 10,
                                BlockCollisionExpectation.PASSABLE)));
        FakeDriver driver = new FakeDriver();
        BlockCollisionEvidence evidence = BlockCollisionFixture.execute(scenario, driver);
        require(evidence.layer() == ConformanceLayer.ARCHETYPE
                && evidence.boundary() == ReloadBoundary.FRESH_LOGIN
                && evidence.controls().size() == 2 && evidence.treatments().size() == 2,
                "collision evidence drifted");
        require(evidence.canonical().equals(
                "schema=worldline.block-collision-evidence.v1\n"
                + "scenario=slab-envelope\nsubject=b1.7.3:block/044\n"
                + "claim.collision-shape=b1.7.3:block/044#collision-shape|ARCHETYPE\n"
                + "placement.1=4:72:4:44:0\n"
                + "control.1=level|PASSABLE|UNCHALLENGED|dx=0|dy=0|dz=1000\n"
                + "control.2=half-step|PASSABLE|UNCHALLENGED|dx=0|dy=500|dz=1000\n"
                + "treatment.1=level|BLOCKED|CORRECTED|dx=0|dy=0|dz=0\n"
                + "treatment.2=half-step|PASSABLE|UNCHALLENGED|dx=0|dy=500|dz=1000\n"
                + "reload=FRESH_LOGIN\n"), "canonical collision evidence drifted");
        require(driver.actions.indexOf("place") > driver.actions.indexOf("air-control")
                && driver.actions.indexOf("treatment") > driver.actions.indexOf("place")
                && driver.actions.indexOf("reload") > driver.actions.lastIndexOf("treatment"),
                "collision causal order drifted: " + driver.actions);
        rejects(() -> new BlockCollisionScenario("bad", scenario.claim(),
                scenario.placementSlot(), 0F, 0F, scenario.placements(), List.of()));
        System.out.println("BlockCollisionFixtureTest passed");
    }

    private static void rejects(Runnable action) {
        try { action.run(); throw new AssertionError("invalid collision row was accepted"); }
        catch (IllegalArgumentException expected) { }
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static final class FakeDriver implements BlockCollisionDriver {
        final List<String> actions = new ArrayList<String>();
        PlayerPose current = ORIGIN;
        boolean consumed, reloaded;

        @Override public RemoteInventoryView inventory() {
            actions.add("inventory"); List<RemoteInventorySlot> slots = new ArrayList<>();
            for (int index = 0; index < 45; index++) slots.add(new RemoteInventorySlot(index,
                    index == 37 && !consumed ? new RemoteItemStack(44, 1, 0) : null));
            return new RemoteInventoryView(0, slots);
        }
        @Override public void selectHeldSlot(int slot) { actions.add("select:" + slot); }
        @Override public void look(float yaw, float pitch) { actions.add("look"); }
        @Override public void useHeldItemOnBlock(BlockPosition support, BlockFace face) {
            require(support.equals(SUPPORT) && face == BlockFace.UP, "placement input drifted");
            consumed = true; actions.add("place");
        }
        @Override public RemoteWorldView awaitBlock(BlockPosition position, BlockState expected) {
            require(consumed && position.equals(TARGET), "unexpected collision observation");
            actions.add(reloaded ? "await-reload" : "await-live"); return view(position, expected);
        }
        @Override public RemoteWorldView sustainTicks(int ticks) {
            actions.add("ticks:" + ticks); return view(TARGET, new BlockState(0, 0));
        }
        @Override public PlayerPose origin() { return ORIGIN; }
        @Override public MovementOutcome moveAndObserve(double dx, double dy, double dz, int ticks) {
            PlayerPose attempted = pose(current.x() + dx, current.y() + dy, current.z() + dz);
            boolean treatment = consumed && dz > 0D;
            boolean blocked = treatment && dy == 0D;
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

    private static PlayerPose pose(double x, double y, double z) {
        return new PlayerPose(x, y, z, 0F, 0F);
    }
    private static RemoteWorldView view(BlockPosition position, BlockState state) {
        RemoteChunkObservation region = new RemoteChunkObservation(0, 0, 0, 16, 128, 16, 81920);
        RemoteChunkSnapshot empty = new RemoteChunkSnapshot(region, new byte[32768],
                new byte[16384], new byte[16384], new byte[16384]);
        return new RemoteWorldView(List.of(empty.withBlock(
                position.x(), position.y(), position.z(), state)));
    }
}
