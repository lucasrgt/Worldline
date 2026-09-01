package worldline.testkit;

import java.util.Objects;

/** Canonical public observation of the three native rail families. */
public final class RailNetworkSubsystemObservation {
    private final String normalRail;
    private final String poweredRail;
    private final String detectorRail;
    private final String support;

    public RailNetworkSubsystemObservation(String normalRail, String poweredRail,
            String detectorRail, String support) {
        this.normalRail = Objects.requireNonNull(normalRail, "normalRail");
        this.poweredRail = Objects.requireNonNull(poweredRail, "poweredRail");
        this.detectorRail = Objects.requireNonNull(detectorRail, "detectorRail");
        this.support = Objects.requireNonNull(support, "support");
    }

    public String normalRail() { return normalRail; }
    public String poweredRail() { return poweredRail; }
    public String detectorRail() { return detectorRail; }
    public String support() { return support; }

    @Override public boolean equals(Object other) {
        if (!(other instanceof RailNetworkSubsystemObservation))
            return false;
        RailNetworkSubsystemObservation value = (RailNetworkSubsystemObservation) other;
        return normalRail.equals(value.normalRail) && poweredRail.equals(value.poweredRail)
                && detectorRail.equals(value.detectorRail) && support.equals(value.support);
    }

    @Override public int hashCode() {
        return Objects.hash(normalRail, poweredRail, detectorRail, support);
    }
}
