package worldline.api;

import java.util.Objects;

/** Immutable protocol-14 Packet61 world-event observation. */
public final class RemoteWorldEvent {
    private final BlockPosition position;
    private final int effectId, data;

    public RemoteWorldEvent(BlockPosition position, int effectId, int data) {
        if (position == null) throw new IllegalArgumentException("null world-event position");
        if (effectId < 0) throw new IllegalArgumentException("invalid world-event effect");
        this.position = position; this.effectId = effectId; this.data = data;
    }

    public BlockPosition position() { return position; }
    public int effectId() { return effectId; }
    public int data() { return data; }

    @Override public boolean equals(Object other) {
        if (!(other instanceof RemoteWorldEvent)) return false;
        RemoteWorldEvent value = (RemoteWorldEvent) other;
        return effectId == value.effectId && data == value.data && position.equals(value.position);
    }

    @Override public int hashCode() { return Objects.hash(position, effectId, data); }
}
