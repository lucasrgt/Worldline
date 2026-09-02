package worldline.testkit;
import worldline.testapi.BlockConformanceCase;
import worldline.testapi.BlockConformancePlan;
import worldline.testapi.BlockConformanceProfile;
import worldline.testapi.BlockConformanceTemplate;
import worldline.testapi.BlockLifecycleEvidence;
import worldline.testapi.BlockLifecycleFixture;
import worldline.testapi.BlockLifecycleScenario;
import worldline.testapi.BlockLifecycleSlot;
import worldline.testapi.ConformanceLayer;

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

/** Proves ground-cover lifecycle action order and canonical crop evidence. */
public final class GroundCoverLifecycleFixtureTest {
    private static final RemoteItemStack SEEDS = new RemoteItemStack(295, 1, 0);
    private static final RemoteItemStack STICK = new RemoteItemStack(280, 1, 0);
    private static final BlockPosition SUPPORT = new BlockPosition(4, 64, 4);
    private static final BlockState FARMLAND = new BlockState(60, 0);
    private static final BlockState CROP = new BlockState(59, 0);

    private GroundCoverLifecycleFixtureTest() {
    }

    public static void main(String[] arguments) throws Exception {
        execute();
        System.out.println("GroundCoverLifecycleFixtureTest passed");
    }

    static void execute() throws Exception {
        BlockLifecycleScenario row = crops();
        FakeDriver first = new FakeDriver(7);
        FakeDriver second = new FakeDriver(70);
        BlockLifecycleEvidence evidence = BlockLifecycleFixture.execute(row, first);
        require(evidence.equals(BlockLifecycleFixture.execute(row, second)),
                "transient dropped-item identity leaked into ground-cover evidence");
        require(evidence.placementLayer() == ConformanceLayer.UNIVERSAL
                && evidence.dropLayer() == ConformanceLayer.ARCHETYPE
                && evidence.boundary() == BlockLifecycleDriver.ReloadBoundary.FRESH_LOGIN,
                "ground-cover lifecycle evidence route drifted");
        require(evidence.canonical().equals("schema=worldline.block-lifecycle-evidence.v1\n"
                + "scenario=crops\nsubject=b1.7.3:block/059\n"
                + "claim.gameplay-placement=b1.7.3:block/059#gameplay-placement|UNIVERSAL\n"
                + "claim.save-reload=b1.7.3:block/059#save-reload|UNIVERSAL\n"
                + "claim.break-transition=b1.7.3:block/059#break-transition|UNIVERSAL\n"
                + "claim.drop-matrix=b1.7.3:block/059#drop-matrix|ARCHETYPE\n"
                + "support=4:64:4:60:0\ntarget=4:65:4\nplaced=59:0\n"
                + "drops=295:1:0\nreload=FRESH_LOGIN\n"),
                "canonical ground-cover evidence drifted");
        require(first.actions.equals(List.of("await-support:60", "await:0", "inventory", "select:0",
                "place", "await:59", "inventory", "reload", "await:59", "inventory", "select:1",
                "drops", "begin", "ticks:1", "finish", "await:0", "ticks:3", "drops",
                "inventory", "reload", "await:0")), "ground-cover lifecycle action order drifted");
    }

    private static BlockLifecycleScenario crops() {
        String subject = "b1.7.3:block/059";
        BlockConformanceProfile profile = new BlockConformanceProfile(subject,
                List.of("ground-cover"), false, Map.of());
        List<BlockConformanceTemplate> templates = List.of(
                new BlockConformanceTemplate("gameplay-placement", ConformanceLayer.UNIVERSAL),
                new BlockConformanceTemplate("save-reload", ConformanceLayer.UNIVERSAL),
                new BlockConformanceTemplate("break-transition", ConformanceLayer.UNIVERSAL),
                new BlockConformanceTemplate("drop-matrix", ConformanceLayer.ARCHETYPE));
        List<BlockConformanceCase> cases = new BlockConformancePlan(List.of(profile), templates)
                .cases();
        return new BlockLifecycleScenario("crops", claim(cases, "gameplay-placement"),
                claim(cases, "save-reload"), claim(cases, "break-transition"),
                claim(cases, "drop-matrix"), SUPPORT, FARMLAND, BlockFace.UP, CROP,
                new BlockLifecycleSlot(0, 36, SEEDS, null),
                new BlockLifecycleSlot(1, 37, STICK, STICK), List.of(SEEDS), 1, 3);
    }

    private static BlockConformanceCase claim(List<BlockConformanceCase> cases, String template) {
        for (BlockConformanceCase value : cases) {
            if (value.template().id().equals(template)) return value;
        }
        throw new AssertionError("missing ground-cover claim");
    }

    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }

    private static final class FakeDriver implements BlockLifecycleDriver {
        final List<String> actions = new ArrayList<>();
        final int firstEntity;
        BlockState state = new BlockState(0, 0);
        boolean placementConsumed, finished, dropsAdded;
        int selected = -1, reloads;

        FakeDriver(int firstEntity) {
            this.firstEntity = firstEntity;
        }

        @Override public RemoteInventoryView inventory() {
            actions.add("inventory");
            List<RemoteInventorySlot> slots = new ArrayList<>();
            for (int index = 0; index < 45; index++) {
                RemoteItemStack item = index == 36 && !placementConsumed ? SEEDS
                        : index == 37 ? STICK : null;
                slots.add(new RemoteInventorySlot(index, item));
            }
            return new RemoteInventoryView(0, slots);
        }

        @Override public void selectHeldSlot(int slot) {
            selected = slot;
            actions.add("select:" + slot);
        }

        @Override public void placeHeldBlock(BlockPosition support, BlockFace face) {
            require(selected == 0 && state.legacyId() == 0, "fake crop placement precondition");
            state = CROP;
            placementConsumed = true;
            actions.add("place");
        }

        @Override public void beginBreak(BlockPosition position) {
            require(selected == 1 && state.legacyId() == 59, "fake crop break precondition");
            actions.add("begin");
        }

        @Override public void finishBreak(BlockPosition position) {
            state = new BlockState(0, 0);
            finished = true;
            actions.add("finish");
        }

        @Override public RemoteWorldView awaitBlock(BlockPosition position, BlockState expected) {
            if (position.equals(SUPPORT)) {
                actions.add("await-support:" + expected.legacyId());
                require(expected.equals(FARMLAND), "fake farmland expectation");
                return view(position, FARMLAND);
            }
            actions.add("await:" + expected.legacyId());
            require(state.equals(expected), "fake crop block expectation");
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
            if (dropsAdded) result.add(new RemoteDroppedItem(firstEntity, SEEDS,
                    4.5D, 65D, 4.5D, 0D, 0D, 0D));
            return result;
        }

        @Override public void saveAndReload() {
            reloads++;
            actions.add("reload");
        }

        @Override public ReloadBoundary reloadBoundary() {
            require(reloads > 0, "fake reload absent");
            return ReloadBoundary.FRESH_LOGIN;
        }

        @Override public void close() {
        }
    }

    private static RemoteWorldView view(BlockPosition position, BlockState state) {
        RemoteChunkObservation region = new RemoteChunkObservation(0, 0, 0, 16, 128, 16, 81920);
        RemoteChunkSnapshot empty = new RemoteChunkSnapshot(region, new byte[32768],
                new byte[16384], new byte[16384], new byte[16384]);
        return new RemoteWorldView(List.of(empty.withBlock(
                position.x(), position.y(), position.z(), state)));
    }
}
