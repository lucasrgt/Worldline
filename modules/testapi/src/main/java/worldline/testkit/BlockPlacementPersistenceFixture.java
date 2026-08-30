package worldline.testkit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import worldline.api.BlockLifecycleDriver.ReloadBoundary;

/** Validates simple, oriented, or multiblock placement across a reload boundary. */
public final class BlockPlacementPersistenceFixture {
    private static final Comparator<BlockStateCell> ORDER = Comparator
            .comparingInt((BlockStateCell cell) -> cell.position().x())
            .thenComparingInt(cell -> cell.position().y())
            .thenComparingInt(cell -> cell.position().z());

    private BlockPlacementPersistenceFixture() { }

    public static BlockPlacementPersistenceEvidence execute(String subject,
            String archetype, boolean singular, int placementItemId,
            int itemCountBefore, int itemCountAfter, int placements,
            List<BlockStateCell> expected, List<BlockStateCell> live,
            List<BlockStateCell> reloaded, ReloadBoundary boundary) {
        return execute(new BlockConformanceProfile(subject,
                Collections.singletonList(archetype), singular, Collections.emptyMap()),
                placementItemId,
                itemCountBefore, itemCountAfter, placements,
                expected, live, reloaded, boundary);
    }

    public static BlockPlacementPersistenceEvidence execute(BlockConformanceProfile profile,
            int placementItemId, int itemCountBefore, int itemCountAfter, int placements,
            List<BlockStateCell> expected, List<BlockStateCell> live,
            List<BlockStateCell> reloaded, ReloadBoundary boundary) {
        Objects.requireNonNull(profile, "profile");
        if (placementItemId < 1 || itemCountBefore < 1 || itemCountAfter < 0
                || placements < 1 || itemCountBefore - itemCountAfter != placements) {
            throw new IllegalArgumentException("invalid placement inventory effect");
        }
        List<BlockStateCell> canonical = cells(expected, "expected");
        require(canonical.equals(cells(live, "live")), "live placement cells drifted");
        require(canonical.equals(cells(reloaded, "reloaded")), "reloaded cells drifted");
        if (boundary == null) throw new NullPointerException("reload boundary");
        return new BlockPlacementPersistenceEvidence(profile, placementItemId,
                itemCountBefore, itemCountAfter, placements, canonical, boundary);
    }

    private static List<BlockStateCell> cells(List<BlockStateCell> source, String label) {
        if (source == null || source.isEmpty())
            throw new IllegalArgumentException(label + " cells are empty");
        List<BlockStateCell> result = new ArrayList<BlockStateCell>(source);
        Set<worldline.api.BlockPosition> positions = new HashSet<worldline.api.BlockPosition>();
        for (BlockStateCell cell : result) if (cell == null || !positions.add(cell.position()))
            throw new IllegalArgumentException(label + " cells contain a duplicate");
        result.sort(ORDER);
        return Collections.unmodifiableList(result);
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
