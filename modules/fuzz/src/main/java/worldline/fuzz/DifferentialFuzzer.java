package worldline.fuzz;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import worldline.minimization.Scenario;
import worldline.trace.CanonicalStateDocument;

/** Deterministic differential fuzzer over public-grammar scenarios. */
public final class DifferentialFuzzer {
    /** Immutable outcome: every finding plus the total execution count. */
    public static final class Result {
        private final List<FuzzFinding> findings;
        private final int evaluations;

        Result(List<FuzzFinding> findings, int evaluations) {
            this.findings = Collections.unmodifiableList(new ArrayList<>(findings));
            this.evaluations = evaluations;
        }

        public List<FuzzFinding> findings() { return findings; }
        public int evaluations() { return evaluations; }
    }

    private final List<FuzzSubject> subjects;
    private final long seed;
    private int evaluations;

    private DifferentialFuzzer(List<FuzzSubject> subjects, long seed) {
        this.subjects = subjects; this.seed = seed; this.evaluations = 0;
    }

    /**
     * Executes every plan scenario against the subject matrix. One subject
     * reruns itself to hunt nondeterminism; several subjects are compared
     * pairwise and divergences are shrunk by the M9 minimizer.
     */
    public static Result fuzz(List<FuzzSubject> subjects, FuzzPlan plan, long seed,
            boolean stopOnFirst, int minimizerBudget) {
        if (subjects == null || subjects.isEmpty()) {
            throw new IllegalArgumentException("fuzzing requires at least one subject");
        }
        return new DifferentialFuzzer(subjects, seed).execute(plan, stopOnFirst, minimizerBudget);
    }

    private Result execute(FuzzPlan plan, boolean stopOnFirst, int budget) {
        List<FuzzFinding> findings = new ArrayList<>();
        for (Scenario scenario : plan.scenarios()) {
            if (subjects.size() == 1) {
                FuzzFinding finding = selfCheck(subjects.get(0), scenario);
                if (finding != null) { findings.add(finding); break; }
            } else {
                FuzzFinding finding = crossCheck(scenario, budget);
                if (finding != null) {
                    findings.add(finding);
                    if (stopOnFirst) break;
                }
            }
        }
        return new Result(findings, evaluations);
    }

    private FuzzFinding selfCheck(FuzzSubject subject, Scenario scenario) {
        CanonicalStateDocument first = execute(subject, scenario);
        CanonicalStateDocument second = execute(subject, scenario);
        if (!first.canonical().equals(second.canonical())) {
            return new FuzzFinding(FuzzFinding.Kind.NONDETERMINISM,
                    subject.label(), subject.label(), scenario, null);
        }
        return null;
    }

    private FuzzFinding crossCheck(Scenario scenario, int budget) {
        for (int index = 0; index < subjects.size() - 1; index++) {
            FuzzSubject left = subjects.get(index), right = subjects.get(index + 1);
            if (diverges(left, right, scenario)) {
                Scenario minimized = shrink(left, right, scenario, budget);
                return new FuzzFinding(FuzzFinding.Kind.DIVERGENCE,
                        left.label(), right.label(), scenario, minimized);
            }
        }
        return null;
    }

    private boolean diverges(FuzzSubject left, FuzzSubject right, Scenario scenario) {
        return !execute(left, scenario).canonical().equals(execute(right, scenario).canonical());
    }

    private Scenario shrink(FuzzSubject left, FuzzSubject right, Scenario original, int budget) {
        worldline.minimization.ScenarioMinimizer.Result result =
                worldline.minimization.ScenarioMinimizer.minimize(original, budget,
                        candidate -> diverges(left, right, candidate),
                        worldline.semantics.SemanticSteps::disposable);
        return result.minimized();
    }

    private CanonicalStateDocument execute(FuzzSubject subject, Scenario scenario) {
        evaluations++;
        try {
            return subject.run(scenario, seed);
        } catch (RuntimeException failure) {
            Throwable cause = failure;
            while (cause.getCause() != null) cause = cause.getCause();
            throw new IllegalStateException("subject " + subject.label()
                    + " failed on a candidate: " + cause, failure);
        }
    }

    /** Base64url encoding of one canonical scenario for report embedding. */
    static String embed(Scenario scenario) {
        byte[] bytes = scenario.bytes();
        String encoded = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        return encoded + "." + scenario.sha256();
    }
}
