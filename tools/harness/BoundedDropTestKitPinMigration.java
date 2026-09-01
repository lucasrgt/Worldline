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

/** Carries TestKit proofs across the additive bounded drop-matrix extension. */
final class BoundedDropTestKitPinMigration {
    static final String INTRODUCTION = "27b8a16d6e748ba5a0f63723ebf16f61b8a78845";
    static final String BASE = "a0462e34967a8ea43c951a9b501a7e1b8bde4250";
    static final String NEW_ID = "b173-multi-drop-mineral-lifecycle-cycle";
    static final int BASE_PINS = 625;
    static final List<String> FILES = List.of(
            "adapters/b173-server/src/testkit/java/worldline/b173server/B173LifecycleScenarioFactory.java",
            "modules/testapi/src/main/java/worldline/testkit/BlockLifecycleDropMatrix.java",
            "modules/testapi/src/main/java/worldline/testkit/BlockLifecycleEvidence.java",
            "modules/testapi/src/main/java/worldline/testkit/BlockLifecycleFixture.java",
            "modules/testapi/src/main/java/worldline/testkit/BlockLifecycleScenario.java",
            "modules/testkit/src/main/java/worldline/testkit/BlockLifecycleFamilyEvidence.java",
            "modules/testkit/src/test/java/worldline/testkit/BlockLifecycleDropMatrixTest.java",
            "modules/testkit/src/test/java/worldline/testkit/TestKitContractTest.java");
    static final List<String> CARRIED = List.of(
            "controlled-client-tick", "testkit-cycle", "m7-mod-loading",
            "m8-mod-version-diff", "m9-scenario-minimization",
            "m70-aero-combat-window", "m71-paired-aero-window",
            "m147-piston-push-limit", "m324-furnace-rest-smelts",
            "m338-furnace-fuel-set", "m370-remaining-furnace-smelts",
            "m563-nether-exit-create-set", "m618-wolf-tame-set",
            "m620-stationapi-testkit-driver", "m626-dungeon-generation-census",
            "m627-chunk-unload-reload", "m628-minecart-booster-bug",
            "m629-door-sound-event", "m630-server-acl-matrix",
            "m631-protocol14-edge-packets", "m632-map-data-content",
            "m635-natural-slime-spawn", "m636-bonemeal-wheat", "m637-tnt-chain",
            "m638-creeper-tnt-differential", "m643-multiplayer-sleep-quorum",
            "m645-note-pitch-ladder", "m649-chest-access-constraints",
            "m651-portal-invalid-frame", "m652-portal-reentry-cooldown",
            "m653-chunk-restart-persistence", "m655-rain-stop-event",
            "m656-server-admission-matrix",
            "b173-cardinal-placement-state-domain-cycle",
            "b173-deterministic-harvest-lifecycle-cycle",
            "b173-dirt-flora-lifecycle-cycle",
            "b173-floor-mounted-lifecycle-cycle",
            "b173-furnace-state-domain-cycle",
            "b173-gold-shovel-harvest-lifecycle-cycle",
            "b173-harvestable-vegetation-lifecycle-cycle",
            "b173-lifecycle-provider-cycle",
            "b173-redstone-device-lifecycle-cycle",
            "b173-shaded-mushroom-lifecycle-cycle",
            "b173-slab-lifecycle-cycle",
            "b173-static-collision-envelope-cycle",
            "b173-static-light-transport-cycle",
            "b173-static-self-drop-lifecycle-cycle",
            "b173-support-dependent-plant-lifecycle-cycle",
            "b173-support-face-attachment-lifecycle-cycle",
            "b173-wall-attachment-state-domain-cycle",
            "b173-wooden-door-state-domain-cycle",
            "gui-tree", "seed-atlas");
    private final Path root = Path.of("").toAbsolutePath().normalize();

    public static void main(String[] arguments) {
        try {
            require(List.of(arguments).equals(List.of("--apply")),
                    "usage: BoundedDropTestKitPinMigration --apply");
            new BoundedDropTestKitPinMigration().apply();
        } catch (Exception error) {
            System.err.println("bounded-drop TestKit pin migration failed: " + error.getMessage());
            System.exit(1);
        }
    }

    private void apply() throws Exception {
        require(clean(), "bounded-drop TestKit migration requires a clean committed tree");
        require(status("merge-base", "--is-ancestor", INTRODUCTION, "HEAD") == 0,
                "bounded-drop TestKit introduction is not an ancestor of HEAD");
        Path migration = root.resolve("smokes/bounded-drop-testkit-migration.lock");
        Properties previous = Files.isRegularFile(migration)
                ? NeighborTestKitPinMigration.load(migration) : new Properties();
        require("1".equals(previous.getProperty("schema")),
                "bounded-drop historical migration is absent");
        require(Integer.toString(CARRIED.size()).equals(previous.getProperty("carried.count")),
                "bounded-drop historical carried census drift");
        List<SmokeDiscovery.Entry> catalog = SmokeDiscovery.discover(root);
        SmokeInputFingerprint fingerprints = new SmokeInputFingerprint(root);
        SmokePins currentPins = new SmokePins(root);
        SmokeDiscovery.Entry added = smoke(catalog, NEW_ID);
        SmokePins.Entry exact = currentPins.verifiedMatch(NEW_ID, fingerprints.compute(added));
        require(exact != null && exact.source().equals("executed"),
                "multi-drop lifecycle lacks exact official proof");

        byte[] baselineBytes = committed(BASE, "smokes/qualification.lock");
        require(baselineBytes != null, "missing bounded-drop baseline qualification lock");
        Properties lock = new Properties();
        lock.setProperty("schema", "1"); lock.setProperty("introduction.commit", INTRODUCTION);
        lock.setProperty("base.commit", BASE);
        lock.setProperty("base.pin_count", Integer.toString(BASE_PINS));
        lock.setProperty("base.qualification_sha256", digest(baselineBytes));
        for (int index = 0; index < FILES.size(); index++) attest(lock, index, FILES.get(index));
        lock.setProperty("file.count", Integer.toString(FILES.size()));
        for (int index = 0; index < CARRIED.size(); index++) {
            String id = CARRIED.get(index), stem = "smoke." + index + ".";
            require(id.equals(previous.getProperty(stem + "id")),
                    "bounded-drop historical carried order drift: " + id);
            SmokeDiscovery.Entry item = smoke(catalog, id);
            String fingerprint = fingerprints.compute(item);
            SmokePins.Entry pin = currentPins.match(id, fingerprint);
            require(pin != null, "bounded-drop carried smoke lacks current proof: " + id);
            String prior = previous.getProperty(stem + "prior_fingerprint", "");
            require(prior.matches("[0-9a-f]{64}"),
                    "bounded-drop carried smoke lacks historical proof: " + id);
            lock.setProperty(stem + "id", id);
            lock.setProperty(stem + "prior_fingerprint", prior);
            lock.setProperty(stem + "current_fingerprint", fingerprint);
            lock.setProperty(stem + "evidence_sha256", pin.evidence());
        }
        lock.setProperty("carried.count", Integer.toString(CARRIED.size()));
        lock.setProperty("anchor.id", NEW_ID);
        lock.setProperty("anchor.fingerprint", fingerprints.compute(added));
        lock.setProperty("anchor.evidence_sha256", exact.evidence());
        store(migration, lock);
        System.out.println("bounded-drop TestKit proofs refreshed: " + CARRIED.size()
                + " historical carries, 1 exact anchor, "
                + currentPins.entries().size() + " total pins preserved");
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
        process.getInputStream().transferTo(output);
        return process.waitFor() == 0 ? output.toByteArray() : null;
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
            new StringBuilder("# Bounded-drop lifecycle TestKit pin migration v1\n");
        for (String key : values.stringPropertyNames().stream().sorted(Comparator.naturalOrder()).toList())
            text.append(key).append('=').append(values.getProperty(key)).append('\n');
        Files.writeString(path, text.toString(), StandardCharsets.UTF_8); }
    static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
