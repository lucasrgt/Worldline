package worldline.testkit;

import java.util.Objects;

/** Canonical public observation of lever, button, and pressure-plate behavior. */
public final class RedstoneInputControlsSubsystemObservation {
    private final String lever;
    private final String button;
    private final String stonePlate;
    private final String woodenPlate;
    private final String support;

    public RedstoneInputControlsSubsystemObservation(String lever, String button,
            String stonePlate, String woodenPlate, String support) {
        this.lever = Objects.requireNonNull(lever, "lever");
        this.button = Objects.requireNonNull(button, "button");
        this.stonePlate = Objects.requireNonNull(stonePlate, "stonePlate");
        this.woodenPlate = Objects.requireNonNull(woodenPlate, "woodenPlate");
        this.support = Objects.requireNonNull(support, "support");
    }

    public String lever() { return lever; }
    public String button() { return button; }
    public String stonePlate() { return stonePlate; }
    public String woodenPlate() { return woodenPlate; }
    public String support() { return support; }

    @Override public boolean equals(Object other) {
        if (!(other instanceof RedstoneInputControlsSubsystemObservation))
            return false;
        RedstoneInputControlsSubsystemObservation value =
                (RedstoneInputControlsSubsystemObservation) other;
        return lever.equals(value.lever) && button.equals(value.button)
                && stonePlate.equals(value.stonePlate)
                && woodenPlate.equals(value.woodenPlate) && support.equals(value.support);
    }

    @Override public int hashCode() {
        return Objects.hash(lever, button, stonePlate, woodenPlate, support);
    }
}
