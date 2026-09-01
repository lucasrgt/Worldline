package worldline.testkit;

import java.util.Objects;
import worldline.api.BlockLifecycleDriver.ReloadBoundary;
import worldline.api.BlockPosition;
import worldline.api.BlockState;

/** Canonical evidence for source placement, gate release, propagation, and reload. */
public final class FluidDynamicsEvidence {
    private final FluidDynamicsScenario scenario;
    private final ReloadBoundary boundary;

    FluidDynamicsEvidence(FluidDynamicsScenario scenario, ReloadBoundary boundary) {
        this.scenario = Objects.requireNonNull(scenario, "scenario");
        this.boundary = Objects.requireNonNull(boundary, "boundary");
    }

    public String scenarioId() { return scenario.id(); }
    public String subject() { return scenario.subject(); }
    public ReloadBoundary boundary() { return boundary; }

    public String canonical() {
        StringBuilder value = new StringBuilder();
        value.append("schema=worldline.fluid-dynamics-evidence.v1\n");
        value.append("scenario=").append(scenario.id()).append('\n');
        value.append("subject=").append(scenario.subject()).append('\n');
        claim(value, "gameplay-placement", scenario.placement());
        claim(value, "save-reload", scenario.persistence());
        claim(value, "tick-policy", scenario.tickPolicy());
        claim(value, "neighbor-response", scenario.neighborResponse());
        value.append("source=").append(position(scenario.source())).append(':')
                .append(state(scenario.sourceState())).append('\n');
        value.append("gate=").append(position(scenario.flow())).append(':')
                .append(state(scenario.gateState())).append("->0:0->")
                .append(state(scenario.flowState())).append('\n');
        value.append("settle-ticks=").append(scenario.settleTicks()).append('\n');
        value.append("gate-break-ticks=").append(scenario.breakTicks()).append('\n');
        value.append("flow-ticks=").append(scenario.flowTicks()).append('\n');
        return value.append("reload=").append(boundary).append('\n').toString();
    }

    private static void claim(StringBuilder value, String template,
            BlockConformanceCase claim) {
        value.append("claim.").append(template).append('=').append(claim.claimId())
                .append('|').append(claim.layer()).append('\n');
    }

    private static String position(BlockPosition value) {
        return value.x() + ":" + value.y() + ":" + value.z();
    }

    private static String state(BlockState value) {
        return value.legacyId() + ":" + value.metadata();
    }
}
