package worldline.b173server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import worldline.api.PlayerPose;
import worldline.api.RemoteChunkObservation;

/** Original bounded codec for the protocol-14 initial play-position exchange. */
final class B173PlayChannel {
    private final DataInputStream input;
    private final DataOutputStream output;
    private PlayerPose pose;
    private double stanceHeight;

    B173PlayChannel(DataInputStream input, DataOutputStream output) {
        this.input = input; this.output = output;
    }

    PlayerPose synchronize() throws IOException {
        require(pose == null, "play channel was already synchronized");
        StringBuilder packets = new StringBuilder();
        for (int count = 0; count < 128; count++) {
            int packet = input.readUnsignedByte();
            if (packets.length() > 0) packets.append(',');
            packets.append(packet);
            if (packet == 13) {
                double x = input.readDouble();
                double clientY = input.readDouble();
                double feetY = input.readDouble();
                double z = input.readDouble();
                float yaw = input.readFloat(), pitch = input.readFloat();
                input.readBoolean();
                require(clientY > feetY && clientY - feetY < 2.0D,
                        "server position stance drift");
                stanceHeight = clientY - feetY;
                pose = new PlayerPose(x, feetY, z, yaw, pitch);
                acknowledge(x, feetY, clientY, z, yaw, pitch);
                return pose;
            }
            try { skipPrelude(packet); }
            catch (IOException error) { throw new IOException(
                    "play prelude failed after packets " + packets, error); }
        }
        throw new IOException("initial play position packet absent");
    }

    void look(float yaw, float pitch) throws IOException {
        require(pose != null, "play channel is not synchronized");
        new PlayerPose(pose.x(), pose.y(), pose.z(), yaw, pitch);
        output.writeByte(12);
        output.writeFloat(yaw); output.writeFloat(pitch); output.writeBoolean(false);
        output.flush();
        pose = new PlayerPose(pose.x(), pose.y(), pose.z(), yaw, pitch);
    }

    PlayerPose moveBy(double deltaX, double deltaY, double deltaZ) throws IOException {
        require(pose != null, "play channel is not synchronized");
        PlayerPose target = new PlayerPose(pose.x() + deltaX, pose.y() + deltaY,
                pose.z() + deltaZ, pose.yaw(), pose.pitch());
        output.writeByte(13);
        output.writeDouble(target.x()); output.writeDouble(target.y());
        output.writeDouble(target.y() + stanceHeight); output.writeDouble(target.z());
        output.writeFloat(target.yaw()); output.writeFloat(target.pitch());
        output.writeBoolean(true); output.flush();
        pose = target; return target;
    }

    void sendChat(String message) throws IOException {
        require(pose != null, "play channel is not synchronized");
        if (message == null || message.trim().isEmpty() || message.length() > 100)
            throw new IllegalArgumentException("invalid chat message");
        output.writeByte(3); writeString(message); output.flush();
    }

    String awaitChat() throws IOException {
        require(pose != null, "play channel is not synchronized");
        for (int count = 0; count < 2048; count++) {
            int packet = input.readUnsignedByte();
            if (packet == 3) return B173InboundPacket.string(input, 119);
            if (packet == 255) throw new IOException(
                    "server disconnected: " + B173InboundPacket.string(input, 256));
            B173InboundPacket.skip(input, packet);
        }
        throw new IOException("chat packet absent from bounded inbound window");
    }

    RemoteChunkObservation awaitChunk() throws IOException {
        require(pose != null, "play channel is not synchronized");
        for (int count = 0; count < 4096; count++) {
            int packet = input.readUnsignedByte();
            if (packet == 51) return B173InboundPacket.chunk(input);
            if (packet == 3) { B173InboundPacket.string(input, 119); continue; }
            if (packet == 255) throw new IOException(
                    "server disconnected: " + B173InboundPacket.string(input, 256));
            B173InboundPacket.skip(input, packet);
        }
        throw new IOException("chunk packet absent from bounded inbound window");
    }

    private void acknowledge(double x, double feetY, double clientY, double z,
            float yaw, float pitch) throws IOException {
        output.writeByte(13);
        output.writeDouble(x); output.writeDouble(feetY); output.writeDouble(clientY);
        output.writeDouble(z); output.writeFloat(yaw); output.writeFloat(pitch);
        output.writeBoolean(false); output.flush();
    }

    private void skipPrelude(int packet) throws IOException {
        if (packet == 3) B173InboundPacket.string(input, 119);
        else if (packet == 255) throw new IOException(
                "server disconnected: " + B173InboundPacket.string(input, 256));
        else B173InboundPacket.skip(input, packet);
    }

    private void writeString(String value) throws IOException {
        output.writeShort(value.length());
        for (int index = 0; index < value.length(); index++) output.writeChar(value.charAt(index));
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
