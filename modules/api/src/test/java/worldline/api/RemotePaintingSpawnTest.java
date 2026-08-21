package worldline.api;

final class RemotePaintingSpawnTest {
    private RemotePaintingSpawnTest() {}

    static void run() {
        RemotePaintingSpawn value = new RemotePaintingSpawn(11, "Kebab", 5, 72, 4, 1);
        if (!value.equals(new RemotePaintingSpawn(11, "Kebab", 5, 72, 4, 1))
                || value.hashCode() != new RemotePaintingSpawn(11, "Kebab", 5, 72, 4, 1).hashCode()
                || value.packet() != 25 || value.entityId() != 11 || !"Kebab".equals(value.title())
                || value.direction() != 1 || value.y() != 72)
            throw new AssertionError("painting spawn value drift");
        fail(() -> new RemotePaintingSpawn(-1, "Kebab", 0, 64, 0, 0));
        fail(() -> new RemotePaintingSpawn(1, null, 0, 64, 0, 0));
        fail(() -> new RemotePaintingSpawn(1, "Unknown", 0, 64, 0, 0));
        fail(() -> new RemotePaintingSpawn(1, "Kebab", 0, 128, 0, 0));
        fail(() -> new RemotePaintingSpawn(1, "Kebab", 0, 64, 0, 4));
    }

    private static void fail(Runnable runnable) {
        try { runnable.run(); throw new AssertionError("expected painting spawn failure"); }
        catch (IllegalArgumentException expected) {}
    }
}
