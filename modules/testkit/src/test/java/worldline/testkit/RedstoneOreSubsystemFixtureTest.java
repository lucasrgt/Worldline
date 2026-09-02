package worldline.testkit;
import worldline.testapi.RedstoneOreSubsystemEvidence;
import worldline.testapi.RedstoneOreSubsystemFixture;
import worldline.testapi.RedstoneOreSubsystemObservation;

/** Contract tests for reusable redstone-ore subsystem evidence. */
public final class RedstoneOreSubsystemFixtureTest {
    private RedstoneOreSubsystemFixtureTest() { }
    public static void main(String[] arguments) { execute(); }
    static void execute() {
        RedstoneOreSubsystemEvidence first = RedstoneOreSubsystemFixture.execute(
                RedstoneOreSubsystemFixtureTest::observation);
        RedstoneOreSubsystemEvidence second = RedstoneOreSubsystemFixture.execute(
                RedstoneOreSubsystemFixtureTest::observation);
        require(first.equals(second) && first.hashCode() == second.hashCode(),
                "redstone ore evidence is not equatable");
        require(first.canonical().contains("claims=13|")
                && first.canonical().contains("fade=74:0->73:0"),
                "canonical evidence drifted");
        rejects(() -> RedstoneOreSubsystemFixture.execute(() -> invalidObservation()));
        System.out.println("redstone ore subsystem fixture tests passed");
    }
    private static RedstoneOreSubsystemObservation observation() {
        return new RedstoneOreSubsystemObservation("73+74=same-BlockRedstoneOre",
                "73=0,74=0,activate=73:0->74:0",
                "break=74:0->0:0,drop=331x4..5:0,saved=73:0+74:0",
                "collision=73:full+74:full,light=73:255:0+74:255:9",
                "random=FT,activate=click,fade=74:0->73:0",
                "73:0+74:0=stable@1+69");
    }
    private static RedstoneOreSubsystemObservation invalidObservation() {
        RedstoneOreSubsystemObservation value = observation();
        return new RedstoneOreSubsystemObservation(value.registry(), value.domains(),
                value.lifecycle(), value.physics().replace("255:9", "255:8"),
                value.timing(), value.neighbors());
    }
    private static void rejects(Runnable action) {
        try { action.run(); }
        catch (IllegalStateException expected) { return; }
        throw new AssertionError("invalid redstone ore evidence was accepted");
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
