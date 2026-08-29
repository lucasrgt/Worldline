package worldline.testkit;

/** Contract tests for the reusable two-block repeater subsystem evidence. */
public final class RepeaterSubsystemFixtureTest {
    private RepeaterSubsystemFixtureTest() { }

    public static void main(String[] arguments) { execute(); }

    static void execute() {
        RepeaterSubsystemEvidence first = RepeaterSubsystemFixture.execute(
                RepeaterSubsystemFixtureTest::observation);
        RepeaterSubsystemEvidence second = RepeaterSubsystemFixture.execute(
                RepeaterSubsystemFixtureTest::observation);
        require(first.equals(second) && first.hashCode() == second.hashCode(),
                "repeater subsystem evidence is not equatable");
        require(first.canonical().contains("claims=14|")
                && first.canonical().contains("delays=2+4+6+8")
                && first.canonical().contains("saved=93:0+94:15"),
                "repeater subsystem canonical evidence drifted");
        rejects(() -> RepeaterSubsystemFixture.execute(() -> invalidObservation()));
        System.out.println("repeater subsystem fixture tests passed");
    }

    private static RepeaterSubsystemObservation observation() {
        return new RepeaterSubsystemObservation(
                "93=0..15,94=0..15",
                "item356=93:0..15,signal=93>94:0..15",
                "on=94:15->0:0+drop=356x1:0,saved=93:0+94:15",
                "collision=93:1/8+94:1/8,light=93:0:0+94:0:9",
                "random=FF,delays=2+4+6+8,power=93>94,release=94>93,"
                        + "stable=93:15+94:15@20-window",
                "signal=all-directions,support=94:15->0:0+drop=356x1:0");
    }

    private static RepeaterSubsystemObservation invalidObservation() {
        RepeaterSubsystemObservation value = observation();
        return new RepeaterSubsystemObservation(value.domains(), value.materialization(),
                value.lifecycle(), value.physics(), value.timing().replace("2+4+6+8", "2+4+6"),
                value.neighbors());
    }

    private static void rejects(Runnable action) {
        try { action.run(); }
        catch (IllegalStateException expected) { return; }
        throw new AssertionError("invalid repeater subsystem evidence was accepted");
    }
    private static void require(boolean condition, String message) {
        if (!condition)
            throw new AssertionError(message);
    }
}
