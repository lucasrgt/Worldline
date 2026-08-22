package worldline.api;

import java.util.Objects;

/** One live official protocol-14 rain-start observation: Packet70Bed reason 1, dry to rain. */
public final class RemoteRainStart {
    public static final int RAIN_PACKET_ID = 70;
    public static final int BEGIN_RAIN_REASON = 1;

    private final int packetId, reason;
    private final boolean dryBefore, rainingAfter;

    public RemoteRainStart(int packetId, int reason, boolean dryBefore, boolean rainingAfter) {
        if (packetId != RAIN_PACKET_ID || reason != BEGIN_RAIN_REASON)
            throw new IllegalArgumentException("rain start requires Packet70 reason 1");
        if (!dryBefore || !rainingAfter)
            throw new IllegalArgumentException("rain start requires dry-before then raining-after");
        this.packetId = packetId;
        this.reason = reason;
        this.dryBefore = dryBefore;
        this.rainingAfter = rainingAfter;
    }

    public int packetId() { return packetId; }
    public int reason() { return reason; }
    public boolean dryBefore() { return dryBefore; }
    public boolean rainingAfter() { return rainingAfter; }

    @Override public boolean equals(Object other) {
        if (!(other instanceof RemoteRainStart)) return false;
        RemoteRainStart value = (RemoteRainStart) other;
        return packetId == value.packetId && reason == value.reason
                && dryBefore == value.dryBefore && rainingAfter == value.rainingAfter;
    }

    @Override public int hashCode() { return Objects.hash(packetId, reason, dryBefore, rainingAfter); }
}
