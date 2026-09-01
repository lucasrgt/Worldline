package worldline.testkit;

import java.util.Objects;

/** Equatable public evidence for one tick-policy case. */
public final class BlockTickPolicyEvidence {
    private final BlockTickPolicyScenario scenario;

    BlockTickPolicyEvidence(BlockTickPolicyScenario scenario) {
        this.scenario = Objects.requireNonNull(scenario, "scenario");
    }

    public String id() { return scenario.id(); }
    public String subject() { return scenario.claim().profile().subject(); }
    public ConformanceLayer layer() { return scenario.claim().layer(); }

    public String canonical() {
        return "scenario=" + scenario.id() + '\n'
                + "claim=" + scenario.claim().claimId() + '|' + scenario.claim().layer() + '\n'
                + "mechanism=" + scenario.mechanism() + '\n'
                + "initial=" + scenario.initial() + '\n'
                + "effect=" + scenario.effect() + '\n'
                + "persisted=" + scenario.persisted() + '\n';
    }
}
