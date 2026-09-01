package worldline.testkit;

import java.util.Objects;

/** Canonical observation of fluids, sponge, snow, and ice lifecycle boundaries. */
public final class FluidFrozenMatterObservation {
    private final String fluids, sponge, snowLayer, ice, snowBlock;

    public FluidFrozenMatterObservation(String fluids, String sponge,
            String snowLayer, String ice, String snowBlock) {
        this.fluids = Objects.requireNonNull(fluids, "fluids");
        this.sponge = Objects.requireNonNull(sponge, "sponge");
        this.snowLayer = Objects.requireNonNull(snowLayer, "snowLayer");
        this.ice = Objects.requireNonNull(ice, "ice");
        this.snowBlock = Objects.requireNonNull(snowBlock, "snowBlock");
    }

    public String fluids() { return fluids; }
    public String sponge() { return sponge; }
    public String snowLayer() { return snowLayer; }
    public String ice() { return ice; }
    public String snowBlock() { return snowBlock; }

    @Override public boolean equals(Object other) {
        if (!(other instanceof FluidFrozenMatterObservation)) return false;
        FluidFrozenMatterObservation value = (FluidFrozenMatterObservation) other;
        return fluids.equals(value.fluids) && sponge.equals(value.sponge)
                && snowLayer.equals(value.snowLayer) && ice.equals(value.ice)
                && snowBlock.equals(value.snowBlock);
    }

    @Override public int hashCode() {
        return Objects.hash(fluids, sponge, snowLayer, ice, snowBlock);
    }
}
