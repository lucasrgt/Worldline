package worldline.testkit;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import worldline.extension.ExtensionCapabilities;
import worldline.extension.ExtensionContract;
import worldline.extension.ExtensionEvidence;
import worldline.extension.ExtensionMode;
import worldline.extension.ExtensionOracles;
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
        require(result.passed() && result.count(TestStatus.PASSED) == 1,
                "extension TestKit execution failed");
        try (java.util.stream.Stream<Path> paths = Files.walk(artifacts)) {
            long evidence = paths.filter(Files::isRegularFile).filter(path -> path.getFileName()
                    .toString().endsWith(".evidence.properties")).count();
            require(evidence == 1, "extension evidence artifact missing");
        }
        System.out.println("ExtensionTestSpecsTest passed");
    }

    public static final class CounterExtension implements WorldlineExtension {
        private int count;
        @Override public void register(WorldlineExtensionRegistry registry) {
            registry.subject(ExtensionSubject.of("example.counter:counter",
                    ExtensionSubjectKind.SUBSYSTEM, "Counter"));
            registry.fixture("empty", context -> count = 0);
            registry.action("increment", context -> count++);
            registry.observation("count", context -> Integer.toString(count));
            registry.oracle("equatable", ExtensionOracles.equatable());
            Map<String, String> expected = new LinkedHashMap<String, String>(); expected.put("count", "1");
            registry.contract(ExtensionContract.builder("increment", "example.counter:counter")
                    .fixture("empty").action("increment").observation("count").oracle("equatable")
                    .mode(ExtensionMode.CUSTOM_CONTRACT).custom(ExtensionEvidence.signature(expected)).build());
        }
    }

    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}
