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
        Properties formatting = FormattingPinCheck.manifest(root);
        Properties train = TrainPinCheck.manifest(root);
        require((digest(root.resolve("tools/smoke/DataDrivenCycle.java")).equals(
                        migrations.getProperty("runner_sha256"))
                        || FormattingPinCheck.transportsFile(formatting, root,
                        "tools/smoke/DataDrivenCycle.java", migrations.getProperty("runner_sha256")))
                        && digest(root.resolve("tools/harness/DataDrivenCyclePlan.java")).equals(
                        migrations.getProperty("plan_source_sha256"))
                        && digest(root.resolve("tools/harness/DataDrivenSupport.java")).equals(
                        migrations.getProperty("support_source_sha256"))
                        && digest(root.resolve("tools/harness/SmokeSupport.java")).equals(
                        migrations.getProperty("runtime_support_source_sha256")),
                "data-driven shared source attestation drift");
        SmokePins pins = new SmokePins(root); SmokeInputFingerprint fingerprints =
                new SmokeInputFingerprint(root); Properties telemetry = TelemetryPinCheck.manifest(root);
        Properties schemas = SchemaPinCheck.manifest(root);
        int fixtureRefactors = integer(migrations, "refresh.fixture.count");
        require(fixtureRefactors >= 1 && fixtureRefactors <= 16,
                "generic fixture-refactor census drift");
        for (int index = 1; index <= fixtureRefactors; index++) {
            String stem = "refresh.fixture." + index + ".";
            String id = migrations.getProperty(stem + "id", "");
            String relative = migrations.getProperty(stem + "path", "");
            require(relative.startsWith("smokes/" + id + "/") && relative.endsWith(".java")
                            && hash(migrations, stem + "prior_sha256")
                            && digest(root.resolve(relative)).equals(
                                    migrations.getProperty(stem + "current_sha256")),
                    "generic fixture-refactor attestation drift");
        }
        int formattingRefactors = Integer.parseInt(
                migrations.getProperty("refresh.formatting.count", "0"));
        require(formattingRefactors <= 16, "generic formatting-refactor census drift");
        for (int index = 1; index <= formattingRefactors; index++) {
            String stem = "refresh.formatting." + index + ".";
            String id = migrations.getProperty(stem + "id", "");
            String relative = migrations.getProperty(stem + "path", "");
            require(relative.startsWith("smokes/" + id + "/") && relative.endsWith(".java")
                            && hash(migrations, stem + "prior_sha256")
                            && digest(root.resolve(relative)).equals(
                                    migrations.getProperty(stem + "current_sha256")),
                    "generic formatting-refactor attestation drift");
        }
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
                            && (plan.fingerprint().equals(migrations.getProperty(stem + "plan_sha256"))
                            || LifecycleClaimTestKitPinCheck.transportsPlan(root, smoke.id,
                                    migrations.getProperty(stem + "plan_sha256"),
                                    plan.fingerprint())),
                    "data-driven migration evidence drift: " + smoke.id);
            String current = fingerprints.compute(smoke); SmokePins.Entry pin = pins.match(smoke.id, current);
            require(pin != null && (pin.source().equals("executed")
                            || pin.source().equals("refactor-equivalent")
                            && (pin.evidence().equals(migrations.getProperty(stem + "evidence_sha256"))
                            || TelemetryPinCheck.carries(telemetry, smoke.id, pin, current)
                            || SchemaPinCheck.carries(schemas, smoke.id, pin, current)
                            || FormattingPinCheck.carries(formatting, smoke.id, pin, current)
                            || TrainPinCheck.carriesCurrent(train, smoke.id, pin, current)
                            || LifecycleClaimTestKitPinCheck.carries(
                                    root, smoke.id, pin, current))),
                    "data-driven refactor pin drift: " + smoke.id);
        }
        int expected = integer(migrations, "count");
        require(migrated == expected && migrated == seen.size(), "migration census drift");
        require(generic >= integer(policy, "minimum.generic.milestones"),
                "data-driven milestone ratchet regressed");
        System.out.println("  data-driven cycles: " + generic + " generic, " + migrated
                + " source-refactor attestations");
    }

    static boolean carriesPlan(Path root, String id, SmokePins.Entry pin) {
        try {
            if (pin == null || !pin.source().equals("refactor-equivalent")) return false;
            Properties migrations = load(root.resolve("smokes/data-driven-migration.lock"));
            Properties descriptor = load(root.resolve("smokes").resolve(id)
                    .resolve("smoke.properties"));
            String stem = "cycle." + id + ".";
            return descriptor.getProperty("runner.source", "")
                            .equals("tools/smoke/DataDrivenCycle.java")
                    && hash(migrations, stem + "evidence_sha256")
                    && pin.evidence().equals(migrations.getProperty(stem + "evidence_sha256"))
                    && DataDrivenCyclePlan.load(root, id).fingerprint()
                            .equals(migrations.getProperty(stem + "plan_sha256"));
        } catch (Exception error) { return false; }
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
