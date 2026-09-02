package worldline.testkit;
import worldline.testapi.VegetationEcologyEvidence;
import worldline.testapi.VegetationEcologyFixture;
import worldline.testapi.VegetationEcologyObservation;

/** Contract test for stable vegetation ecology evidence. */
public final class VegetationEcologyFixtureTest {
    private VegetationEcologyFixtureTest() { }

    public static void execute() {
        VegetationEcologyEvidence first = VegetationEcologyFixture.execute(
                VegetationEcologyFixtureTest::observation);
        VegetationEcologyEvidence second = VegetationEcologyFixture.execute(
                VegetationEcologyFixtureTest::observation);
        if (!first.equals(second) || first.hashCode() != second.hashCode()
                || !first.canonical().contains("claims=19|"))
            throw new AssertionError("vegetation ecology evidence is unstable");
        rejects(() -> VegetationEcologyFixture.execute(() ->
                new VegetationEcologyObservation("wrong", observation().shapes(),
                        observation().light(), observation().neighbors())));
    }

    private static VegetationEcologyObservation observation() {
        return new VegetationEcologyObservation(
                "2:0,6:0+1+2+8+9+10,18:0+1+2+4+5+6+8+9+10+12+13+14,31:0+1+2,59:0-7,83:0-15",
                "2+18:full,6+59+83:passable",
                "2:255/0,6+59+83:0/0",
                "2:stable,6+59:support-drop,18:decay-mark");
    }

    private static void rejects(Runnable action) {
        try {
            action.run();
            throw new AssertionError("invalid vegetation ecology evidence was accepted");
        } catch (IllegalStateException expected) {
            // Expected.
        }
    }
}
