package worldline.testkit;

import worldline.api.SkyBrightnessCycleEvidence;
import worldline.api.WorldlineBehavior;
import worldline.api.WorldlineEnvironmentBehaviors;

final class SkyBrightnessCycleFixtureTest {
    private static final long[] TIMES = {
        0L, 6000L, 12000L, 12500L, 13000L, 13500L, 14000L,
        18000L, 22000L, 22500L, 23000L, 23500L, 23999L
    };
    private static final int[] SUBTRACTION = {
        0, 0, 0, 3, 6, 9, 11, 11, 11, 9, 6, 3, 0
    };

    private SkyBrightnessCycleFixtureTest() { }

    static void execute() {
        SkyBrightnessCycleEvidence first = SkyBrightnessCycleFixture.observe(TIMES, SUBTRACTION);
        SkyBrightnessCycleEvidence second = SkyBrightnessCycleFixture.observe(
                TIMES.clone(), SUBTRACTION.clone());
        SkyBrightnessCycleFixture.compare(first, second);
        require(first.equals(second) && first.hashCode() == second.hashCode(),
                "sky brightness evidence is not equatable");
        require(first.samples() == 13 && first.timeAt(6) == 14000L
                && first.skylightSubtractedAt(6) == 11,
                "sky brightness evidence access drifted");
        require(WorldlineBehavior.require("sky-brightness-cycle")
                == WorldlineEnvironmentBehaviors.SKY_BRIGHTNESS_CYCLE,
                "sky brightness behavior registration drifted");
        int[] invalid = SUBTRACTION.clone();
        invalid[4] = 7;
        fail(() -> SkyBrightnessCycleFixture.observe(TIMES, invalid));
    }

    private static void fail(Runnable action) {
        try {
            action.run();
            throw new AssertionError("invalid sky brightness evidence accepted");
        } catch (IllegalStateException expected) {
            return;
        }
    }

    private static void require(boolean value, String message) {
        if (!value) {
            throw new AssertionError(message);
        }
    }
}
