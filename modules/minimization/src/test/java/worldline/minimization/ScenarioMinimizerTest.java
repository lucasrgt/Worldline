package worldline.minimization;

import java.util.Arrays;
import java.util.Collections;
import worldline.analysis.TraceDiff;
import worldline.trace.CanonicalStateDocument;

public final class ScenarioMinimizerTest {
    private ScenarioMinimizerTest() {}

    public static void main(String[] arguments) {
        Scenario original = Scenario.of(Arrays.asList("noise:a", "cause", "noise:b", "observe", "noise:c"));
        Scenario parsed = Scenario.parse(original.bytes());
        require(original.equals(parsed) && parsed.size() == 5 && parsed.step(1).equals("cause"),
                "scenario round trip failed");
        ScenarioMinimizer.Result result = ScenarioMinimizer.minimize(original, 100,
                candidate -> candidate.steps().contains("cause") && candidate.steps().contains("observe"),
                step -> step.startsWith("noise:"));
        require(result.complete() && result.minimized().steps().equals(Arrays.asList("cause", "observe"))
                && result.removedSteps() == 3 && result.evaluations() > 1, "scenario was not minimized");
        for (int index = 0; index < result.minimized().size(); index++) {
            java.util.List<String> reduced = new java.util.ArrayList<>(result.minimized().steps());
            reduced.remove(index); require(!(reduced.contains("cause") && reduced.contains("observe")),
                    "result is not one-minimal");
        }
        ScenarioMinimizer.Result partial = ScenarioMinimizer.minimize(original, 1, candidate -> true);
        require(!partial.complete() && partial.minimized().equals(original), "budget exhaustion was hidden");
        rejects(() -> ScenarioMinimizer.minimize(original, 10, candidate -> false));
        rejects(() -> Scenario.of(Collections.singletonList("bad\nstep")));
        byte[] corrupt = original.bytes(); corrupt[corrupt.length - 3] ^= 1;
        rejects(() -> Scenario.parse(corrupt));
        require(Scenario.parse(Scenario.of(Collections.emptyList()).bytes()).size() == 0,
                "empty canonical scenario failed");
        TraceDiff difference = diff(1, 2); DivergenceFingerprint fingerprint = DivergenceFingerprint.from(difference);
        require(fingerprint.matches(diff(1, 2)) && !fingerprint.matches(diff(1, 3)),
                "divergence fingerprint was not exact");
        rejects(() -> DivergenceFingerprint.from(diff(1, 1)));
        System.out.println("ScenarioMinimizerTest passed");
    }

    private static TraceDiff diff(long left, long right) {
        return TraceDiff.compare(CanonicalStateDocument.parse("v2|seed=7|schema=x|tick0=" + left),
                CanonicalStateDocument.parse("v2|seed=7|schema=x|tick0=" + right));
    }
    private static void rejects(Runnable action) {
        try { action.run(); throw new AssertionError("invalid input was accepted"); }
        catch (AssertionError error) { throw error; }
        catch (RuntimeException expected) { }
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
