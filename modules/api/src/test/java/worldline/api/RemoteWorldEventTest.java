package worldline.api;

final class RemoteWorldEventTest {
    private RemoteWorldEventTest() { }

    static void run() {
        BlockPosition position = new BlockPosition(4, 72, 5);
        RemoteWorldEvent event = new RemoteWorldEvent(position, 1003, 0);
        RemoteWorldEvent equal = new RemoteWorldEvent(position, 1003, 0);
        if (!event.equals(equal) || event.hashCode() != equal.hashCode()
                || event.position() != position || event.effectId() != 1003 || event.data() != 0)
            throw new AssertionError("world event value contract drifted");
        fail(() -> new RemoteWorldEvent(null, 1003, 0));
        fail(() -> new RemoteWorldEvent(position, -1, 0));
    }

    private static void fail(Runnable action) {
        try { action.run(); throw new AssertionError("invalid world event accepted"); }
        catch (IllegalArgumentException expected) { }
    }
}
