import java.nio.file.Path;
import java.util.Properties;

/** Authorizes semantic generic-plan revisions only from exact executed evidence. */
final class DataDrivenPlanRevision {
    private final Path root;
    private final Properties manifest;
    private final SmokeReceiptCache cache;

    DataDrivenPlanRevision(Path root, Properties manifest, SmokeReceiptCache cache) {
        this.root = root; this.manifest = manifest; this.cache = cache;
    }

    static void selfTest() {
        SmokePins.Entry executed = new SmokePins.Entry("sample", "0".repeat(64),
                "1".repeat(64), "executed");
        SmokePins.Entry carried = new SmokePins.Entry("sample", "0".repeat(64),
                "1".repeat(64), "refactor-equivalent");
        require(executed(executed) && !executed(carried) && !executed(null),
                "plan revision proof boundary drifted");
    }

    boolean available() throws Exception {
        for (SmokeDiscovery.Entry smoke : SmokeDiscovery.discover(root)) {
            if (!generic(smoke)) continue;
            DataDrivenCyclePlan plan = DataDrivenCyclePlan.load(root, smoke.id);
            if (changed(smoke, plan) && executed(cache.availablePin(smoke))) return true;
        }
        return false;
    }

    boolean refresh(SmokeDiscovery.Entry smoke, DataDrivenCyclePlan plan,
            SmokePins.Entry local) throws Exception {
        if (!changed(smoke, plan)) return false;
        require(executed(local), "revised plan lacks an exact executed proof: " + smoke.id);
        String stem = "cycle." + smoke.id + ".";
        manifest.setProperty(stem + "plan_sha256", plan.fingerprint());
        manifest.setProperty(stem + "evidence_sha256", local.evidence());
        return true;
    }

    private boolean changed(SmokeDiscovery.Entry smoke, DataDrivenCyclePlan plan) throws Exception {
        String recorded = manifest.getProperty("cycle." + smoke.id + ".plan_sha256");
        return recorded != null && !plan.fingerprint().equals(recorded);
    }

    private static boolean generic(SmokeDiscovery.Entry smoke) {
        return smoke.runner.equals("tools/smoke/DataDrivenCycle.java");
    }

    private static boolean executed(SmokePins.Entry pin) {
        return pin != null && pin.source().equals("executed");
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
