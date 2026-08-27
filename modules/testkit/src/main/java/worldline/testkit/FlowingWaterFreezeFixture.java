package worldline.testkit;

import java.util.Objects;
import worldline.api.BlockState;

/** Bounded cold-biome contract separating freezing source water from flowing water. */
public final class FlowingWaterFreezeFixture {
    private static final BlockState STILL_WATER = new BlockState(9, 0);
    private static final BlockState FLOWING_WATER = new BlockState(8, 1);
    private static final BlockState ICE = new BlockState(79, 0);

    private FlowingWaterFreezeFixture() {
    }

    public static Evidence verify(int maximumPasses, Pass pass) {
        if (maximumPasses < 1 || pass == null)
            throw new IllegalArgumentException("invalid flowing-water freeze boundary");
        for (int index = 1; index <= maximumPasses; index++) {
            Observation value = requireObservation(pass.advance(index));
            require(value.coldBiome, "water cells are not in a snow biome");
            require(value.stillLight < 10 && value.flowingLight < 10,
                    "flowing-water fixture crossed the block-light boundary");
            require(FLOWING_WATER.equals(value.flowingState),
                    "flowing water changed before the freeze observation");
            if (ICE.equals(value.stillState)) {
                require(value.stillLight < 10, "still water froze above the block-light boundary");
                return new Evidence(maximumPasses, value.stillLight, value.flowingLight);
            }
            require(STILL_WATER.equals(value.stillState),
                    "still water changed to an unexpected block");
        }
        throw new IllegalStateException("still water absent after bounded ambient passes");
    }

    private static Observation requireObservation(Observation value) {
        if (value == null || value.stillState == null || value.flowingState == null
                || value.stillLight < 0 || value.stillLight > 15
                || value.flowingLight < 0 || value.flowingLight > 15)
            throw new IllegalStateException("invalid flowing-water freeze observation");
        return value;
    }

    private static void require(boolean value, String message) {
        if (!value) {
            throw new IllegalStateException(message);
        }
    }

    @FunctionalInterface
    public interface Pass {
        Observation advance(int pass);
    }

    public static final class Observation {
        private final BlockState stillState;
        private final BlockState flowingState;
        private final boolean coldBiome;
        private final int stillLight;
        private final int flowingLight;

        public Observation(BlockState stillState, BlockState flowingState, boolean coldBiome,
                int stillLight, int flowingLight) {
            this.stillState = stillState;
            this.flowingState = flowingState;
            this.coldBiome = coldBiome;
            this.stillLight = stillLight;
            this.flowingLight = flowingLight;
        }

        public BlockState stillState() {
            return stillState;
        }

        public BlockState flowingState() {
            return flowingState;
        }

        public boolean coldBiome() {
            return coldBiome;
        }

        public int stillBlockLight() {
            return stillLight;
        }

        public int flowingBlockLight() {
            return flowingLight;
        }
    }

    public static final class Evidence {
        private final int maximumPasses;
        private final int stillLight;
        private final int flowingLight;

        Evidence(int maximumPasses, int stillLight, int flowingLight) {
            this.maximumPasses = maximumPasses;
            this.stillLight = stillLight;
            this.flowingLight = flowingLight;
        }

        public BlockState before() {
            return STILL_WATER;
        }

        public BlockState after() {
            return ICE;
        }

        public BlockState flowing() {
            return FLOWING_WATER;
        }

        public int maximumPasses() {
            return maximumPasses;
        }

        public int stillBlockLight() {
            return stillLight;
        }

        public int flowingBlockLight() {
            return flowingLight;
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof Evidence)) return false;
            Evidence value = (Evidence) other;
            return maximumPasses == value.maximumPasses && stillLight == value.stillLight
                    && flowingLight == value.flowingLight;
        }

        @Override
        public int hashCode() {
            return Objects.hash(maximumPasses, stillLight, flowingLight);
        }
    }
}
