package worldline.modtest;

import worldline.analysis.TraceDiff;

/** Metadata and first behavioral divergence for two mod test results. */
public final class ModTestComparison {
    private final ModTestResult left, right;
    private final TraceDiff traceDiff;

    private ModTestComparison(ModTestResult left, ModTestResult right) {
        this.left = left; this.right = right;
        traceDiff = TraceDiff.compare(left.trace(), right.trace());
    }

    public static ModTestComparison compare(ModTestResult left, ModTestResult right) {
        if (left == null || right == null) throw new NullPointerException("mod test result");
        return new ModTestComparison(left, right);
    }

    public boolean behaviorDiverged() { return traceDiff.diverged(); }
    public boolean sameMod() { return left.modId().equals(right.modId()); }
    public boolean sameVersion() { return left.modVersion().equals(right.modVersion()); }
    public boolean sameRuntime() { return left.runtime().equals(right.runtime()); }
    public boolean sameWorldlineApi() { return left.worldlineApi().equals(right.worldlineApi()); }
    public TraceDiff traceDiff() { return traceDiff; }

    public String render() {
        return "left.mod=" + left.modId() + "@" + left.modVersion() + "\n"
                + "right.mod=" + right.modId() + "@" + right.modVersion() + "\n"
                + "same.mod=" + sameMod() + "\n"
                + "same.version=" + sameVersion() + "\n"
                + "same.runtime=" + sameRuntime() + "\n"
                + "same.worldline.api=" + sameWorldlineApi() + "\n"
                + "left.artifact.sha256=" + left.artifactSha256() + "\n"
                + "right.artifact.sha256=" + right.artifactSha256() + "\n"
                + traceDiff.render();
    }
}
