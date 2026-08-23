package worldline.api;

final class RemoteObjectMovementTest {
    private RemoteObjectMovementTest() {}

    static void run() {
        RemoteObjectMovement move = new RemoteObjectMovement(7, 33, 144, 2304, 128, 160, 2304, 128, 64, 0);
        RemoteObjectMovement copy = new RemoteObjectMovement(7, 33, 144, 2304, 128, 160, 2304, 128, 64, 0);
        if (!move.equals(copy) || move.hashCode() != copy.hashCode() || move.fromX() != 4.5D
                || move.toFixedX() != 160 || move.packetId() != 33)
            throw new AssertionError("object movement value drift");
        fail(() -> new RemoteObjectMovement(7, 32, 0, 0, 0, 1, 0, 0, 0, 0));
        fail(() -> new RemoteObjectMovement(7, 31, 0, 0, 0, 0, 0, 0, 0, 0));
    }

    private static void fail(Runnable runnable) {
        try { runnable.run(); throw new AssertionError("expected object movement failure"); }
        catch (IllegalArgumentException expected) { }
    }
}
