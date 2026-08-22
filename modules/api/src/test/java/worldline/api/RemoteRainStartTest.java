package worldline.api;

final class RemoteRainStartTest {
    private RemoteRainStartTest() {}

    static void run() {
        RemoteRainStart value = new RemoteRainStart(70, 1, true, true);
        if (!value.equals(new RemoteRainStart(70, 1, true, true)) || value.packetId() != 70
                || value.reason() != 1 || !value.dryBefore() || !value.rainingAfter())
            throw new AssertionError("rain start value drifted");
        fail(() -> new RemoteRainStart(71, 1, true, true));
        fail(() -> new RemoteRainStart(70, 2, true, true));
        fail(() -> new RemoteRainStart(70, 1, false, true));
        fail(() -> new RemoteRainStart(70, 1, true, false));
    }

    private static void fail(Runnable action) {
        try { action.run(); throw new AssertionError("rain start accepted invalid value"); }
        catch (IllegalArgumentException expected) { }
    }
}
