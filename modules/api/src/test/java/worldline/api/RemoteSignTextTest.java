package worldline.api;

final class RemoteSignTextTest {
    private RemoteSignTextTest() {}

    static void run() {
        BlockPosition cell = new BlockPosition(4, 72, 4);
        RemoteSignText value = new RemoteSignText(cell, "World", "line", "M176", "ok");
        if (!value.equals(new RemoteSignText(cell, "World", "line", "M176", "ok"))
                || value.hashCode() != new RemoteSignText(cell, "World", "line", "M176", "ok").hashCode()
                || value.packetId() != 130 || !value.position().equals(cell)
                || !value.line(0).equals("World") || !value.line(3).equals("ok"))
            throw new AssertionError("sign text value drift");
        fail(() -> new RemoteSignText(null, "World", "line", "M176", "ok"));
        fail(() -> new RemoteSignText(cell, null, "line", "M176", "ok"));
        fail(() -> new RemoteSignText(cell, "0123456789abcdef", "line", "M176", "ok"));
        fail(() -> value.line(4));
    }

    private static void fail(Runnable runnable) {
        try { runnable.run(); throw new AssertionError("expected sign text failure"); }
        catch (IllegalArgumentException expected) {}
    }
}
