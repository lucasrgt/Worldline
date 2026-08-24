import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/** Validates composite plans and their carried-forward qualification evidence. */
final class CompositeCycleCheck {
    private CompositeCycleCheck() { }

    static int execute(Path root) throws Exception {
        Path lock = root.resolve("smokes/composite-cycle-migration.lock");
        if (!Files.isRegularFile(lock)) return 0;
        Properties migrations = load(lock); require("1".equals(migrations.getProperty("schema")),
                "invalid composite migration schema");
        Properties formatting = FormattingPinCheck.manifest(root);
        Properties train = TrainPinCheck.manifest(root);
        require((digest(root.resolve("tools/smoke/CompositeCycle.java")).equals(
                        migrations.getProperty("runner_sha256"))
                        || FormattingPinCheck.transportsFile(formatting, root,
                        "tools/smoke/CompositeCycle.java", migrations.getProperty("runner_sha256")))
                        && digest(root.resolve("tools/harness/CompositeCyclePlan.java")).equals(
                        migrations.getProperty("plan_source_sha256"))
                        && digest(root.resolve("tools/harness/DataDrivenSupport.java")).equals(
                        migrations.getProperty("support_source_sha256"))
                        && digest(root.resolve("tools/harness/SmokeSupport.java")).equals(
                        migrations.getProperty("runtime_support_source_sha256")),
                "composite shared source attestation drift");
        SmokePins pins = new SmokePins(root); SmokeInputFingerprint fingerprints =
                new SmokeInputFingerprint(root); Properties telemetry = TelemetryPinCheck.manifest(root);
        Properties schemas = SchemaPinCheck.manifest(root);
        Set<String> seen = new HashSet<>(); int generic = 0;
        for (SmokeDiscovery.Entry smoke : SmokeDiscovery.discover(root)) {
            if (!smoke.runner.equals("tools/smoke/CompositeCycle.java")) continue;
            generic++; seen.add(smoke.id); CompositeCyclePlan plan = CompositeCyclePlan.load(root, smoke.id);
            String stem = "cycle." + smoke.id + "."; String source = migrations.getProperty(stem + "source", "");
            require(source.matches("tools/smoke/[A-Za-z0-9]+Cycle\\.java")
                            && !Files.exists(root.resolve(source)),
                    "migrated composite source was restored: " + smoke.id);
            require(hash(migrations, stem + "source_sha256")
                            && hash(migrations, stem + "prior_fingerprint")
                            && hash(migrations, stem + "evidence_sha256")
                            && plan.fingerprint().equals(migrations.getProperty(stem + "plan_sha256")),
                    "composite migration evidence drift: " + smoke.id);
            String current = fingerprints.compute(smoke); SmokePins.Entry pin = pins.match(smoke.id, current);
            require(pin != null && (pin.source().equals("executed")
                            || pin.source().equals("refactor-equivalent")
                            && (pin.evidence().equals(migrations.getProperty(stem + "evidence_sha256"))
                            || TelemetryPinCheck.carries(telemetry, smoke.id, pin, current)
                            || SchemaPinCheck.carries(schemas, smoke.id, pin, current)
                            || FormattingPinCheck.carries(formatting, smoke.id, pin, current)
                            || TrainPinCheck.carriesCurrent(train, smoke.id, pin, current))),
                    "composite refactor pin drift: " + smoke.id);
        }
        require(generic == integer(migrations, "count") && generic == seen.size(),
                "composite migration census drift");
        Properties policy = load(root.resolve("quality/data-driven-cycles.properties"));
        int exceptional;
        try (var paths = Files.list(root.resolve("tools/smoke"))) {
            exceptional = (int) paths.filter(path -> path.getFileName().toString().endsWith("Cycle.java"))
                    .filter(path -> !Set.of("DataDrivenCycle.java", "CompositeCycle.java")
                            .contains(path.getFileName().toString())).count();
        }
        require(exceptional <= integer(policy, "maximum.exceptional.coordinators"),
                "exceptional coordinator ratchet regressed");
        System.out.println("  composite cycles: " + generic + " source-refactor attestations");
        System.out.println("  exceptional coordinators: " + exceptional);
        return generic;
    }

    private static boolean hash(Properties values, String key) {
        return values.getProperty(key, "").matches("[0-9a-f]{64}");
    }
    private static String digest(Path path) throws Exception { return java.util.HexFormat.of().formatHex(
            java.security.MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(path)));
    }
    private static int integer(Properties values, String key) {
        try { return Integer.parseInt(values.getProperty(key, "")); }
        catch (NumberFormatException error) { throw new IllegalStateException("invalid " + key); }
    }
    private static Properties load(Path path) throws Exception {
        Properties values = new Properties();
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) { values.load(reader); }
        return values;
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
