package worldline.testkit;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import worldline.extension.ExtensionCapabilities;
import worldline.extension.ExtensionContracts;
import worldline.extension.ExtensionMode;
import worldline.extension.ExtensionSubject;
import worldline.extension.ExtensionSubjectKind;
import worldline.extension.WorldlineExtension;
import worldline.extension.WorldlineExtensionDiscovery;
import worldline.extension.WorldlineExtensionPlan;
import worldline.extension.WorldlineExtensionRegistry;
import worldline.test.WorldlineSpec;

public final class ExtensionTestSpecsTest {
    private ExtensionTestSpecsTest() {}

    public static void main(String[] arguments) throws Exception {
        Path root = Files.createTempDirectory("worldline-extension-spec-");
        Path manifest = root.resolve("worldline/extensions/example.counter/manifest.properties");
        Files.createDirectories(manifest.getParent());
        Files.write(manifest, ("schema=worldline.extension.v1\nid=example.counter\nversion=1.0.0\n"
                + "entrypoint=" + CounterExtension.class.getName()
                + "\nworldline.api=1\nrequires=testkit.v1\nprovides=custom-contract.v1\n")
                .getBytes(StandardCharsets.UTF_8));
        WorldlineExtensionPlan plan = WorldlineExtensionDiscovery.discover(root,
                ExtensionTestSpecsTest.class.getClassLoader(),
                ExtensionCapabilities.of(ExtensionCapabilities.TESTKIT_V1)).get(0);
        List<WorldlineSpec> specs = ExtensionTestSpecs.create(plan, ExtensionMode.CUSTOM_CONTRACT);
        Path artifacts = root.resolve("results");
        TestRunResult result = new TestRunner().run(specs, new RunnerOptions().artifacts(artifacts),
                new TestReporter() {});
        require(result.passed() && result.count(TestStatus.PASSED) == 2,
                "extension TestKit execution failed");
        try (java.util.stream.Stream<Path> paths = Files.walk(artifacts)) {
            long evidence = paths.filter(Files::isRegularFile).filter(path -> path.getFileName()
                    .toString().endsWith(".evidence.properties")).count();
            require(evidence == 2, "extension evidence artifact missing");
        }
        System.out.println("ExtensionTestSpecsTest passed");
    }

    public static final class CounterExtension implements WorldlineExtension {
        private int count;
        @Override public void register(WorldlineExtensionRegistry registry) {
            registry.fixture("empty", context -> count = 0);
            CounterContracts.register(registry, this);
            MeterContracts.register(registry, this);
        }
    }

    private static final class CounterContracts {
        static void register(WorldlineExtensionRegistry registry, CounterExtension state) {
            registry.subject(ExtensionSubject.of("example.counter:counter",
                    ExtensionSubjectKind.BLOCK, "Counter block"));
            ExtensionContracts.custom("increment", "example.counter:counter")
                    .fixture("empty")
                    .action("increment", context -> state.count++)
                    .expect("count", context -> Integer.toString(state.count), "1")
                    .registerInto(registry);
        }
    }

    private static final class MeterContracts {
        static void register(WorldlineExtensionRegistry registry, CounterExtension state) {
            registry.subject(ExtensionSubject.of("example.counter:meter",
                    ExtensionSubjectKind.BLOCK, "Meter block"));
            ExtensionContracts.custom("meter-read", "example.counter:meter")
                    .fixture("empty").action("increment")
                    .expect("count", "1")
                    .expect("meter-ready", context -> Boolean.toString(state.count > 0), "true")
                    .registerInto(registry);
        }
    }

    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}
