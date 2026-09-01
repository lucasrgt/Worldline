package worldline.testkit;

/** Locks the public locked-chest mini-subsystem contract. */
public final class LockedChestSubsystemFixtureTest {
    private LockedChestSubsystemFixtureTest() {
    }
    public static void execute() {
        LockedChestSubsystemObservation observation = new LockedChestSubsystemObservation(
                "95=0,item-route=95x1->0,placed=95:0",
                "break=95:0->0:0,strength=infinite,drop=95x1", "chunk-nbt=95:0",
                "collision=full,light=255:15", "random-enrolled=T,callback=95:0->0:0",
                "stone+lever=stable-95:0");
        LockedChestSubsystemEvidence first = LockedChestSubsystemFixture.execute(
                () -> observation);
        LockedChestSubsystemEvidence second = LockedChestSubsystemFixture.execute(
                () -> observation);
        require(first.equals(second) && first.hashCode() == second.hashCode(),
                "locked-chest evidence equality drifted");
        require(first.canonical().contains("claims=9|")
                && first.canonical().contains("strength=infinite,drop=95x1"),
                "locked-chest evidence inventory drifted");
        System.out.println("locked chest subsystem fixture tests passed");
    }
    private static void require(boolean value, String message) {
        if (!value)
            throw new AssertionError(message);
    }
}
