package worldline.testkit;

import worldline.api.RemoteRainStop;
import worldline.api.WorldlineBehavior;
import worldline.api.WorldlineEnvironmentBehaviors;

final class RainStopFixtureTest {
    private RainStopFixtureTest() { }

    static void execute() {
        RemoteRainStop first = RainStopFixture.observe(new RemoteRainStop(70, 2, true, true));
        RemoteRainStop second = RainStopFixture.observe(new RemoteRainStop(70, 2, true, true));
        RainStopFixture.compare(first, second);
        require(first.equals(second) && first.hashCode() == second.hashCode(),
                "rain stop evidence is not equatable");
        require(WorldlineBehavior.require("rain-stop-event")
                == WorldlineEnvironmentBehaviors.RAIN_STOP_EVENT,
                "rain stop behavior registration drifted");
        fail(() -> RainStopFixture.observe(null));
        fail(() -> RainStopFixture.compare(first, null));
    }

    private static void fail(Runnable action) {
        try {
            action.run();
            throw new AssertionError("invalid rain stop evidence accepted");
        } catch (IllegalStateException expected) {
            return;
        }
    }

    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}
