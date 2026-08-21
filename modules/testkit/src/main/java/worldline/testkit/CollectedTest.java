package worldline.testkit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import worldline.test.SuiteDefinition;
import worldline.test.TestDefinition;
import worldline.test.TestHook;

/** Runner-private flattened test with inherited suite state. */
final class CollectedTest {
    final TestDefinition definition;
    final String path;
    final List<SuiteDefinition> suites;
    final List<TestHook> beforeEach, afterEach, failed, finished;
    final boolean skipped, todo, only, concurrent;

    CollectedTest(TestDefinition definition, String path, List<SuiteDefinition> suites,
            List<TestHook> beforeEach, List<TestHook> afterEach, List<TestHook> failed,
            List<TestHook> finished, boolean skipped, boolean todo, boolean only,
            boolean concurrent) {
        this.definition = definition; this.path = path;
        this.suites = immutable(suites); this.beforeEach = immutable(beforeEach);
        this.afterEach = immutable(afterEach); this.failed = immutable(failed);
        this.finished = immutable(finished); this.skipped = skipped; this.todo = todo;
        this.only = only; this.concurrent = concurrent;
    }

    private static <T> List<T> immutable(List<T> value) {
        return Collections.unmodifiableList(new ArrayList<>(value));
    }
}
