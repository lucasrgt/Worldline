package worldline.testkit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import worldline.api.BlockLifecycleDriver;
import worldline.api.RemoteItemStack;
import worldline.test.Worldline;
import worldline.test.TestCaseBuilder;

/** Registers validated source-fluid dynamics rows as executable Worldline tests. */
public final class FluidDynamicsPlan {
    public static final String EVIDENCE_ARTIFACT = "fluid-dynamics.properties";
    private final String runtimeId;
    private final List<FluidDynamicsScenario> scenarios;
    private final long timeoutMillis;

    public FluidDynamicsPlan(String runtimeId, List<FluidDynamicsScenario> scenarios) {
        this(runtimeId, scenarios, 360_000L);
    }

    public FluidDynamicsPlan(String runtimeId, List<FluidDynamicsScenario> scenarios,
            long timeoutMillis) {
        if (runtimeId == null || !runtimeId.matches("[A-Za-z0-9._-]{1,64}")) {
            throw new IllegalArgumentException("invalid fluid dynamics runtime");
        }
        if (scenarios == null || scenarios.isEmpty() || timeoutMillis < 1L
                || timeoutMillis > 3_600_000L) {
            throw new IllegalArgumentException("invalid fluid dynamics plan");
        }
        List<FluidDynamicsScenario> copy = new ArrayList<FluidDynamicsScenario>(scenarios);
        Set<String> ids = new HashSet<String>();
        for (FluidDynamicsScenario scenario : copy) {
            if (scenario == null || !ids.add(scenario.id())) {
                throw new IllegalArgumentException("invalid fluid dynamics scenario set");
            }
        }
        this.runtimeId = runtimeId;
        this.scenarios = Collections.unmodifiableList(copy);
        this.timeoutMillis = timeoutMillis;
    }

    public List<FluidDynamicsScenario> scenarios() { return scenarios; }

    /** Must be called while a {@link worldline.test.WorldlineSpec} is being defined. */
    public void register(String suite) {
        Worldline.describe(suite, () -> {
            for (FluidDynamicsScenario scenario : scenarios) register(scenario);
        });
    }

    private void register(FluidDynamicsScenario scenario) {
        TestCaseBuilder runtime = Worldline.worldline().runtime(runtimeId)
                .runtimeOption(BlockLifecyclePlan.PLACEMENT_SLOT_OPTION,
                        slot(scenario.sourceSlot()))
                .runtimeOption(BlockLifecyclePlan.BREAK_SLOT_OPTION,
                        slot(scenario.gateToolSlot()))
                .runtimeOption(BlockLifecyclePlan.SUPPORT_STATE_OPTION,
                        state(scenario.supportState()))
                .runtimeOption(BlockLifecyclePlan.OVERHEAD_STATE_OPTION, "none")
                .runtimeOption(BlockLifecyclePlan.NEIGHBOR_STATE_OPTION, "none")
                .runtimeOption(BlockLifecyclePlan.NEIGHBOR_FACE_OPTION, "none")
                .runtimeOption(BlockLifecyclePlan.NEIGHBOR_SLOT_OPTION, "none");
        Worldline.test(scenario.id(), runtime.run(context -> {
            BlockLifecycleDriver driver = context.capability(BlockLifecycleDriver.class);
            FluidDynamicsEvidence evidence = FluidDynamicsFixture.execute(scenario, driver);
            context.attach(EVIDENCE_ARTIFACT, evidence.canonical());
        })).timeout(timeoutMillis).tag("fluid-dynamics");
    }

    private static String slot(BlockLifecycleSlot slot) {
        RemoteItemStack item = slot.before();
        return slot.hotbarSlot() + ":" + slot.inventorySlot() + ":" + item.legacyId()
                + ":" + item.count() + ":" + item.damage();
    }

    private static String state(worldline.api.BlockState state) {
        return state.legacyId() + ":" + state.metadata();
    }
}
