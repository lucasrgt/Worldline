package worldline.testkit;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import worldline.test.TestPlan;
import worldline.test.WorldlineSpec;

/** Collects all files before executing one project-wide reporter lifecycle. */
final class TestBatchRunner {
    TestRunResult run(List<? extends WorldlineSpec> specs, RunnerOptions options,
            TestReporter reporter) {
        if (specs == null || options == null) throw new NullPointerException("runner input");
        TestReporter sink = reporter == null ? new TestReporter() {} : reporter;
        long started = System.nanoTime(); List<TestPlan> plans = new ArrayList<>();
        try {
            for (WorldlineSpec spec : specs) {
                if (spec == null) throw new NullPointerException("spec");
                TestPlan plan = spec.collect(); plans.add(plan);
            }
        } catch (RuntimeException | Error failure) {
            sink.runStarted(plans, 0);
            return finish(sink, new ArrayList<>(), started,
                    "collection failed: " + describe(failure), false);
        }
        boolean hasOnly = false; int selected = 0;
        for (TestPlan plan : plans) hasOnly |= TestPlanExecutor.hasOnly(plan);
        for (TestPlan plan : plans) selected += TestPlanExecutor.select(plan, options, hasOnly).size();
        sink.runStarted(plans, selected);
        for (TestPlan plan : plans) sink.fileCollected(plan);
        if (hasOnly && options.ci && !options.allowOnly)
            return finish(sink, new ArrayList<>(), started, ".only is forbidden in CI", false);
        if (selected == 0) return finish(sink, new ArrayList<>(), started,
                options.passWithNoTests ? null : "no tests matched", options.passWithNoTests);
        List<TestResult> results = new ArrayList<>(); String fatal = null; RunControl control = new RunControl();
        for (TestPlan plan : plans) {
            TestRunResult part = new TestPlanExecutor().run(plan, options, sink, hasOnly, control);
            results.addAll(part.tests());
            if (part.fatalError() != null) { fatal = part.fatalError(); break; }
        }
        return finish(sink, results, started, fatal, false);
    }
    private static TestRunResult finish(TestReporter reporter, List<TestResult> results,
            long started, String fatal, boolean emptyAllowed) {
        TestRunResult run = new TestRunResult(results,
                TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started), fatal, emptyAllowed);
        reporter.runFinished(run); return run;
    }
    private static String describe(Throwable failure) {
        return failure.getClass().getSimpleName() + ": " + String.valueOf(failure.getMessage());
    }
}
