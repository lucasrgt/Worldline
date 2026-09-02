package worldline.testkit;
import worldline.testapi.BedSubsystemEvidence;
import worldline.testapi.BedSubsystemFixture;
import worldline.testapi.BedSubsystemObservation;

/** Locks the public bed mini-subsystem contract. */
public final class BedSubsystemFixtureTest {
    private BedSubsystemFixtureTest() { }
    public static void execute() {
        BedSubsystemObservation observation = new BedSubsystemObservation(
                "26=foot:0..3,head:8..15,occupied-head:12..15",
                "break=foot+head->air,drops=foot:355x1+head:none,strength=finite",
                "collision=1x9/16x1,opaque=F,cube=F,light=0:0",
                "scheduled=F,callback-stable=26:0+26:8",
                "paired=stable,orphan-foot=air+355x1,orphan-head=air+none");
        BedSubsystemEvidence first = BedSubsystemFixture.execute(() -> observation);
        BedSubsystemEvidence second = BedSubsystemFixture.execute(() -> observation);
        require(first.equals(second) && first.hashCode() == second.hashCode(),
                "bed evidence equality drifted");
        require(first.canonical().contains("claims=7|")
                && first.canonical().contains("drops=foot:355x1+head:none"),
                "bed evidence inventory drifted");
        System.out.println("bed subsystem fixture tests passed");
    }
    private static void require(boolean value, String message) {
        if (!value)
            throw new AssertionError(message);
    }
}
