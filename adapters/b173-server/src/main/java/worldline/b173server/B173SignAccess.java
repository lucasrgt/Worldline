package worldline.b173server;

import java.io.DataOutputStream;
import java.io.IOException;
import worldline.api.RemoteSignText;

/** Public Packet130 sign-text boundary kept out of the capped play client. */
public final class B173SignAccess {
    private B173SignAccess() {}

    public static void update(B173WireClient client, RemoteSignText text) {
        if (text == null) throw new IllegalArgumentException("null sign text");
        try {
            B173PlayChannel channel = client.channel();
            channel.inbound();
            DataOutputStream output = channel.output;
            synchronized (output) {
                output.writeByte(RemoteSignText.PACKET);
                output.writeInt(text.position().x());
                output.writeShort(text.position().y());
                output.writeInt(text.position().z());
                for (int index = 0; index < 4; index++)
                    B173InboundPacket.string(output, text.line(index));
                output.flush();
            }
        } catch (IOException error) {
            throw new IllegalStateException("sign text send failed", error);
        }
    }

    public static RemoteSignText poll(B173WireClient client) {
        return client.channel().inbound().signs().take();
    }

    public static RemoteSignText await(B173WireClient client) {
        try { return client.channel().awaitSignText(); }
        catch (IOException error) { throw new IllegalStateException("sign text receive failed", error); }
    }

    public static RemoteSignText await(B173WireClient client, RemoteSignText expected) {
        if (expected == null) throw new IllegalArgumentException("null expected sign text");
        RemoteSignText pending = poll(client);
        if (expected.equals(pending)) return pending;
        for (int attempt = 0; attempt < 8; attempt++) {
            RemoteSignText value = await(client);
            if (value.equals(expected)) return value;
        }
        throw new IllegalStateException("expected sign text absent");
    }
}
