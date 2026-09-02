package worldline.b173server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import worldline.api.BlockFace;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.RemoteItemStack;
import worldline.testapi.BlockCollisionExpectation;
import worldline.testapi.BlockCollisionPlacement;
import worldline.testapi.BlockCollisionProbe;
import worldline.testapi.BlockCollisionScenario;
import worldline.testapi.BlockConformanceCase;
import worldline.testapi.BlockConformancePlan;
import worldline.testapi.BlockConformanceProfile;
import worldline.testapi.BlockConformanceTemplate;
import worldline.testapi.BlockLifecycleSlot;
import worldline.testapi.BlockLightExpectation;
import worldline.testapi.BlockLightPlacement;
import worldline.testapi.BlockLightProbe;
import worldline.testapi.BlockLightScenario;
import worldline.testapi.BlockStateDomainScenario;
import worldline.testapi.BlockStateDomainStep;
import worldline.testapi.BlockStateObservation;
import worldline.testapi.ConformanceLayer;

/** Public singular rows for six tile-backed utilities with per-subject light envelopes. */
public final class B173TileUtilityPhysicalScenarioFactory {
    public static final long SEED = 17_320_110_707L;
    private static final int HOTBAR = 1, INVENTORY = 37;
    private static final BlockState AIR = new BlockState(0, 0);

    private B173TileUtilityPhysicalScenarioFactory() { }

    public static List<BlockStateDomainScenario> stateDomains() {
        List<BlockStateDomainScenario> rows = new ArrayList<BlockStateDomainScenario>();
        rows.add(B173StateDomainScenarioFactory.dispenserFacing());
        rows.add(singletonState(find(25))); rows.add(singletonState(find(52)));
        rows.add(B173StateDomainScenarioFactory.chestPlacementMetadata());
        rows.add(B173StateDomainScenarioFactory.furnaceFacing());
        return Collections.unmodifiableList(rows);
    }

    public static List<BlockCollisionScenario> collisions() {
        List<BlockCollisionScenario> rows = new ArrayList<BlockCollisionScenario>();
        for (B173TileUtilityPhysicalCatalog.Subject subject
                : B173TileUtilityPhysicalCatalog.subjects()) rows.add(collision(subject));
        return Collections.unmodifiableList(rows);
    }

    public static List<BlockLightScenario> lights() {
        List<BlockLightScenario> rows = new ArrayList<BlockLightScenario>();
        for (B173TileUtilityPhysicalCatalog.Subject subject
                : B173TileUtilityPhysicalCatalog.subjects()) rows.add(light(subject));
        return Collections.unmodifiableList(rows);
    }

    private static BlockStateDomainScenario singletonState(
            B173TileUtilityPhysicalCatalog.Subject subject) {
        List<BlockStateDomainStep> steps = new ArrayList<BlockStateDomainStep>();
        float[] yaws = {0F, 90F, 180F, -90F};
        for (int index = 0; index < yaws.length; index++) {
            BlockPosition support = B173StateDomainArena.SUPPORTS.get(index);
            steps.add(BlockStateDomainStep.place("place-yaw-" + index, support, BlockFace.UP,
                    yaws[index], 0F, Collections.singletonList(new BlockStateObservation(
                            BlockFace.UP.adjacent(support), subject.state()))));
        }
        return new BlockStateDomainScenario(subject.scenario(), claim(subject, "state-domain"),
                slot(subject, 4), Collections.singletonList(subject.state()), steps, 40);
    }

    private static BlockCollisionScenario collision(
            B173TileUtilityPhysicalCatalog.Subject subject) {
        return new BlockCollisionScenario(subject.scenario(), claim(subject, "collision-shape"),
                slot(subject, 1), 0F, 0F, Collections.singletonList(new BlockCollisionPlacement(
                        B173CollisionArena.TARGET_SUPPORT, BlockFace.UP, subject.state())),
                Arrays.asList(probe("level", 0D, BlockCollisionExpectation.BLOCKED),
                        probe("half-step", 0.5D, BlockCollisionExpectation.BLOCKED),
                        probe("full-step", 1D, BlockCollisionExpectation.PASSABLE)));
    }

    private static BlockLightScenario light(B173TileUtilityPhysicalCatalog.Subject subject) {
        return new BlockLightScenario(subject.scenario(), claim(subject, "light-behavior"),
                slot(subject, 1), 0F, 0F, Collections.singletonList(new BlockLightPlacement(
                        B173LightArena.SOURCE_SUPPORT, BlockFace.UP, subject.state())),
                Collections.singletonList(new BlockLightProbe("source", B173LightArena.SOURCE,
                        new BlockLightExpectation(AIR, 0, 15),
                        new BlockLightExpectation(subject.state(), 0, subject.sourceSkyLight))));
    }

    private static BlockCollisionProbe probe(String id, double rise,
            BlockCollisionExpectation expected) {
        return new BlockCollisionProbe(id, 0D, rise, 1D, 10, expected);
    }

    private static BlockLifecycleSlot slot(
            B173TileUtilityPhysicalCatalog.Subject subject, int count) {
        return new BlockLifecycleSlot(HOTBAR, INVENTORY,
                new RemoteItemStack(subject.id, count, 0), null);
    }

    private static BlockConformanceCase claim(
            B173TileUtilityPhysicalCatalog.Subject subject, String template) {
        BlockConformancePlan plan = new BlockConformancePlan(Collections.singletonList(
                new BlockConformanceProfile(subject.subject(), subject.archetypes, true,
                        Collections.<String, ConformanceLayer>emptyMap())),
                Collections.singletonList(new BlockConformanceTemplate(
                        template, ConformanceLayer.ARCHETYPE)));
        return plan.caseFor(subject.subject(), template);
    }

    private static B173TileUtilityPhysicalCatalog.Subject find(int id) {
        for (B173TileUtilityPhysicalCatalog.Subject subject
                : B173TileUtilityPhysicalCatalog.subjects()) if (subject.id == id) return subject;
        throw new IllegalArgumentException("unknown tile utility " + id);
    }
}
