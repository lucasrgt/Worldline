package worldline.b173server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import worldline.api.PlayerPose;

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
        for (int count = 0; count < 128; count++) {
            int packet = input.readUnsignedByte();
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
            skipPrelude(packet);
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

    private void acknowledge(double x, double feetY, double clientY, double z,
            float yaw, float pitch) throws IOException {
        output.writeByte(13);
        output.writeDouble(x); output.writeDouble(feetY); output.writeDouble(clientY);
        output.writeDouble(z); output.writeFloat(yaw); output.writeFloat(pitch);
        output.writeBoolean(false); output.flush();
    }

    private void skipPrelude(int packet) throws IOException {
        switch (packet) {
            case 0: break;
            case 3: readString(256); break;
            case 4: skip(8); break;
            case 6: skip(12); break;
            case 50: skip(9); break;
            case 51: skipMapChunk(); break;
            case 70: skip(1); break;
            case 255: throw new IOException("server disconnected: " + readString(256));
            default: throw new IOException("unexpected play prelude packet " + packet);
        }
    }

    private void skipMapChunk() throws IOException {
        skip(13);
        int compressedBytes = input.readInt();
        if (compressedBytes < 0 || compressedBytes > 4_000_000)
            throw new IOException("invalid map chunk length " + compressedBytes);
        skip(compressedBytes);
    }

    private String readString(int maximum) throws IOException {
        int length = input.readShort();
        if (length < 0 || length > maximum) throw new IOException("invalid string length " + length);
        StringBuilder value = new StringBuilder(length);
        for (int index = 0; index < length; index++) value.append(input.readChar());
        return value.toString();
    }

    private void skip(int bytes) throws IOException {
        for (int remaining = bytes; remaining > 0; ) {
            int count = input.skipBytes(remaining);
            if (count <= 0) throw new IOException("truncated play prelude");
            remaining -= count;
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
