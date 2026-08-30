import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/** Reseals exact reviewed generated-documentation train source successors. */
final class TrainGeneratedDocumentationMigration extends TrainPinSupport {
    private TrainGeneratedDocumentationMigration() { }

    static boolean apply(Path root, String base) throws Exception {
        return apply(root, base, TrainGeneratedDocumentationMigration::carriesSource,
                TrainGeneratedDocumentationMigration::validateProofs);
    }

    private static boolean apply(Path root, String base, SourceReview review,
            ProofValidation proofValidation) throws Exception {
        Path path = root.resolve("smokes/train-reconciliation.lock");
        if (!Files.isRegularFile(path)) return false;
        Properties prior = load(path);
        if (!"1".equals(prior.getProperty("schema"))) return false;
        int drift = sourceDrift(root, prior, review);
        if (drift == 0) return false;
        Properties next = new Properties(); next.putAll(prior);
        proofValidation.validate(root, prior, next);
        TrainSourceHistory.load(root).writeSources(root, next, prior, prior, base);
        store(path, next);
        System.out.println("train generated documentation sources refreshed: " + drift);
        return true;
    }

    private static int sourceDrift(Path root, Properties lock, SourceReview review)
            throws Exception {
        int count = Integer.parseInt(required(lock, "source.count")), drift = 0;
        for (int index = 0; index < count; index++) {
            String stem = "source." + index + ".";
            String relative = required(lock, stem + "path");
            Path path = root.resolve(relative);
            String sealed = required(lock, stem + "current_sha256");
            String current = Files.isRegularFile(path) ? sourceDigest(path) : "removed";
            if (sealed.equals(current)) continue;
            require(review.carries(relative, sealed, current),
                    "unreviewed train source refresh: " + relative);
            drift++;
        }
        return drift;
    }

    private static void validateProofs(Path root, Properties lock, Properties next)
            throws Exception {
        SmokePins pins = new SmokePins(root); pins.validateEvidence();
        SmokeInputFingerprint fingerprints = new SmokeInputFingerprint(root);
        Properties schemas = SchemaPinCheck.manifest(root);
        java.util.List<SmokeDiscovery.Entry> catalog = SmokeDiscovery.discover(root);
        require(Integer.parseInt(required(lock, "catalog.count")) == catalog.size(),
                "train refresh catalog drift");
        for (SmokeDiscovery.Entry smoke : catalog) {
            String stem = "smoke." + smoke.id + ".";
            String current = fingerprints.compute(smoke);
            SmokePins.Entry pin = pins.match(smoke.id, current);
            String prior = required(lock, stem + "current_fingerprint");
            String evidence = required(lock, stem + "evidence_sha256");
            boolean successor = pin != null && SchemaPinCheck.follows(
                    schemas, smoke.id, prior, evidence, pin, current);
            require(carriesProof(pin, current, prior, evidence, successor),
                    "train refresh proof drift: " + smoke.id);
            if (!current.equals(prior)) {
                next.setProperty(stem + "prior_fingerprint", prior);
                next.setProperty(stem + "current_fingerprint", current);
                next.setProperty(stem + "evidence_sha256", pin.evidence());
            }
        }
    }

    private static boolean carriesProof(SmokePins.Entry pin, String current,
            String prior, String evidence, boolean successor) {
        return pin != null && (pin.source().equals("executed")
                || pin.source().equals("refactor-equivalent"))
                && (successor || current.equals(prior)
                && pin.evidence().equals(evidence));
    }

    static boolean carriesSource(String relative, String prior, String current) {
        return reviewed(relative, prior, current,
                "docs/generated/MILESTONES.md",
                "e55084a899ee2b6719ad571034e1a637389bce20db4619bdd65da2d9716b8c02",
                "0a2a41227387f89d13cb31db4306bc31045dfab4fd74ac1c458a7cf16bf9f3f0")
                || reviewed(relative, prior, current,
                        "tools/harness/HarnessFeatureSelfTest.java",
                        "2a19f68c15d319a8213493b8ce746abe89f350ead2158a4bf172050aca428be8",
                        "cc30674744a136922cb8e5be2fa89516549eeef7229a2a923a4d6fb327226ffc")
                || reviewed(relative, prior, current,
                        "tools/harness/SchemaPinCheck.java",
                        "c73eef7d6cd6dce776d6e5de94b03bed6e2b573496c8627ab867480783e6f7b1",
                        "bc2eaca13abaa16bb7120418f8703605cbeee52e5b090d680ae986f7f78c79b0")
                || reviewed(relative, prior, current,
                        "tools/harness/TrainPinMigration.java",
                        "d7d1d9d4ee0d109b730581f7947b5727faf9d98b89c7a092f7420c5c52c8e208",
                        "f08028a332e3d7f890e8c7e8004a02e0f46b5c14064e2b91af7ad5c56a9db9fa");
    }

    private static boolean reviewed(String relative, String prior, String current,
            String expectedRelative, String expectedPrior, String expectedCurrent) {
        return relative.equals(expectedRelative) && prior.equals(expectedPrior)
                && current.equals(expectedCurrent);
    }

    static void selfTest() throws Exception {
        require(!carriesSource("unreviewed", "old", "new"),
                "train generated-documentation source allowlist drifted");
        SmokePins.Entry executed = new SmokePins.Entry(
                "fixture", "current-proof", "executed-evidence", "executed");
        require(carriesProof(executed, "current-proof", "prior-proof",
                        "prior-evidence", true)
                        && carriesProof(executed, "current-proof", "current-proof",
                                "executed-evidence", false)
                        && !carriesProof(executed, "current-proof", "prior-proof",
                                "prior-evidence", false)
                        && !carriesProof(new SmokePins.Entry("fixture", "current-proof",
                                "executed-evidence", "unreviewed"), "current-proof",
                                "current-proof", "executed-evidence", false),
                "train generated-documentation proof transport drifted");
        Path root = Files.createTempDirectory("worldline-train-source-refresh-");
        try { refreshSelfTest(root); }
        finally { SafeTreeDelete.delete(root); }
    }

    private static void refreshSelfTest(Path root) throws Exception {
        Path source = root.resolve("docs/generated/MILESTONES.md");
        Path lockPath = root.resolve("smokes/train-reconciliation.lock");
        Files.createDirectories(source.getParent()); Files.createDirectories(lockPath.getParent());
        Files.writeString(source, "old\n", StandardCharsets.UTF_8);
        Properties lock = new Properties(); lock.setProperty("schema", "1");
        lock.setProperty("source.count", "1");
        lock.setProperty("source.0.path", "docs/generated/MILESTONES.md");
        lock.setProperty("source.0.prior_sha256", "added");
        lock.setProperty("source.0.current_sha256", sourceDigest(source));
        lock.setProperty("source.0.ancestor.count", "0");
        lock.setProperty("executed.count", "1");
        lock.setProperty("smoke.fixture.prior_fingerprint", "prior-proof");
        lock.setProperty("smoke.fixture.evidence_sha256", "executed-evidence");
        lock.setProperty("smoke.fixture.receipt.signature", "executed-receipt");
        store(lockPath, lock); capture(root, "init"); capture(root, "add", ".");
        commit(root, "base"); String base = capture(root, "rev-parse", "HEAD").trim();
        String prior = sourceDigest(source);
        Files.writeString(source, "new\n", StandardCharsets.UTF_8);
        String current = sourceDigest(source); capture(root, "add", "."); commit(root, "successor");
        boolean[] validated = {false};
        require(apply(root, base, (path, old, next) -> path.endsWith("MILESTONES.md")
                        && old.equals(prior) && next.equals(current),
                (ignoredRoot, proof, successor) -> {
                    require("executed-receipt".equals(proof.getProperty(
                            "smoke.fixture.receipt.signature")), "executed provenance drifted");
                    successor.setProperty("smoke.fixture.prior_fingerprint", "current-proof");
                    successor.setProperty("smoke.fixture.current_fingerprint", "successor-proof");
                    validated[0] = true;
                }) && validated[0], "train source refresh did not validate proofs");
        Properties refreshed = load(lockPath);
        require(current.equals(refreshed.getProperty("source.0.current_sha256"))
                        && TrainSourceHistory.connects(refreshed, "source.0.", prior)
                        && "current-proof".equals(refreshed.getProperty(
                                "smoke.fixture.prior_fingerprint"))
                        && "successor-proof".equals(refreshed.getProperty(
                                "smoke.fixture.current_fingerprint"))
                        && "executed-evidence".equals(refreshed.getProperty(
                                "smoke.fixture.evidence_sha256")),
                "train source refresh proof ancestry drifted");
        Files.writeString(source, "rogue\n", StandardCharsets.UTF_8);
        try { sourceDrift(root, refreshed, (path, old, next) -> false);
            throw new IllegalStateException("train source refresh accepted unreviewed drift"); }
        catch (IllegalStateException expected) { require(expected.getMessage().startsWith(
                "unreviewed train source refresh"), "train source rejection drifted"); }
    }

    private static void commit(Path root, String message) throws Exception {
        capture(root, "-c", "user.name=Worldline", "-c", "user.email=worldline@test",
                "commit", "-m", message);
    }

    private static String sourceDigest(Path path) throws Exception {
        return digest(PortableText.normalize(Files.readAllBytes(path)));
    }

    @FunctionalInterface
    private interface SourceReview {
        boolean carries(String relative, String prior, String current);
    }
    @FunctionalInterface
    private interface ProofValidation {
        void validate(Path root, Properties lock, Properties successor) throws Exception;
    }
}
