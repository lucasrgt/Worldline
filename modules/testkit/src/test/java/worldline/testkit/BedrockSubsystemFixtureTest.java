package worldline.testkit;
import worldline.testapi.BedrockSubsystemEvidence;
import worldline.testapi.BedrockSubsystemFixture;
import worldline.testapi.BedrockSubsystemObservation;

/** Locks the public bedrock mini-subsystem contract. */
public final class BedrockSubsystemFixtureTest {
    private BedrockSubsystemFixtureTest() { }
    public static void execute() {
        BedrockSubsystemObservation observation = new BedrockSubsystemObservation(
                "7=0,item-route=7x1->0,placed=7:0",
                "break-attempt=7:0->7:0,strength=0,drop=none", "chunk-nbt=7:0",
                "collision=full,light=255:0", "scheduled=F,callback-stable=7:0",
                "stone+lever=stable-7:0");
        BedrockSubsystemEvidence first = BedrockSubsystemFixture.execute(() -> observation);
        BedrockSubsystemEvidence second = BedrockSubsystemFixture.execute(() -> observation);
        require(first.equals(second) && first.hashCode() == second.hashCode(),
                "bedrock evidence equality drifted");
        require(first.canonical().contains("claims=9|")
                && first.canonical().contains("strength=0,drop=none"),
                "bedrock evidence inventory drifted");
        System.out.println("bedrock subsystem fixture tests passed");
    }
    private static void require(boolean value, String message) {
        if (!value)
            throw new AssertionError(message);
    }
}
