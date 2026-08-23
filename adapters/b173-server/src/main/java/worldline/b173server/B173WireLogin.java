package worldline.b173server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/** Performs the protocol-14 handshake and creates a connected play channel. */
final class B173WireLogin {
    private B173WireLogin() { }

    static Result connect(String host, int port, String username, int timeoutMillis)
            throws IOException {
        Socket socket = new Socket();
        try {
            socket.connect(new InetSocketAddress(host, port), timeoutMillis);
            socket.setSoTimeout(timeoutMillis);
            DataInputStream input = new DataInputStream(socket.getInputStream());
            DataOutputStream output = new DataOutputStream(socket.getOutputStream());
            output.writeByte(2);
            B173InboundPacket.string(output, username);
            output.flush();
            require(input.readUnsignedByte() == 2, "handshake response packet drift");
            require(B173InboundPacket.string(input, 32).equals("-"),
                    "server did not use offline handshake");
            output.writeByte(1);
            output.writeInt(B173WireClient.PROTOCOL);
            B173InboundPacket.string(output, username);
            output.writeLong(0L);
            output.writeByte(0);
            output.flush();
            int packet = input.readUnsignedByte();
            if (packet == 255) {
                throw new IllegalStateException("login rejected: "
                        + B173InboundPacket.string(input, 256));
            }
            require(packet == 1, "login response packet drift: " + packet);
            int entityId = input.readInt();
            B173InboundPacket.string(input, 16);
            input.readLong();
            int dimension = input.readByte();
            require(dimension == 0 || dimension == -1,
                    "server returned invalid dimension");
            require(entityId >= 0, "server returned invalid entity id");
            B173PlayChannel play = new B173PlayChannel(input, output, timeoutMillis,
                    entityId, username, dimension);
            return new Result(socket, play, entityId, dimension);
        } catch (IOException | RuntimeException error) {
            try {
                socket.close();
            } catch (IOException closeError) {
                error.addSuppressed(closeError);
            }
            throw error;
        }
    }

    static final class Result {
        private final Socket socket;
        private final B173PlayChannel play;
        private final int entityId;
        private final int dimension;

        Result(Socket socket, B173PlayChannel play, int entityId, int dimension) {
            this.socket = socket;
            this.play = play;
            this.entityId = entityId;
            this.dimension = dimension;
        }

        Socket socket() { return socket; }
        B173PlayChannel play() { return play; }
        int entityId() { return entityId; }
        int dimension() { return dimension; }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
