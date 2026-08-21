package worldline.b173server;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;

final class B173LevelDat {
    private B173LevelDat() {}

    static long worldTime(Path path) {
        try (DataInputStream input = new DataInputStream(
                new GZIPInputStream(Files.newInputStream(path)))) {
            int type = input.readUnsignedByte();
            require(type == 10, "level.dat root is not a compound");
            input.readUTF();
            Long result = scanCompound(input);
            require(result != null, "level.dat has no Time tag");
            return result;
        } catch (IOException error) {
            throw new IllegalStateException("could not read level.dat", error);
        }
    }

    private static Long scanCompound(DataInputStream input) throws IOException {
        while (true) {
            int type = input.readUnsignedByte();
            if (type == 0) return null;
            String name = input.readUTF();
            if (type == 4 && name.equals("Time")) return input.readLong();
            Long nested = scanPayload(input, type);
            if (nested != null) return nested;
        }
    }

    private static Long scanPayload(DataInputStream input, int type) throws IOException {
        switch (type) {
            case 1: input.readByte(); return null;
            case 2: input.readShort(); return null;
            case 3: input.readInt(); return null;
            case 4: input.readLong(); return null;
            case 5: input.readFloat(); return null;
            case 6: input.readDouble(); return null;
            case 7: skip(input, input.readInt()); return null;
            case 8: input.readUTF(); return null;
            case 9:
                int child = input.readUnsignedByte();
                int count = input.readInt();
                for (int index = 0; index < count; index++) {
                    Long nested = scanPayload(input, child);
                    if (nested != null) return nested;
                }
                return null;
            case 10: return scanCompound(input);
            case 11: skip(input, Math.multiplyExact(input.readInt(), 4)); return null;
            default: throw new IllegalStateException("unknown NBT tag " + type);
        }
    }

    private static void skip(DataInputStream input, int bytes) throws IOException {
        if (bytes < 0) throw new IllegalStateException("negative NBT length");
        for (int remaining = bytes; remaining > 0; ) {
            int skipped = input.skipBytes(remaining);
            if (skipped == 0) throw new IllegalStateException("truncated NBT payload");
            remaining -= skipped;
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
