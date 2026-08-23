package worldline.testapi;

import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import worldline.test.TestRuntimeProvider;
import worldline.test.TestRuntimeProviders;
import worldline.test.TestRuntimeRequest;
import worldline.test.TestRuntimeSession;

/** Runtime-provider SPI discovery, compatibility, and ambiguity contract. */
public final class TestRuntimeProvidersTest {
    private TestRuntimeProvidersTest() {}

    public static void main(String[] arguments) throws Exception {
        Path root = Files.createTempDirectory("worldline-provider-spi-");
        Path service = root.resolve("META-INF/services/" + TestRuntimeProvider.class.getName());
        Files.createDirectories(service.getParent());
        Files.writeString(service, First.class.getName() + "\n", StandardCharsets.UTF_8);
        try (URLClassLoader loader = new URLClassLoader(
                new java.net.URL[] {root.toUri().toURL()}, TestRuntimeProvidersTest.class.getClassLoader())) {
            require(TestRuntimeProviders.discover("stationapi-b1.7.3", loader) instanceof First,
                    "runtime id was not discovered through SPI");
            require(TestRuntimeProviders.discover(First.class.getName(), loader) instanceof First,
                    "explicit provider compatibility drifted");
            failure(() -> TestRuntimeProviders.discover("missing-runtime", loader),
                    "missing runtime provider was accepted");
            Files.writeString(service, First.class.getName() + "\n" + Second.class.getName() + "\n",
                    StandardCharsets.UTF_8);
            try (URLClassLoader duplicate = new URLClassLoader(
                    new java.net.URL[] {root.toUri().toURL()}, TestRuntimeProvidersTest.class.getClassLoader())) {
                failure(() -> TestRuntimeProviders.discover("stationapi-b1.7.3", duplicate),
                        "duplicate runtime providers were accepted");
            }
        }
        System.out.println("TestRuntimeProvidersTest passed");
    }

    public static final class First implements TestRuntimeProvider {
        @Override public String runtimeId() { return "stationapi-b1.7.3"; }
        @Override public TestRuntimeSession open(TestRuntimeRequest request) {
            throw new UnsupportedOperationException("discovery fixture");
        }
    }
    public static final class Second implements TestRuntimeProvider {
        @Override public String runtimeId() { return "stationapi-b1.7.3"; }
        @Override public TestRuntimeSession open(TestRuntimeRequest request) {
            throw new UnsupportedOperationException("discovery fixture");
        }
    }
    private static void failure(Action action, String message) {
        try { action.run(); throw new AssertionError(message); }
        catch (IllegalArgumentException | IllegalStateException expected) { /* expected */ }
        catch (Exception error) { throw new AssertionError("unexpected checked failure", error); }
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
    @FunctionalInterface private interface Action { void run() throws Exception; }
}
