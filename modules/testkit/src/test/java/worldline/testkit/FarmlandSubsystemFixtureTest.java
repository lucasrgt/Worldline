package worldline.testkit;

/** Locks the public farmland mini-subsystem contract. */
public final class FarmlandSubsystemFixtureTest {
    private FarmlandSubsystemFixtureTest() {
    }
    public static void execute() {
        FarmlandSubsystemObservation observation = new FarmlandSubsystemObservation(
                "60=0..7,item-route=60x1->0,placed=60:0",
                "break=60:0->0:0,strength=finite,drop=3x1", "chunk-nbt=60:7",
                "collision=full,visual-height=15/16,opaque=F,cube=F,light=255:0",
                "random-enrolled=T,hydration=0->7,dry=7->0",
                "air-above=stable-60:0,solid-cover=60:0->3:0");
        FarmlandSubsystemEvidence first = FarmlandSubsystemFixture.execute(
                () -> observation);
        FarmlandSubsystemEvidence second = FarmlandSubsystemFixture.execute(
                () -> observation);
        require(first.equals(second) && first.hashCode() == second.hashCode(),
                "farmland evidence equality drifted");
        require(first.canonical().contains("claims=8|")
                && first.canonical().contains("strength=finite,drop=3x1"),
                "farmland evidence inventory drifted");
        System.out.println("farmland subsystem fixture tests passed");
    }
    private static void require(boolean value, String message) {
        if (!value)
            throw new AssertionError(message);
    }
}
