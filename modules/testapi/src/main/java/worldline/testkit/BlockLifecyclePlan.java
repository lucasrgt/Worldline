package worldline.testkit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import worldline.api.BlockLifecycleDriver;
import worldline.test.Worldline;
import worldline.test.TestCaseBuilder;

/** Registers validated lifecycle data rows as executable Worldline tests. */
public final class BlockLifecyclePlan {
    public static final String EVIDENCE_ARTIFACT = "block-lifecycle.properties";
    public static final long DEFAULT_TIMEOUT_MILLIS = 180_000L;
    public static final String PLACEMENT_SLOT_OPTION = "block-lifecycle.placement-slot";
    public static final String BREAK_SLOT_OPTION = "block-lifecycle.break-slot";

    private final String runtimeId;
    private final List<BlockLifecycleScenario> scenarios;
    private final long timeoutMillis;

    public BlockLifecyclePlan(String runtimeId, List<BlockLifecycleScenario> scenarios) {
        this(runtimeId, scenarios, DEFAULT_TIMEOUT_MILLIS);
    }

    public BlockLifecyclePlan(String runtimeId, List<BlockLifecycleScenario> scenarios,
            long timeoutMillis) {
        if (runtimeId == null || !runtimeId.matches("[A-Za-z0-9._-]{1,64}")) {
            throw new IllegalArgumentException("invalid lifecycle runtime");
        }
        if (scenarios == null || scenarios.isEmpty()) {
            throw new IllegalArgumentException("lifecycle scenarios are empty");
        }
        if (timeoutMillis < 1L || timeoutMillis > 3_600_000L) {
            throw new IllegalArgumentException("invalid lifecycle timeout");
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
        this.timeoutMillis = timeoutMillis;
    }

    public String runtimeId() { return runtimeId; }
    public List<BlockLifecycleScenario> scenarios() { return scenarios; }
    public long timeoutMillis() { return timeoutMillis; }

    /** Must be called while a {@link worldline.test.WorldlineSpec} is being defined. */
    public void register(String suite) {
        Worldline.describe(suite, () -> {
            for (BlockLifecycleScenario scenario : scenarios) register(scenario);
        });
    }

    private void register(BlockLifecycleScenario scenario) {
        TestCaseBuilder runtime = Worldline.worldline().runtime(runtimeId)
                .runtimeOption(PLACEMENT_SLOT_OPTION, slot(scenario.placementSlot()))
                .runtimeOption(BREAK_SLOT_OPTION, slot(scenario.breakSlot()));
        Worldline.test(scenario.id(), runtime.run(context -> {
            BlockLifecycleDriver driver = context.capability(BlockLifecycleDriver.class);
            BlockLifecycleEvidence evidence = BlockLifecycleFixture.execute(scenario, driver);
            context.attach(EVIDENCE_ARTIFACT, evidence.canonical());
        })).timeout(timeoutMillis).tag("block-lifecycle");
    }

    private static String slot(BlockLifecycleSlot slot) {
        worldline.api.RemoteItemStack item = slot.before();
        return slot.hotbarSlot() + ":" + slot.inventorySlot() + ":" + item.legacyId()
                + ":" + item.count() + ":" + item.damage();
    }
}
