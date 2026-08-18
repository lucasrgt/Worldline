package worldline.b173server;

import java.io.DataOutputStream;
import java.io.IOException;
import worldline.api.BlockFace;
import worldline.api.BlockPosition;
import worldline.api.RemoteInventorySlot;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteItemStack;
import worldline.api.RemoteContainerWindow;
import worldline.api.RemoteWindowKind;

/** Strict outbound selected-item actions derived from authoritative inventory. */
final class B173HeldItemChannel {
    private final DataOutputStream output;
    private final B173PlayInbound inbound;
    private int selectedSlot;

    B173HeldItemChannel(DataOutputStream output, B173PlayInbound inbound) {
        this.output = output; this.inbound = inbound; }

    void select(int slot) throws IOException {
        if (slot < 0 || slot > 8) throw new IllegalArgumentException("invalid held hotbar slot");
        output.writeByte(16); output.writeShort(slot); output.flush(); selectedSlot = slot;
    }

    void drop() throws IOException {
        output.writeByte(14); output.writeByte(4); output.writeInt(0); output.writeByte(0);
        output.writeInt(0); output.writeByte(0); output.flush();
    }

    void place(BlockPosition support, BlockFace face) throws IOException {
        if (support == null || face == null) throw new IllegalArgumentException("null block placement");
        BlockPosition target = face.adjacent(support);
        if (support.y() < 0 || support.y() >= 128 || target.y() < 0 || target.y() >= 128)
            throw new IllegalArgumentException("placement outside world height");
        RemoteInventoryView inventory = inbound.inventory();
        if (inventory.windowId() != 0 || inventory.size() != 45)
            throw new IllegalStateException("player inventory window is not active");
        RemoteInventorySlot slot = inventory.slot(36 + selectedSlot);
        if (slot.empty()) throw new IllegalStateException("selected held slot is empty");
        RemoteItemStack item = slot.item();
        if (item.legacyId() < 1 || item.legacyId() > 255)
            throw new IllegalStateException("selected held item is not a legacy block");
        output.writeByte(16); output.writeShort(selectedSlot); output.writeByte(15);
        output.writeInt(support.x()); output.writeByte(support.y()); output.writeInt(support.z());
        output.writeByte(face(face)); output.writeShort(item.legacyId()); output.writeByte(item.count());
        output.writeShort(item.damage()); output.flush();
    }

    void use(BlockPosition position, BlockFace face) throws IOException {
        if (position == null || face == null || position.y() < 0 || position.y() >= 128)
            throw new IllegalArgumentException("invalid block activation");
        RemoteInventoryView inventory = inbound.inventory();
        if (inventory.windowId() != 0 || inventory.size() != 45
                || !inventory.slot(36 + selectedSlot).empty())
            throw new IllegalStateException("block activation requires an empty selected hand");
        output.writeByte(16); output.writeShort(selectedSlot); output.writeByte(15);
        output.writeInt(position.x()); output.writeByte(position.y()); output.writeInt(position.z());
        output.writeByte(face(face)); output.writeShort(-1); output.flush();
    }

    int closeWindow() throws IOException {
        if (!inbound.cursorObserved() || inbound.cursor() != null)
            throw new IllegalStateException("remote window close requires an observed empty cursor");
        RemoteContainerWindow active = inbound.activeWindow();
        if (active.descriptor().kind() == RemoteWindowKind.WORKBENCH)
            for (int slot = 0; slot < active.descriptor().playerTailOffset(); slot++)
                if (!active.inventory().slot(slot).empty())
                    throw new IllegalStateException("workbench close requires an empty result and matrix");
        int windowId = inbound.activeWindowId(); output.writeByte(101); output.writeByte(windowId);
        output.flush(); return windowId;
    }
    boolean selectedEquals(RemoteItemStack expected) { RemoteInventoryView inventory = inbound.inventory();
        RemoteInventorySlot slot = inventory.slot(36 + selectedSlot); return !slot.empty() && slot.item().equals(expected); }

    private static int face(BlockFace face) {
        switch (face) {
            case DOWN: return 0;
            case UP: return 1;
            case NORTH: return 2;
            case SOUTH: return 3;
            case WEST: return 4;
            case EAST: return 5;
            default: throw new IllegalArgumentException("unknown block face");
        }
    }
}
