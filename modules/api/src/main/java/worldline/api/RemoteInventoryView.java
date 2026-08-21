package worldline.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Immutable bounded server-authoritative view of one protocol inventory window. */
public final class RemoteInventoryView {
    public static final int MAX_SLOTS = 1024;
    private final int windowId;
    private final List<RemoteInventorySlot> slots;

    public RemoteInventoryView(int windowId, List<RemoteInventorySlot> values) {
        if (windowId < 0 || windowId > 127) throw new IllegalArgumentException("invalid inventory window ID");
        if (values == null || values.size() > MAX_SLOTS)
            throw new IllegalArgumentException("invalid inventory slot collection");
        List<RemoteInventorySlot> copy = new ArrayList<>(values);
        for (int index = 0; index < copy.size(); index++) {
            RemoteInventorySlot slot = copy.get(index);
            if (slot == null || slot.index() != index)
                throw new IllegalArgumentException("inventory slots must be contiguous and indexed");
        }
        this.windowId = windowId; this.slots = Collections.unmodifiableList(copy);
    }

    public int windowId() { return windowId; }
    public int size() { return slots.size(); }
    public List<RemoteInventorySlot> slots() { return slots; }
    public RemoteInventorySlot slot(int index) { return slots.get(index); }
    public int occupiedSlots() { int count = 0;
        for (RemoteInventorySlot slot : slots) if (!slot.empty()) count++; return count; }

    @Override public boolean equals(Object other) {
        return other instanceof RemoteInventoryView && windowId == ((RemoteInventoryView) other).windowId
                && slots.equals(((RemoteInventoryView) other).slots);
    }
    @Override public int hashCode() { return 31 * windowId + slots.hashCode(); }
}
