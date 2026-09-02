package worldline.testkit;
import worldline.testapi.PistonSubsystemEvidence;
import worldline.testapi.PistonSubsystemFixture;
import worldline.testapi.PistonSubsystemObservation;

/** Contract tests for the reusable four-block piston subsystem evidence. */
public final class PistonSubsystemFixtureTest {
    private PistonSubsystemFixtureTest() { }

    public static void main(String[] arguments) { execute(); }

    static void execute() {
        PistonSubsystemEvidence first = PistonSubsystemFixture.execute(
                PistonSubsystemFixtureTest::observation);
        PistonSubsystemEvidence second = PistonSubsystemFixture.execute(
                PistonSubsystemFixtureTest::observation);
        require(first.equals(second) && first.hashCode() == second.hashCode(),
                "piston subsystem evidence is not equatable");
        require(first.canonical().contains("claims=28|")
                && first.canonical().contains("moving=36:5+te=34:5:5:true")
                && first.canonical().contains("head=2:plate+rod"),
                "piston subsystem canonical evidence drifted");
        rejects(() -> PistonSubsystemFixture.execute(() -> invalidObservation()));
        System.out.println("piston subsystem fixture tests passed");
    }

    private static PistonSubsystemObservation observation() {
        return new PistonSubsystemObservation(
                "29=0..5+8..13,33=0..5+8..13,34=0..5+8..13,36=0..5+8..13",
                "normal=33:5>36:5>34:5,sticky=29:5>36:13>34:13,items=34:none+36:none",
                "head=34:5->0:0+base=33:13->0:0+drop=33x1:0,"
                        + "moving=36:0->0:0+drop=4x1:0",
                "head=34:5,moving=36:5+te=34:5:5:true",
                "base=1:full,head=2:plate+rod,moving=1:translated",
                "29=0:0,33=0:0,34=0:0,36=0:0",
                "random=FFFF,idle=33:5+29:5+34:5@20-window,moving=36:5->34:5@3-te",
                "normal=33:5->13->5,sticky=29:5->13->5,"
                        + "head=34:5->0:0,moving-te=held");
    }

    private static PistonSubsystemObservation invalidObservation() {
        PistonSubsystemObservation value = observation();
        return new PistonSubsystemObservation(value.domains(), value.materialization(),
                value.breakAndDrops(), value.persistence(), value.collision(), value.light(),
                "random=TFFF", value.neighbors());
    }

    private static void rejects(Runnable action) {
        try { action.run(); }
        catch (IllegalStateException expected) { return; }
        throw new AssertionError("invalid piston subsystem evidence was accepted");
    }
    private static void require(boolean condition, String message) {
        if (!condition)
            throw new AssertionError(message);
    }
}
