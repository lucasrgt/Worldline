import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

/** Applies the reviewed descriptor, map, and narrative schemas as one proof-preserving rewrite. */
final class RepositorySchemaMigration {
    private final Path root;
    private RepositorySchemaMigration(Path root) { this.root = root.toAbsolutePath().normalize(); }
    public static void main(String[] arguments) {
        try {
            require(List.of(arguments).equals(List.of("--apply")),
                    "usage: RepositorySchemaMigration --apply");
            new RepositorySchemaMigration(Path.of("")).apply();
        } catch (Exception error) {
            System.err.println("repository schema migration failed: " + error.getMessage()); System.exit(1);
        }
    }

    private void apply() throws Exception {
        Path lock = root.resolve("smokes/schema-migration.lock");
        Properties manifest = Files.isRegularFile(lock) ? load(lock) : new Properties();
        require(reviewed(manifest),
                "stage a reviewed schema change or commit the shared fingerprint change before refreshing pins");
        List<SmokeDiscovery.Entry> catalog = SmokeDiscovery.discover(root);
        if ("1".equals(manifest.getProperty("schema"))) {
            refresh(catalog, lock, manifest); return;
        }
        SmokePins existing = new SmokePins(root); SmokeInputFingerprint before =
                new SmokeInputFingerprint(root); List<Row> rows = new ArrayList<>(); int narratives = 0;
        for (SmokeDiscovery.Entry smoke : catalog) {
            Path directory = root.resolve("smokes").resolve(smoke.id);
            Path descriptorPath = directory.resolve("smoke.properties");
            String priorDescriptor = Files.readString(descriptorPath, StandardCharsets.UTF_8);
            Properties descriptor = load(descriptorPath); String priorFingerprint = before.compute(smoke);
            SmokePins.Entry pin = existing.match(smoke.id, priorFingerprint);
            require(pin != null, "schema migration lacks a current proof: " + smoke.id);
            String era = "1".equals(descriptor.getProperty("qualification.schema"))
                    ? "qualification-v1" : "legacy";
            String next = append(priorDescriptor, "id", smoke.id);
            next = append(next, "smoke.schema", "1");
            next = append(next, "smoke.era", era);
            next = append(next, "runner.source", smoke.runner);
            if (era.equals("qualification-v1")) {
                if (!"1".equals(descriptor.getProperty("narrative.schema")))
                    next = migrateNarrative(smoke.id, descriptor, next);
                narratives++;
            }
            Files.writeString(descriptorPath, next, StandardCharsets.UTF_8);
            Properties currentDescriptor = load(descriptorPath);
            Path map = root.resolve(required(currentDescriptor, "qualification.semantic-map",
                    "smokes/" + smoke.id + "/MAP.md"));
            schemaMap(map, required(currentDescriptor, "testkit.contract", smoke.id),
                    required(currentDescriptor, "expected.signature", "tooling"));
            rows.add(new Row(smoke, pin, priorFingerprint, digest(priorDescriptor), descriptorPath, map));
        }
        Path aggregate = root.resolve("smokes/redstone-semantics/MAP.md");
        schemaMap(aggregate, "redstone-catalog", "aggregate:redstone-runtime-oracles");
        require(catalog.size() == 525 && narratives == 36, "schema migration census drift");
        SmokeInputFingerprint after = new SmokeInputFingerprint(root); List<SmokePins.Entry> pins =
                new ArrayList<>(); manifest.setProperty("schema", "1"); int changed = 0;
        manifest.setProperty("smoke.count", Integer.toString(catalog.size()));
        manifest.setProperty("map.count", "526"); manifest.setProperty("narrative.count", "36");
        manifest.setProperty("aggregate.map.sha256", digest(aggregate));
        for (Row row : rows) {
            String current = after.compute(row.smoke); String stem = "smoke." + row.smoke.id + ".";
            if (current.equals(row.priorFingerprint)) pins.add(row.pin);
            else { changed++; pins.add(new SmokePins.Entry(row.smoke.id, current,
                    row.pin.evidence(), "refactor-equivalent")); }
            manifest.putIfAbsent(stem + "prior_fingerprint", row.priorFingerprint);
            manifest.setProperty(stem + "current_fingerprint", current);
            manifest.setProperty(stem + "evidence_sha256", row.pin.evidence());
            manifest.putIfAbsent(stem + "prior_descriptor_sha256", row.priorDescriptorHash);
            manifest.setProperty(stem + "descriptor_sha256", digest(row.descriptor));
            manifest.setProperty(stem + "map_sha256", digest(row.map));
        }
        require(changed >= 1, "repository schema migration made no changes");
        manifest.setProperty("fingerprint_source_sha256", digest(
                root.resolve("tools/harness/SmokeInputFingerprint.java")));
        existing.write(pins); store(lock, manifest);
        System.out.println("repository schemas migrated: " + changed
                + " changed; 525 descriptors, 526 maps, 36 narratives");
    }

    private void refresh(List<SmokeDiscovery.Entry> catalog, Path lock,
            Properties manifest) throws Exception {
        SmokePins existing = new SmokePins(root); SmokeInputFingerprint fingerprints =
                new SmokeInputFingerprint(root); SmokeReceiptCache cache = new SmokeReceiptCache(root);
        Properties providers = ProviderDiscoveryPinCheck.manifest(root);
        List<SmokePins.Entry> pins = new ArrayList<>();
        int carried = 0, executed = 0, introduced = 0, introducedNarratives = 0;
        for (SmokeDiscovery.Entry smoke : catalog) {
            String current = fingerprints.compute(smoke); SmokePins.Entry local = cache.availablePin(smoke);
            SmokePins.Entry prior = existing.entry(smoke.id);
            if (local != null && local.source().equals("executed")) {
                pins.add(local); executed++;
            } else {
                require(prior != null && current.equals(prior.fingerprint()),
                        "schema refresh lacks a current proof: " + smoke.id);
                pins.add(prior);
            }
            if (ProviderDiscoveryPinCheck.exemptsLegacy(providers, smoke.id)) continue;
            carried++; String stem = "smoke." + smoke.id + ".";
            SmokePins.Entry proof = local != null && local.source().equals("executed") ? local : prior;
            String recorded = manifest.getProperty(stem + "current_fingerprint");
            if (recorded == null) {
                require(local != null && local.source().equals("executed"),
                        "new schema row lacks exact execution: " + smoke.id);
                Path directory = root.resolve("smokes").resolve(smoke.id);
                Properties descriptor = load(directory.resolve("smoke.properties"));
                manifest.setProperty(stem + "introduced", "true");
                manifest.setProperty(stem + "descriptor_sha256", digest(
                        directory.resolve("smoke.properties")));
                manifest.setProperty(stem + "map_sha256", digest(directory.resolve("MAP.md")));
                introduced++;
                if ("1".equals(descriptor.getProperty("narrative.schema"))) introducedNarratives++;
            }
            if (recorded != null && !current.equals(recorded))
                manifest.setProperty(stem + "prior_fingerprint", recorded);
            manifest.setProperty(stem + "current_fingerprint", current);
            manifest.setProperty(stem + "evidence_sha256", proof.evidence());
        }
        int pending = ProviderDiscoveryPinCheck.pendingCount(providers);
        int smokeCount = carried + pending;
        require(executed >= 1,
                "repository schema refresh lacks exact support proofs: executed=" + executed);
        manifest.setProperty("smoke.count", Integer.toString(smokeCount));
        manifest.setProperty("map.count", Integer.toString(smokeCount + 1));
        manifest.setProperty("narrative.count", Integer.toString(
                integer(manifest, "narrative.count") + introducedNarratives));
        manifest.setProperty("fingerprint_source_sha256", digest(
                root.resolve("tools/harness/SmokeInputFingerprint.java")));
        existing.write(pins); store(lock, manifest);
        System.out.println("repository schema pins refreshed: " + carried
                + " carried, " + introduced + " introduced, " + executed
                + " exact support proofs");
    }

    private String migrateNarrative(String id, Properties descriptor, String text) throws Exception {
        Path docs = root.resolve(required(descriptor, "qualification.docs", null));
        Path cycle = root.resolve(required(descriptor, "qualification.cycle", null));
        String cycleBody = body(Files.readString(cycle, StandardCharsets.UTF_8));
        String claim = docs.equals(cycle)
                ? "This milestone freezes the behavior identified by its expected signal and semantic signature."
                : body(Files.readString(docs, StandardCharsets.UTF_8));
        text = set(text, "qualification.docs", root.relativize(cycle).toString().replace('\\', '/'));
        text = set(text, "qualification.cycle", root.relativize(cycle).toString().replace('\\', '/'));
        text = append(text, "narrative.schema", "1");
        text = append(text, "narrative.title", title(id));
        text = append(text, "narrative.claim", escape(claim));
        text = append(text, "narrative.cycle", escape(cycleBody));
        Path descriptorPath = root.resolve("smokes").resolve(id).resolve("smoke.properties");
        Files.writeString(descriptorPath, text, StandardCharsets.UTF_8);
        Properties migrated = load(descriptorPath);
        Files.writeString(cycle, MilestoneNarrative.render(migrated), StandardCharsets.UTF_8);
        if (!docs.equals(cycle)) Files.delete(docs);
        return Files.readString(descriptorPath, StandardCharsets.UTF_8);
    }

    private void schemaMap(Path path, String boundary, String trace) throws Exception {
        String text = Files.readString(path, StandardCharsets.UTF_8);
        if (text.startsWith("<!-- worldline-map-schema=1 -->")) return;
        String header = "<!-- worldline-map-schema=1 -->\n<!-- boundary=" + token(boundary)
                + " -->\n<!-- nonclaims=bounded-to-qualified-evidence -->\n<!-- frozen-trace="
                + token(trace) + " -->\n\n";
        Files.writeString(path, header + text, StandardCharsets.UTF_8);
    }
    private static String body(String text) {
        String normalized = text.replace("\r\n", "\n"); int line = normalized.indexOf('\n');
        return (line < 0 ? normalized : normalized.substring(line + 1)).trim();
    }
    private static String title(String id) {
        String value = id.replaceFirst("^m[0-9]+(?:-m[0-9]+)?-", "").replace("-sw-", "-")
                .replace('-', ' '); return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }
    private static String escape(String value) { return value.replace("\\", "\\\\")
            .replace("\r", "").replace("\n", "\\n"); }
    private static String token(String value) { return value.replace("--", "-").replace('\n', ' ').trim(); }
    private static String append(String text, String key, String value) {
        if (Pattern.compile("(?m)^" + Pattern.quote(key) + "=").matcher(text).find()) return text;
        return (text.endsWith("\n") ? text : text + "\n") + key + "=" + value + "\n";
    }
    private static String set(String text, String key, String value) {
        return text.replaceFirst("(?m)^" + Pattern.quote(key) + "=.*$", key + "=" + value);
    }
    private static String required(Properties values, String key, String fallback) {
        String value = values.getProperty(key, fallback); require(value != null && !value.isBlank(),
                "missing " + key + " for " + values.getProperty("id")); return value.trim();
    }
    private static int integer(Properties values, String key) {
        try { return Integer.parseInt(required(values, key, null)); }
        catch (NumberFormatException error) { throw new IllegalStateException("invalid " + key); }
    }
    private boolean dirtyIndex() throws Exception { Process process = new ProcessBuilder("git", "diff",
            "--cached", "--quiet").directory(root.toFile()).start(); return process.waitFor() == 1; }
    private boolean reviewed(Properties manifest) throws Exception {
        if (dirtyIndex()) return true;
        if (!"1".equals(manifest.getProperty("schema"))) return false;
        Process worktree = new ProcessBuilder("git", "diff", "--quiet")
                .directory(root.toFile()).start();
        Process index = new ProcessBuilder("git", "diff", "--cached", "--quiet")
                .directory(root.toFile()).start();
        String recorded = manifest.getProperty("fingerprint_source_sha256", "");
        return worktree.waitFor() == 0 && index.waitFor() == 0
                && !digest(root.resolve("tools/harness/SmokeInputFingerprint.java"))
                        .equals(recorded);
    }
    private static Properties load(Path path) throws Exception { Properties values = new Properties();
        try (var reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            values.load(reader); } return values;
    }
    private static String digest(String value) throws Exception { return HexFormat.of().formatHex(
            MessageDigest.getInstance("SHA-256").digest(value.replace("\r\n", "\n")
                    .getBytes(StandardCharsets.UTF_8)));
    }
    private static String digest(Path path) throws Exception {
        return digest(Files.readString(path, StandardCharsets.UTF_8));
    }
    private static void store(Path path, Properties values) throws Exception {
        StringBuilder output = new StringBuilder("# Worldline repository schema migration v1\n");
        for (String key : values.stringPropertyNames().stream().sorted(Comparator.naturalOrder()).toList())
            output.append(key).append('=').append(values.getProperty(key)).append('\n');
        Files.writeString(path, output.toString(), StandardCharsets.UTF_8);
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
    private record Row(SmokeDiscovery.Entry smoke, SmokePins.Entry pin, String priorFingerprint,
            String priorDescriptorHash, Path descriptor, Path map) { }
}
