package worldline.testapi;

import java.util.Objects;
import worldline.api.RemoteItemStack;

/** Public expected inventory change for one server-authoritative hotbar action. */
public final class BlockLifecycleSlot {
    private final int hotbarSlot, inventorySlot;
    private final RemoteItemStack before, after;

    public BlockLifecycleSlot(int hotbarSlot, int inventorySlot,
            RemoteItemStack before, RemoteItemStack after) {
        if (hotbarSlot < 0 || hotbarSlot > 8 || inventorySlot < 0) {
            throw new IllegalArgumentException("invalid lifecycle slot");
        }
        this.hotbarSlot = hotbarSlot;
        this.inventorySlot = inventorySlot;
        this.before = Objects.requireNonNull(before, "before");
        this.after = after;
    }

    public int hotbarSlot() { return hotbarSlot; }
    public int inventorySlot() { return inventorySlot; }
    public RemoteItemStack before() { return before; }
    public RemoteItemStack after() { return after; }
}
