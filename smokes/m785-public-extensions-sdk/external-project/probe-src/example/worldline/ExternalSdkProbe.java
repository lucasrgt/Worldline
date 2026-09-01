package example.worldline;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import worldline.atlas.AtlasQuery;
import worldline.atlas.AtlasStore;
import worldline.extension.ExtensionCapabilities;
import worldline.extension.ExtensionMode;
import worldline.extension.WorldlineExtensionDiscovery;
import worldline.extension.WorldlineExtensionPlan;
import worldline.test.WorldlineSpec;
import worldline.testkit.ExtensionTestSpecs;
import worldline.testkit.RunnerOptions;
import worldline.testkit.TestReporter;
import worldline.testkit.TestRunResult;
import worldline.testkit.TestRunner;

/** External end-to-end proof that imports only published SDK and TestKit packages. */
public final class ExternalSdkProbe {
    private ExternalSdkProbe() {}

    public static void main(String[] arguments) throws Exception {
        if (arguments.length != 2) throw new IllegalArgumentException("project and artifact roots required");
        Path project = Paths.get(arguments[0]), artifacts = Paths.get(arguments[1]);
        ExtensionCapabilities host = ExtensionCapabilities.of(ExtensionCapabilities.TESTKIT_V1,
                ExtensionCapabilities.ATLAS_V1, ExtensionCapabilities.CUSTOM_CONTRACT_V1);
        List<WorldlineExtensionPlan> plans = WorldlineExtensionDiscovery.discover(project, host);
        require(plans.size() == 1, "extension discovery");
        WorldlineExtensionPlan plan = plans.get(0);
        require(plan.subjects().size() == 4 && plan.contracts().size() == 3,
                "extension registration census");
        int tests = run(plan, ExtensionMode.CONFORMANCE, artifacts)
                + run(plan, ExtensionMode.DIFFERENTIAL, artifacts)
                + run(plan, ExtensionMode.CUSTOM_CONTRACT, artifacts);
        require(tests == 5, "extension mode test census");
        require(plan.atlasDocument().contains("pages=8\n")
                && plan.atlasDocument().contains("provenance=extension:example.sdk-fixture@1.0.0"),
                "extension Atlas contribution");
        AtlasStore atlas = AtlasStore.standard(Paths.get("."), project);
        require(atlas.search("extension:example.sdk-fixture@1.0.0").size() == 8,
                "canonical Atlas extension census");
        require(atlas.get("atlas.api.example.sdk-fixture.fixture-block").control()
                .contains("extension=example.sdk-fixture;version=1.0.0"),
                "canonical Atlas provenance");
        require(atlas.get("atlas.scenario.example.sdk-fixture.block-placement").refs()
                .contains("atlas.api.example.sdk-fixture.fixture-block"),
                "canonical Atlas relation");
        require(AtlasQuery.tags(atlas).contains("tag=extension\trecords=8"),
                "canonical Atlas tags");
        System.out.println("WORLDLINE_EXTENSION_SDK=PASS");
        System.out.println("extensions=1,subjects=4,contracts=3,modes=conformance+differential+custom-contract,"
                + "tests=5,atlas-pages=8,imports=public-only");
    }

    private static int run(WorldlineExtensionPlan plan, ExtensionMode mode, Path artifacts) {
        List<WorldlineSpec> specs = ExtensionTestSpecs.create(plan, mode);
        TestRunResult result = new TestRunner().run(specs,
                new RunnerOptions().artifacts(artifacts.resolve(mode.token())), new TestReporter() {});
        require(result.passed(), "extension mode failed: " + mode.token());
        return result.tests().size();
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
