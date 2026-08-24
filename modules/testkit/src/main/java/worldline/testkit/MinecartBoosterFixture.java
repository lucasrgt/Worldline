package worldline.testkit;

import java.util.Objects;
import worldline.api.RemoteObjectMovement;
import worldline.api.RemoteObjectSpawn;

/** Reusable evidence boundary for the classic parallel-track minecart booster. */
public final class MinecartBoosterFixture {
    private MinecartBoosterFixture() { }

    public static Evidence observe(RemoteObjectSpawn driver, RemoteObjectSpawn booster,
            RemoteObjectMovement driverMove, RemoteObjectMovement boosterMove,
            int axisX, int axisZ) {
        if (driver == null || booster == null || driverMove == null || boosterMove == null)
            throw new IllegalArgumentException("null minecart booster evidence");
        if ((axisX == 0) == (axisZ == 0) || Math.abs(axisX) > 1 || Math.abs(axisZ) > 1)
            throw new IllegalArgumentException("invalid booster axis");
        require(driver.type() == 10 && booster.type() == 10
                && driver.entityId() != booster.entityId(), "distinct empty minecarts required");
        int lateral = axisX == 0
                ? Math.abs(driver.fixedX() - booster.fixedX())
                : Math.abs(driver.fixedZ() - booster.fixedZ());
        int longitudinal = axisX == 0
                ? Math.abs(driver.fixedZ() - booster.fixedZ())
                : Math.abs(driver.fixedX() - booster.fixedX());
        require(lateral == 32 && longitudinal == 0, "minecarts are not on adjacent parallel rails");
        int driverAdvance = advance(driver, driverMove, axisX, axisZ);
        int boosterAdvance = advance(booster, boosterMove, axisX, axisZ);
        require(driverAdvance > 0 && boosterAdvance > 0,
                "parallel cart did not receive forward booster motion");
        return new Evidence(axisX, axisZ, lateral, driverAdvance > 0, boosterAdvance > 0);
    }

    private static int advance(RemoteObjectSpawn spawn, RemoteObjectMovement movement,
            int axisX, int axisZ) {
        require(spawn.entityId() == movement.entityId(), "movement entity does not match spawn");
        return (movement.toFixedX() - spawn.fixedX()) * axisX
                + (movement.toFixedZ() - spawn.fixedZ()) * axisZ;
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }

    public static final class Evidence {
        private final int axisX, axisZ, lateralFixed;
        private final boolean driverForward, boosterForward;
        Evidence(int axisX, int axisZ, int lateralFixed,
                boolean driverForward, boolean boosterForward) {
            this.axisX = axisX; this.axisZ = axisZ; this.lateralFixed = lateralFixed;
            this.driverForward = driverForward; this.boosterForward = boosterForward;
        }
        public int axisX() { return axisX; }
        public int axisZ() { return axisZ; }
        public int lateralFixed() { return lateralFixed; }
        public boolean driverForward() { return driverForward; }
        public boolean boosterForward() { return boosterForward; }
        @Override public boolean equals(Object other) {
            if (!(other instanceof Evidence)) return false;
            Evidence value = (Evidence) other;
            return axisX == value.axisX && axisZ == value.axisZ
                    && lateralFixed == value.lateralFixed
                    && driverForward == value.driverForward
                    && boosterForward == value.boosterForward;
        }
        @Override public int hashCode() {
            return Objects.hash(axisX, axisZ, lateralFixed, driverForward, boosterForward);
        }
    }
}
