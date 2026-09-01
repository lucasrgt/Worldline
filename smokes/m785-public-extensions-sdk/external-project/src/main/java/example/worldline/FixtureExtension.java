package example.worldline;

import java.util.LinkedHashMap;
import java.util.Map;
import worldline.extension.ExtensionContract;
import worldline.extension.ExtensionEvidence;
import worldline.extension.ExtensionMode;
import worldline.extension.ExtensionOracles;
import worldline.extension.ExtensionRuntimeAdapter;
import worldline.extension.ExtensionSubject;
import worldline.extension.ExtensionSubjectKind;
import worldline.extension.WorldlineExtension;
import worldline.extension.WorldlineExtensionRegistry;

/** External extension compiled only against the published Java 8 authoring surface. */
public final class FixtureExtension implements WorldlineExtension {
    private final ExternalModState state = new ExternalModState();

    @Override public void register(WorldlineExtensionRegistry registry) {
        subject(registry, "fixture-block", ExtensionSubjectKind.BLOCK, "Fixture block");
        subject(registry, "fixture-item", ExtensionSubjectKind.ITEM, "Fixture item");
        subject(registry, "fixture-entity", ExtensionSubjectKind.ENTITY, "Fixture entity");
        subject(registry, "fixture-network", ExtensionSubjectKind.SUBSYSTEM, "Fixture network");
        registry.fixture("empty", context -> state.reset());
        registry.action("place-block", context -> state.placeBlock());
        registry.action("register-item", context -> state.registerItem());
        registry.action("spawn-entity", context -> state.spawnEntity());
        registry.observation("block-state", context -> state.blockState());
        registry.observation("item-count", context -> state.itemCount());
        registry.observation("entity-count", context -> state.entityCount());
        registry.oracle("equatable", ExtensionOracles.equatable());
        registry.contract(blockContract());
        registry.contract(itemContract());
        registry.contract(entityContract());
        registry.adapter(ExtensionRuntimeAdapter.of("modloader", "modloader-b1.7.3",
                "worldline.modloader.testkit.LegacyTestRuntimeProvider"));
    }

    private static ExtensionContract blockContract() {
        Map<String, String> expected = value("block-state", "placed");
        String signature = ExtensionEvidence.signature(expected);
        return ExtensionContract.builder("block-placement", id("fixture-block")).fixture("empty")
                .action("place-block").observation("block-state").oracle("equatable")
                .mode(ExtensionMode.CONFORMANCE).mode(ExtensionMode.DIFFERENTIAL)
                .mode(ExtensionMode.CUSTOM_CONTRACT)
                .vanilla("block-placement-persistence", signature).custom(signature).build();
    }

    private static ExtensionContract itemContract() {
        return ExtensionContract.builder("item-registration", id("fixture-item")).fixture("empty")
                .action("register-item").observation("item-count").oracle("equatable")
                .mode(ExtensionMode.CUSTOM_CONTRACT)
                .custom(ExtensionEvidence.signature(value("item-count", "1"))).build();
    }

    private static ExtensionContract entityContract() {
        return ExtensionContract.builder("entity-spawn", id("fixture-entity")).fixture("empty")
                .action("spawn-entity").observation("entity-count").oracle("equatable")
                .mode(ExtensionMode.CUSTOM_CONTRACT)
                .custom(ExtensionEvidence.signature(value("entity-count", "1"))).build();
    }

    private static void subject(WorldlineExtensionRegistry registry, String token,
            ExtensionSubjectKind kind, String name) {
        registry.subject(ExtensionSubject.of(id(token), kind, name));
    }
    private static String id(String token) { return "example.sdk-fixture:" + token; }
    private static Map<String, String> value(String key, String value) {
        Map<String, String> values = new LinkedHashMap<String, String>();
        values.put(key, value);
        return values;
    }
}
