package worldline.api;

import java.util.Arrays;
import java.util.Objects;

/** Immutable protocol-14 Packet131 map-data observation. */
public final class RemoteMapData {
    public static final int PACKET = 131;
    private final int itemId, mapId;
    private final byte[] payload;

    public RemoteMapData(int itemId, int mapId, byte[] payload) {
        if (itemId < 0 || itemId > 32767 || mapId < 0 || mapId > 32767
                || payload == null || payload.length > 255)
            throw new IllegalArgumentException("invalid map data");
        this.itemId = itemId; this.mapId = mapId; this.payload = payload.clone();
    }

    public int packetId() { return PACKET; }
    public int itemId() { return itemId; }
    public int mapId() { return mapId; }
    public int payloadLength() { return payload.length; }
    public int payloadByte(int index) { return payload[index] & 255; }
    public byte[] payload() { return payload.clone(); }

    @Override public boolean equals(Object other) {
        if (!(other instanceof RemoteMapData)) return false;
        RemoteMapData value = (RemoteMapData) other;
        return itemId == value.itemId && mapId == value.mapId
                && Arrays.equals(payload, value.payload);
    }

    @Override public int hashCode() { return Objects.hash(itemId, mapId, Arrays.hashCode(payload)); }
}
