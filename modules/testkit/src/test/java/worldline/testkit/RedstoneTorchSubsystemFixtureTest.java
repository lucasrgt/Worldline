package worldline.testkit;
import worldline.testapi.RedstoneTorchSubsystemEvidence;
import worldline.testapi.RedstoneTorchSubsystemFixture;
import worldline.testapi.RedstoneTorchSubsystemObservation;

/** Contract tests for reusable redstone torch subsystem evidence. */
public final class RedstoneTorchSubsystemFixtureTest {
    private RedstoneTorchSubsystemFixtureTest() { }
    public static void main(String[] arguments) { execute(); }
    static void execute() {
        RedstoneTorchSubsystemEvidence first = RedstoneTorchSubsystemFixture.execute(
                RedstoneTorchSubsystemFixtureTest::observation);
        RedstoneTorchSubsystemEvidence second = RedstoneTorchSubsystemFixture.execute(
                RedstoneTorchSubsystemFixtureTest::observation);
        require(first.equals(second) && first.hashCode() == second.hashCode(),
                "redstone torch evidence is not equatable");
        require(first.canonical().contains("claims=13|")
                && first.canonical().contains("burnout=8@100"), "canonical evidence drifted");
        rejects(() -> RedstoneTorchSubsystemFixture.execute(() -> invalidObservation()));
        System.out.println("redstone torch subsystem fixture tests passed");
    }
    private static RedstoneTorchSubsystemObservation observation() {
        return new RedstoneTorchSubsystemObservation("75=1..5,76=1..5",
                "item76=76:1..5,signal=76>75:1..5",
                "off=75:5->0:0+drop=76x1:0,saved=75:1+76:5",
                "collision=75:none+76:none,light=75:0:0+76:0:7",
                "random=TT,delay=2,invert=76>75>76,burnout=8@100,recovery=101+2",
                "faces=1..5,support=75:5->0:0+drop=76x1:0");
    }
    private static RedstoneTorchSubsystemObservation invalidObservation() {
        RedstoneTorchSubsystemObservation value = observation();
        return new RedstoneTorchSubsystemObservation(value.domains(), value.materialization(),
                value.lifecycle(), value.physics(), value.timing().replace("8@100", "7@100"),
                value.neighbors());
    }
    private static void rejects(Runnable action) {
        try { action.run(); }
        catch (IllegalStateException expected) { return; }
        throw new AssertionError("invalid redstone torch evidence was accepted");
    }
    private static void require(boolean condition, String message) {
        if (!condition)
            throw new AssertionError(message);
    }
}
