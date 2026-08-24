package worldline.testkit;

import worldline.api.BlockPosition;
import worldline.api.RemoteKeepAliveTimeout;
import worldline.api.RemoteMapData;
import worldline.api.RemoteProtocol14Chain;
import worldline.api.RemoteSignText;

final class Protocol14EdgeFixtureTest {
    private Protocol14EdgeFixtureTest() { }

    static void execute() {
        RemoteSignText sign = new RemoteSignText(
                new BlockPosition(4, 72, 4), "World", "line", "M631", "edge");
        RemoteProtocol14Chain chain = new RemoteProtocol14Chain(
                sign, 4, new RemoteMapData(358, 0, new byte[] {0, 1, 2}), 7, 2);
        RemoteKeepAliveTimeout timeout = new RemoteKeepAliveTimeout(0, 60_000L, true);
        String record = "EdgeSilent631 lost connection: disconnect.genericReason";
        Protocol14EdgeFixture.Evidence first = Protocol14EdgeFixture.observe(
                sign, chain, timeout, record);
        Protocol14EdgeFixture.Evidence second = Protocol14EdgeFixture.observe(
                sign, chain, timeout, record);
        require(first.equals(second) && first.hashCode() == second.hashCode()
                && first.ordered() && first.itemId() == 358 && first.mapId() == 0
                && first.boundedPayload() && first.keepAliveAbsent()
                && first.timeoutReason().equals("socket-read-timeout"),
                "protocol-14 evidence drifted");
        fail(() -> Protocol14EdgeFixture.observe(sign, chain,
                new RemoteKeepAliveTimeout(1, 60_000L, true), record));
    }

    private static void fail(Runnable action) {
        try { action.run(); throw new AssertionError("invalid protocol-14 evidence accepted"); }
        catch (IllegalStateException expected) { }
    }
    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}
