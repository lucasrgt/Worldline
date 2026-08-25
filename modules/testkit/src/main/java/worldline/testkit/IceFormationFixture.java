package worldline.testkit;

import java.util.Objects;
import worldline.api.BlockState;

/** Reusable bounded contract for cold-biome water freezing while lit water stays liquid. */
public final class IceFormationFixture {
    private static final BlockState WATER = new BlockState(9, 0);
    private static final BlockState ICE = new BlockState(79, 0);
    private IceFormationFixture() { }

    public static Evidence verify(int maximumPasses, Pass pass) {
        if (maximumPasses < 1 || pass == null)
            throw new IllegalArgumentException("invalid ice formation boundary");
        for (int index = 1; index <= maximumPasses; index++) {
            Observation dark = requireObservation(pass.advance(false, index));
            Observation lit = requireObservation(pass.advance(true, index));
            require(dark.coldBiome && lit.coldBiome, "formation cells are not in a snow biome");
            require(lit.light >= 10 && WATER.equals(lit.state),
                    "lit control water did not remain liquid");
            if (ICE.equals(dark.state)) {
                require(dark.light < 10, "ice formed above the block-light boundary");
                return new Evidence(maximumPasses, dark.light, lit.light);
            }
            require(WATER.equals(dark.state), "dark water changed to an unexpected block");
        }
        throw new IllegalStateException("ice absent after bounded ambient passes");
    }

    private static Observation requireObservation(Observation value) {
        if (value == null || value.state == null || value.light < 0 || value.light > 15)
            throw new IllegalStateException("invalid ice formation observation");
        return value;
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }

    @FunctionalInterface public interface Pass {
        Observation advance(boolean litControl, int pass);
    }

    public static final class Observation {
        private final BlockState state; private final boolean coldBiome; private final int light;
        public Observation(BlockState state, boolean coldBiome, int light) {
            this.state = state; this.coldBiome = coldBiome; this.light = light;
        }
        public BlockState state() { return state; }
        public boolean coldBiome() { return coldBiome; }
        public int blockLight() { return light; }
    }

    public static final class Evidence {
        private final int maximumPasses, formationLight, controlLight;
        Evidence(int maximumPasses, int formationLight, int controlLight) {
            this.maximumPasses = maximumPasses; this.formationLight = formationLight;
            this.controlLight = controlLight;
        }
        public BlockState before() { return WATER; }
        public BlockState after() { return ICE; }
        public int maximumPasses() { return maximumPasses; }
        public int formationLight() { return formationLight; }
        public int controlLight() { return controlLight; }
        @Override public boolean equals(Object other) {
            if (!(other instanceof Evidence)) return false;
            Evidence value = (Evidence) other;
            return maximumPasses == value.maximumPasses && formationLight == value.formationLight
                    && controlLight == value.controlLight;
        }
        @Override public int hashCode() {
            return Objects.hash(maximumPasses, formationLight, controlLight);
        }
    }
}
