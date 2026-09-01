package worldline.testkit;

import java.util.Objects;
import worldline.api.MobObservationSession;
import worldline.api.RemoteDroppedItem;
import worldline.api.RemoteItemStack;
import worldline.api.RemoteMobDeath;
import worldline.api.RemoteMobMovement;
import worldline.api.RemoteMobSpawn;

/** Adapts the public protocol-14 mob boundary to the lifecycle TestKit fixture. */
public final class MobObservationEntityScenario implements EntityLifecycleScenario {
    private final MobObservationSession session;
    private final EntityKillAction killAction;

    public MobObservationEntityScenario(MobObservationSession session,
            EntityKillAction killAction) {
        this.session = Objects.requireNonNull(session, "session");
        this.killAction = Objects.requireNonNull(killAction, "killAction");
    }

    @Override public RemoteMobSpawn materialize(int expectedLegacyType) {
        return session.awaitMobSpawn(expectedLegacyType);
    }

    @Override public RemoteMobMovement awaitMovement(int entityId) {
        return session.awaitMobMovement(entityId);
    }

    @Override public void kill(int entityId) {
        killAction.kill(session, entityId);
    }

    @Override public RemoteMobDeath awaitDeath(int entityId) {
        return session.awaitMobDeath(entityId);
    }

    @Override public RemoteDroppedItem awaitDrop(RemoteItemStack expected) {
        return session.peekDroppedItem(expected);
    }
}
