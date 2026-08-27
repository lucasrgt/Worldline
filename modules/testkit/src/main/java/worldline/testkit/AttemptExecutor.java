package worldline.testkit;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import worldline.minimization.Scenario;
import worldline.minimization.ScenarioMinimizer;
import worldline.mods.ModArtifact;
import worldline.mods.ModLoader;
import worldline.modtest.ModTestResult;
import worldline.test.SnapshotExpectation;
import worldline.test.TestHook;
import worldline.test.TestObservation;
import worldline.test.TestMappingAccess;
import worldline.test.TestRuntimeRequest;
import worldline.test.TestRuntimeSession;
import worldline.trace.CanonicalStateDocument;

/** Fresh-runtime attempt execution and optional deterministic minimization. */
final class AttemptExecutor {
    private final CollectedTest test;
    private final RunnerOptions options;
    private final TestSettings settings;
    private final ArtifactStore artifacts;
    private final AtomicReference<TestExecutionContext> activeContext = new AtomicReference<>();

    AttemptExecutor(CollectedTest test, RunnerOptions options, TestSettings settings, ArtifactStore artifacts) {
        this.test = test; this.options = options; this.settings = settings; this.artifacts = artifacts;
    }

    AttemptOutcome execute(int number, long timeout, List<String> allowed, boolean writeFailure) {
        ExecutorService executor = Executors.newSingleThreadExecutor(new DaemonThreads());
        long started = System.nanoTime();
        Future<AttemptOutcome> future = executor.submit(new Attempt(number, allowed, writeFailure));
        try { return future.get(timeout, TimeUnit.MILLISECONDS); }
        catch (TimeoutException error) {
            future.cancel(true);
            try { artifacts.write("timeout-inventory.txt", TimeoutInventory.capture()); }
            catch (Exception ignored) { /* timeout remains authoritative */ }
            TestExecutionContext context = activeContext.get();
            try { if (context != null) context.writeTimeout();
                else artifacts.write("timeout.wltrace", new byte[0]); }
            catch (Exception ignored) { /* timeout remains authoritative */ }
            executor.shutdownNow(); boolean stopped = terminated(executor);
            Throwable timeoutFailure = options.provider != null && !stopped
                    ? new RuntimeIsolationException("timed-out runtime thread remained active; execution stopped")
                    : new TimeoutException("test timed out after " + timeout + "ms");
            return new AttemptOutcome(timeoutFailure, null, context, elapsed(started));
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt(); future.cancel(true);
            return new AttemptOutcome(error, null, null, elapsed(started));
        } catch (ExecutionException error) {
            Throwable cause = error.getCause() == null ? error : error.getCause();
            return new AttemptOutcome(cause, null, null, elapsed(started));
        } finally { executor.shutdownNow(); }
    }

    void minimize(AttemptOutcome original, long timeout) {
        Throwable expected = original.failure; Scenario scenario = original.context.scenario();
        try {
            ScenarioMinimizer.Result result = ScenarioMinimizer.minimize(scenario,
                    options.minimizeBudget, candidate -> sameFailure(expected,
                            execute(1, timeout, candidate.steps(), false).failure));
            artifacts.write("minimized.wlscenario", result.minimized().bytes());
        } catch (Exception failure) {
            try { artifacts.write("minimization.txt", describe(failure)
                    .getBytes(StandardCharsets.UTF_8)); } catch (Exception ignored) { /* diagnostic only */ }
        }
    }

    void recordModTest(AttemptOutcome outcome) {
        if (settings.mod == null || outcome.context == null || outcome.context.trace() == null) return;
        try {
            String runtime = options.provider == null ? "unknown" : options.provider.runtimeId();
            ModArtifact mod = ModLoader.inspect(settings.mod, runtime, "1");
            ModTestResult result = ModTestResult.create(mod,
                    CanonicalStateDocument.parse(outcome.context.trace()));
            artifacts.write("failure.wlmtest", result.bytes());
        } catch (Exception failure) {
            try { artifacts.write("mod-test-artifact.txt", describe(failure)
                    .getBytes(StandardCharsets.UTF_8)); } catch (Exception ignored) { /* diagnostic only */ }
        }
    }

    private final class Attempt implements Callable<AttemptOutcome> {
        private final int number; private final List<String> allowed; private final boolean writeFailure;
        Attempt(int number, List<String> allowed, boolean writeFailure) {
            this.number = number; this.allowed = allowed; this.writeFailure = writeFailure;
        }
        @Override public AttemptOutcome call() {
            long started = System.nanoTime(); TestRuntimeSession session = null;
            RuntimeLease lease = null; TestExecutionContext context = null;
            Throwable failure = null; String skipped = null;
            try {
                if (options.provider != null) {
                    lease = RuntimeLease.acquire(options.runtimeLock);
                    session = options.provider.open(
                            new TestRuntimeRequest(settings.seed, settings.world, settings.mod));
                }
                context = new TestExecutionContext(settings.seed, number,
                        session, artifacts, allowed);
                activeContext.set(context);
                SnapshotStore store = new SnapshotStore(options.snapshots,
                        allowed == null && options.updateSnapshots);
                SnapshotExpectation.install((name, value) -> store.match(test.path, name, value));
                for (TestHook hook : test.beforeEach) hook.run(context);
                test.definition.body().run(context);
            } catch (SkipSignal signal) { skipped = signal.getMessage(); }
            catch (Throwable caught) { failure = caught; }
            finally {
                SnapshotExpectation.clear();
                if (context != null) {
                    if (failure != null) failure = hooks(test.failed, context, failure);
                    if (failure != null) failure = hooks(context.dynamicFailed(), context, failure);
                    failure = hooks(test.afterEach, context, failure);
                    if (skipped == null) failure = hooks(context.dynamicFinished(), context, failure);
                    if (skipped == null) failure = hooks(test.finished, context, failure);
                    if (writeFailure && failure != null) try { context.writeFailure(failure); }
                    catch (Throwable artifactFailure) { failure.addSuppressed(artifactFailure); }
                }
                if (session != null) try { session.close(); }
                catch (Throwable closeFailure) { failure = combine(failure, closeFailure); }
                if (lease != null) try { lease.close(); }
                catch (Throwable lockFailure) { failure = combine(failure, lockFailure); }
                TestObservation.clear();
                TestMappingAccess.clear();
                activeContext.compareAndSet(context, null);
            }
            return new AttemptOutcome(failure, skipped, context, elapsed(started));
        }
    }

    private static Throwable hooks(List<TestHook> hooks, TestExecutionContext context, Throwable failure) {
        for (TestHook hook : hooks) try { hook.run(context); }
        catch (Throwable hookFailure) { failure = combine(failure, hookFailure); }
        return failure;
    }
    private static Throwable combine(Throwable primary, Throwable next) {
        if (primary == null) return next; primary.addSuppressed(next); return primary;
    }
    private static boolean sameFailure(Throwable left, Throwable right) {
        return left != null && right != null && left.getClass().equals(right.getClass())
                && String.valueOf(left.getMessage()).equals(String.valueOf(right.getMessage()));
    }
    private static long elapsed(long started) {
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started);
    }
    private static boolean terminated(ExecutorService executor) {
        try { return executor.awaitTermination(2, TimeUnit.SECONDS); }
        catch (InterruptedException error) { Thread.currentThread().interrupt(); return false; }
    }
    private static String describe(Throwable failure) {
        return failure.getClass().getSimpleName() + ": " + String.valueOf(failure.getMessage());
    }
    private static final class DaemonThreads implements ThreadFactory {
        @Override public Thread newThread(Runnable task) {
            Thread thread = new Thread(task, "worldline-test-attempt"); thread.setDaemon(true); return thread;
        }
    }
}
