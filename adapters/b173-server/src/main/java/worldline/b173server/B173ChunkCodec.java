package worldline.b173server;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;
import worldline.api.RemoteChunkObservation;
import worldline.api.RemoteChunkSnapshot;

/** Strict Packet51 decompressor and adapter-private legacy plane mapper. */
final class B173ChunkCodec {
    private static final int MAX_COMPRESSED = 4_000_000;
    private static final int MAX_BLOCKS = 1_600_000;

    private B173ChunkCodec() {}

    static RemoteChunkSnapshot read(DataInputStream input) throws IOException {
        int x = input.readInt(), y = input.readShort(), z = input.readInt();
        int width = input.readUnsignedByte() + 1;
        int height = input.readUnsignedByte() + 1;
        int depth = input.readUnsignedByte() + 1;
        int compressedSize = input.readInt();
        if (compressedSize < 1 || compressedSize > MAX_COMPRESSED)
            throw new IOException("invalid chunk payload length " + compressedSize);
        int volume;
        try { volume = Math.multiplyExact(Math.multiplyExact(width, height), depth); }
        catch (ArithmeticException error) { throw new IOException("chunk volume overflow", error); }
        if (volume < 2 || volume > MAX_BLOCKS || (volume & 1) != 0)
            throw new IOException("unsupported chunk volume " + volume);
        byte[] compressed = new byte[compressedSize]; input.readFully(compressed);
        byte[] raw = inflate(compressed, Math.multiplyExact(volume, 5) / 2);
        int half = volume / 2;
        return new RemoteChunkSnapshot(
                new RemoteChunkObservation(x, y, z, width, height, depth, compressedSize),
                Arrays.copyOfRange(raw, 0, volume),
                Arrays.copyOfRange(raw, volume, volume + half),
                Arrays.copyOfRange(raw, volume + half, volume * 2),
                Arrays.copyOfRange(raw, volume * 2, raw.length));
    }

    private static byte[] inflate(byte[] compressed, int expected) throws IOException {
        byte[] raw = new byte[expected]; Inflater inflater = new Inflater();
        try {
            inflater.setInput(compressed); int offset = 0;
            while (!inflater.finished() && offset < raw.length) {
                int count = inflater.inflate(raw, offset, raw.length - offset);
                if (count == 0 && inflater.needsInput()) break;
                if (count == 0 && inflater.needsDictionary())
                    throw new IOException("chunk payload requires a dictionary");
                if (count == 0) throw new IOException("chunk inflater made no progress");
                offset += count;
            }
            if (offset != expected || !inflater.finished() || inflater.getRemaining() != 0)
                throw new IOException("chunk inflated size mismatch");
            return raw;
        } catch (DataFormatException error) {
            throw new IOException("bad compressed chunk data", error);
        } finally { inflater.end(); }
    }
}
