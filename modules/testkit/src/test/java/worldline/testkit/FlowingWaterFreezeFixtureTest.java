package worldline.testkit;

import worldline.api.BlockState;
import worldline.api.WorldlineBehavior;
import worldline.api.WorldlineWorldBehaviors;

final class FlowingWaterFreezeFixtureTest {
    private FlowingWaterFreezeFixtureTest() {
    }

    static void execute() {
        FlowingWaterFreezeFixture.Evidence first = evidence(2);
        FlowingWaterFreezeFixture.Evidence second = evidence(5);
        require(first.equals(second) && first.hashCode() == second.hashCode()
                && first.before().equals(new BlockState(9, 0))
                && first.after().equals(new BlockState(79, 0))
                && first.flowing().equals(new BlockState(8, 1))
                && first.maximumPasses() == 8,
                "flowing-water freeze evidence is not equatable across successful draws");
        require(WorldlineBehavior.require("flowing-water-freeze")
                == WorldlineWorldBehaviors.FLOWING_WATER_FREEZE,
                "flowing-water freeze behavior registration drifted");
        requireNonNotifyingPrecondition();
        fail(() -> FlowingWaterFreezeFixture.verify(0, (pass) -> null));
        fail(() -> FlowingWaterFreezeFixture.verify(8, null));
    }

    private static void requireNonNotifyingPrecondition() {
        FlowingWaterFreezeFixture.Evidence intact = FlowingWaterFreezeFixture.verify(8,
                pass -> new FlowingWaterFreezeFixture.Observation(
                        new BlockState(pass >= 2 ? 79 : 9, 0), new BlockState(8, 1), true, 0, 0));
        require(intact.before().equals(new BlockState(9, 0))
                && intact.after().equals(new BlockState(79, 0))
                && intact.flowing().equals(new BlockState(8, 1)),
                "non-notifying fixture did not preserve the initial still/moving pair");
        failObservation(() -> FlowingWaterFreezeFixture.verify(8,
                pass -> new FlowingWaterFreezeFixture.Observation(
                        new BlockState(8, 1), new BlockState(8, 1), true, 0, 0)));
        failObservation(() -> FlowingWaterFreezeFixture.verify(8,
                pass -> new FlowingWaterFreezeFixture.Observation(
                        new BlockState(9, 0), new BlockState(9, 0), true, 0, 0)));
    }

    private static FlowingWaterFreezeFixture.Evidence evidence(int success) {
        return FlowingWaterFreezeFixture.verify(8, pass -> new FlowingWaterFreezeFixture.Observation(
                new BlockState(pass >= success ? 79 : 9, 0), new BlockState(8, 1), true, 0, 0));
    }

    private static void fail(Runnable action) {
        try {
            action.run();
            throw new AssertionError("invalid flowing-water freeze evidence accepted");
        } catch (IllegalArgumentException expected) {
        }
    }

    private static void failObservation(Runnable action) {
        try {
            action.run();
            throw new AssertionError("disturbed flowing-water freeze observation accepted");
        } catch (IllegalStateException expected) {
        }
    }

    private static void require(boolean value, String message) {
        if (!value) {
            throw new AssertionError(message);
        }
    }
}
