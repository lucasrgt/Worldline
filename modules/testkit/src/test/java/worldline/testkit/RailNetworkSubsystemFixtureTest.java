package worldline.testkit;

/** Contract test for stable rail-network evidence. */
public final class RailNetworkSubsystemFixtureTest {
    private RailNetworkSubsystemFixtureTest() { }

    public static void execute() {
        RailNetworkSubsystemEvidence first = RailNetworkSubsystemFixture.execute(
                RailNetworkSubsystemFixtureTest::observation);
        RailNetworkSubsystemEvidence second = RailNetworkSubsystemFixture.execute(
                RailNetworkSubsystemFixtureTest::observation);
        if (!first.equals(second) || first.hashCode() != second.hashCode()
                || !first.canonical().contains("claims=14|"))
            throw new AssertionError("rail-network evidence is unstable");
        rejects(() -> RailNetworkSubsystemFixture.execute(() ->
                new RailNetworkSubsystemObservation("wrong", observation().poweredRail(),
                        observation().detectorRail(), observation().support())));
    }

    private static RailNetworkSubsystemObservation observation() {
        return new RailNetworkSubsystemObservation(
                "states=0-9,bounds=0:20+2:100,collision=none,light=0/0,tick=stable",
                "states=0-5+8-13,bounds=2:100+10:20,collision=none,light=0/0,tick=stable",
                "states=0>8>0,bounds=2:100+10:20,collision=none,light=0/0,tick=20",
                "27+28+66=air+single-drop");
    }

    private static void rejects(Runnable action) {
        try {
            action.run();
            throw new AssertionError("invalid rail-network evidence was accepted");
        } catch (IllegalStateException expected) {
            // Expected.
        }
    }
}
