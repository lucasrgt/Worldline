package worldline.testkit;
import worldline.testapi.FireSubsystemEvidence;
import worldline.testapi.FireSubsystemFixture;
import worldline.testapi.FireSubsystemObservation;

/** Locks the public fire mini-subsystem contract. */
public final class FireSubsystemFixtureTest {
    private FireSubsystemFixtureTest() {
    }
    public static void execute() {
        FireSubsystemObservation observation = new FireSubsystemObservation(
                "51=0..15,item-route=51x1->0,placed=51:0",
                "break=51:0->0:0,strength=infinite,drop=none", "chunk-nbt=51:15",
                "collision=none,collidable=F,light=0:15",
                "random-enrolled=T,age=0->15,tick-rate=40",
                "supported=stable-51:0,support-loss=51:0->0:0");
        FireSubsystemEvidence first = FireSubsystemFixture.execute(
                () -> observation);
        FireSubsystemEvidence second = FireSubsystemFixture.execute(
                () -> observation);
        require(first.equals(second) && first.hashCode() == second.hashCode(),
                "fire evidence equality drifted");
        require(first.canonical().contains("claims=8|")
                && first.canonical().contains("strength=infinite,drop=none"),
                "fire evidence inventory drifted");
        System.out.println("fire subsystem fixture tests passed");
    }
    private static void require(boolean value, String message) {
        if (!value)
            throw new AssertionError(message);
    }
}
