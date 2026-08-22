import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Properties;

/** Latest vanilla milestone release gate, extracted from ReleaseCheck's packed file. */
final class ReleaseVanilla {
    public static void main(String[] arguments) {
        if (arguments.length != 0) {
            System.err.println("usage: java tools/harness/ReleaseVanilla.java");
            System.exit(2);
        }
        try {
            Path root = Path.of("").toAbsolutePath().normalize();
            Properties release = load(root, "release/worldline.properties");
            check(root, release);
        } catch (Exception error) {
            System.err.println("release vanilla failed: " + error.getMessage());
            System.exit(1);
        }
    }

    private ReleaseVanilla() {}

    static void check(Path root, Properties release) throws Exception {
        Properties m566 = load(root, "smokes/m566-grass-spread-set/smoke.properties");
        Properties m555 = load(root, "smokes/m555-torch-burnout-set/smoke.properties");
        Properties m567 = load(root, "smokes/m567-bed-spawn-set/smoke.properties");
        Properties m556 = load(root, "smokes/m556-rs-nor-latch-set/smoke.properties");
        Properties m563 = load(root, "smokes/m563-nether-exit-create-set/smoke.properties");
        Properties m569 = load(root, "smokes/m569-spawner-delay-set/smoke.properties");
        match(release, "version", "1.462.0");
        match(release, "milestone", "m569-consolidated-vanilla-qualification");
        match(release, "m549.status", "retracted-hidden-fallback-cause");
        match(release, "m552.status", "retracted-ordinary-adjacent-power-not-qc");
        match(release, "m557.status", "retracted-no-tick-resolved-one-tick-proof");
        match(release, "m564.status", "retracted-noncausal-light-control");
        match(release, "m555.status", "qualified-corrected-scope");
        same(release, "m555.signature", m555, "expected.signature");
        same(release, "m566.signature", m566, "expected.signature");
        same(release, "server.sha256", m566, "server.jar.sha256");
        same(release, "server.sha256", m555, "server.jar.sha256");
        same(release, "m567.signature", m567, "expected.signature");
        same(release, "server.sha256", m567, "server.jar.sha256");
        same(release, "m556.signature", m556, "expected.signature");
        same(release, "server.sha256", m556, "server.jar.sha256");
        same(release, "m563.signature", m563, "expected.signature");
        same(release, "server.sha256", m563, "server.jar.sha256");
        same(release, "m569.signature", m569, "expected.signature");
        same(release, "server.sha256", m569, "server.jar.sha256");
        match(release, "m562.status", "rejected-central-frame-construction");
        match(release, "m568.status", "rejected-central-reload-packet21-absent");
        String[][] signatures = {
            {"m500.signature", "m500-sw-rain-transition"}, {"m501.signature", "m501-sw-entity-item-grounding"},
            {"m502.signature", "m502-sw-entity-collision-resolution"}, {"m503.signature", "m503-sw-pig-wander"},
            {"m504-m508.signature", "m504-m508-sw-entity-dynamics"}, {"m506.signature", "m506-sw-sheep-sheared-persistence"},
            {"m509.signature", "m509-sw-redstone-wire-fanout"}, {"m510.signature", "m510-sw-redstone-wire-loop-recovery"},
            {"m511.signature", "m511-sw-redstone-ore-trigger"}, {"m512-m516.signature", "m512-m516-sw-random-blocks"},
            {"m513.signature", "m513-sw-water-downward-flow"}, {"m514.signature", "m514-sw-lava-downward-flow"},
            {"m515.signature", "m515-sw-fire-support-extinguish"}, {"m517.signature", "m517-sw-item-despawn-age"},
            {"m518.signature", "m518-sw-tnt-fuse-lifecycle"}, {"m519.signature", "m519-sw-dispenser-rng-membership"},
            {"m521.signature", "m521-sw-hotbar-empty-selection"}, {"m522.signature", "m522-sw-personal-slot-swap"},
            {"m523.signature", "m523-sw-world-time-advance"}, {"m524.signature", "m524-sw-dig-status-boundaries"},
            {"m525.signature", "m525-sw-wire-crossing-isolation"}
        };
        for (String[] entry : signatures) {
            Properties smoke = load(root, "smokes/" + entry[1] + "/smoke.properties");
            same(release, entry[0], smoke, "expected.signature");
            same(release, "server.sha256", smoke, "server.jar.sha256");
        }
        match(release, "m520.status", "rejected-later-version-behavior");
        for (String file : Arrays.asList("docs/M566_GRASS_SPREAD_SET.md", "docs/M566_CYCLE.md",
                "smokes/m566-grass-spread-set/MAP.md", "docs/M555_TORCH_BURNOUT_SET.md", "docs/M555_CYCLE.md",
                "smokes/m555-torch-burnout-set/MAP.md", "docs/M567_BED_SPAWN_SET.md", "docs/M567_CYCLE.md",
                "smokes/m567-bed-spawn-set/MAP.md", "docs/M556_RS_NOR_LATCH_SET.md", "docs/M556_CYCLE.md",
                "smokes/m556-rs-nor-latch-set/MAP.md", "docs/M563_NETHER_EXIT_CREATE_SET.md", "docs/M563_CYCLE.md",
                "smokes/m563-nether-exit-create-set/MAP.md", "docs/M569_SPAWNER_DELAY_SET.md", "docs/M569_CYCLE.md",
                "smokes/m569-spawner-delay-set/MAP.md", "docs/M520_SW_REJECTED.md"))
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        for (String file : Arrays.asList("smokes/m549-sticky-bud-set/smoke.properties",
                "smokes/m552-tnt-qc-set/smoke.properties", "smokes/m557-one-tick-pulse-set/smoke.properties",
                "smokes/m564-spawn-light-set/smoke.properties"))
            if (Files.exists(root.resolve(file))) throw new IllegalStateException("retracted smoke remains: " + file);
        System.out.println("  release: Worldline v1.462.0 consolidated vanilla qualification GO");
    }

    private static Properties load(Path root, String relative) throws IOException {
        Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(root.resolve(relative), StandardCharsets.UTF_8)) {
            properties.load(reader);
        }
        return properties;
    }

    private static void match(Properties properties, String key, String expected) {
        String value = properties.getProperty(key);
        if (value == null || !expected.equals(value.trim()))
            throw new IllegalStateException("expected " + key + "=" + expected + " but was " + value);
    }

    private static void same(Properties left, String leftKey, Properties right, String rightKey) {
        String leftValue = left.getProperty(leftKey), rightValue = right.getProperty(rightKey);
        if (leftValue == null || rightValue == null || !leftValue.trim().equals(rightValue.trim()))
            throw new IllegalStateException(leftKey + " drifted from " + rightKey);
    }
}
