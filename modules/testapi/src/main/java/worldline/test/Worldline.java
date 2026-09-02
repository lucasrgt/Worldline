package worldline.test;

import java.util.ArrayDeque;
import java.util.Deque;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.test.SemanticSelector.Kind;

/** Static Vitest-style collection DSL for Java 8 specs. */
public final class Worldline {
    private static final ThreadLocal<Collector> ACTIVE = new ThreadLocal<>();

    private Worldline() {}

    static void begin(String spec) {
        if (ACTIVE.get() != null) throw new IllegalStateException("nested spec collection");
        ACTIVE.set(new Collector(spec));
    }

    static TestPlan end() {
        Collector value = active(); ACTIVE.remove(); return new TestPlan(value.spec, value.root);
    }

    static void abort() { ACTIVE.remove(); }

    public static TestDefinition test(String name, TestBody body) {
        if (body == null) throw new NullPointerException("body");
        TestDefinition test = new TestDefinition(name(name, "test"), body, location());
        active().current().add(test); return test;
    }

    public static TestDefinition it(String name, TestBody body) { return test(name, body); }

    public static SuiteDefinition describe(String name, SuiteBody body) {
        if (body == null) throw new NullPointerException("body");
        Collector collector = active(); SuiteDefinition suite = new SuiteDefinition(name(name, "suite"));
        collector.current().add(suite); collector.stack.push(suite);
        try { body.define(); } finally { collector.stack.pop(); }
        return suite;
    }

    public static SuiteDefinition suite(String name, SuiteBody body) { return describe(name, body); }
    public static void beforeAll(Runnable hook) { require(hook, "beforeAll"); active().current().beforeAll(hook); }
    public static void afterAll(Runnable hook) { require(hook, "afterAll"); active().current().afterAll(hook); }
    public static void beforeEach(TestHook hook) { require(hook, "beforeEach"); active().current().beforeEach(hook); }
    public static void afterEach(TestHook hook) { require(hook, "afterEach"); active().current().afterEach(hook); }
    public static void onTestFailed(TestHook hook) { require(hook, "onTestFailed"); active().current().onFailed(hook); }
    public static void onTestFinished(TestHook hook) { require(hook, "onTestFinished"); active().current().onFinished(hook); }
    public static <T> Each<T> each(Iterable<T> values) { return new Each<>(values); }
    public static TestCaseBuilder worldline() { return new TestCaseBuilder(); }
    public static BlockPosition pos(int x, int y, int z) { return new BlockPosition(x, y, z); }
    public static BlockState block(String key) {
        SemanticSelector value = SemanticSelectors.require(key, Kind.BLOCK);
        BlockState state = new BlockState(value.legacyId(), value.metadata());
        TestMappingAccess.record(state, value.key(), value.evidence(), value.writable()); return state;
    }
    public static SemanticSelector item(String key) { return SemanticSelectors.require(key, Kind.ITEM); }
    public static SemanticSelector entity(String key) { return SemanticSelectors.require(key, Kind.ENTITY); }
    public static SemanticSelector packet(String key) { return SemanticSelectors.require(key, Kind.PACKET); }
    public static WorldlineAwait awaitPolls(int maximumPolls) {
        return new WorldlineAwait(maximumPolls);
    }

    static String name(String value, String role) {
        if (value == null || value.trim().isEmpty()) throw new IllegalArgumentException(role + " name is blank");
        String clean = value.trim(); if (clean.length() > 200) throw new IllegalArgumentException(role + " name too long");
        for (int index = 0; index < clean.length(); index++)
            if (Character.isISOControl(clean.charAt(index))) throw new IllegalArgumentException(role + " name has control character");
        return clean;
    }

    private static void require(Object value, String role) { if (value == null) throw new NullPointerException(role); }
    private static Collector active() {
        Collector value = ACTIVE.get();
        if (value == null) throw new IllegalStateException("Worldline DSL used outside WorldlineSpec.define");
        return value;
    }

    private static TestLocation location() {
        for (StackTraceElement frame : new Throwable().getStackTrace())
            if (!frame.getClassName().startsWith("worldline.test."))
                return new TestLocation(frame.getFileName() == null ? frame.getClassName() : frame.getFileName(),
                        Math.max(frame.getLineNumber(), 0));
        return new TestLocation("unknown", 0);
    }

    private static final class Collector {
        final String spec; final SuiteDefinition root = new SuiteDefinition("root");
        final Deque<SuiteDefinition> stack = new ArrayDeque<>();
        Collector(String spec) { this.spec = spec; stack.push(root); }
        SuiteDefinition current() { return stack.peek(); }
    }
}
