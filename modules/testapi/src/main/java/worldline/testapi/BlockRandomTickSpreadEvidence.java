package worldline.testapi;

import java.util.Objects;
import worldline.api.BlockLifecycleDriver.ReloadBoundary;

/** Equatable evidence for a bounded random-tick spread row. */
public final class BlockRandomTickSpreadEvidence {
    private final BlockRandomTickSpreadScenario scenario;
    private final ReloadBoundary boundary;

    public BlockRandomTickSpreadEvidence(BlockRandomTickSpreadScenario scenario,
            ReloadBoundary boundary) {
        this.scenario = Objects.requireNonNull(scenario, "scenario");
        this.boundary = Objects.requireNonNull(boundary, "boundary");
    }

    public String canonical() {
        StringBuilder value = new StringBuilder();
        value.append("schema=worldline.block-random-tick-spread-evidence.v1\n");
        value.append("scenario=").append(scenario.id()).append('\n');
        value.append("subject=").append(scenario.subject()).append('\n');
        for (String template : new String[] {"state-domain", "collision-shape",
                "light-behavior", "tick-policy", "neighbor-response"}) {
            BlockConformanceCase claim = scenario.claim(template);
            value.append("claim.").append(template).append('=').append(claim.claimId())
                    .append('|').append(claim.layer()).append('\n');
        }
        value.append("state=").append(state()).append(";domain=singleton\n");
        value.append("sources=").append(scenario.sources().size()).append(";targets=")
                .append(scenario.targets().size()).append('\n');
        value.append("collision=PASSABLE\n");
        value.append("light=block:").append(scenario.blockLight()).append("->")
                .append(scenario.blockLight()).append(";sky:").append(scenario.skyLight())
                .append("->").append(scenario.skyLight()).append('\n');
        value.append("tick=windows<=").append(scenario.maxWindows()).append("x")
                .append(scenario.windowTicks()).append(";transition=0:0->")
                .append(state()).append(";winning-window=excluded\n");
        value.append("control=invalid-support-air\n");
        value.append("neighbor=support-remove;break-ticks=").append(scenario.breakTicks())
                .append(";source=").append(state()).append("->0:0\n");
        return value.append("reload=").append(boundary).append('\n').toString();
    }

    private String state() {
        return scenario.state().legacyId() + ":" + scenario.state().metadata();
    }
}
