import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

/** Validates bounded-drop TestKit proof transport and its official lifecycle anchor. */
final class BoundedDropTestKitPinCheck {
    private BoundedDropTestKitPinCheck() { }

    static void execute(Path root) throws Exception {
        Path path = root.resolve("smokes/bounded-drop-testkit-migration.lock");
        require(Files.isRegularFile(path), "missing bounded-drop TestKit migration lock");
        Properties lock = NeighborTestKitPinMigration.load(path);
        require("1".equals(lock.getProperty("schema"))
                        && BoundedDropTestKitPinMigration.INTRODUCTION.equals(
                                lock.getProperty("introduction.commit"))
                        && BoundedDropTestKitPinMigration.BASE.equals(lock.getProperty("base.commit")),
                "bounded-drop TestKit migration identity drift");
        byte[] baseline = gitShow(root, BoundedDropTestKitPinMigration.BASE
                + ":smokes/qualification.lock");
        require(baseline != null && BoundedDropTestKitPinMigration.digest(baseline)
                        .equals(required(lock, "base.qualification_sha256"))
                        && integer(lock, "base.pin_count") == BoundedDropTestKitPinMigration.BASE_PINS,
                "bounded-drop baseline qualification drift");
        int files = integer(lock, "file.count");
        require(files == BoundedDropTestKitPinMigration.FILES.size(),
                "bounded-drop source census drift");
        Properties train = TrainPinCheck.manifest(root);
        for (int index = 0; index < files; index++) verifyFile(root, lock, train, index);

        SmokePins pins = new SmokePins(root); pins.validateEvidence();
        require(pins.entries().size() >= BoundedDropTestKitPinMigration.BASE_PINS + 1,
                "bounded-drop sealed pin census regressed");
        SmokeInputFingerprint fingerprints = new SmokeInputFingerprint(root);
        List<SmokeDiscovery.Entry> catalog = SmokeDiscovery.discover(root);
        int carried = integer(lock, "carried.count");
        require(carried == BoundedDropTestKitPinMigration.CARRIED.size(),
                "bounded-drop carried census drift");
        for (int index = 0; index < carried; index++) {
            String stem = "smoke." + index + ".", id = required(lock, stem + "id");
            require(id.equals(BoundedDropTestKitPinMigration.CARRIED.get(index)),
                    "bounded-drop carried order drift");
            String current = fingerprints.compute(smoke(catalog, id));
            SmokePins.Entry pin = pins.match(id, current);
            boolean transported = pin != null && pin.source().equals("refactor-equivalent")
                            && current.equals(required(lock, stem + "current_fingerprint"))
                            && pin.evidence().equals(required(lock, stem + "evidence_sha256"))
                            && required(lock, stem + "prior_fingerprint").matches("[0-9a-f]{64}");
            boolean exactSuccessor = pin != null
                    && TrainPinCheck.carriesCurrent(train, id, pin, current);
            boolean dataDrivenSuccessor = pin != null
                    && pin.evidence().equals(required(lock, stem + "evidence_sha256"))
                    && DataDrivenCycleCheck.carriesPlan(root, id, pin);
            boolean schemaSuccessor = pin != null
                    && pin.evidence().equals(required(lock, stem + "evidence_sha256"))
                    && SchemaPinCheck.carries(SchemaPinCheck.manifest(root), id, pin, current);
            boolean compositeSuccessor = pin != null
                    && pin.evidence().equals(required(lock, stem + "evidence_sha256"))
                    && CompositeCycleCheck.carriesPlan(root, id, pin);
            boolean executedSuccessor = NeighborTestKitPinCheck.reexecuted(pin);
            require(transported || exactSuccessor || dataDrivenSuccessor || schemaSuccessor
                            || compositeSuccessor || executedSuccessor,
                    "bounded-drop carried proof drift: " + id);
        }
        String id = required(lock, "anchor.id");
        require(id.equals(BoundedDropTestKitPinMigration.NEW_ID), "bounded-drop anchor drift");
        String current = fingerprints.compute(smoke(catalog, id));
        SmokePins.Entry pin = pins.match(id, current);
        String prior = required(lock, "anchor.fingerprint");
        String evidence = required(lock, "anchor.evidence_sha256");
        boolean direct = pin != null && pin.source().equals("executed")
                && current.equals(prior) && pin.evidence().equals(evidence);
        boolean exactSuccessor = pin != null && pin.evidence().equals(evidence)
                && TrainPinCheck.carriesCurrent(train, id, pin, current);
        boolean dataDrivenSuccessor = pin != null && pin.evidence().equals(evidence)
                && DataDrivenCycleCheck.carriesPlan(root, id, pin);
        boolean schemaSuccessor = pin != null && pin.evidence().equals(evidence)
                && SchemaPinCheck.carries(SchemaPinCheck.manifest(root), id, pin, current);
        boolean compositeSuccessor = pin != null && pin.evidence().equals(evidence)
                && CompositeCycleCheck.carriesPlan(root, id, pin);
        boolean executedSuccessor = NeighborTestKitPinCheck.reexecuted(pin);
        require(direct || exactSuccessor || dataDrivenSuccessor || schemaSuccessor
                        || compositeSuccessor || executedSuccessor,
                "bounded-drop exact anchor drift");
        System.out.println("  bounded-drop TestKit migration: " + carried
                + " carried proofs, 1 exact anchor");
    }

    static boolean transportsFile(Path root, String relative, String priorSha256) {
        try {
            Properties lock = NeighborTestKitPinMigration.load(
                    root.resolve("smokes/bounded-drop-testkit-migration.lock"));
            for (int index = 0; index < integer(lock, "file.count"); index++) {
                String stem = "file." + index + ".";
                if (!relative.equals(lock.getProperty(stem + "path"))) continue;
                return priorSha256.equals(lock.getProperty(stem + "prior_sha256"))
                        && BoundedDropTestKitPinMigration.digest(
                                Files.readAllBytes(root.resolve(relative)))
                                .equals(lock.getProperty(stem + "current_sha256"));
            }
            return false;
        } catch (Exception error) { return false; }
    }

    static boolean transportsSmoke(Path root, String id, String priorFingerprint,
            String evidenceSha256, String currentFingerprint) {
        try {
            Properties lock = NeighborTestKitPinMigration.load(
                    root.resolve("smokes/bounded-drop-testkit-migration.lock"));
            for (int index = 0; index < integer(lock, "carried.count"); index++) {
                String stem = "smoke." + index + ".";
                if (!id.equals(lock.getProperty(stem + "id"))) continue;
                return priorFingerprint.equals(lock.getProperty(stem + "prior_fingerprint"))
                        && evidenceSha256.equals(lock.getProperty(stem + "evidence_sha256"))
                        && currentFingerprint.equals(lock.getProperty(stem + "current_fingerprint"));
            }
            return false;
        } catch (Exception error) { return false; }
    }

    private static void verifyFile(Path root, Properties lock, Properties train, int index)
            throws Exception {
        String stem = "file." + index + ".", relative = required(lock, stem + "path");
        require(relative.equals(BoundedDropTestKitPinMigration.FILES.get(index)),
                "bounded-drop source order drift");
        String expectedCurrent = required(lock, stem + "current_sha256");
        require(BoundedDropTestKitPinMigration.digest(Files.readAllBytes(root.resolve(relative)))
                        .equals(expectedCurrent)
                        || TrainPinCheck.transportsFile(train, root, relative, expectedCurrent),
                "bounded-drop current source drift: " + relative);
        byte[] prior = gitShow(root, BoundedDropTestKitPinMigration.BASE + ":" + relative);
        String expected = required(lock, stem + "prior_sha256");
        require(prior == null ? expected.equals("absent")
                        : BoundedDropTestKitPinMigration.digest(prior).equals(expected),
                "bounded-drop prior source drift: " + relative);
    }
    private static SmokeDiscovery.Entry smoke(List<SmokeDiscovery.Entry> catalog, String id) {
        return catalog.stream().filter(row -> row.id.equals(id)).findFirst()
                .orElseThrow(() -> new IllegalStateException("missing bounded-drop smoke: " + id));
    }
    private static byte[] gitShow(Path root, String object) throws Exception {
        Process process = new ProcessBuilder("git", "show", object).directory(root.toFile())
                .redirectError(ProcessBuilder.Redirect.DISCARD).start();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        process.getInputStream().transferTo(output);
        return process.waitFor() == 0 ? output.toByteArray() : null;
    }
    private static int integer(Properties values, String key) {
        try { return Integer.parseInt(required(values, key)); }
        catch (NumberFormatException error) { throw new IllegalStateException("invalid " + key); }
    }
    private static String required(Properties values, String key) {
        String value = values.getProperty(key);
        require(value != null && !value.isBlank(), "missing " + key); return value;
    }
    private static void require(boolean value, String message) {
        BoundedDropTestKitPinMigration.require(value, message);
    }
}
