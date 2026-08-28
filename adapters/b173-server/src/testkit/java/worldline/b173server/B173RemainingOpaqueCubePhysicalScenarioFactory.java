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

/** Data-driven official rows for remaining stone-placeable opaque full cubes. */
public final class B173RemainingOpaqueCubePhysicalScenarioFactory {
    public static final long SEED = 17_320_110_707L;
    private static final int HOTBAR = 1, INVENTORY = 37;
    private static final BlockState AIR = new BlockState(0, 0);
    private static final List<String> ARCHETYPES = Collections.singletonList("simple-solid");
    private static final Cube[] CUBES = {
        new Cube(35, "white-wool"), new Cube(43, "double-slab"),
        new Cube(12, "sand"), new Cube(13, "gravel")
    };

    private B173RemainingOpaqueCubePhysicalScenarioFactory() { }

    public static List<BlockStateDomainScenario> stateDomains() {
        List<BlockStateDomainScenario> rows = new ArrayList<BlockStateDomainScenario>();
        for (Cube cube : CUBES) rows.add(stateDomain(cube));
        return Collections.unmodifiableList(rows);
    }

    public static List<BlockCollisionScenario> collisions() {
        List<BlockCollisionScenario> rows = new ArrayList<BlockCollisionScenario>();
        for (Cube cube : CUBES) rows.add(collision(cube));
        return Collections.unmodifiableList(rows);
    }

    public static List<BlockLightScenario> lights() {
        List<BlockLightScenario> rows = new ArrayList<BlockLightScenario>();
        for (Cube cube : CUBES) rows.add(light(cube));
        return Collections.unmodifiableList(rows);
    }

    private static BlockStateDomainScenario stateDomain(Cube cube) {
        List<BlockStateDomainStep> steps = new ArrayList<BlockStateDomainStep>();
        float[] yaws = {0F, 90F, 180F, -90F};
        for (int index = 0; index < yaws.length; index++) {
            steps.add(BlockStateDomainStep.place("place-yaw-" + index,
                    B173StateDomainArena.SUPPORTS.get(index), BlockFace.UP, yaws[index], 0F,
                    Collections.singletonList(new BlockStateObservation(BlockFace.UP.adjacent(
                            B173StateDomainArena.SUPPORTS.get(index)), cube.state()))));
        }
        return new BlockStateDomainScenario(cube.scenario(), claim(cube, "state-domain"),
                slot(cube, 4), Collections.singletonList(cube.state()), steps, 40);
    }

    private static BlockCollisionScenario collision(Cube cube) {
        return new BlockCollisionScenario(cube.scenario(), claim(cube, "collision-shape"),
                slot(cube, 1), 0F, 0F, Collections.singletonList(new BlockCollisionPlacement(
                        B173CollisionArena.TARGET_SUPPORT, BlockFace.UP, cube.state())),
                Arrays.asList(probe("level", 0D, BlockCollisionExpectation.BLOCKED),
                        probe("half-step", 0.5D, BlockCollisionExpectation.BLOCKED),
                        probe("full-step", 1D, BlockCollisionExpectation.PASSABLE)));
    }

    private static BlockLightScenario light(Cube cube) {
        BlockLightExpectation control = new BlockLightExpectation(AIR, 0, 15);
        BlockLightExpectation treatment = new BlockLightExpectation(cube.state(), 0, 0);
        return new BlockLightScenario(cube.scenario(), claim(cube, "light-behavior"),
                slot(cube, 1), 0F, 0F, Collections.singletonList(new BlockLightPlacement(
                        B173LightArena.SOURCE_SUPPORT, BlockFace.UP, cube.state())),
                Collections.singletonList(new BlockLightProbe("source", B173LightArena.SOURCE,
                        control, treatment)));
    }

    private static BlockCollisionProbe probe(String id, double rise,
            BlockCollisionExpectation expectation) {
        return new BlockCollisionProbe(id, 0D, rise, 1D, 10, expectation);
    }

    private static BlockLifecycleSlot slot(Cube cube, int count) {
        return new BlockLifecycleSlot(HOTBAR, INVENTORY,
                new RemoteItemStack(cube.id, count, 0), null);
    }

    private static BlockConformanceCase claim(Cube cube, String template) {
        BlockConformancePlan plan = new BlockConformancePlan(Collections.singletonList(
                new BlockConformanceProfile(cube.subject(), ARCHETYPES, false,
                        Collections.<String, ConformanceLayer>emptyMap())),
                Collections.singletonList(new BlockConformanceTemplate(
                        template, ConformanceLayer.ARCHETYPE)));
        return plan.caseFor(cube.subject(), template);
    }

    private static final class Cube {
        final int id; final String name;
        Cube(int id, String name) { this.id = id; this.name = name; }
        String subject() { return String.format("b1.7.3:block/%03d", id); }
        String scenario() { return name + "-static-physical-envelope"; }
        BlockState state() { return new BlockState(id, 0); }
    }
}
