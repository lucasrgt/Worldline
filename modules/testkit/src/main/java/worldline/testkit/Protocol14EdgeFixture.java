package worldline.testkit;

import java.util.Objects;
import worldline.api.RemoteKeepAliveTimeout;
import worldline.api.RemoteProtocol14Chain;
import worldline.api.RemoteSignText;

/** Reusable evidence boundary for protocol-14 variable packets and idle timeout. */
public final class Protocol14EdgeFixture {
    private static final long MIN_TIMEOUT_MILLIS = 20_000L;
    private static final long MAX_TIMEOUT_MILLIS = 90_000L;
    private Protocol14EdgeFixture() { }

    public static Evidence observe(RemoteSignText expectedSign, RemoteProtocol14Chain chain,
            RemoteKeepAliveTimeout timeout, String serverRecord) {
        if (expectedSign == null || chain == null || timeout == null || serverRecord == null)
            throw new IllegalArgumentException("null protocol-14 evidence");
        require(expectedSign.equals(chain.sign()) && chain.sign().packetId() == 130
                && chain.map().packetId() == 131 && chain.signSequence() < chain.mapSequence(),
                "Packet130 to Packet131 chain drifted");
        require(chain.map().itemId() == 358 && chain.map().mapId() == 0
                && chain.map().payloadLength() >= 1 && chain.map().payloadLength() <= 255,
                "Packet131 map envelope drifted");
        require(timeout.keepAlivePackets() == 0 && timeout.streamClosed()
                && timeout.elapsedMillis() >= MIN_TIMEOUT_MILLIS
                && timeout.elapsedMillis() <= MAX_TIMEOUT_MILLIS
                && serverRecord.contains(" lost connection: disconnect.genericReason"),
                "Packet0 absence or silent timeout drifted");
        return new Evidence(true, chain.map().itemId(), chain.map().mapId(), true,
                true, "socket-read-timeout");
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }

    public static final class Evidence {
        private final boolean ordered, boundedPayload, keepAliveAbsent;
        private final int itemId, mapId;
        private final String timeoutReason;
        Evidence(boolean ordered, int itemId, int mapId, boolean boundedPayload,
                boolean keepAliveAbsent, String timeoutReason) {
            this.ordered = ordered; this.itemId = itemId; this.mapId = mapId;
            this.boundedPayload = boundedPayload; this.keepAliveAbsent = keepAliveAbsent;
            this.timeoutReason = timeoutReason;
        }
        public boolean ordered() { return ordered; }
        public int itemId() { return itemId; }
        public int mapId() { return mapId; }
        public boolean boundedPayload() { return boundedPayload; }
        public boolean keepAliveAbsent() { return keepAliveAbsent; }
        public String timeoutReason() { return timeoutReason; }
        @Override public boolean equals(Object other) {
            if (!(other instanceof Evidence)) return false;
            Evidence value = (Evidence) other;
            return ordered == value.ordered && itemId == value.itemId && mapId == value.mapId
                    && boundedPayload == value.boundedPayload
                    && keepAliveAbsent == value.keepAliveAbsent
                    && timeoutReason.equals(value.timeoutReason);
        }
        @Override public int hashCode() {
            return Objects.hash(ordered, itemId, mapId, boundedPayload,
                    keepAliveAbsent, timeoutReason);
        }
    }
}
