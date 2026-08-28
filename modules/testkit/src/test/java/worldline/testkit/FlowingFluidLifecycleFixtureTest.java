package worldline.testkit;

import java.util.Arrays;
import worldline.api.BlockLifecycleDriver.ReloadBoundary;
import worldline.api.BlockState;

public final class FlowingFluidLifecycleFixtureTest {
    private FlowingFluidLifecycleFixtureTest() { }

    public static void main(String[] arguments) { execute(); }

    static void execute() {
        FlowingFluidLifecycleEvidence first = FlowingFluidLifecycleFixture.execute(
                FlowingFluidLifecycleFixtureTest::observation);
        FlowingFluidLifecycleEvidence second = FlowingFluidLifecycleFixture.execute(
                FlowingFluidLifecycleFixtureTest::observation);
        require(first.equals(second) && first.hashCode() == second.hashCode(),
                "flowing-fluid lifecycle evidence is not equatable");
        require(first.canonical().contains("water=8|domain=0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15")
                && first.canonical().contains("partial-claims=lava:state-domain-overworld-only")
                && first.canonical().contains("reload=CHUNK_RELOAD"),
                "flowing-fluid lifecycle canonical evidence drifted");
        fail(() -> FlowingFluidLifecycleFixture.execute(
                () -> new FlowingFluidLifecycleObservation(water(6), lava(),
                        ReloadBoundary.CHUNK_RELOAD)));
        System.out.println("FlowingFluidLifecycleFixtureTest passed");
    }

    private static FlowingFluidLifecycleObservation observation() {
        return new FlowingFluidLifecycleObservation(water(5), lava(),
                ReloadBoundary.CHUNK_RELOAD);
    }

    private static FlowingFluidObservation water(int tick) {
        return new FlowingFluidObservation(8, Arrays.asList(
                0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15), tick,
                state(9, 0), state(8, 0), true, 3, 0, 0, 12,
                state(8, 1), state(8, 1));
    }

    private static FlowingFluidObservation lava() {
        return new FlowingFluidObservation(10, Arrays.asList(0, 2, 4, 6, 8, 10, 12, 14),
                30, state(11, 0), state(10, 0), true, 0, 15, 15, 0,
                state(10, 2), state(10, 2));
    }

    private static BlockState state(int id, int metadata) {
        return new BlockState(id, metadata);
    }

    private static void fail(Runnable action) {
        try { action.run(); }
        catch (IllegalStateException expected) { return; }
        throw new AssertionError("invalid flowing-fluid observation accepted");
    }

    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}
