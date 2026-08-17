package worldline.api;

import java.util.ArrayList;
import java.util.List;

/** Resolved session that continues each relative route step from the latest pose. */
public interface RecoveringMovementMultiplayerSession extends ResolvedMovementMultiplayerSession {
    default MovementRouteResult moveRoute(List<MovementStep> steps) {
        if (steps == null || steps.isEmpty() || steps.size() > 64)
            throw new IllegalArgumentException("invalid movement route");
        List<MovementOutcome> outcomes = new ArrayList<>(steps.size());
        for (MovementStep step : steps) {
            if (step == null) throw new IllegalArgumentException("null movement step");
            outcomes.add(moveAndObserve(step.deltaX(), step.deltaY(), step.deltaZ(), step.ticks()));
        }
        return new MovementRouteResult(outcomes);
    }
}
