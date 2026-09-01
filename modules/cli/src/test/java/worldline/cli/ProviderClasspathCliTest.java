package worldline.cli;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import worldline.test.TestRuntimeProvider;
import worldline.test.TestRuntimeRequest;
import worldline.test.TestRuntimeSession;
import worldline.test.WorldlineSpec;
import static worldline.test.Worldline.test;

/** Providers registered only on --classpath are visible to the CLI test run. */
public final class ProviderClasspathCliTest {
    private ProviderClasspathCliTest() {}

    public static void main(String[] arguments) throws Exception {
        Path service = Files.createTempFile("worldline-provider-extension-", ".jar");
        Path artifacts = Files.createTempDirectory("worldline-provider-artifacts-");
        writeServiceJar(service);
        ByteArrayOutputStream output = new ByteArrayOutputStream(), error = new ByteArrayOutputStream();
        String classes = System.getProperty("worldline.test.classes", ".worldline/build/test-classes");
        int status = WorldlineCli.run(new String[] {"test", "run", classes,
                CapabilitySpec.class.getName(), "--classpath=" + service,
                "--provider=classpath-capability", "--reporter=agent", "--artifacts=" + artifacts},
                new PrintStream(output), new PrintStream(error));
        require(status == 0 && output.toString("UTF-8").contains("WORLDLINE_TEST=PASS"),
                "classpath provider test failed: " + error.toString("UTF-8"));
        require(ClasspathProvider.opened.get() == 1 && ClasspathProvider.closed.get() == 1,
                "classpath provider session lifecycle mismatch");
        System.out.println("ProviderClasspathCliTest passed");
    }

    public static final class CapabilitySpec extends WorldlineSpec {
        @Override protected void define() {
            test("uses extension capability", context -> require(
                    "classpath-ready".equals(context.capability(Marker.class).value),
                    "extension capability mismatch"));
        }
    }
    public static final class ClasspathProvider implements TestRuntimeProvider {
        static final AtomicInteger opened = new AtomicInteger(), closed = new AtomicInteger();
        @Override public String runtimeId() { return "classpath-capability"; }
        @Override public TestRuntimeSession open(TestRuntimeRequest request) {
            opened.incrementAndGet(); Marker marker = new Marker("classpath-ready");
            return new TestRuntimeSession() {
                @Override public <T> T capability(Class<T> type) {
                    if (type == Marker.class) return type.cast(marker);
                    return TestRuntimeSession.super.capability(type);
                }
                @Override public void close() { closed.incrementAndGet(); }
            };
        }
    }
    public static final class Marker {
        final String value;
        Marker(String value) { this.value = value; }
    }

    private static void writeServiceJar(Path path) throws Exception {
        try (JarOutputStream output = new JarOutputStream(Files.newOutputStream(path))) {
            output.putNextEntry(new JarEntry("META-INF/services/" + TestRuntimeProvider.class.getName()));
            output.write((ClasspathProvider.class.getName() + "\n").getBytes(StandardCharsets.US_ASCII));
            output.closeEntry();
        }
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
