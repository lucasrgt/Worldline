package worldline.api;

import java.util.Objects;

/** Immutable indexed slot whose item is absent when the remote slot is empty. */
public final class RemoteInventorySlot {
    private final int index;
    private final RemoteItemStack item;

    public RemoteInventorySlot(int index, RemoteItemStack item) {
        if (index < 0 || index >= RemoteInventoryView.MAX_SLOTS)
            throw new IllegalArgumentException("invalid inventory slot index");
        this.index = index; this.item = item;
    }

    public int index() { return index; }
    public boolean empty() { return item == null; }
    public RemoteItemStack item() {
        if (item == null) throw new IllegalStateException("inventory slot is empty");
        return item;
    }

    @Override public boolean equals(Object other) {
        if (!(other instanceof RemoteInventorySlot)) return false;
        RemoteInventorySlot value = (RemoteInventorySlot) other;
        return index == value.index && Objects.equals(item, value.item);
    }
    @Override public int hashCode() { return Objects.hash(index, item); }
}
