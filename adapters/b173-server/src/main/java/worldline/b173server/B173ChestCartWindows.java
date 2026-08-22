package worldline.b173server;

import java.io.IOException;
import worldline.api.RemoteContainerWindow;
import worldline.api.RemoteWindowKind;

/** Opens the official chest-minecart Packet100 window through Packet7. */
public final class B173ChestCartWindows {
    private B173ChestCartWindows() {}

    public static RemoteContainerWindow open(B173WireClient client, int entityId) {
        try {
            B173PlayChannel channel = client.channel(); int local = client.state().entityId();
            if (entityId < 1 || entityId == local)
                throw new IllegalArgumentException("invalid chest-cart entity");
            if (channel.inbound().windowActive() || !channel.inbound().cursorObserved()
                    || channel.inbound().cursor() != null)
                throw new IllegalStateException("chest-cart open requires no active window and empty cursor");
            synchronized (channel.output) {
                channel.output.writeByte(7); channel.output.writeInt(local);
                channel.output.writeInt(entityId); channel.output.writeByte(0); channel.output.flush();
            }
            channel.inbound().beginWindow(RemoteWindowKind.CHEST);
            return channel.inbound().awaitChest();
        } catch (IOException error) {
            throw new IllegalStateException("chest-cart window receive failed", error);
        }
    }
}
