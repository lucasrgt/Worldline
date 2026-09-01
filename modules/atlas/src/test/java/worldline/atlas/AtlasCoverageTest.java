package worldline.atlas;

import java.nio.file.Paths;
import java.util.List;

public final class AtlasCoverageTest {
    private AtlasCoverageTest() {}

    public static void main(String[] arguments) {
        AtlasStore store = AtlasStore.standard(Paths.get("."));
        require(store.kind(AtlasKind.COVERAGE_UNIT).size() == 182, "unit count");
        require("1".equals(store.get("atlas.coverage-unit.worldgen.TESTABILITY").control()),
                "worldgen testability filled by explicit smoke scope");
        require(AtlasStatus.UNKNOWN.equals(
                store.get("atlas.coverage-unit.worldgen.SEMANTIC").status()),
                "worldgen semantic unknown");
        require("1".equals(store.get("atlas.coverage-unit.tick-lifecycle.SEMANTIC").control()),
                "tick semantic filled");
        require("1".equals(store.get("atlas.coverage-unit.tick-lifecycle.CONTROL").control()),
                "tick control filled");
        require("1".equals(store.get("atlas.coverage-unit.inventory.ORACLE").control()),
                "inventory oracle filled");
        require("1".equals(store.get("atlas.coverage-unit.aero.TESTABILITY").control()),
                "aero testability filled");
        require("1".equals(store.get("atlas.coverage-unit.profiling.TESTABILITY").control()),
                "profiling testability filled");
        require("1".equals(store.get("atlas.coverage-unit.animation.TESTABILITY").control()),
                "animation testability filled");
        require("0".equals(store.get("atlas.coverage-unit.redstone.SEMANTIC").control()),
                "redstone semantic empty");
        require("1".equals(store.get("atlas.coverage-unit.mappings.SEMANTIC").control()),
                "mapping semantics filled");
        List<AtlasRecord> gaps = AtlasGaps.list(store);
        require(!gaps.isEmpty(), "gaps exist");
        boolean worldgenGap = false;
        for (AtlasRecord gap : gaps) {
            if (gap.id().contains("worldgen")) worldgenGap = true;
        }
        require(worldgenGap, "worldgen queued");
        String matrix = AtlasQuery.coverage(store);
        require(matrix.contains("0/1") && matrix.contains("1/1")
                && matrix.contains("source=declared-coverage-unit"), "coverage matrix");
        System.out.println("AtlasCoverageTest passed");
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
