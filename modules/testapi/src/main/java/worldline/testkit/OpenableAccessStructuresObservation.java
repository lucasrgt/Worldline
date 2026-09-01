package worldline.testkit;

import java.util.Objects;

/** Canonical observation of chest, wooden-door, and trapdoor physical behavior. */
public final class OpenableAccessStructuresObservation {
    private final String chest, woodenDoor, trapdoor;

    public OpenableAccessStructuresObservation(String chest, String woodenDoor, String trapdoor) {
        this.chest = Objects.requireNonNull(chest, "chest");
        this.woodenDoor = Objects.requireNonNull(woodenDoor, "woodenDoor");
        this.trapdoor = Objects.requireNonNull(trapdoor, "trapdoor");
    }

    public String chest() { return chest; }
    public String woodenDoor() { return woodenDoor; }
    public String trapdoor() { return trapdoor; }

    @Override public boolean equals(Object other) {
        if (!(other instanceof OpenableAccessStructuresObservation)) return false;
        OpenableAccessStructuresObservation value =
                (OpenableAccessStructuresObservation) other;
        return chest.equals(value.chest) && woodenDoor.equals(value.woodenDoor)
                && trapdoor.equals(value.trapdoor);
    }

    @Override public int hashCode() { return Objects.hash(chest, woodenDoor, trapdoor); }
}
