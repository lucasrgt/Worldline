package worldline.api;

final class RemoteNoteEventTest {
    private RemoteNoteEventTest() {}
    static void run() {
        BlockPosition cell = new BlockPosition(4, 72, 4);
        RemoteNoteEvent value = new RemoteNoteEvent(54, cell, 1, 1);
        if (!value.equals(new RemoteNoteEvent(54, cell, 1, 1))
                || value.hashCode() != new RemoteNoteEvent(54, cell, 1, 1).hashCode()
                || value.packetId() != 54 || value.instrument() != 1 || value.pitch() != 1
                || !value.position().equals(cell)) throw new AssertionError("note event value drift");
        if (!new RemoteNoteEvent(61, cell, 1000, -3).equals(new RemoteNoteEvent(61, cell, 1000, -3)))
            throw new AssertionError("world-event value drift");
        fail(() -> new RemoteNoteEvent(53, cell, 1, 1));
        fail(() -> new RemoteNoteEvent(54, null, 1, 1));
        fail(() -> new RemoteNoteEvent(54, cell, -1, 0));
        fail(() -> new RemoteNoteEvent(54, cell, 0, 256));
    }
    private static void fail(Runnable runnable) {
        try { runnable.run(); throw new AssertionError("expected note event failure"); }
        catch (IllegalArgumentException expected) {}
    }
}
