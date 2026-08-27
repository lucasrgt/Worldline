package worldline.testkit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

/** Proves exact lifecycle action order, drop normalization, and three-layer routing. */
public final class BlockLifecycleFixtureTest {
    private static final RemoteItemStack BLOCK = new RemoteItemStack(4, 1, 0);
    private static final RemoteItemStack TOOL = new RemoteItemStack(257, 1, 0);
    private static final RemoteItemStack WORN_TOOL = new RemoteItemStack(257, 1, 1);

    private BlockLifecycleFixtureTest() {
    }

    static void execute() {
        List<BlockConformanceCase> cases = cases();
        require(claim(cases, "b1.7.3:block/004", "drop-matrix").layer()
                == ConformanceLayer.ARCHETYPE, "archetype lifecycle route drifted");
        require(claim(cases, "b1.7.3:block/054", "drop-matrix").layer()
                == ConformanceLayer.SINGULAR, "singular lifecycle route drifted");

        BlockLifecycleScenario cobble = scenario(cases, List.of(BLOCK));
        FakeDriver first = new FakeDriver(List.of(BLOCK), 7);
        FakeDriver second = new FakeDriver(List.of(BLOCK), 70);
        BlockLifecycleEvidence evidence = BlockLifecycleFixture.execute(cobble, first);
        require(evidence.equals(BlockLifecycleFixture.execute(cobble, second)),
                "transient dropped-item identity leaked into evidence");
        require(evidence.placementLayer() == ConformanceLayer.UNIVERSAL
                && evidence.dropLayer() == ConformanceLayer.ARCHETYPE
                && evidence.boundary() == BlockLifecycleDriver.ReloadBoundary.FRESH_LOGIN,
                "lifecycle evidence route drifted");
        require(first.actions.equals(List.of("await:0", "inventory", "select:0", "place",
                "await:4", "inventory", "reload", "await:4", "inventory", "select:1",
                "drops", "begin", "ticks:2", "finish", "await:0", "ticks:3", "drops",
                "inventory", "reload", "await:0")), "lifecycle action order drifted");

        BlockLifecycleScenario noDrop = scenario(cases, List.of());
        require(BlockLifecycleFixture.execute(noDrop, new FakeDriver(List.of(), 90))
                .drops().isEmpty(), "zero-drop lifecycle was rejected");
        List<RemoteItemStack> mixed = List.of(new RemoteItemStack(3, 1, 0), BLOCK);
        require(BlockLifecycleFixture.execute(scenario(cases, mixed),
                new FakeDriver(List.of(BLOCK, new RemoteItemStack(3, 1, 0)), 100))
                .drops().equals(mixed), "multi-drop normalization drifted");
        rejects(() -> new BlockLifecycleScenario(
                claim(cases, "b1.7.3:block/004", "gameplay-placement"),
                claim(cases, "b1.7.3:block/054", "save-reload"),
                claim(cases, "b1.7.3:block/004", "break-transition"),
                claim(cases, "b1.7.3:block/004", "drop-matrix"),
                new BlockPosition(4, 64, 4), BlockFace.UP, new BlockState(4, 0),
                new BlockLifecycleSlot(0, 36, BLOCK, null),
                new BlockLifecycleSlot(1, 37, TOOL, WORN_TOOL), List.of(BLOCK), 2, 3));
    }

    private static BlockLifecycleScenario scenario(List<BlockConformanceCase> cases,
            List<RemoteItemStack> drops) {
        String subject = "b1.7.3:block/004";
        return new BlockLifecycleScenario(claim(cases, subject, "gameplay-placement"),
                claim(cases, subject, "save-reload"),
                claim(cases, subject, "break-transition"),
                claim(cases, subject, "drop-matrix"), new BlockPosition(4, 64, 4),
                BlockFace.UP, new BlockState(4, 0),
                new BlockLifecycleSlot(0, 36, BLOCK, null),
                new BlockLifecycleSlot(1, 37, TOOL, WORN_TOOL), drops, 2, 3);
    }

    private static List<BlockConformanceCase> cases() {
        BlockConformanceProfile cobble = new BlockConformanceProfile(
                "b1.7.3:block/004", List.of("simple-solid"), false, Map.of());
        BlockConformanceProfile chest = new BlockConformanceProfile(
                "b1.7.3:block/054", List.of("container", "tile-entity"), true, Map.of());
        List<BlockConformanceTemplate> templates = List.of(
                new BlockConformanceTemplate("gameplay-placement", ConformanceLayer.UNIVERSAL),
                new BlockConformanceTemplate("save-reload", ConformanceLayer.UNIVERSAL),
                new BlockConformanceTemplate("break-transition", ConformanceLayer.UNIVERSAL),
                new BlockConformanceTemplate("drop-matrix", ConformanceLayer.ARCHETYPE));
        return new BlockConformancePlan(List.of(cobble, chest), templates).cases();
    }

    private static BlockConformanceCase claim(List<BlockConformanceCase> cases,
            String subject, String template) {
        for (BlockConformanceCase value : cases) {
            if (value.profile().subject().equals(subject)
                    && value.template().id().equals(template)) return value;
        }
        throw new AssertionError("missing lifecycle claim");
    }

    private static void rejects(Runnable action) {
        try { action.run(); throw new AssertionError("invalid lifecycle scenario was accepted"); }
        catch (IllegalArgumentException expected) { }
    }

    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }

    private static final class FakeDriver implements BlockLifecycleDriver {
        final List<String> actions = new ArrayList<>();
        final List<RemoteItemStack> expectedDrops;
        final int firstEntity;
        BlockState state = new BlockState(0, 0);
        boolean placementConsumed, toolWorn, finished, dropsAdded;
        int selected = -1, reloads;

        FakeDriver(List<RemoteItemStack> expectedDrops, int firstEntity) {
            this.expectedDrops = expectedDrops;
            this.firstEntity = firstEntity;
        }

        @Override public RemoteInventoryView inventory() {
            actions.add("inventory");
            List<RemoteInventorySlot> slots = new ArrayList<>();
            for (int index = 0; index < 45; index++) {
                RemoteItemStack item = index == 36 && !placementConsumed ? BLOCK
                        : index == 37 ? toolWorn ? WORN_TOOL : TOOL : null;
                slots.add(new RemoteInventorySlot(index, item));
            }
            return new RemoteInventoryView(0, slots);
        }
        @Override public void selectHeldSlot(int slot) { selected = slot; actions.add("select:" + slot); }
        @Override public void placeHeldBlock(BlockPosition support, BlockFace face) {
            require(selected == 0 && state.legacyId() == 0, "fake placement precondition");
            state = new BlockState(4, 0); placementConsumed = true; actions.add("place");
        }
        @Override public void beginBreak(BlockPosition position) {
            require(selected == 1 && state.legacyId() == 4, "fake break precondition");
            actions.add("begin");
        }
        @Override public void finishBreak(BlockPosition position) {
            state = new BlockState(0, 0); toolWorn = true; finished = true; actions.add("finish");
        }
        @Override public RemoteWorldView awaitBlock(BlockPosition position, BlockState expected) {
            actions.add("await:" + expected.legacyId());
            require(state.equals(expected), "fake block expectation");
            return view(position, state);
        }
        @Override public RemoteWorldView sustainTicks(int ticks) {
            actions.add("ticks:" + ticks);
            if (finished) dropsAdded = true;
            return view(new BlockPosition(4, 65, 4), state);
        }
        @Override public List<RemoteDroppedItem> droppedItems() {
            actions.add("drops");
            List<RemoteDroppedItem> result = new ArrayList<>();
            if (dropsAdded) for (int index = 0; index < expectedDrops.size(); index++) {
                result.add(new RemoteDroppedItem(firstEntity + index, expectedDrops.get(index),
                        4.5D, 65D, 4.5D, 0D, 0D, 0D));
            }
            return result;
        }
        @Override public void saveAndReload() { reloads++; actions.add("reload"); }
        @Override public ReloadBoundary reloadBoundary() {
            require(reloads > 0, "fake reload absent"); return ReloadBoundary.FRESH_LOGIN;
        }
        @Override public void close() { }
    }

    private static RemoteWorldView view(BlockPosition position, BlockState state) {
        RemoteChunkObservation region = new RemoteChunkObservation(0, 0, 0, 16, 128, 16, 81920);
        RemoteChunkSnapshot empty = new RemoteChunkSnapshot(region, new byte[32768],
                new byte[16384], new byte[16384], new byte[16384]);
        return new RemoteWorldView(List.of(empty.withBlock(
                position.x(), position.y(), position.z(), state)));
    }
}
