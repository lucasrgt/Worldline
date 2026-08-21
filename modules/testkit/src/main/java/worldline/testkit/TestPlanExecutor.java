package worldline.testkit;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import worldline.test.SuiteDefinition;
import worldline.test.TestPlan;

/** Executes one already-collected plan without owning reporter lifecycle. */
final class TestPlanExecutor {
    TestRunResult run(TestPlan plan, RunnerOptions options, TestReporter sink, boolean globalOnly,
            RunControl control) {
        List<CollectedTest> selected = select(plan, options, globalOnly);
        List<TestResult> results = new ArrayList<>(); List<SuiteDefinition> active = new ArrayList<>();
        String fatal = null;
        for (int index = 0; index < selected.size(); index++) {
            CollectedTest test = selected.get(index);
            if (options.bail > 0 && control.failures >= options.bail) {
                TestResult interrupted = terminal(test, index, plan.specName(), TestStatus.INTERRUPTED,
                        "bail threshold reached", options.seed);
                results.add(interrupted); sink.testFinished(interrupted); continue;
            }
            if (!test.skipped && !test.todo) try { transition(active, test.suites); }
            catch (RuntimeException | Error failure) {
                fatal = "suite hook failed: " + describe(failure); break;
            }
            sink.testQueued(test.path); TestResult result = execute(index, plan.specName(), test, options, sink);
            results.add(result);
            for (java.nio.file.Path artifact : result.artifacts()) sink.artifactRecorded(test.path, artifact);
            sink.testFinished(result);
            if (RuntimeIsolationException.class.getName().equals(result.errorType())) {
                fatal = result.errorMessage(); break;
            }
            if (result.status() == TestStatus.FAILED || result.status() == TestStatus.INTERRUPTED)
                control.failures++;
        }
        try { exit(active, 0); }
        catch (RuntimeException | Error failure) { fatal = append(fatal, "afterAll failed: " + describe(failure)); }
        return new TestRunResult(results, 0, fatal, true);
    }

    private TestResult execute(int index, String spec, CollectedTest test, RunnerOptions options,
            TestReporter reporter) {
        TestSettings settings = TestSettings.resolve(test.definition.body(), options);
        if (test.todo) return terminal(test, index, spec, TestStatus.TODO, "todo", settings.seed);
        if (test.skipped) return terminal(test, index, spec, TestStatus.SKIPPED, "skipped", settings.seed);
        try { settings.validate(options); }
        catch (RuntimeException failure) { return failure(test, index, spec, 0, 0, settings.seed, failure, null); }
        if (test.concurrent && options.provider != null) return failure(test, index, spec, 0, 0,
                settings.seed, new IllegalStateException("official runtime tests cannot be concurrent"), null);
        String id = id(spec, test.path, index); ArtifactStore artifacts;
        try { artifacts = new ArtifactStore(options.artifacts, id); }
        catch (Exception failure) { return failure(test, index, spec, 0, 0, settings.seed, failure, null); }
        int retries = Math.max(options.retry, test.definition.retries()); long duration = 0;
        AttemptOutcome last = null; AttemptExecutor executor = new AttemptExecutor(test, options, settings, artifacts);
        for (int attempt = 1; attempt <= retries + 1; attempt++) {
            reporter.testStarted(test.path, attempt);
            long timeout = options.timeoutOverride ? options.timeout : test.definition.timeoutMillis();
            last = executor.execute(attempt, timeout, null, true); duration += last.durationMillis;
            if (last.skipReason != null && last.failure == null) return result(test, id, spec,
                    TestStatus.SKIPPED, last.skipReason, null, duration, attempt, settings.seed, artifacts);
            if (last.failure == null) return result(test, id, spec,
                    attempt == 1 ? TestStatus.PASSED : TestStatus.FLAKY,
                    attempt == 1 ? null : "passed after retry", null, duration, attempt, settings.seed, artifacts);
        }
        if (options.minimize && last != null && last.context != null && last.context.scenario() != null)
            executor.minimize(last, options.timeoutOverride ? options.timeout : test.definition.timeoutMillis());
        if (last != null) executor.recordModTest(last);
        return failure(test, index, spec, duration, retries + 1, settings.seed, last.failure, artifacts);
    }

    static boolean hasOnly(TestPlan plan) {
        for (CollectedTest test : PlanIndex.collect(plan)) if (test.only) return true; return false;
    }
    static List<CollectedTest> select(TestPlan plan, RunnerOptions options, boolean hasOnly) {
        List<CollectedTest> selected = new ArrayList<>();
        for (CollectedTest test : PlanIndex.collect(plan)) {
            if (hasOnly && !test.only) continue;
            if (options.namePattern != null && !options.namePattern.matcher(test.path).find()) continue;
            if (options.filePattern != null
                    && !options.filePattern.matcher(test.definition.location().file()).find()) continue;
            if (options.tag != null && !test.definition.tags().contains(options.tag)) continue;
            if (options.line > 0 && test.definition.location().line() != options.line) continue;
            selected.add(test);
        }
        if (options.shuffle) Collections.shuffle(selected, new Random(options.seed)); return selected;
    }
    private static void transition(List<SuiteDefinition> active, List<SuiteDefinition> target) {
        int common = 0;
        while (common < active.size() && common < target.size() && active.get(common) == target.get(common)) common++;
        exit(active, common);
        for (int index = common; index < target.size(); index++) {
            SuiteDefinition suite = target.get(index);
            for (Runnable hook : suite.beforeAllHooks()) hook.run(); active.add(suite);
        }
    }
    private static void exit(List<SuiteDefinition> active, int remaining) {
        for (int index = active.size() - 1; index >= remaining; index--) {
            List<Runnable> hooks = active.remove(index).afterAllHooks();
            for (int hook = hooks.size() - 1; hook >= 0; hook--) hooks.get(hook).run();
        }
    }
    private static TestResult terminal(CollectedTest test, int index, String spec,
            TestStatus status, String note, long seed) {
        return result(test, id(spec, test.path, index), spec, status, note, null, 0, 0, seed, null);
    }
    private static TestResult failure(CollectedTest test, int index, String spec, long duration,
            int attempts, long seed, Throwable failure, ArtifactStore artifacts) {
        return result(test, id(spec, test.path, index), spec,
                interrupted(failure) ? TestStatus.INTERRUPTED : TestStatus.FAILED,
                null, failure, duration, attempts, seed, artifacts);
    }
    private static TestResult result(CollectedTest test, String id, String spec, TestStatus status,
            String note, Throwable failure, long duration, int attempts, long seed, ArtifactStore artifacts) {
        TestResult.Builder builder = TestResult.builder(id, spec, test.path, test.definition.location());
        builder.status = status; builder.note = note; builder.durationMillis = duration;
        builder.attempts = attempts; builder.seed = seed; builder.tags.addAll(test.definition.tags());
        if (failure != null) {
            builder.errorType = failure.getClass().getName(); builder.errorMessage = failure.getMessage();
            if (failure instanceof worldline.test.WorldlineAssertionError) {
                worldline.test.WorldlineAssertionError assertion = (worldline.test.WorldlineAssertionError) failure;
                builder.expected = assertion.expected(); builder.received = assertion.received();
                if (assertion.divergence() != null) {
                    builder.divergenceTick = assertion.divergence().tick();
                    builder.divergenceField = assertion.divergence().field();
                    builder.divergenceRole = assertion.divergence().role();
                }
            }
        }
        if (artifacts != null) builder.artifacts.addAll(artifacts.files()); return builder.build();
    }
    private static boolean interrupted(Throwable failure) {
        for (Throwable value = failure; value != null; value = value.getCause())
            if (value instanceof InterruptedException) return true; return false;
    }
    private static String id(String spec, String path, int index) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(
                    (spec + "\n" + path + "\n" + index).getBytes(StandardCharsets.UTF_8));
            StringBuilder value = new StringBuilder("t");
            for (int item = 0; item < 8; item++) value.append(String.format("%02x", digest[item] & 255));
            return value.toString();
        } catch (NoSuchAlgorithmException error) { throw new IllegalStateException(error); }
    }
    private static String append(String left, String right) { return left == null ? right : left + "; " + right; }
    private static String describe(Throwable failure) {
        return failure.getClass().getSimpleName() + ": " + String.valueOf(failure.getMessage());
    }
}
