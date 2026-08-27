package worldline.testkit;

import java.util.List;
import java.util.Objects;
import worldline.api.BlockLifecycleDriver.ReloadBoundary;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.RemoteItemStack;

/** Stable lifecycle result with transient entity details normalized away. */
public final class BlockLifecycleEvidence {
    private final String placementClaim, persistenceClaim, transitionClaim, dropClaim;
    private final ConformanceLayer placementLayer, persistenceLayer, transitionLayer, dropLayer;
    private final BlockPosition position;
    private final BlockState state;
    private final List<RemoteItemStack> drops;
    private final ReloadBoundary boundary;

    BlockLifecycleEvidence(BlockLifecycleScenario scenario, List<RemoteItemStack> drops,
            ReloadBoundary boundary) {
        placementClaim = scenario.placement().claimId();
        persistenceClaim = scenario.persistence().claimId();
        transitionClaim = scenario.transition().claimId();
        dropClaim = scenario.drops().claimId();
        placementLayer = scenario.placement().layer();
        persistenceLayer = scenario.persistence().layer();
        transitionLayer = scenario.transition().layer();
        dropLayer = scenario.drops().layer();
        position = scenario.target();
        state = scenario.placedState();
        this.drops = List.copyOf(drops);
        this.boundary = Objects.requireNonNull(boundary, "boundary");
    }

    public String placementClaim() { return placementClaim; }
    public String persistenceClaim() { return persistenceClaim; }
    public String transitionClaim() { return transitionClaim; }
    public String dropClaim() { return dropClaim; }
    public ConformanceLayer placementLayer() { return placementLayer; }
    public ConformanceLayer persistenceLayer() { return persistenceLayer; }
    public ConformanceLayer transitionLayer() { return transitionLayer; }
    public ConformanceLayer dropLayer() { return dropLayer; }
    public BlockPosition position() { return position; }
    public BlockState state() { return state; }
    public List<RemoteItemStack> drops() { return drops; }
    public ReloadBoundary boundary() { return boundary; }

    @Override public boolean equals(Object other) {
        if (!(other instanceof BlockLifecycleEvidence)) return false;
        BlockLifecycleEvidence value = (BlockLifecycleEvidence) other;
        return placementClaim.equals(value.placementClaim)
                && persistenceClaim.equals(value.persistenceClaim)
                && transitionClaim.equals(value.transitionClaim)
                && dropClaim.equals(value.dropClaim)
                && placementLayer == value.placementLayer
                && persistenceLayer == value.persistenceLayer
                && transitionLayer == value.transitionLayer && dropLayer == value.dropLayer
                && position.equals(value.position) && state.equals(value.state)
                && drops.equals(value.drops) && boundary == value.boundary;
    }

    @Override public int hashCode() {
        return Objects.hash(placementClaim, persistenceClaim, transitionClaim, dropClaim,
                placementLayer, persistenceLayer, transitionLayer, dropLayer,
                position, state, drops, boundary);
    }
}
