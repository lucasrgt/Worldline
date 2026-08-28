package worldline.testkit;

import java.util.List;
import java.util.Map;
import worldline.api.BlockFace;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.RemoteItemStack;

/** Locks the archetype three-family contract used by source-fluid physical rows. */
public final class SourceFluidPhysicalEnvelopeContractTest {
    private static final BlockPosition SUPPORT = new BlockPosition(4, 71, 4);
    private static final BlockPosition SOURCE = BlockFace.UP.adjacent(SUPPORT);
    private static final BlockState WATER = new BlockState(9, 0);
    private static final BlockState LAVA = new BlockState(11, 0);

    private SourceFluidPhysicalEnvelopeContractTest() { }

    public static void main(String[] arguments) {
        BlockConformancePlan plan = plan();
        require(plan.cases().size() == 6 && plan.cases().stream().allMatch(
                value -> value.layer() == ConformanceLayer.ARCHETYPE),
                "source-fluid archetype routing drifted");

        BlockStateDomainScenario state = new BlockStateDomainScenario(
                "still-water-source-fluid-physical-envelope",
                plan.caseFor("b1.7.3:block/009", "state-domain"), slot(9, 1), List.of(WATER),
                List.of(BlockStateDomainStep.place("place", SUPPORT, BlockFace.UP, 0F, 0F,
                        List.of(new BlockStateObservation(SOURCE, WATER)))), 40);
        require(state.domain().equals(List.of(WATER)) && state.finalStates().get(SOURCE).equals(WATER),
                "still-water singleton state contract drifted");

        BlockCollisionScenario collision = new BlockCollisionScenario(
                "still-water-source-fluid-physical-envelope",
                plan.caseFor("b1.7.3:block/009", "collision-shape"), slot(9, 1), 0F, 0F,
                List.of(new BlockCollisionPlacement(SUPPORT, BlockFace.UP, WATER)),
                List.of(new BlockCollisionProbe("level", 0D, 0D, 1D, 10,
                        BlockCollisionExpectation.PASSABLE)));
        require(collision.probes().get(0).expected() == BlockCollisionExpectation.PASSABLE,
                "source-fluid passable collision contract drifted");

        BlockLightScenario waterLight = light(plan, WATER, 0);
        BlockLightScenario lavaLight = light(plan, LAVA, 15);
        require(waterLight.probes().get(0).treatment().skyLight() == 12
                && lavaLight.probes().get(0).treatment().blockLight() == 15
                && lavaLight.probes().get(0).treatment().skyLight() == 12,
                "source-fluid light transport contract drifted");
        System.out.println("SourceFluidPhysicalEnvelopeContractTest passed");
    }

    private static BlockLightScenario light(BlockConformancePlan plan, BlockState state,
            int blockLight) {
        String subject = String.format("b1.7.3:block/%03d", state.legacyId());
        return new BlockLightScenario(state.legacyId() == 9
                ? "still-water-source-fluid-physical-envelope"
                : "still-lava-source-fluid-physical-envelope",
                plan.caseFor(subject, "light-behavior"), slot(state.legacyId(), 1), 0F, 0F,
                List.of(new BlockLightPlacement(SUPPORT, BlockFace.UP, state)),
                List.of(new BlockLightProbe("source", SOURCE,
                        new BlockLightExpectation(new BlockState(0, 0), 0, 15),
                        new BlockLightExpectation(state, blockLight, 12))));
    }

    private static BlockConformancePlan plan() {
        return new BlockConformancePlan(List.of(
                new BlockConformanceProfile("b1.7.3:block/009",
                        List.of("fluid", "tick-driven"), false, Map.of()),
                new BlockConformanceProfile("b1.7.3:block/011",
                        List.of("fluid", "luminous", "tick-driven"), false, Map.of())),
                List.of(new BlockConformanceTemplate("state-domain", ConformanceLayer.ARCHETYPE),
                        new BlockConformanceTemplate("collision-shape", ConformanceLayer.ARCHETYPE),
                        new BlockConformanceTemplate("light-behavior", ConformanceLayer.ARCHETYPE)));
    }

    private static BlockLifecycleSlot slot(int id, int count) {
        return new BlockLifecycleSlot(1, 37, new RemoteItemStack(id, count, 0), null);
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
