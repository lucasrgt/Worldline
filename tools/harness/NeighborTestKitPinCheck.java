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
        SmokePins pins = new SmokePins(root); SmokeInputFingerprint fingerprints =
                new SmokeInputFingerprint(root); List<SmokeDiscovery.Entry> catalog =
                SmokeDiscovery.discover(root);
        int anchors = integer(lock, "anchor.count");
        NeighborTestKitPinMigration.require(anchors == NeighborTestKitPinMigration.ANCHORS.size(),
                "neighbor TestKit anchor census drift");
        for (int index = 0; index < anchors; index++) {
            String stem = "anchor." + index + ".", id = required(lock, stem + "id");
            SmokeDiscovery.Entry smoke = requireSmoke(catalog, id); String current = fingerprints.compute(smoke);
            SmokePins.Entry pin = pins.match(id, current);
            NeighborTestKitPinMigration.require(pin != null && pin.source().equals("executed")
                    && current.equals(required(lock, stem + "fingerprint"))
                    && pin.evidence().equals(required(lock, stem + "evidence_sha256")),
                    "neighbor TestKit exact anchor drift: " + id);
        }
        int carried = integer(lock, "carried.count");
        NeighborTestKitPinMigration.require(carried == 41, "neighbor TestKit carried census drift");
        for (int index = 0; index < carried; index++) {
            String stem = "smoke." + index + ".", id = required(lock, stem + "id");
            SmokeDiscovery.Entry smoke = requireSmoke(catalog, id); String current = fingerprints.compute(smoke);
            SmokePins.Entry pin = pins.match(id, current);
            NeighborTestKitPinMigration.require(pin != null
                    && current.equals(required(lock, stem + "current_fingerprint"))
                    && pin.evidence().equals(required(lock, stem + "evidence_sha256"))
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
        NeighborTestKitPinMigration.require(NeighborTestKitPinMigration.digest(current)
                .equals(required(lock, stem + "current_sha256")),
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
    private static int integer(Properties values, String key) {
        try { return Integer.parseInt(required(values, key)); }
        catch (NumberFormatException error) { throw new IllegalStateException("invalid " + key); }
    }
    private static String required(Properties values, String key) {
        String value = values.getProperty(key); NeighborTestKitPinMigration.require(
                value != null && !value.isBlank(), "missing " + key); return value; }
}
