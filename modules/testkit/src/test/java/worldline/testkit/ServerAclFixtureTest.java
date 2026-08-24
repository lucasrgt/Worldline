package worldline.testkit;

final class ServerAclFixtureTest {
    private ServerAclFixtureTest() { }

    static void execute() {
        ServerAclFixture.Evidence first = valid();
        ServerAclFixture.Evidence second = valid();
        require(first.equals(second) && first.hashCode() == second.hashCode()
                && first.regularDenied() && first.operatorAllowed() && first.deoperatorDenied()
                && first.kickDisconnected() && first.kickReconnect()
                && first.banDisconnected() && first.banRejected() && first.pardonReconnect(),
                "server ACL evidence drifted");
        fail(() -> ServerAclFixture.observe("Actor tried command: time set 200000", 1000L, 200000L,
                "Actor issued server command: time set 300000", 300010L, 300000L,
                "Actor tried command: time set 400000", 300020L, 400000L,
                "server disconnected: kicked", true,
                "server disconnected: banned", "login accepted", true));
    }

    private static ServerAclFixture.Evidence valid() {
        return ServerAclFixture.observe("Actor tried command: time set 200000", 1000L, 200000L,
                "Actor issued server command: time set 300000", 300010L, 300000L,
                "Actor tried command: time set 400000", 300020L, 400000L,
                "server disconnected: Kicked by admin", true,
                "server disconnected: Banned by admin",
                "login rejected: You are banned from this server", true);
    }
    private static void fail(Runnable action) {
        try { action.run(); throw new AssertionError("invalid server ACL evidence accepted"); }
        catch (IllegalStateException expected) { }
    }
    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}
