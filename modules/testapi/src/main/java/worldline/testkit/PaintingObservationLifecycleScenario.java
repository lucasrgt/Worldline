package worldline.testkit;

import java.util.Objects;
import worldline.api.PaintingObservationSession;
import worldline.api.RemoteDroppedItem;
import worldline.api.RemoteItemStack;
import worldline.api.RemotePaintingSpawn;

/** Adapts the public protocol-14 painting boundary to the lifecycle fixture. */
public final class PaintingObservationLifecycleScenario implements PaintingLifecycleScenario {
    private final PaintingObservationSession session;
    private final PaintingSupportAction supportAction;

    public PaintingObservationLifecycleScenario(PaintingObservationSession session,
            PaintingSupportAction supportAction) {
        this.session = Objects.requireNonNull(session, "session");
        this.supportAction = Objects.requireNonNull(supportAction, "supportAction");
    }

    @Override public RemotePaintingSpawn materialize(PaintingSpawnExpectation expectation) {
        return session.awaitPaintingSpawn();
    }

    @Override public void removeSupport(RemotePaintingSpawn painting) {
        supportAction.remove(session, painting);
    }

    @Override public int awaitDestroy(int entityId) {
        return session.awaitPaintingDestroy(entityId);
    }

    @Override public RemoteDroppedItem awaitDrop(RemoteItemStack expected) {
        return session.peekDroppedItem(expected);
    }
}
