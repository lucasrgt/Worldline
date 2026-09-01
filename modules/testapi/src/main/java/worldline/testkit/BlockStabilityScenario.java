package worldline.testkit;

import java.util.Objects;
import worldline.api.BlockFace;
import worldline.api.BlockPosition;
import worldline.api.BlockState;

/** Public row for bounded tick stability followed by causal overhead-neighbor removal. */
public final class BlockStabilityScenario {
    private final String id;
    private final BlockConformanceCase tickPolicy, neighborResponse;
    private final BlockPosition support;
    private final BlockState supportState, targetState, overheadState;
    private final BlockLifecycleSlot placementSlot, breakSlot;
    private final int tickWindow, breakTicks, observationTicks;

    public BlockStabilityScenario(String id, BlockConformanceCase tickPolicy,
            BlockConformanceCase neighborResponse, BlockPosition support,
            BlockState supportState, BlockState targetState, BlockState overheadState,
            BlockLifecycleSlot placementSlot, BlockLifecycleSlot breakSlot,
            int tickWindow, int breakTicks, int observationTicks) {
        require(tickPolicy, "tick-policy");
        require(neighborResponse, "neighbor-response");
        if (!tickPolicy.profile().subject().equals(neighborResponse.profile().subject())) {
            throw new IllegalArgumentException("stability claims target different subjects");
        }
        if (id == null || !id.matches("[a-z0-9][a-z0-9._-]{0,127}")) {
            throw new IllegalArgumentException("invalid stability scenario id");
        }
        if (tickWindow < 1 || tickWindow > 24_000 || breakTicks < 0
                || observationTicks < 1 || observationTicks > 1_200) {
            throw new IllegalArgumentException("invalid stability tick bounds");
        }
        this.id = id;
        this.tickPolicy = tickPolicy;
        this.neighborResponse = neighborResponse;
        this.support = Objects.requireNonNull(support, "support");
        this.supportState = nonAir(supportState, "supportState");
        this.targetState = nonAir(targetState, "targetState");
        this.overheadState = nonAir(overheadState, "overheadState");
        this.placementSlot = Objects.requireNonNull(placementSlot, "placementSlot");
        this.breakSlot = Objects.requireNonNull(breakSlot, "breakSlot");
        this.tickWindow = tickWindow;
        this.breakTicks = breakTicks;
        this.observationTicks = observationTicks;
        if (placementSlot.hotbarSlot() == breakSlot.hotbarSlot()) {
            throw new IllegalArgumentException("stability slots overlap");
        }
    }

    public String id() { return id; }
    public String subject() { return tickPolicy.profile().subject(); }
    public BlockConformanceCase tickPolicy() { return tickPolicy; }
    public BlockConformanceCase neighborResponse() { return neighborResponse; }
    public BlockPosition support() { return support; }
    public BlockPosition target() { return BlockFace.UP.adjacent(support); }
    public BlockPosition overhead() { return BlockFace.UP.adjacent(target()); }
    public BlockState supportState() { return supportState; }
    public BlockState targetState() { return targetState; }
    public BlockState overheadState() { return overheadState; }
    public BlockLifecycleSlot placementSlot() { return placementSlot; }
    public BlockLifecycleSlot breakSlot() { return breakSlot; }
    public int tickWindow() { return tickWindow; }
    public int breakTicks() { return breakTicks; }
    public int observationTicks() { return observationTicks; }

    private static BlockState nonAir(BlockState state, String role) {
        Objects.requireNonNull(state, role);
        if (state.legacyId() == 0) throw new IllegalArgumentException(role + " is air");
        return state;
    }

    private static void require(BlockConformanceCase claim, String template) {
        if (claim == null || !claim.template().id().equals(template)) {
            throw new IllegalArgumentException("claim does not target " + template);
        }
    }
}
