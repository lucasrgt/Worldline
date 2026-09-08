import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.Set;

/** Binds the reopened particle frontier to its bounded official native lifecycle proof. */
final class ClientRenderParticleBoundary {
    private static final Set<String> KEYS = Set.of("schema", "status", "decision", "scope",
            "evidence.level", "pilot.status", "aero.claims", "qualified.milestone", "document");
    private ClientRenderParticleBoundary() { }

    static void validate(Path root) throws Exception {
        Properties values = load(root.resolve("quality/client-render-particle-boundary.properties"));
        require(values.stringPropertyNames().equals(KEYS), "client render/particle keys drifted");
        match(values, "schema", "2");
        match(values, "status", "registered");
        match(values, "decision", "bounded-official-claim");
        match(values, "scope", "smoke,flame,red-dust");
        match(values, "evidence.level", "native-four-process-rgba-and-lifecycle");
        match(values, "pilot.status", "qualified");
        match(values, "aero.claims", "block-model-rendering,frame-census");
        match(values, "qualified.milestone", "b173-native-particle-lifecycle-cycle");
        Path document = root.resolve(required(values, "document")).normalize();
        require(document.startsWith(root) && Files.isRegularFile(document),
                "missing client render/particle decision document");
        String text = Files.readString(document, StandardCharsets.UTF_8);
        for (String phrase : Set.of("bounded official claim", "EffectRenderer.updateEffects",
                "official Beta 1.7.3 client", "reusable TestKit contract"))
            require(text.contains(phrase), "client render/particle decision lacks: " + phrase);
        String tick = Files.readString(root.resolve(
                "modules/semantics/src/main/java/worldline/semantics/TickSemantics.java"));
        String render = Files.readString(root.resolve(
                "modules/semantics/src/main/java/worldline/semantics/RenderSemantics.java"));
        require(tick.contains("EFFECT_TICK") && tick.contains("EffectRenderer")
                        && render.contains("TEXTURE_EFFECT_TICK"),
                "client effect semantic anchors drifted");
        String id = required(values, "qualified.milestone");
        Properties descriptor = load(root.resolve("smokes").resolve(id).resolve("smoke.properties"));
        descriptor(descriptor);
        Properties changed = new Properties(); changed.putAll(descriptor);
        changed.setProperty("testkit.binding", "net.minecraft.src.EffectRenderer#updateEffects");
        boolean rejected = false;
        try { descriptor(changed); } catch (IllegalStateException expected) { rejected = true; }
        require(rejected, "native private binding accepted as public particle evidence");
        System.out.println("  client render/particles: bounded official lifecycle and native frames");
    }

    private static void descriptor(Properties values) {
        match(values, "behavior", "client-particle-render");
        match(values, "native.capture.schema", "1");
        match(values, "runner.source", "tools/smoke/NativeInventoryRenderCycle.java");
        match(values, "testkit.binding", "worldline.testkit.ParticleLifecycleFixture#verify");
        match(values, "testkit.evidence", "equatable");
        match(values, "milestone.atoms",
                "native-creation-invariants,manager-aging-and-removal,live-and-terminal-render");
        require(required(values, "expected.signature").matches("[0-9a-f]{64}")
                && !required(values, "expected.signature").equals("0".repeat(64)), "unfrozen particle proof");
    }

    private static Properties load(Path path) throws Exception {
        Properties values = new Properties();
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            values.load(reader);
        }
        return values;
    }

    private static void match(Properties values, String key, String expected) {
        require(expected.equals(required(values, key)), "client render/particle field drift: " + key);
    }

    private static String required(Properties values, String key) {
        String value = values.getProperty(key, "").trim();
        require(!value.isEmpty(), "missing client render/particle field: " + key);
        return value;
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
