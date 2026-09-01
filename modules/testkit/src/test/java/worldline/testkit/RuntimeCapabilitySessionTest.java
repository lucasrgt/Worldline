package worldline.testkit;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;
import worldline.test.TestRuntimeProvider;
import worldline.test.TestRuntimeRequest;
import worldline.test.TestRuntimeSession;
import worldline.test.WorldlineSpec;
import static worldline.test.Worldline.test;

/** Capability-only sessions remain isolated and fail closed for absent surfaces. */
public final class RuntimeCapabilitySessionTest {
    private RuntimeCapabilitySessionTest() {}

    public static void main(String[] arguments) throws Exception {
        Path root = Files.createTempDirectory("worldline-capability-session-");
        CapabilityProvider provider = new CapabilityProvider();
        TestRunResult result = new TestRunner().run(new CapabilitySpec(),
                new RunnerOptions().provider(provider).artifacts(root.resolve("artifacts"))
                        .runtimeLock(root.resolve("runtime.lock")), null);
        require(result.passed(), "capability-only test failed");
        require(provider.opened.get() == 1 && provider.closed.get() == 1,
                "capability-only session was not isolated and closed");
        System.out.println("RuntimeCapabilitySessionTest passed");
    }

    private static final class CapabilitySpec extends WorldlineSpec {
        @Override protected void define() {
            test("capability is available without a generic runtime", context -> {
                require("ready".equals(context.capability(Marker.class).value), "capability mismatch");
                rejected(() -> context.capability(String.class), "unsupported capability was accepted");
                rejected(context::runtime, "missing generic runtime was accepted");
            });
        }
    }

    private static final class CapabilityProvider implements TestRuntimeProvider {
        final AtomicInteger opened = new AtomicInteger(), closed = new AtomicInteger();
        @Override public String runtimeId() { return "capability-only"; }
        @Override public TestRuntimeSession open(TestRuntimeRequest request) {
            opened.incrementAndGet(); Marker marker = new Marker("ready");
            return new TestRuntimeSession() {
                @Override public <T> T capability(Class<T> type) {
                    if (type == Marker.class) return type.cast(marker);
                    return TestRuntimeSession.super.capability(type);
                }
                @Override public void close() { closed.incrementAndGet(); }
            };
        }
    }

    private static final class Marker {
        final String value;
        Marker(String value) { this.value = value; }
    }
    private static void rejected(Action action, String message) throws Exception {
        try { action.run(); throw new AssertionError(message); }
        catch (IllegalStateException expected) { /* fail closed */ }
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
    @FunctionalInterface private interface Action { void run() throws Exception; }
}
