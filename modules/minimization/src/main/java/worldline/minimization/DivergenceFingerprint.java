package worldline.minimization;

import worldline.analysis.TraceDiff;

/** Exact immutable predicate captured from a first-divergence report. */
public final class DivergenceFingerprint {
    private final String report;

    private DivergenceFingerprint(String report) { this.report = report; }

    public static DivergenceFingerprint from(TraceDiff difference) {
        if (difference == null) throw new NullPointerException("difference");
        if (!difference.diverged()) throw new IllegalArgumentException("a divergence is required");
        return new DivergenceFingerprint(difference.render());
    }

    public boolean matches(TraceDiff difference) {
        return difference != null && difference.diverged() && report.equals(difference.render());
    }

    public String render() { return report; }

    @Override public boolean equals(Object other) {
        return other instanceof DivergenceFingerprint
                && report.equals(((DivergenceFingerprint) other).report);
    }
    @Override public int hashCode() { return report.hashCode(); }
    @Override public String toString() { return "DivergenceFingerprint[" + report.replace('\n', ',') + "]"; }
}
