package worldline.b173server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import worldline.api.BlockFace;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.RemoteItemStack;
import worldline.testkit.BlockCollisionPlacement;
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

/** Data-driven official rows for non-full-cube physical envelopes. */
public final class B173SpecialCollisionPhysicalScenarioFactory {
    public static final long SEED = 17_320_110_707L;
    private static final int HOTBAR = 1, INVENTORY = 37;
    private static final BlockState AIR = new BlockState(0, 0);

    private B173SpecialCollisionPhysicalScenarioFactory() {
    }

    public static List<BlockStateDomainScenario> stateDomains() {
        List<BlockStateDomainScenario> rows = new ArrayList<BlockStateDomainScenario>();
        for (B173SpecialCollisionPhysicalCatalog.Subject subject
                : B173SpecialCollisionPhysicalCatalog.subjects()) {
            rows.add(stateDomain(subject));
        }
        return Collections.unmodifiableList(rows);
    }

    public static List<BlockCollisionScenario> collisions() {
        List<BlockCollisionScenario> rows = new ArrayList<BlockCollisionScenario>();
        for (B173SpecialCollisionPhysicalCatalog.Subject subject
                : B173SpecialCollisionPhysicalCatalog.subjects()) {
            rows.add(collision(subject));
        }
        return Collections.unmodifiableList(rows);
    }

    public static List<BlockLightScenario> lights() {
        List<BlockLightScenario> rows = new ArrayList<BlockLightScenario>();
        for (B173SpecialCollisionPhysicalCatalog.Subject subject
                : B173SpecialCollisionPhysicalCatalog.subjects()) {
            rows.add(light(subject));
        }
        return Collections.unmodifiableList(rows);
    }

    private static BlockStateDomainScenario stateDomain(
            B173SpecialCollisionPhysicalCatalog.Subject subject) {
        List<BlockStateDomainStep> steps = new ArrayList<BlockStateDomainStep>();
        float[] yaws = {0F, 90F, 180F, -90F};
        for (int index = 0; index < yaws.length; index++) {
            BlockPosition pad = support(subject, index);
            steps.add(BlockStateDomainStep.place("place-yaw-" + index, pad, BlockFace.UP,
                    yaws[index], 0F, Collections.singletonList(new BlockStateObservation(
                            BlockFace.UP.adjacent(pad), subject.state()))));
        }
        return new BlockStateDomainScenario(subject.scenario(), claim(subject, "state-domain"),
                slot(subject, 4), Collections.singletonList(subject.state()), steps, 40,
                subject.support);
    }

    private static BlockCollisionScenario collision(
            B173SpecialCollisionPhysicalCatalog.Subject subject) {
        return new BlockCollisionScenario(subject.scenario(), claim(subject, "collision-shape"),
                slot(subject, 1), 0F, 0F, Collections.singletonList(new BlockCollisionPlacement(
                        B173CollisionArena.TARGET_SUPPORT, BlockFace.UP, subject.state())),
                subject.probes, subject.support);
    }

    private static BlockLightScenario light(
            B173SpecialCollisionPhysicalCatalog.Subject subject) {
        BlockLightExpectation control = new BlockLightExpectation(AIR, 0, 15);
        BlockLightExpectation treatment = new BlockLightExpectation(subject.state(), 0,
                subject.sky);
        return new BlockLightScenario(subject.scenario(), claim(subject, "light-behavior"),
                slot(subject, 1), 0F, 0F, Collections.singletonList(new BlockLightPlacement(
                        B173LightArena.SOURCE_SUPPORT, BlockFace.UP, subject.state())),
                Collections.singletonList(new BlockLightProbe("source", B173LightArena.SOURCE,
                        control, treatment)), subject.support);
    }

    private static BlockPosition support(
            B173SpecialCollisionPhysicalCatalog.Subject subject, int index) {
        BlockPosition pad = B173StateDomainArena.SUPPORTS.get(index);
        if (subject.support.equals(B173SpecialCollisionPhysicalCatalog.SAND)) {
            return BlockFace.UP.adjacent(pad);
        }
        return pad;
    }

    private static BlockLifecycleSlot slot(
            B173SpecialCollisionPhysicalCatalog.Subject subject, int count) {
        return new BlockLifecycleSlot(HOTBAR, INVENTORY,
                new RemoteItemStack(subject.id, count, 0), null);
    }

    private static BlockConformanceCase claim(
            B173SpecialCollisionPhysicalCatalog.Subject subject, String template) {
        BlockConformancePlan plan = new BlockConformancePlan(Collections.singletonList(
                new BlockConformanceProfile(subject.subject(), subject.archetypes, false,
                        Collections.<String, ConformanceLayer>emptyMap())),
                Collections.singletonList(new BlockConformanceTemplate(
                        template, ConformanceLayer.ARCHETYPE)));
        return plan.caseFor(subject.subject(), template);
    }
}
