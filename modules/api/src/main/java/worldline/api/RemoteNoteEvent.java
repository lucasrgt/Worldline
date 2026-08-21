package worldline.api;

import java.util.Objects;

/** Immutable protocol-14 Packet54 play-note or Packet61 world-event observation. */
public final class RemoteNoteEvent {
    private final int packetId, instrument, pitch;
    private final BlockPosition position;

    public RemoteNoteEvent(int packetId, BlockPosition position, int instrument, int pitch) {
        if (packetId != 54 && packetId != 61) throw new IllegalArgumentException("invalid note packet");
        if (position == null) throw new IllegalArgumentException("null note position");
        if (packetId == 54 && (instrument < 0 || instrument > 255 || pitch < 0 || pitch > 255))
            throw new IllegalArgumentException("invalid note action");
        this.packetId = packetId; this.position = position;
        this.instrument = instrument; this.pitch = pitch;
    }

    public int packetId() { return packetId; }
    public BlockPosition position() { return position; }
    public int instrument() { return instrument; }
    public int pitch() { return pitch; }

    @Override public boolean equals(Object other) {
        if (!(other instanceof RemoteNoteEvent)) return false;
        RemoteNoteEvent value = (RemoteNoteEvent) other;
        return packetId == value.packetId && instrument == value.instrument
                && pitch == value.pitch && position.equals(value.position);
    }

    @Override public int hashCode() {
        return Objects.hash(packetId, position, instrument, pitch);
    }
}
