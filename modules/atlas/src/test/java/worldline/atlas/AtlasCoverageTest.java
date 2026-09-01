package worldline.atlas;

import java.nio.file.Paths;
import java.util.List;

public final class AtlasCoverageTest {
    private AtlasCoverageTest() {}

    public static void main(String[] arguments) {
        AtlasStore store = AtlasStore.standard(Paths.get("."));
        require(store.kind(AtlasKind.COVERAGE_UNIT).size() == 189, "unit count");
        require("1".equals(store.get("atlas.coverage-unit.worldgen.TESTABILITY").control()),
                "worldgen testability filled by explicit smoke scope");
        require("1".equals(store.get("atlas.coverage-unit.worldgen.SEMANTIC").control()),
                "worldgen TestKit semantics filled");
        require("1".equals(store.get("atlas.coverage-unit.worldgen.CONTROL").control()),
                "worldgen public boundary controlled");
        require("1".equals(store.get("atlas.coverage-unit.worldgen.DETERMINISM").control()),
                "worldgen normalized evidence deterministic");
        AtlasRecord worldgen = store.get("atlas.boundary.WORLDGEN");
        require("CONTROLLED".equals(worldgen.control())
                && worldgen.refs().contains("atlas.subsystem.worldgen"),
                "worldgen boundary classification");
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
        require("1".equals(store.get("atlas.coverage-unit.weather.SEMANTIC").control()),
                "weather TestKit semantics filled");
        require("1".equals(store.get("atlas.coverage-unit.weather.CONTROL").control()),
                "weather TestKit boundary controlled");
        require("1".equals(store.get("atlas.coverage-unit.weather.DETERMINISM").control()),
                "weather public evidence deterministic");
        AtlasRecord weather = store.get("atlas.boundary.WEATHER");
        require("CONTROLLED".equals(weather.control())
                && weather.refs().contains("atlas.subsystem.weather"),
                "weather boundary classification");
        require("1".equals(store.get("atlas.coverage-unit.mob-ai.SEMANTIC").control()),
                "mob AI TestKit semantics filled");
        require("1".equals(store.get("atlas.coverage-unit.mob-ai.CONTROL").control()),
                "mob AI TestKit boundary controlled");
        require("1".equals(store.get("atlas.coverage-unit.mob-ai.DETERMINISM").control()),
                "mob AI public evidence deterministic");
        AtlasRecord mobAi = store.get("atlas.boundary.MOB_AI");
        require("CONTROLLED".equals(mobAi.control())
                && mobAi.refs().contains("atlas.subsystem.mob-ai"),
                "mob AI boundary classification");
        require("1".equals(store.get("atlas.coverage-unit.dimensions.SEMANTIC").control()),
                "dimension semantics filled");
        require("1".equals(store.get("atlas.coverage-unit.dimensions.CONTROL").control()),
                "dimension public boundary controlled");
        require("1".equals(store.get("atlas.coverage-unit.dimensions.DETERMINISM").control()),
                "dimension public evidence deterministic");
        AtlasRecord dimension = store.get("atlas.boundary.DIMENSION");
        require("CONTROLLED".equals(dimension.control())
                && dimension.refs().contains("atlas.subsystem.dimensions"),
                "dimension boundary classification");
        require("1".equals(store.get("atlas.coverage-unit.dedicated-server.SEMANTIC").control()),
                "dedicated server semantics filled");
        require("1".equals(store.get("atlas.coverage-unit.dedicated-server.CONTROL").control()),
                "dedicated server public boundary controlled");
        require("1".equals(store.get(
                "atlas.coverage-unit.dedicated-server.DETERMINISM").control()),
                "dedicated server normalized evidence deterministic");
        AtlasRecord dedicatedServer = store.get("atlas.boundary.DEDICATED_SERVER");
        require("CONTROLLED".equals(dedicatedServer.control())
                && dedicatedServer.refs().contains("atlas.subsystem.dedicated-server"),
                "dedicated server boundary classification");
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
        require("1".equals(store.get("atlas.coverage-unit.profiling.TESTABILITY").control()),
                "profiling testability filled");
        require("1".equals(store.get("atlas.coverage-unit.animation.TESTABILITY").control()),
                "animation testability filled");
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
        require("1".equals(store.get("atlas.coverage-unit.mappings.CONTROL").control()),
                "mapping audit boundary controlled");
        require("1".equals(store.get("atlas.coverage-unit.mappings.DETERMINISM").control()),
                "mapping evidence derivation deterministic");
        AtlasRecord mappings = store.get("atlas.boundary.MAPPINGS");
        require("CONTROLLED".equals(mappings.control())
                && mappings.refs().contains("atlas.subsystem.mappings"),
                "mapping boundary classification");
        require("1".equals(store.get("atlas.coverage-unit.stationapi.SEMANTIC").control()),
                "StationAPI driver semantics filled");
        require("1".equals(store.get("atlas.coverage-unit.stationapi.CONTROL").control()),
                "StationAPI driver boundary controlled");
        require("1".equals(store.get("atlas.coverage-unit.stationapi.DETERMINISM").control()),
                "StationAPI gated ticks deterministic");
        AtlasRecord stationapi = store.get("atlas.boundary.STATIONAPI");
        require("INTERCEPTED".equals(stationapi.control())
                && stationapi.refs().contains("atlas.subsystem.stationapi"),
                "StationAPI boundary classification");
        require("1".equals(store.get("atlas.coverage-unit.aero.SEMANTIC").control()),
                "Aero overlay semantics filled");
        require("1".equals(store.get("atlas.coverage-unit.aero.CONTROL").control()),
                "Aero overlay boundary controlled");
        require("1".equals(store.get("atlas.coverage-unit.aero.DETERMINISM").control()),
                "Aero overlay evidence deterministic");
        AtlasRecord aero = store.get("atlas.boundary.AERO");
        require("INTERCEPTED".equals(aero.control())
                && aero.refs().contains("atlas.subsystem.aero"),
                "Aero boundary classification");
        require("1".equals(store.get(
                "atlas.coverage-unit.mod-ecosystem.SEMANTIC").control()),
                "mod ecosystem semantics filled");
        require("1".equals(store.get(
                "atlas.coverage-unit.mod-ecosystem.CONTROL").control()),
                "mod ecosystem boundary controlled");
        require("1".equals(store.get(
                "atlas.coverage-unit.mod-ecosystem.DETERMINISM").control()),
                "mod ecosystem evidence deterministic");
        AtlasRecord modEcosystem = store.get("atlas.boundary.MOD_ECOSYSTEM");
        require("CONTROLLED".equals(modEcosystem.control())
                && modEcosystem.refs().contains("atlas.subsystem.mod-ecosystem"),
                "mod ecosystem boundary classification");
        List<AtlasRecord> gaps = AtlasGaps.list(store);
        require(!gaps.isEmpty(), "gaps exist");
        boolean coverageGap = false;
        for (AtlasRecord gap : gaps) {
            if (gap.id().startsWith("atlas.coverage-unit.")) coverageGap = true;
        }
        require(!coverageGap, "declared coverage matrix is incomplete");
        String matrix = AtlasQuery.coverage(store);
        require(!matrix.contains("0/1") && matrix.contains("1/1")
                && matrix.contains("source=declared-coverage-unit"), "coverage matrix");
        System.out.println("AtlasCoverageTest passed");
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
