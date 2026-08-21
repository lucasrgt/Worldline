package worldline.testkit;

import java.util.Collections;
import java.util.List;
import worldline.test.WorldlineSpec;

/** Public entry point for deterministic single- and multi-spec execution. */
public final class TestRunner {
    public TestRunResult run(WorldlineSpec spec, RunnerOptions options, TestReporter reporter) {
        if (spec == null) throw new NullPointerException("spec");
        return run(Collections.singletonList(spec), options, reporter);
    }

    public TestRunResult run(List<? extends WorldlineSpec> specs, RunnerOptions options,
            TestReporter reporter) {
        return new TestBatchRunner().run(specs, options, reporter);
    }
}
