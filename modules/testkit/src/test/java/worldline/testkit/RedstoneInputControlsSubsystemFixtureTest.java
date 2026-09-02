package worldline.testkit;
import worldline.testapi.RedstoneInputControlsSubsystemEvidence;
import worldline.testapi.RedstoneInputControlsSubsystemFixture;
import worldline.testapi.RedstoneInputControlsSubsystemObservation;

/** Contract test for stable redstone input-control evidence. */
public final class RedstoneInputControlsSubsystemFixtureTest {
    private RedstoneInputControlsSubsystemFixtureTest() { }

    public static void execute() {
        RedstoneInputControlsSubsystemEvidence first =
                RedstoneInputControlsSubsystemFixture.execute(
                        RedstoneInputControlsSubsystemFixtureTest::observation);
        RedstoneInputControlsSubsystemEvidence second =
                RedstoneInputControlsSubsystemFixture.execute(
                        RedstoneInputControlsSubsystemFixtureTest::observation);
        if (!first.equals(second) || first.hashCode() != second.hashCode()
                || !first.canonical().contains("claims=20|"))
            throw new AssertionError("redstone input-control evidence is unstable");
        rejects(() -> RedstoneInputControlsSubsystemFixture.execute(() ->
                new RedstoneInputControlsSubsystemObservation("wrong", observation().button(),
                        observation().stonePlate(), observation().woodenPlate(),
                        observation().support())));
    }

    private static RedstoneInputControlsSubsystemObservation observation() {
        return new RedstoneInputControlsSubsystemObservation(
                "state=5>13>5,bounds=40.0.40.120.96.120,collision=none,light=0/0,tick=latch",
                "state=1>9>1,bounds=0.60.50.20.100.110>0.60.50.10.100.110,collision=none,light=0/0,tick=20",
                "state=0>1>0,item=ignored,bounds=10.0.10.150.10.150>10.0.10.150.5.150,collision=none,light=0/0,tick=20",
                "state=0>1>0,item=accepted,bounds=10.0.10.150.10.150>"
                        + "10.0.10.150.5.150,collision=none,light=0/0,tick=20",
                "69+70+72+77=air+single-drop");
    }

    private static void rejects(Runnable action) {
        try {
            action.run();
            throw new AssertionError("invalid redstone input-control evidence was accepted");
        } catch (IllegalStateException expected) {
            // Expected.
        }
    }
}
