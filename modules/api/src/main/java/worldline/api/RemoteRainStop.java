package worldline.api;

import java.util.Objects;

/** One live official protocol-14 rain-stop observation: Packet70 reason 2, rain to dry. */
public final class RemoteRainStop {
    public static final int RAIN_PACKET_ID = 70;
    public static final int END_RAIN_REASON = 2;

    private final int packetId, reason;
    private final boolean rainingBefore, dryAfter;

    public RemoteRainStop(int packetId, int reason, boolean rainingBefore, boolean dryAfter) {
        if (packetId != RAIN_PACKET_ID || reason != END_RAIN_REASON)
            throw new IllegalArgumentException("rain stop requires Packet70 reason 2");
        if (!rainingBefore || !dryAfter)
            throw new IllegalArgumentException("rain stop requires raining-before then dry-after");
        this.packetId = packetId;
        this.reason = reason;
        this.rainingBefore = rainingBefore;
        this.dryAfter = dryAfter;
    }

    public int packetId() { return packetId; }
    public int reason() { return reason; }
    public boolean rainingBefore() { return rainingBefore; }
    public boolean dryAfter() { return dryAfter; }

    @Override public boolean equals(Object other) {
        if (!(other instanceof RemoteRainStop)) return false;
        RemoteRainStop value = (RemoteRainStop) other;
        return packetId == value.packetId && reason == value.reason
                && rainingBefore == value.rainingBefore && dryAfter == value.dryAfter;
    }

    @Override public int hashCode() {
        return Objects.hash(packetId, reason, rainingBefore, dryAfter);
    }
}
