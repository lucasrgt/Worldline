import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Reviewed migration of deterministic composite coordinators to declarative plans. */
final class CompositeCycleMigration {
    private static final Pattern ID = Pattern.compile(
            "private\\s+static\\s+final\\s+String\\s+ID\\s*=\\s*\"([^\"]+)\"");
    private static final Pattern MAIN = Pattern.compile(
            "\"(worldline\\.(?:smoke|b173server)\\.[A-Za-z0-9_.]+)\"");
    private static final Pattern VALUE = Pattern.compile("value\\(config,\\s*\"([^\"]+)\"\\)");
    private static final Pattern PRODUCT = Pattern.compile("product\\(\\s*\"([a-z0-9-]+)\"\\)");
    private static final Pattern INPUT = Pattern.compile(
            "javaFiles\\(root\\.resolve\\(\\s*\"([^\"]+)\"\\)\\)");
    private static final Pattern PREFIX = Pattern.compile("line\\(output,\\s*\"([^\"]+)\"\\)");
    private static final Pattern OUTPUT = Pattern.compile("output\\.contains\\(\\s*\"([^\"]+)\"\\)");
    private static final Pattern CONTAINS = Pattern.compile(
            "(!?)(?:first\\.)?(signal|trace)\\.contains\\(\\s*\"([^\"]+)\"\\)");
    private final Path root;

    private CompositeCycleMigration(Path root) { this.root = root.toAbsolutePath().normalize(); }
    public static void main(String[] arguments) {
        try {
            require(arguments.length == 1 && (arguments[0].equals("--apply")
                    || arguments[0].equals("--refresh")),
                    "usage: CompositeCycleMigration [--apply|--refresh]");
            CompositeCycleMigration migration = new CompositeCycleMigration(Path.of(""));
            if (arguments[0].equals("--apply")) migration.apply(); else migration.refresh();
        } catch (Exception error) {
            System.err.println("composite cycle migration failed: " + error.getMessage()); System.exit(1);
        }
    }

    private void apply() throws Exception {
        require(dirtyIndex(), "stage the migration implementation before applying the rewrite");
        SmokeInputFingerprint oldFingerprints = new SmokeInputFingerprint(root);
        SmokePins oldPins = new SmokePins(root); List<Migration> migrations = new ArrayList<>();
        for (Path source : cycleSources()) {
            String text = Files.readString(source, StandardCharsets.UTF_8);
            if (text.lines().count() > 180) continue;
            Plan plan; try { plan = parse(source, text); }
            catch (IllegalStateException unsupported) { continue; }
            SmokeDiscovery.Entry smoke = new SmokeDiscovery.Entry(plan.id, relative(source));
            String prior = oldFingerprints.compute(smoke); SmokePins.Entry pin = oldPins.match(plan.id, prior);
            require(pin != null, "composite cycle lacks a current pin: " + plan.id);
            migrations.add(new Migration(source, text, plan, prior, pin));
        }
        require(migrations.size() >= 19, "composite cycle census unexpectedly small: " + migrations.size());
        for (Migration migration : migrations) {
            appendPlan(migration.plan); Files.delete(migration.source);
        }
        Properties manifest = new Properties(); manifest.setProperty("schema", "1");
        manifest.setProperty("count", Integer.toString(migrations.size()));
        manifest.setProperty("runner_sha256", digest(root.resolve("tools/smoke/CompositeCycle.java")));
        manifest.setProperty("plan_source_sha256", digest(
                root.resolve("tools/harness/CompositeCyclePlan.java")));
        manifest.setProperty("support_source_sha256", digest(
                root.resolve("tools/harness/DataDrivenSupport.java")));
        SmokeInputFingerprint current = new SmokeInputFingerprint(root);
        List<SmokePins.Entry> pins = new ArrayList<>();
        for (SmokeDiscovery.Entry smoke : SmokeDiscovery.discover(root)) {
            Migration migration = migrations.stream().filter(value -> value.plan.id.equals(smoke.id))
                    .findFirst().orElse(null);
            if (migration == null) { pins.add(requiredEntry(oldPins, smoke.id)); continue; }
            String fingerprint = current.compute(smoke); CompositeCyclePlan plan =
                    CompositeCyclePlan.load(root, smoke.id); String stem = "cycle." + smoke.id + ".";
            pins.add(new SmokePins.Entry(smoke.id, fingerprint, migration.pin.evidence(),
                    "refactor-equivalent"));
            manifest.setProperty(stem + "source", relative(migration.source));
            manifest.setProperty(stem + "source_sha256", digest(migration.text));
            manifest.setProperty(stem + "prior_fingerprint", migration.priorFingerprint);
            manifest.setProperty(stem + "plan_sha256", plan.fingerprint());
            manifest.setProperty(stem + "evidence_sha256", migration.pin.evidence());
        }
        oldPins.write(pins); store(root.resolve("smokes/composite-cycle-migration.lock"), manifest);
        System.out.println("composite cycle migration applied: " + migrations.size() + " runners");
    }

    private void refresh() throws Exception {
        require(dirtyIndex(), "stage the reviewed shared-support change before refreshing composite pins");
        Path manifestPath = root.resolve("smokes/composite-cycle-migration.lock");
        Properties manifest = load(manifestPath); SmokePins existing = new SmokePins(root);
        Properties train = TrainPinCheck.manifest(root);
        SmokeReceiptCache cache = new SmokeReceiptCache(root); List<SmokePins.Entry> pins = new ArrayList<>();
        int composite = 0, executed = 0, imported = 0;
        for (SmokeDiscovery.Entry smoke : SmokeDiscovery.discover(root)) {
            SmokePins.Entry local = cache.availablePin(smoke);
            if (local != null && local.source().equals("executed")) executed++;
            if (!smoke.runner.equals("tools/smoke/CompositeCycle.java")) {
                pins.add(requiredEntry(existing, smoke.id)); continue;
            }
            composite++; String stem = "cycle." + smoke.id + ".";
            CompositeCyclePlan plan = CompositeCyclePlan.load(root, smoke.id);
            String recordedPlan = manifest.getProperty(stem + "plan_sha256");
            if (recordedPlan == null) {
                Properties descriptor = load(root.resolve("smokes").resolve(smoke.id)
                        .resolve("smoke.properties"));
                String source = required(descriptor, "cycle.legacy.source");
                require(source.matches("tools/smoke/[A-Za-z0-9]+Cycle[.]java")
                                && !Files.exists(root.resolve(source)),
                        "incremental composite legacy source was not removed: " + smoke.id);
                SmokePins.Entry prior = requiredEntry(existing, smoke.id);
                require(TrainPinCheck.carriesCurrent(train, smoke.id, prior, prior.fingerprint()),
                        "incremental composite lacks a current train proof: " + smoke.id);
                manifest.setProperty(stem + "source", source);
                manifest.setProperty(stem + "source_sha256", digest(committed(source)));
                manifest.setProperty(stem + "prior_fingerprint", prior.fingerprint());
                manifest.setProperty(stem + "plan_sha256", plan.fingerprint());
                manifest.setProperty(stem + "evidence_sha256", prior.evidence());
                imported++;
            } else require(plan.fingerprint().equals(recordedPlan),
                    "composite plan changed outside the reviewed migration: " + smoke.id);
            pins.add(new SmokePins.Entry(smoke.id, cache.fingerprint(smoke),
                    requiredHash(manifest, stem + "evidence_sha256"), "refactor-equivalent"));
        }
        require(composite == integer(manifest, "count") + imported && (executed >= 1 || imported >= 1),
                "composite refresh requires an exact support proof or current train receipt");
        manifest.setProperty("count", Integer.toString(composite));
        manifest.setProperty("runner_sha256", digest(root.resolve("tools/smoke/CompositeCycle.java")));
        manifest.setProperty("plan_source_sha256", digest(
                root.resolve("tools/harness/CompositeCyclePlan.java")));
        manifest.setProperty("support_source_sha256", digest(
                root.resolve("tools/harness/DataDrivenSupport.java")));
        manifest.setProperty("runtime_support_source_sha256", digest(
                root.resolve("tools/harness/SmokeSupport.java")));
        existing.write(pins); store(manifestPath, manifest);
        System.out.println("composite pins refreshed: " + composite + " plans, " + executed
                + " exact support proofs, " + imported + " train-imported plans");
    }

    private Plan parse(Path source, String text) {
        String id = one(ID, text, "id", source); int runStart = text.indexOf("private Outcome run");
        int compileStart = text.indexOf("private Path compile");
        require(runStart > 0 && compileStart > 0, "unsupported cycle sections: " + source);
        String run = text.substring(runStart), compile = text.substring(compileStart);
        List<String> mains = matches(MAIN, run); require(!mains.isEmpty(), "unsupported main: " + source);
        String main = mains.get(mains.size() - 1); int mainPosition = run.indexOf(main);
        int mainEnd = run.indexOf('"', mainPosition + main.length());
        int commandStart = run.lastIndexOf("Arrays.asList(", mainPosition);
        int commandEnd = run.indexOf("));", mainEnd);
        require(commandStart >= 0 && commandEnd > mainEnd, "unsupported command: " + source);
        String command = run.substring(commandStart, commandEnd);
        List<String> arguments = matches(VALUE, run.substring(mainEnd, commandEnd));
        List<String> prefixes = unique(matches(PREFIX, run));
        List<String> traces = prefixes.stream().filter(value -> value.contains("_TRACE=")).toList();
        List<String> signatures = prefixes.stream().filter(value -> value.contains("_SIGNATURE=")).toList();
        List<String> signals = prefixes.stream().filter(value -> !traces.contains(value)
                && !signatures.contains(value)).toList();
        require(!arguments.isEmpty() && traces.size() == 1 && signatures.size() == 1
                && !signals.isEmpty(), "unsupported outputs: " + source);
        boolean compare = Pattern.compile("first\\.[A-Za-z]+\\.equals\\(second\\.[A-Za-z]+\\)")
                .matcher(text).find();
        boolean expected = signals.size() == 1 && text.contains("expected.signal");
        return new Plan(id, main, arguments, unique(matches(INPUT, compile)),
                unique(matches(PRODUCT, compile)), unique(matches(PRODUCT, command)), traces.get(0),
                signatures.get(0), signals, unique(matches(OUTPUT, run)),
                assertions(text, "signal", false), assertions(text, "signal", true),
                assertions(text, "trace", false), assertions(text, "trace", true), compare, expected);
    }

    private void appendPlan(Plan plan) throws Exception {
        Path path = root.resolve("smokes").resolve(plan.id).resolve("smoke.properties");
        StringBuilder output = new StringBuilder(Files.readString(path, StandardCharsets.UTF_8));
        if (output.length() > 0 && output.charAt(output.length() - 1) != '\n') output.append('\n');
        output.append("runner.source=tools/smoke/CompositeCycle.java\ncycle.composite.schema=1\n")
                .append("cycle.migration=legacy-composite-v1\n")
                .append("cycle.artifact=artifacts/minecraft-b1.7.3-server.properties\n")
                .append("cycle.main=").append(plan.main).append('\n');
        row(output, "cycle.args", plan.arguments); row(output, "cycle.inputs", plan.inputs);
        row(output, "cycle.compile.products", plan.compileProducts);
        row(output, "cycle.runtime.products", plan.runtimeProducts);
        output.append("cycle.trace.prefix=").append(plan.tracePrefix).append('\n')
                .append("cycle.signature.prefix=").append(plan.signaturePrefix).append('\n')
                .append("cycle.compare.signal=").append(plan.compareSignal).append('\n')
                .append("cycle.require.expected.signal=").append(plan.requireExpectedSignal).append('\n');
        numbered(output, "cycle.signal.prefix", plan.signalPrefixes);
        numbered(output, "cycle.output.contains", plan.outputContains);
        numbered(output, "cycle.signal.contains", plan.signalContains);
        numbered(output, "cycle.signal.excludes", plan.signalExcludes);
        numbered(output, "cycle.trace.contains", plan.traceContains);
        numbered(output, "cycle.trace.excludes", plan.traceExcludes);
        Files.writeString(path, output.toString(), StandardCharsets.UTF_8);
    }

    private List<Path> cycleSources() throws Exception { try (var paths = Files.list(root.resolve("tools/smoke"))) {
        return paths.filter(path -> path.getFileName().toString().endsWith("Cycle.java"))
                .filter(path -> !List.of("DataDrivenCycle.java", "CompositeCycle.java")
                        .contains(path.getFileName().toString())).sorted().toList();
    } }
    private boolean dirtyIndex() throws Exception { Process process = new ProcessBuilder("git", "diff", "--cached",
            "--quiet").directory(root.toFile()).start(); return process.waitFor() == 1; }
    private String relative(Path path) { return root.relativize(path).toString().replace('\\', '/'); }
    private static SmokePins.Entry requiredEntry(SmokePins pins, String id) {
        SmokePins.Entry entry = pins.entry(id); require(entry != null, "missing unchanged pin: " + id); return entry;
    }
    private static int integer(Properties values, String key) {
        try { return Integer.parseInt(values.getProperty(key, "")); }
        catch (NumberFormatException error) { throw new IllegalStateException("invalid " + key); }
    }
    private static String requiredHash(Properties values, String key) {
        String value = values.getProperty(key, "");
        require(value.matches("[0-9a-f]{64}"), "missing " + key); return value;
    }
    private static String one(Pattern pattern, String text, String label, Path source) {
        List<String> values = matches(pattern, text);
        require(values.size() == 1, "unsupported " + label + ": " + source); return values.get(0);
    }
    private static List<String> matches(Pattern pattern, String text) {
        List<String> values = new ArrayList<>(); Matcher matcher = pattern.matcher(text);
        while (matcher.find()) values.add(matcher.group(1)); return values;
    }
    private static List<String> assertions(String text, String field, boolean excluded) {
        List<String> values = new ArrayList<>(); Matcher matcher = CONTAINS.matcher(text);
        while (matcher.find()) if (matcher.group(2).equals(field)
                && matcher.group(1).equals(excluded ? "!" : "")) values.add(matcher.group(3));
        return unique(values);
    }
    private static List<String> unique(List<String> values) {
        return List.copyOf(new LinkedHashSet<>(values));
    }
    private static void row(StringBuilder output, String key, List<String> values) {
        output.append(key).append('=').append(String.join(",", values)).append('\n');
    }
    private static void numbered(StringBuilder output, String key, List<String> values) {
        output.append(key).append(".count=").append(values.size()).append('\n');
        for (int index = 0; index < values.size(); index++)
            output.append(key).append('.').append(index + 1).append('=').append(values.get(index)).append('\n');
    }
    private static String digest(String value) throws Exception { return HexFormat.of().formatHex(
            MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
    }
    private static String digest(Path path) throws Exception { return HexFormat.of().formatHex(
            MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(path)));
    }
    private String committed(String relative) throws Exception {
        Process process = new ProcessBuilder("git", "show", "HEAD:" + relative)
                .directory(root.toFile()).redirectErrorStream(true).start();
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        require(process.waitFor() == 0, "missing committed legacy source: " + relative);
        return output;
    }
    private static void store(Path path, Properties values) throws Exception {
        StringBuilder output = new StringBuilder("# Worldline composite cycle migration v1\n");
        for (String key : values.stringPropertyNames().stream().sorted(Comparator.naturalOrder()).toList())
            output.append(key).append('=').append(values.getProperty(key)).append('\n');
        Files.writeString(path, output.toString(), StandardCharsets.UTF_8);
    }
    private static Properties load(Path path) throws Exception { Properties values = new Properties();
        try (var reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            values.load(reader); } return values; }
    private static String required(Properties values, String key) {
        String value = values.getProperty(key);
        require(value != null && !value.isBlank(), "missing " + key);
        return value.trim();
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
    private record Migration(Path source, String text, Plan plan, String priorFingerprint,
            SmokePins.Entry pin) { }
    private record Plan(String id, String main, List<String> arguments, List<String> inputs,
            List<String> compileProducts, List<String> runtimeProducts, String tracePrefix,
            String signaturePrefix, List<String> signalPrefixes, List<String> outputContains,
            List<String> signalContains, List<String> signalExcludes, List<String> traceContains,
            List<String> traceExcludes, boolean compareSignal, boolean requireExpectedSignal) { }
}
