package worldline.testkit;

import java.util.Objects;
import worldline.api.BlockState;

/** Reusable bounded contract for a snow layer that does not gain a second layer. */
public final class SnowLayerNonstackingFixture {
    private static final BlockState AIR = new BlockState(0, 0);
    private static final BlockState SNOW = new BlockState(78, 0);

    private SnowLayerNonstackingFixture() {
    }

    public static Evidence verify(int maximumFormationPasses,
            int maximumSettlingPasses, Pass pass) {
        if (maximumFormationPasses < 1 || maximumSettlingPasses < 1 || pass == null) {
            throw new IllegalArgumentException("invalid snow layer boundary");
        }
        for (int index = 1; index <= maximumFormationPasses; index++) {
            Observation snowfall = requireObservation(pass.advance(true, index));
            Observation dry = requireObservation(pass.advance(false, index));
            requireContext(snowfall, dry);
            requireDry(dry);
            if (SNOW.equals(snowfall.state)) {
                requireSingleLayer(snowfall, true);
                for (int settling = 1; settling <= maximumSettlingPasses; settling++) {
                    Observation continued = requireObservation(
                            pass.advance(true, maximumFormationPasses + settling));
                    Observation dryContinued = requireObservation(
                            pass.advance(false, maximumFormationPasses + settling));
                    requireContext(continued, dryContinued);
                    requireSingleLayer(continued, true);
                    requireDry(dryContinued);
                }
                return new Evidence(maximumFormationPasses, maximumSettlingPasses,
                        snowfall.blockLight);
            }
            require(AIR.equals(snowfall.state) && AIR.equals(snowfall.above)
                    && snowfall.columnSnowCount == 0,
                    "snowfall cell changed before its first layer");
        }
        throw new IllegalStateException("snow layer absent after bounded formation passes");
    }

    private static Observation requireObservation(Observation value) {
        if (value == null || value.state == null || value.above == null
                || value.blockLight < 0 || value.blockLight > 15
                || value.columnSnowCount < 0 || value.columnSnowCount > 127) {
            throw new IllegalStateException("invalid snow layer observation");
        }
        return value;
    }

    private static void requireContext(Observation snowfall, Observation dry) {
        require(snowfall.coldBiome && dry.coldBiome,
                "snow cells are not in a snow biome");
        require(snowfall.blockLight < 10 && dry.blockLight < 10,
                "snow layer crossed the block-light boundary");
    }

    private static void requireDry(Observation dry) {
        require(!dry.raining && AIR.equals(dry.state) && AIR.equals(dry.above)
                && dry.columnSnowCount == 0, "dry control gained snow");
    }

    private static void requireSingleLayer(Observation value, boolean raining) {
        require(raining == value.raining && SNOW.equals(value.state)
                && AIR.equals(value.above) && value.columnSnowCount == 1,
                "snow layer stacked or lost its rain cause");
    }

    private static void require(boolean value, String message) {
        if (!value) {
            throw new IllegalStateException(message);
        }
    }

    @FunctionalInterface
    public interface Pass {
        Observation advance(boolean snowfall, int pass);
    }

    public static final class Observation {
        private final BlockState state;
        private final BlockState above;
        private final int columnSnowCount;
        private final boolean coldBiome;
        private final boolean raining;
        private final int blockLight;

        public Observation(BlockState state, BlockState above, int columnSnowCount,
                boolean coldBiome, boolean raining, int blockLight) {
            this.state = Objects.requireNonNull(state, "state");
            this.above = Objects.requireNonNull(above, "above");
            this.columnSnowCount = columnSnowCount;
            this.coldBiome = coldBiome;
            this.raining = raining;
            this.blockLight = blockLight;
        }

        public BlockState state() {
            return state;
        }

        public BlockState above() {
            return above;
        }

        public int columnSnowCount() {
            return columnSnowCount;
        }

        public boolean coldBiome() {
            return coldBiome;
        }

        public boolean raining() {
            return raining;
        }

        public int blockLight() {
            return blockLight;
        }
    }

    public static final class Evidence {
        private final int maximumFormationPasses;
        private final int maximumSettlingPasses;
        private final int blockLight;

        Evidence(int maximumFormationPasses, int maximumSettlingPasses, int blockLight) {
            this.maximumFormationPasses = maximumFormationPasses;
            this.maximumSettlingPasses = maximumSettlingPasses;
            this.blockLight = blockLight;
        }

        public BlockState before() {
            return AIR;
        }

        public BlockState after() {
            return SNOW;
        }

        public BlockState above() {
            return AIR;
        }

        public int columnSnowCount() {
            return 1;
        }

        public int maximumFormationPasses() {
            return maximumFormationPasses;
        }

        public int maximumSettlingPasses() {
            return maximumSettlingPasses;
        }

        public int blockLight() {
            return blockLight;
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof Evidence)) {
                return false;
            }
            Evidence value = (Evidence) other;
            return maximumFormationPasses == value.maximumFormationPasses
                    && maximumSettlingPasses == value.maximumSettlingPasses
                    && blockLight == value.blockLight;
        }

        @Override
        public int hashCode() {
            return Objects.hash(maximumFormationPasses, maximumSettlingPasses, blockLight);
        }
    }
}
