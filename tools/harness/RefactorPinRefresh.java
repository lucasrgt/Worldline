import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/** Carries qualification pins across a source-only refactor without rerunning smokes. */
final class RefactorPinRefresh extends TrainPinSupport {
    private static final String BASE = "fd1e11d7c5e878d06137170e51b46aa9a5352569";

    private RefactorPinRefresh() { }

    public static void main(String[] arguments) {
        try {
            require(List.of(arguments).equals(List.of("--apply")),
                    "usage: RefactorPinRefresh --apply");
            apply(Path.of("").toAbsolutePath().normalize());
        } catch (Exception error) {
            System.err.println("refactor pin refresh failed: " + error.getMessage());
            System.exit(1);
        }
    }

    private static void apply(Path root) throws Exception {
        require(capture(root, "status", "--porcelain", "--untracked-files=all").isBlank()
                        && status(root, "merge-base", "--is-ancestor", BASE, "HEAD") == 0,
                "refactor pin refresh requires a clean committed tree and valid base");
        List<SmokeDiscovery.Entry> catalog = SmokeDiscovery.discover(root);
        SmokePins pins = new SmokePins(root);
        pins.validateEvidence();
        SmokeInputFingerprint fingerprints = new SmokeInputFingerprint(root);
        List<SmokePins.Entry> updated = new ArrayList<>();
        Properties train = TrainPinCheck.manifest(root);
        Properties next = new Properties();
        next.putAll(train);
        int changed = 0;
        for (SmokeDiscovery.Entry smoke : catalog) {
            String current = fingerprints.compute(smoke);
            SmokePins.Entry prior = pins.entry(smoke.id);
            require(prior != null, "missing qualification pin: " + smoke.id);
            String stem = "smoke." + smoke.id + ".";
            String kind = train.getProperty(stem + "kind");
            String source = "executed".equals(kind) ? "executed"
                    : current.equals(prior.fingerprint()) ? prior.source()
                    : "refactor-equivalent";
            SmokePins.Entry entry = current.equals(prior.fingerprint())
                    && source.equals(prior.source()) ? prior
                    : new SmokePins.Entry(smoke.id, current, prior.evidence(), source);
            if (!current.equals(prior.fingerprint()) || !source.equals(prior.source()))
                changed++;
            updated.add(entry);
            String sealed = train.getProperty(stem + "current_fingerprint");
            if (sealed != null && !current.equals(sealed)) {
                next.setProperty(stem + "prior_fingerprint", sealed);
                next.setProperty(stem + "current_fingerprint", current);
                next.setProperty(stem + "evidence_sha256", prior.evidence());
            }
        }
        pins.write(updated);
        TrainSourceHistory.load(root).writeSources(root, next, train, train, BASE);
        store(root.resolve("smokes/train-reconciliation.lock"), next);
        System.out.println("refactor pin refresh: " + changed + " fingerprints, "
                + catalog.size() + " catalog");
    }
}
