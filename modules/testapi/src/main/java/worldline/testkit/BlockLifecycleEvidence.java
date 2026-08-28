package worldline.testkit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import worldline.api.BlockLifecycleDriver.ReloadBoundary;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.RemoteItemStack;

/** Public canonical lifecycle result with transient entity details normalized away. */
public final class BlockLifecycleEvidence {
    private final String scenarioId, subject;
    private final String placementClaim, persistenceClaim, transitionClaim, dropClaim;
    private final ConformanceLayer placementLayer, persistenceLayer, transitionLayer, dropLayer;
    private final BlockPosition support, position, overhead, neighbor;
    private final BlockState supportState, state, overheadState, neighborState;
    private final List<RemoteItemStack> drops;
    private final String canonicalDrops;
    private final ReloadBoundary boundary;

    BlockLifecycleEvidence(BlockLifecycleScenario scenario, List<RemoteItemStack> drops,
            ReloadBoundary boundary) {
        scenarioId = scenario.id();
        subject = scenario.subject();
        placementClaim = scenario.placement().claimId();
        persistenceClaim = scenario.persistence().claimId();
        transitionClaim = scenario.transition().claimId();
        dropClaim = scenario.drops().claimId();
        placementLayer = scenario.placement().layer();
        persistenceLayer = scenario.persistence().layer();
        transitionLayer = scenario.transition().layer();
        dropLayer = scenario.drops().layer();
        support = scenario.support();
        supportState = scenario.supportState();
        position = scenario.target();
        state = scenario.placedState();
        overhead = scenario.overhead();
        overheadState = scenario.overheadState();
        neighbor = scenario.neighborPosition();
        neighborState = scenario.neighbor() == null ? null : scenario.neighbor().state();
        this.drops = Collections.unmodifiableList(new ArrayList<RemoteItemStack>(drops));
        canonicalDrops = scenario.dropMatrix().exact()
                ? items(drops) : scenario.dropMatrix().canonical();
        this.boundary = Objects.requireNonNull(boundary, "boundary");
    }

    public String scenarioId() { return scenarioId; }
    public String subject() { return subject; }
    public String placementClaim() { return placementClaim; }
    public String persistenceClaim() { return persistenceClaim; }
    public String transitionClaim() { return transitionClaim; }
    public String dropClaim() { return dropClaim; }
    public ConformanceLayer placementLayer() { return placementLayer; }
    public ConformanceLayer persistenceLayer() { return persistenceLayer; }
    public ConformanceLayer transitionLayer() { return transitionLayer; }
    public ConformanceLayer dropLayer() { return dropLayer; }
    public BlockPosition support() { return support; }
    public BlockState supportState() { return supportState; }
    public BlockPosition position() { return position; }
    public BlockState state() { return state; }
    public BlockPosition overhead() { return overhead; }
    public BlockState overheadState() { return overheadState; }
    public BlockPosition neighbor() { return neighbor; }
    public BlockState neighborState() { return neighborState; }
    public List<RemoteItemStack> drops() { return drops; }
    public ReloadBoundary boundary() { return boundary; }

    public String canonical() {
        StringBuilder value = new StringBuilder();
        value.append("schema=worldline.block-lifecycle-evidence.v1\n");
        value.append("scenario=").append(scenarioId).append('\n');
        value.append("subject=").append(subject).append('\n');
        claim(value, "gameplay-placement", placementClaim, placementLayer);
        claim(value, "save-reload", persistenceClaim, persistenceLayer);
        claim(value, "break-transition", transitionClaim, transitionLayer);
        claim(value, "drop-matrix", dropClaim, dropLayer);
        value.append("support=").append(position(support)).append(':')
                .append(state(supportState)).append('\n');
        if (overheadState != null) value.append("overhead=").append(position(overhead))
                .append(':').append(state(overheadState)).append('\n');
        if (neighborState != null) value.append("neighbor=").append(position(neighbor))
                .append(':').append(state(neighborState)).append('\n');
        value.append("target=").append(position(position)).append('\n');
        value.append("placed=").append(state(state)).append('\n');
        value.append("drops=").append(canonicalDrops);
        return value.append("\nreload=").append(boundary).append('\n').toString();
    }

    @Override public boolean equals(Object other) {
        if (!(other instanceof BlockLifecycleEvidence)) return false;
        BlockLifecycleEvidence value = (BlockLifecycleEvidence) other;
        return scenarioId.equals(value.scenarioId) && subject.equals(value.subject)
                && placementClaim.equals(value.placementClaim)
                && persistenceClaim.equals(value.persistenceClaim)
                && transitionClaim.equals(value.transitionClaim)
                && dropClaim.equals(value.dropClaim)
                && placementLayer == value.placementLayer
                && persistenceLayer == value.persistenceLayer
                && transitionLayer == value.transitionLayer && dropLayer == value.dropLayer
                && support.equals(value.support) && Objects.equals(supportState, value.supportState)
                && overhead.equals(value.overhead)
                && Objects.equals(overheadState, value.overheadState)
                && Objects.equals(neighbor, value.neighbor)
                && Objects.equals(neighborState, value.neighborState)
                && position.equals(value.position) && state.equals(value.state)
                && drops.equals(value.drops) && canonicalDrops.equals(value.canonicalDrops)
                && boundary == value.boundary;
    }

    @Override public int hashCode() {
        return Objects.hash(scenarioId, subject, placementClaim, persistenceClaim, transitionClaim, dropClaim,
                placementLayer, persistenceLayer, transitionLayer, dropLayer,
                support, supportState, overhead, overheadState, neighbor, neighborState,
                position, state, drops, canonicalDrops, boundary);
    }

    private static void claim(StringBuilder value, String template, String claim,
            ConformanceLayer layer) {
        value.append("claim.").append(template).append('=').append(claim)
                .append('|').append(layer).append('\n');
    }

    private static String position(BlockPosition value) {
        return value.x() + ":" + value.y() + ":" + value.z();
    }

    private static String state(BlockState value) {
        return value == null ? "unverified" : value.legacyId() + ":" + value.metadata();
    }

    private static String items(List<RemoteItemStack> drops) {
        StringBuilder value = new StringBuilder();
        for (RemoteItemStack item : drops) {
            if (value.length() > 0) value.append(',');
            value.append(item.legacyId()).append(':').append(item.count())
                    .append(':').append(item.damage());
        }
        return value.toString();
    }
}
