package worldline.testkit;

final class PortalReentryCooldownFixtureTest {
    private PortalReentryCooldownFixtureTest() { }

    static void execute() {
        PortalReentryCooldownFixture.Evidence first = valid();
        PortalReentryCooldownFixture.Evidence second = valid();
        require(first.equals(second) && first.hashCode() == second.hashCode()
                && first.blockedTicks() == 120 && first.releaseTicks() == 220
                && "PortalGate652".equals(first.actorKey()) && first.exitedCollision()
                && first.reenteredCollision()
                && first.sourcePortalCells() == 6 && first.destinationPortalCells() == 6
                && first.returnPortalCells() == 6 && first.persistedDimension() == 0,
                "portal cooldown evidence was not equatable");
        reject(() -> verify(contact("PortalGate652"), released("OtherPlayer", true, true),
                6, 6, 6, 0));
        reject(() -> verify(contact("PortalGate652"), released("PortalGate652", false, true),
                6, 6, 6, 0));
        reject(() -> verify(contact("PortalGate652"), released("PortalGate652", true, false),
                6, 6, 6, 0));
        reject(() -> verify(contact("PortalGate652"), released("PortalGate652", true, true),
                6, 5, 6, 0));
    }

    private static PortalReentryCooldownFixture.Evidence valid() {
        return verify(contact("PortalGate652"), released("PortalGate652", true, true),
                6, 6, 6, 0);
    }
    private static PortalReentryCooldownFixture.Trial contact(String actor) {
        return new PortalReentryCooldownFixture.Trial(
                actor, -1, -1, 0, 120, true, false, false);
    }
    private static PortalReentryCooldownFixture.Trial released(
            String actor, boolean exited, boolean reentered) {
        return new PortalReentryCooldownFixture.Trial(
                actor, -1, 0, 220, 120, true, exited, reentered);
    }
    private static PortalReentryCooldownFixture.Evidence verify(
            PortalReentryCooldownFixture.Trial contact,
            PortalReentryCooldownFixture.Trial released, int source, int destination,
            int returned, int persisted) {
        return PortalReentryCooldownFixture.verify(contact, released, source, destination,
                returned, persisted);
    }
    private static void reject(Runnable action) {
        try {
            action.run();
            throw new AssertionError("invalid portal cooldown evidence accepted");
        } catch (IllegalStateException expected) {
            // Expected fail-closed boundary.
        }
    }
    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}
