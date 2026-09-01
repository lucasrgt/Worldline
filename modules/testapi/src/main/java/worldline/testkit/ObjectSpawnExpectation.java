package worldline.testkit;

import worldline.api.RemoteObjectSpawn;

/** Expected protocol-14 Packet23 identity with optional thrower and stationary constraints. */
public final class ObjectSpawnExpectation {
    private final int type;
    private final Integer throwerId;
    private final boolean stationary;

    private ObjectSpawnExpectation(int type, Integer throwerId, boolean stationary) {
        if (type < 1 || type > 127 || throwerId != null && throwerId.intValue() < 0) {
            throw new IllegalArgumentException("object spawn expectation");
        }
        this.type = type;
        this.throwerId = throwerId;
        this.stationary = stationary;
    }

    public static ObjectSpawnExpectation typeOnly(int type) {
        return new ObjectSpawnExpectation(type, null, false);
    }

    public static ObjectSpawnExpectation withThrower(int type, int throwerId) {
        return new ObjectSpawnExpectation(type, Integer.valueOf(throwerId), false);
    }

    public static ObjectSpawnExpectation stationary(int type, int throwerId) {
        return new ObjectSpawnExpectation(type, Integer.valueOf(throwerId), true);
    }

    public int type() { return type; }
    public boolean constrainsThrower() { return throwerId != null; }
    public int throwerId() {
        if (throwerId == null) throw new IllegalStateException("unconstrained thrower");
        return throwerId.intValue();
    }
    public boolean stationary() { return stationary; }

    public boolean matches(RemoteObjectSpawn spawn) {
        return spawn != null && spawn.entityId() > 0 && spawn.type() == type
                && (throwerId == null || spawn.throwerId() == throwerId.intValue())
                && (!stationary || spawn.velocityX() == 0 && spawn.velocityY() == 0
                && spawn.velocityZ() == 0);
    }

    String canonicalThrower() {
        if (throwerId == null) return "any";
        return throwerId.intValue() == 0 ? "zero" : "expected-positive";
    }
}
