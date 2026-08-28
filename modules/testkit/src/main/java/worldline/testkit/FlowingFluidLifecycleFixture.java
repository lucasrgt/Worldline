package worldline.testkit;

import java.util.Arrays;
import java.util.List;
import worldline.api.BlockLifecycleDriver.ReloadBoundary;
import worldline.api.BlockState;
import worldline.api.WorldlineBehavior;

/** Validates derived generation, physics, cadence, neighbors, and persistence for moving fluids. */
public final class FlowingFluidLifecycleFixture {
    private static final List<Integer> WATER_DOMAIN = Arrays.asList(
            0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15);
    private static final List<Integer> OVERWORLD_LAVA_DOMAIN = Arrays.asList(
            0, 2, 4, 6, 8, 10, 12, 14);

    private FlowingFluidLifecycleFixture() { }

    public static FlowingFluidLifecycleEvidence execute(FlowingFluidLifecycleScenario scenario) {
        if (scenario == null) throw new NullPointerException("flowing-fluid scenario");
        FlowingFluidLifecycleObservation observed = scenario.observe();
        if (observed == null) throw new IllegalStateException("flowing-fluid observation is absent");
        verify(observed.water(), 8, 9, WATER_DOMAIN, 5, 1, 3, 0, 0, 12, "water");
        verify(observed.lava(), 10, 11, OVERWORLD_LAVA_DOMAIN, 30, 2, 0, 15, 15, 0, "lava");
        require(observed.boundary() == ReloadBoundary.CHUNK_RELOAD,
                "flowing-fluid reload boundary drifted");
        require(WorldlineBehavior.require("fluid-flow") == WorldlineBehavior.FLUID_FLOW,
                "fluid-flow behavior registration drifted");
        return new FlowingFluidLifecycleEvidence(observed);
    }

    private static void verify(FlowingFluidObservation row, int movingId,
            int stillId, List<Integer> domain, int firstTick, int savedMetadata,
            int opacity, int emission, int blockLight, int skyLight, String role) {
        require(row.movingId() == movingId && row.metadataDomain().equals(domain),
                role + " flowing metadata domain drifted");
        require(row.firstFlowTick() == firstTick, role + " scheduler cadence drifted");
        require(row.blocked().equals(new BlockState(stillId, 0))
                && row.recomputed().equals(new BlockState(movingId, 0)),
                role + " neighbor recomputation drifted");
        require(row.passable(), role + " collision envelope drifted");
        require(row.opacity() == opacity && row.emission() == emission
                && row.blockLight() == blockLight && row.skyLight() == skyLight,
                role + " light transport drifted");
        BlockState expected = new BlockState(movingId, savedMetadata);
        require(row.saved().equals(expected) && row.reloaded().equals(expected),
                role + " flowing-state persistence drifted");
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
