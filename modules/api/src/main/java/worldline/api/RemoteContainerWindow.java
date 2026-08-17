package worldline.api;

import java.util.Objects;

/** Immutable descriptor paired with the matching authoritative full-window view. */
public final class RemoteContainerWindow {
    private final RemoteWindowDescriptor descriptor;
    private final RemoteInventoryView inventory;

    public RemoteContainerWindow(RemoteWindowDescriptor descriptor, RemoteInventoryView inventory) {
        if (descriptor == null || inventory == null || descriptor.windowId() != inventory.windowId())
            throw new IllegalArgumentException("remote window identity mismatch");
        if (descriptor.kind() == RemoteWindowKind.CHEST
                && inventory.size() != descriptor.containerSlots() + 36)
            throw new IllegalArgumentException("remote chest window shape mismatch");
        this.descriptor = descriptor; this.inventory = inventory;
    }

    public RemoteWindowDescriptor descriptor() { return descriptor; }
    public RemoteInventoryView inventory() { return inventory; }

    @Override public boolean equals(Object other) {
        if (!(other instanceof RemoteContainerWindow)) return false;
        RemoteContainerWindow value = (RemoteContainerWindow) other;
        return descriptor.equals(value.descriptor) && inventory.equals(value.inventory);
    }
    @Override public int hashCode() { return Objects.hash(descriptor, inventory); }
}
