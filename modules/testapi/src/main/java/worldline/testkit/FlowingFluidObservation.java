package worldline.testkit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import worldline.api.BlockState;

/** Immutable observation row for one native moving-fluid block. */
public final class FlowingFluidObservation {
    private final int movingId;
    private final List<Integer> metadataDomain;
    private final int firstFlowTick;
    private final BlockState blocked, recomputed, saved, reloaded;
    private final boolean passable;
    private final int opacity, emission, blockLight, skyLight;

    public FlowingFluidObservation(int movingId, List<Integer> metadataDomain,
            int firstFlowTick, BlockState blocked, BlockState recomputed,
            boolean passable, int opacity, int emission, int blockLight,
            int skyLight, BlockState saved, BlockState reloaded) {
        if (movingId < 1 || movingId > 255 || metadataDomain == null
                || metadataDomain.isEmpty() || firstFlowTick < 1 || firstFlowTick > 1_200) {
            throw new IllegalArgumentException("invalid flowing-fluid observation");
        }
        List<Integer> copy = new ArrayList<Integer>(metadataDomain);
        Set<Integer> distinct = new HashSet<Integer>();
        int previous = -1;
        for (Integer metadata : copy) {
            if (metadata == null || metadata < 0 || metadata > 15
                    || metadata <= previous || !distinct.add(metadata)) {
                throw new IllegalArgumentException("invalid flowing-fluid metadata domain");
            }
            previous = metadata;
        }
        if (opacity < 0 || opacity > 255 || emission < 0 || emission > 15
                || blockLight < 0 || blockLight > 15 || skyLight < 0 || skyLight > 15) {
            throw new IllegalArgumentException("invalid flowing-fluid light observation");
        }
        this.movingId = movingId;
        this.metadataDomain = Collections.unmodifiableList(copy);
        this.firstFlowTick = firstFlowTick;
        this.blocked = Objects.requireNonNull(blocked, "blocked");
        this.recomputed = Objects.requireNonNull(recomputed, "recomputed");
        this.passable = passable;
        this.opacity = opacity; this.emission = emission;
        this.blockLight = blockLight; this.skyLight = skyLight;
        this.saved = Objects.requireNonNull(saved, "saved");
        this.reloaded = Objects.requireNonNull(reloaded, "reloaded");
    }

    public int movingId() { return movingId; }
    public List<Integer> metadataDomain() { return metadataDomain; }
    public int firstFlowTick() { return firstFlowTick; }
    public BlockState blocked() { return blocked; }
    public BlockState recomputed() { return recomputed; }
    public boolean passable() { return passable; }
    public int opacity() { return opacity; }
    public int emission() { return emission; }
    public int blockLight() { return blockLight; }
    public int skyLight() { return skyLight; }
    public BlockState saved() { return saved; }
    public BlockState reloaded() { return reloaded; }

    @Override public boolean equals(Object other) {
        if (!(other instanceof FlowingFluidObservation)) return false;
        FlowingFluidObservation value = (FlowingFluidObservation) other;
        return movingId == value.movingId && firstFlowTick == value.firstFlowTick
                && passable == value.passable && opacity == value.opacity
                && emission == value.emission && blockLight == value.blockLight
                && skyLight == value.skyLight && metadataDomain.equals(value.metadataDomain)
                && blocked.equals(value.blocked) && recomputed.equals(value.recomputed)
                && saved.equals(value.saved) && reloaded.equals(value.reloaded);
    }

    @Override public int hashCode() {
        return Objects.hash(movingId, metadataDomain, firstFlowTick, blocked,
                recomputed, passable, opacity, emission, blockLight, skyLight,
                saved, reloaded);
    }
}
