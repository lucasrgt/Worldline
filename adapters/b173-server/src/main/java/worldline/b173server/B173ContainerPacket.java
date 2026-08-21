package worldline.b173server;

import java.io.DataOutputStream;
import java.io.IOException;
import worldline.api.RemoteItemStack;

/** Strict Packet102 encoder shared with byte-level smoke fixtures. */
final class B173ContainerPacket {
    private B173ContainerPacket() {}
    static void write(DataOutputStream output, int windowId, int slot, int button,
            int action, RemoteItemStack predicted) throws IOException {
        if (windowId < 0 || windowId > 100 || slot < 0 || slot > 127
                || button < 0 || button > 1 || action < 1 || action > 32767)
            throw new IllegalArgumentException("invalid container click packet");
        output.writeByte(102); output.writeByte(windowId); output.writeShort(slot);
        output.writeByte(button); output.writeShort(action); output.writeBoolean(false);
        B173InventoryCodec.item(output, predicted);
    }
}
