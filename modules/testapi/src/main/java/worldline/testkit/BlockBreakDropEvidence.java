package worldline.testkit;

import java.util.List;
import java.util.Objects;
import worldline.api.RemoteItemStack;

/** Equatable evidence for one simple or coupled break/drop contract. */
public final class BlockBreakDropEvidence {
    private final BlockConformanceProfile profile;
    private final ConformanceLayer dropLayer;
    private final int toolItemId;
    private final List<BlockCellTransition> transitions;
    private final List<RemoteItemStack> drops;
    private final String dropContract;

    BlockBreakDropEvidence(BlockConformanceProfile profile, ConformanceLayer dropLayer,
            int toolItemId, List<BlockCellTransition> transitions,
            List<RemoteItemStack> drops, String dropContract) {
        this.profile = profile;
        this.dropLayer = dropLayer;
        this.toolItemId = toolItemId;
        this.transitions = transitions;
        this.drops = drops;
        this.dropContract = dropContract;
    }

    public String subject() { return profile.subject(); }
    public String breakTransitionClaim() { return profile.subject() + "#break-transition"; }
    public String dropMatrixClaim() { return profile.subject() + "#drop-matrix"; }
    public ConformanceLayer breakLayer() { return ConformanceLayer.UNIVERSAL; }
    public ConformanceLayer dropLayer() { return dropLayer; }
    public int toolItemId() { return toolItemId; }
    public List<BlockCellTransition> transitions() { return transitions; }
    public List<RemoteItemStack> drops() { return drops; }
    public String dropContract() { return dropContract; }

    public String canonical() {
        StringBuilder value = new StringBuilder("schema=worldline.block-break-drop-evidence.v1\n");
        value.append("subject=").append(profile.subject()).append('\n');
        value.append("claim.break-transition=").append(profile.subject())
                .append("#break-transition|UNIVERSAL\n");
        value.append("claim.drop-matrix=").append(profile.subject())
                .append("#drop-matrix|").append(dropLayer).append('\n');
        value.append("tool=").append(toolItemId).append('\n');
        value.append("transitions=");
        for (int index = 0; index < transitions.size(); index++) {
            if (index > 0) value.append(',');
            value.append(transitions.get(index).canonical());
        }
        value.append("\ndrop-contract=").append(dropContract).append("\ndrops=");
        for (int index = 0; index < drops.size(); index++) {
            if (index > 0) value.append(',');
            RemoteItemStack item = drops.get(index);
            value.append(item.legacyId()).append(':').append(item.count())
                    .append(':').append(item.damage());
        }
        return value.append('\n').toString();
    }

    @Override public boolean equals(Object other) {
        if (!(other instanceof BlockBreakDropEvidence)) return false;
        BlockBreakDropEvidence value = (BlockBreakDropEvidence) other;
        return profile.subject().equals(value.profile.subject())
                && dropLayer == value.dropLayer && toolItemId == value.toolItemId
                && transitions.equals(value.transitions) && drops.equals(value.drops)
                && dropContract.equals(value.dropContract);
    }

    @Override public int hashCode() {
        return Objects.hash(profile.subject(), dropLayer, toolItemId,
                transitions, drops, dropContract);
    }
}
