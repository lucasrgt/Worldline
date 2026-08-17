package worldline.invariants;

/** Named rule checked against each observation. Fail closed on violation. */
public interface Invariant {
    String name();

    void observe(InvariantObservation observation);
}
