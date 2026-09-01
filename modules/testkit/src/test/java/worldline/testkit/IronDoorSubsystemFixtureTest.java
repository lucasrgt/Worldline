package worldline.testkit;

/** Locks the public iron-door mini-subsystem contract. */
public final class IronDoorSubsystemFixtureTest {
    private IronDoorSubsystemFixtureTest() { }
    public static void execute() {
        IronDoorSubsystemObservation observation = new IronDoorSubsystemObservation(
                "71=lower:0..7,upper:8..15,open-bit=4",
                "break=lower+upper->air,drops=lower:330x1+upper:none,strength=finite",
                "collision=closed-x-3/16+open-z-3/16,opaque=F,cube=F,light=0:0",
                "scheduled=F,callback-stable=71:0+71:8",
                "paired=stable,orphan-lower=air+330x1,orphan-upper=air+none,"
                        + "support-loss=both-air+330x1");
        IronDoorSubsystemEvidence first = IronDoorSubsystemFixture.execute(() -> observation);
        IronDoorSubsystemEvidence second = IronDoorSubsystemFixture.execute(() -> observation);
        require(first.equals(second) && first.hashCode() == second.hashCode(),
                "iron-door evidence equality drifted");
        require(first.canonical().contains("claims=7|")
                && first.canonical().contains("drops=lower:330x1+upper:none"),
                "iron-door evidence inventory drifted");
        System.out.println("iron-door subsystem fixture tests passed");
    }
    private static void require(boolean value, String message) {
        if (!value)
            throw new AssertionError(message);
    }
}
