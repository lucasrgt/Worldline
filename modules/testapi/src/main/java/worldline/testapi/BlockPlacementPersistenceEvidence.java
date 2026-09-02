package worldline.testapi;

import java.util.List;
import java.util.Objects;
import worldline.api.BlockLifecycleDriver.ReloadBoundary;

/** Equatable evidence for item-driven placement followed by a reload boundary. */
public final class BlockPlacementPersistenceEvidence {
    private final BlockConformanceProfile profile;
    private final int itemId, beforeCount, afterCount, placements;
    private final List<BlockStateCell> cells;
    private final ReloadBoundary boundary;

    public BlockPlacementPersistenceEvidence(BlockConformanceProfile profile, int itemId,
            int beforeCount, int afterCount, int placements, List<BlockStateCell> cells,
            ReloadBoundary boundary) {
        this.profile = profile;
        this.itemId = itemId;
        this.beforeCount = beforeCount;
        this.afterCount = afterCount;
        this.placements = placements;
        this.cells = cells;
        this.boundary = boundary;
    }

    public String subject() { return profile.subject(); }
    public String gameplayPlacementClaim() {
        return profile.subject() + "#gameplay-placement";
    }
    public String saveReloadClaim() { return profile.subject() + "#save-reload"; }
    public int placementItemId() { return itemId; }
    public int itemCountBefore() { return beforeCount; }
    public int itemCountAfter() { return afterCount; }
    public int placements() { return placements; }
    public List<BlockStateCell> cells() { return cells; }
    public ReloadBoundary boundary() { return boundary; }

    public String canonical() {
        StringBuilder value = new StringBuilder(
                "schema=worldline.block-placement-persistence-evidence.v1\n");
        value.append("subject=").append(profile.subject()).append('\n');
        value.append("claim.gameplay-placement=").append(profile.subject())
                .append("#gameplay-placement|UNIVERSAL\n");
        value.append("claim.save-reload=").append(profile.subject())
                .append("#save-reload|UNIVERSAL\n");
        value.append("item=").append(itemId).append(':').append(beforeCount).append("->")
                .append(afterCount).append(";placements=").append(placements).append('\n');
        value.append("cells=");
        for (int index = 0; index < cells.size(); index++) {
            if (index > 0) value.append(',');
            value.append(cells.get(index).canonical());
        }
        return value.append("\nreload=").append(boundary).append('\n').toString();
    }

    @Override public boolean equals(Object other) {
        if (!(other instanceof BlockPlacementPersistenceEvidence)) return false;
        BlockPlacementPersistenceEvidence value = (BlockPlacementPersistenceEvidence) other;
        return profile.subject().equals(value.profile.subject()) && itemId == value.itemId
                && beforeCount == value.beforeCount && afterCount == value.afterCount
                && placements == value.placements && cells.equals(value.cells)
                && boundary == value.boundary;
    }

    @Override public int hashCode() {
        return Objects.hash(profile.subject(), itemId, beforeCount, afterCount,
                placements, cells, boundary);
    }
}
