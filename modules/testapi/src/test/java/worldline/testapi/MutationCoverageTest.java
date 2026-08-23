package worldline.testapi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import worldline.test.MutationBoundary;
import worldline.test.MutationCoverage;
import worldline.test.MutationQuality;

public final class MutationCoverageTest {
    private MutationCoverageTest() {}
    public static void main(String[] arguments) {
        if (arguments.length == 3 && arguments[0].equals("--explore")) {
            explore(Long.parseLong(arguments[1]), Integer.parseInt(arguments[2]));
            return;
        }
        require(arguments.length == 0, "usage: MutationCoverageTest [--explore SEED CASES]");
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
    private static void explore(long seed, int cases) {
        if (cases < 1 || cases > 1_000_000) throw new IllegalArgumentException("invalid cases");
        Random random = new Random(seed);
        for (int index = 0; index < cases; index++) {
            int size = 1 + random.nextInt(8);
            List<MutationBoundary> boundaries = new ArrayList<>();
            List<String> required = new ArrayList<>();
            for (int slot = 0; slot < size; slot++) {
                String id = "nightly." + index + "." + slot;
                required.add(id);
                boundaries.add(new MutationBoundary(id, "mapping." + random.nextInt(1_000_000),
                        MutationQuality.values()[random.nextInt(MutationQuality.values().length)],
                        "nightly:seed-" + seed));
            }
            MutationCoverage coverage = new MutationCoverage(boundaries);
            coverage.requireAll(required);
            failure(() -> coverage.boundary("nightly.missing"));
            List<MutationBoundary> duplicate = new ArrayList<>(boundaries);
            duplicate.add(boundaries.get(0));
            failure(() -> new MutationCoverage(duplicate));
        }
        System.out.println("MutationCoverageTest exploratory campaign passed: seed=" + seed
                + ", cases=" + cases);
    }
    private static void failure(Runnable action) {
        try { action.run(); throw new AssertionError("invalid coverage accepted"); }
        catch (IllegalArgumentException expected) { }
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
