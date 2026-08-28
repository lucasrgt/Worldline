package worldline.b173server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import worldline.api.BlockFace;
import worldline.api.BlockPosition;
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

/** Public archetype rows for the still-water and still-lava physical envelopes. */
public final class B173SourceFluidPhysicalScenarioFactory {
    public static final long SEED = 17_320_110_811L;
    private static final int HOTBAR = 1, INVENTORY = 37;
    private static final BlockState AIR = new BlockState(0, 0);
    private static final List<Subject> SUBJECTS = Collections.unmodifiableList(Arrays.asList(
            new Subject(9, "still-water", 0, 12, "fluid", "tick-driven"),
            new Subject(11, "still-lava", 15, 12, "fluid", "luminous", "tick-driven")));

    private B173SourceFluidPhysicalScenarioFactory() { }

    public static List<BlockStateDomainScenario> stateDomains() {
        List<BlockStateDomainScenario> rows = new ArrayList<BlockStateDomainScenario>();
        for (Subject subject : SUBJECTS) rows.add(stateDomain(subject));
        return Collections.unmodifiableList(rows);
    }

    public static List<BlockCollisionScenario> collisions() {
        List<BlockCollisionScenario> rows = new ArrayList<BlockCollisionScenario>();
        for (Subject subject : SUBJECTS) rows.add(collision(subject));
        return Collections.unmodifiableList(rows);
    }

    public static List<BlockLightScenario> lights() {
        List<BlockLightScenario> rows = new ArrayList<BlockLightScenario>();
        for (Subject subject : SUBJECTS) rows.add(light(subject));
        return Collections.unmodifiableList(rows);
    }

    private static BlockStateDomainScenario stateDomain(Subject subject) {
        List<BlockStateDomainStep> steps = new ArrayList<BlockStateDomainStep>();
        for (int index = 0; index < B173StateDomainArena.SUPPORTS.size(); index++) {
            BlockPosition support = B173StateDomainArena.SUPPORTS.get(index);
            steps.add(BlockStateDomainStep.place("place-source-" + index, support, BlockFace.UP,
                    index * 90F, 0F, Collections.singletonList(new BlockStateObservation(
                            BlockFace.UP.adjacent(support), subject.state()))));
        }
        return new BlockStateDomainScenario(subject.scenario(), claim(subject, "state-domain"),
                slot(subject, 4), Collections.singletonList(subject.state()), steps, 40);
    }

    private static BlockCollisionScenario collision(Subject subject) {
        return new BlockCollisionScenario(subject.scenario(), claim(subject, "collision-shape"),
                slot(subject, 1), 0F, 0F, Collections.singletonList(new BlockCollisionPlacement(
                        B173CollisionArena.TARGET_SUPPORT, BlockFace.UP, subject.state())),
                Arrays.asList(probe("level", 0D), probe("half-step", 0.5D),
                        probe("full-step", 1D)));
    }

    private static BlockLightScenario light(Subject subject) {
        return new BlockLightScenario(subject.scenario(), claim(subject, "light-behavior"),
                slot(subject, 1), 0F, 0F, Collections.singletonList(new BlockLightPlacement(
                        B173LightArena.SOURCE_SUPPORT, BlockFace.UP, subject.state())),
                Collections.singletonList(new BlockLightProbe("source", B173LightArena.SOURCE,
                        new BlockLightExpectation(AIR, 0, 15), new BlockLightExpectation(
                                subject.state(), subject.blockLight, subject.skyLight))));
    }

    private static BlockCollisionProbe probe(String id, double rise) {
        return new BlockCollisionProbe(id, 0D, rise, 1D, 10,
                BlockCollisionExpectation.PASSABLE);
    }

    private static BlockLifecycleSlot slot(Subject subject, int count) {
        return new BlockLifecycleSlot(HOTBAR, INVENTORY,
                new RemoteItemStack(subject.id, count, 0), null);
    }

    private static BlockConformanceCase claim(Subject subject, String template) {
        BlockConformancePlan plan = new BlockConformancePlan(Collections.singletonList(
                new BlockConformanceProfile(subject.subject(), subject.archetypes, false,
                        Collections.<String, ConformanceLayer>emptyMap())),
                Collections.singletonList(new BlockConformanceTemplate(
                        template, ConformanceLayer.ARCHETYPE)));
        return plan.caseFor(subject.subject(), template);
    }

    private static final class Subject {
        final int id, blockLight, skyLight;
        final String name;
        final List<String> archetypes;

        Subject(int id, String name, int blockLight, int skyLight, String... archetypes) {
            this.id = id; this.name = name; this.blockLight = blockLight; this.skyLight = skyLight;
            this.archetypes = Collections.unmodifiableList(Arrays.asList(archetypes));
        }

        String subject() { return String.format("b1.7.3:block/%03d", id); }
        String scenario() { return name + "-source-fluid-physical-envelope"; }
        BlockState state() { return new BlockState(id, 0); }
    }
}
