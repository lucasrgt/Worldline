package worldline.testkit;

import java.util.ArrayList;
import java.util.List;
import worldline.test.SuiteDefinition;
import worldline.test.TestDefinition;
import worldline.test.TestHook;
import worldline.test.TestNode;
import worldline.test.TestPlan;

/** Deterministically flattens a collected plan while preserving declaration order. */
final class PlanIndex {
    private final List<CollectedTest> tests = new ArrayList<>();

    static List<CollectedTest> collect(TestPlan plan) {
        PlanIndex index = new PlanIndex();
        index.visit(plan.root(), new ArrayList<SuiteDefinition>(), new ArrayList<TestHook>(),
                new ArrayList<TestHook>(), new ArrayList<TestHook>(), new ArrayList<TestHook>(),
                false, false, false, false, "");
        return index.tests;
    }

    private void visit(SuiteDefinition suite, List<SuiteDefinition> parents,
            List<TestHook> before, List<TestHook> after, List<TestHook> failed,
            List<TestHook> finished, boolean skip, boolean todo, boolean only,
            boolean concurrent, String prefix) {
        List<SuiteDefinition> suites = plus(parents, suite);
        List<TestHook> nextBefore = join(before, suite.beforeEachHooks(), false);
        List<TestHook> nextAfter = join(after, suite.afterEachHooks(), true);
        List<TestHook> nextFailed = join(failed, suite.failedHooks(), true);
        List<TestHook> nextFinished = join(finished, suite.finishedHooks(), true);
        String nextPrefix = "root".equals(suite.name()) ? prefix : append(prefix, suite.name());
        boolean nextSkip = skip || suite.skipped(), nextTodo = todo || suite.todoMode();
        boolean nextOnly = only || suite.onlyMode(), nextConcurrent = concurrent || suite.concurrentMode();
        for (TestNode child : suite.children()) {
            if (child instanceof SuiteDefinition) {
                visit((SuiteDefinition) child, suites, nextBefore, nextAfter, nextFailed,
                        nextFinished, nextSkip, nextTodo, nextOnly, nextConcurrent, nextPrefix);
            } else {
                TestDefinition test = (TestDefinition) child;
                tests.add(new CollectedTest(test, append(nextPrefix, test.name()), suites,
                        nextBefore, nextAfter, nextFailed, nextFinished,
                        nextSkip || test.skipped(), nextTodo || test.todoMode(),
                        nextOnly || test.onlyMode(), nextConcurrent || test.concurrentMode()));
            }
        }
    }

    private static String append(String prefix, String name) {
        return prefix.isEmpty() ? name : prefix + " > " + name;
    }
    private static <T> List<T> plus(List<T> values, T value) {
        List<T> result = new ArrayList<>(values); result.add(value); return result;
    }
    private static <T> List<T> join(List<T> left, List<T> right, boolean reverseRight) {
        List<T> result = new ArrayList<>();
        if (reverseRight) for (int index = right.size() - 1; index >= 0; index--) result.add(right.get(index));
        result.addAll(left);
        if (!reverseRight) result.addAll(right);
        return result;
    }
}
