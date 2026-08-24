package worldline.api;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Objects;

/** Immutable converged 128x128 color grid reconstructed from protocol-14 Packet131. */
public final class RemoteMapContent {
    public static final int WIDTH = 128;
    private final int itemId, mapId, observedColumns, colorPackets, markerPackets;
    private final byte[] colors;

    public RemoteMapContent(int itemId, int mapId, int observedColumns,
            int colorPackets, int markerPackets, byte[] colors) {
        if (itemId < 0 || mapId < 0 || observedColumns < 0 || observedColumns > WIDTH
                || colorPackets < 0 || markerPackets < 0
                || colors == null || colors.length != WIDTH * WIDTH)
            throw new IllegalArgumentException("invalid remote map content");
        this.itemId = itemId; this.mapId = mapId; this.observedColumns = observedColumns;
        this.colorPackets = colorPackets; this.markerPackets = markerPackets;
        this.colors = colors.clone();
    }

    public int itemId() { return itemId; }
    public int mapId() { return mapId; }
    public int observedColumns() { return observedColumns; }
    public int colorPackets() { return colorPackets; }
    public int markerPackets() { return markerPackets; }
    public int colorAt(int x, int y) {
        if (x < 0 || x >= WIDTH || y < 0 || y >= WIDTH)
            throw new IndexOutOfBoundsException("map color outside 128x128 grid");
        return colors[y * WIDTH + x] & 255;
    }
    public byte[] colors() { return colors.clone(); }
    public int nonZeroColors() {
        int count = 0; for (byte color : colors) if (color != 0) count++; return count;
    }
    public int distinctColors() {
        boolean[] found = new boolean[256]; int count = 0;
        for (byte color : colors) { int value = color & 255;
            if (!found[value]) { found[value] = true; count++; } }
        return count;
    }
    public String colorsSha256() {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(colors);
            StringBuilder value = new StringBuilder(64);
            for (byte part : digest) value.append(String.format("%02x", part & 255));
            return value.toString();
        } catch (NoSuchAlgorithmException impossible) { throw new AssertionError(impossible); }
    }

    @Override public boolean equals(Object other) {
        if (!(other instanceof RemoteMapContent)) return false;
        RemoteMapContent value = (RemoteMapContent) other;
        return itemId == value.itemId && mapId == value.mapId
                && observedColumns == value.observedColumns && colorPackets == value.colorPackets
                && markerPackets == value.markerPackets && Arrays.equals(colors, value.colors);
    }
    @Override public int hashCode() {
        return Objects.hash(itemId, mapId, observedColumns, colorPackets,
                markerPackets, Arrays.hashCode(colors));
    }
}
