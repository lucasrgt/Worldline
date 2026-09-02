package worldline.testapi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import worldline.api.BlockLifecycleDriver;
import worldline.api.BlockState;
import worldline.api.RemoteItemStack;
import worldline.test.TestCaseBuilder;
import worldline.test.Worldline;

/** Registers bounded block-stability rows on the public lifecycle capability. */
public final class BlockStabilityPlan {
    public static final String EVIDENCE_ARTIFACT = "block-stability.properties";
    public static final long DEFAULT_TIMEOUT_MILLIS = 300_000L;
    private final String runtimeId;
    private final List<BlockStabilityScenario> scenarios;
    private final long timeoutMillis;

    public BlockStabilityPlan(String runtimeId, List<BlockStabilityScenario> scenarios) {
        this(runtimeId, scenarios, DEFAULT_TIMEOUT_MILLIS);
    }

    public BlockStabilityPlan(String runtimeId, List<BlockStabilityScenario> scenarios,
            long timeoutMillis) {
        if (runtimeId == null || !runtimeId.matches("[A-Za-z0-9._-]{1,64}")) {
            throw new IllegalArgumentException("invalid stability runtime");
        }
        if (scenarios == null || scenarios.isEmpty()) {
            throw new IllegalArgumentException("stability scenarios are empty");
        }
        if (timeoutMillis < 1L || timeoutMillis > 3_600_000L) {
            throw new IllegalArgumentException("invalid stability timeout");
        }
        List<BlockStabilityScenario> copy = new ArrayList<BlockStabilityScenario>(scenarios);
        Set<String> ids = new HashSet<String>();
        for (BlockStabilityScenario scenario : copy) {
            if (scenario == null || !ids.add(scenario.id())) {
                throw new IllegalArgumentException("null or duplicate stability scenario");
            }
        }
        this.runtimeId = runtimeId;
        this.scenarios = Collections.unmodifiableList(copy);
        this.timeoutMillis = timeoutMillis;
    }

    public List<BlockStabilityScenario> scenarios() { return scenarios; }

    /** Must be called while a {@link worldline.test.WorldlineSpec} is being defined. */
    public void register(String suite) {
        Worldline.describe(suite, () -> {
            for (BlockStabilityScenario scenario : scenarios) register(scenario);
        });
    }

    private void register(BlockStabilityScenario scenario) {
        TestCaseBuilder runtime = Worldline.worldline().runtime(runtimeId)
                .runtimeOption(BlockLifecyclePlan.PLACEMENT_SLOT_OPTION,
                        slot(scenario.placementSlot()))
                .runtimeOption(BlockLifecyclePlan.BREAK_SLOT_OPTION,
                        slot(scenario.breakSlot()))
                .runtimeOption(BlockLifecyclePlan.SUPPORT_STATE_OPTION,
                        state(scenario.supportState()))
                .runtimeOption(BlockLifecyclePlan.OVERHEAD_STATE_OPTION,
                        state(scenario.overheadState()))
                .runtimeOption(BlockLifecyclePlan.NEIGHBOR_STATE_OPTION, "none")
                .runtimeOption(BlockLifecyclePlan.NEIGHBOR_FACE_OPTION, "none")
                .runtimeOption(BlockLifecyclePlan.NEIGHBOR_SLOT_OPTION, "none");
        Worldline.test(scenario.id(), runtime.run(context -> {
            BlockLifecycleDriver driver = context.capability(BlockLifecycleDriver.class);
            BlockStabilityEvidence evidence = BlockStabilityFixture.execute(scenario, driver);
            context.attach(EVIDENCE_ARTIFACT, evidence.canonical());
        })).timeout(timeoutMillis).tag("block-stability");
    }

    private static String slot(BlockLifecycleSlot slot) {
        RemoteItemStack item = slot.before();
        return slot.hotbarSlot() + ":" + slot.inventorySlot() + ":" + item.legacyId()
                + ":" + item.count() + ":" + item.damage();
    }

    private static String state(BlockState value) {
        return value.legacyId() + ":" + value.metadata();
    }
}
