package worldline.testkit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import worldline.api.BlockFace;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.RemoteItemStack;

/** Public data row for one complete gameplay lifecycle and its four census claims. */
public final class BlockLifecycleScenario {
    private final String id;
    private final BlockConformanceCase placement, persistence, transition, drops;
    private final BlockPosition support;
    private final BlockState supportState, overheadState;
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
        this(defaultId(placement), placement, persistence, transition, drops, support, null,
                null, face, placedState, placementSlot, breakSlot, expectedDrops,
                breakTicks, observationTicks);
    }

    public BlockLifecycleScenario(String id, BlockConformanceCase placement,
            BlockConformanceCase persistence, BlockConformanceCase transition,
            BlockConformanceCase drops, BlockPosition support, BlockState supportState,
            BlockFace face, BlockState placedState, BlockLifecycleSlot placementSlot,
            BlockLifecycleSlot breakSlot, List<RemoteItemStack> expectedDrops,
            int breakTicks, int observationTicks) {
        this(id, placement, persistence, transition, drops, support, supportState, null,
                face, placedState, placementSlot, breakSlot, expectedDrops,
                breakTicks, observationTicks);
    }

    public BlockLifecycleScenario(String id, BlockConformanceCase placement,
            BlockConformanceCase persistence, BlockConformanceCase transition,
            BlockConformanceCase drops, BlockPosition support, BlockState supportState,
            BlockState overheadState, BlockFace face, BlockState placedState,
            BlockLifecycleSlot placementSlot, BlockLifecycleSlot breakSlot,
            List<RemoteItemStack> expectedDrops, int breakTicks, int observationTicks) {
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
        if (id == null || !id.matches("[a-z0-9][a-z0-9._-]{0,127}")) {
            throw new IllegalArgumentException("invalid lifecycle scenario id");
        }
        this.id = id;
        this.placement = placement;
        this.persistence = persistence;
        this.transition = transition;
        this.drops = drops;
        this.support = Objects.requireNonNull(support, "support");
        this.supportState = supportState;
        this.overheadState = overheadState;
        this.face = Objects.requireNonNull(face, "face");
        this.placedState = Objects.requireNonNull(placedState, "placedState");
        if (placedState.legacyId() == 0) throw new IllegalArgumentException("placed state is air");
        this.placementSlot = Objects.requireNonNull(placementSlot, "placementSlot");
        this.breakSlot = Objects.requireNonNull(breakSlot, "breakSlot");
        if (expectedDrops == null || expectedDrops.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("invalid expected drops");
        }
        this.expectedDrops = Collections.unmodifiableList(
                new ArrayList<RemoteItemStack>(expectedDrops));
        this.breakTicks = breakTicks;
        this.observationTicks = observationTicks;
    }

    public static BlockLifecycleScenario from(String id, BlockConformancePlan plan,
            String subject, BlockPosition support, BlockState supportState, BlockFace face,
            BlockState placedState, BlockLifecycleSlot placementSlot,
            BlockLifecycleSlot breakSlot, List<RemoteItemStack> expectedDrops,
            int breakTicks, int observationTicks) {
        return from(id, plan, subject, support, supportState, null, face, placedState,
                placementSlot, breakSlot, expectedDrops, breakTicks, observationTicks);
    }

    public static BlockLifecycleScenario from(String id, BlockConformancePlan plan,
            String subject, BlockPosition support, BlockState supportState,
            BlockState overheadState, BlockFace face, BlockState placedState,
            BlockLifecycleSlot placementSlot, BlockLifecycleSlot breakSlot,
            List<RemoteItemStack> expectedDrops, int breakTicks, int observationTicks) {
        if (plan == null) throw new NullPointerException("plan");
        return new BlockLifecycleScenario(id,
                plan.caseFor(subject, "gameplay-placement"),
                plan.caseFor(subject, "save-reload"),
                plan.caseFor(subject, "break-transition"),
                plan.caseFor(subject, "drop-matrix"), support, supportState, overheadState,
                face, placedState, placementSlot, breakSlot, expectedDrops,
                breakTicks, observationTicks);
    }

    public String id() { return id; }
    public String subject() { return placement.profile().subject(); }
    public BlockConformanceCase placement() { return placement; }
    public BlockConformanceCase persistence() { return persistence; }
    public BlockConformanceCase transition() { return transition; }
    public BlockConformanceCase drops() { return drops; }
    public BlockPosition support() { return support; }
    public BlockState supportState() { return supportState; }
    public BlockPosition target() { return face.adjacent(support); }
    public BlockPosition overhead() { return BlockFace.UP.adjacent(target()); }
    public BlockState overheadState() { return overheadState; }
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

    private static String defaultId(BlockConformanceCase placement) {
        if (placement == null) throw new IllegalArgumentException("placement claim is absent");
        return placement.profile().subject().replace(':', '-').replace('/', '-');
    }
}
