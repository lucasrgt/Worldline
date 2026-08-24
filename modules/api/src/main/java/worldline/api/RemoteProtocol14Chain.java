package worldline.api;

import java.util.Objects;

/** Ordered protocol-14 sign then map-data observation from one play stream. */
public final class RemoteProtocol14Chain {
    private final RemoteSignText sign;
    private final RemoteMapData map;
    private final int signSequence, mapSequence, keepAlivePackets;

    public RemoteProtocol14Chain(RemoteSignText sign, int signSequence,
            RemoteMapData map, int mapSequence, int keepAlivePackets) {
        if (sign == null || map == null || signSequence < 1 || mapSequence <= signSequence
                || keepAlivePackets < 0)
            throw new IllegalArgumentException("invalid protocol-14 chain");
        this.sign = sign; this.signSequence = signSequence;
        this.map = map; this.mapSequence = mapSequence;
        this.keepAlivePackets = keepAlivePackets;
    }

    public RemoteSignText sign() { return sign; }
    public RemoteMapData map() { return map; }
    public int signSequence() { return signSequence; }
    public int mapSequence() { return mapSequence; }
    public int keepAlivePackets() { return keepAlivePackets; }

    @Override public boolean equals(Object other) {
        if (!(other instanceof RemoteProtocol14Chain)) return false;
        RemoteProtocol14Chain value = (RemoteProtocol14Chain) other;
        return signSequence == value.signSequence && mapSequence == value.mapSequence
                && keepAlivePackets == value.keepAlivePackets
                && sign.equals(value.sign) && map.equals(value.map);
    }

    @Override public int hashCode() {
        return Objects.hash(sign, signSequence, map, mapSequence, keepAlivePackets);
    }
}
