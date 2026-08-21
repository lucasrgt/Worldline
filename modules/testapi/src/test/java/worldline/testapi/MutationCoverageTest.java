package worldline.testapi;

import java.util.Arrays;
import java.util.Collections;
import worldline.test.MutationBoundary;
import worldline.test.MutationCoverage;
import worldline.test.MutationQuality;

public final class MutationCoverageTest {
    private MutationCoverageTest() {}
    public static void main(String[] arguments) {
        MutationBoundary slot = new MutationBoundary("inventory.slot-write",
                "b1.7.3.inventory.set-slot", MutationQuality.PUSH, "consumer:boundary-spec");
        MutationBoundary load = new MutationBoundary("inventory.nbt-load",
                "b1.7.3.inventory.nbt-load", MutationQuality.DIRTY_NOTIFY, "consumer:restart-spec");
        MutationCoverage coverage = new MutationCoverage(Arrays.asList(slot, load));
        coverage.requireAll(Arrays.asList("inventory.slot-write", "inventory.nbt-load"));
        require(coverage.boundary("inventory.slot-write").quality() == MutationQuality.PUSH,
                "quality drifted");
        failure(() -> coverage.requireAll(Collections.singletonList("inventory.container-transfer")));
        failure(() -> new MutationCoverage(Arrays.asList(slot, slot)));
        System.out.println("MutationCoverageTest passed");
    }
    private static void failure(Runnable action) {
        try { action.run(); throw new AssertionError("invalid coverage accepted"); }
        catch (IllegalArgumentException expected) { }
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
