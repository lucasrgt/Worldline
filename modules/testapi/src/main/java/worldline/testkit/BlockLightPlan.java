package worldline.testkit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import worldline.api.BlockLightDriver;
import worldline.api.RemoteItemStack;
import worldline.test.TestCaseBuilder;
import worldline.test.Worldline;

/** Registers validated light-transport rows as executable Worldline tests. */
public final class BlockLightPlan {
    public static final String EVIDENCE_ARTIFACT = "block-light.properties";
    public static final String PLACEMENT_SLOT_OPTION = "block-light.placement-slot";
    public static final long DEFAULT_TIMEOUT_MILLIS = 180_000L;

    private final String runtimeId;
    private final List<BlockLightScenario> scenarios;
    private final long timeoutMillis;

    public BlockLightPlan(String runtimeId, List<BlockLightScenario> scenarios) {
        this(runtimeId, scenarios, DEFAULT_TIMEOUT_MILLIS);
    }
    public BlockLightPlan(String runtimeId, List<BlockLightScenario> scenarios,
            long timeoutMillis) {
        if (runtimeId == null || !runtimeId.matches("[A-Za-z0-9._-]{1,64}")) {
            throw new IllegalArgumentException("invalid light runtime");
        }
        if (scenarios == null || scenarios.isEmpty()) {
            throw new IllegalArgumentException("light scenarios are empty");
        }
        if (timeoutMillis < 1L || timeoutMillis > 3_600_000L) {
            throw new IllegalArgumentException("invalid light timeout");
        }
        List<BlockLightScenario> copy = new ArrayList<BlockLightScenario>(scenarios);
        Set<String> ids = new HashSet<String>();
        for (BlockLightScenario scenario : copy) if (scenario == null || !ids.add(scenario.id())) {
            throw new IllegalArgumentException("null or duplicate light scenario");
        }
        this.runtimeId = runtimeId; this.scenarios = Collections.unmodifiableList(copy);
        this.timeoutMillis = timeoutMillis;
    }

    public String runtimeId() { return runtimeId; }
    public List<BlockLightScenario> scenarios() { return scenarios; }
    public long timeoutMillis() { return timeoutMillis; }

    /** Must be called while a {@link worldline.test.WorldlineSpec} is being defined. */
    public void register(String suite) {
        Worldline.describe(suite, () -> {
            for (BlockLightScenario scenario : scenarios) register(scenario);
        });
    }

    private void register(BlockLightScenario scenario) {
        TestCaseBuilder runtime = Worldline.worldline().runtime(runtimeId)
                .runtimeOption(PLACEMENT_SLOT_OPTION, slot(scenario.placementSlot()));
        Worldline.test(scenario.id(), runtime.run(context -> {
            BlockLightDriver driver = context.capability(BlockLightDriver.class);
            BlockLightEvidence evidence = BlockLightFixture.execute(scenario, driver);
            context.attach(EVIDENCE_ARTIFACT, evidence.canonical());
        })).timeout(timeoutMillis).tag("block-light");
    }

    private static String slot(BlockLifecycleSlot slot) {
        RemoteItemStack item = slot.before();
        return slot.hotbarSlot() + ":" + slot.inventorySlot() + ":" + item.legacyId()
                + ":" + item.count() + ":" + item.damage();
    }
}
