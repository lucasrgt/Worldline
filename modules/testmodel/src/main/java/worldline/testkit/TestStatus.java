package worldline.testkit;

/** Terminal and intermediate states visible to reporters. */
public enum TestStatus {
    QUEUED, RUNNING, PASSED, FAILED, SKIPPED, TODO, INTERRUPTED, FLAKY
}
