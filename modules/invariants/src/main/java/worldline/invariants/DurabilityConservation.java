package worldline.invariants;

import worldline.api.InvariantViolation;
import worldline.api.WearCensus;

/**
 * Damageable stacks may wear or disappear. They may not lose damage points
 * while keeping the same or fewer stacks of that item ID.
 */
public final class DurabilityConservation implements Invariant {
    public static final String NAME = "durability-conservation";
    private WearCensus previous;

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void observe(InvariantObservation observation) {
        if (observation == null) throw new NullPointerException("observation");
        WearCensus current = observation.wear();
        if (previous == null) {
            previous = current;
            return;
        }
        if (current.repairedVersus(previous)) {
            throw new InvariantViolation(NAME, "tool damage decreased without a new stack");
        }
        previous = current;
    }
}
