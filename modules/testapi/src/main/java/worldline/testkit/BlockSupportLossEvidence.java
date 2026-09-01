package worldline.testkit;

import java.util.Objects;
import worldline.api.BlockLifecycleDriver.ReloadBoundary;
import worldline.api.BlockPosition;
import worldline.api.BlockState;

/** Canonical evidence for one support-dependent stability and removal row. */
public final class BlockSupportLossEvidence {
    private final BlockSupportLossScenario scenario;
    private final ReloadBoundary boundary;

    BlockSupportLossEvidence(BlockSupportLossScenario scenario, ReloadBoundary boundary) {
        this.scenario = Objects.requireNonNull(scenario, "scenario");
        this.boundary = Objects.requireNonNull(boundary, "boundary");
    }

    public String scenarioId() { return scenario.id(); }
    public String subject() { return scenario.subject(); }
    public ReloadBoundary boundary() { return boundary; }

    public String canonical() {
        StringBuilder value = new StringBuilder();
        value.append("schema=worldline.block-support-loss-evidence.v1\n");
        value.append("scenario=").append(scenario.id()).append('\n');
        value.append("subject=").append(scenario.subject()).append('\n');
        claim(value, "tick-policy", scenario.tickPolicy());
        claim(value, "neighbor-response", scenario.neighborResponse());
        value.append("support=").append(position(scenario.support())).append(':')
                .append(state(scenario.supportState())).append("->0:0\n");
        value.append("target=").append(position(scenario.target())).append(':')
                .append(state(scenario.targetState())).append("->0:0\n");
        value.append("tick-window=").append(scenario.tickWindow()).append('\n');
        value.append("support-break-ticks=").append(scenario.breakTicks()).append('\n');
        value.append("support-observation-ticks=")
                .append(scenario.observationTicks()).append('\n');
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
