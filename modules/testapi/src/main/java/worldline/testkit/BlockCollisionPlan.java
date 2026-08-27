package worldline.testkit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import worldline.api.BlockCollisionDriver;
import worldline.api.RemoteItemStack;
import worldline.test.TestCaseBuilder;
import worldline.test.Worldline;

/** Registers validated collision-envelope rows as executable Worldline tests. */
public final class BlockCollisionPlan {
    public static final String EVIDENCE_ARTIFACT = "block-collision.properties";
    public static final String PLACEMENT_SLOT_OPTION = "block-collision.placement-slot";
    public static final long DEFAULT_TIMEOUT_MILLIS = 180_000L;

    private final String runtimeId;
    private final List<BlockCollisionScenario> scenarios;
    private final long timeoutMillis;

    public BlockCollisionPlan(String runtimeId, List<BlockCollisionScenario> scenarios) {
        this(runtimeId, scenarios, DEFAULT_TIMEOUT_MILLIS);
    }

    public BlockCollisionPlan(String runtimeId, List<BlockCollisionScenario> scenarios,
            long timeoutMillis) {
        if (runtimeId == null || !runtimeId.matches("[A-Za-z0-9._-]{1,64}")) {
            throw new IllegalArgumentException("invalid collision runtime");
        }
        if (scenarios == null || scenarios.isEmpty()) {
            throw new IllegalArgumentException("collision scenarios are empty");
        }
        if (timeoutMillis < 1L || timeoutMillis > 3_600_000L) {
            throw new IllegalArgumentException("invalid collision timeout");
        }
        List<BlockCollisionScenario> copy = new ArrayList<BlockCollisionScenario>(scenarios);
        Set<String> ids = new HashSet<String>();
        for (BlockCollisionScenario scenario : copy) {
            if (scenario == null || !ids.add(scenario.id())) {
                throw new IllegalArgumentException("null or duplicate collision scenario");
            }
        }
        this.runtimeId = runtimeId; this.scenarios = Collections.unmodifiableList(copy);
        this.timeoutMillis = timeoutMillis;
    }

    public String runtimeId() { return runtimeId; }
    public List<BlockCollisionScenario> scenarios() { return scenarios; }
    public long timeoutMillis() { return timeoutMillis; }

    /** Must be called while a {@link worldline.test.WorldlineSpec} is being defined. */
    public void register(String suite) {
        Worldline.describe(suite, () -> {
            for (BlockCollisionScenario scenario : scenarios) register(scenario);
        });
    }

    private void register(BlockCollisionScenario scenario) {
        TestCaseBuilder runtime = Worldline.worldline().runtime(runtimeId)
                .runtimeOption(PLACEMENT_SLOT_OPTION, slot(scenario.placementSlot()));
        Worldline.test(scenario.id(), runtime.run(context -> {
            BlockCollisionDriver driver = context.capability(BlockCollisionDriver.class);
            BlockCollisionEvidence evidence = BlockCollisionFixture.execute(scenario, driver);
            context.attach(EVIDENCE_ARTIFACT, evidence.canonical());
        })).timeout(timeoutMillis).tag("block-collision");
    }

    private static String slot(BlockLifecycleSlot slot) {
        RemoteItemStack item = slot.before();
        return slot.hotbarSlot() + ":" + slot.inventorySlot() + ":" + item.legacyId()
                + ":" + item.count() + ":" + item.damage();
    }
}
