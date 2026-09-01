package worldline.testkit;

import java.util.Objects;
import worldline.api.BlockFace;
import worldline.api.BlockPosition;
import worldline.api.BlockState;

/** Public row for gameplay source placement followed by causal horizontal flow. */
public final class FluidDynamicsScenario {
    private final String id;
    private final BlockConformanceCase placement, persistence, tickPolicy, neighborResponse;
    private final BlockPosition support;
    private final BlockState supportState, sourceState, gateState, flowState;
    private final BlockLifecycleSlot sourceSlot, gateToolSlot;
    private final int settleTicks, breakTicks, flowTicks;

    public FluidDynamicsScenario(String id, BlockConformanceCase placement,
            BlockConformanceCase persistence, BlockConformanceCase tickPolicy,
            BlockConformanceCase neighborResponse, BlockPosition support,
            BlockState supportState, BlockState sourceState, BlockState gateState,
            BlockState flowState, BlockLifecycleSlot sourceSlot,
            BlockLifecycleSlot gateToolSlot, int settleTicks, int breakTicks, int flowTicks) {
        require(placement, "gameplay-placement");
        require(persistence, "save-reload");
        require(tickPolicy, "tick-policy");
        require(neighborResponse, "neighbor-response");
        String subject = placement.profile().subject();
        if (!persistence.profile().subject().equals(subject)
                || !tickPolicy.profile().subject().equals(subject)
                || !neighborResponse.profile().subject().equals(subject)) {
            throw new IllegalArgumentException("fluid claims target different subjects");
        }
        if (id == null || !id.matches("[a-z0-9][a-z0-9._-]{0,127}")) {
            throw new IllegalArgumentException("invalid fluid dynamics scenario id");
        }
        if (settleTicks < 1 || settleTicks > 1_200 || breakTicks < 0
                || flowTicks < 1 || flowTicks > 1_200) {
            throw new IllegalArgumentException("invalid fluid dynamics tick bounds");
        }
        this.id = id; this.placement = placement; this.persistence = persistence;
        this.tickPolicy = tickPolicy; this.neighborResponse = neighborResponse;
        this.support = Objects.requireNonNull(support, "support");
        this.supportState = nonAir(supportState, "supportState");
        this.sourceState = nonAir(sourceState, "sourceState");
        this.gateState = nonAir(gateState, "gateState");
        this.flowState = nonAir(flowState, "flowState");
        this.sourceSlot = Objects.requireNonNull(sourceSlot, "sourceSlot");
        this.gateToolSlot = Objects.requireNonNull(gateToolSlot, "gateToolSlot");
        if (sourceSlot.hotbarSlot() == gateToolSlot.hotbarSlot()) {
            throw new IllegalArgumentException("fluid dynamics slots overlap");
        }
        this.settleTicks = settleTicks; this.breakTicks = breakTicks; this.flowTicks = flowTicks;
    }

    public String id() { return id; }
    public String subject() { return placement.profile().subject(); }
    public BlockConformanceCase placement() { return placement; }
    public BlockConformanceCase persistence() { return persistence; }
    public BlockConformanceCase tickPolicy() { return tickPolicy; }
    public BlockConformanceCase neighborResponse() { return neighborResponse; }
    public BlockPosition support() { return support; }
    public BlockPosition source() { return BlockFace.UP.adjacent(support); }
    public BlockPosition flowSupport() { return BlockFace.EAST.adjacent(support); }
    public BlockPosition flow() { return BlockFace.UP.adjacent(flowSupport()); }
    public BlockState supportState() { return supportState; }
    public BlockState sourceState() { return sourceState; }
    public BlockState gateState() { return gateState; }
    public BlockState flowState() { return flowState; }
    public BlockLifecycleSlot sourceSlot() { return sourceSlot; }
    public BlockLifecycleSlot gateToolSlot() { return gateToolSlot; }
    public int settleTicks() { return settleTicks; }
    public int breakTicks() { return breakTicks; }
    public int flowTicks() { return flowTicks; }

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
