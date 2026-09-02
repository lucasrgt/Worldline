package worldline.testkit;
import worldline.testapi.PortalBlockSubsystemEvidence;
import worldline.testapi.PortalBlockSubsystemFixture;
import worldline.testapi.PortalBlockSubsystemObservation;

/** Contract tests for reusable portal-block subsystem evidence. */
public final class PortalBlockSubsystemFixtureTest {
    private PortalBlockSubsystemFixtureTest() { }
    public static void main(String[] arguments) { execute(); }
    static void execute() {
        PortalBlockSubsystemEvidence first = PortalBlockSubsystemFixture.execute(
                PortalBlockSubsystemFixtureTest::observation);
        PortalBlockSubsystemEvidence second = PortalBlockSubsystemFixture.execute(
                PortalBlockSubsystemFixtureTest::observation);
        require(first.equals(second) && first.hashCode() == second.hashCode(),
                "portal block evidence is not equatable");
        require(first.canonical().contains("claims=9|")
                && first.canonical().contains("frame-loss=6x90:0->air"),
                "canonical portal evidence drifted");
        rejects(() -> PortalBlockSubsystemFixture.execute(() -> invalidObservation()));
        System.out.println("portal block subsystem fixture tests passed");
    }
    private static PortalBlockSubsystemObservation observation() {
        return new PortalBlockSubsystemObservation("90=0,frames=X+Z,cells=6+6",
                "break=90:0->0:0,drop=none", "chunk-nbt=6x90:0",
                "collision=none,light=0:11", "scheduled=F,callback-stable=90:0,entities=0",
                "frame-loss=6x90:0->air");
    }
    private static PortalBlockSubsystemObservation invalidObservation() {
        PortalBlockSubsystemObservation value = observation();
        return new PortalBlockSubsystemObservation(value.domains(), value.lifecycle(),
                value.persistence(), value.physics().replace("0:11", "0:10"),
                value.timing(), value.neighbors());
    }
    private static void rejects(Runnable action) {
        try { action.run(); }
        catch (IllegalStateException expected) { return; }
        throw new AssertionError("invalid portal block evidence was accepted");
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
