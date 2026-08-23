import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/** Finalizes reviewed fixed-wait classifications and their dependent attestations. */
final class FixedWaitMigration {
    private final Path root = Path.of("").toAbsolutePath().normalize();

    public static void main(String[] arguments) {
        try {
            require(arguments.length == 1 && (arguments[0].equals("--finalize")
                    || arguments[0].equals("--refresh-dependencies")),
                    "usage: FixedWaitMigration [--finalize|--refresh-dependencies]");
            FixedWaitMigration migration = new FixedWaitMigration();
            if (arguments[0].equals("--finalize")) migration.finalizeClassifications();
            else migration.refreshDependencies();
        } catch (Exception error) {
            System.err.println("fixed-wait migration failed: " + error.getMessage()); System.exit(1);
        }
    }

    private void finalizeClassifications() throws Exception {
        Properties fixed = load(root.resolve("smokes/fixed-wait-migration.lock"));
        fixed.setProperty("support.sha256", digest(root.resolve(
                "modules/smoketest/src/main/java/worldline/test/WorldlineSmokeAwait.java")));
        for (int index = 0; index < integer(fixed, "source.count"); index++) {
            String stem = "source." + index + "."; Path source = root.resolve(
                    fixed.getProperty(stem + "path")); String text = Files.readString(source,
                            StandardCharsets.UTF_8);
            require(!text.contains("\nimport worldline.test.WorldlineSmokeAwait;")
                    && !text.contains("worldline.test.worldline.test.WorldlineSmokeAwait"),
                    "unfinalized fixed-wait source: " + source);
            String current = digest(source); fixed.setProperty(stem + "current_sha256", current);
            fixed.setProperty(stem + "transition_sha256", transition(
                    fixed.getProperty(stem + "path"), fixed.getProperty(stem + "prior_sha256"), current));
        }
        SmokePins existing = new SmokePins(root); SmokeInputFingerprint fingerprints =
                new SmokeInputFingerprint(root); List<SmokePins.Entry> pins = new ArrayList<>(); int count = 0;
        for (SmokeDiscovery.Entry smoke : SmokeDiscovery.discover(root)) {
            String stem = "milestone." + smoke.id + ".";
            if (fixed.getProperty(stem + "current_fingerprint") == null) {
                pins.add(requiredEntry(existing, smoke.id)); continue;
            }
            count++; String fingerprint = fingerprints.compute(smoke);
            fixed.setProperty(stem + "current_fingerprint", fingerprint);
            fixed.setProperty(stem + "runner_sha256", digest(root.resolve(smoke.runner)));
            Path descriptor = root.resolve("smokes").resolve(smoke.id).resolve("smoke.properties");
            boolean dataDriven = smoke.runner.equals("tools/smoke/DataDrivenCycle.java");
            if (fixed.getProperty(stem + "prior_descriptor_sha256") == null
                    || dataDriven && fixed.getProperty(stem + "prior_descriptor_inputs") == null) {
                byte[] prior = priorDescriptor(smoke.id);
                fixed.putIfAbsent(stem + "prior_descriptor_sha256", digest(prior));
                if (dataDriven) fixed.putIfAbsent(stem + "prior_descriptor_inputs",
                        descriptorInputs(prior));
            }
            fixed.setProperty(stem + "descriptor_sha256", digest(descriptor));
            if (dataDriven) fixed.setProperty(stem + "descriptor_inputs",
                    descriptorInputs(smoke.id));
            pins.add(new SmokePins.Entry(smoke.id, fingerprint,
                    fixed.getProperty(stem + "evidence_sha256"), "refactor-equivalent"));
        }
        require(count == 216, "fixed-wait finalize census drift: " + count);
        existing.write(pins); annotatePins(fixed);
        store(root.resolve("smokes/fixed-wait-migration.lock"), fixed);
        refreshDependencies();
        System.out.println("fixed-wait classifications finalized: " + count + " milestones");
    }

    private void refreshDependencies() throws Exception {
        Properties fixed = load(root.resolve("smokes/fixed-wait-migration.lock"));
        Properties data = load(root.resolve("smokes/data-driven-migration.lock"));
        Properties retries = load(root.resolve("smokes/eof-retry-migration.lock"));
        SmokeInputFingerprint fingerprints = new SmokeInputFingerprint(root); int checked = 0;
        for (SmokeDiscovery.Entry smoke : SmokeDiscovery.discover(root)) {
            boolean affected = fixed.getProperty("milestone." + smoke.id
                    + ".current_fingerprint") != null;
            if (affected) {
                checked++;
                if (smoke.runner.equals("tools/smoke/DataDrivenCycle.java")) {
                    String priorInputs = fixed.getProperty("milestone." + smoke.id
                            + ".prior_descriptor_inputs"), currentInputs = descriptorInputs(smoke.id);
                    require(priorInputs != null && !priorInputs.equals(currentInputs)
                                    && smokeSupportInput(smoke.id),
                            "affected data descriptor lacks smoketest input: " + smoke.id);
                    data.setProperty("cycle." + smoke.id + ".plan_sha256",
                            DataDrivenCyclePlan.load(root, smoke.id).fingerprint());
                    data.setProperty("cycle." + smoke.id + ".prior_inputs", priorInputs);
                    data.setProperty("cycle." + smoke.id + ".current_inputs", currentInputs);
                }
            }
            String retry = "retry." + smoke.id + ".";
            if (affected && retries.getProperty(retry + "source") != null) {
                retries.setProperty(retry + "current_source_sha256",
                        digest(root.resolve(retries.getProperty(retry + "source"))));
                retries.setProperty(retry + "current_fingerprint", fingerprints.compute(smoke));
                retries.setProperty(retry + "evidence_sha256",
                        fixed.getProperty("milestone." + smoke.id + ".evidence_sha256"));
            }
        }
        require(checked == 216, "fixed-wait dependency census drift: " + checked);
        store(root.resolve("smokes/data-driven-migration.lock"), data);
        store(root.resolve("smokes/eof-retry-migration.lock"), retries);
        System.out.println("fixed-wait dependent attestations refreshed: " + checked + " milestones");
    }

    private static SmokePins.Entry requiredEntry(SmokePins pins, String id) {
        SmokePins.Entry entry = pins.entry(id); require(entry != null, "missing unchanged pin: " + id);
        return entry;
    }
    private void annotatePins(Properties fixed) throws Exception {
        Path path = root.resolve("smokes/qualification.lock"); List<String> output = new ArrayList<>();
        for (String line : Files.readAllLines(path, StandardCharsets.UTF_8)) {
            if (line.startsWith("# fixed-wait-refactor-proof=")) continue;
            if (line.startsWith("smoke.") && line.contains(".fingerprint=")) {
                String id = line.substring(6, line.indexOf(".fingerprint="));
                if (fixed.getProperty("milestone." + id + ".current_fingerprint") != null)
                    output.add("# fixed-wait-refactor-proof="
                            + "smokes/fixed-wait-migration.lock:milestone." + id);
            }
            output.add(line);
        }
        Files.writeString(path, String.join("\n", output) + "\n", StandardCharsets.UTF_8);
    }
    private boolean smokeSupportInput(String id) throws Exception {
        return ("," + descriptorInputs(id) + ",")
                .contains(",modules/smoketest/src/main/java,");
    }
    private String descriptorInputs(String id) throws Exception { return load(root.resolve("smokes")
            .resolve(id).resolve("smoke.properties")).getProperty("cycle.inputs", ""); }
    private static String descriptorInputs(byte[] content) throws Exception { Properties values =
            new Properties(); try (Reader reader = new java.io.StringReader(
                    new String(content, StandardCharsets.UTF_8))) { values.load(reader); }
        return values.getProperty("cycle.inputs", ""); }
    private byte[] priorDescriptor(String id) throws Exception {
        String relative = "smokes/" + id + "/smoke.properties";
        Process process = new ProcessBuilder("git", "show", "HEAD:" + relative)
                .directory(root.toFile()).redirectErrorStream(true).start();
        if (!process.waitFor(30, TimeUnit.SECONDS)) {
            process.destroyForcibly(); throw new IllegalStateException("prior descriptor timed out: " + id);
        }
        byte[] content = process.getInputStream().readAllBytes();
        require(process.exitValue() == 0, "missing prior descriptor: " + id);
        return content;
    }
    private static String digest(byte[] content) throws Exception { return HexFormat.of().formatHex(
            MessageDigest.getInstance("SHA-256").digest(content)); }
    private static String transition(String path, String prior, String current) throws Exception {
        return digest(String.join("\0", "worldline-fixed-wait-transition-v1", path, prior, current)
                .getBytes(StandardCharsets.UTF_8));
    }
    private static String digest(Path path) throws Exception { return HexFormat.of().formatHex(
            MessageDigest.getInstance("SHA-256").digest(Files.readString(path, StandardCharsets.UTF_8)
                    .replace("\r\n", "\n").getBytes(StandardCharsets.UTF_8))); }
    private static void store(Path path, Properties values) throws Exception { String name =
            path.getFileName().toString(); String title = name.equals("data-driven-migration.lock")
                    ? "# Worldline data-driven cycle migration v1\n"
                    : name.equals("eof-retry-migration.lock")
                            ? "# Worldline EOF retry migration v1\n"
                            : "# Worldline fixed-wait migration v1\n";
        StringBuilder output = new StringBuilder(title);
        for (String key : values.stringPropertyNames().stream().sorted().toList())
            output.append(key).append('=').append(values.getProperty(key)).append('\n');
        Files.writeString(path, output.toString(), StandardCharsets.UTF_8); }
    private static Properties load(Path path) throws Exception { Properties values = new Properties();
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            values.load(reader); } return values; }
    private static int integer(Properties values, String key) {
        try { return Integer.parseInt(values.getProperty(key, "")); }
        catch (NumberFormatException error) { throw new IllegalStateException("invalid " + key); }
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message); }
}
