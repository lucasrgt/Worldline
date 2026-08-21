package worldline.b173server;

import java.util.Objects;

/** Immutable protocol-14 Packet39 entity-attach observation. */
public final class B173VehicleAttach {
    private final int passengerId, vehicleId;

    public B173VehicleAttach(int passengerId, int vehicleId) {
        if (passengerId < 0) throw new IllegalArgumentException("invalid passenger entity id");
        if (vehicleId < -1) throw new IllegalArgumentException("invalid vehicle entity id");
        this.passengerId = passengerId; this.vehicleId = vehicleId;
    }

    public int passengerId() { return passengerId; }
    public int vehicleId() { return vehicleId; }

    @Override public boolean equals(Object other) {
        if (!(other instanceof B173VehicleAttach)) return false;
        B173VehicleAttach value = (B173VehicleAttach) other;
        return passengerId == value.passengerId && vehicleId == value.vehicleId;
    }

    @Override public int hashCode() { return Objects.hash(passengerId, vehicleId); }
}
