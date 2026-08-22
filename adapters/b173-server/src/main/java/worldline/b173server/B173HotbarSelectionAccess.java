package worldline.b173server;

import java.io.IOException;

/** Explicit empty-selection and invalid Packet16 probe boundary. */
public final class B173HotbarSelectionAccess {
    private B173HotbarSelectionAccess() { }

    public static boolean selectedEmpty(B173WireClient client) {
        if (client == null) throw new IllegalArgumentException("missing wire client");
        return client.channel().selectedEmpty();
    }

    public static void sendInvalidSlot(B173WireClient client, int slot) {
        if (client == null || slot >= 0 && slot <= 8)
            throw new IllegalArgumentException("slot is not an invalid hotbar selection");
        try { client.channel().output.writeByte(16); client.channel().output.writeShort(slot);
            client.channel().output.flush(); }
        catch (IOException error) { throw new IllegalStateException("invalid Packet16 probe failed", error); }
    }
}
