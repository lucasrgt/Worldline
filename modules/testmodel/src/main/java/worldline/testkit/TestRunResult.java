package worldline.testkit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Immutable aggregate supplied to reporters and the CLI. */
public final class TestRunResult {
    private final List<TestResult> tests;
    private final long durationMillis;
    private final String fatalError;
    private final boolean emptyAllowed;

    public TestRunResult(List<TestResult> tests, long durationMillis, String fatalError,
            boolean emptyAllowed) {
        this.tests = Collections.unmodifiableList(new ArrayList<>(tests));
        this.durationMillis = durationMillis; this.fatalError = fatalError; this.emptyAllowed = emptyAllowed;
    }
    public List<TestResult> tests() { return tests; }
    public long durationMillis() { return durationMillis; }
    public String fatalError() { return fatalError; }
    public long count(TestStatus status) {
        long count = 0; for (TestResult test : tests) if (test.status() == status) count++; return count;
    }
    public boolean passed() {
        if (fatalError != null || tests.isEmpty() && !emptyAllowed) return false;
        for (TestResult test : tests) if (test.status() == TestStatus.FAILED
                || test.status() == TestStatus.INTERRUPTED || test.status() == TestStatus.FLAKY) return false;
        return true;
    }
}
