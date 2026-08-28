package worldline.b173server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import worldline.api.BlockFace;
import worldline.api.BlockState;
import worldline.api.RemoteItemStack;
import worldline.testkit.BlockCollisionExpectation;
import worldline.testkit.BlockCollisionPlacement;
import worldline.testkit.BlockCollisionProbe;
import worldline.testkit.BlockCollisionScenario;
import worldline.testkit.BlockConformanceCase;
import worldline.testkit.BlockConformancePlan;
import worldline.testkit.BlockConformanceProfile;
import worldline.testkit.BlockConformanceTemplate;
import worldline.testkit.BlockLifecycleSlot;
import worldline.testkit.BlockLightExpectation;
import worldline.testkit.BlockLightPlacement;
import worldline.testkit.BlockLightProbe;
import worldline.testkit.BlockLightScenario;
import worldline.testkit.BlockStateDomainScenario;
import worldline.testkit.BlockStateDomainStep;
import worldline.testkit.BlockStateObservation;
import worldline.testkit.ConformanceLayer;

/** Data-driven official rows for five metadata-zero opaque common cubes. */
public final class B173CommonCubePhysicalScenarioFactory {
    public static final long SEED = 17_320_110_707L;
    private static final int HOTBAR = 1, INVENTORY = 37;
    private static final BlockState AIR = new BlockState(0, 0);
    private static final List<String> ARCHETYPES = Collections.singletonList("simple-solid");
    private static final Solid[] SOLIDS = {
        new Solid(3, "dirt"), new Solid(5, "wood-planks"),
        new Solid(24, "sandstone"), new Solid(47, "bookshelf"),
        new Solid(58, "crafting-table")
    };

    private B173CommonCubePhysicalScenarioFactory() { }

    public static List<BlockStateDomainScenario> stateDomains() {
        List<BlockStateDomainScenario> rows = new ArrayList<BlockStateDomainScenario>();
        for (Solid solid : SOLIDS) rows.add(stateDomain(solid));
        return Collections.unmodifiableList(rows);
    }

    public static List<BlockCollisionScenario> collisions() {
        List<BlockCollisionScenario> rows = new ArrayList<BlockCollisionScenario>();
        for (Solid solid : SOLIDS) rows.add(collision(solid));
        return Collections.unmodifiableList(rows);
    }

    public static List<BlockLightScenario> lights() {
        List<BlockLightScenario> rows = new ArrayList<BlockLightScenario>();
        for (Solid solid : SOLIDS) rows.add(light(solid));
        return Collections.unmodifiableList(rows);
    }

    private static BlockStateDomainScenario stateDomain(Solid solid) {
        List<BlockStateDomainStep> steps = new ArrayList<BlockStateDomainStep>();
        float[] yaws = {0F, 90F, 180F, -90F};
        for (int index = 0; index < yaws.length; index++) {
            steps.add(BlockStateDomainStep.place("place-yaw-" + index,
                    B173StateDomainArena.SUPPORTS.get(index), BlockFace.UP, yaws[index], 0F,
                    Collections.singletonList(new BlockStateObservation(BlockFace.UP.adjacent(
                            B173StateDomainArena.SUPPORTS.get(index)), solid.state()))));
        }
        return new BlockStateDomainScenario(solid.scenario(), claim(solid, "state-domain"),
                slot(solid, 4), Collections.singletonList(solid.state()), steps, 40);
    }

    private static BlockCollisionScenario collision(Solid solid) {
        return new BlockCollisionScenario(solid.scenario(), claim(solid, "collision-shape"),
                slot(solid, 1), 0F, 0F, Collections.singletonList(new BlockCollisionPlacement(
                        B173CollisionArena.TARGET_SUPPORT, BlockFace.UP, solid.state())),
                Arrays.asList(probe("level", 0D, BlockCollisionExpectation.BLOCKED),
                        probe("half-step", 0.5D, BlockCollisionExpectation.BLOCKED),
                        probe("full-step", 1D, BlockCollisionExpectation.PASSABLE)));
    }

    private static BlockLightScenario light(Solid solid) {
        BlockLightExpectation control = new BlockLightExpectation(AIR, 0, 15);
        BlockLightExpectation treatment = new BlockLightExpectation(solid.state(), 0, 0);
        return new BlockLightScenario(solid.scenario(), claim(solid, "light-behavior"),
                slot(solid, 1), 0F, 0F, Collections.singletonList(new BlockLightPlacement(
                        B173LightArena.SOURCE_SUPPORT, BlockFace.UP, solid.state())),
                Collections.singletonList(new BlockLightProbe("source", B173LightArena.SOURCE,
                        control, treatment)));
    }

    private static BlockCollisionProbe probe(String id, double rise,
            BlockCollisionExpectation expectation) {
        return new BlockCollisionProbe(id, 0D, rise, 1D, 10, expectation);
    }

    private static BlockLifecycleSlot slot(Solid solid, int count) {
        return new BlockLifecycleSlot(HOTBAR, INVENTORY,
                new RemoteItemStack(solid.id, count, 0), null);
    }

    private static BlockConformanceCase claim(Solid solid, String template) {
        BlockConformancePlan plan = new BlockConformancePlan(Collections.singletonList(
                new BlockConformanceProfile(solid.subject(), ARCHETYPES, false,
                        Collections.<String, ConformanceLayer>emptyMap())),
                Collections.singletonList(new BlockConformanceTemplate(
                        template, ConformanceLayer.ARCHETYPE)));
        return plan.caseFor(solid.subject(), template);
    }

    private static final class Solid {
        final int id; final String name;
        Solid(int id, String name) { this.id = id; this.name = name; }
        String subject() { return String.format("b1.7.3:block/%03d", id); }
        String scenario() { return name + "-static-physical-envelope"; }
        BlockState state() { return new BlockState(id, 0); }
    }
}
