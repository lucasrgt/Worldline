import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Properties;

/** Seals proof transport for the additive public placement and break claim fixtures. */
final class LifecycleClaimTestKitPinMigration {
    static final String INTRODUCTION = "cb4b0c6c50075355b4d19b1873cf2aff406f4483";
    static final String BASE = "71fb626814cdfacece454bb7531c4945d81de710";
    static final int BASE_PINS = 664;
    static final List<String> DIRECT = List.of(
            "m208-oak-log", "m217-mossy-cobble", "m241-iron-door-place",
            "m301-axe-log-breaks", "m308-fragile-set", "m375-remaining-pick-breaks",
            "m427-remaining-piston-orient-set", "m428-remaining-door-orient-set",
            "m431-remaining-bed-orient-set", "m434-remaining-sponge-glass-ice",
            "m554-extended-head-break-set", "m573-sticky-head-break-set",
            "m593-door-upper-break-set");
    static final List<String> ANCHORS = List.of(
            "m208-oak-log", "m217-mossy-cobble", "m427-remaining-piston-orient-set",
            "m428-remaining-door-orient-set", "m431-remaining-bed-orient-set",
            "m434-remaining-sponge-glass-ice");

    private final Path root = Path.of("").toAbsolutePath().normalize();

    public static void main(String[] arguments) {
        try {
            require(List.of(arguments).equals(List.of("--apply")),
                    "usage: LifecycleClaimTestKitPinMigration --apply");
            new LifecycleClaimTestKitPinMigration().apply();
        } catch (Exception error) {
            System.err.println("lifecycle-claim TestKit pin migration failed: "
                    + error.getMessage());
            System.exit(1);
        }
    }

    private void apply() throws Exception {
        require(clean(), "lifecycle-claim TestKit migration requires a clean committed tree");
        require(status("merge-base", "--is-ancestor", INTRODUCTION, "HEAD") == 0,
                "lifecycle-claim TestKit introduction is not an ancestor of HEAD");
        byte[] baselineBytes = committed(BASE, "smokes/qualification.lock");
        require(baselineBytes != null, "missing lifecycle-claim baseline qualification lock");
        SmokePins current = new SmokePins(root);
        require(current.entries().size() == BASE_PINS,
                "lifecycle-claim baseline pin census drift");
        SmokeInputFingerprint fingerprints = new SmokeInputFingerprint(root);
        SmokeReceiptCache cache = new SmokeReceiptCache(root);
        List<SmokePins.Entry> next = new ArrayList<>();
        Properties lock = new Properties();
        lock.setProperty("schema", "1");
        lock.setProperty("introduction.commit", INTRODUCTION);
        lock.setProperty("base.commit", BASE);
        lock.setProperty("base.pin_count", Integer.toString(BASE_PINS));
        lock.setProperty("base.qualification_sha256", digest(baselineBytes));
        List<String> files = files(root);
        for (int index = 0; index < files.size(); index++) attest(lock, index, files.get(index));
        lock.setProperty("file.count", Integer.toString(files.size()));

        Properties plans = properties(committed(BASE, "smokes/data-driven-migration.lock"));
        List<SmokeDiscovery.Entry> catalog = SmokeDiscovery.discover(root);
        int carried = 0, exact = 0;
        for (SmokeDiscovery.Entry smoke : catalog) {
            SmokePins.Entry prior = current.entry(smoke.id);
            require(prior != null, "baseline lacks lifecycle-claim smoke: " + smoke.id);
            String fingerprint = fingerprints.compute(smoke);
            if (fingerprint.equals(prior.fingerprint())) { next.add(prior); continue; }
            String stem = "smoke." + carried++ + ".";
            lock.setProperty(stem + "id", smoke.id);
            lock.setProperty(stem + "prior_fingerprint", prior.fingerprint());
            lock.setProperty(stem + "current_fingerprint", fingerprint);
            SmokePins.Entry replacement;
            if (ANCHORS.contains(smoke.id)) {
                SmokePins.Entry observed = cache.availablePin(smoke);
                require(observed != null && observed.source().equals("executed")
                                && observed.fingerprint().equals(fingerprint),
                        "lifecycle-claim exact anchor is absent: " + smoke.id);
                replacement = observed; exact++;
                lock.setProperty(stem + "source", "executed");
            } else {
                replacement = new SmokePins.Entry(smoke.id, fingerprint,
                        prior.evidence(), "refactor-equivalent");
                lock.setProperty(stem + "source", "refactor-equivalent");
            }
            lock.setProperty(stem + "evidence_sha256", replacement.evidence());
            next.add(replacement);
        }
        lock.setProperty("carried.count", Integer.toString(carried));
        lock.setProperty("anchor.count", Integer.toString(exact));
        for (int index = 0; index < DIRECT.size(); index++) {
            String id = DIRECT.get(index), stem = "direct." + index + ".";
            require(contains(lock, carried, id), "direct lifecycle smoke did not change: " + id);
            lock.setProperty(stem + "id", id);
            lock.setProperty(stem + "prior_plan_sha256",
                    required(plans, "cycle." + id + ".plan_sha256"));
            lock.setProperty(stem + "current_plan_sha256",
                    DataDrivenCyclePlan.load(root, id).fingerprint());
        }
        lock.setProperty("direct.count", Integer.toString(DIRECT.size()));
        require(exact == ANCHORS.size(), "lifecycle-claim exact anchor census drift");
        current.write(next);
        store(root.resolve("smokes/lifecycle-claim-testkit-migration.lock"), lock);
        System.out.println("lifecycle-claim TestKit proofs migrated: " + carried
                + " transported, " + exact + " exact anchors, " + next.size() + " total");
    }

    static List<String> files(Path root) throws Exception {
        return capture(root, "diff", "--name-only", BASE, INTRODUCTION).lines()
                .filter(value -> !value.isBlank()).sorted().toList();
    }

    private void attest(Properties lock, int index, String relative) throws Exception {
        String stem = "file." + index + ".";
        lock.setProperty(stem + "path", relative);
        lock.setProperty(stem + "current_sha256", digest(Files.readAllBytes(root.resolve(relative))));
        byte[] prior = committed(BASE, relative);
        lock.setProperty(stem + "prior_sha256", prior == null ? "absent" : digest(prior));
    }

    private static boolean contains(Properties lock, int count, String id) {
        for (int index = 0; index < count; index++)
            if (id.equals(lock.getProperty("smoke." + index + ".id"))) return true;
        return false;
    }

    static byte[] committed(Path root, String revision, String relative) throws Exception {
        Process process = new ProcessBuilder("git", "show", revision + ":" + relative)
                .directory(root.toFile()).redirectError(ProcessBuilder.Redirect.DISCARD).start();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        process.getInputStream().transferTo(output);
        return process.waitFor() == 0 ? output.toByteArray() : null;
    }
    private byte[] committed(String revision, String relative) throws Exception {
        return committed(root, revision, relative);
    }
    private static Properties properties(byte[] bytes) throws Exception {
        require(bytes != null, "missing historical lifecycle-claim properties");
        Properties values = new Properties();
        values.load(new ByteArrayInputStream(bytes));
        return values;
    }
    static String capture(Path root, String... arguments) throws Exception {
        List<String> command = new ArrayList<>(List.of("git"));
        command.addAll(List.of(arguments));
        Process process = new ProcessBuilder(command).directory(root.toFile())
                .redirectErrorStream(true).start();
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        require(process.waitFor() == 0, "git command failed: " + String.join(" ", command));
        return output;
    }
    private boolean clean() throws Exception {
        return capture(root, "status", "--porcelain", "--untracked-files=all").isBlank();
    }
    private int status(String... arguments) throws Exception {
        List<String> command = new ArrayList<>(List.of("git"));
        command.addAll(List.of(arguments));
        return new ProcessBuilder(command).directory(root.toFile())
                .redirectOutput(ProcessBuilder.Redirect.DISCARD).start().waitFor();
    }
    static String digest(byte[] value) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value));
    }
    static void store(Path path, Properties values) throws Exception {
        StringBuilder text = new StringBuilder(
                "# Public lifecycle-claim TestKit pin migration v1\n");
        for (String key : values.stringPropertyNames().stream()
                .sorted(Comparator.naturalOrder()).toList())
            text.append(key).append('=').append(values.getProperty(key)).append('\n');
        Files.writeString(path, text.toString(), StandardCharsets.UTF_8);
    }
    static String required(Properties values, String key) {
        String value = values.getProperty(key, "");
        require(!value.isBlank(), "missing lifecycle-claim property: " + key);
        return value;
    }
    static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
