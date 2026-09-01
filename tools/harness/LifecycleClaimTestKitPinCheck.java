import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/** Validates public lifecycle-claim proof transport and its exact placement anchors. */
final class LifecycleClaimTestKitPinCheck {
    private LifecycleClaimTestKitPinCheck() { }

    static void execute(Path root) throws Exception {
        Properties lock = load(root);
        LifecycleClaimTestKitPinMigration.require(
                "1".equals(lock.getProperty("schema"))
                        && LifecycleClaimTestKitPinMigration.INTRODUCTION.equals(
                                lock.getProperty("introduction.commit"))
                        && LifecycleClaimTestKitPinMigration.BASE.equals(
                                lock.getProperty("base.commit")),
                "lifecycle-claim TestKit migration identity drift");
        byte[] baseline = LifecycleClaimTestKitPinMigration.committed(root,
                LifecycleClaimTestKitPinMigration.BASE, "smokes/qualification.lock");
        require(baseline != null && LifecycleClaimTestKitPinMigration.digest(baseline)
                        .equals(required(lock, "base.qualification_sha256"))
                        && integer(lock, "base.pin_count")
                                == LifecycleClaimTestKitPinMigration.BASE_PINS,
                "lifecycle-claim baseline qualification drift");
        List<String> files = LifecycleClaimTestKitPinMigration.files(root);
        require(integer(lock, "file.count") == files.size(),
                "lifecycle-claim source census drift");
        for (int index = 0; index < files.size(); index++) verifyFile(root, lock, files, index);

        SmokePins pins = new SmokePins(root); pins.validateEvidence();
        List<SmokeDiscovery.Entry> catalog = SmokeDiscovery.discover(root);
        require(pins.entries().size() == catalog.size()
                        && pins.entries().size() >= LifecycleClaimTestKitPinMigration.BASE_PINS,
                "lifecycle-claim sealed pin census drift");
        SmokeInputFingerprint fingerprints = new SmokeInputFingerprint(root);
        int carried = integer(lock, "carried.count"), exact = 0;
        Set<String> ids = new HashSet<>();
        for (int index = 0; index < carried; index++) {
            String stem = "smoke." + index + ".", id = required(lock, stem + "id");
            require(ids.add(id), "duplicate lifecycle-claim transported smoke: " + id);
            SmokeDiscovery.Entry smoke = smoke(catalog, id);
            String current = fingerprints.compute(smoke);
            SmokePins.Entry pin = pins.match(id, current);
            String source = required(lock, stem + "source");
            boolean direct = pin != null
                    && current.equals(required(lock, stem + "current_fingerprint"))
                            && pin.evidence().equals(required(lock, stem + "evidence_sha256"))
                    && source.equals(pin.source());
            boolean successor = trainSuccessor(root, lock, stem, id, pin, current);
            boolean executedSuccessor = NeighborTestKitPinCheck.reexecuted(pin);
            require((direct || successor || executedSuccessor)
                            && required(lock, stem + "prior_fingerprint").matches("[0-9a-f]{64}"),
                    "lifecycle-claim transported proof drift: " + id);
            if (source.equals("executed")) exact++;
            else require(source.equals("refactor-equivalent"),
                    "invalid lifecycle-claim proof source: " + id);
        }
        require(exact == integer(lock, "anchor.count")
                        && exact == LifecycleClaimTestKitPinMigration.ANCHORS.size(),
                "lifecycle-claim exact anchor drift");
        int direct = integer(lock, "direct.count");
        require(direct == LifecycleClaimTestKitPinMigration.DIRECT.size(),
                "lifecycle-claim direct census drift");
        for (int index = 0; index < direct; index++) {
            String stem = "direct." + index + ".", id = required(lock, stem + "id");
            require(id.equals(LifecycleClaimTestKitPinMigration.DIRECT.get(index))
                            && ids.contains(id)
                            && required(lock, stem + "prior_plan_sha256").matches("[0-9a-f]{64}")
                            && (DataDrivenCyclePlan.load(root, id).fingerprint().equals(
                                    required(lock, stem + "current_plan_sha256"))
                            || trainPlanSuccessor(root, id)),
                    "lifecycle-claim direct plan drift: " + id);
        }
        System.out.println("  lifecycle-claim TestKit migration: " + carried
                + " transported proofs, " + exact + " exact anchors");
    }

    static boolean carries(Path root, String id, SmokePins.Entry pin, String current) {
        try {
            if (pin == null) return false;
            Properties lock = load(root);
            for (int index = 0; index < integer(lock, "carried.count"); index++) {
                String stem = "smoke." + index + ".";
                if (!id.equals(lock.getProperty(stem + "id"))) continue;
                boolean direct = current.equals(lock.getProperty(stem + "current_fingerprint"))
                        && pin.evidence().equals(lock.getProperty(stem + "evidence_sha256"))
                        && pin.source().equals(lock.getProperty(stem + "source"));
                return direct || trainSuccessor(root, lock, stem, id, pin, current);
            }
            return false;
        } catch (Exception error) { return false; }
    }

    static boolean transportsPlan(Path root, String id, String prior, String current) {
        try {
            Properties lock = load(root);
            for (int index = 0; index < integer(lock, "direct.count"); index++) {
                String stem = "direct." + index + ".";
                if (!id.equals(lock.getProperty(stem + "id"))) continue;
                return prior.equals(lock.getProperty(stem + "prior_plan_sha256"))
                        && current.equals(lock.getProperty(stem + "current_plan_sha256"));
            }
            return false;
        } catch (Exception error) { return false; }
    }

    private static boolean trainPlanSuccessor(Path root, String id) {
        try {
            String plan = DataDrivenCyclePlan.load(root, id).fingerprint();
            Properties migrations = StrictProperties.load(
                    root.resolve("smokes/data-driven-migration.lock"));
            if (!plan.equals(migrations.getProperty("cycle." + id + ".plan_sha256"))) return false;
            SmokeDiscovery.Entry smoke = smoke(SmokeDiscovery.discover(root), id);
            String current = new SmokeInputFingerprint(root).compute(smoke);
            SmokePins.Entry pin = new SmokePins(root).match(id, current);
            return TrainPinCheck.carriesCurrent(
                    TrainPinCheck.manifest(root), id, pin, current);
        } catch (Exception error) { return false; }
    }

    static boolean transportsFile(Path root, String relative, String prior) {
        try {
            Properties lock = load(root);
            for (int index = 0; index < integer(lock, "file.count"); index++) {
                String stem = "file." + index + ".";
                if (!relative.equals(lock.getProperty(stem + "path"))) continue;
                String introduced = lock.getProperty(stem + "prior_sha256");
                return connectsFile(root, relative, prior, introduced)
                        && (LifecycleClaimTestKitPinMigration.digest(
                                Files.readAllBytes(root.resolve(relative)))
                                .equals(lock.getProperty(stem + "current_sha256"))
                        || DocumentationCatalog.current(root, relative));
            }
            return false;
        } catch (Exception error) { return false; }
    }

    private static boolean connectsFile(Path root, String relative,
            String prior, String introduced) throws Exception {
        if (prior.equals(introduced)) return true;
        if (SharedHelperPinCheck.transitionsFile(SharedHelperPinCheck.manifest(root),
                relative, prior, introduced)) return true;
        Properties train = TrainPinCheck.manifest(root);
        int count = Integer.parseInt(train.getProperty("source.count", "0"));
        for (int index = 0; index < count; index++) {
            String stem = "source." + index + ".";
            if (!relative.equals(train.getProperty(stem + "path"))
                    || !introduced.equals(train.getProperty(stem + "current_sha256"))) continue;
            String predecessor = train.getProperty(stem + "prior_sha256");
            return prior.equals(predecessor) || TrainSourceHistory.connects(train, stem, prior)
                    || GuiWorkbenchPinCheck.transitionsFile(
                            GuiWorkbenchPinCheck.manifest(root), relative, prior, predecessor)
                    || TestKitReleasePinCheck.transitionsFile(
                            TestKitReleasePinCheck.manifest(root), relative, prior, predecessor)
                    || SharedHelperPinCheck.transitionsFile(
                            SharedHelperPinCheck.manifest(root), relative, prior, predecessor);
        }
        return false;
    }

    private static boolean trainSuccessor(Path root, Properties lock, String stem,
            String id, SmokePins.Entry pin, String current) throws Exception {
        return pin != null
                && ("refactor-equivalent".equals(lock.getProperty(stem + "source"))
                        || "executed".equals(lock.getProperty(stem + "source")))
                && ("refactor-equivalent".equals(pin.source())
                        || "executed".equals(pin.source()))
                && TrainPinCheck.carriesCurrent(
                        TrainPinCheck.manifest(root), id, pin, current);
    }

    private static void verifyFile(Path root, Properties lock, List<String> files, int index)
            throws Exception {
        String stem = "file." + index + ".", relative = required(lock, stem + "path");
        String sealed = required(lock, stem + "current_sha256");
        require(relative.equals(files.get(index))
                        && (LifecycleClaimTestKitPinMigration.digest(
                                Files.readAllBytes(root.resolve(relative))).equals(sealed)
                        || DocumentationCatalog.current(root, relative)
                        || transportedByTrain(root, relative, sealed)),
                "lifecycle-claim current source drift: " + relative);
        byte[] prior = LifecycleClaimTestKitPinMigration.committed(root,
                LifecycleClaimTestKitPinMigration.BASE, relative);
        String expected = required(lock, stem + "prior_sha256");
        require(prior == null ? expected.equals("absent")
                        : LifecycleClaimTestKitPinMigration.digest(prior).equals(expected),
                "lifecycle-claim prior source drift: " + relative);
    }

    private static boolean transportedByTrain(Path root, String relative, String sealed)
            throws Exception {
        Properties train = TrainPinCheck.manifest(root);
        if (TrainPinCheck.transportsFile(train, root, relative, sealed)) return true;
        byte[] introduced = LifecycleClaimTestKitPinMigration.committed(root,
                LifecycleClaimTestKitPinMigration.INTRODUCTION, relative);
        return introduced != null && TrainPinCheck.transportsFile(train, root, relative,
                LifecycleClaimTestKitPinMigration.digest(PortableText.normalize(introduced)));
    }
    private static Properties load(Path root) throws Exception {
        Properties values = new Properties();
        try (java.io.Reader reader = Files.newBufferedReader(
                root.resolve("smokes/lifecycle-claim-testkit-migration.lock"))) {
            values.load(reader);
        }
        return values;
    }
    private static SmokeDiscovery.Entry smoke(List<SmokeDiscovery.Entry> catalog, String id) {
        return catalog.stream().filter(row -> row.id.equals(id)).findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "missing lifecycle-claim smoke: " + id));
    }
    private static int integer(Properties values, String key) {
        try { return Integer.parseInt(required(values, key)); }
        catch (NumberFormatException error) { throw new IllegalStateException("invalid " + key); }
    }
    private static String required(Properties values, String key) {
        return LifecycleClaimTestKitPinMigration.required(values, key);
    }
    private static void require(boolean value, String message) {
        LifecycleClaimTestKitPinMigration.require(value, message);
    }
}
