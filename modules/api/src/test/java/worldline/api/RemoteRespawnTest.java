package worldline.api;

final class RemoteRespawnTest {
    private RemoteRespawnTest() {}
    static void run() {
        RemoteRespawn value = new RemoteRespawn(0, 0, 20);
        if (!value.equals(new RemoteRespawn(0, 0, 20)) || value.hashCode() != new RemoteRespawn(0, 0, 20).hashCode()
                || value.dimension() != 0 || value.healthBefore() != 0 || value.healthAfter() != 20)
            throw new AssertionError("respawn value drifted");
        fail(() -> new RemoteRespawn(1, 0, 20)); fail(() -> new RemoteRespawn(0, 1, 20)); fail(() -> new RemoteRespawn(0, 0, 19));
    }
    private static void fail(Runnable action) { try { action.run(); throw new AssertionError("respawn accepted invalid value"); }
        catch (IllegalArgumentException expected) { } }
}
