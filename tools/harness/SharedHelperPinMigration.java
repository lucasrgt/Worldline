import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;

/** Seals reviewed shared-helper rewrites and carries existing runtime observations. */
final class SharedHelperPinMigration {
    private final Path root = Path.of("").toAbsolutePath().normalize();

    public static void main(String[] arguments) {
        try {
            require(List.of(arguments).equals(List.of("--apply")),
                    "usage: SharedHelperPinMigration --apply");
            new SharedHelperPinMigration().apply();
        } catch (Exception error) {
            System.err.println("shared-helper pin migration failed: " + error.getMessage());
            System.exit(1);
        }
    }

    private void apply() throws Exception {
        Path lockPath = root.resolve("smokes/shared-helper-migration.lock");
        if (Files.isRegularFile(lockPath)) {
            Properties existing = load(lockPath);
            if ("1".equals(existing.getProperty("schema"))) {
                refresh(lockPath, existing); return;
            }
        }
        List<String> files = changedSmokeJava();
        require(files.size() == 354, "shared-helper source census drift: " + files.size());
        Map<String, String> priors = priorSources(files);
        Properties lock = Files.isRegularFile(lockPath) ? load(lockPath) : new Properties();
        lock.setProperty("schema", "1"); lock.setProperty("file.count", Integer.toString(files.size()));
        int variants = variantCount();
        require(variants > 0 && variants < 100, "shared-helper variant census drift: " + variants);
        lock.setProperty("variant.count", Integer.toString(variants));
        int index = 0;
        for (String relative : files) {
            String prior = priors.get(relative);
            String current = Files.readString(root.resolve(relative), StandardCharsets.UTF_8);
            require(SharedFixturePatch.rewrite(prior).equals(current),
                    "non-canonical shared-helper rewrite: " + relative);
            String stem = "file." + index++ + ".";
            lock.setProperty(stem + "path", relative);
            lock.setProperty(stem + "prior_sha256", digest(prior));
            lock.setProperty(stem + "current_sha256", digest(current));
        }
        attest(lock, "fixture", "adapters/b173-server/src/main/java/worldline/b173server/B173FixtureSupport.java");
        attest(lock, "aero_parser", "tools/harness/AeroLogRow.java");
        attest(lock, "aero_test", "tools/harness/AeroLogRowTest.java");
        attestAero(lock, "combat", "tools/smoke/AeroCombatWindowCycle.java");
        attestAero(lock, "login", "tools/smoke/AeroMultiplayerLoginCycle.java");
        SmokePins existing = new SmokePins(root); existing.validateEvidence();
        SmokeInputFingerprint fingerprints = new SmokeInputFingerprint(root);
        List<SmokePins.Entry> pins = new ArrayList<>(); int changed = 0;
        for (SmokeDiscovery.Entry smoke : SmokeDiscovery.discover(root)) {
            SmokePins.Entry prior = existing.entry(smoke.id);
            require(prior != null, "shared-helper migration lacks proof: " + smoke.id);
            String current = fingerprints.compute(smoke), stem = "smoke." + smoke.id + ".";
            if (current.equals(prior.fingerprint())
                    && current.equals(lock.getProperty(stem + "current_fingerprint"))
                    && lock.getProperty(stem + "prior_fingerprint", "").matches("[0-9a-f]{64}")) {
                if (!current.equals(lock.getProperty(stem + "prior_fingerprint"))) changed++;
                pins.add(prior); continue;
            }
            if (!current.equals(prior.fingerprint())) changed++;
            pins.add(current.equals(prior.fingerprint()) ? prior : new SmokePins.Entry(
                    smoke.id, current, prior.evidence(), "refactor-equivalent"));
            lock.setProperty(stem + "prior_fingerprint", prior.fingerprint());
            lock.setProperty(stem + "current_fingerprint", current);
            lock.setProperty(stem + "evidence_sha256", prior.evidence());
        }
        require(pins.size() == 525 && changed >= 354,
                "shared-helper smoke census drift: " + pins.size() + "/" + changed);
        lock.setProperty("smoke.count", Integer.toString(pins.size()));
        lock.setProperty("smoke.changed", Integer.toString(changed));
        existing.write(pins); store(lockPath, lock);
        System.out.println("shared-helper proofs migrated: 354 files; " + changed + " smoke inputs");
    }

    private void refresh(Path path, Properties lock) throws Exception {
        Properties data = load(root.resolve("smokes/data-driven-migration.lock"));
        int refactors = Integer.parseInt(data.getProperty("refresh.fixture.count", "0"));
        require(refactors >= 1 && refactors <= 16, "shared-helper refresh census drift");
        lock.setProperty("refresh.count", Integer.toString(refactors));
        for (int index = 1; index <= refactors; index++) {
            String source = "refresh.fixture." + index + ".", target = "refresh." + index + ".";
            for (String key : List.of("id", "path", "prior_sha256", "current_sha256"))
                lock.setProperty(target + key, required(data, source + key));
            require(digest(Files.readString(root.resolve(required(data, source + "path")),
                    StandardCharsets.UTF_8)).equals(required(data, source + "current_sha256")),
                    "shared-helper refreshed source drift");
        }
        lock.setProperty("variant.count", Integer.toString(variantCount()));
        SmokePins pins = new SmokePins(root); SmokeInputFingerprint fingerprints =
                new SmokeInputFingerprint(root); Properties providers =
                ProviderDiscoveryPinCheck.manifest(root); int carried = 0, introduced = 0;
        for (SmokeDiscovery.Entry smoke : SmokeDiscovery.discover(root)) {
            if (ProviderDiscoveryPinCheck.exemptsLegacy(providers, smoke.id)) continue;
            carried++; String current = fingerprints.compute(smoke); SmokePins.Entry pin =
                    pins.match(smoke.id, current); require(pin != null,
                    "shared-helper refresh lacks current proof: " + smoke.id);
            String stem = "smoke." + smoke.id + ".";
            String recorded = lock.getProperty(stem + "current_fingerprint");
            if (recorded == null) {
                require("executed".equals(pin.source()),
                        "new shared-helper row lacks exact execution: " + smoke.id);
                lock.setProperty(stem + "introduced", "true"); introduced++;
            } else if (!current.equals(recorded))
                lock.setProperty(stem + "prior_fingerprint", recorded);
            lock.setProperty(stem + "current_fingerprint", current);
            lock.setProperty(stem + "evidence_sha256", pin.evidence());
        }
        int smokeCount = Integer.parseInt(lock.getProperty("smoke.count")) + introduced;
        require(carried == smokeCount - ProviderDiscoveryPinCheck.pendingCount(providers),
                "shared-helper refresh smoke census drift");
        lock.setProperty("smoke.count", Integer.toString(smokeCount));
        pins.write(pins.entries()); store(path, lock);
        System.out.println("shared-helper pins refreshed: " + refactors
                + " canonical refactors, " + carried + " carried, " + introduced
                + " introduced");
    }

    private int variantCount() throws Exception {
        java.util.regex.Pattern family = java.util.regex.Pattern.compile(
                "(?m)^  private static (?:void awaitPlayers|int local|boolean water|String sha|"
                + "BlockPosition place)\\(");
        int variants = 0;
        try (var paths = Files.walk(root.resolve("smokes"))) {
            for (Path path : paths.filter(item -> item.toString().endsWith(".java")).toList()) {
                String source = Files.readString(path);
                require(SharedFixturePatch.rewrite(source).equals(source),
                        "canonical shared-helper clone remains: " + path);
                var matcher = family.matcher(source); while (matcher.find()) variants++;
            }
        }
        return variants;
    }

    private List<String> changedSmokeJava() throws Exception {
        String output = git("diff", "--name-only", "--diff-filter=M", "HEAD", "--", "smokes");
        return output.lines().filter(row -> row.endsWith(".java")).sorted().toList();
    }
    private void attest(Properties lock, String key, String relative) throws Exception {
        lock.setProperty(key + ".path", relative);
        lock.setProperty(key + ".current_sha256", digest(Files.readString(
                root.resolve(relative), StandardCharsets.UTF_8)));
    }
    private void attestAero(Properties lock, String key, String relative) throws Exception {
        String prior = git("show", "HEAD:" + relative);
        String current = Files.readString(root.resolve(relative), StandardCharsets.UTF_8);
        require(prior.contains("void parseAero(String row)") && !current.contains("void parseAero(")
                        && current.contains("AeroLogRow.parse(row)"),
                "invalid Aero parser consolidation: " + relative);
        lock.setProperty(key + ".path", relative);
        lock.setProperty(key + ".prior_sha256", digest(prior));
        lock.setProperty(key + ".current_sha256", digest(current));
    }
    private String git(String... arguments) throws Exception {
        List<String> command = new ArrayList<>(List.of("git")); command.addAll(List.of(arguments));
        Process process = new ProcessBuilder(command).directory(root.toFile())
                .redirectError(ProcessBuilder.Redirect.DISCARD).start();
        ByteArrayOutputStream output = new ByteArrayOutputStream(); process.getInputStream().transferTo(output);
        require(process.waitFor() == 0, "git command failed: " + String.join(" ", command));
        return output.toString(StandardCharsets.UTF_8);
    }
    private Map<String, String> priorSources(List<String> files) throws Exception {
        Process process = new ProcessBuilder("git", "cat-file", "--batch").directory(root.toFile()).start();
        Map<String, String> result = new HashMap<>();
        try (BufferedWriter writer = process.outputWriter(StandardCharsets.UTF_8);
                BufferedInputStream input = new BufferedInputStream(process.getInputStream())) {
            for (String relative : files) {
                writer.write("HEAD:" + relative); writer.newLine(); writer.flush();
                String[] header = line(input).split(" "); require(header.length == 3
                        && header[1].equals("blob"), "missing prior blob: " + relative);
                int size = Integer.parseInt(header[2]); byte[] bytes = input.readNBytes(size);
                require(bytes.length == size && input.read() == '\n', "truncated prior blob: " + relative);
                result.put(relative, new String(bytes, StandardCharsets.UTF_8));
            }
        }
        require(process.waitFor() == 0, "git cat-file failed"); return result;
    }
    private static String line(BufferedInputStream input) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream(); int value;
        while ((value = input.read()) >= 0 && value != '\n') output.write(value);
        require(value == '\n', "truncated git header"); return output.toString(StandardCharsets.UTF_8);
    }
    private static String digest(String value) throws Exception { return HexFormat.of().formatHex(
            MessageDigest.getInstance("SHA-256").digest(value.replace("\r\n", "\n")
                    .getBytes(StandardCharsets.UTF_8))); }
    private static Properties load(Path path) throws Exception {
        Properties values = new Properties();
        try (var reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            values.load(reader);
        }
        return values;
    }
    private static void store(Path path, Properties values) throws Exception {
        StringBuilder output = new StringBuilder("# Worldline shared-helper proof migration v1\n");
        for (String key : values.stringPropertyNames().stream().sorted().toList())
            output.append(key).append('=').append(values.getProperty(key)).append('\n');
        Files.writeString(path, output.toString(), StandardCharsets.UTF_8);
    }
    private static String required(Properties values, String key) {
        String value = values.getProperty(key); require(value != null && !value.isBlank(),
                "missing " + key); return value;
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
