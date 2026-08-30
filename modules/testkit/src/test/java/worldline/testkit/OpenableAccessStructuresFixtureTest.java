package worldline.testkit;

/** Contract tests for the public openable access-structures fixture. */
public final class OpenableAccessStructuresFixtureTest {
    private OpenableAccessStructuresFixtureTest() { }

    public static void execute() {
        OpenableAccessStructuresEvidence first = OpenableAccessStructuresFixture.execute(
                OpenableAccessStructuresFixtureTest::observation);
        OpenableAccessStructuresEvidence second = OpenableAccessStructuresFixture.execute(
                OpenableAccessStructuresFixtureTest::observation);
        require(first.equals(second), "openable access evidence equality drifted");
        require(first.canonical().contains("claims=10|"), "claim inventory drifted");
        rejects(() -> OpenableAccessStructuresFixture.execute(() ->
                new OpenableAccessStructuresObservation("wrong", observation().woodenDoor(),
                        observation().trapdoor())));
    }

    private static OpenableAccessStructuresObservation observation() {
        return new OpenableAccessStructuresObservation(
                "54:scheduled=F+callback-stable+neighbor-stable",
                "64:collision=closed-x-3/16+open-z-3/16,light=0:0,"
                        + "scheduled=F+callback-stable",
                "96:meta=0..7,collision=closed-floor-3/16+open-four-faces,light=0:0,"
                        + "scheduled=F+callback-stable,"
                        + "neighbor=support-stable+support-loss-air+96x1");
    }

    private static void rejects(Runnable action) {
        try { action.run(); }
        catch (IllegalStateException expected) { return; }
        throw new IllegalStateException("invalid openable access observation was accepted");
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
