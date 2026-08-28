import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

/** Validates the additive lifecycle API proof transport and its exact support anchors. */
final class NeighborTestKitPinCheck {
    private NeighborTestKitPinCheck() { }
    static void execute(Path root) throws Exception {
        Path path = root.resolve("smokes/neighbor-testkit-migration.lock");
        NeighborTestKitPinMigration.require(Files.isRegularFile(path),
                "missing neighbor TestKit migration lock");
        Properties lock = NeighborTestKitPinMigration.load(path);
        NeighborTestKitPinMigration.require("1".equals(lock.getProperty("schema"))
                && NeighborTestKitPinMigration.INTRODUCTION.equals(
                        lock.getProperty("introduction.commit"))
                && NeighborTestKitPinMigration.BASE.equals(lock.getProperty("base.commit")),
                "neighbor TestKit migration identity drift");
        int files = integer(lock, "file.count");
        NeighborTestKitPinMigration.require(files == NeighborTestKitPinMigration.FILES.size(),
                "neighbor TestKit source census drift");
        for (int index = 0; index < files; index++) verifyFile(root, lock, index);
        SmokePins pins = new SmokePins(root); pins.validateEvidence();
        SmokeInputFingerprint fingerprints = new SmokeInputFingerprint(root);
        List<SmokeDiscovery.Entry> catalog = SmokeDiscovery.discover(root);
        Properties train = TrainPinCheck.manifest(root);
        int anchors = integer(lock, "anchor.count");
        NeighborTestKitPinMigration.require(anchors == NeighborTestKitPinMigration.ANCHORS.size(),
                "neighbor TestKit anchor census drift");
        for (int index = 0; index < anchors; index++) {
            String stem = "anchor." + index + ".", id = required(lock, stem + "id");
            SmokeDiscovery.Entry smoke = requireSmoke(catalog, id); String current = fingerprints.compute(smoke);
            SmokePins.Entry pin = pins.match(id, current);
            String prior = required(lock, stem + "fingerprint");
            String evidence = required(lock, stem + "evidence_sha256");
            boolean direct = pin != null && pin.source().equals("executed")
                    && current.equals(prior) && pin.evidence().equals(evidence);
            boolean successor = pin != null && pin.source().equals("refactor-equivalent")
                    && pin.evidence().equals(evidence)
                    && SupportFaceTestKitPinCheck.transportsSmoke(
                            root, id, prior, evidence, current);
            successor |= pin != null && TrainPinCheck.carriesCurrent(
                    train, id, pin, current);
            NeighborTestKitPinMigration.require(direct || successor,
                    "neighbor TestKit exact anchor drift: " + id);
        }
        int carried = integer(lock, "carried.count");
        NeighborTestKitPinMigration.require(carried == 41, "neighbor TestKit carried census drift");
        for (int index = 0; index < carried; index++) {
            String stem = "smoke." + index + ".", id = required(lock, stem + "id");
            SmokeDiscovery.Entry smoke = requireSmoke(catalog, id); String current = fingerprints.compute(smoke);
            SmokePins.Entry pin = pins.match(id, current);
            String prior = required(lock, stem + "current_fingerprint");
            String evidence = required(lock, stem + "evidence_sha256");
            boolean transported = pin != null && pin.evidence().equals(evidence)
                    && (current.equals(prior) || SupportFaceTestKitPinCheck.transportsSmoke(
                            root, id, prior, evidence, current));
            boolean exactSuccessor = pin != null
                    && TrainPinCheck.carriesCurrent(train, id, pin, current);
            boolean schemaSuccessor = pin != null && pin.evidence().equals(evidence)
                    && SchemaPinCheck.carries(SchemaPinCheck.manifest(root), id, pin, current);
            boolean dataDrivenSuccessor = pin != null && pin.evidence().equals(evidence)
                    && DataDrivenCycleCheck.carriesPlan(root, id, pin);
            boolean executedSuccessor = reexecuted(pin);
            NeighborTestKitPinMigration.require((transported || exactSuccessor || schemaSuccessor
                            || dataDrivenSuccessor || executedSuccessor)
                    && required(lock, stem + "prior_fingerprint").matches("[0-9a-f]{64}"),
                    "neighbor TestKit carried proof drift: " + id);
        }
        System.out.println("  neighbor-aware TestKit migration: " + carried
                + " carried proofs, " + anchors + " exact anchors");
    }

    private static void verifyFile(Path root, Properties lock, int index) throws Exception {
        String stem = "file." + index + ".", relative = required(lock, stem + "path");
        NeighborTestKitPinMigration.require(relative.equals(NeighborTestKitPinMigration.FILES.get(index)),
                "neighbor TestKit source order drift");
        byte[] current = Files.readAllBytes(root.resolve(relative));
        String expectedCurrent = required(lock, stem + "current_sha256");
        NeighborTestKitPinMigration.require(NeighborTestKitPinMigration.digest(current)
                .equals(expectedCurrent)
                || SupportFaceTestKitPinCheck.transportsFile(root, relative, expectedCurrent)
                || BoundedDropTestKitPinCheck.transportsFile(root, relative, expectedCurrent),
                "neighbor TestKit current source drift: " + relative);
        byte[] prior = gitShow(root, NeighborTestKitPinMigration.BASE + ":" + relative);
        String expected = required(lock, stem + "prior_sha256");
        NeighborTestKitPinMigration.require(prior == null ? expected.equals("absent")
                : NeighborTestKitPinMigration.digest(prior).equals(expected),
                "neighbor TestKit prior source drift: " + relative);
    }
    private static byte[] gitShow(Path root, String object) throws Exception {
        Process process = new ProcessBuilder("git", "show", object).directory(root.toFile())
                .redirectError(ProcessBuilder.Redirect.DISCARD).start();
        byte[] bytes = process.getInputStream().readAllBytes(); return process.waitFor() == 0 ? bytes : null;
    }
    private static SmokeDiscovery.Entry requireSmoke(List<SmokeDiscovery.Entry> catalog, String id) {
        return catalog.stream().filter(row -> row.id.equals(id)).findFirst()
                .orElseThrow(() -> new IllegalStateException("missing neighbor TestKit smoke: " + id));
    }
    static boolean reexecuted(SmokePins.Entry pin) {
        return pin != null && pin.source().equals("executed");
    }
    static void selfTest() {
        String hash = "0".repeat(64);
        NeighborTestKitPinMigration.require(reexecuted(new SmokePins.Entry(
                        "fresh", hash, hash, hash, "executed")),
                "current executed proof was not accepted as a successor");
        NeighborTestKitPinMigration.require(!reexecuted(new SmokePins.Entry(
                        "carried", hash, hash, hash, "refactor-equivalent")),
                "refactor-equivalent proof bypassed migration transport");
        NeighborTestKitPinMigration.require(!reexecuted(null),
                "missing proof was accepted as a successor");
    }
    private static int integer(Properties values, String key) {
        try { return Integer.parseInt(required(values, key)); }
        catch (NumberFormatException error) { throw new IllegalStateException("invalid " + key); }
    }
    private static String required(Properties values, String key) {
        String value = values.getProperty(key); NeighborTestKitPinMigration.require(
                value != null && !value.isBlank(), "missing " + key); return value; }
}
