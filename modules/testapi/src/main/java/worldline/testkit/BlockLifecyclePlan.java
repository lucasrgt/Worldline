package worldline.testkit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import worldline.api.BlockLifecycleDriver;
import worldline.test.Worldline;

/** Registers validated lifecycle data rows as executable Worldline tests. */
public final class BlockLifecyclePlan {
    public static final String EVIDENCE_ARTIFACT = "block-lifecycle.properties";

    private final String runtimeId;
    private final List<BlockLifecycleScenario> scenarios;

    public BlockLifecyclePlan(String runtimeId, List<BlockLifecycleScenario> scenarios) {
        if (runtimeId == null || !runtimeId.matches("[A-Za-z0-9._-]{1,64}")) {
            throw new IllegalArgumentException("invalid lifecycle runtime");
        }
        if (scenarios == null || scenarios.isEmpty()) {
            throw new IllegalArgumentException("lifecycle scenarios are empty");
        }
        List<BlockLifecycleScenario> copy = new ArrayList<BlockLifecycleScenario>(scenarios);
        Set<String> ids = new HashSet<String>();
        for (BlockLifecycleScenario scenario : copy) {
            if (scenario == null) throw new IllegalArgumentException("null lifecycle scenario");
            if (scenario.supportState() == null) {
                throw new IllegalArgumentException(
                        "executable lifecycle scenario has no support-state precondition: "
                                + scenario.id());
            }
            if (!ids.add(scenario.id())) {
                throw new IllegalArgumentException("duplicate lifecycle scenario: " + scenario.id());
            }
        }
        this.runtimeId = runtimeId;
        this.scenarios = Collections.unmodifiableList(copy);
    }

    public String runtimeId() { return runtimeId; }
    public List<BlockLifecycleScenario> scenarios() { return scenarios; }

    /** Must be called while a {@link worldline.test.WorldlineSpec} is being defined. */
    public void register(String suite) {
        Worldline.describe(suite, () -> {
            for (BlockLifecycleScenario scenario : scenarios) register(scenario);
        });
    }

    private void register(BlockLifecycleScenario scenario) {
        Worldline.test(scenario.id(), Worldline.worldline().runtime(runtimeId).run(context -> {
            BlockLifecycleDriver driver = context.capability(BlockLifecycleDriver.class);
            BlockLifecycleEvidence evidence = BlockLifecycleFixture.execute(scenario, driver);
            context.attach(EVIDENCE_ARTIFACT, evidence.canonical());
        })).tag("block-lifecycle");
    }
}
