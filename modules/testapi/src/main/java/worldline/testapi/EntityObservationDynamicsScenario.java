package worldline.testapi;

import java.util.Objects;
import worldline.api.MobObservationSession;

/** Connects public mob observation to a controlled entity-dynamics driver callback. */
public final class EntityObservationDynamicsScenario implements EntityDynamicsScenario {
    private final MobObservationSession session;
    private final EntityDynamicsAction action;

    public EntityObservationDynamicsScenario(MobObservationSession session,
            EntityDynamicsAction action) {
        this.session = Objects.requireNonNull(session, "session");
        this.action = Objects.requireNonNull(action, "action");
    }

    @Override public EntityDynamicsObservation observe(EntityDynamicsScene scene) {
        return action.observe(session, scene);
    }
}
