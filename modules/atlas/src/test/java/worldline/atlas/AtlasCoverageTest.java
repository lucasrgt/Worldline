package worldline.atlas;

import java.nio.file.Paths;
import java.util.List;

public final class AtlasCoverageTest {
    private AtlasCoverageTest() {}

    public static void main(String[] arguments) {
        AtlasStore store = AtlasStore.standard(Paths.get("."));
        require(store.kind(AtlasKind.COVERAGE_UNIT).size() == 175, "unit count");
        require("1".equals(store.get("atlas.coverage-unit.worldgen.TESTABILITY").control()),
                "worldgen testability filled by explicit smoke scope");
        require(AtlasStatus.UNKNOWN.equals(
                store.get("atlas.coverage-unit.worldgen.SEMANTIC").status()),
                "worldgen semantic unknown");
        require("1".equals(store.get("atlas.coverage-unit.tick-lifecycle.SEMANTIC").control()),
                "tick semantic filled");
        require("1".equals(store.get("atlas.coverage-unit.tick-lifecycle.CONTROL").control()),
                "tick control filled");
        require("1".equals(store.get("atlas.coverage-unit.block-ticks.SEMANTIC").control()),
                "block tick semantic filled");
        require("1".equals(store.get("atlas.coverage-unit.block-ticks.CONTROL").control()),
                "block tick TestKit boundary controlled");
        require("1".equals(store.get("atlas.coverage-unit.block-ticks.DETERMINISM").control()),
                "block tick evidence normalization deterministic");
        AtlasRecord blockTick = store.get("atlas.boundary.BLOCK_TICK");
        require("CONTROLLED".equals(blockTick.control())
                && blockTick.refs().contains("atlas.subsystem.block-ticks"),
                "block tick boundary classification");
        require("1".equals(store.get("atlas.coverage-unit.fluids.SEMANTIC").control()),
                "fluid TestKit semantics filled");
        require("1".equals(store.get("atlas.coverage-unit.fluids.CONTROL").control()),
                "fluid TestKit boundary controlled");
        require("1".equals(store.get("atlas.coverage-unit.fluids.DETERMINISM").control()),
                "fluid public evidence deterministic");
        AtlasRecord fluid = store.get("atlas.boundary.FLUID");
        require("CONTROLLED".equals(fluid.control())
                && fluid.refs().contains("atlas.subsystem.fluids"),
                "fluid boundary classification");
        require("1".equals(store.get("atlas.coverage-unit.lighting.SEMANTIC").control()),
                "lighting TestKit semantics filled");
        require("1".equals(store.get("atlas.coverage-unit.lighting.CONTROL").control()),
                "lighting TestKit boundary controlled");
        require("1".equals(store.get("atlas.coverage-unit.lighting.DETERMINISM").control()),
                "lighting public evidence deterministic");
        AtlasRecord light = store.get("atlas.boundary.LIGHT");
        require("CONTROLLED".equals(light.control())
                && light.refs().contains("atlas.subsystem.lighting"),
                "lighting boundary classification");
        require("1".equals(store.get("atlas.coverage-unit.inventory.ORACLE").control()),
                "inventory oracle filled");
        require("1".equals(store.get("atlas.coverage-unit.crafting.CONTROL").control()),
                "crafting TestKit boundary controlled");
        require("1".equals(store.get("atlas.coverage-unit.crafting.DETERMINISM").control()),
                "crafting public evidence deterministic");
        AtlasRecord crafting = store.get("atlas.boundary.RECIPE");
        require("CONTROLLED".equals(crafting.control())
                && crafting.refs().contains("atlas.subsystem.crafting"),
                "crafting boundary classification");
        require("1".equals(store.get("atlas.coverage-unit.tile-entities.SEMANTIC").control()),
                "tile entity TestKit semantics filled");
        require("1".equals(store.get("atlas.coverage-unit.tile-entities.CONTROL").control()),
                "tile entity TestKit boundary controlled");
        require("1".equals(store.get("atlas.coverage-unit.tile-entities.DETERMINISM").control()),
                "tile entity public evidence deterministic");
        AtlasRecord tileEntity = store.get("atlas.boundary.TILE_ENTITY");
        require("CONTROLLED".equals(tileEntity.control())
                && tileEntity.refs().contains("atlas.subsystem.tile-entities"),
                "tile entity boundary classification");
        require("1".equals(store.get("atlas.coverage-unit.aero.TESTABILITY").control()),
                "aero testability filled");
        require("1".equals(store.get("atlas.coverage-unit.redstone.SEMANTIC").control()),
                "redstone public TestKit semantics filled");
        require("1".equals(store.get("atlas.coverage-unit.redstone.CONTROL").control()),
                "redstone TestKit boundary controlled");
        require("1".equals(store.get("atlas.coverage-unit.redstone.DETERMINISM").control()),
                "redstone evidence normalization deterministic");
        AtlasRecord redstone = store.get("atlas.boundary.REDSTONE");
        require("CONTROLLED".equals(redstone.control())
                && redstone.refs().contains("atlas.subsystem.redstone"),
                "redstone boundary classification");
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
