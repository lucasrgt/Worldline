package worldline.testkit;

import java.util.Objects;
import worldline.api.BlockState;

/** Bounded snowfall contract requiring a matching dry control to remain air. */
public final class SnowAccumulationFixture {
    private static final BlockState AIR = new BlockState(0, 0);
    private static final BlockState SNOW = new BlockState(78, 0);
    private SnowAccumulationFixture() { }

    public static Evidence verify(int maximumPasses, Pass pass) {
        if (maximumPasses < 1 || pass == null)
            throw new IllegalArgumentException("invalid snow accumulation boundary");
        for (int index = 1; index <= maximumPasses; index++) {
            Observation snowfall = requireObservation(pass.advance(true, index));
            Observation dry = requireObservation(pass.advance(false, index));
            require(snowfall.coldBiome && dry.coldBiome, "snow cells are not in a snow biome");
            require(snowfall.blockLight < 10 && dry.blockLight < 10,
                    "snow fixture crossed the block-light boundary");
            require(!dry.raining && AIR.equals(dry.state), "dry control did not remain air");
            if (SNOW.equals(snowfall.state)) {
                require(snowfall.raining, "snow layer formed without snowfall");
                return new Evidence(maximumPasses, snowfall.blockLight);
            }
            require(AIR.equals(snowfall.state), "snowfall cell changed unexpectedly");
        }
        throw new IllegalStateException("snow layer absent after bounded snowfall passes");
    }

    private static Observation requireObservation(Observation value) {
        if (value == null || value.state == null || value.blockLight < 0 || value.blockLight > 15)
            throw new IllegalStateException("invalid snow accumulation observation");
        return value;
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }

    @FunctionalInterface public interface Pass {
        Observation advance(boolean snowfall, int pass);
    }

    public static final class Observation {
        private final BlockState state; private final boolean coldBiome, raining;
        private final int blockLight;
        public Observation(BlockState state, boolean coldBiome, boolean raining, int blockLight) {
            this.state = state; this.coldBiome = coldBiome; this.raining = raining;
            this.blockLight = blockLight;
        }
        public BlockState state() { return state; }
        public boolean coldBiome() { return coldBiome; }
        public boolean raining() { return raining; }
        public int blockLight() { return blockLight; }
    }

    public static final class Evidence {
        private final int maximumPasses, blockLight;
        Evidence(int maximumPasses, int blockLight) {
            this.maximumPasses = maximumPasses; this.blockLight = blockLight;
        }
        public BlockState before() { return AIR; }
        public BlockState after() { return SNOW; }
        public int maximumPasses() { return maximumPasses; }
        public int blockLight() { return blockLight; }
        @Override public boolean equals(Object other) {
            if (!(other instanceof Evidence)) return false;
            Evidence value = (Evidence) other;
            return maximumPasses == value.maximumPasses && blockLight == value.blockLight;
        }
        @Override public int hashCode() { return Objects.hash(maximumPasses, blockLight); }
    }
}
