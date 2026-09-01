import java.util.List;

/** Exact in-context Cell Page versus direct framebuffer oracle. */
record M787VisualOracle(long changedPixels, int maxDelta, int comparisons) {
    static M787VisualOracle evaluate(List<M787Artifact> values) throws Exception {
        return evaluateArtifacts(values, values.size());
    }

    static M787VisualOracle evaluateFirstPair(List<M787Artifact> values) throws Exception {
        return evaluateArtifacts(values, 1);
    }

    private static M787VisualOracle evaluateArtifacts(List<M787Artifact> values,
            int artifactCount) throws Exception {
        long changed = 0L;
        int maximum = 0;
        int comparisons = 0;
        SmokeSupport.require(values.size() >= artifactCount, "M787 paired artifact absent");
        for (int artifact = 0; artifact < artifactCount; artifact++) {
            M787Artifact value = values.get(artifact);
            for (int capture = 0; capture < value.captures; capture++) {
                byte[] pages = value.pixels("pages", capture);
                byte[] direct = value.pixels("direct", capture);
                SmokeSupport.require(pages.length == direct.length,
                    "M787 framebuffer size diverged");
                for (int pixel = 0; pixel < pages.length; pixel += 4) {
                    boolean differs = false;
                    for (int channel = 0; channel < 4; channel++) {
                        int delta = Math.abs((pages[pixel + channel] & 255)
                            - (direct[pixel + channel] & 255));
                        maximum = Math.max(maximum, delta);
                        differs |= delta != 0;
                    }
                    if (differs) changed++;
                }
                comparisons++;
            }
        }
        return new M787VisualOracle(changed, maximum, comparisons);
    }

    boolean passes() {
        return passes(80);
    }

    boolean passes(int expectedComparisons) {
        return changedPixels == 0L && maxDelta == 0 && comparisons == expectedComparisons;
    }

    String summary() {
        return "visual.changed.pixels=" + changedPixels + ",visual.max.delta=" + maxDelta
            + ",visual.comparisons=" + comparisons;
    }
}
