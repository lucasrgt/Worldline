package worldline.extension;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class WorldlineExtensionTest {
    private WorldlineExtensionTest() {}

    public static void main(String[] arguments) throws Exception {
        Path root = Files.createTempDirectory("worldline-extension-test-");
        Path manifest = root.resolve("worldline/extensions/example.probe/manifest.properties");
        Files.createDirectories(manifest.getParent());
        Files.write(manifest, ("schema=worldline.extension.v1\n"
                + "id=example.probe\nversion=1.2.3\nentrypoint=" + ProbeExtension.class.getName()
                + "\nworldline.api=1\nrequires=testkit.v1,atlas.v1\n"
                + "provides=custom-contract.v1\n").getBytes(StandardCharsets.UTF_8));
        ExtensionCapabilities host = ExtensionCapabilities.of(
                ExtensionCapabilities.TESTKIT_V1, ExtensionCapabilities.ATLAS_V1);
        List<WorldlineExtensionPlan> plans = WorldlineExtensionDiscovery.discover(root,
                WorldlineExtensionTest.class.getClassLoader(), host);
        require(plans.size() == 1, "extension discovery count");
        WorldlineExtensionPlan plan = plans.get(0);
        require(plan.subjects().size() == 4 && plan.contracts().size() == 1,
                "extension registry surfaces");
        require(plan.atlasDocument().contains("atlas.api.example.probe.probe-block")
                && plan.atlasDocument().contains("atlas.scenario.example.probe.roundtrip")
                && plan.atlasDocument().contains("version=1.2.3"), "extension Atlas projection");
        Map<String, String> values = new LinkedHashMap<String, String>(); values.put("count", "1");
        ExtensionEvidence evidence = ExtensionEvidence.capture(plan.manifest(), plan.contracts().get(0),
                ExtensionMode.CUSTOM_CONTRACT, values);
        require(evidence.signature().equals(ExtensionEvidence.signature(values))
                && evidence.canonical().contains("extension=example.probe"), "extension evidence");
        boolean rejected = false;
        try { WorldlineExtensionDiscovery.discover(root, WorldlineExtensionTest.class.getClassLoader(),
                ExtensionCapabilities.of(ExtensionCapabilities.TESTKIT_V1)); }
        catch (IllegalArgumentException expected) { rejected = true; }
        require(rejected, "missing capability was accepted");
        System.out.println("WorldlineExtensionTest passed");
    }

    public static final class ProbeExtension implements WorldlineExtension {
        @Override public void register(WorldlineExtensionRegistry registry) {
            registry.subject(ExtensionSubject.of("example.probe:probe-block",
                    ExtensionSubjectKind.BLOCK, "Probe block"));
            registry.subject(ExtensionSubject.of("example.probe:probe-item",
                    ExtensionSubjectKind.ITEM, "Probe item"));
            registry.subject(ExtensionSubject.of("example.probe:probe-entity",
                    ExtensionSubjectKind.ENTITY, "Probe entity"));
            registry.subject(ExtensionSubject.of("example.probe:probe-network",
                    ExtensionSubjectKind.SUBSYSTEM, "Probe network"));
            registry.fixture("empty", context -> { });
            registry.action("insert", context -> { });
            registry.observation("count", context -> "1");
            registry.oracle("equatable", ExtensionOracles.equatable());
            Map<String, String> values = new LinkedHashMap<String, String>(); values.put("count", "1");
            registry.contract(ExtensionContract.builder("roundtrip", "example.probe:probe-block")
                    .fixture("empty").action("insert").observation("count").oracle("equatable")
                    .mode(ExtensionMode.CUSTOM_CONTRACT).custom(ExtensionEvidence.signature(values)).build());
            registry.adapter(ExtensionRuntimeAdapter.of("modloader", "modloader-b1.7.3",
                    "example.probe.ProbeRuntimeProvider"));
        }
    }

    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}
