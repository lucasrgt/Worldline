package worldline.b173server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.RemoteItemStack;
import worldline.testkit.BlockConformanceCase;
import worldline.testkit.BlockConformancePlan;
import worldline.testkit.BlockConformanceProfile;
import worldline.testkit.BlockConformanceTemplate;
import worldline.testkit.BlockLifecycleSlot;
import worldline.testkit.ConformanceLayer;
import worldline.testkit.FluidDynamicsScenario;

/** Public source-water and source-lava propagation rows for the official server. */
public final class B173FluidDynamicsScenarioFactory {
    public static final long SEED = B173LifecycleArena.SEED;
    private static final BlockState STONE = new BlockState(1, 0);
    private static final List<Subject> SUBJECTS = Collections.unmodifiableList(Arrays.asList(
            new Subject(9, "still-water", new BlockState(9, 1)),
            new Subject(11, "still-lava", new BlockState(11, 2))));

    private B173FluidDynamicsScenarioFactory() { }

    public static List<FluidDynamicsScenario> rows() {
        List<FluidDynamicsScenario> rows = new ArrayList<FluidDynamicsScenario>();
        for (Subject subject : SUBJECTS) rows.add(row(subject));
        return Collections.unmodifiableList(rows);
    }

    private static FluidDynamicsScenario row(Subject subject) {
        BlockConformancePlan plan = new BlockConformancePlan(Collections.singletonList(
                new BlockConformanceProfile(subject.subject(),
                        Arrays.asList("fluid", "tick-driven"), false,
                        Collections.<String, ConformanceLayer>emptyMap())), Arrays.asList(
                new BlockConformanceTemplate("gameplay-placement", ConformanceLayer.UNIVERSAL),
                new BlockConformanceTemplate("save-reload", ConformanceLayer.UNIVERSAL),
                new BlockConformanceTemplate("tick-policy", ConformanceLayer.ARCHETYPE),
                new BlockConformanceTemplate("neighbor-response", ConformanceLayer.ARCHETYPE)));
        return new FluidDynamicsScenario(subject.name + "-source-fluid-dynamics",
                claim(plan, subject, "gameplay-placement"), claim(plan, subject, "save-reload"),
                claim(plan, subject, "tick-policy"), claim(plan, subject, "neighbor-response"),
                B173FluidDynamicsArena.SOURCE_SUPPORT, STONE, new BlockState(subject.id, 0),
                STONE, subject.flow, slot(1, subject.id, 1, null),
                slot(2, 278, 1, new RemoteItemStack(278, 1, 1)), 40, 20, 70);
    }

    private static BlockConformanceCase claim(BlockConformancePlan plan,
            Subject subject, String template) {
        return plan.caseFor(subject.subject(), template);
    }

    private static BlockLifecycleSlot slot(int hotbar, int id, int count,
            RemoteItemStack after) {
        return new BlockLifecycleSlot(hotbar, hotbar + 36,
                new RemoteItemStack(id, count, 0), after);
    }

    private static final class Subject {
        final int id; final String name; final BlockState flow;
        Subject(int id, String name, BlockState flow) {
            this.id = id; this.name = name; this.flow = flow;
        }
        String subject() { return String.format("b1.7.3:block/%03d", id); }
    }
}
