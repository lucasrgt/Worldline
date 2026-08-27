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
    private final BlockPosition support, position;
    private final BlockState supportState, state;
    private final List<RemoteItemStack> drops;
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
        this.drops = Collections.unmodifiableList(new ArrayList<RemoteItemStack>(drops));
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
        value.append("target=").append(position(position)).append('\n');
        value.append("placed=").append(state(state)).append('\n');
        value.append("drops=");
        for (int index = 0; index < drops.size(); index++) {
            if (index > 0) value.append(',');
            RemoteItemStack item = drops.get(index);
            value.append(item.legacyId()).append(':').append(item.count())
                    .append(':').append(item.damage());
        }
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
                && position.equals(value.position) && state.equals(value.state)
                && drops.equals(value.drops) && boundary == value.boundary;
    }

    @Override public int hashCode() {
        return Objects.hash(scenarioId, subject, placementClaim, persistenceClaim, transitionClaim, dropClaim,
                placementLayer, persistenceLayer, transitionLayer, dropLayer,
                support, supportState, position, state, drops, boundary);
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
}
