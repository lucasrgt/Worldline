package worldline.testkit;
import worldline.testapi.BlockCollisionExpectation;
import worldline.testapi.BlockCollisionPlacement;
import worldline.testapi.BlockCollisionProbe;
import worldline.testapi.BlockCollisionScenario;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import worldline.api.BlockFace;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.RemoteItemStack;

/** Locks the singular three-family contract used by tile-utility physical rows. */
public final class TileUtilityPhysicalEnvelopeContractTest {
    private static final BlockPosition SUPPORT = new BlockPosition(4, 71, 4);
    private static final BlockState NOTE = new BlockState(25, 0);
    private static final BlockState SPAWNER = new BlockState(52, 0);
    private static final int[] SUBJECTS = {23, 25, 52, 54, 61, 84};

    private TileUtilityPhysicalEnvelopeContractTest() { }

    public static void main(String[] arguments) {
        BlockConformancePlan plan = plan();
        require(plan.cases().size() == 18 && plan.cases().stream().allMatch(
                value -> value.layer() == ConformanceLayer.SINGULAR),
                "tile-utility singular routing drifted");
        long selected = plan.cases().stream().filter(value -> !value.claimId().equals(
                "b1.7.3:block/084#state-domain")).count();
        require(selected == 17, "occupied-jukebox state must remain outside this package");

        BlockStateDomainScenario state = new BlockStateDomainScenario(
                "note-block-tile-utility-physical-envelope",
                plan.caseFor("b1.7.3:block/025", "state-domain"), slot(25, 1), List.of(NOTE),
                List.of(BlockStateDomainStep.place("place", SUPPORT, BlockFace.UP, 0F, 0F,
                        List.of(new BlockStateObservation(BlockFace.UP.adjacent(SUPPORT), NOTE)))),
                40);
        require(state.domain().equals(List.of(NOTE)) && state.finalStates().size() == 1,
                "singleton tile state-domain contract drifted");

        BlockCollisionScenario collision = new BlockCollisionScenario(
                "note-block-tile-utility-physical-envelope",
                plan.caseFor("b1.7.3:block/025", "collision-shape"), slot(25, 1), 0F, 0F,
                List.of(new BlockCollisionPlacement(SUPPORT, BlockFace.UP, NOTE)),
                List.of(new BlockCollisionProbe("level", 0D, 0D, 1D, 10,
                        BlockCollisionExpectation.BLOCKED)));
        require(collision.claim().layer() == ConformanceLayer.SINGULAR
                && collision.probes().get(0).expected() == BlockCollisionExpectation.BLOCKED,
                "tile full-cube collision contract drifted");

        BlockLightScenario light = new BlockLightScenario(
                "note-block-tile-utility-physical-envelope",
                plan.caseFor("b1.7.3:block/025", "light-behavior"), slot(25, 1), 0F, 0F,
                List.of(new BlockLightPlacement(SUPPORT, BlockFace.UP, NOTE)),
                List.of(new BlockLightProbe("source", BlockFace.UP.adjacent(SUPPORT),
                        new BlockLightExpectation(new BlockState(0, 0), 0, 15),
                        new BlockLightExpectation(NOTE, 0, 0))));
        require(light.claim().layer() == ConformanceLayer.SINGULAR
                && light.probes().get(0).treatment().skyLight() == 0,
                "tile opaque light contract drifted");

        BlockLightScenario transparentLight = new BlockLightScenario(
                "mob-spawner-tile-utility-physical-envelope",
                plan.caseFor("b1.7.3:block/052", "light-behavior"), slot(52, 1), 0F, 0F,
                List.of(new BlockLightPlacement(SUPPORT, BlockFace.UP, SPAWNER)),
                List.of(new BlockLightProbe("source", BlockFace.UP.adjacent(SUPPORT),
                        new BlockLightExpectation(new BlockState(0, 0), 0, 15),
                        new BlockLightExpectation(SPAWNER, 0, 15))));
        require(transparentLight.claim().layer() == ConformanceLayer.SINGULAR
                && transparentLight.probes().get(0).treatment().skyLight() == 15,
                "tile transparent-light exception contract drifted");
        SourceFluidPhysicalEnvelopeContractTest.execute();
        FluidDynamicsContractTest.execute();
        System.out.println("TileUtilityPhysicalEnvelopeContractTest passed");
    }

    private static BlockConformancePlan plan() {
        List<BlockConformanceProfile> profiles = new ArrayList<BlockConformanceProfile>();
        for (int id : SUBJECTS) profiles.add(new BlockConformanceProfile(
                String.format("b1.7.3:block/%03d", id), List.of("tile-entity"), true, Map.of()));
        return new BlockConformancePlan(profiles, List.of(
                new BlockConformanceTemplate("state-domain", ConformanceLayer.ARCHETYPE),
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
