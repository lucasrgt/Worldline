package worldline.atlas;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import worldline.extension.ExtensionAtlasPage;
import worldline.extension.ExtensionCapabilities;
import worldline.extension.ExtensionContract;
import worldline.extension.ExtensionEvidence;
import worldline.extension.ExtensionMode;
import worldline.extension.ExtensionOracles;
import worldline.extension.ExtensionRuntimeAdapter;
import worldline.extension.ExtensionSubject;
import worldline.extension.ExtensionSubjectKind;
import worldline.extension.WorldlineExtension;
import worldline.extension.WorldlineExtensionRegistry;

public final class AtlasExtensionImportTest {
    private AtlasExtensionImportTest() { }

    public static void main(String[] arguments) throws Exception {
        Path root = Files.createTempDirectory("worldline-atlas-extension-");
        try {
            Path owner = root.resolve("worldline/extensions/sample.mod");
            Files.createDirectories(owner);
            Files.write(owner.resolve("manifest.properties"), ("schema=worldline.extension.v1\n"
                    + "id=sample.mod\nversion=1.2.3\n"
                    + "entrypoint=worldline.atlas.AtlasExtensionImportTest$FixtureExtension\n"
                    + "worldline.api=1\nrequires=testkit.v1,atlas.v1\n"
                    + "provides=custom-contract.v1\n").getBytes(StandardCharsets.UTF_8));
            ExtensionCapabilities host = ExtensionCapabilities.of(
                    ExtensionCapabilities.TESTKIT_V1, ExtensionCapabilities.ATLAS_V1,
                    ExtensionCapabilities.CUSTOM_CONTRACT_V1);
            AtlasStore first = AtlasStore.standard(Paths.get("."), root,
                    AtlasExtensionImportTest.class.getClassLoader(), host);
            AtlasStore second = AtlasStore.standard(Paths.get("."), root,
                    AtlasExtensionImportTest.class.getClassLoader(), host);
            require(first.sha256().equals(second.sha256()), "extension Atlas is not deterministic");
            require(first.search("extension:sample.mod@1.2.3").size() == 4,
                    "extension record census");
            AtlasRecord subject = first.get("atlas.api.sample.mod.fixture-block");
            require(subject.refs().contains("atlas.subsystem.mod-ecosystem")
                    && subject.control().contains("extension=sample.mod;version=1.2.3"),
                    "extension subject provenance");
            require(first.get("atlas.scenario.sample.mod.block-contract").refs()
                    .contains(subject.id()), "extension contract relation");
            require(first.get("atlas.loader.sample.mod.modloader").evidence()
                    .contains("requires:testkit.v1"), "extension capabilities");
            require(AtlasQuery.tags(first).contains("tag=extension\trecords=4")
                    && AtlasQuery.tags(first).contains("tag=extension-tag-block\trecords=1"),
                    "extension tags");
            System.out.println("AtlasExtensionImportTest passed");
        } finally {
            try (java.util.stream.Stream<Path> files = Files.walk(root)) {
                files.sorted(Comparator.reverseOrder()).forEach(path -> {
                    try { Files.deleteIfExists(path); }
                    catch (java.io.IOException error) { throw new IllegalStateException(error); }
                });
            }
        }
    }

    public static final class FixtureExtension implements WorldlineExtension {
        @Override public void register(WorldlineExtensionRegistry registry) {
            String subject = "sample.mod:fixture-block";
            registry.subject(ExtensionSubject.of(subject, ExtensionSubjectKind.BLOCK,
                    "Fixture block"));
            registry.fixture("empty", context -> { });
            registry.action("place", context -> { });
            registry.observation("state", context -> "placed");
            registry.oracle("equatable", ExtensionOracles.equatable());
            Map<String, String> expected = Collections.singletonMap("state", "placed");
            registry.contract(ExtensionContract.builder("block-contract", subject)
                    .fixture("empty").action("place").observation("state").oracle("equatable")
                    .mode(ExtensionMode.CUSTOM_CONTRACT)
                    .custom(ExtensionEvidence.signature(expected)).build());
            registry.adapter(ExtensionRuntimeAdapter.of("modloader", "modloader-b1.7.3",
                    "sample.mod.RuntimeProvider"));
            registry.atlas(ExtensionAtlasPage.builder(
                    "atlas.ecosystem-claim.sample.mod.compatibility", "Fixture compatibility")
                    .tag("extension").tag("compatibility")
                    .relation("atlas.api.sample.mod.fixture-block")
                    .provenance("extension:sample.mod@1.2.3").build());
        }
    }

    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}
