package worldline.b173server;

import java.io.IOException;

/** Packet7 interact (leftClick=0) against a furnace minecart. Vanilla opens no Packet100. */
public final class B173FurnaceCartInteract {
    private B173FurnaceCartInteract() {}

    public static void use(B173WireClient client, int entityId) {
        try {
            B173PlayChannel channel = client.channel();
            int local = client.state().entityId();
            if (entityId < 1 || entityId == local)
                throw new IllegalArgumentException("invalid furnace-cart entity");
            if (channel.inbound().windowActive() || !channel.inbound().cursorObserved()
                    || channel.inbound().cursor() != null)
                throw new IllegalStateException("furnace-cart interact requires no active window and empty cursor");
            channel.output.writeByte(7);
            channel.output.writeInt(local);
            channel.output.writeInt(entityId);
            channel.output.writeByte(0);
            channel.output.flush();
        } catch (IOException error) {
            throw new IllegalStateException("furnace-cart interact failed", error);
        }
    }
}
