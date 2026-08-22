package worldline.b173server;

import java.io.IOException;

/** Interacts with a furnace minecart while asserting a synchronized empty window state. */
public final class B173FurnaceCartInteract {
    private B173FurnaceCartInteract() {}

    public static void use(B173WireClient client, int entityId) {
        try {
            B173PlayChannel channel = client.channel(); int local = client.state().entityId();
            if (entityId < 1 || entityId == local)
                throw new IllegalArgumentException("invalid furnace-cart entity");
            if (channel.inbound().windowActive() || !channel.inbound().cursorObserved()
                    || channel.inbound().cursor() != null)
                throw new IllegalStateException("furnace-cart interact requires no active window and empty cursor");
            synchronized (channel.output) {
                channel.output.writeByte(7); channel.output.writeInt(local);
                channel.output.writeInt(entityId); channel.output.writeByte(0); channel.output.flush();
            }
        } catch (IOException error) {
            throw new IllegalStateException("furnace-cart interact failed", error);
        }
    }
}
