package worldline.testkit;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import worldline.test.TestRuntimeSession;

/** Diagnostic projection of actual runner events, scoped to one batch and attempt. */
final class RunnerProgress {
    private final int selected;
    private final boolean enabled;
    private final String runtime;
    private int completed, passed, failed, skipped, flaky;
    private String lastFailure = "";

    RunnerProgress(int selected, boolean enabled, String runtime) {
        this.selected = selected; this.enabled = enabled; this.runtime = runtime;
    }

    void finished(TestResult result) {
        completed++;
        switch (result.status()) {
            case PASSED: passed++; break;
            case FLAKY: flaky++; break;
            case FAILED: case INTERRUPTED:
                failed++; lastFailure = clean(result.path() + ": " + result.errorMessage()); break;
            default: skipped++; break;
        }
    }

    Attempt attempt(String spec, String path, int number, int maximum, long seed, long timeout) {
        return new Attempt(spec, path, number, maximum, seed, timeout);
    }

    final class Attempt {
        private final String spec, path;
        private final int number, maximum;
        private final long seed, timeout, started = System.nanoTime();
        private TestRuntimeSession session;
        private String phase = "Preparing", detail = "", status = "RUNNING";
        private long ticks, lastPublish;
        private boolean closed;

        Attempt(String spec, String path, int number, int maximum, long seed, long timeout) {
            this.spec = spec; this.path = path; this.number = number; this.maximum = maximum;
            this.seed = seed; this.timeout = timeout;
        }

        synchronized void open(TestRuntimeSession value) { session = value; publish(); }
        synchronized void phase(String value) { phase = value; publish(); }
        synchronized void tick() {
            ticks++;
            if (System.nanoTime() - lastPublish >= TimeUnit.MILLISECONDS.toNanos(100)) publish();
        }
        synchronized void result(Throwable failure, String skip) {
            status = failure != null ? "FAILED" : skip != null ? "SKIPPED" : number > 1 ? "FLAKY" : "PASSED";
            phase = failure != null && number < maximum ? "Retry pending" : "Closing session";
            detail = failure != null ? failure.getClass().getSimpleName() + ": " + failure.getMessage()
                    : skip == null ? "" : skip;
            publish();
        }
        synchronized void close() { closed = true; session = null; }

        private void publish() {
            if (!enabled || closed || session == null) return;
            lastPublish = System.nanoTime();
            boolean terminal = !"RUNNING".equals(status) && !("FAILED".equals(status) && number < maximum);
            int done = completed + (terminal ? 1 : 0);
            int leaf = path.lastIndexOf(" > ");
            List<String> lines = new ArrayList<>();
            lines.add(status + ": " + (completed + 1) + "/" + selected);
            lines.add("Suite: " + clean(spec.substring(spec.lastIndexOf('.') + 1)));
            lines.add("Test: " + clean(path.substring(leaf < 0 ? 0 : leaf + 3)));
            lines.add("Step: " + clean(phase.replace("extension-fixture:", "Fixture: ")
                    .replace("extension-action:", "Action: ").replace("extension-observation:", "Observe: ")
                    .replace("extension-oracle:", "Oracle: ")));
            if (!detail.isEmpty()) lines.add(("FAILED".equals(status) ? "Failure: " : "Detail: ") + clean(detail));
            else if (!lastFailure.isEmpty()) lines.add("Last failure: " + lastFailure);
            lines.add("Runtime: " + clean(runtime));
            lines.add("Seed: " + seed);
            lines.add("Attempt: " + number + "/" + maximum);
            lines.add("Elapsed: " + TimeUnit.NANOSECONDS.toMillis(lastPublish - started) + "ms");
            lines.add("Timeout: " + timeout + "ms");
            lines.add("Done: " + done + "/" + selected);
            lines.add("Passed: " + (passed + ("PASSED".equals(status) ? 1 : 0))
                    + " | Failed: " + (failed + (terminal && "FAILED".equals(status) ? 1 : 0)));
            lines.add("Skipped: " + (skipped + ("SKIPPED".equals(status) ? 1 : 0))
                    + " | Flaky: " + (flaky + ("FLAKY".equals(status) ? 1 : 0)));
            lines.add("Ticks: " + ticks);
            session.showProgress(lines);
        }
    }

    private static String clean(String value) {
        if (value == null) return "";
        StringBuilder text = new StringBuilder();
        for (int index = 0; index < value.length() && text.length() < 220; index++) {
            char item = value.charAt(index); text.append(item >= 32 && item <= 126 ? item : '?');
        }
        return text.toString();
    }
}
