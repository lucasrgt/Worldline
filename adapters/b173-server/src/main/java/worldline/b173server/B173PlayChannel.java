package worldline.b173server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import worldline.api.PlayerPose;
import worldline.api.RemoteChunkObservation;
import worldline.api.RemoteChunkSnapshot;
import worldline.api.RemoteWorldView;
import worldline.api.BlockPosition;
import worldline.api.BlockState;

/** Original bounded codec for the protocol-14 initial play-position exchange. */
final class B173PlayChannel {
    private final DataInputStream input;
    private final DataOutputStream output;
    private final B173PlayInbound inbound;
    private PlayerPose pose;
    private double stanceHeight;

    B173PlayChannel(DataInputStream input, DataOutputStream output, int timeoutMillis) {
        this.input = input; this.output = output;
        this.inbound = new B173PlayInbound(input, output, timeoutMillis);
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
            try { inbound.skip(packet); }
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
        return inbound.awaitChat();
    }

    RemoteChunkObservation awaitChunk() throws IOException {
        require(pose != null, "play channel is not synchronized");
        return inbound.awaitChunk().observation();
    }

    RemoteChunkSnapshot awaitChunkSnapshot() throws IOException {
        require(pose != null, "play channel is not synchronized");
        return inbound.awaitChunk();
    }

    RemoteWorldView awaitRemoteWorld(int minimumChunks) throws IOException {
        require(pose != null, "play channel is not synchronized");
        return inbound.awaitWorld(minimumChunks);
    }

    RemoteWorldView awaitRemoteChunk(int chunkX, int chunkZ) throws IOException {
        require(pose != null, "play channel is not synchronized"); return inbound.awaitChunk(chunkX, chunkZ);
    }

    void beginBreak(BlockPosition position) throws IOException { dig(position, 0); }
    void finishBreak(BlockPosition position) throws IOException { dig(position, 2); }

    RemoteWorldView awaitBlock(BlockPosition position, BlockState expected) throws IOException {
        require(pose != null, "play channel is not synchronized");
        return inbound.awaitBlock(position, expected);
    }

    RemoteWorldView sustainTicks(int ticks) throws IOException, InterruptedException {
        require(pose != null, "play channel is not synchronized");
        if (ticks < 1 || ticks > 1200) throw new IllegalArgumentException("invalid heartbeat tick count");
        for (int tick = 1; tick <= ticks; tick++) {
            if (tick % 20 == 0) acknowledge(pose.x(), pose.y(), pose.y() + stanceHeight,
                    pose.z(), pose.yaw(), pose.pitch());
            else { output.writeByte(10); output.writeBoolean(false); output.flush(); }
            Thread.sleep(50L); inbound.pumpAvailable();
        }
        return inbound.snapshot();
    }

    private void acknowledge(double x, double feetY, double clientY, double z,
            float yaw, float pitch) throws IOException {
        output.writeByte(13);
        output.writeDouble(x); output.writeDouble(feetY); output.writeDouble(clientY);
        output.writeDouble(z); output.writeFloat(yaw); output.writeFloat(pitch);
        output.writeBoolean(false); output.flush();
    }

    private void dig(BlockPosition position, int status) throws IOException {
        require(pose != null, "play channel is not synchronized");
        if (position == null || position.y() < 0 || position.y() >= 128)
            throw new IllegalArgumentException("invalid dig position");
        output.writeByte(14); output.writeByte(status); output.writeInt(position.x());
        output.writeByte(position.y()); output.writeInt(position.z()); output.writeByte(1); output.flush();
    }

    private void writeString(String value) throws IOException {
        output.writeShort(value.length());
        for (int index = 0; index < value.length(); index++) output.writeChar(value.charAt(index));
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
