package worldline.coverage;

import java.util.Arrays;
import worldline.minimization.Scenario;
import worldline.semantics.SemanticRoles;
import worldline.trace.CanonicalStateDocument;

/** Proves category classification, role extraction, and report framing. */
public final class CoverageTest {
    private CoverageTest() {}

    public static void main(String[] arguments) {
        classification();
        rolesFromTrace();
        reportFraming();
        System.out.println("CoverageTest passed");
    }

    private static void classification() {
        Scenario scenario = Scenario.of(Arrays.asList("observe:before", "reseed:5",
                "tap:2", "block:8,65,8:20", "tick:3"));
        ScenarioCoverage coverage = ScenarioCoverage.of(scenario, null);
        require(coverage.categories().equals(Arrays.asList(
                "rng", "input", "tick", "world", "lab")),
                "canonical category order drifted: " + coverage.categories());
        int total = SemanticRoles.categories().size();
        require(coverage.totalCategories() == total
                && coverage.percentCategories() == 5 * 100 / total, "percent math drifted");
        require(coverage.stepCounts().get("tick") == 1
                && coverage.stepCounts().get("lab") == 1, "step counts drifted");
        Scenario single = Scenario.of(Arrays.asList("tick"));
        require(ScenarioCoverage.of(single, null).categories().equals(
                Arrays.asList("tick")), "single classification drifted");
        rejects(() -> ScenarioCoverage.of(null, null));
    }

    private static void rolesFromTrace() {
        CanonicalStateDocument trace = CanonicalStateDocument.parse(
                "v2|seed=7|schema=clientTick,block65,mystery|t0=0,0,0");
        Scenario scenario = Scenario.of(Arrays.asList("observe:t0", "tick"));
        ScenarioCoverage coverage = ScenarioCoverage.of(scenario, trace);
        require(coverage.roles().equals(Arrays.asList("BLOCK_ID_READ",
                "CLIENT_TICK_COUNTER")), "role extraction drifted: " + coverage.roles());
        ScenarioCoverage bare = ScenarioCoverage.of(scenario, null);
        require(bare.roles().isEmpty(), "empty trace invented roles");
    }

    private static void reportFraming() {
        Scenario scenario = Scenario.of(Arrays.asList("observe:before", "tick"));
        CanonicalStateDocument trace = CanonicalStateDocument.parse(
                "v2|seed=7|schema=clientTick|t0=0");
        ScenarioCoverage coverage = ScenarioCoverage.of(scenario, trace);
        CoverageReport report = CoverageReport.of(scenario, trace, coverage);
        String text = new String(report.bytes(), java.nio.charset.StandardCharsets.UTF_8);
        require(text.startsWith("WORLDLINE-COVERAGE/1\nscenario.sha256="
                + scenario.sha256() + "\nsteps=2\ntrace.sha256=" + trace.signature() + "\n"),
                "report header drifted");
        require(text.contains("categories.total=" + SemanticRoles.categories().size())
                && text.contains("categories.touched=tick,lab")
                && text.contains("categories.percent=8")
                && text.contains("steps.tick=1")
                && text.contains("roles.observed=CLIENT_TICK_COUNTER")
                && text.endsWith("sha256=" + report.sha256() + "\n"), "report body drifted");
        CoverageReport bare = CoverageReport.of(scenario, null,
                ScenarioCoverage.of(scenario, null));
        String bareText = new String(bare.bytes(), java.nio.charset.StandardCharsets.UTF_8);
        require(bareText.contains("trace=none") && bareText.contains("roles.observed=none"),
                "bare report drifted");
    }

    private static void rejects(Runnable action) {
        try { action.run(); throw new AssertionError("invalid input was accepted"); }
        catch (Exception expected) { }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
