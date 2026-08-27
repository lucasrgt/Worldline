package worldline.testkit;

import java.util.List;
import java.util.Objects;
import worldline.api.BlockFace;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.RemoteItemStack;

/** One complete gameplay lifecycle routed through four Functional Census claims. */
public final class BlockLifecycleScenario {
    private final BlockConformanceCase placement, persistence, transition, drops;
    private final BlockPosition support;
    private final BlockFace face;
    private final BlockState placedState;
    private final BlockLifecycleSlot placementSlot, breakSlot;
    private final List<RemoteItemStack> expectedDrops;
    private final int breakTicks, observationTicks;

    public BlockLifecycleScenario(BlockConformanceCase placement,
            BlockConformanceCase persistence, BlockConformanceCase transition,
            BlockConformanceCase drops, BlockPosition support, BlockFace face,
            BlockState placedState, BlockLifecycleSlot placementSlot,
            BlockLifecycleSlot breakSlot, List<RemoteItemStack> expectedDrops,
            int breakTicks, int observationTicks) {
        require(placement, "gameplay-placement");
        require(persistence, "save-reload");
        require(transition, "break-transition");
        require(drops, "drop-matrix");
        String subject = placement.profile().subject();
        if (!persistence.profile().subject().equals(subject)
                || !transition.profile().subject().equals(subject)
                || !drops.profile().subject().equals(subject)) {
            throw new IllegalArgumentException("lifecycle claims target different subjects");
        }
        if (breakTicks < 0 || observationTicks < 1) {
            throw new IllegalArgumentException("invalid lifecycle tick bounds");
        }
        this.placement = placement;
        this.persistence = persistence;
        this.transition = transition;
        this.drops = drops;
        this.support = Objects.requireNonNull(support, "support");
        this.face = Objects.requireNonNull(face, "face");
        this.placedState = Objects.requireNonNull(placedState, "placedState");
        if (placedState.legacyId() == 0) throw new IllegalArgumentException("placed state is air");
        this.placementSlot = Objects.requireNonNull(placementSlot, "placementSlot");
        this.breakSlot = Objects.requireNonNull(breakSlot, "breakSlot");
        if (expectedDrops == null || expectedDrops.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("invalid expected drops");
        }
        this.expectedDrops = List.copyOf(expectedDrops);
        this.breakTicks = breakTicks;
        this.observationTicks = observationTicks;
    }

    public BlockConformanceCase placement() { return placement; }
    public BlockConformanceCase persistence() { return persistence; }
    public BlockConformanceCase transition() { return transition; }
    public BlockConformanceCase drops() { return drops; }
    public BlockPosition support() { return support; }
    public BlockPosition target() { return face.adjacent(support); }
    public BlockFace face() { return face; }
    public BlockState placedState() { return placedState; }
    public BlockLifecycleSlot placementSlot() { return placementSlot; }
    public BlockLifecycleSlot breakSlot() { return breakSlot; }
    public List<RemoteItemStack> expectedDrops() { return expectedDrops; }
    public int breakTicks() { return breakTicks; }
    public int observationTicks() { return observationTicks; }

    private static void require(BlockConformanceCase claim, String template) {
        if (claim == null || !claim.template().id().equals(template)) {
            throw new IllegalArgumentException("claim does not target " + template);
        }
    }
}
