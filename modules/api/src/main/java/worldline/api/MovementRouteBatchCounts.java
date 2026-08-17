package worldline.api;

import java.util.Objects;

/** Immutable aggregate counts over completed correlated route executions. */
public final class MovementRouteBatchCounts {
    private final int routes, outcomes, corrections;

    public MovementRouteBatchCounts(int routes, int outcomes, int corrections) {
        if (routes < 1 || routes > 16 || outcomes < routes || outcomes > routes * 64
                || corrections < 0 || corrections > outcomes)
            throw new IllegalArgumentException("invalid movement route batch counts");
        this.routes = routes; this.outcomes = outcomes; this.corrections = corrections;
    }

    public int routes() { return routes; }
    public int outcomes() { return outcomes; }
    public int corrections() { return corrections; }

    @Override public boolean equals(Object other) {
        if (!(other instanceof MovementRouteBatchCounts)) return false;
        MovementRouteBatchCounts value = (MovementRouteBatchCounts) other;
        return routes == value.routes && outcomes == value.outcomes && corrections == value.corrections;
    }
    @Override public int hashCode() { return Objects.hash(routes, outcomes, corrections); }
}
