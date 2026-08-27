package worldline.b173server;

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
import worldline.testkit.BlockConformancePlan;
import worldline.testkit.BlockConformanceProfile;
import worldline.testkit.BlockConformanceTemplate;
import worldline.testkit.BlockLifecycleSlot;
import worldline.testkit.ConformanceLayer;

/** Official b1.7.3 static collision-envelope rows. */
public final class B173CollisionScenarioFactory {
    private static final int HOTBAR = 1;
    private static final int INVENTORY = 37;

    private B173CollisionScenarioFactory() { }

    public static List<BlockCollisionScenario> staticEnvelopeFamily() {
        return Collections.unmodifiableList(Arrays.asList(
                stoneFullCube(), stoneSlab(), woodStairs(), fence(), torch()));
    }

    public static BlockCollisionScenario stoneFullCube() {
        return scenario("stone-full-cube-envelope", "b1.7.3:block/001", 1, 0F,
                Collections.singletonList("simple-solid"),
                placements(new BlockCollisionPlacement(B173CollisionArena.TARGET_SUPPORT,
                        BlockFace.UP, new BlockState(1, 0))),
                probes(blocked("level"), blocked("half-step", 0.5D),
                        passable("full-step", 1D)));
    }

    public static BlockCollisionScenario stoneSlab() {
        return scenario("stone-slab-half-envelope", "b1.7.3:block/044", 44, 0F,
                Arrays.asList("stateful-metadata", "special-collision"),
                placements(new BlockCollisionPlacement(B173CollisionArena.TARGET_SUPPORT,
                        BlockFace.UP, new BlockState(44, 0))),
                probes(blocked("level"), passable("half-step", 0.5D)));
    }

    public static BlockCollisionScenario woodStairs() {
        // Metadata 2 keeps the north z-half low; the short probe stays wholly inside it.
        return scenario("wood-stairs-step-envelope", "b1.7.3:block/053", 53, 0F,
                Arrays.asList("directional", "special-collision"),
                placements(new BlockCollisionPlacement(B173CollisionArena.TARGET_SUPPORT,
                        BlockFace.UP, new BlockState(53, 2))),
                probes(blocked("level"), new BlockCollisionProbe("low-half-step",
                        0D, 0.5D, 0.6D, 10, BlockCollisionExpectation.PASSABLE)));
    }

    public static BlockCollisionScenario fence() {
        return scenario("fence-raised-envelope", "b1.7.3:block/085", 85, 0F,
                Collections.singletonList("special-collision"),
                placements(
                        new BlockCollisionPlacement(B173CollisionArena.TARGET_SUPPORT,
                                BlockFace.UP, new BlockState(85, 0)),
                        new BlockCollisionPlacement(B173CollisionArena.EAST_SUPPORT,
                                BlockFace.UP, new BlockState(85, 0))),
                probes(blocked("level"), blocked("full-step", 1D)));
    }

    public static BlockCollisionScenario torch() {
        return scenario("torch-empty-envelope", "b1.7.3:block/050", 50, 0F,
                Arrays.asList("support-dependent", "luminous", "directional"),
                placements(new BlockCollisionPlacement(B173CollisionArena.TARGET_SUPPORT,
                        BlockFace.UP, new BlockState(50, 5))),
                probes(passable("level", 0D)));
    }

    private static BlockCollisionScenario scenario(String id, String subject, int itemId,
            float yaw, List<String> archetypes, List<BlockCollisionPlacement> placements,
            List<BlockCollisionProbe> probes) {
        BlockConformancePlan plan = new BlockConformancePlan(Collections.singletonList(
                new BlockConformanceProfile(subject, archetypes, false,
                        Collections.<String, ConformanceLayer>emptyMap())),
                Collections.singletonList(new BlockConformanceTemplate(
                        "collision-shape", ConformanceLayer.ARCHETYPE)));
        return new BlockCollisionScenario(id, plan.caseFor(subject, "collision-shape"),
                new BlockLifecycleSlot(HOTBAR, INVENTORY,
                        new RemoteItemStack(itemId, placements.size(), 0), null),
                yaw, 0F, placements, probes);
    }

    private static BlockCollisionProbe blocked(String id) { return blocked(id, 0D); }
    private static BlockCollisionProbe blocked(String id, double rise) {
        return new BlockCollisionProbe(id, 0D, rise, 1D, 10,
                BlockCollisionExpectation.BLOCKED);
    }
    private static BlockCollisionProbe passable(String id, double rise) {
        return new BlockCollisionProbe(id, 0D, rise, 1D, 10,
                BlockCollisionExpectation.PASSABLE);
    }
    private static List<BlockCollisionProbe> probes(BlockCollisionProbe... values) {
        return Arrays.asList(values);
    }
    private static List<BlockCollisionPlacement> placements(BlockCollisionPlacement... values) {
        return Arrays.asList(values);
    }
}
