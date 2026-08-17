package worldline.b173server;

import java.io.DataInputStream;
import java.io.IOException;
import worldline.api.RemoteInventoryView;

/** Replaces full windows and applies matching authoritative slot deltas. */
final class B173InventoryTracker {
    private RemoteInventoryView view;

    void accept(int packet, DataInputStream input) throws IOException {
        if (packet == 104) view = B173InventoryCodec.window(input);
        else if (packet == 103) view = B173InventoryCodec.slot(view, input);
        else throw new IllegalArgumentException("not an inventory packet");
    }

    RemoteInventoryView snapshot() { return view; }
}
