package worldline.testkit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import worldline.api.BlockStateDomainDriver;
import worldline.api.RemoteItemStack;
import worldline.test.TestCaseBuilder;
import worldline.test.Worldline;

/** Registers validated reachable-state data rows as executable Worldline tests. */
public final class BlockStateDomainPlan {
    public static final String EVIDENCE_ARTIFACT = "block-state-domain.properties";
    public static final String PLACEMENT_SLOT_OPTION = "block-state-domain.placement-slot";
    public static final long DEFAULT_TIMEOUT_MILLIS = 180_000L;

    private final String runtimeId;
    private final List<BlockStateDomainScenario> scenarios;
    private final long timeoutMillis;

    public BlockStateDomainPlan(String runtimeId, List<BlockStateDomainScenario> scenarios) {
        this(runtimeId, scenarios, DEFAULT_TIMEOUT_MILLIS);
    }

    public BlockStateDomainPlan(String runtimeId, List<BlockStateDomainScenario> scenarios,
            long timeoutMillis) {
        if (runtimeId == null || !runtimeId.matches("[A-Za-z0-9._-]{1,64}")) {
            throw new IllegalArgumentException("invalid state-domain runtime");
        }
        if (scenarios == null || scenarios.isEmpty()) {
            throw new IllegalArgumentException("state-domain scenarios are empty");
        }
        if (timeoutMillis < 1L || timeoutMillis > 3_600_000L) {
            throw new IllegalArgumentException("invalid state-domain timeout");
        }
        List<BlockStateDomainScenario> copy = new ArrayList<BlockStateDomainScenario>(scenarios);
        Set<String> ids = new HashSet<String>();
        for (BlockStateDomainScenario scenario : copy) {
            if (scenario == null || !ids.add(scenario.id())) {
                throw new IllegalArgumentException("null or duplicate state-domain scenario");
            }
        }
        this.runtimeId = runtimeId;
        this.scenarios = Collections.unmodifiableList(copy);
        this.timeoutMillis = timeoutMillis;
    }

    public String runtimeId() { return runtimeId; }
    public List<BlockStateDomainScenario> scenarios() { return scenarios; }
    public long timeoutMillis() { return timeoutMillis; }

    /** Must be called while a {@link worldline.test.WorldlineSpec} is being defined. */
    public void register(String suite) {
        Worldline.describe(suite, () -> {
            for (BlockStateDomainScenario scenario : scenarios) register(scenario);
        });
    }

    private void register(BlockStateDomainScenario scenario) {
        TestCaseBuilder runtime = Worldline.worldline().runtime(runtimeId)
                .runtimeOption(PLACEMENT_SLOT_OPTION, slot(scenario.placementSlot()));
        Worldline.test(scenario.id(), runtime.run(context -> {
            BlockStateDomainDriver driver = context.capability(BlockStateDomainDriver.class);
            BlockStateDomainEvidence evidence = BlockStateDomainFixture.execute(scenario, driver);
            context.attach(EVIDENCE_ARTIFACT, evidence.canonical());
        })).timeout(timeoutMillis).tag("block-state-domain");
    }

    private static String slot(BlockLifecycleSlot slot) {
        RemoteItemStack item = slot.before();
        return slot.hotbarSlot() + ":" + slot.inventorySlot() + ":" + item.legacyId()
                + ":" + item.count() + ":" + item.damage();
    }
}
