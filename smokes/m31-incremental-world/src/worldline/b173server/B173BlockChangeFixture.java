package worldline.b173server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import worldline.api.BlockState;
import worldline.api.RemoteChunkObservation;
import worldline.api.RemoteChunkSnapshot;

/** Proves mapped Packet52/53 coordinate and plane application semantics. */
public final class B173BlockChangeFixture {
    private B173BlockChangeFixture() {}

    public static void main(String[] arguments) throws Exception {
        B173RemoteWorldCache cache = new B173RemoteWorldCache();
        cache.preChunk(prechunk(-1, 2)); cache.accept(chunk(-1, 2));
        cache.singleBlock(single(-14, 7, 35, 20, 3));
        require(cache.snapshot().blockAt(-14, 7, 35).equals(new BlockState(20, 3)),
                "Packet53 coordinate/state drift");
        cache.multiBlock(multi(-1, 2));
        require(cache.snapshot().blockAt(-15, 9, 34).equals(new BlockState(12, 4))
                && cache.snapshot().blockAt(-3, 127, 47).equals(new BlockState(5, 2)),
                "Packet52 coordinate/state drift");
        require(cache.changes() == 3, "incremental change count drift");
        System.out.println("WORLDLINE_M31_UPDATE_ORACLE=PASS");
    }

    private static DataInputStream prechunk(int x, int z) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream(); DataOutputStream out = new DataOutputStream(bytes);
        out.writeInt(x); out.writeInt(z); out.writeBoolean(true); out.close(); return input(bytes);
    }
    private static DataInputStream single(int x, int y, int z, int id, int meta) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream(); DataOutputStream out = new DataOutputStream(bytes);
        out.writeInt(x); out.writeByte(y); out.writeInt(z); out.writeByte(id); out.writeByte(meta);
        out.close(); return input(bytes);
    }
    private static DataInputStream multi(int x, int z) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream(); DataOutputStream out = new DataOutputStream(bytes);
        out.writeInt(x); out.writeInt(z); out.writeShort(2);
        out.writeShort(1 << 12 | 2 << 8 | 9); out.writeShort(13 << 12 | 15 << 8 | 127);
        out.writeByte(12); out.writeByte(5); out.writeByte(4); out.writeByte(2); out.close(); return input(bytes);
    }
    private static DataInputStream input(ByteArrayOutputStream bytes) {
        return new DataInputStream(new ByteArrayInputStream(bytes.toByteArray()));
    }
    private static RemoteChunkSnapshot chunk(int x, int z) {
        return new RemoteChunkSnapshot(new RemoteChunkObservation(x * 16, 0, z * 16,
                16, 128, 16, 1024), new byte[32768], new byte[16384], new byte[16384], new byte[16384]);
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
