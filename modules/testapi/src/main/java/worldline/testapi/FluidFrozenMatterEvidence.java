package worldline.testapi;

import java.util.Objects;

/** Equatable evidence for the native fluid and frozen-matter matrix. */
public final class FluidFrozenMatterEvidence {
    private final FluidFrozenMatterObservation observation;

    public FluidFrozenMatterEvidence(FluidFrozenMatterObservation observation) {
        this.observation = Objects.requireNonNull(observation, "observation");
    }

    public FluidFrozenMatterObservation observation() { return observation; }

    public String canonical() {
        StringBuilder value = new StringBuilder(
                "schema=worldline.fluid-frozen-matter-evidence.v1\n");
        value.append("subjects=b1.7.3:block/008+009+010+011+019+078+079+080\n");
        value.append("claims=21|fluids=11+sponge=2+snow-layer=2+ice=5+snow-block=1\n");
        value.append("fluids=").append(observation.fluids()).append('\n');
        value.append("sponge=").append(observation.sponge()).append('\n');
        value.append("snow-layer=").append(observation.snowLayer()).append('\n');
        value.append("ice=").append(observation.ice()).append('\n');
        return value.append("snow-block=").append(observation.snowBlock()).append('\n').toString();
    }

    @Override public boolean equals(Object other) {
        return other instanceof FluidFrozenMatterEvidence
                && observation.equals(((FluidFrozenMatterEvidence) other).observation);
    }

    @Override public int hashCode() { return observation.hashCode(); }
}
