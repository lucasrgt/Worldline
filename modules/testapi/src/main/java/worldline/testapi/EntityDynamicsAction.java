package worldline.testapi;

import worldline.api.MobObservationSession;

/** Driver callback that executes one controlled entity-dynamics scene. */
@FunctionalInterface
public interface EntityDynamicsAction {
    EntityDynamicsObservation observe(MobObservationSession session, EntityDynamicsScene scene);
}
