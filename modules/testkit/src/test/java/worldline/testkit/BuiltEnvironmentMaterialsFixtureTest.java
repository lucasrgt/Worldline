package worldline.testkit;

/** Contract test for stable built-environment evidence. */
public final class BuiltEnvironmentMaterialsFixtureTest {
    private BuiltEnvironmentMaterialsFixtureTest() { }

    public static void execute() {
        BuiltEnvironmentMaterialsEvidence first = BuiltEnvironmentMaterialsFixture.execute(
                BuiltEnvironmentMaterialsFixtureTest::observation);
        BuiltEnvironmentMaterialsEvidence second = BuiltEnvironmentMaterialsFixture.execute(
                BuiltEnvironmentMaterialsFixtureTest::observation);
        if (!first.equals(second) || first.hashCode() != second.hashCode()
                || !first.canonical().contains("claims=47|"))
            throw new AssertionError("built-environment evidence is unstable");
        rejects(() -> BuiltEnvironmentMaterialsFixture.execute(() ->
                new BuiltEnvironmentMaterialsObservation("wrong", observation().shapes(),
                        observation().light(), observation().ticks(), observation().neighbors())));
    }

    private static BuiltEnvironmentMaterialsObservation observation() {
        return new BuiltEnvironmentMaterialsObservation(
                "1:0+17:0-2+20:0+44:0-3+85:0+89:0",
                "17+20+86+89+91:full,65:wall-2/16,67:two-box-stair",
                "1+17+44+53+67+86:255/0,65+85:0/0",
                "86+91:on-load-stable,17+20+30+43+44+53+65+67+85+88+89:manual-stable",
                "65:support-drop,17+20+30+43+44+53+67+85+86+88+89+91:stable");
    }

    private static void rejects(Runnable action) {
        try {
            action.run();
            throw new AssertionError("invalid built-environment evidence was accepted");
        } catch (IllegalStateException expected) {
            // Expected.
        }
    }
}
