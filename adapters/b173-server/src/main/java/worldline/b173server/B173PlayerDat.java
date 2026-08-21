package worldline.b173server;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;
import worldline.api.ServerPlayerState;

final class B173PlayerDat {
    private B173PlayerDat() {}

    static ServerPlayerState read(Path path, String username) {
        try (DataInputStream input = new DataInputStream(new GZIPInputStream(Files.newInputStream(path)))) {
            require(input.readUnsignedByte() == 10, "player root is not a compound");
            input.readUTF();
            int dimension = Integer.MIN_VALUE, health = -1, inventory = -1;
            double[] position = null;
            float[] rotation = null;
            while (true) {
                int type = input.readUnsignedByte();
                if (type == 0) break;
                String name = input.readUTF();
                if (type == 3 && name.equals("Dimension")) dimension = input.readInt();
                else if (type == 2 && name.equals("Health")) health = input.readShort();
                else if (type == 9 && name.equals("Pos")) position = doubles(input, 3);
                else if (type == 9 && name.equals("Rotation")) rotation = floats(input, 2);
                else if (type == 9 && name.equals("Inventory")) inventory = listSizeAndSkip(input);
                else skipPayload(input, type);
            }
            require(dimension != Integer.MIN_VALUE && health >= 0 && inventory >= 0
                    && position != null && rotation != null,
                    "player NBT fields are incomplete");
            return new ServerPlayerState(username, dimension, position[0], position[1], position[2],
                    rotation[0], rotation[1], health, inventory);
        } catch (IOException error) { throw new IllegalStateException("could not read player data", error); }
    }

    private static float[] floats(DataInputStream input, int expected) throws IOException {
        require(input.readUnsignedByte() == 5, "rotation list is not float");
        int count = input.readInt();
        require(count == expected, "rotation list length drift");
        float[] result = new float[count];
        for (int index = 0; index < count; index++) result[index] = input.readFloat();
        return result;
    }

    private static double[] doubles(DataInputStream input, int expected) throws IOException {
        require(input.readUnsignedByte() == 6, "position list is not double");
        int count = input.readInt();
        require(count == expected, "position list length drift");
        double[] result = new double[count];
        for (int index = 0; index < count; index++) result[index] = input.readDouble();
        return result;
    }

    private static int listSizeAndSkip(DataInputStream input) throws IOException {
        int type = input.readUnsignedByte(), count = input.readInt();
        require(count >= 0, "negative list length");
        for (int index = 0; index < count; index++) skipPayload(input, type);
        return count;
    }

    private static void skipPayload(DataInputStream input, int type) throws IOException {
        switch (type) {
            case 1: input.readByte(); break;
            case 2: input.readShort(); break;
            case 3: input.readInt(); break;
            case 4: input.readLong(); break;
            case 5: input.readFloat(); break;
            case 6: input.readDouble(); break;
            case 7: skip(input, input.readInt()); break;
            case 8: input.readUTF(); break;
            case 9: listSizeAndSkip(input); break;
            case 10:
                while (true) {
                    int child = input.readUnsignedByte();
                    if (child == 0) break;
                    input.readUTF(); skipPayload(input, child);
                }
                break;
            case 11: skip(input, Math.multiplyExact(input.readInt(), 4)); break;
            default: throw new IllegalStateException("unknown NBT tag " + type);
        }
    }

    private static void skip(DataInputStream input, int bytes) throws IOException {
        require(bytes >= 0, "negative NBT length");
        for (int remaining = bytes; remaining > 0; ) {
            int skipped = input.skipBytes(remaining);
            require(skipped > 0, "truncated NBT payload"); remaining -= skipped;
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
