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
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** One reviewed mechanical migration from packed ordinary cycles to declarative plans. */
final class DataDrivenCycleMigration {
    private static final Pattern ID = Pattern.compile(
            "private\\s+static\\s+final\\s+String\\s+ID\\s*=\\s*\\\"([^\\\"]+)\\\"");
    private static final Pattern MAIN = Pattern.compile("\\\"(worldline\\.smoke\\.[^\\\"]+)\\\"");
    private static final Pattern VALUE = Pattern.compile(
            "value\\(config,\\s*\\\"([^\\\"]+)\\\"\\)");
    private static final Pattern PRODUCT = Pattern.compile(
            "product\\(\\s*\\\"([a-z0-9-]+)\\\"\\)");
    private static final Pattern INPUT = Pattern.compile(
            "javaFiles\\(root\\.resolve\\(\\s*\\\"([^\\\"]+)\\\"\\)\\)");
    private static final Pattern PREFIX = Pattern.compile(
            "line\\(output,\\s*\\\"([^\\\"]+)\\\"\\)");
    private static final Pattern OUTPUT = Pattern.compile(
            "output\\.contains\\(\\s*\\\"([^\\\"]+)\\\"\\)");
    private static final Pattern CONTAINS = Pattern.compile(
            "(!?)(?:first\\.)?(signal|trace)\\.contains\\(\\s*\\\"([^\\\"]+)\\\"\\)");
    private final Path root;

    DataDrivenCycleMigration(Path root) { this.root = root.toAbsolutePath().normalize(); }

    public static void main(String[] arguments) {
        try {
            require(arguments.length == 1 && (arguments[0].equals("--apply")
                    || arguments[0].equals("--refresh")),
                    "usage: DataDrivenCycleMigration [--apply|--refresh]");
            DataDrivenCycleMigration migration = new DataDrivenCycleMigration(Path.of(""));
            if (arguments[0].equals("--apply")) migration.apply(); else migration.refresh();
        } catch (Exception error) {
            System.err.println("data-driven migration failed: " + error.getMessage()); System.exit(1);
        }
    }

    void apply() throws Exception {
        require(cleanIndex(), "stage the migration implementation before applying the rewrite");
        SmokeInputFingerprint oldFingerprints = new SmokeInputFingerprint(root);
        SmokePins oldPins = new SmokePins(root); List<Migration> migrations = new ArrayList<>();
        Properties manifest = load(root.resolve("smokes/data-driven-migration.lock"));
        int priorCount = integer(manifest, "count");
        for (Path source : cycleSources()) {
            String text = Files.readString(source, StandardCharsets.UTF_8);
            long lines = text.lines().count(); if (lines < 10 || lines > 180) continue;
            Plan plan;
            try { plan = parse(source, text); }
            catch (IllegalStateException unsupported) { continue; }
            SmokeDiscovery.Entry smoke =
                    new SmokeDiscovery.Entry(plan.id, relative(source));
            String prior = oldFingerprints.compute(smoke); SmokePins.Entry pin = oldPins.match(plan.id, prior);
            require(pin != null, "ordinary cycle lacks a current pin: " + plan.id);
            migrations.add(new Migration(source, text, plan, prior, pin));
        }
        require(migrations.size() >= 12,
                "second-wave cycle census unexpectedly small: " + migrations.size());
        for (Migration migration : migrations) {
            appendPlan(migration.plan); Files.delete(migration.source);
        }
        List<SmokeDiscovery.Entry> after = SmokeDiscovery.discover(root);
        SmokeInputFingerprint newFingerprints = new SmokeInputFingerprint(root);
        List<SmokePins.Entry> pins = new ArrayList<>();
        manifest.setProperty("schema", "1");
        manifest.setProperty("count", Integer.toString(priorCount + migrations.size()));
        for (SmokeDiscovery.Entry smoke : after) {
            Migration migration = migrations.stream().filter(value -> value.plan.id.equals(smoke.id))
                    .findFirst().orElse(null);
            if (migration == null) { pins.add(requiredEntry(oldPins, smoke.id)); continue; }
            String current = newFingerprints.compute(smoke); DataDrivenCyclePlan plan =
                    DataDrivenCyclePlan.load(root, smoke.id);
            pins.add(new SmokePins.Entry(smoke.id, current, migration.pin.evidence(),
                    "refactor-equivalent"));
            String stem = "cycle." + smoke.id + ".";
            manifest.setProperty(stem + "source", relative(migration.source));
            manifest.setProperty(stem + "source_sha256", digest(migration.text));
            manifest.setProperty(stem + "prior_fingerprint", migration.priorFingerprint);
            manifest.setProperty(stem + "plan_sha256", plan.fingerprint());
            manifest.setProperty(stem + "evidence_sha256", migration.pin.evidence());
        }
        oldPins.write(pins); store(root.resolve("smokes/data-driven-migration.lock"), manifest);
        System.out.println("data-driven cycle migration applied: " + migrations.size() + " runners");
    }

    void refresh() throws Exception {
        Path manifestPath = root.resolve("smokes/data-driven-migration.lock");
        Properties manifest = load(manifestPath);
        require(reviewedRefresh(manifest),
                "stage the reviewed runner change or commit a clean shared-support change before refreshing pins");
        SmokePins existing = new SmokePins(root);
        Properties train = TrainPinCheck.manifest(root);
        SmokeReceiptCache cache = new SmokeReceiptCache(root); List<SmokePins.Entry> pins = new ArrayList<>();
        boolean sharedPlanRefactor = !digest(root.resolve("tools/harness/DataDrivenCyclePlan.java"))
                .equals(manifest.getProperty("plan_source_sha256", ""));
        int generic = 0, executed = 0, importedPlans = 0;
        int fixtureRefactors = integer(manifest, "refresh.fixture.count"), newRefactors = 0;
        int formattingRefactors = Integer.parseInt(
                manifest.getProperty("refresh.formatting.count", "0"));
        for (SmokeDiscovery.Entry smoke : SmokeDiscovery.discover(root)) {
            if (!smoke.runner.equals("tools/smoke/DataDrivenCycle.java")) {
                pins.add(requiredEntry(existing, smoke.id)); continue;
            }
            generic++; String stem = "cycle." + smoke.id + ".";
            DataDrivenCyclePlan plan = DataDrivenCyclePlan.load(root, smoke.id);
            String recordedPlan = manifest.getProperty(stem + "plan_sha256");
            if (recordedPlan == null) {
                require(unchangedMilestone(smoke.id),
                        "unregistered generic plan changed with the shared runner: " + smoke.id);
                SmokePins.Entry prior = cache.availablePin(smoke);
                if (prior == null) prior = requiredEntry(existing, smoke.id);
                if (!"executed".equals(prior.source())) {
                    require(TrainPinCheck.carriesCurrent(train, smoke.id, prior,
                                    cache.fingerprint(smoke)),
                            "unregistered generic plan lacks current train proof: " + smoke.id);
                    importedPlans++;
                }
                manifest.setProperty(stem + "plan_sha256", plan.fingerprint());
                manifest.setProperty(stem + "evidence_sha256", prior.evidence());
            } else require(plan.fingerprint().equals(recordedPlan),
                    "plan changed outside the reviewed migration: " + smoke.id);
            SmokePins.Entry local = cache.availablePin(smoke);
            SmokePins.Entry existingPin = requiredEntry(existing, smoke.id);
            String current = cache.fingerprint(smoke);
            if (local == null && !current.equals(existingPin.fingerprint()) && !sharedPlanRefactor) {
                FixtureRefactor refactor = fixtureRefactor(smoke.id);
                if (refactor != null) {
                    fixtureRefactors++; newRefactors++;
                    String key = "refresh.fixture." + fixtureRefactors + ".";
                    manifest.setProperty(key + "id", smoke.id);
                    manifest.setProperty(key + "path", refactor.path);
                    manifest.setProperty(key + "prior_sha256", digest(refactor.prior));
                    manifest.setProperty(key + "current_sha256", digest(refactor.current));
                } else {
                    List<SmokeSourceRefactor.Row> rows = SmokeSourceRefactor.formatting(root,
                            smoke.id, manifest);
                    require(!rows.isEmpty(), "generic milestone changed without proof: " + smoke.id);
                    for (SmokeSourceRefactor.Row row : rows) {
                        String key = "refresh.formatting." + (++formattingRefactors) + ".";
                        manifest.setProperty(key + "id", smoke.id); manifest.setProperty(key + "path", row.path());
                        manifest.setProperty(key + "prior_sha256", row.prior());
                        manifest.setProperty(key + "current_sha256", row.current()); newRefactors++;
                    }
                }
            }
            if (local != null && local.source().equals("executed")) { pins.add(local); executed++; }
            else if (current.equals(existingPin.fingerprint())) pins.add(existingPin);
            else pins.add(new SmokePins.Entry(smoke.id, current,
                    requiredHash(manifest, stem + "evidence_sha256"), "refactor-equivalent"));
        }
        require(generic >= 300 && (executed >= 1 || newRefactors >= 1 || importedPlans >= 1
                        || sharedPlanRefactor),
                "refresh requires an exact proof, train receipt, or canonical fixture refactor");
        manifest.setProperty("refresh.fixture.count", Integer.toString(fixtureRefactors));
        manifest.setProperty("refresh.formatting.count", Integer.toString(formattingRefactors));
        manifest.setProperty("runner_sha256", digest(root.resolve("tools/smoke/DataDrivenCycle.java")));
        manifest.setProperty("plan_source_sha256", digest(
                root.resolve("tools/harness/DataDrivenCyclePlan.java")));
        manifest.setProperty("support_source_sha256", digest(
                root.resolve("tools/harness/DataDrivenSupport.java")));
        manifest.setProperty("runtime_support_source_sha256", digest(
                root.resolve("tools/harness/SmokeSupport.java")));
        existing.write(pins); store(manifestPath, manifest);
        System.out.println("data-driven pins refreshed: " + generic + " generic, " + executed
                + " freshly executed, " + importedPlans + " train-imported plans, " + fixtureRefactors
                + " canonical fixture refactors, " + formattingRefactors
                + " token-equivalent source refactors");
    }

    private FixtureRefactor fixtureRefactor(String id) throws Exception {
        List<String> paths = capture("diff", "--name-only", "HEAD", "--", "smokes/" + id)
                .lines().filter(value -> !value.isBlank()).toList();
        if (paths.size() != 1 || !paths.get(0).endsWith(".java")) return null;
        String path = paths.get(0), prior = capture("show", "HEAD:" + path);
        String current = Files.readString(root.resolve(path), StandardCharsets.UTF_8);
        String expected = SharedFixturePatch.rewrite(prior).replace("\r\n", "\n");
        String normalized = current.replace("\r\n", "\n");
        return expected.equals(normalized)
                ? new FixtureRefactor(path, prior, current) : null;
    }

    private String capture(String... arguments) throws Exception {
        List<String> command = new ArrayList<>(List.of("git")); command.addAll(List.of(arguments));
        Process process = new ProcessBuilder(command).directory(root.toFile()).redirectErrorStream(true).start();
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        require(process.waitFor() == 0, "git command failed: " + String.join(" ", command)); return output;
    }

    private boolean unchangedMilestone(String id) throws Exception {
        Process process = new ProcessBuilder("git", "diff", "--quiet", "HEAD", "--",
                "smokes/" + id).directory(root.toFile()).start();
        return process.waitFor() == 0;
    }

    private boolean reviewedRefresh(Properties manifest) throws Exception {
        if (cleanIndex()) return true;
        Process worktree = new ProcessBuilder("git", "diff", "--quiet")
                .directory(root.toFile()).start();
        Process index = new ProcessBuilder("git", "diff", "--cached", "--quiet")
                .directory(root.toFile()).start();
        String recorded = manifest.getProperty("runtime_support_source_sha256", "");
        String recordedPlan = manifest.getProperty("plan_source_sha256", "");
        return worktree.waitFor() == 0 && index.waitFor() == 0
                && (!digest(root.resolve("tools/harness/SmokeSupport.java")).equals(recorded)
                || !digest(root.resolve("tools/harness/DataDrivenCyclePlan.java"))
                        .equals(recordedPlan));
    }

    private Plan parse(Path source, String text) {
        String id = one(ID, text, "id", source); int runStart = text.indexOf("private Outcome run");
        int compileStart = text.indexOf("private Path compile");
        require(runStart > 0 && compileStart > 0, "unsupported cycle sections: " + source);
        String run = text.substring(runStart), compile = text.substring(compileStart);
        String main = one(MAIN, run, "main", source); int mainPosition = run.indexOf(main);
        int mainEnd = run.indexOf('"', mainPosition + main.length());
        int commandStart = run.lastIndexOf("Arrays.asList(", mainPosition);
        int commandEnd = run.indexOf("));", mainEnd);
        require(commandStart >= 0 && commandEnd > mainEnd, "unsupported command: " + source);
        String command = run.substring(commandStart, commandEnd);
        List<String> arguments = matches(VALUE, run.substring(mainEnd, commandEnd));
        List<String> prefixes = matches(PREFIX, run);
        require(!arguments.isEmpty() && prefixes.size() == 3, "unsupported outputs: " + source);
        return new Plan(id, main, arguments, unique(matches(INPUT, compile)),
                unique(matches(PRODUCT, compile)), unique(matches(PRODUCT, command)), prefixes,
                unique(matches(OUTPUT, run)), assertions(text, "signal", false),
                assertions(text, "signal", true), assertions(text, "trace", false),
                assertions(text, "trace", true));
    }

    private void appendPlan(Plan plan) throws Exception {
        Path path = root.resolve("smokes").resolve(plan.id).resolve("smoke.properties");
        StringBuilder output = new StringBuilder(Files.readString(path, StandardCharsets.UTF_8));
        if (output.length() > 0 && output.charAt(output.length() - 1) != '\n') output.append('\n');
        output.append("runner.source=tools/smoke/DataDrivenCycle.java\ncycle.schema=1\n")
                .append("cycle.migration=legacy-v1\ncycle.artifact=artifacts/minecraft-b1.7.3-server.properties\n")
                .append("cycle.main=").append(plan.main).append('\n');
        row(output, "cycle.args", plan.arguments); row(output, "cycle.inputs", plan.inputs);
        row(output, "cycle.compile.products", plan.compileProducts);
        row(output, "cycle.runtime.products", plan.runtimeProducts);
        output.append("cycle.trace.prefix=").append(plan.prefixes.get(0)).append('\n')
                .append("cycle.signature.prefix=").append(plan.prefixes.get(1)).append('\n')
                .append("cycle.signal.prefix=").append(plan.prefixes.get(2)).append('\n');
        numbered(output, "cycle.output.contains", plan.outputContains);
        numbered(output, "cycle.signal.contains", plan.signalContains);
        numbered(output, "cycle.signal.excludes", plan.signalExcludes);
        numbered(output, "cycle.trace.contains", plan.traceContains);
        numbered(output, "cycle.trace.excludes", plan.traceExcludes);
        Files.writeString(path, output.toString(), StandardCharsets.UTF_8);
    }

    private List<Path> cycleSources() throws Exception { try (var paths = Files.list(root.resolve("tools/smoke"))) {
        return paths.filter(path -> path.getFileName().toString().endsWith("Cycle.java"))
                .filter(path -> !path.getFileName().toString().equals("DataDrivenCycle.java"))
                .sorted().toList(); } }
    private boolean cleanIndex() throws Exception { Process process = new ProcessBuilder("git", "diff", "--cached",
            "--quiet").directory(root.toFile()).start(); int exit = process.waitFor(); return exit == 1; }
    private String relative(Path path) { return root.relativize(path).toString().replace('\\', '/'); }
    private static SmokePins.Entry requiredEntry(SmokePins pins, String id) {
        SmokePins.Entry entry = pins.entry(id);
        require(entry != null, "missing unchanged pin: " + id); return entry; }
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
        require(values.size() == 1, "unsupported " + label + ": " + source); return values.get(0); }
    private static List<String> matches(Pattern pattern, String text) {
        List<String> values = new ArrayList<>(); Matcher matcher = pattern.matcher(text);
        while (matcher.find()) values.add(matcher.group(1)); return values; }
    private static List<String> assertions(String text, String field, boolean excluded) {
        List<String> values = new ArrayList<>();
        Matcher matcher = CONTAINS.matcher(text); while (matcher.find())
            if (matcher.group(2).equals(field) && matcher.group(1).equals(excluded ? "!" : ""))
                values.add(matcher.group(3)); return unique(values); }
    private static List<String> unique(List<String> values) { return List.copyOf(new LinkedHashSet<>(values)); }
    private static void row(StringBuilder output, String key, List<String> values) {
        output.append(key).append('=').append(String.join(",", values)).append('\n'); }
    private static void numbered(StringBuilder output, String key, List<String> values) { output.append(key)
            .append(".count=").append(values.size()).append('\n'); for (int index = 0; index < values.size(); index++)
        output.append(key).append('.').append(index + 1).append('=').append(values.get(index)).append('\n'); }
    private static String digest(String value) throws Exception { return HexFormat.of().formatHex(
            MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8))); }
    private static String digest(Path path) throws Exception { return HexFormat.of().formatHex(
            MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(path))); }
    private static Properties load(Path path) throws Exception { Properties values = new Properties();
        try (var reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            values.load(reader); } return values; }
    private static void store(Path path, Properties values) throws Exception {
        List<String> keys = values.stringPropertyNames().stream()
                .sorted(Comparator.naturalOrder()).toList();
        StringBuilder output = new StringBuilder("# Worldline data-driven cycle migration v1\n");
        for (String key : keys) output.append(key).append('=').append(values.getProperty(key)).append('\n');
        Files.writeString(path, output.toString(), StandardCharsets.UTF_8); }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message); }
    private record Migration(Path source, String text, Plan plan, String priorFingerprint, SmokePins.Entry pin) { }
    private record FixtureRefactor(String path, String prior, String current) { }
    private record Plan(String id, String main, List<String> arguments, List<String> inputs,
            List<String> compileProducts, List<String> runtimeProducts, List<String> prefixes,
            List<String> outputContains, List<String> signalContains, List<String> signalExcludes,
            List<String> traceContains, List<String> traceExcludes) { }
}
