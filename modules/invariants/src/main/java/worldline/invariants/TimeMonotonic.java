package worldline.invariants;

import worldline.api.InvariantViolation;

/** World time may stay or advance. It may not move backward. */
public final class TimeMonotonic implements Invariant {
    public static final String NAME = "time-monotonic";
    private Long previous;

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void observe(InvariantObservation observation) {
        if (observation == null) throw new NullPointerException("observation");
        long time = observation.time();
        if (previous != null && time < previous.longValue()) {
            throw new InvariantViolation(NAME, "time moved from " + previous + " to " + time);
        }
        previous = Long.valueOf(time);
    }
}
