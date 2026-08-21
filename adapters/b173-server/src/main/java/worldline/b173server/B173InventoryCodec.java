package worldline.b173server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import worldline.api.RemoteInventorySlot;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteItemStack;

/** Strict protocol-14 codec for full-window and incremental-slot inventory packets. */
final class B173InventoryCodec {
    private B173InventoryCodec() {}

    static RemoteInventoryView window(DataInputStream input) throws IOException {
        int windowId = input.readByte(), count = input.readShort();
        if (windowId < 0 || count < 0 || count > RemoteInventoryView.MAX_SLOTS)
            throw new IOException("invalid remote inventory window");
        List<RemoteInventorySlot> slots = new ArrayList<>(count);
        for (int index = 0; index < count; index++)
            slots.add(new RemoteInventorySlot(index, item(input)));
        return new RemoteInventoryView(windowId, slots);
    }

    static B173InventoryUpdate update(DataInputStream input) throws IOException {
        return new B173InventoryUpdate(input.readByte(), input.readShort(), item(input));
    }

    static void item(DataOutputStream output, RemoteItemStack item) throws IOException {
        if (item == null) { output.writeShort(-1); return; }
        output.writeShort(item.legacyId()); output.writeByte(item.count()); output.writeShort(item.damage());
    }

    private static RemoteItemStack item(DataInputStream input) throws IOException {
        int id = input.readShort(); if (id == -1) return null;
        if (id < -1) throw new IOException("invalid remote empty-item sentinel");
        int count = input.readUnsignedByte(), damage = input.readShort();
        try { return new RemoteItemStack(id, count, damage); }
        catch (IllegalArgumentException error) { throw new IOException("invalid remote item stack", error); }
    }
}
