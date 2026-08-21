package worldline.b173server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.zip.Deflater;
import net.minecraft.src.NibbleArray;
import worldline.api.BlockState;
import worldline.api.RemoteChunkSnapshot;

/** Compares adapter indexing with the mapped vanilla NibbleArray implementation. */
public final class B173ChunkCodecFixture {
    private static final int WIDTH = 16, HEIGHT = 128, DEPTH = 16;

    private B173ChunkCodecFixture() {}

    public static void main(String[] arguments) throws Exception {
        int volume = WIDTH * HEIGHT * DEPTH;
        byte[] ids = new byte[volume];
        NibbleArray metadata = new NibbleArray(volume);
        NibbleArray blockLight = new NibbleArray(volume);
        NibbleArray skyLight = new NibbleArray(volume);
        for (int x = 0; x < WIDTH; x++) for (int z = 0; z < DEPTH; z++) {
            for (int y = 0; y < HEIGHT; y++) {
                int index = (x * DEPTH + z) * HEIGHT + y;
                ids[index] = (byte) ((x * 17 + y * 3 + z * 5) & 255);
                metadata.setNibble(x, y, z, (x + y + z) & 15);
                blockLight.setNibble(x, y, z, (x + y) & 15);
                skyLight.setNibble(x, y, z, (15 - z + y) & 15);
            }
        }
        byte[] raw = planes(ids, metadata.data, blockLight.data, skyLight.data);
        byte[] encoded = packet(raw);
        RemoteChunkSnapshot snapshot = B173ChunkCodec.read(
                new DataInputStream(new ByteArrayInputStream(encoded)));
        for (int x = 0; x < WIDTH; x++) for (int z = 0; z < DEPTH; z++) {
            for (int y = 0; y < HEIGHT; y++) {
                int index = (x * DEPTH + z) * HEIGHT + y;
                require(snapshot.blockAt(x, y, z).equals(new BlockState(
                        ids[index] & 255, metadata.getNibble(x, y, z))), "block layout drift");
                require(snapshot.blockLightAt(x, y, z) == blockLight.getNibble(x, y, z),
                        "block-light layout drift");
                require(snapshot.skyLightAt(x, y, z) == skyLight.getNibble(x, y, z),
                        "sky-light layout drift");
            }
        }
        byte[] truncated = java.util.Arrays.copyOf(encoded, encoded.length - 1);
        try {
            B173ChunkCodec.read(new DataInputStream(new ByteArrayInputStream(truncated)));
            throw new AssertionError("truncated chunk was accepted");
        } catch (IOException expected) { }
        System.out.println("WORLDLINE_M29_MAPPED_ORACLE=PASS");
    }

    private static byte[] planes(byte[]... values) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        for (byte[] value : values) bytes.write(value);
        return bytes.toByteArray();
    }

    private static byte[] packet(byte[] raw) throws IOException {
        Deflater deflater = new Deflater(-1); deflater.setInput(raw); deflater.finish();
        byte[] compressed = new byte[raw.length]; int size = deflater.deflate(compressed); deflater.end();
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream output = new DataOutputStream(bytes);
        output.writeInt(32); output.writeShort(0); output.writeInt(-16);
        output.writeByte(WIDTH - 1); output.writeByte(HEIGHT - 1); output.writeByte(DEPTH - 1);
        output.writeInt(size); output.write(compressed, 0, size); output.close();
        return bytes.toByteArray();
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
