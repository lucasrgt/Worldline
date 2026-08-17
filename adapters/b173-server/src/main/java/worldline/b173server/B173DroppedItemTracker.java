package worldline.b173server;

import java.io.DataInputStream;
import java.io.IOException;
import worldline.api.RemoteDroppedItem;
import worldline.api.RemoteItemStack;

/** Decodes the latest strict protocol-14 dropped-item spawn. */
final class B173DroppedItemTracker {
    private RemoteDroppedItem latest;

    void spawn(DataInputStream input) throws IOException {
        int entityId = input.readInt(), itemId = input.readShort(), count = input.readUnsignedByte();
        int damage = input.readShort(); double x = input.readInt() / 32D;
        double y = input.readInt() / 32D, z = input.readInt() / 32D;
        double velocityX = input.readByte() / 128D, velocityY = input.readByte() / 128D;
        double velocityZ = input.readByte() / 128D;
        try { latest = new RemoteDroppedItem(entityId, new RemoteItemStack(itemId, count, damage),
                x, y, z, velocityX, velocityY, velocityZ); }
        catch (IllegalArgumentException | NullPointerException error) {
            throw new IOException("invalid dropped-item spawn", error); }
    }

    RemoteDroppedItem matching(RemoteItemStack expected) {
        if (expected == null) throw new IllegalArgumentException("null expected dropped item");
        return latest != null && latest.item().equals(expected) ? latest : null;
    }
}
