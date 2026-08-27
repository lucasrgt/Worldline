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

/** Carries proofs across the reviewed additive neighbor-aware lifecycle TestKit extension. */
final class NeighborTestKitPinMigration {
    static final String INTRODUCTION = "69cecbc26c5971ff72af511c688ae9157eb57450";
    static final String BASE = "5da0c5d70f77a4d15a86cb2913e69d5bcb0f4c4d";
    static final List<String> FILES = List.of(
            "adapters/b173-server/src/testkit/java/worldline/b173server/B173LifecycleArena.java",
            "adapters/b173-server/src/testkit/java/worldline/b173server/B173LifecycleLoadout.java",
            "adapters/b173-server/src/testkit/java/worldline/b173server/B173LifecycleScenarioFactory.java",
            "adapters/b173-server/src/testkitTest/java/worldline/b173server/B173LifecycleSupportTest.java",
            "modules/testapi/src/main/java/worldline/testkit/BlockLifecycleEvidence.java",
            "modules/testapi/src/main/java/worldline/testkit/BlockLifecycleFixture.java",
            "modules/testapi/src/main/java/worldline/testkit/BlockLifecycleNeighbor.java",
            "modules/testapi/src/main/java/worldline/testkit/BlockLifecyclePlan.java",
            "modules/testapi/src/main/java/worldline/testkit/BlockLifecycleScenario.java",
            "modules/testkit/src/main/java/worldline/testkit/BlockLifecycleFamilyEvidence.java",
            "modules/testkit/src/test/java/worldline/testkit/BlockLifecycleFixtureTest.java");
    static final List<String> ANCHORS = List.of(
            "b173-support-dependent-plant-lifecycle-cycle", "controlled-client-tick",
            "testkit-cycle");
    private final Path root = Path.of("").toAbsolutePath().normalize();

    public static void main(String[] arguments) {
        try {
            require(List.of(arguments).equals(List.of("--apply")),
                    "usage: NeighborTestKitPinMigration --apply");
            new NeighborTestKitPinMigration().apply();
        } catch (Exception error) {
            System.err.println("neighbor TestKit pin migration failed: " + error.getMessage());
            System.exit(1);
        }
    }

    private void apply() throws Exception {
        require(clean(), "neighbor TestKit migration requires a clean committed tree");
        require(status("merge-base", "--is-ancestor", INTRODUCTION, "HEAD") == 0,
                "neighbor TestKit introduction is not an ancestor of HEAD");
        SmokePins existing = new SmokePins(root);
        SmokeInputFingerprint fingerprints = new SmokeInputFingerprint(root);
        List<SmokeDiscovery.Entry> catalog = SmokeDiscovery.discover(root);
        Properties lock = new Properties();
        lock.setProperty("schema", "1");
        lock.setProperty("introduction.commit", INTRODUCTION);
        lock.setProperty("base.commit", BASE);
        lock.setProperty("file.count", Integer.toString(FILES.size()));
        for (int index = 0; index < FILES.size(); index++) attest(lock, index, FILES.get(index));
        lock.setProperty("anchor.count", Integer.toString(ANCHORS.size()));
        for (int index = 0; index < ANCHORS.size(); index++) {
            String id = ANCHORS.get(index);
            SmokeDiscovery.Entry smoke = catalog.stream().filter(row -> row.id.equals(id))
                    .findFirst().orElseThrow(() -> new IllegalStateException("missing anchor " + id));
            String current = fingerprints.compute(smoke); SmokePins.Entry pin = existing.match(id, current);
            require(pin != null && pin.source().equals("executed"),
                    "neighbor TestKit anchor lacks exact current proof: " + id);
            lock.setProperty("anchor." + index + ".id", id);
            lock.setProperty("anchor." + index + ".fingerprint", current);
            lock.setProperty("anchor." + index + ".evidence_sha256", pin.evidence());
        }
        List<SmokePins.Entry> pins = new ArrayList<>(); int carried = 0;
        for (SmokeDiscovery.Entry smoke : catalog) {
            SmokePins.Entry prior = existing.entry(smoke.id);
            require(prior != null, "neighbor TestKit migration lacks prior proof: " + smoke.id);
            String current = fingerprints.compute(smoke);
            if (current.equals(prior.fingerprint())) { pins.add(prior); continue; }
            String stem = "smoke." + carried++ + ".";
            lock.setProperty(stem + "id", smoke.id);
            lock.setProperty(stem + "prior_fingerprint", prior.fingerprint());
            lock.setProperty(stem + "current_fingerprint", current);
            lock.setProperty(stem + "evidence_sha256", prior.evidence());
            pins.add(new SmokePins.Entry(smoke.id, current, prior.evidence(), "refactor-equivalent"));
        }
        require(carried == 41, "neighbor TestKit carried smoke census drift: " + carried);
        lock.setProperty("carried.count", Integer.toString(carried));
        existing.write(pins); store(root.resolve("smokes/neighbor-testkit-migration.lock"), lock);
        System.out.println("neighbor-aware TestKit proofs migrated: " + carried
                + " carried, " + ANCHORS.size() + " exact anchors");
    }

    private void attest(Properties lock, int index, String relative) throws Exception {
        String stem = "file." + index + "."; lock.setProperty(stem + "path", relative);
        Path current = root.resolve(relative); require(Files.isRegularFile(current),
                "missing current neighbor TestKit source: " + relative);
        lock.setProperty(stem + "current_sha256", digest(Files.readAllBytes(current)));
        byte[] prior = committed(BASE, relative);
        lock.setProperty(stem + "prior_sha256", prior == null ? "absent" : digest(prior));
    }

    private byte[] committed(String revision, String relative) throws Exception {
        Process process = new ProcessBuilder("git", "show", revision + ":" + relative)
                .directory(root.toFile()).redirectError(ProcessBuilder.Redirect.DISCARD).start();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        process.getInputStream().transferTo(output); int exit = process.waitFor();
        return exit == 0 ? output.toByteArray() : null;
    }

    private boolean clean() throws Exception {
        Process process = new ProcessBuilder("git", "status", "--porcelain", "--untracked-files=all")
                .directory(root.toFile()).redirectErrorStream(true).start();
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        return process.waitFor() == 0 && output.isBlank();
    }
    private int status(String... arguments) throws Exception {
        List<String> command = new ArrayList<>(List.of("git")); command.addAll(List.of(arguments));
        return new ProcessBuilder(command).directory(root.toFile())
                .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                .redirectError(ProcessBuilder.Redirect.DISCARD).start().waitFor();
    }
    static String digest(byte[] bytes) throws Exception { return HexFormat.of().formatHex(
            MessageDigest.getInstance("SHA-256").digest(bytes)); }
    static Properties load(Path path) throws Exception { Properties values = new Properties();
        try (var reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            values.load(reader); } return values; }
    static void store(Path path, Properties values) throws Exception {
        StringBuilder text = new StringBuilder("# Neighbor-aware lifecycle TestKit pin migration v1\n");
        for (String key : values.stringPropertyNames().stream().sorted(Comparator.naturalOrder()).toList())
            text.append(key).append('=').append(values.getProperty(key)).append('\n');
        Files.writeString(path, text.toString(), StandardCharsets.UTF_8);
    }
    static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message); }
}
