package worldline.testkit;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Proves entity matrix expansion across universal, archetype and singular routes. */
public final class EntityConformancePlanTest {
    private EntityConformancePlanTest() { }

    public static void main(String[] arguments) {
        execute();
        System.out.println("EntityConformancePlanTest passed");
    }

    static void execute() {
        EntityConformanceProfile pig = profile("b1.7.3:entity/090", false, "animal");
        EntityConformanceProfile creeper = profile("b1.7.3:entity/050", true, "explosive");
        EntityConformancePlan plan = new EntityConformancePlan(Arrays.asList(pig, creeper),
                templates());
        require(plan.cases().size() == 8, "entity matrix width drifted");
        require(plan.caseFor(pig.subject(), "spawn-materialization").layer()
                == ConformanceLayer.UNIVERSAL, "universal route drifted");
        require(plan.caseFor(pig.subject(), "movement-policy").layer()
                == ConformanceLayer.ARCHETYPE, "archetype route drifted");
        require(plan.caseFor(creeper.subject(), "movement-policy").layer()
                == ConformanceLayer.SINGULAR, "singular route drifted");
        reject(() -> plan.caseFor("b1.7.3:entity/999", "movement-policy"));
        reject(() -> new EntityConformancePlan(Arrays.asList(pig, pig), templates()));
    }

    static EntityConformancePlan lifecyclePlan() {
        return new EntityConformancePlan(Arrays.asList(
                profile("b1.7.3:entity/090", false, "animal"),
                profile("b1.7.3:entity/050", true, "explosive")), templates());
    }

    private static EntityConformanceProfile profile(String subject, boolean singular,
            String archetype) {
        return new EntityConformanceProfile(subject, Collections.singletonList(archetype),
                singular, Collections.<String, ConformanceLayer>emptyMap());
    }

    private static List<EntityConformanceTemplate> templates() {
        return Arrays.asList(
                new EntityConformanceTemplate("spawn-materialization", ConformanceLayer.UNIVERSAL),
                new EntityConformanceTemplate("movement-policy", ConformanceLayer.ARCHETYPE),
                new EntityConformanceTemplate("damage-death", ConformanceLayer.ARCHETYPE),
                new EntityConformanceTemplate("drop-matrix", ConformanceLayer.ARCHETYPE));
    }

    private static void reject(Runnable action) {
        try { action.run(); throw new AssertionError("invalid entity plan accepted"); }
        catch (IllegalArgumentException expected) { }
    }

    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}
