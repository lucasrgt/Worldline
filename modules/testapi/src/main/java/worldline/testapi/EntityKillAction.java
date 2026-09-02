package worldline.testapi;

import worldline.api.MobObservationSession;

/** Driver action that reaches one causally prepared mob-death boundary. */
@FunctionalInterface
public interface EntityKillAction {
    void kill(MobObservationSession session, int entityId);
}
