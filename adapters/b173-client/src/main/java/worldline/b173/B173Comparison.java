package worldline.b173;

/** Immutable result of running two hypotheses from the same checkpoint. */
public final class B173Comparison {
    private final B173Observation baseline;
    private final B173Observation alternative;

    B173Comparison(B173Observation baseline, B173Observation alternative) {
        this.baseline = baseline;
        this.alternative = alternative;
    }

    public B173Observation baseline() { return baseline; }

    public B173Observation alternative() { return alternative; }

    public boolean diverged() {
        return !baseline.fingerprint().equals(alternative.fingerprint());
    }
}
