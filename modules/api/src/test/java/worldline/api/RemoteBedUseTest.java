package worldline.api;

final class RemoteBedUseTest {
    private RemoteBedUseTest() {}

    static void run() {
        RemoteBedUse value = new RemoteBedUse(7, 0, 4, 73, 5, RemoteBedUse.NO_PACKET70);
        if (!value.equals(new RemoteBedUse(7, 0, 4, 73, 5, -1)) || value.sleepPacket() != 17
                || value.bedPacket() != 70 || value.packet70() != -1 || value.y() != 73)
            throw new AssertionError("bed use value drifted");
        fail(() -> new RemoteBedUse(-1, 0, 0, 64, 0, -1));
        fail(() -> new RemoteBedUse(1, 1, 0, 64, 0, -1));
        fail(() -> new RemoteBedUse(1, 0, 0, 128, 0, -1));
        fail(() -> new RemoteBedUse(1, 0, 0, 64, 0, 3));
    }

    private static void fail(Runnable action) {
        try { action.run(); throw new AssertionError("bed use accepted invalid value"); }
        catch (IllegalArgumentException expected) { }
    }
}
