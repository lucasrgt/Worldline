package worldline.testapi;

import worldline.api.PaintingObservationSession;
import worldline.api.RemotePaintingSpawn;

/** Gameplay callback that removes the support associated with an observed painting. */
@FunctionalInterface
public interface PaintingSupportAction {
    void remove(PaintingObservationSession session, RemotePaintingSpawn painting);
}
