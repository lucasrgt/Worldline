package worldline.fuzz;

import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import worldline.minimization.Scenario;
import worldline.minimization.ScenarioDsl;
import worldline.trace.CanonicalStateDocument;

/** Proves deterministic planning, differential detection, and auto-shrinking. */
public final class FuzzTest {
    private FuzzTest() {}

    public static void main(String[] arguments) {
        if (arguments.length == 4 && arguments[0].equals("--explore")) {
            explore(Long.parseLong(arguments[1]), Integer.parseInt(arguments[2]),
                    Integer.parseInt(arguments[3]));
            return;
        }
        require(arguments.length == 0, "usage: FuzzTest [--explore SEED CASES MAX_STEPS]");
        planIsDeterministicAndBounded();
        crossSubjectShrink();
        selfSubjectNondeterminism();
        cleanCampaign();
        reportIsCanonical();
        System.out.println("FuzzTest passed");
    }

    private static void explore(long seed, int cases, int steps) {
        FuzzSubject left = subject("nightly-left", candidate -> document(candidate.size()));
        FuzzSubject right = subject("nightly-right", candidate -> document(candidate.size()));
        FuzzPlan plan = FuzzPlan.generate(seed, cases, steps);
        DifferentialFuzzer.Result result = DifferentialFuzzer.fuzz(
                Arrays.asList(left, right), plan, seed, false, 100);
        require(result.findings().isEmpty() && result.evaluations() == cases * 2,
                "nightly differential campaign drifted");
        for (Scenario scenario : plan.scenarios()) ScenarioDsl.validate(scenario);
        System.out.println("FuzzTest exploratory campaign passed: seed=" + seed
                + ", cases=" + cases + ", steps=" + steps + ", evaluations=" + result.evaluations());
    }

    private static void planIsDeterministicAndBounded() {
        FuzzPlan first = FuzzPlan.generate(42L, 12, 6);
        FuzzPlan second = FuzzPlan.generate(42L, 12, 6);
        FuzzPlan other = FuzzPlan.generate(43L, 12, 6);
        require(first.size() == 12 && other.size() == 12, "plan size lost");
        for (int index = 0; index < 12; index++) {
            require(first.scenarios().get(index).equals(second.scenarios().get(index)),
                    "plan is not deterministic");
            ScenarioDsl.validate(first.scenarios().get(index));
            int size = first.scenarios().get(index).size();
            require(size >= 1 && size <= 6, "plan violated step bounds");
        }
        boolean differs = false;
        for (int index = 0; index < 12 && !differs; index++) {
            differs = !first.scenarios().get(index).equals(other.scenarios().get(index));
        }
        require(differs, "different seeds produced identical plans");
        rejects(() -> FuzzPlan.generate(1L, 0, 4));
        rejects(() -> FuzzPlan.generate(1L, 4, 0));
    }

    private static void crossSubjectShrink() {
        FuzzSubject quiet = subject("quiet", candidate -> document(0));
        FuzzSubject loud = subject("loud", candidate ->
                document(candidate.steps().contains("tick:2") ? 7 : 0));
        Scenario planted = Scenario.of(Arrays.asList("observe:before",
                "tap:3", "tick:2", "reseed:9", "observe:target"));
        FuzzPlan plan = new FuzzPlan(Arrays.asList(planted));
        DifferentialFuzzer.Result result = DifferentialFuzzer.fuzz(
                Arrays.asList(quiet, loud), plan, 5L, true, 100);
        require(result.findings().size() == 1, "planted divergence was not found");
        FuzzFinding finding = result.findings().get(0);
        require(finding.kind() == FuzzFinding.Kind.DIVERGENCE
                && finding.leftSubject().equals("quiet")
                && finding.rightSubject().equals("loud"), "finding metadata lost");
        require(finding.minimized() != null
                && finding.minimized().steps().equals(Arrays.asList("tick:2")),
                "minimizer did not isolate the cause: " + finding.minimized().steps());
    }

    private static void selfSubjectNondeterminism() {
        final int[] calls = {0};
        FuzzSubject flaky = new FuzzSubject() {
            @Override public String label() { return "flaky"; }
            @Override public CanonicalStateDocument run(Scenario scenario, long seed) {
                calls[0]++;
                return document(calls[0]);
            }
        };
        DifferentialFuzzer.Result result = DifferentialFuzzer.fuzz(
                Collections.singletonList(flaky),
                new FuzzPlan(Arrays.asList(Scenario.of(Collections.singletonList("tick")))),
                1L, false, 10);
        require(result.findings().size() == 1
                && result.findings().get(0).kind() == FuzzFinding.Kind.NONDETERMINISM
                && result.findings().get(0).minimized() == null
                && result.evaluations() == 2, "nondeterminism hunt failed");
    }

    private static void cleanCampaign() {
        FuzzSubject same = subject("same", candidate -> document(3));
        DifferentialFuzzer.Result result = DifferentialFuzzer.fuzz(
                Arrays.asList(same, same), FuzzPlan.generate(7L, 4, 3), 9L, false, 50);
        require(result.findings().isEmpty(), "clean campaign reported findings");
        require(result.evaluations() == 8, "unexpected evaluation count");
    }

    private static void reportIsCanonical() {
        FuzzSubject quiet = subject("quiet", candidate -> document(0));
        FuzzSubject loud = subject("loud", candidate ->
                document(candidate.steps().contains("tick:2") ? 7 : 0));
        DifferentialFuzzer.Result result = DifferentialFuzzer.fuzz(
                Arrays.asList(quiet, loud),
                new FuzzPlan(Arrays.asList(Scenario.of(Collections.singletonList("tick:2")))),
                3L, false, 100);
        FuzzReport report = FuzzReport.of(3L, 1, 4,
                Arrays.asList("quiet", "loud"), result);
        String text = new String(report.bytes(), java.nio.charset.StandardCharsets.UTF_8);
        require(text.startsWith("WORLDLINE-FUZZ/1\nseed=3\ncases=1\nmax-steps=4\n"),
                "header drifted");
        require(text.contains("subjects=quiet,loud"), "subjects line drifted");
        require(text.contains("findings=1"), "findings count drifted");
        require(text.contains("finding.0.kind=divergence"), "kind line drifted");
        require(text.endsWith("sha256=" + report.sha256() + "\n"), "checksum line drifted");
        String embedded = line(text, "finding.0.minimized=");
        String encoded = embedded.substring(0, embedded.lastIndexOf('.'));
        Scenario restored = Scenario.parse(Base64.getUrlDecoder().decode(encoded));
        require(restored.equals(result.findings().get(0).minimized()),
                "embedded scenario did not round trip");
        rejects(() -> FuzzReport.of(1L, 1, 1, Arrays.asList("Bad Label"), result));
    }

    private static FuzzSubject subject(String label,
            java.util.function.Function<Scenario, CanonicalStateDocument> behavior) {
        return new FuzzSubject() {
            @Override public String label() { return label; }
            @Override public CanonicalStateDocument run(Scenario scenario, long seed) {
                return behavior.apply(scenario);
            }
        };
    }

    private static CanonicalStateDocument document(long value) {
        return CanonicalStateDocument.parse("v2|seed=1|schema=x|t0=" + value);
    }

    private static String line(String text, String prefix) {
        for (String row : text.split("\n", -1)) {
            if (row.startsWith(prefix)) return row.substring(prefix.length());
        }
        throw new AssertionError("missing " + prefix);
    }

    private static void rejects(Runnable action) {
        try { action.run(); throw new AssertionError("invalid input was accepted"); }
        catch (Exception expected) { }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
