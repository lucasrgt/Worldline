package worldline.testkit;

import worldline.api.WorldlineBehavior;
import worldline.api.WorldlinePlayerBehaviors;

final class ServerEntryPolicyFixtureTest {
    private ServerEntryPolicyFixtureTest() { }

    static void execute() {
        ServerEntryPolicyFixture.Evidence first = ServerEntryPolicyFixture.observe(1, 3,
                "login rejected: You are not white-listed on this server!", true,
                "login rejected: The server is full!", true);
        ServerEntryPolicyFixture.Evidence second = ServerEntryPolicyFixture.observe(1, 3,
                "LOGIN REJECTED: NOT WHITE-LISTED", true,
                "LOGIN REJECTED: SERVER IS FULL", true);
        ServerEntryPolicyFixture.compare(first, second);
        require(first.equals(second) && first.hashCode() == second.hashCode()
                        && first.maximumPlayers() == 1 && first.identities() == 3
                        && first.unlistedRejected() && first.listedAccepted()
                        && first.overflowRejected() && first.openAccepted(),
                "server entry policy evidence drifted");
        WorldlineBehavior behavior = WorldlineBehavior.require("server-entry-policy");
        require(behavior == WorldlinePlayerBehaviors.SERVER_ENTRY_POLICY,
                "server entry policy registration drifted");
        fail(() -> ServerEntryPolicyFixture.observe(2, 3,
                "login rejected: white-list", true,
                "login rejected: server is full", true));
        fail(() -> ServerEntryPolicyFixture.observe(1, 3, "login rejected: banned", true,
                "login rejected: server is full", true));
        fail(() -> ServerEntryPolicyFixture.observe(1, 3,
                "login rejected: white-list", false,
                "login rejected: server is full", true));
        fail(() -> ServerEntryPolicyFixture.observe(1, 3,
                "login rejected: white-list", true,
                "login rejected: banned", true));
        fail(() -> ServerEntryPolicyFixture.observe(1, 3,
                "login rejected: white-list", true,
                "login rejected: server is full", false));
        System.out.println("ServerEntryPolicyFixtureTest passed");
    }

    private static void fail(Runnable action) {
        try {
            action.run();
            throw new AssertionError("invalid server entry evidence accepted");
        } catch (IllegalStateException expected) {
            return;
        }
    }

    private static void require(boolean value, String message) {
        if (!value) {
            throw new AssertionError(message);
        }
    }
}
