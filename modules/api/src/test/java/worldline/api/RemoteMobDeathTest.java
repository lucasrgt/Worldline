package worldline.api;

final class RemoteMobDeathTest {
    private RemoteMobDeathTest() {}
    static void run() {
        RemoteMobDeath death = new RemoteMobDeath(7, 3, true);
        if (!death.equals(new RemoteMobDeath(7, 3, true)) || death.hashCode() != new RemoteMobDeath(7, 3, true).hashCode()
                || death.entityId() != 7 || death.deathStatus() != 3 || !death.hurtObserved() || death.destroyPacket() != 29)
            throw new AssertionError("mob death value drift");
        fail(() -> new RemoteMobDeath(-1, 3, true));
        fail(() -> new RemoteMobDeath(7, 2, true));
    }
    private static void fail(Runnable r) {
        try { r.run(); throw new AssertionError("expected death failure"); }
        catch (IllegalArgumentException expected) {}
    }
}
