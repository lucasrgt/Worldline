import java.io.ByteArrayOutputStream;
import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/** Seals one token-equivalent formatting rewrite and transports its smoke proofs. */
final class FormattingPinMigration {
    private final Path root;
    private FormattingPinMigration(Path root) { this.root = root.toAbsolutePath().normalize(); }
    public static void main(String[] arguments) {
        try {
            require(List.of(arguments).equals(List.of("--apply")),
                    "usage: FormattingPinMigration --apply");
            new FormattingPinMigration(Path.of("")).apply();
        } catch (Exception error) {
            System.err.println("formatting pin migration failed: " + error.getMessage());
            System.exit(1);
        }
    }

    private void apply() throws Exception {
        Path manifestPath = root.resolve("smokes/formatting-migration.lock");
        if (Files.isRegularFile(manifestPath)) {
            Properties existing = load(manifestPath);
            if ("1".equals(existing.getProperty("schema"))) {
                refresh(manifestPath, existing); return;
            }
        }
        List<String> files = changedJava();
        require(!files.isEmpty(), "formatting migration found no changed Java files");
        Properties manifest = new Properties(); manifest.setProperty("schema", "1");
        manifest.setProperty("formatter.name", "clang-format");
        manifest.setProperty("formatter.version", "22.1.0");
        manifest.setProperty("formatter.sha256",
                "9cb6986411e434da20d56162c2bb1ed4556d0f07f7f6c0bff2d5274dd1cbc863");
        manifest.setProperty("file.count", Integer.toString(files.size()));
        Map<String, String> priors = priorSources(files);
        int index = 0; List<String> divergent = new ArrayList<>();
        for (String relative : files) {
            String prior = priors.get(relative);
            String current = Files.readString(root.resolve(relative), StandardCharsets.UTF_8);
            String priorTokens = tokens(prior), currentTokens = tokens(current);
            if (!priorTokens.equals(currentTokens)) divergent.add(relative);
            String stem = "file." + index++ + ".";
            manifest.setProperty(stem + "path", relative);
            manifest.setProperty(stem + "prior_sha256", digest(prior));
            manifest.setProperty(stem + "current_sha256", digest(current));
            manifest.setProperty(stem + "token_sha256", digest(currentTokens));
        }
        require(divergent.isEmpty(), "formatter changed Java tokens:\n"
                + String.join("\n", divergent));
        SmokePins existing = new SmokePins(root); existing.validateEvidence();
        SmokeInputFingerprint fingerprints = new SmokeInputFingerprint(root);
        List<SmokePins.Entry> pins = new ArrayList<>(); int changed = 0;
        for (SmokeDiscovery.Entry smoke : SmokeDiscovery.discover(root)) {
            SmokePins.Entry prior = existing.entry(smoke.id);
            require(prior != null, "formatting migration lacks proof: " + smoke.id);
            String current = fingerprints.compute(smoke), stem = "smoke." + smoke.id + ".";
            if (!current.equals(prior.fingerprint())) changed++;
            pins.add(current.equals(prior.fingerprint()) ? prior : new SmokePins.Entry(
                    smoke.id, current, prior.evidence(), "refactor-equivalent"));
            manifest.setProperty(stem + "prior_fingerprint", prior.fingerprint());
            manifest.setProperty(stem + "current_fingerprint", current);
            manifest.setProperty(stem + "evidence_sha256", prior.evidence());
        }
        require(pins.size() == 525 && changed >= 1, "formatting smoke census drift");
        manifest.setProperty("smoke.count", Integer.toString(pins.size()));
        manifest.setProperty("smoke.changed", Integer.toString(changed));
        existing.write(pins); store(manifestPath, manifest);
        System.out.println("formatting proofs migrated: " + files.size() + " files; "
                + changed + " changed smoke inputs");
    }

    private void refresh(Path path, Properties manifest) throws Exception {
        Properties data = load(root.resolve("smokes/data-driven-migration.lock"));
        int fixtureRefactors = Integer.parseInt(data.getProperty("refresh.fixture.count", "0"));
        require(fixtureRefactors >= 1 && fixtureRefactors <= 16,
                "formatting refresh requires the canonical fixture-refactor attestation");
        SmokePins pins = new SmokePins(root); SmokeInputFingerprint fingerprints =
                new SmokeInputFingerprint(root); Properties providers =
                ProviderDiscoveryPinCheck.manifest(root); int carried = 0, introduced = 0;
        for (SmokeDiscovery.Entry smoke : SmokeDiscovery.discover(root)) {
            if (ProviderDiscoveryPinCheck.exemptsLegacy(providers, smoke.id)) continue;
            carried++; String current = fingerprints.compute(smoke);
            SmokePins.Entry pin = pins.match(smoke.id, current);
            require(pin != null, "formatting refresh lacks current proof: " + smoke.id);
            String stem = "smoke." + smoke.id + ".";
            String recorded = manifest.getProperty(stem + "current_fingerprint");
            if (recorded == null) {
                require("executed".equals(pin.source()),
                        "new formatting row lacks exact execution: " + smoke.id);
                manifest.setProperty(stem + "introduced", "true"); introduced++;
            } else if (!current.equals(recorded))
                manifest.setProperty(stem + "prior_fingerprint", recorded);
            manifest.setProperty(stem + "current_fingerprint", current);
            manifest.setProperty(stem + "evidence_sha256", pin.evidence());
        }
        Properties schemas = SchemaPinCheck.manifest(root);
        int successorIntroductions = SchemaPinCheck.introductionsAfter(schemas, manifest);
        int smokeCount = carried + ProviderDiscoveryPinCheck.pendingCount(providers)
                - successorIntroductions;
        require(smokeCount >= 0, "formatting successor introduction census drift");
        manifest.setProperty("smoke.count", Integer.toString(smokeCount));
        pins.write(pins.entries()); store(path, manifest);
        System.out.println("formatting pins refreshed: " + carried + " carried, "
                + introduced + " introduced");
    }

    private List<String> changedJava() throws Exception {
        String output = git("diff", "--name-only", "--diff-filter=M", "HEAD", "--",
                "tools/smoke", "smokes"); List<String> result = new ArrayList<>();
        for (String line : output.split("\\R")) if (line.endsWith(".java")) result.add(line);
        return result.stream().sorted().toList();
    }
    private String git(String... arguments) throws Exception {
        List<String> command = new ArrayList<>(List.of("git")); command.addAll(List.of(arguments));
        Process process = new ProcessBuilder(command).directory(root.toFile())
                .redirectError(ProcessBuilder.Redirect.DISCARD).start();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        process.getInputStream().transferTo(output);
        require(process.waitFor() == 0, "git command failed: " + String.join(" ", command));
        return output.toString(StandardCharsets.UTF_8);
    }
    private Map<String, String> priorSources(List<String> files) throws Exception {
        Process process = new ProcessBuilder("git", "cat-file", "--batch")
                .directory(root.toFile()).start();
        Map<String, String> result = new HashMap<>();
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                process.getOutputStream(), StandardCharsets.UTF_8));
                BufferedInputStream input = new BufferedInputStream(process.getInputStream())) {
            for (String relative : files) {
                writer.write("HEAD:" + relative); writer.newLine(); writer.flush();
                String header = line(input); String[] fields = header.split(" ");
                require(fields.length == 3 && fields[1].equals("blob"),
                        "missing prior formatting blob: " + relative);
                int size = Integer.parseInt(fields[2]); byte[] bytes = input.readNBytes(size);
                require(bytes.length == size && input.read() == '\n',
                        "truncated prior formatting blob: " + relative);
                result.put(relative, new String(bytes, StandardCharsets.UTF_8));
            }
        }
        require(process.waitFor() == 0, "git cat-file failed"); return result;
    }
    private static String line(BufferedInputStream input) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream(); int value;
        while ((value = input.read()) >= 0 && value != '\n') output.write(value);
        require(value == '\n', "truncated git cat-file header");
        return output.toString(StandardCharsets.UTF_8);
    }
    static String tokens(String source) {
        StringBuilder result = new StringBuilder(); boolean literal = false, character = false;
        boolean lineComment = false, blockComment = false, escaped = false;
        for (int index = 0; index < source.length(); index++) {
            char value = source.charAt(index), next = index + 1 < source.length()
                    ? source.charAt(index + 1) : 0;
            if (lineComment) { result.append(value); if (value == '\n') lineComment = false; continue; }
            if (blockComment) { result.append(value); if (value == '*' && next == '/') {
                result.append(next); index++; blockComment = false; } continue; }
            if (literal || character) { result.append(value); if (escaped) escaped = false;
                else if (value == '\\') escaped = true; else if (literal && value == '"') literal = false;
                else if (character && value == '\'') character = false; continue; }
            if (value == '/' && next == '/') { result.append("//"); index++; lineComment = true; }
            else if (value == '/' && next == '*') { result.append("/*"); index++; blockComment = true; }
            else if (value == '"') { result.append(value); literal = true; }
            else if (value == '\'') { result.append(value); character = true; }
            else if (!Character.isWhitespace(value)) result.append(value);
        }
        return result.toString();
    }
    private static String digest(String value) throws Exception { return HexFormat.of().formatHex(
            MessageDigest.getInstance("SHA-256").digest(value.replace("\r\n", "\n")
                    .getBytes(StandardCharsets.UTF_8))); }
    private static void store(Path path, Properties values) throws Exception {
        StringBuilder output = new StringBuilder("# Worldline token-equivalent formatting migration v1\n");
        for (String key : values.stringPropertyNames().stream().sorted(Comparator.naturalOrder()).toList())
            output.append(key).append('=').append(values.getProperty(key)).append('\n');
        Files.writeString(path, output.toString(), StandardCharsets.UTF_8);
    }
    private static Properties load(Path path) throws Exception { Properties values = new Properties();
        try (var reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            values.load(reader); } return values;
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
