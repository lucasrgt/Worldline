package worldline.minimization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Deterministic delta debugger with final one-step minimality verification. */
public final class ScenarioMinimizer {
    private ScenarioMinimizer() {}

    @FunctionalInterface
    public interface Evaluator { boolean preserves(Scenario candidate); }

    public static Result minimize(Scenario original, int maxEvaluations, Evaluator evaluator) {
        return minimize(original, maxEvaluations, evaluator, new java.util.function.Predicate<String>() {
            @Override public boolean test(String step) { return false; }
        });
    }

    public static Result minimize(Scenario original, int maxEvaluations, Evaluator evaluator,
            java.util.function.Predicate<String> tryFirst) {
        if (original == null || evaluator == null || tryFirst == null) {
            throw new NullPointerException("minimization input");
        }
        if (maxEvaluations <= 0) throw new IllegalArgumentException("evaluation budget must be positive");
        Session session = new Session(maxEvaluations, evaluator);
        Boolean initial = session.test(original);
        if (!Boolean.TRUE.equals(initial)) throw new IllegalArgumentException("original scenario fails predicate");
        List<String> current = new ArrayList<String>(original.steps());
        for (int index = 0; index < current.size();) {
            if (!tryFirst.test(current.get(index))) { index++; continue; }
            Scenario candidate = without(current, index, index + 1);
            Boolean accepted = session.test(candidate);
            if (accepted == null) return result(original, current, session, false);
            if (accepted) current = new ArrayList<String>(candidate.steps());
            else index++;
        }
        int granularity = 2;
        while (current.size() >= 2) {
            int chunk = (current.size() + granularity - 1) / granularity; boolean reduced = false;
            for (int start = 0; start < current.size(); start += chunk) {
                Scenario candidate = without(current, start, Math.min(current.size(), start + chunk));
                Boolean accepted = session.test(candidate);
                if (accepted == null) return result(original, current, session, false);
                if (accepted) { current = new ArrayList<>(candidate.steps());
                    granularity = Math.max(2, granularity - 1); reduced = true; break; }
            }
            if (reduced) continue;
            if (granularity >= current.size()) break;
            granularity = Math.min(current.size(), granularity * 2);
        }
        for (int index = 0; index < current.size();) {
            Scenario candidate = without(current, index, index + 1); Boolean accepted = session.test(candidate);
            if (accepted == null) return result(original, current, session, false);
            if (accepted) { current = new ArrayList<>(candidate.steps()); index = 0; }
            else index++;
        }
        return result(original, current, session, true);
    }

    private static Result result(Scenario original, List<String> steps, Session session, boolean complete) {
        return new Result(original, Scenario.of(steps), session.evaluations, complete);
    }
    private static Scenario without(List<String> source, int start, int end) {
        List<String> result = new ArrayList<>(source.size() - (end - start));
        result.addAll(source.subList(0, start)); result.addAll(source.subList(end, source.size()));
        return Scenario.of(result);
    }

    public static final class Result {
        private final Scenario original, minimized;
        private final int evaluations;
        private final boolean complete;
        private Result(Scenario original, Scenario minimized, int evaluations, boolean complete) {
            this.original = original; this.minimized = minimized;
            this.evaluations = evaluations; this.complete = complete;
        }
        public Scenario original() { return original; }
        public Scenario minimized() { return minimized; }
        public int evaluations() { return evaluations; }
        public int removedSteps() { return original.size() - minimized.size(); }
        public boolean complete() { return complete; }
    }

    private static final class Session {
        private final int limit; private final Evaluator evaluator;
        private final Map<String, Boolean> cache = new HashMap<>(); private int evaluations;
        Session(int limit, Evaluator evaluator) { this.limit = limit; this.evaluator = evaluator; }
        Boolean test(Scenario scenario) {
            Boolean known = cache.get(scenario.sha256()); if (known != null) return known;
            if (evaluations >= limit) return null;
            boolean result = evaluator.preserves(scenario); evaluations++;
            cache.put(scenario.sha256(), result); return result;
        }
    }
}
