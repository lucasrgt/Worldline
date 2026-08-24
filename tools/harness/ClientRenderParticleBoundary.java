import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.Set;

/** Keeps the client-render/particle frontier explicit until an official pilot exists. */
final class ClientRenderParticleBoundary {
    private static final Set<String> KEYS = Set.of("schema", "status", "decision", "scope",
            "evidence.level", "pilot.status", "aero.claims", "reopen.requires", "document");
    private ClientRenderParticleBoundary() { }

    static void validate(Path root) throws Exception {
        Properties values = load(root.resolve("quality/client-render-particle-boundary.properties"));
        require(values.stringPropertyNames().equals(KEYS), "client render/particle keys drifted");
        match(values, "schema", "1");
        match(values, "status", "registered");
        match(values, "decision", "explicit-non-claim");
        match(values, "scope", "vanilla-client-rendering,particles");
        match(values, "evidence.level", "semantic-mappings-only");
        match(values, "pilot.status", "not-qualified");
        match(values, "aero.claims", "block-model-rendering,frame-census");
        match(values, "reopen.requires", "official-client-two-replica-particle-oracle");
        Path document = root.resolve(required(values, "document")).normalize();
        require(document.startsWith(root) && Files.isRegularFile(document),
                "missing client render/particle decision document");
        String text = Files.readString(document, StandardCharsets.UTF_8);
        for (String phrase : Set.of("explicit non-claim", "EffectRenderer.updateEffects",
                "official Beta 1.7.3 client", "reusable TestKit contract"))
            require(text.contains(phrase), "client render/particle decision lacks: " + phrase);
        String tick = Files.readString(root.resolve(
                "modules/semantics/src/main/java/worldline/semantics/TickSemantics.java"));
        String render = Files.readString(root.resolve(
                "modules/semantics/src/main/java/worldline/semantics/RenderSemantics.java"));
        require(tick.contains("EFFECT_TICK") && tick.contains("EffectRenderer")
                        && render.contains("TEXTURE_EFFECT_TICK"),
                "client effect semantic anchors drifted");
        for (SmokeDiscovery.Entry smoke : SmokeDiscovery.discover(root)) {
            String descriptor = Files.readString(root.resolve("smokes").resolve(smoke.id)
                    .resolve("smoke.properties"), StandardCharsets.UTF_8).toLowerCase();
            require(!descriptor.contains("behavior=client-particle-render")
                            && !descriptor.contains("testkit.binding=net.minecraft.src.effectrenderer"),
                    "particle behavior exists while FRONT-10 remains a non-claim: " + smoke.id);
        }
        System.out.println("  client render/particles: explicit non-claim registered");
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
