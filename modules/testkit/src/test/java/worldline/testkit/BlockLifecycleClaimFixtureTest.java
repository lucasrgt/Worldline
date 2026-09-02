package worldline.testkit;
import worldline.testapi.BlockBreakDropEvidence;
import worldline.testapi.BlockBreakDropFixture;
import worldline.testapi.BlockCellTransition;
import worldline.testapi.BlockConformanceProfile;
import worldline.testapi.BlockLifecycleDropMatrix;
import worldline.testapi.BlockPlacementPersistenceEvidence;
import worldline.testapi.BlockPlacementPersistenceFixture;
import worldline.testapi.BlockStateCell;
import worldline.testapi.ConformanceLayer;

import java.util.List;
import java.util.Map;
import worldline.api.BlockLifecycleDriver.ReloadBoundary;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.RemoteItemStack;

/** Proves reusable placement/persistence and break/drop claim projections. */
public final class BlockLifecycleClaimFixtureTest {
    private static final BlockPosition LOWER = new BlockPosition(4, 65, 4);
    private static final BlockPosition UPPER = new BlockPosition(4, 66, 4);
    private static final BlockState AIR = new BlockState(0, 0);
    private BlockLifecycleClaimFixtureTest() { }

    public static void main(String[] arguments) {
        execute();
        System.out.println("BlockLifecycleClaimFixtureTest passed");
    }

    static void execute() {
        BlockConformanceProfile door = profile("064", true);
        List<BlockStateCell> expected = List.of(
                new BlockStateCell(UPPER, new BlockState(64, 8)),
                new BlockStateCell(LOWER, new BlockState(64, 0)));
        BlockPlacementPersistenceEvidence first = BlockPlacementPersistenceFixture.execute(
                door, 324, 4, 3, 1, expected, expected.reversed(), expected,
                ReloadBoundary.FRESH_LOGIN);
        BlockPlacementPersistenceEvidence second = BlockPlacementPersistenceFixture.execute(
                door, 324, 4, 3, 1, expected.reversed(), expected, expected.reversed(),
                ReloadBoundary.FRESH_LOGIN);
        require(first.equals(second) && first.canonical().contains(
                "claim.save-reload=b1.7.3:block/064#save-reload|UNIVERSAL")
                        && first.placementItemId() == 324 && first.placements() == 1,
                "placement evidence is not canonical and equatable");
        rejects(() -> BlockPlacementPersistenceFixture.execute(door, 324, 4, 2, 1,
                expected, expected, expected, ReloadBoundary.FRESH_LOGIN));
        rejects(() -> BlockPlacementPersistenceFixture.execute(door, 324, 4, 3, 1,
                expected, expected, List.of(expected.get(0)), ReloadBoundary.FRESH_LOGIN));

        List<BlockCellTransition> transitions = List.of(
                new BlockCellTransition(UPPER, new BlockState(64, 8), AIR),
                new BlockCellTransition(LOWER, new BlockState(64, 0), AIR));
        BlockLifecycleDropMatrix matrix = BlockLifecycleDropMatrix.exact(
                List.of(new RemoteItemStack(324, 1, 0)));
        BlockBreakDropEvidence broken = BlockBreakDropFixture.execute(door, 258,
                transitions, transitions.reversed(), matrix,
                List.of(new RemoteItemStack(324, 1, 0)));
        require(broken.dropLayer() == ConformanceLayer.SINGULAR
                        && broken.breakLayer() == ConformanceLayer.UNIVERSAL
                        && broken.toolItemId() == 258
                        && broken.canonical().contains(
                                "claim.break-transition=b1.7.3:block/064"
                                        + "#break-transition|UNIVERSAL"),
                "singular break/drop route drifted");
        BlockBreakDropEvidence glass = BlockBreakDropFixture.execute(profile("020", false),
                0, List.of(new BlockCellTransition(LOWER, new BlockState(20, 0), AIR)),
                List.of(new BlockCellTransition(LOWER, new BlockState(20, 0), AIR)),
                BlockLifecycleDropMatrix.exact(List.of()), List.of());
        require(glass.dropLayer() == ConformanceLayer.ARCHETYPE,
                "bare-hand archetype break/drop route drifted");
        rejects(() -> BlockBreakDropFixture.execute(door, -1, transitions, transitions,
                matrix, List.of(new RemoteItemStack(324, 1, 0))));
        rejects(() -> BlockBreakDropFixture.execute(door, 258, transitions,
                List.of(transitions.get(0)), matrix, List.of(new RemoteItemStack(324, 1, 0))));
        rejects(() -> BlockBreakDropFixture.execute(door, 258, transitions, transitions,
                matrix, List.of()));
    }

    private static BlockConformanceProfile profile(String id, boolean singular) {
        return new BlockConformanceProfile("b1.7.3:block/" + id,
                List.of("lifecycle-claim"), singular, Map.of());
    }

    private static void rejects(Runnable action) {
        try { action.run(); throw new AssertionError("invalid claim evidence was accepted"); }
        catch (IllegalArgumentException | IllegalStateException expected) { }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
