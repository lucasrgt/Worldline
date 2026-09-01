package worldline.b173server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import worldline.api.BlockState;
import worldline.api.RemoteItemStack;
import worldline.testkit.BlockConformancePlan;
import worldline.testkit.BlockConformanceProfile;
import worldline.testkit.BlockConformanceTemplate;
import worldline.testkit.BlockLifecycleSlot;
import worldline.testkit.BlockStabilityScenario;
import worldline.testkit.ConformanceLayer;

/** Official rows for non-singular simple solids with no context-dependent tick transition. */
public final class B173InertSolidStabilityScenarioFactory {
    public static final long SEED = 17_320_110_707L;
    private static final BlockState STONE = new BlockState(1, 0);
    private static final Solid[] SOLIDS = {
        new Solid(4, "cobblestone"), new Solid(14, "gold-ore"),
        new Solid(15, "iron-ore"), new Solid(16, "coal-ore"),
        new Solid(21, "lapis-ore"), new Solid(22, "lapis-block"),
        new Solid(41, "gold-block"), new Solid(42, "iron-block"),
        new Solid(45, "bricks"), new Solid(48, "mossy-cobblestone"),
        new Solid(49, "obsidian"), new Solid(56, "diamond-ore"),
        new Solid(57, "diamond-block"), new Solid(82, "clay"),
        new Solid(87, "netherrack")
    };
    private B173InertSolidStabilityScenarioFactory() { }

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
