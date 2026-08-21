package worldline.api;

/** Sustained session that classifies the bounded server response to movement. */
public interface ResolvedMovementMultiplayerSession extends SustainedRemoteWorldMultiplayerSession {
    MovementOutcome moveAndObserve(double deltaX, double deltaY, double deltaZ, int ticks);
}
