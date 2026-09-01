package worldline.testkit;

import java.util.Arrays;
import java.util.List;

/** Proves deterministic universal entity persistence evidence and fail-closed checks. */
public final class EntityPersistenceFixtureTest {
    private EntityPersistenceFixtureTest() { }

    public static void main(String[] arguments) {
        execute();
        System.out.println("EntityPersistenceFixtureTest passed");
    }

    static void execute() {
        EntityPersistenceScenario scenario = () -> observations(true);
        EntityPersistenceEvidence evidence = EntityPersistenceFixture.execute(scenario);
        require(evidence.observations().size() == 2,
                "entity persistence evidence width");
        String canonical = evidence.canonical();
        require(canonical.contains("claims=2\n")
                && canonical.contains("entity/001#save-reload|UNIVERSAL")
                && canonical.contains("registry-name=Item|runtime-type=EntityItem")
                && canonical.contains("|common-state-exact=true|nbt-exact=true"),
                "entity persistence canonical form");
        require(evidence.equals(EntityPersistenceFixture.execute(scenario))
                && evidence.hashCode() == EntityPersistenceFixture.execute(scenario).hashCode(),
                "entity persistence evidence must be equatable");
        reject(() -> EntityPersistenceFixture.execute(() -> observations(false)));
        reject(() -> EntityPersistenceFixture.execute(() -> Arrays.asList(
                observations(true).get(0), observations(true).get(0))));
    }

    private static List<EntityPersistenceObservation> observations(boolean exact) {
        String digest = "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef";
        return Arrays.asList(new EntityPersistenceObservation("b1.7.3:entity/001", "Item",
                        "EntityItem", true, true, exact, true, digest),
                new EntityPersistenceObservation("b1.7.3:entity/020", "PrimedTnt",
                        "EntityTNTPrimed", true, true, true, true, digest));
    }

    private static void reject(Runnable action) {
        try { action.run(); throw new AssertionError("invalid entity persistence accepted"); }
        catch (IllegalArgumentException | IllegalStateException expected) { }
    }

    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}
