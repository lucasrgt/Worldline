package worldline.testkit;
import worldline.testapi.BlockCollisionExpectation;
import worldline.testapi.BlockCollisionProbe;
import worldline.testapi.BlockConformanceCase;
import worldline.testapi.BlockConformancePlan;
import worldline.testapi.BlockConformanceProfile;
import worldline.testapi.BlockConformanceTemplate;
import worldline.testapi.BlockLifecycleSlot;
import worldline.testapi.BlockRandomTickSpreadEvidence;
import worldline.testapi.BlockRandomTickSpreadFixture;
import worldline.testapi.BlockRandomTickSpreadScenario;
import worldline.testapi.ConformanceLayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import worldline.api.BlockFace;
import worldline.api.BlockLifecycleDriver.ReloadBoundary;
import worldline.api.BlockPosition;
import worldline.api.BlockRandomTickSpreadDriver;
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

/** Locks normalized random-tick evidence and the causal action order. */
public final class BlockRandomTickSpreadFixtureTest {
    private static final BlockState AIR = new BlockState(0, 0);
    private static final BlockState DIRT = new BlockState(3, 0);
    private static final BlockState MUSHROOM = new BlockState(39, 0);
    private static final BlockPosition SOURCE_A = new BlockPosition(4, 64, 4);
    private static final BlockPosition SOURCE_B = new BlockPosition(5, 64, 4);
    private static final BlockPosition TARGET = new BlockPosition(4, 65, 5);
    private static final BlockPosition CONTROL = new BlockPosition(5, 65, 5);
    private static final PlayerPose ORIGIN = new PlayerPose(4.5D, 65D, 3.5D, 0F, 0F);
    private BlockRandomTickSpreadFixtureTest() { }

    public static void execute() {
        BlockRandomTickSpreadScenario scenario = scenario(); FakeDriver driver = new FakeDriver();
        BlockRandomTickSpreadEvidence evidence = BlockRandomTickSpreadFixture.execute(
                scenario, driver);
        require(evidence.canonical().equals("schema=worldline.block-random-tick-spread-evidence.v1\n"
                + "scenario=brown-mushroom-spread\nsubject=b1.7.3:block/039\n"
                + "claim.state-domain=b1.7.3:block/039#state-domain|ARCHETYPE\n"
                + "claim.collision-shape=b1.7.3:block/039#collision-shape|ARCHETYPE\n"
                + "claim.light-behavior=b1.7.3:block/039#light-behavior|ARCHETYPE\n"
                + "claim.tick-policy=b1.7.3:block/039#tick-policy|ARCHETYPE\n"
                + "claim.neighbor-response=b1.7.3:block/039#neighbor-response|ARCHETYPE\n"
                + "state=39:0;domain=singleton\nsources=2;targets=1\n"
                + "collision=PASSABLE\nlight=block:12->12;sky:12->12\n"
                + "tick=windows<=4x20;transition=0:0->39:0;winning-window=excluded\n"
                + "control=invalid-support-air\nneighbor=support-remove;break-ticks=4;source=39:0->0:0\n"
                + "reload=FRESH_LOGIN\n"), "random-tick spread evidence drifted");
        require(driver.reloaded && driver.spread && !driver.support,
                "random-tick spread fixture did not reach final state");
        rejects(() -> new BlockRandomTickSpreadScenario("bad", scenario.claims(), MUSHROOM,
                DIRT, scenario.sourceSupports(), scenario.targets(), CONTROL, SOURCE_A,
                SOURCE_A, scenario.placementSlot(), scenario.breakSlot(), scenario.collision(),
                12, 12, 0, 4, 4, 10));
    }

    private static BlockRandomTickSpreadScenario scenario() {
        String subject = "b1.7.3:block/039";
        List<BlockConformanceTemplate> templates = new ArrayList<>();
        for (String id : Arrays.asList("state-domain", "collision-shape", "light-behavior",
                "tick-policy", "neighbor-response")) {
            templates.add(new BlockConformanceTemplate(id, ConformanceLayer.ARCHETYPE));
        }
        BlockConformancePlan plan = new BlockConformancePlan(Collections.singletonList(
                new BlockConformanceProfile(subject, Arrays.asList("vegetation",
                        "support-dependent", "random-tick"), false,
                        Collections.emptyMap())), templates);
        List<BlockConformanceCase> claims = new ArrayList<BlockConformanceCase>();
        for (BlockConformanceTemplate template : templates) {
            claims.add(plan.caseFor(subject, template.id()));
        }
        return new BlockRandomTickSpreadScenario("brown-mushroom-spread", claims, MUSHROOM,
                DIRT, Arrays.asList(SOURCE_A, SOURCE_B), Collections.singletonList(TARGET),
                CONTROL, SOURCE_A,
                BlockFace.UP.adjacent(SOURCE_A), slot(1, 37, new RemoteItemStack(39, 2, 0), null),
                slot(2, 38, new RemoteItemStack(277, 1, 0),
                        new RemoteItemStack(277, 1, 1)),
                new BlockCollisionProbe("level", 0D, 0D, 1D, 4,
                        BlockCollisionExpectation.PASSABLE), 12, 12, 20, 4, 4, 10);
    }
    private static BlockLifecycleSlot slot(int hotbar, int inventory, RemoteItemStack before,
            RemoteItemStack after) {
        return new BlockLifecycleSlot(hotbar, inventory, before, after);
    }
    private static void rejects(Runnable action) {
        try { action.run(); throw new AssertionError("invalid spread scenario was accepted"); }
        catch (IllegalArgumentException expected) { }
    }
    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }

    private static final class FakeDriver implements BlockRandomTickSpreadDriver {
        final Map<BlockPosition, BlockState> blocks = new HashMap<>();
        PlayerPose pose = ORIGIN; boolean placed, spread, support = true, damaged, reloaded;
        FakeDriver() { blocks.put(SOURCE_A, DIRT); blocks.put(SOURCE_B, DIRT); }
        @Override public RemoteInventoryView inventory() {
            List<RemoteInventorySlot> slots = new ArrayList<>();
            for (int index = 0; index < 45; index++) {
                RemoteItemStack item = index == 37 && !placed ? new RemoteItemStack(39, 2, 0)
                        : index == 38 ? new RemoteItemStack(277, 1, damaged ? 1 : 0) : null;
                slots.add(new RemoteInventorySlot(index, item));
            }
            return new RemoteInventoryView(0, slots);
        }
        @Override public void selectHeldSlot(int slot) { }
        @Override public void placeHeldBlock(BlockPosition supportAt, BlockFace face) {
            blocks.put(BlockFace.UP.adjacent(supportAt), MUSHROOM);
            placed = blocks.containsKey(BlockFace.UP.adjacent(SOURCE_A))
                    && blocks.containsKey(BlockFace.UP.adjacent(SOURCE_B));
        }
        @Override public void beginBreak(BlockPosition position) {
            require(position.equals(SOURCE_A), "unexpected support break");
        }
        @Override public void finishBreak(BlockPosition position) {
            support = false; damaged = true; blocks.put(position, AIR);
            blocks.put(BlockFace.UP.adjacent(position), AIR);
        }
        @Override public RemoteWorldView awaitBlock(BlockPosition position, BlockState expected) {
            require(state(position).equals(expected), "unexpected block observation at " + position);
            return view();
        }
        @Override public RemoteWorldView observe() { return view(); }
        @Override public RemoteWorldView sustainTicks(int ticks) {
            if (placed && !spread && ticks == 20) { spread = true; blocks.put(TARGET, MUSHROOM); }
            return view();
        }
        @Override public PlayerPose origin() { return ORIGIN; }
        @Override public MovementOutcome moveAndObserve(double x, double y, double z, int ticks) {
            pose = new PlayerPose(pose.x() + x, pose.y() + y, pose.z() + z, 0F, 0F);
            return new MovementOutcome(pose, pose, MovementDisposition.UNCHALLENGED);
        }
        @Override public void saveAndReload() { reloaded = true; }
        @Override public ReloadBoundary reloadBoundary() {
            require(reloaded, "reload boundary requested early"); return ReloadBoundary.FRESH_LOGIN;
        }
        @Override public void close() { }
        private BlockState state(BlockPosition position) { return blocks.getOrDefault(position, AIR); }
        private RemoteWorldView view() {
            RemoteChunkObservation region = new RemoteChunkObservation(0, 0, 0, 16, 128, 16, 81920);
            byte[] ids = new byte[32768], data = new byte[16384];
            byte[] light = new byte[16384]; java.util.Arrays.fill(light, (byte) 0xcc);
            RemoteChunkSnapshot chunk = new RemoteChunkSnapshot(region, ids, data, light, light);
            for (Map.Entry<BlockPosition, BlockState> entry : blocks.entrySet()) {
                BlockPosition p = entry.getKey(); chunk = chunk.withBlock(p.x(), p.y(), p.z(), entry.getValue());
            }
            return new RemoteWorldView(Collections.singletonList(chunk));
        }
    }
}
