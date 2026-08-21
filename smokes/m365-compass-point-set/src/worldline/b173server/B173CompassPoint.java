package worldline.b173server;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;
import worldline.api.PlayerPose;
import worldline.api.RemoteItemStack;

/** Official Overworld spawn plus the vanilla compass-345 needle/bearing oracle. */
public final class B173CompassPoint {
    public static final RemoteItemStack COMPASS = new RemoteItemStack(345, 1, 0);
    private static final String[] WIND = {"N", "NE", "E", "SE", "S", "SW", "W", "NW"};
    public final int x, y, z;

    public B173CompassPoint(int x, int y, int z) {
        this.x = x; this.y = y; this.z = z;
    }

    public static B173CompassPoint read(Path level) {
        try (DataInputStream input = new DataInputStream(new GZIPInputStream(Files.newInputStream(level)))) {
            require(input.readUnsignedByte() == 10, "level.dat root is not a compound");
            input.readUTF();
            int[] spawn = new int[] {Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE};
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

    public int bearing(double px, double pz) {
        int deg = (int) Math.round(Math.toDegrees(Math.atan2(x - px, -(z - pz))));
        return ((deg % 360) + 360) % 360;
    }

    /**
     * Instantaneous TextureCompassFX target: (yaw-90)*pi/180 - atan2(spawnZ-z, spawnX-x).
     * Overworld only; Nether spin is not claimed.
     */
    public double target(double px, double pz, float yaw) {
        double angle = ((double) (yaw - 90.0F) * Math.PI / 180.0D) - Math.atan2(z - pz, x - px);
        if (angle > Math.PI) angle -= Math.PI * 2.0D;
        if (angle < -Math.PI) angle += Math.PI * 2.0D;
        if (angle > Math.PI) angle -= Math.PI * 2.0D;
        if (angle < -Math.PI) angle += Math.PI * 2.0D;
        return angle;
    }

    public String needle(double px, double pz, float yaw) {
        int deg = (int) Math.round(Math.toDegrees(target(px, pz, yaw)));
        return wind(((deg % 360) + 360) % 360);
    }

    public boolean oppositeNeedles(double px, double pz, float yaw0, float yaw1) {
        return (index(needle(px, pz, yaw0)) + 4) % 8 == index(needle(px, pz, yaw1));
    }

    private static String wind(int deg) {
        return WIND[((int) Math.round(deg / 45.0D) + 8) % 8];
    }

    private static int index(String name) {
        for (int i = 0; i < WIND.length; i++) if (WIND[i].equals(name)) return i;
        throw new IllegalStateException("unknown compass wind " + name);
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
                for (int index = 0; index < count; index++) skip(input, child, spawn);
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
