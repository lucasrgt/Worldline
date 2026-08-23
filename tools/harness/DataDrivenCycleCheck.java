import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/** Validates declarative cycle plans and reviewed source-refactor attestations. */
final class DataDrivenCycleCheck {
    private DataDrivenCycleCheck() { }

    static void execute(Path root) throws Exception {
        Properties policy = load(root.resolve("quality/data-driven-cycles.properties"));
        Properties migrations = load(root.resolve("smokes/data-driven-migration.lock"));
        require("1".equals(migrations.getProperty("schema")), "invalid data-driven migration schema");
        require(digest(root.resolve("tools/smoke/DataDrivenCycle.java")).equals(
                        migrations.getProperty("runner_sha256"))
                        && digest(root.resolve("tools/harness/DataDrivenCyclePlan.java")).equals(
                        migrations.getProperty("plan_source_sha256"))
                        && digest(root.resolve("tools/harness/DataDrivenSupport.java")).equals(
                        migrations.getProperty("support_source_sha256")),
                "data-driven shared source attestation drift");
        SmokePins pins = new SmokePins(root); SmokeInputFingerprint fingerprints =
                new SmokeInputFingerprint(root); Properties telemetry = TelemetryPinCheck.manifest(root);
        int generic = 0, migrated = 0;
        Set<String> seen = new HashSet<>();
        for (SmokeDiscovery.Entry smoke : SmokeDiscovery.discover(root)) {
            if (!smoke.runner.equals("tools/smoke/DataDrivenCycle.java")) continue;
            generic++; DataDrivenCyclePlan plan = DataDrivenCyclePlan.load(root, smoke.id);
            Properties descriptor = load(root.resolve("smokes").resolve(smoke.id)
                    .resolve("smoke.properties"));
            if (!"legacy-v1".equals(descriptor.getProperty("cycle.migration"))) continue;
            migrated++; seen.add(smoke.id); String stem = "cycle." + smoke.id + ".";
            require(migrations.getProperty(stem + "source", "").matches("tools/smoke/[A-Za-z0-9]+Cycle\\.java")
                            && !Files.exists(root.resolve(migrations.getProperty(stem + "source"))),
                    "migrated source was restored: " + smoke.id);
            require(hash(migrations, stem + "source_sha256")
                            && hash(migrations, stem + "prior_fingerprint")
                            && hash(migrations, stem + "evidence_sha256")
                            && plan.fingerprint().equals(migrations.getProperty(stem + "plan_sha256")),
                    "data-driven migration evidence drift: " + smoke.id);
            String current = fingerprints.compute(smoke); SmokePins.Entry pin = pins.match(smoke.id, current);
            require(pin != null && (pin.source().equals("executed")
                            || pin.source().equals("refactor-equivalent")
                            && (pin.evidence().equals(migrations.getProperty(stem + "evidence_sha256"))
                            || TelemetryPinCheck.carries(telemetry, smoke.id, pin, current))),
                    "data-driven refactor pin drift: " + smoke.id);
        }
        int expected = integer(migrations, "count");
        require(migrated == expected && migrated == seen.size(), "migration census drift");
        require(generic >= integer(policy, "minimum.generic.milestones"),
                "data-driven milestone ratchet regressed");
        System.out.println("  data-driven cycles: " + generic + " generic, " + migrated
                + " source-refactor attestations");
    }

    private static boolean hash(Properties values, String key) {
        return values.getProperty(key, "").matches("[0-9a-f]{64}");
    }
    private static String digest(Path path) throws Exception {
        return java.util.HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256")
                .digest(Files.readAllBytes(path)));
    }
    private static int integer(Properties values, String key) {
        try { return Integer.parseInt(values.getProperty(key, "")); }
        catch (NumberFormatException error) { throw new IllegalStateException("invalid " + key); }
    }
    private static Properties load(Path path) throws Exception {
        Properties values = new Properties();
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            values.load(reader);
        }
        return values;
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
