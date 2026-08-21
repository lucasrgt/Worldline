package worldline.api;

final class RemoteCombatTest {
    private RemoteCombatTest() {}
    static void run() {
        RemoteCombatStrike strike = new RemoteCombatStrike("Attacker", 1, "Victim", 2);
        RemoteIncomingHit hit = new RemoteIncomingHit("Victim", 2, 20, 18);
        if (strike.targetEntityId() != 2 || hit.damage() != 2 || hit.healthAfter() != 18)
            throw new AssertionError("combat accessors drifted");
        failure(() -> new RemoteCombatStrike("Attacker", 1, "Attacker", 1));
        failure(() -> new RemoteIncomingHit("Victim", 2, 18, 18));
    }
    private static void failure(Runnable action) { try { action.run(); throw new AssertionError("expected failure"); }
        catch (IllegalArgumentException expected) { } }
}
