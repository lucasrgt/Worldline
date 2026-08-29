package worldline.testkit;

import java.util.Objects;
import worldline.api.WorldlineBehavior;
import worldline.api.WorldlineWorldBehaviors;

/** Validates the reusable Beta 1.7.3 rail-network fixture. */
public final class RailNetworkSubsystemFixture {
    private static final String NORMAL =
            "states=0-9,bounds=0:20+2:100,collision=none,light=0/0,tick=stable";
    private static final String POWERED =
            "states=0-5+8-13,bounds=2:100+10:20,collision=none,light=0/0,tick=stable";
    private static final String DETECTOR =
            "states=0>8>0,bounds=2:100+10:20,collision=none,light=0/0,tick=20";
    private static final String SUPPORT = "27+28+66=air+single-drop";

    private RailNetworkSubsystemFixture() { }

    public static RailNetworkSubsystemEvidence execute(RailNetworkSubsystemScenario scenario) {
        RailNetworkSubsystemObservation actual = Objects.requireNonNull(
                Objects.requireNonNull(scenario, "scenario").observe(), "observation");
        expect(NORMAL, actual.normalRail(), "normal rail");
        expect(POWERED, actual.poweredRail(), "powered rail");
        expect(DETECTOR, actual.detectorRail(), "detector rail");
        expect(SUPPORT, actual.support(), "support response");
        if (WorldlineBehavior.require("rail-network-subsystem")
                != WorldlineWorldBehaviors.RAIL_NETWORK_SUBSYSTEM)
            throw new IllegalStateException("rail-network registration drifted");
        return new RailNetworkSubsystemEvidence(actual);
    }

    private static void expect(String expected, String actual, String label) {
        if (!expected.equals(actual))
            throw new IllegalStateException(label + " drifted: " + actual);
    }
}
