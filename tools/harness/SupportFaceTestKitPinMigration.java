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

/** Carries lifecycle proofs across the additive arbitrary-face scenario factory extension. */
final class SupportFaceTestKitPinMigration {
    static final String INTRODUCTION = "ac0f1cc7e254a68797d1e2ea2e266368e28e026e";
    static final String BASE = "20b40da396d2ba7874623fde31885c008b574105";
    static final String NEW_ID = "b173-support-face-attachment-lifecycle-cycle";
    static final int BASE_PINS = 622;
    static final List<String> FILES = List.of(
            "adapters/b173-server/src/testkit/java/worldline/b173server/B173LifecycleScenarioFactory.java",
            "adapters/b173-server/src/testkitTest/java/worldline/b173server/B173LifecycleSupportTest.java");
    static final List<String> CARRIED = List.of(
            "b173-cardinal-placement-state-domain-cycle",
            "b173-deterministic-harvest-lifecycle-cycle",
            "b173-dirt-flora-lifecycle-cycle",
            "b173-floor-mounted-lifecycle-cycle",
            "b173-furnace-state-domain-cycle",
            "b173-gold-shovel-harvest-lifecycle-cycle",
            "b173-lifecycle-provider-cycle",
            "b173-shaded-mushroom-lifecycle-cycle",
            "b173-slab-lifecycle-cycle",
            "b173-static-collision-envelope-cycle",
            "b173-static-light-transport-cycle",
            "b173-static-self-drop-lifecycle-cycle",
            "b173-support-dependent-plant-lifecycle-cycle",
            "b173-wall-attachment-state-domain-cycle",
            "b173-wooden-door-state-domain-cycle");
    static final List<String> ANCHORS = List.of(NEW_ID, "controlled-client-tick", "testkit-cycle");
    private final Path root = Path.of("").toAbsolutePath().normalize();

    public static void main(String[] arguments) {
        try {
            require(List.of(arguments).equals(List.of("--apply")),
                    "usage: SupportFaceTestKitPinMigration --apply");
            new SupportFaceTestKitPinMigration().apply();
        } catch (Exception error) {
            System.err.println("support-face TestKit pin migration failed: " + error.getMessage());
            System.exit(1);
        }
    }

    private void apply() throws Exception {
        require(clean(), "support-face TestKit migration requires a clean committed tree");
        require(status("merge-base", "--is-ancestor", INTRODUCTION, "HEAD") == 0,
                "support-face TestKit introduction is not an ancestor of HEAD");
        List<SmokeDiscovery.Entry> catalog = SmokeDiscovery.discover(root);
        SmokeInputFingerprint fingerprints = new SmokeInputFingerprint(root);
        SmokePins refreshed = new SmokePins(root);
        SmokeDiscovery.Entry added = smoke(catalog, NEW_ID);
        String addedFingerprint = fingerprints.compute(added);
        SmokePins.Entry exact = refreshed.verifiedMatch(NEW_ID, addedFingerprint);
        require(exact != null && exact.source().equals("executed"),
                "support-face lifecycle lacks exact official proof");

        byte[] baselineBytes = committed(BASE, "smokes/qualification.lock");
        require(baselineBytes != null, "missing baseline qualification lock");
        Files.write(root.resolve("smokes/qualification.lock"), baselineBytes);
        SmokePins baseline = new SmokePins(root);
        require(baseline.entries().size() == BASE_PINS, "baseline smoke pin census drift");
        List<SmokePins.Entry> pins = new ArrayList<>();
        Properties lock = new Properties();
        lock.setProperty("schema", "1");
        lock.setProperty("introduction.commit", INTRODUCTION);
        lock.setProperty("base.commit", BASE);
        lock.setProperty("base.pin_count", Integer.toString(BASE_PINS));
        lock.setProperty("base.qualification_sha256", digest(baselineBytes));
        for (int index = 0; index < FILES.size(); index++) attest(lock, index, FILES.get(index));
        lock.setProperty("file.count", Integer.toString(FILES.size()));
        int carried = 0;
        for (SmokeDiscovery.Entry item : catalog) {
            if (item.id.equals(NEW_ID)) { pins.add(exact); continue; }
            SmokePins.Entry prior = baseline.entry(item.id);
            require(prior != null, "baseline lacks smoke proof: " + item.id);
            if (!CARRIED.contains(item.id)) { pins.add(prior); continue; }
            String current = fingerprints.compute(item);
            require(!current.equals(prior.fingerprint()), "carried fingerprint did not change: " + item.id);
            String stem = "smoke." + carried++ + ".";
            lock.setProperty(stem + "id", item.id);
            lock.setProperty(stem + "prior_fingerprint", prior.fingerprint());
            lock.setProperty(stem + "current_fingerprint", current);
            lock.setProperty(stem + "evidence_sha256", prior.evidence());
            pins.add(new SmokePins.Entry(item.id, current, prior.evidence(), "refactor-equivalent"));
        }
        require(carried == CARRIED.size(), "support-face carried smoke census drift: " + carried);
        lock.setProperty("carried.count", Integer.toString(carried));
        baseline.write(pins);
        SmokePins sealed = new SmokePins(root);
        require(sealed.entries().size() == BASE_PINS + 1, "sealed smoke pin census drift");
        lock.setProperty("anchor.count", Integer.toString(ANCHORS.size()));
        for (int index = 0; index < ANCHORS.size(); index++) {
            String id = ANCHORS.get(index), current = fingerprints.compute(smoke(catalog, id));
            SmokePins.Entry pin = sealed.match(id, current);
            require(pin != null && pin.source().equals("executed"), "non-exact anchor: " + id);
            lock.setProperty("anchor." + index + ".id", id);
            lock.setProperty("anchor." + index + ".fingerprint", current);
            lock.setProperty("anchor." + index + ".evidence_sha256", pin.evidence());
        }
        store(root.resolve("smokes/support-face-testkit-migration.lock"), lock);
        System.out.println("support-face TestKit proofs migrated: " + carried
                + " carried, " + ANCHORS.size() + " exact anchors, " + pins.size() + " total");
    }

    private void attest(Properties lock, int index, String relative) throws Exception {
        String stem = "file." + index + "."; lock.setProperty(stem + "path", relative);
        lock.setProperty(stem + "current_sha256", digest(Files.readAllBytes(root.resolve(relative))));
        byte[] prior = committed(BASE, relative);
        lock.setProperty(stem + "prior_sha256", prior == null ? "absent" : digest(prior));
    }
    private byte[] committed(String revision, String relative) throws Exception {
        Process process = new ProcessBuilder("git", "show", revision + ":" + relative)
                .directory(root.toFile()).redirectError(ProcessBuilder.Redirect.DISCARD).start();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        process.getInputStream().transferTo(output); return process.waitFor() == 0 ? output.toByteArray() : null;
    }
    private static SmokeDiscovery.Entry smoke(List<SmokeDiscovery.Entry> catalog, String id) {
        return catalog.stream().filter(row -> row.id.equals(id)).findFirst()
                .orElseThrow(() -> new IllegalStateException("missing smoke: " + id));
    }
    private boolean clean() throws Exception { Process process = new ProcessBuilder("git", "status",
            "--porcelain", "--untracked-files=all").directory(root.toFile()).start();
        return process.waitFor() == 0 && new String(process.getInputStream().readAllBytes(),
                StandardCharsets.UTF_8).isBlank(); }
    private int status(String... arguments) throws Exception { List<String> command = new ArrayList<>(List.of("git"));
        command.addAll(List.of(arguments)); return new ProcessBuilder(command).directory(root.toFile())
                .redirectOutput(ProcessBuilder.Redirect.DISCARD).start().waitFor(); }
    static String digest(byte[] value) throws Exception { return HexFormat.of().formatHex(
            MessageDigest.getInstance("SHA-256").digest(value)); }
    static void store(Path path, Properties values) throws Exception { StringBuilder text =
            new StringBuilder("# Support-face lifecycle TestKit pin migration v1\n");
        for (String key : values.stringPropertyNames().stream().sorted(Comparator.naturalOrder()).toList())
            text.append(key).append('=').append(values.getProperty(key)).append('\n');
        Files.writeString(path, text.toString(), StandardCharsets.UTF_8); }
    static void require(boolean value, String message) { if (!value) throw new IllegalStateException(message); }
}
