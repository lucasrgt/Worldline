package worldline.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Collected suite and its ordered hooks and children. */
public final class SuiteDefinition implements TestNode {
    private final String name;
    private final List<TestNode> children = new ArrayList<>();
    private final List<Runnable> beforeAll = new ArrayList<>(), afterAll = new ArrayList<>();
    private final List<TestHook> beforeEach = new ArrayList<>(), afterEach = new ArrayList<>();
    private final List<TestHook> failed = new ArrayList<>(), finished = new ArrayList<>();
    private boolean skip, todo, only, concurrent, frozen;

    SuiteDefinition(String name) { this.name = name; }
    @Override public String name() { return name; }
    public SuiteDefinition skip() { mutable(); skip = true; return this; }
    public SuiteDefinition todo() { mutable(); todo = true; return this; }
    public SuiteDefinition only() { mutable(); only = true; return this; }
    public SuiteDefinition concurrent() { mutable(); concurrent = true; return this; }
    void add(TestNode node) { mutable(); children.add(node); }
    void beforeAll(Runnable value) { mutable(); beforeAll.add(value); }
    void afterAll(Runnable value) { mutable(); afterAll.add(value); }
    void beforeEach(TestHook value) { mutable(); beforeEach.add(value); }
    void afterEach(TestHook value) { mutable(); afterEach.add(value); }
    void onFailed(TestHook value) { mutable(); failed.add(value); }
    void onFinished(TestHook value) { mutable(); finished.add(value); }
    void freeze() {
        frozen = true;
        for (TestNode child : children) {
            if (child instanceof SuiteDefinition) ((SuiteDefinition) child).freeze();
            else ((TestDefinition) child).freeze();
        }
    }
    public List<TestNode> children() { return Collections.unmodifiableList(children); }
    public List<Runnable> beforeAllHooks() { return Collections.unmodifiableList(beforeAll); }
    public List<Runnable> afterAllHooks() { return Collections.unmodifiableList(afterAll); }
    public List<TestHook> beforeEachHooks() { return Collections.unmodifiableList(beforeEach); }
    public List<TestHook> afterEachHooks() { return Collections.unmodifiableList(afterEach); }
    public List<TestHook> failedHooks() { return Collections.unmodifiableList(failed); }
    public List<TestHook> finishedHooks() { return Collections.unmodifiableList(finished); }
    public boolean skipped() { return skip; }
    public boolean todoMode() { return todo; }
    public boolean onlyMode() { return only; }
    public boolean concurrentMode() { return concurrent; }
    private void mutable() { if (frozen) throw new IllegalStateException("test plan is frozen"); }
}
