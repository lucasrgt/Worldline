package worldline.testkit;
import worldline.testapi.FurnaceSubsystemEvidence;
import worldline.testapi.FurnaceSubsystemFixture;
import worldline.testapi.FurnaceSubsystemObservation;

/** Contract tests for reusable furnace subsystem evidence. */
public final class FurnaceSubsystemFixtureTest {
    private FurnaceSubsystemFixtureTest() {
    }
    public static void main(String[] arguments) {
        execute();
    }
    static void execute() {
        FurnaceSubsystemEvidence first = FurnaceSubsystemFixture.execute(
                FurnaceSubsystemFixtureTest::observation);
        FurnaceSubsystemEvidence second = FurnaceSubsystemFixture.execute(
                FurnaceSubsystemFixtureTest::observation);
        require(first.equals(second) && first.hashCode() == second.hashCode(),
                "furnace evidence is not equatable");
        require(first.canonical().contains("claims=11|idle:tick-policy+neighbor-response")
                && first.canonical().contains("tile=burn1600+cook200"),
                "canonical evidence drifted");
        rejects(() -> FurnaceSubsystemFixture.execute(() -> invalidObservation()));
        System.out.println("furnace subsystem fixture tests passed");
    }
    private static FurnaceSubsystemObservation observation() {
        return new FurnaceSubsystemObservation("61=2..5,62=2..5",
                "item61=61:2..5,smelt=61:4>62:4>61:3",
                "active=62:4->0:0,drops=61+12+263+20,saved=62:5+burn777+cook88",
                "collision=61:full+62:full,light=61:255:0+62:255:13",
                "random=FF,tile=burn1600+cook200,output=20x1:0",
                "stable=61:3+62:2,orientation=2..5");
    }
    private static FurnaceSubsystemObservation invalidObservation() {
        FurnaceSubsystemObservation value = observation();
        return new FurnaceSubsystemObservation(value.domains(), value.materialization(),
                value.lifecycle(), value.physics(), value.timing().replace("cook200", "cook199"),
                value.neighbors());
    }
    private static void rejects(Runnable action) {
        try { action.run(); }
        catch (IllegalStateException expected) { return; }
        throw new AssertionError("invalid furnace evidence was accepted");
    }
    private static void require(boolean condition, String message) {
        if (!condition)
            throw new AssertionError(message);
    }
}
