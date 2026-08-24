package worldline.b173server;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.time.Duration;
import worldline.api.RemoteKeepAliveTimeout;
import worldline.api.RemoteProtocol14Chain;
import worldline.api.RemoteSignText;

/** Public edge-packet boundary for one qualified protocol-14 play stream. */
public final class B173Protocol14Access {
    private B173Protocol14Access() { }

    public static void reset(B173WireClient client) {
        client.channel().inbound().protocol().reset();
    }

    public static RemoteProtocol14Chain awaitSignThenMap(
            B173WireClient client, RemoteSignText expectedSign) {
        if (expectedSign == null) throw new IllegalArgumentException("null expected sign");
        try { return client.channel().inbound().awaitProtocolChain(expectedSign); }
        catch (IOException error) {
            throw new IllegalStateException("protocol-14 sign/map chain absent", error);
        }
    }

    public static RemoteKeepAliveTimeout awaitSilentTimeout(
            B173WireClient client, Duration deadline) {
        if (deadline == null || deadline.compareTo(Duration.ofSeconds(45)) < 0
                || deadline.compareTo(Duration.ofMinutes(3)) > 0)
            throw new IllegalArgumentException("invalid silent timeout deadline");
        DataInputStream input = client.channel().input();
        long started = System.nanoTime(), limit = started + deadline.toNanos();
        int keepAlivePackets = 0;
        try {
            for (int count = 0; count < 65_536 && System.nanoTime() < limit; count++) {
                int packet = input.readUnsignedByte();
                if (packet == 0) { keepAlivePackets++; continue; }
                if (packet == 255) {
                    B173InboundPacket.string(input, 256);
                    return closed(keepAlivePackets, started);
                }
                B173InboundPacket.skip(input, packet);
            }
            throw new IOException("silent disconnect packet absent before deadline");
        } catch (EOFException expected) {
            return closed(keepAlivePackets, started);
        } catch (IOException error) {
            throw new IllegalStateException("protocol-14 silent timeout failed", error);
        }
    }

    private static RemoteKeepAliveTimeout closed(int keepAlivePackets, long started) {
        return new RemoteKeepAliveTimeout(keepAlivePackets,
                (System.nanoTime() - started) / 1_000_000L, true);
    }
}
