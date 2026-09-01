package worldline.testkit;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Proves deterministic routed entity envelope evidence and fail-closed geometry checks. */
public final class EntityPhysicalEnvelopeFixtureTest {
    private EntityPhysicalEnvelopeFixtureTest() { }

    public static void main(String[] arguments) {
        execute();
        System.out.println("EntityPhysicalEnvelopeFixtureTest passed");
    }

    static void execute() {
        EntityPhysicalEnvelopeScenario scenario = () -> observations(true);
        EntityPhysicalEnvelopeEvidence evidence = EntityPhysicalEnvelopeFixture.execute(
                plan(), scenario);
        require(evidence.claims().size() == 2 && evidence.observations().size() == 2,
                "entity physical envelope evidence width");
        String canonical = evidence.canonical();
        require(canonical.contains("claims=2\n")
                && canonical.contains("entity/001#collision-shape|ARCHETYPE")
                && canonical.contains("entity/020#collision-shape|SINGULAR")
                && canonical.contains("width=0.25|height=0.25|y-offset=0.125"),
                "entity physical envelope canonical form");
        require(evidence.equals(EntityPhysicalEnvelopeFixture.execute(plan(), scenario))
                && evidence.hashCode() == EntityPhysicalEnvelopeFixture.execute(
                        plan(), scenario).hashCode(),
                "entity physical envelope evidence must be equatable");
        reject(() -> EntityPhysicalEnvelopeFixture.execute(plan(), () -> observations(false)));
        reject(() -> EntityPhysicalEnvelopeFixture.execute(plan(), () -> Collections.singletonList(
                observations(true).get(0))));
    }

    private static EntityConformancePlan plan() {
        List<EntityConformanceProfile> profiles = Arrays.asList(
                profile("b1.7.3:entity/001", false), profile("b1.7.3:entity/020", true));
        return new EntityConformancePlan(profiles, Collections.singletonList(
                new EntityConformanceTemplate("collision-shape", ConformanceLayer.ARCHETYPE)));
    }

    private static EntityConformanceProfile profile(String subject, boolean singular) {
        return new EntityConformanceProfile(subject, Collections.singletonList("physical"),
                singular, Collections.<String, ConformanceLayer>emptyMap());
    }

    private static List<EntityPhysicalEnvelopeObservation> observations(boolean valid) {
        return Arrays.asList(new EntityPhysicalEnvelopeObservation("b1.7.3:entity/001",
                        0.25F, 0.25F, 0.125F, false, false, false, valid, true),
                new EntityPhysicalEnvelopeObservation("b1.7.3:entity/020",
                        0.98F, 0.98F, 0.49F, false, false, false, true, true));
    }

    private static void reject(Runnable action) {
        try { action.run(); throw new AssertionError("invalid entity envelope accepted"); }
        catch (IllegalArgumentException | IllegalStateException expected) { }
    }

    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}
