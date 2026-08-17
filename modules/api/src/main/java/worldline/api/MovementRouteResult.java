package worldline.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Immutable ordered outcomes from a route that continues after corrections. */
public final class MovementRouteResult {
    private final List<MovementOutcome> outcomes;

    public MovementRouteResult(List<MovementOutcome> outcomes) {
        if (outcomes == null || outcomes.isEmpty() || outcomes.size() > 64)
            throw new IllegalArgumentException("invalid route outcomes");
        ArrayList<MovementOutcome> copy = new ArrayList<>(outcomes.size());
        for (MovementOutcome outcome : outcomes) {
            if (outcome == null) throw new IllegalArgumentException("null route outcome");
            copy.add(outcome);
        }
        this.outcomes = Collections.unmodifiableList(copy);
    }

    public List<MovementOutcome> outcomes() { return outcomes; }
    public PlayerPose finalPose() { return outcomes.get(outcomes.size() - 1).resulting(); }
    public int corrections() { int count = 0;
        for (MovementOutcome outcome : outcomes) if (outcome.corrected()) count++; return count; }
}
