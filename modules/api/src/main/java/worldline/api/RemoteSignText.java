package worldline.api;

import java.util.Objects;

/** Immutable protocol-14 Packet130 standing-sign text observation. */
public final class RemoteSignText {
    public static final int PACKET = 130;
    private final BlockPosition position;
    private final String line0, line1, line2, line3;

    public RemoteSignText(BlockPosition position, String line0, String line1, String line2, String line3) {
        if (position == null) throw new IllegalArgumentException("null sign position");
        this.position = position;
        this.line0 = line(line0);
        this.line1 = line(line1);
        this.line2 = line(line2);
        this.line3 = line(line3);
    }

    public int packetId() { return PACKET; }
    public BlockPosition position() { return position; }
    public String line(int index) {
        if (index == 0) return line0;
        if (index == 1) return line1;
        if (index == 2) return line2;
        if (index == 3) return line3;
        throw new IllegalArgumentException("invalid sign line");
    }

    @Override public boolean equals(Object other) {
        if (!(other instanceof RemoteSignText)) return false;
        RemoteSignText value = (RemoteSignText) other;
        return position.equals(value.position) && line0.equals(value.line0) && line1.equals(value.line1)
                && line2.equals(value.line2) && line3.equals(value.line3);
    }

    @Override public int hashCode() {
        return Objects.hash(position, line0, line1, line2, line3);
    }

    private static String line(String value) {
        if (value == null || value.length() > 15) throw new IllegalArgumentException("invalid sign line");
        return value;
    }
}
