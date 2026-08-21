package worldline.minimization;

public final class ScenarioDslTest {
    private ScenarioDslTest() {}

    public static void main(String[] arguments) {
        roundTrips();
        rejections();
        validateIntegratesWithScenario();
        System.out.println("ScenarioDslTest passed");
    }

    private static void roundTrips() {
        equal(ScenarioStep.tick(1), ScenarioDsl.parse("tick"), "bare tick");
        equal(ScenarioStep.tick(12), ScenarioDsl.parse("tick:12"), "counted tick");
        equal(ScenarioStep.reseed(-42L), ScenarioDsl.parse("reseed:-42"), "negative reseed");
        equal(ScenarioStep.tap(4), ScenarioDsl.parse("tap:4"), "tap");
        equal(ScenarioStep.observe("target_2"), ScenarioDsl.parse("observe:target_2"), "observe");
        equal(ScenarioStep.block(-8, 64, 8, 20, 0), ScenarioDsl.parse("block:-8,64,8:20:0"),
                "full block step");
        equal(ScenarioStep.block(8, 64, 8, 20, 0), ScenarioDsl.parse("block:8,64,8:20"),
                "default metadata block step");
        for (ScenarioStep step : java.util.Arrays.asList(ScenarioStep.tick(3),
                ScenarioStep.reseed(Long.MAX_VALUE), ScenarioStep.tap(255),
                ScenarioStep.observe("a1_"), ScenarioStep.block(0, -64, 0, 255, 15))) {
            equal(step, ScenarioDsl.parse(ScenarioDsl.render(step)), "canonical round trip");
        }
        equal("tick", ScenarioDsl.render(ScenarioStep.tick(1)), "tick spelling");
        equal("tick:9", ScenarioDsl.render(ScenarioStep.tick(9)), "counted spelling");
    }

    private static void rejections() {
        rejects(() -> ScenarioDsl.parse(null));
        rejects(() -> ScenarioDsl.parse(""));
        rejects(() -> ScenarioDsl.parse("fly"));
        rejects(() -> ScenarioDsl.parse("tick:0"));
        rejects(() -> ScenarioDsl.parse("tick:" + (ScenarioStep.MAX_TICKS + 1)));
        rejects(() -> ScenarioDsl.parse("tick:+2"));
        rejects(() -> ScenarioDsl.parse("reseed:007"));
        rejects(() -> ScenarioDsl.parse("tap:-1"));
        rejects(() -> ScenarioDsl.parse("tap:" + (ScenarioStep.MAX_KEY + 1)));
        rejects(() -> ScenarioDsl.parse("observe:UPPER"));
        rejects(() -> ScenarioDsl.parse("observe:with space"));
        rejects(() -> ScenarioDsl.parse("block:8,64:20"));
        rejects(() -> ScenarioDsl.parse("block:8,64,8:256"));
        rejects(() -> ScenarioDsl.parse("block:8,64,8:20:16"));
        rejects(() -> ScenarioDsl.parse("block:8,64,8:20:0:extra"));
    }

    private static void validateIntegratesWithScenario() {
        Scenario valid = Scenario.of(java.util.Arrays.asList("observe:before", "block:8,65,8:20",
                "tick", "reseed:101", "observe:after"));
        equal(5, ScenarioDsl.parseAll(valid).size(), "parse all size");
        ScenarioDsl.validate(valid);
        Scenario invalid = Scenario.of(java.util.Arrays.asList("tick", "warp:3"));
        rejects(() -> ScenarioDsl.validate(invalid));
    }

    private static void rejects(Runnable action) {
        try { action.run(); throw new AssertionError("invalid step was accepted"); }
        catch (Exception expected) { }
    }

    private static void equal(Object expected, Object actual, String label) {
        if (!expected.equals(actual) || expected.hashCode() != actual.hashCode()) {
            throw new AssertionError(label + " equality contract failed");
        }
    }
}
