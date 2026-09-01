package worldline.testkit;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;
import worldline.analysis.CensusDocument;
import worldline.analysis.CensusRunner;

/** Proves deterministic universal EntityList registry evidence. */
public final class EntityRegistryFixtureTest {
    private EntityRegistryFixtureTest() { }

    public static void main(String[] arguments) {
        execute();
        System.out.println("EntityRegistryFixtureTest passed");
    }

    static void execute() {
        TreeMap<String, String> rows = rows();
        String document = CensusDocument.section("entities", rows);
        EntityRegistryCensusScenario scenario = new EntityRegistryCensusScenario(
                new FixedRunner(document), "b1.7.3");
        EntityRegistryEvidence evidence = EntityRegistryFixture.execute(plan(), scenario);
        require(evidence.claims().size() == 2 && evidence.observations().size() == 2,
                "entity registry evidence width");
        String canonical = evidence.canonical();
        require(canonical.contains("claims=2\n")
                && canonical.contains("b1.7.3:entity/001#registry-presence|UNIVERSAL")
                && canonical.contains("row.002=e090=name=Pig|class=EntityPig"),
                "entity registry canonical form");
        require(evidence.equals(EntityRegistryFixture.execute(plan(), scenario))
                && evidence.hashCode() == EntityRegistryFixture.execute(plan(), scenario).hashCode(),
                "entity registry evidence must be equatable");
        reject(() -> EntityRegistryFixture.execute(plan(),
                () -> Collections.singletonList(new EntityRegistryObservation(
                        "b1.7.3:entity/001", "e001=name=Item|class=EntityItem"))));
        reject(() -> new EntityRegistryCensusScenario(
                new AlternatingRunner(document, CensusDocument.section("entities", oneRow())),
                "b1.7.3").observe());
    }

    private static EntityConformancePlan plan() {
        List<EntityConformanceProfile> profiles = Arrays.asList(
                profile("b1.7.3:entity/001"), profile("b1.7.3:entity/090"));
        return new EntityConformancePlan(profiles, Collections.singletonList(
                new EntityConformanceTemplate("registry-presence", ConformanceLayer.UNIVERSAL)));
    }

    private static EntityConformanceProfile profile(String subject) {
        return new EntityConformanceProfile(subject, Collections.singletonList("registry"),
                false, Collections.<String, ConformanceLayer>emptyMap());
    }

    private static TreeMap<String, String> rows() {
        TreeMap<String, String> rows = new TreeMap<String, String>();
        rows.put("e001", "name=Item|class=EntityItem");
        rows.put("e090", "name=Pig|class=EntityPig");
        return rows;
    }

    private static TreeMap<String, String> oneRow() {
        TreeMap<String, String> rows = new TreeMap<String, String>();
        rows.put("e001", "name=Item|class=EntityItem");
        return rows;
    }

    private static void reject(Runnable action) {
        try { action.run(); throw new AssertionError("invalid entity registry accepted"); }
        catch (IllegalArgumentException | IllegalStateException expected) { }
    }

    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }

    private static class FixedRunner implements CensusRunner {
        private final String document;
        FixedRunner(String document) { this.document = document; }
        @Override public List<String> sections() { return Collections.singletonList("entities"); }
        @Override public String section(String name) { return document; }
    }

    private static final class AlternatingRunner extends FixedRunner {
        private final String second;
        private int calls;
        AlternatingRunner(String first, String second) { super(first); this.second = second; }
        @Override public String section(String name) {
            return calls++ == 0 ? super.section(name) : second;
        }
    }
}
