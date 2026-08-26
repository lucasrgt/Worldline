package worldline.testkit;

import worldline.api.RemoteRainStop;

/** Reusable TestKit fixture for an observed official raining-to-dry transition. */
public final class RainStopFixture {
    private RainStopFixture() { }

    public static RemoteRainStop observe(RemoteRainStop transition) {
        if (transition == null || transition.packetId() != RemoteRainStop.RAIN_PACKET_ID
                || transition.reason() != RemoteRainStop.END_RAIN_REASON
                || !transition.rainingBefore() || !transition.dryAfter())
            throw new IllegalStateException("rain stop transition evidence drifted");
        return transition;
    }

    public static void compare(RemoteRainStop expected, RemoteRainStop observed) {
        if (expected == null || !expected.equals(observed))
            throw new IllegalStateException("rain stop evidence diverged");
    }
}
