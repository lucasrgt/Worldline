package worldline.api;

import java.util.ArrayList;
import java.util.List;

/** Resolved session that continues each relative route step from the latest pose. */
public interface RecoveringMovementMultiplayerSession extends ResolvedMovementMultiplayerSession {
    default MovementRouteResult moveRoute(List<MovementStep> steps) {
        return moveRoute(steps, RouteCorrectionPolicy.CONTINUE);
    }

    default MovementRouteResult moveRoute(List<MovementStep> steps, RouteCorrectionPolicy policy) {
        if (steps == null || steps.isEmpty() || steps.size() > 64)
            throw new IllegalArgumentException("invalid movement route");
        if (policy == null) throw new IllegalArgumentException("null route correction policy");
        List<MovementOutcome> outcomes = new ArrayList<>(steps.size());
        for (MovementStep step : steps) {
            if (step == null) throw new IllegalArgumentException("null movement step");
            MovementOutcome outcome = moveAndObserve(
                    step.deltaX(), step.deltaY(), step.deltaZ(), step.ticks());
            outcomes.add(outcome);
            if (outcome.corrected() && policy == RouteCorrectionPolicy.STOP_ON_CORRECTION) break;
        }
        return new MovementRouteResult(outcomes);
    }

    default MovementRouteResult moveRouteWithFallback(List<MovementAlternative> alternatives) {
        if (alternatives == null || alternatives.isEmpty() || alternatives.size() > 32)
            throw new IllegalArgumentException("invalid movement alternatives");
        List<MovementOutcome> outcomes = new ArrayList<>(alternatives.size() * 2);
        for (MovementAlternative alternative : alternatives) {
            if (alternative == null) throw new IllegalArgumentException("null movement alternative");
            MovementStep step = alternative.primary(); MovementOutcome primary = moveAndObserve(
                    step.deltaX(), step.deltaY(), step.deltaZ(), step.ticks()); outcomes.add(primary);
            if (primary.corrected()) { step = alternative.fallback(); outcomes.add(moveAndObserve(
                    step.deltaX(), step.deltaY(), step.deltaZ(), step.ticks())); }
        }
        return new MovementRouteResult(outcomes);
    }
}
