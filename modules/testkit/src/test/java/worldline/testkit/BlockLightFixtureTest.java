package worldline.testkit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import worldline.api.BlockFace;
import worldline.api.BlockLifecycleDriver.ReloadBoundary;
import worldline.api.BlockLightDriver;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.RemoteChunkObservation;
import worldline.api.RemoteChunkSnapshot;
import worldline.api.RemoteInventorySlot;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteItemStack;
import worldline.api.RemoteWorldView;

/** Proves exact light planes, gameplay consumption, fresh login, and canonical evidence. */
public final class BlockLightFixtureTest {
    private static final BlockPosition SUPPORT = new BlockPosition(4, 71, 4);
    private static final BlockPosition SOURCE = new BlockPosition(4, 72, 4);
    private static final BlockPosition NEAR = new BlockPosition(5, 72, 4);
    private static final BlockPosition FAR = new BlockPosition(6, 72, 4);

    private BlockLightFixtureTest() { }

    public static void main(String[] arguments) {
        BlockConformancePlan plan = new BlockConformancePlan(List.of(
                new BlockConformanceProfile("b1.7.3:block/089",
                        List.of("luminous", "simple-solid"), false, Map.of())),
                List.of(new BlockConformanceTemplate(
                        "light-behavior", ConformanceLayer.ARCHETYPE)));
        BlockLightScenario scenario = new BlockLightScenario("glowstone-propagation",
                plan.caseFor("b1.7.3:block/089", "light-behavior"),
                new BlockLifecycleSlot(1, 37, new RemoteItemStack(89, 1, 0), null), 0F, 0F,
                List.of(new BlockLightPlacement(SUPPORT, BlockFace.UP, new BlockState(89, 0))),
                List.of(probe("source", SOURCE, new BlockState(89, 0), 15),
                        probe("distance-1", NEAR, new BlockState(0, 0), 14),
                        probe("distance-2", FAR, new BlockState(0, 0), 13)));
        FakeDriver driver = new FakeDriver();
        BlockLightEvidence evidence = BlockLightFixture.execute(scenario, driver);
        require(evidence.layer() == ConformanceLayer.ARCHETYPE
                && evidence.boundary() == ReloadBoundary.FRESH_LOGIN
                && evidence.controls().size() == 3 && evidence.treatments().size() == 3,
                "light evidence drifted");
        require(evidence.canonical().equals(
                "schema=worldline.block-light-evidence.v1\n"
                + "scenario=glowstone-propagation\nsubject=b1.7.3:block/089\n"
                + "claim.light-behavior=b1.7.3:block/089#light-behavior|ARCHETYPE\n"
                + "placement.1=4:72:4:89:0\n"
                + "control.1=source|4:72:4|0:0|block=0|sky=15\n"
                + "control.2=distance-1|5:72:4|0:0|block=0|sky=15\n"
                + "control.3=distance-2|6:72:4|0:0|block=0|sky=15\n"
                + "treatment.1=source|4:72:4|89:0|block=15|sky=15\n"
                + "treatment.2=distance-1|5:72:4|0:0|block=14|sky=15\n"
                + "treatment.3=distance-2|6:72:4|0:0|block=13|sky=15\n"
                + "reload=FRESH_LOGIN\n"), "canonical light evidence drifted");
        require(driver.actions.indexOf("observe-control") < driver.actions.indexOf("place")
                && driver.actions.indexOf("reload") < driver.actions.indexOf("observe-treatment"),
                "light causal order drifted: " + driver.actions);
        rejects(() -> new BlockLightScenario("bad", scenario.claim(), scenario.placementSlot(),
                0F, 0F, scenario.placements(), List.of()));
        System.out.println("BlockLightFixtureTest passed");
    }

    private static BlockLightProbe probe(String id, BlockPosition position,
            BlockState treatment, int blockLight) {
        return new BlockLightProbe(id, position,
                new BlockLightExpectation(new BlockState(0, 0), 0, 15),
                new BlockLightExpectation(treatment, blockLight, 15));
    }
    private static void rejects(Runnable action) {
        try { action.run(); throw new AssertionError("invalid light row was accepted"); }
        catch (IllegalArgumentException expected) { }
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static final class FakeDriver implements BlockLightDriver {
        final List<String> actions = new ArrayList<String>();
        boolean consumed, reloaded;
        @Override public RemoteInventoryView inventory() {
            List<RemoteInventorySlot> slots = new ArrayList<RemoteInventorySlot>();
            for (int index = 0; index < 45; index++) slots.add(new RemoteInventorySlot(index,
                    index == 37 && !consumed ? new RemoteItemStack(89, 1, 0) : null));
            return new RemoteInventoryView(0, slots);
        }
        @Override public void selectHeldSlot(int slot) { actions.add("select:" + slot); }
        @Override public void look(float yaw, float pitch) { actions.add("look"); }
        @Override public void useHeldItemOnBlock(BlockPosition support, BlockFace face) {
            require(support.equals(SUPPORT) && face == BlockFace.UP, "light placement drifted");
            consumed = true; actions.add("place");
        }
        @Override public RemoteWorldView awaitBlock(BlockPosition position, BlockState expected) {
            require(consumed && position.equals(SOURCE), "unexpected light block wait");
            actions.add(reloaded ? "await-reload" : "await-live"); return view(true);
        }
        @Override public RemoteWorldView observe() {
            actions.add(reloaded ? "observe-treatment" : "observe-control"); return view(reloaded);
        }
        @Override public void saveAndReload() { reloaded = true; actions.add("reload"); }
        @Override public ReloadBoundary reloadBoundary() {
            require(reloaded, "reload requested early"); return ReloadBoundary.FRESH_LOGIN;
        }
        @Override public void close() { }
    }

    private static RemoteWorldView view(boolean treatment) {
        RemoteChunkObservation region = new RemoteChunkObservation(0, 0, 0, 16, 128, 16, 81920);
        byte[] ids = new byte[32768], metadata = new byte[16384];
        byte[] block = new byte[16384], sky = new byte[16384]; Arrays.fill(sky, (byte) 255);
        if (treatment) {
            ids[index(SOURCE)] = 89; light(block, SOURCE, 15);
            light(block, NEAR, 14); light(block, FAR, 13);
        }
        return new RemoteWorldView(List.of(new RemoteChunkSnapshot(region,
                ids, metadata, block, sky)));
    }
    private static int index(BlockPosition value) {
        return (value.x() * 16 + value.z()) * 128 + value.y();
    }
    private static void light(byte[] values, BlockPosition position, int light) {
        int index = index(position), pair = values[index >> 1] & 255;
        values[index >> 1] = (byte) ((index & 1) == 0
                ? pair & 240 | light : pair & 15 | light << 4);
    }
}
