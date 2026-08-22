package worldline.b173server;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;
import worldline.api.PlayerPose;
import worldline.api.RemoteItemStack;

/** Reads official Overworld spawn data and exposes a candidate compass target. */
public final class B173CompassPoint {
    public static final RemoteItemStack COMPASS = new RemoteItemStack(345, 1, 0);
    public final int x, y, z;

    public B173CompassPoint(int x, int y, int z) {
        this.x = x; this.y = y; this.z = z;
    }

    public static B173CompassPoint read(Path level) {
        try (DataInputStream input = new DataInputStream(new GZIPInputStream(Files.newInputStream(level)))) {
            require(input.readUnsignedByte() == 10, "level.dat root is not a compound");
            input.readUTF();
            int[] spawn = {Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE};
            scan(input, spawn);
            require(spawn[0] != Integer.MIN_VALUE && spawn[1] != Integer.MIN_VALUE
                    && spawn[2] != Integer.MIN_VALUE, "level.dat is missing SpawnX/Y/Z");
            return new B173CompassPoint(spawn[0], spawn[1], spawn[2]);
        } catch (IOException error) {
            throw new IllegalStateException("could not read compass spawn", error);
        }
    }

    public String token() { return x + ":" + y + ":" + z; }

    public String cell(PlayerPose pose) {
        return floor(pose.x()) + ":" + floor(pose.y()) + ":" + floor(pose.z());
    }

    private static void scan(DataInputStream input, int[] spawn) throws IOException {
        while (true) {
            int type = input.readUnsignedByte();
            if (type == 0) return;
            String name = input.readUTF();
            if (type == 3 && name.equals("SpawnX")) spawn[0] = input.readInt();
            else if (type == 3 && name.equals("SpawnY")) spawn[1] = input.readInt();
            else if (type == 3 && name.equals("SpawnZ")) spawn[2] = input.readInt();
            else skip(input, type, spawn);
        }
    }

    private static void skip(DataInputStream input, int type, int[] spawn) throws IOException {
        switch (type) {
            case 1: input.readByte(); return;
            case 2: input.readShort(); return;
            case 3: input.readInt(); return;
            case 4: input.readLong(); return;
            case 5: input.readFloat(); return;
            case 6: input.readDouble(); return;
            case 7: drop(input, input.readInt()); return;
            case 8: input.readUTF(); return;
            case 9:
                int child = input.readUnsignedByte(), count = input.readInt();
                for (int item = 0; item < count; item++) skip(input, child, spawn);
                return;
            case 10: scan(input, spawn); return;
            case 11: drop(input, Math.multiplyExact(input.readInt(), 4)); return;
            default: throw new IOException("unknown NBT tag " + type);
        }
    }

    private static void drop(DataInputStream input, int bytes) throws IOException {
        if (bytes < 0) throw new IOException("negative NBT length");
        for (int remaining = bytes; remaining > 0; ) {
            int skipped = input.skipBytes(remaining);
            if (skipped == 0) throw new IOException("truncated NBT payload");
            remaining -= skipped;
        }
    }

    private static int floor(double value) { return (int) Math.floor(value); }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
