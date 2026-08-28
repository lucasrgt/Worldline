import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

/** Validates support-face lifecycle proof transport and its exact official anchors. */
final class SupportFaceTestKitPinCheck {
    private SupportFaceTestKitPinCheck() { }

    static void execute(Path root) throws Exception {
        Path path = root.resolve("smokes/support-face-testkit-migration.lock");
        SupportFaceTestKitPinMigration.require(Files.isRegularFile(path),
                "missing support-face TestKit migration lock");
        Properties lock = NeighborTestKitPinMigration.load(path);
        require("1".equals(lock.getProperty("schema"))
                        && SupportFaceTestKitPinMigration.INTRODUCTION.equals(
                                lock.getProperty("introduction.commit"))
                        && SupportFaceTestKitPinMigration.BASE.equals(lock.getProperty("base.commit")),
                "support-face TestKit migration identity drift");
        byte[] baseline = gitShow(root, SupportFaceTestKitPinMigration.BASE
                + ":smokes/qualification.lock");
        require(baseline != null && SupportFaceTestKitPinMigration.digest(baseline)
                        .equals(required(lock, "base.qualification_sha256"))
                        && integer(lock, "base.pin_count") == SupportFaceTestKitPinMigration.BASE_PINS,
                "support-face baseline qualification drift");
        int files = integer(lock, "file.count");
        require(files == SupportFaceTestKitPinMigration.FILES.size(),
                "support-face source census drift");
        for (int index = 0; index < files; index++) verifyFile(root, lock, index);

        SmokePins pins = new SmokePins(root); pins.validateEvidence();
        require(pins.entries().size() >= SupportFaceTestKitPinMigration.BASE_PINS + 1,
                "support-face sealed pin census regressed");
        SmokeInputFingerprint fingerprints = new SmokeInputFingerprint(root);
        List<SmokeDiscovery.Entry> catalog = SmokeDiscovery.discover(root);
        Properties train = TrainPinCheck.manifest(root);
        int carried = integer(lock, "carried.count");
        require(carried == SupportFaceTestKitPinMigration.CARRIED.size(),
                "support-face carried census drift");
        for (int index = 0; index < carried; index++) {
            String stem = "smoke." + index + ".";
            String id = required(lock, stem + "id");
            require(id.equals(SupportFaceTestKitPinMigration.CARRIED.get(index)),
                    "support-face carried order drift");
            String current = fingerprints.compute(smoke(catalog, id));
            SmokePins.Entry pin = pins.match(id, current);
            String priorCurrent = required(lock, stem + "current_fingerprint");
            boolean direct = current.equals(priorCurrent);
            boolean successor = BoundedDropTestKitPinCheck.transportsSmoke(root, id,
                    priorCurrent, required(lock, stem + "evidence_sha256"), current);
            boolean transported = pin != null && pin.source().equals("refactor-equivalent")
                            && (direct || successor)
                            && pin.evidence().equals(required(lock, stem + "evidence_sha256"))
                            && required(lock, stem + "prior_fingerprint").matches("[0-9a-f]{64}");
            boolean exactSuccessor = pin != null
                    && TrainPinCheck.carriesCurrent(train, id, pin, current);
            boolean dataDrivenSuccessor = pin != null
                    && pin.evidence().equals(required(lock, stem + "evidence_sha256"))
                    && DataDrivenCycleCheck.carriesPlan(root, id, pin);
            boolean executedSuccessor = NeighborTestKitPinCheck.reexecuted(pin);
            require(transported || exactSuccessor || dataDrivenSuccessor || executedSuccessor,
                    "support-face carried proof drift: " + id);
        }
        int anchors = integer(lock, "anchor.count");
        require(anchors == SupportFaceTestKitPinMigration.ANCHORS.size(),
                "support-face anchor census drift");
        for (int index = 0; index < anchors; index++) {
            String stem = "anchor." + index + ".", id = required(lock, stem + "id");
            require(id.equals(SupportFaceTestKitPinMigration.ANCHORS.get(index)),
                    "support-face anchor order drift");
            String current = fingerprints.compute(smoke(catalog, id));
            SmokePins.Entry pin = pins.match(id, current);
            String priorCurrent = required(lock, stem + "fingerprint");
            boolean direct = current.equals(priorCurrent) && pin != null
                    && pin.source().equals("executed");
            boolean successor = BoundedDropTestKitPinCheck.transportsSmoke(root, id,
                    priorCurrent, required(lock, stem + "evidence_sha256"), current);
            boolean transported = pin != null && (direct || successor)
                    && pin.evidence().equals(required(lock, stem + "evidence_sha256"));
            boolean exactSuccessor = pin != null
                    && TrainPinCheck.carriesCurrent(train, id, pin, current);
            boolean dataDrivenSuccessor = pin != null && pin.evidence().equals(evidence)
                    && DataDrivenCycleCheck.carriesPlan(root, id, pin);
            boolean executedSuccessor = NeighborTestKitPinCheck.reexecuted(pin);
            require(transported || exactSuccessor || dataDrivenSuccessor || executedSuccessor,
                    "support-face exact anchor drift: " + id);
        }
        System.out.println("  support-face TestKit migration: " + carried
                + " carried proofs, " + anchors + " exact anchors");
    }

    static boolean transportsFile(Path root, String relative, String priorSha256) {
        try {
            Properties lock = NeighborTestKitPinMigration.load(
                    root.resolve("smokes/support-face-testkit-migration.lock"));
            int files = integer(lock, "file.count");
            for (int index = 0; index < files; index++) {
                String stem = "file." + index + ".";
                if (!relative.equals(lock.getProperty(stem + "path"))) continue;
                String introduced = lock.getProperty(stem + "current_sha256");
                return priorSha256.equals(lock.getProperty(stem + "prior_sha256"))
                        && (SupportFaceTestKitPinMigration.digest(
                                Files.readAllBytes(root.resolve(relative))).equals(introduced)
                        || BoundedDropTestKitPinCheck.transportsFile(
                                root, relative, introduced));
            }
            return false;
        } catch (Exception error) {
            return false;
        }
    }

    static boolean transportsSmoke(Path root, String id, String priorFingerprint,
            String evidenceSha256, String currentFingerprint) {
        try {
            Properties lock = NeighborTestKitPinMigration.load(
                    root.resolve("smokes/support-face-testkit-migration.lock"));
            int carried = integer(lock, "carried.count");
            for (int index = 0; index < carried; index++) {
                String stem = "smoke." + index + ".";
                if (!id.equals(lock.getProperty(stem + "id"))) continue;
                String introduced = lock.getProperty(stem + "current_fingerprint");
                return priorFingerprint.equals(lock.getProperty(stem + "prior_fingerprint"))
                        && evidenceSha256.equals(lock.getProperty(stem + "evidence_sha256"))
                        && (currentFingerprint.equals(introduced)
                        || BoundedDropTestKitPinCheck.transportsSmoke(root, id, introduced,
                                evidenceSha256, currentFingerprint));
            }
            return false;
        } catch (Exception error) {
            return false;
        }
    }

    private static void verifyFile(Path root, Properties lock, int index) throws Exception {
        String stem = "file." + index + ".", relative = required(lock, stem + "path");
        require(relative.equals(SupportFaceTestKitPinMigration.FILES.get(index)),
                "support-face source order drift");
        String current = required(lock, stem + "current_sha256");
        require(SupportFaceTestKitPinMigration.digest(Files.readAllBytes(root.resolve(relative)))
                        .equals(current)
                        || BoundedDropTestKitPinCheck.transportsFile(root, relative, current),
                "support-face current source drift: " + relative);
        byte[] prior = gitShow(root, SupportFaceTestKitPinMigration.BASE + ":" + relative);
        String expected = required(lock, stem + "prior_sha256");
        require(prior == null ? expected.equals("absent")
                        : SupportFaceTestKitPinMigration.digest(prior).equals(expected),
                "support-face prior source drift: " + relative);
    }
    private static SmokeDiscovery.Entry smoke(List<SmokeDiscovery.Entry> catalog, String id) {
        return catalog.stream().filter(row -> row.id.equals(id)).findFirst()
                .orElseThrow(() -> new IllegalStateException("missing support-face smoke: " + id));
    }
    private static byte[] gitShow(Path root, String object) throws Exception {
        Process process = new ProcessBuilder("git", "show", object).directory(root.toFile())
                .redirectError(ProcessBuilder.Redirect.DISCARD).start();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        process.getInputStream().transferTo(output); return process.waitFor() == 0 ? output.toByteArray() : null;
    }
    private static int integer(Properties values, String key) {
        try { return Integer.parseInt(required(values, key)); }
        catch (NumberFormatException error) { throw new IllegalStateException("invalid " + key); }
    }
    private static String required(Properties values, String key) {
        String value = values.getProperty(key); require(value != null && !value.isBlank(), "missing " + key);
        return value;
    }
    private static void require(boolean value, String message) {
        SupportFaceTestKitPinMigration.require(value, message);
    }
}
