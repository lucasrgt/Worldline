package worldline.b173server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import worldline.api.RemoteItemStack;
import worldline.testkit.BlockConformanceCase;
import worldline.testkit.BlockConformancePlan;
import worldline.testkit.BlockConformanceProfile;
import worldline.testkit.BlockConformanceTemplate;
import worldline.testkit.BlockLifecycleSlot;
import worldline.testkit.BlockSupportLossScenario;
import worldline.testkit.ConformanceLayer;

/** Official lifecycle rows for bounded stability and causal ground-support loss. */
public final class B173GroundFloraSupportLossScenarioFactory {
    public static final long SEED = B173LifecycleArena.SEED;
    private static final int TICK_WINDOW = 240;

    private B173GroundFloraSupportLossScenarioFactory() {
    }

    public static List<BlockSupportLossScenario> rows() {
        List<BlockSupportLossScenario> rows = new ArrayList<BlockSupportLossScenario>();
        for (B173GroundFloraPhysicalCatalog.Subject subject
                : B173GroundFloraPhysicalCatalog.subjects()) rows.add(row(subject));
        return Collections.unmodifiableList(rows);
    }

    private static BlockSupportLossScenario row(
            B173GroundFloraPhysicalCatalog.Subject subject) {
        BlockConformancePlan plan = new BlockConformancePlan(Collections.singletonList(
                new BlockConformanceProfile(subject.subject(), subject.archetypes, false,
                        Collections.<String, ConformanceLayer>emptyMap())), Arrays.asList(
                new BlockConformanceTemplate("tick-policy", ConformanceLayer.ARCHETYPE),
                new BlockConformanceTemplate("neighbor-response", ConformanceLayer.ARCHETYPE)));
        return new BlockSupportLossScenario(subject.name + "-support-loss",
                claim(plan, subject, "tick-policy"), claim(plan, subject, "neighbor-response"),
                B173LifecycleArena.SUPPORT, subject.support, subject.state(),
                new BlockLifecycleSlot(1, 37, new RemoteItemStack(subject.id, 1, 0), null),
                new BlockLifecycleSlot(2, 38, new RemoteItemStack(277, 1, 0),
                        new RemoteItemStack(277, 1, 1)), TICK_WINDOW, 4, 40);
    }

    private static BlockConformanceCase claim(BlockConformancePlan plan,
            B173GroundFloraPhysicalCatalog.Subject subject, String template) {
        return plan.caseFor(subject.subject(), template);
    }
}
