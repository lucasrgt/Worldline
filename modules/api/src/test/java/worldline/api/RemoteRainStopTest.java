package worldline.api;

final class RemoteRainStopTest {
    private RemoteRainStopTest() { }

    static void run() {
        RemoteRainStop value = new RemoteRainStop(70, 2, true, true);
        if (!value.equals(new RemoteRainStop(70, 2, true, true))
                || value.hashCode() != new RemoteRainStop(70, 2, true, true).hashCode()
                || value.packetId() != 70 || value.reason() != 2
                || !value.rainingBefore() || !value.dryAfter())
            throw new AssertionError("rain stop value drifted");
        fail(() -> new RemoteRainStop(71, 2, true, true));
        fail(() -> new RemoteRainStop(70, 1, true, true));
        fail(() -> new RemoteRainStop(70, 2, false, true));
        fail(() -> new RemoteRainStop(70, 2, true, false));
    }

    private static void fail(Runnable action) {
        try {
            action.run();
            throw new AssertionError("rain stop accepted invalid value");
        } catch (IllegalArgumentException expected) {
            return;
        }
    }
}
