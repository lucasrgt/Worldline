package worldline.testkit;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;
import worldline.analysis.CensusDocument;
import worldline.analysis.CensusRunner;

public final class BlockRegistryFixtureTest {
    private BlockRegistryFixtureTest() { }

    public static void main(String[] arguments) {
        TreeMap<String, String> rows = new TreeMap<String, String>();
        rows.put("b001", "class=BlockStone material=rock");
        rows.put("b020", "class=BlockGlass material=glass");
        String document = CensusDocument.section("blocks", rows);
        BlockRegistryCensusScenario scenario = new BlockRegistryCensusScenario(
                new FixedRunner(document), "b1.7.3");
        BlockRegistryEvidence evidence = BlockRegistryFixture.execute(plan(), scenario);
        require(evidence.claims().size() == 2 && evidence.observations().size() == 2,
                "registry evidence width");
        String canonical = evidence.canonical();
        require(canonical.contains("claims=2\n")
                && canonical.contains("b1.7.3:block/001#registry-presence|UNIVERSAL")
                && canonical.contains("row.002=b020=class=BlockGlass material=glass"),
                "registry evidence canonical form");
        require(evidence.equals(BlockRegistryFixture.execute(plan(), scenario))
                && evidence.hashCode() == BlockRegistryFixture.execute(plan(), scenario).hashCode(),
                "registry evidence must be equatable across equivalent plans");
        reject(() -> BlockRegistryFixture.execute(plan(),
                () -> Collections.singletonList(new BlockRegistryObservation(
                        "b1.7.3:block/001", "b001=stone"))));
        reject(() -> new BlockRegistryCensusScenario(
                new AlternatingRunner(document, CensusDocument.section("blocks", oneRow())),
                "b1.7.3").observe());
        System.out.println("BlockRegistryFixtureTest passed");
    }

    private static BlockConformancePlan plan() {
        List<BlockConformanceProfile> profiles = Arrays.asList(
                profile("b1.7.3:block/001"), profile("b1.7.3:block/020"));
        return new BlockConformancePlan(profiles, Collections.singletonList(
                new BlockConformanceTemplate("registry-presence", ConformanceLayer.UNIVERSAL)));
    }

    private static BlockConformanceProfile profile(String subject) {
        return new BlockConformanceProfile(subject, Collections.singletonList("registry"),
                false, Collections.<String, ConformanceLayer>emptyMap());
    }

    private static TreeMap<String, String> oneRow() {
        TreeMap<String, String> rows = new TreeMap<String, String>();
        rows.put("b001", "class=BlockStone material=rock");
        return rows;
    }

    private static void reject(Runnable action) {
        try { action.run(); throw new AssertionError("invalid registry scenario accepted"); }
        catch (IllegalArgumentException | IllegalStateException expected) { }
    }

    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }

    private static class FixedRunner implements CensusRunner {
        private final String document;
        FixedRunner(String document) { this.document = document; }
        @Override public List<String> sections() { return Collections.singletonList("blocks"); }
        @Override public String section(String name) { return document; }
    }

    private static final class AlternatingRunner extends FixedRunner {
        private final String second;
        private int calls;
        AlternatingRunner(String first, String second) { super(first); this.second = second; }
        @Override public String section(String name) { return calls++ == 0 ? super.section(name) : second; }
    }
}
