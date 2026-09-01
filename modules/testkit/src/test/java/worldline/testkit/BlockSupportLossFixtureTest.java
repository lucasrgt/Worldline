package worldline.testkit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import worldline.api.BlockFace;
import worldline.api.BlockLifecycleDriver;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.RemoteChunkObservation;
import worldline.api.RemoteChunkSnapshot;
import worldline.api.RemoteDroppedItem;
import worldline.api.RemoteInventorySlot;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteItemStack;
import worldline.api.RemoteWorldView;

/** Proves causal support removal rather than an unrelated-neighbor observation. */
public final class BlockSupportLossFixtureTest {
    private static final BlockPosition SUPPORT = new BlockPosition(4, 64, 4);
    private static final BlockPosition TARGET = new BlockPosition(4, 65, 4);
    private static final BlockState DIRT = new BlockState(3, 0);
    private static final BlockState FLOWER = new BlockState(37, 0);
    private BlockSupportLossFixtureTest() {
    }

    public static void execute() {
        BlockConformancePlan plan = new BlockConformancePlan(Collections.singletonList(
                new BlockConformanceProfile("b1.7.3:block/037",
                        Arrays.asList("vegetation", "support-dependent"), false,
                        Collections.<String, ConformanceLayer>emptyMap())), Arrays.asList(
                new BlockConformanceTemplate("tick-policy", ConformanceLayer.ARCHETYPE),
                new BlockConformanceTemplate("neighbor-response", ConformanceLayer.ARCHETYPE)));
        BlockSupportLossScenario scenario = new BlockSupportLossScenario("dandelion-support-loss",
                plan.caseFor("b1.7.3:block/037", "tick-policy"),
                plan.caseFor("b1.7.3:block/037", "neighbor-response"), SUPPORT, DIRT,
                FLOWER, slot(1, 37, 0, -1), slot(2, 277, 0, 1), 240, 4, 40);
        FakeDriver driver = new FakeDriver();
        BlockSupportLossEvidence evidence = BlockSupportLossFixture.execute(scenario, driver);
        require(evidence.boundary() == BlockLifecycleDriver.ReloadBoundary.FRESH_LOGIN,
                "support-loss reload boundary drifted");
        require(evidence.canonical().equals(
                "schema=worldline.block-support-loss-evidence.v1\n"
                + "scenario=dandelion-support-loss\nsubject=b1.7.3:block/037\n"
                + "claim.tick-policy=b1.7.3:block/037#tick-policy|ARCHETYPE\n"
                + "claim.neighbor-response=b1.7.3:block/037#neighbor-response|ARCHETYPE\n"
                + "support=4:64:4:3:0->0:0\ntarget=4:65:4:37:0->0:0\n"
                + "tick-window=240\nsupport-break-ticks=4\n"
                + "support-observation-ticks=40\nreload=FRESH_LOGIN\n"),
                "canonical support-loss evidence drifted");
        require(driver.actions.equals(Arrays.asList("await:4:64:4:3:0", "await:4:65:4:0:0",
                "inventory", "select:1", "place", "await:4:65:4:37:0", "inventory",
                "ticks:240", "await:4:64:4:3:0", "await:4:65:4:37:0", "inventory",
                "select:2", "begin:4:64:4", "ticks:4", "finish:4:64:4",
                "await:4:64:4:0:0", "ticks:40", "await:4:65:4:0:0", "inventory",
                "reload", "await:4:64:4:0:0", "await:4:65:4:0:0")),
                "support-loss action order drifted: " + driver.actions);
        rejects(() -> new BlockSupportLossScenario("bad", scenario.tickPolicy(),
                scenario.neighborResponse(), SUPPORT, DIRT, FLOWER,
                scenario.placementSlot(), scenario.breakSlot(), 0, 4, 40));
    }

    private static BlockLifecycleSlot slot(int hotbar, int id, int beforeDamage,
            int afterDamage) {
        return new BlockLifecycleSlot(hotbar, hotbar + 36,
                new RemoteItemStack(id, 1, beforeDamage), afterDamage < 0 ? null
                        : new RemoteItemStack(id, 1, afterDamage));
    }

    private static void rejects(Runnable action) {
        try { action.run(); throw new AssertionError("invalid support-loss row was accepted"); }
        catch (IllegalArgumentException expected) {
        }
    }

    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }

    private static final class FakeDriver implements BlockLifecycleDriver {
        final List<String> actions = new ArrayList<String>();
        boolean placed, support = true, damaged, reloaded;

        @Override public RemoteInventoryView inventory() {
            actions.add("inventory"); List<RemoteInventorySlot> slots = new ArrayList<>();
            for (int index = 0; index < 45; index++) {
                RemoteItemStack item = index == 37 && !placed ? new RemoteItemStack(37, 1, 0)
                        : index == 38 ? new RemoteItemStack(277, 1, damaged ? 1 : 0) : null;
                slots.add(new RemoteInventorySlot(index, item));
            }
            return new RemoteInventoryView(0, slots);
        }
        @Override public void selectHeldSlot(int slot) { actions.add("select:" + slot); }
        @Override public void placeHeldBlock(BlockPosition position, BlockFace face) {
            require(position.equals(SUPPORT) && face == BlockFace.UP, "placement drifted");
            placed = true; actions.add("place");
        }
        @Override public void beginBreak(BlockPosition position) {
            require(position.equals(SUPPORT) && support, "break start drifted");
            actions.add("begin:" + token(position));
        }
        @Override public void finishBreak(BlockPosition position) {
            require(position.equals(SUPPORT) && support, "break finish drifted");
            support = false; damaged = true; actions.add("finish:" + token(position));
        }
        @Override public RemoteWorldView awaitBlock(BlockPosition position, BlockState expected) {
            BlockState actual = position.equals(SUPPORT) ? (support ? DIRT : new BlockState(0, 0))
                    : position.equals(TARGET) && placed && support ? FLOWER : new BlockState(0, 0);
            require(actual.equals(expected), "unexpected support-loss observation " + expected);
            actions.add("await:" + token(position) + ":" + expected.legacyId() + ":"
                    + expected.metadata()); return view(position, actual);
        }
        @Override public RemoteWorldView sustainTicks(int ticks) {
            actions.add("ticks:" + ticks); return view(TARGET,
                    placed && support ? FLOWER : new BlockState(0, 0));
        }
        @Override public List<RemoteDroppedItem> droppedItems() { return Collections.emptyList(); }
        @Override public void saveAndReload() { reloaded = true; actions.add("reload"); }
        @Override public ReloadBoundary reloadBoundary() {
            require(reloaded, "reload boundary requested early"); return ReloadBoundary.FRESH_LOGIN;
        }
        @Override public void close() {
        }
    }

    private static String token(BlockPosition value) {
        return value.x() + ":" + value.y() + ":" + value.z();
    }

    private static RemoteWorldView view(BlockPosition position, BlockState state) {
        RemoteChunkObservation region = new RemoteChunkObservation(0, 0, 0, 16, 128, 16, 81920);
        RemoteChunkSnapshot empty = new RemoteChunkSnapshot(region, new byte[32768],
                new byte[16384], new byte[16384], new byte[16384]);
        return new RemoteWorldView(Collections.singletonList(empty.withBlock(
                position.x(), position.y(), position.z(), state)));
    }
}
