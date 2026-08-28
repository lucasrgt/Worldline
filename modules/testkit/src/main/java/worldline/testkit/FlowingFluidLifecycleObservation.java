package worldline.testkit;

import java.util.Objects;
import worldline.api.BlockLifecycleDriver.ReloadBoundary;

/** Immutable paired observation of the native moving-water and moving-lava lifecycles. */
public final class FlowingFluidLifecycleObservation {
    private final FlowingFluidObservation water, lava;
    private final ReloadBoundary boundary;

    public FlowingFluidLifecycleObservation(FlowingFluidObservation water,
            FlowingFluidObservation lava, ReloadBoundary boundary) {
        this.water = Objects.requireNonNull(water, "water");
        this.lava = Objects.requireNonNull(lava, "lava");
        this.boundary = Objects.requireNonNull(boundary, "boundary");
    }

    public FlowingFluidObservation water() { return water; }
    public FlowingFluidObservation lava() { return lava; }
    public ReloadBoundary boundary() { return boundary; }

    @Override public boolean equals(Object other) {
        if (!(other instanceof FlowingFluidLifecycleObservation)) return false;
        FlowingFluidLifecycleObservation value = (FlowingFluidLifecycleObservation) other;
        return water.equals(value.water) && lava.equals(value.lava)
                && boundary == value.boundary;
    }

    @Override public int hashCode() { return Objects.hash(water, lava, boundary); }
}
