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
        return moveRouteWithFallback(alternatives, MovementRouteObserver.NONE);
    }

    default MovementRouteResult moveRouteWithFallback(List<MovementAlternative> alternatives,
            MovementRouteObserver observer) {
        if (observer == null) throw new IllegalArgumentException("null movement route observer");
        return moveRouteWithFallbackUntil(alternatives, event -> {
            observer.observe(event); return MovementRouteDirective.CONTINUE;
        });
    }

    default MovementRouteResult moveRouteWithFallbackUntil(List<MovementAlternative> alternatives,
            MovementRouteController controller) {
        return moveRouteWithFallbackExecution(alternatives, controller).result();
    }

    default MovementRouteExecution moveRouteWithFallbackExecution(List<MovementAlternative> alternatives,
            MovementRouteController controller) {
        if (alternatives == null || alternatives.isEmpty() || alternatives.size() > 32)
            throw new IllegalArgumentException("invalid movement alternatives");
        if (controller == null) throw new IllegalArgumentException("null movement route controller");
        List<MovementOutcome> outcomes = new ArrayList<>(alternatives.size() * 2);
        boolean stopped = false; MovementRouteEvent terminal = null;
        for (int index = 0; index < alternatives.size(); index++) {
            MovementAlternative alternative = alternatives.get(index);
            if (alternative == null) throw new IllegalArgumentException("null movement alternative");
            MovementStep step = alternative.primary(); MovementOutcome primary = moveAndObserve(
                    step.deltaX(), step.deltaY(), step.deltaZ(), step.ticks()); outcomes.add(primary);
            terminal = new MovementRouteEvent(
                    index, outcomes.size() - 1, MovementAttemptKind.PRIMARY, primary);
            MovementRouteDirective directive = controller.after(terminal);
            if (directive == null) throw new IllegalStateException("null movement route directive");
            stopped = directive == MovementRouteDirective.STOP;
            if (stopped) break;
            if (primary.corrected()) { step = alternative.fallback(); MovementOutcome fallback = moveAndObserve(
                    step.deltaX(), step.deltaY(), step.deltaZ(), step.ticks()); outcomes.add(fallback);
                terminal = new MovementRouteEvent(index, outcomes.size() - 1,
                        MovementAttemptKind.FALLBACK, fallback);
                directive = controller.after(terminal);
                if (directive == null) throw new IllegalStateException("null movement route directive");
                stopped = directive == MovementRouteDirective.STOP; }
            if (stopped) break;
        }
        return new MovementRouteExecution(new MovementRouteResult(outcomes), stopped
                ? MovementRouteTermination.CONTROLLER_STOP : MovementRouteTermination.EXHAUSTED, terminal);
    }

    default CorrelatedMovementRouteExecution moveRouteWithFallbackCorrelated(
            List<MovementAlternative> alternatives, Object correlation,
            CorrelatedMovementRouteController controller) {
        if (correlation == null) throw new IllegalArgumentException("null movement route correlation");
        if (controller == null) throw new IllegalArgumentException("null correlated route controller");
        final CorrelatedMovementRouteEvent[] terminal = new CorrelatedMovementRouteEvent[1];
        MovementRouteExecution execution = moveRouteWithFallbackExecution(alternatives, event -> {
            CorrelatedMovementRouteEvent correlated = new CorrelatedMovementRouteEvent(correlation, event);
            terminal[0] = correlated; return controller.after(correlated);
        });
        return new CorrelatedMovementRouteExecution(correlation, execution, terminal[0]);
    }

    default CorrelatedMovementRouteBatchResult moveCorrelatedRouteBatch(
            List<CorrelatedMovementRoutePlan> plans, CorrelatedMovementRouteController routeController,
            CorrelatedMovementRouteBatchController batchController) {
        if (plans == null || plans.isEmpty() || plans.size() > 16)
            throw new IllegalArgumentException("invalid correlated route batch");
        if (routeController == null || batchController == null)
            throw new IllegalArgumentException("null correlated route batch controller");
        List<CorrelatedMovementRouteExecution> executions = new ArrayList<>(plans.size());
        boolean stopped = false;
        for (CorrelatedMovementRoutePlan plan : plans) {
            if (plan == null) throw new IllegalArgumentException("null correlated route plan");
            CorrelatedMovementRouteExecution execution = moveRouteWithFallbackCorrelated(
                    plan.alternatives(), plan.correlation(), routeController);
            executions.add(execution); MovementRouteDirective directive = batchController.after(execution);
            if (directive == null) throw new IllegalStateException("null movement route batch directive");
            if (directive == MovementRouteDirective.STOP) { stopped = true; break; }
        }
        return new CorrelatedMovementRouteBatchResult(executions, stopped
                ? MovementRouteBatchTermination.CONTROLLER_STOP : MovementRouteBatchTermination.EXHAUSTED);
    }
}
