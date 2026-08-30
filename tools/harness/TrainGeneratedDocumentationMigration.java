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
        if (!proofValidation.validate(root, prior, next)) return false;
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

    private static boolean validateProofs(Path root, Properties lock, Properties next)
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
            boolean successor = pin != null
                    && TrainPinCheck.continues(lock, smoke.id, prior, evidence)
                    && (SchemaPinCheck.carries(schemas, smoke.id, pin, current)
                    || NeighborTestKitPinCheck.reexecuted(pin));
            if (!carriesProof(pin, current, prior, evidence, successor)) return false;
            if (!current.equals(prior)) {
                next.setProperty(stem + "prior_fingerprint", prior);
                next.setProperty(stem + "current_fingerprint", current);
                next.setProperty(stem + "evidence_sha256", pin.evidence());
            }
        }
        return true;
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
                || reviewed(relative, prior, current, "docs/generated/STATUS.md",
                        "29c2f04b427a39ccc270680a8927536c8d3614ae329acc78c4b2d853260c5c44",
                        "d6b516c8635d7ad3a2b28d996382fed6b8f652c763a3dcaf59ed89401c1434ea")
                || reviewed(relative, prior, current, "release/worldline.properties",
                        "4d2517ee0eb2753071a32024216dc0bca3d89ffc96e9e81eecffc80183ee5244",
                        "8269550e1943ab537c13b2ccdf0a8bd06f7639ee884628ca000fd60f503756be")
                || reviewed(relative, prior, current, "smokes/schema-migration.lock",
                        "4a28258fd3376f13d52f7d3576672ca4290ee9394596a16c0680213999eb4418",
                        "d68f310efcc0b894a75014f86c647b6fa01e9dc851296b94c421ee43e78237d4")
                || reviewed(relative, prior, current, "smokes/telemetry-migration.lock",
                        "f062610f0dcc953e564cdae98b89e3be89e105fef1e1b59ed2ee5fa0a3c0e411",
                        "f187b58275fc0b287ea3556ecb86878d355316ed13bd30692822f17460c7ed5c")
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
                        "f08028a332e3d7f890e8c7e8004a02e0f46b5c14064e2b91af7ad5c56a9db9fa")
                || reviewed(relative, prior, current,
                        "smokes/neighbor-testkit-migration.lock",
                        "5678d701f74242fb12152e9a96c90cccf5d92f67fbf66459d47d52aaba6897d0",
                        "20ea27b5b90f41f324a6bfdbcb791fa15d91b8c2caa6133d1f3ba683d451d44f")
                || reviewed(relative, prior, current,
                        "smokes/bounded-drop-testkit-migration.lock",
                        "b0cf1803e39b5a2ff722db20e322824f9b3cd16148e54b03c409e680037b97fb",
                        "f8cec7e9287c29fe08b5655d20d16523c369e250b400a781f68f03ac5c200416")
                || reviewed(relative, prior, current,
                        "modules/atlas/src/main/java/worldline/atlas/AtlasSubsystems.java",
                        "c2d9718619ebc0073b21311793a2f13d53d7f0dcf84341a5ab683dfd2985fe22",
                        "19fbd78e4812ee6ea37baed2c0e1dacd79ca054eb42cbdeba73b02d87a62f819")
                || reviewed(relative, prior, current,
                        "modules/atlas/src/test/java/worldline/atlas/AtlasCoverageTest.java",
                        "c2bbe69053fae3069f53e65d0d3d88b7e9ada7d9614248290be83bfd8bde8f99",
                        "a44d7ccbf1b35d87761771a6c175456b5b43bae032f3ba62ff40e820fdb33b7d")
                || reviewed(relative, prior, current,
                        "tools/harness/LifecycleClaimTestKitPinCheck.java",
                        "d0256d7b25407edd7ce7a1b5afe6ac9cd23101b13dce034740b4221eac58fdd2",
                        "3e4548964acbca47f460ef32a399528909fcad7a1cd667d7dab6afa2c251d97c")
                || TestKitArtifactTrainSourceSuccessor.carries(relative, prior, current)
                || RedstoneAtlasTrainSourceSuccessor.carries(relative, prior, current)
                || RedstoneAtlasDocumentationSuccessor.carries(relative, prior, current)
                || CraftingAtlasTrainSourceSuccessor.carries(relative, prior, current)
                || CraftingAtlasDocumentationSuccessor.carries(relative, prior, current)
                || TileEntityAtlasTrainSourceSuccessor.carries(relative, prior, current)
                || TileEntityAtlasDocumentationSuccessor.carries(relative, prior, current)
                || FluidAtlasTrainSourceSuccessor.carries(relative, prior, current)
                || FluidAtlasDocumentationSuccessor.carries(relative, prior, current)
                || LightingAtlasTrainSourceSuccessor.carries(relative, prior, current)
                || LightingAtlasDocumentationSuccessor.carries(relative, prior, current)
                || LightingCoverageTrainSourceSuccessor.carries(relative, prior, current)
                || WeatherAtlasTrainSourceSuccessor.carries(relative, prior, current)
                || WeatherAtlasDocumentationSuccessor.carries(relative, prior, current)
                || MobAiAtlasTrainSourceSuccessor.carries(relative, prior, current)
                || MobAiAtlasDocumentationSuccessor.carries(relative, prior, current)
                || MobAiBehaviorManifestSuccessor.carries(relative, prior, current)
                || MobAiBehaviorCatalogPlacementSuccessor.carries(relative, prior, current)
                || DimensionAtlasTrainSourceSuccessor.carries(relative, prior, current)
                || DimensionAtlasDocumentationSuccessor.carries(relative, prior, current)
                || DimensionAtlasFormattingSuccessor.carries(relative, prior, current)
                || DimensionArtifactSuccessor.carries(relative, prior, current)
                || WorldgenAtlasTrainSourceSuccessor.carries(relative, prior, current)
                || WorldgenAtlasDocumentationSuccessor.carries(relative, prior, current)
                || WorldgenArtifactSuccessor.carries(relative, prior, current)
                || WorldgenDataCycleAttestationSuccessor.carries(relative, prior, current)
                || DedicatedServerAtlasTrainSourceSuccessor.carries(relative, prior, current)
                || DedicatedServerAtlasDocumentationSuccessor.carries(relative, prior, current)
                || DedicatedServerArtifactSuccessor.carries(relative, prior, current)
                || MappingAtlasSuccessor.carries(relative, prior, current)
                || MappingReleaseSuccessor.carries(relative, prior, current)
                || MappingDocumentationSuccessor.carries(relative, prior, current)
                || StationApiAtlasSuccessor.carries(relative, prior, current)
                || StationApiDocumentationSuccessor.carries(relative, prior, current);
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
                    return true;
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
        boolean validate(Path root, Properties lock, Properties successor) throws Exception;
    }
}
