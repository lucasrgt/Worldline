package worldline.testapi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import worldline.api.BlockRandomTickSpreadDriver;
import worldline.api.RemoteItemStack;
import worldline.test.TestCaseBuilder;
import worldline.test.Worldline;

/** Registers bounded random-tick spread rows on one public runtime. */
public final class BlockRandomTickSpreadPlan {
    public static final String EVIDENCE_ARTIFACT = "block-random-tick-spread.properties";
    public static final String PLACEMENT_SLOT_OPTION = "block-random-tick-spread.placement-slot";
    public static final String BREAK_SLOT_OPTION = "block-random-tick-spread.break-slot";
    private final String runtimeId;
    private final List<BlockRandomTickSpreadScenario> scenarios;

    public BlockRandomTickSpreadPlan(String runtimeId,
            List<BlockRandomTickSpreadScenario> scenarios) {
        if (runtimeId == null || !runtimeId.matches("[A-Za-z0-9._-]{1,64}")) {
            throw new IllegalArgumentException("invalid random-tick spread runtime");
        }
        if (scenarios == null || scenarios.isEmpty()) throw new IllegalArgumentException(
                "random-tick spread scenarios are empty");
        List<BlockRandomTickSpreadScenario> copy = new ArrayList<>(scenarios);
        Set<String> ids = new HashSet<String>();
        for (BlockRandomTickSpreadScenario scenario : copy) if (scenario == null
                || !ids.add(scenario.id())) throw new IllegalArgumentException(
                        "null or duplicate random-tick spread scenario");
        this.runtimeId = runtimeId; this.scenarios = Collections.unmodifiableList(copy);
    }

    public List<BlockRandomTickSpreadScenario> scenarios() { return scenarios; }

    /** Must be called while a {@link worldline.test.WorldlineSpec} is being defined. */
    public void register(String suite) {
        Worldline.describe(suite, () -> scenarios.forEach(this::register));
    }
    private void register(BlockRandomTickSpreadScenario scenario) {
        TestCaseBuilder runtime = Worldline.worldline().runtime(runtimeId)
                .runtimeOption(PLACEMENT_SLOT_OPTION, slot(scenario.placementSlot()))
                .runtimeOption(BREAK_SLOT_OPTION, slot(scenario.breakSlot()));
        Worldline.test(scenario.id(), runtime.run(context -> {
            BlockRandomTickSpreadDriver driver = context.capability(
                    BlockRandomTickSpreadDriver.class);
            BlockRandomTickSpreadEvidence evidence = BlockRandomTickSpreadFixture.execute(
                    scenario, driver);
            context.attach(EVIDENCE_ARTIFACT, evidence.canonical());
        })).timeout(2_400_000L).tag("block-random-tick-spread");
    }
    private static String slot(BlockLifecycleSlot slot) {
        RemoteItemStack item = slot.before();
        return slot.hotbarSlot() + ":" + slot.inventorySlot() + ":" + item.legacyId()
                + ":" + item.count() + ":" + item.damage();
    }
}
