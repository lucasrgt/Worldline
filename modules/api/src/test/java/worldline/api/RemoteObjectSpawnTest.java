package worldline.api;

final class RemoteObjectSpawnTest {
    private RemoteObjectSpawnTest() {}
    static void run() {
        RemoteObjectSpawn cart = new RemoteObjectSpawn(9, 10, 144, 2331, 144, 0, 0, 0, 0);
        if (!cart.equals(new RemoteObjectSpawn(9, 10, 144, 2331, 144, 0, 0, 0, 0))
                || cart.hashCode() != new RemoteObjectSpawn(9, 10, 144, 2331, 144, 0, 0, 0, 0).hashCode()
                || cart.x() != 4.5D || cart.y() != 72.84375D || cart.z() != 4.5D || cart.type() != 10
                || cart.throwerId() != 0) throw new AssertionError("object spawn value drift");
        fail(() -> new RemoteObjectSpawn(-1, 10, 0, 0, 0, 0, 0, 0, 0));
        fail(() -> new RemoteObjectSpawn(1, 0, 0, 0, 0, 0, 0, 0, 0));
        fail(() -> new RemoteObjectSpawn(1, 10, 0, 0, 0, 0, 1, 0, 0));
    }
    private static void fail(Runnable runnable) {
        try { runnable.run(); throw new AssertionError("expected object failure"); }
        catch (IllegalArgumentException expected) {}
    }
}
