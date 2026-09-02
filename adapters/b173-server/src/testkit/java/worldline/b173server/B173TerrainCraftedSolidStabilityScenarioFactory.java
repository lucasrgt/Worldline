package worldline.b173server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import worldline.api.BlockState;
import worldline.api.RemoteItemStack;
import worldline.testapi.BlockConformancePlan;
import worldline.testapi.BlockConformanceProfile;
import worldline.testapi.BlockConformanceTemplate;
import worldline.testapi.BlockLifecycleSlot;
import worldline.testapi.BlockStabilityScenario;
import worldline.testapi.ConformanceLayer;

/** Official rows for remaining inert solids outside the occupied mineral stability cycle. */
public final class B173TerrainCraftedSolidStabilityScenarioFactory {
    public static final long SEED = 17_320_110_707L;
    private static final BlockState STONE = new BlockState(1, 0);
    private static final Solid[] SOLIDS = {
        new Solid(1, "stone"), new Solid(3, "dirt"),
        new Solid(5, "wood-planks"), new Solid(24, "sandstone"),
        new Solid(35, "white-wool"), new Solid(47, "bookshelf"),
        new Solid(58, "crafting-table"), new Solid(80, "snow-block")
    };
    private B173TerrainCraftedSolidStabilityScenarioFactory() { }

    public static List<BlockStabilityScenario> rows() {
        List<BlockStabilityScenario> rows = new ArrayList<BlockStabilityScenario>();
        for (Solid solid : SOLIDS) rows.add(row(solid));
        return Collections.unmodifiableList(rows);
    }

    private static BlockStabilityScenario row(Solid solid) {
        BlockConformancePlan plan = new BlockConformancePlan(Collections.singletonList(
                new BlockConformanceProfile(solid.subject(),
                        Collections.singletonList("simple-solid"), false,
                        Collections.<String, ConformanceLayer>emptyMap())), Arrays.asList(
                new BlockConformanceTemplate("tick-policy", ConformanceLayer.ARCHETYPE),
                new BlockConformanceTemplate("neighbor-response", ConformanceLayer.ARCHETYPE)));
        return new BlockStabilityScenario(solid.name + "-bounded-stability-envelope",
                plan.caseFor(solid.subject(), "tick-policy"),
                plan.caseFor(solid.subject(), "neighbor-response"),
                B173LifecycleArena.SUPPORT, STONE, new BlockState(solid.id, 0), STONE,
                new BlockLifecycleSlot(1, 37, new RemoteItemStack(solid.id, 1, 0), null),
                new BlockLifecycleSlot(2, 38, new RemoteItemStack(278, 1, 0),
                        new RemoteItemStack(278, 1, 1)), 200, 20, 40);
    }

    private static final class Solid {
        final int id; final String name;
        Solid(int id, String name) { this.id = id; this.name = name; }
        String subject() { return String.format("b1.7.3:block/%03d", id); }
    }
}
