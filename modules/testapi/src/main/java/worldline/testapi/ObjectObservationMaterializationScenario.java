package worldline.testapi;

import java.util.Objects;
import worldline.api.ObjectObservationSession;
import worldline.api.RemoteObjectSpawn;

/** Adapts the public object-observation session to the materialization fixture. */
public final class ObjectObservationMaterializationScenario
        implements ObjectMaterializationScenario {
    private final ObjectObservationSession session;

    public ObjectObservationMaterializationScenario(ObjectObservationSession session) {
        this.session = Objects.requireNonNull(session, "session");
    }

    @Override public RemoteObjectSpawn materialize(int expectedType) {
        return session.awaitObjectSpawn(expectedType);
    }
}
