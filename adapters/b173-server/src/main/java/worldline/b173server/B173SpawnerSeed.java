package worldline.b173server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.Deflater;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.Inflater;
import worldline.api.BlockPosition;

/** Rewrites one official MobSpawner EntityId after a clean save, without /summon. */
public final class B173SpawnerSeed {
    private static final byte[] PIG = new byte[] {8, 0, 8, 'E', 'n', 't', 'i', 't', 'y', 'I', 'd', 0, 3, 'P', 'i', 'g'};

    private B173SpawnerSeed() {}

    public static void sheep(Path serverDirectory, BlockPosition spawner) {
        rewrite(serverDirectory, spawner, "Sheep", true); }
    public static void twoSheep(Path serverDirectory, BlockPosition spawner) {
        rewrite(serverDirectory, spawner, "Sheep", false); rewrite(serverDirectory, spawner, "Sheep", true); }
    public static void cow(Path serverDirectory, BlockPosition spawner) {
        rewrite(serverDirectory, spawner, "Cow", true); }
    public static void chicken(Path serverDirectory, BlockPosition spawner) {
        rewrite(serverDirectory, spawner, "Chicken", true); }
    public static void cowAndChicken(Path serverDirectory, BlockPosition spawner) {
        rewrite(serverDirectory, spawner, "Cow", false); rewrite(serverDirectory, spawner, "Chicken", true); }
    public static void ghast(Path serverDirectory, BlockPosition spawner) {
        rewrite(serverDirectory, spawner, "Ghast", true, "world/DIM-1/region"); }

    /** Replaces one saved MobSpawner EntityId Pig with a hostile vanilla name. */
    public static void entity(Path serverDirectory, BlockPosition spawner, String entityId) {
        if (entityId == null || !entityId.matches("Zombie|Skeleton|Spider|Creeper"))
            throw new IllegalArgumentException("invalid hostile EntityId");
        rewrite(serverDirectory, spawner, entityId, false);
    }

    private static void rewrite(Path serverDirectory, BlockPosition spawner, String entityId, boolean unique) {
        rewrite(serverDirectory, spawner, entityId, unique, "world/region"); }
    private static void rewrite(Path serverDirectory, BlockPosition spawner, String entityId, boolean unique, String regionDir) {
        if (serverDirectory == null || spawner == null || entityId == null || entityId.isEmpty() || regionDir == null)
            throw new IllegalArgumentException("invalid spawner seed");
        Path root = serverDirectory.toAbsolutePath().normalize();
        int cx = spawner.x() >> 4, cz = spawner.z() >> 4;
        Path region = root.resolve(regionDir + "/r." + (cx >> 5) + "." + (cz >> 5) + ".mcr").normalize();
        if (!region.startsWith(root) || !Files.isRegularFile(region))
            throw new IllegalStateException("spawner region absent");
        try (RandomAccessFile file = new RandomAccessFile(region.toFile(), "rw")) {
            int index = (cx & 31) + (cz & 31) * 32;
            file.seek(index * 4L);
            int loc = file.readInt(), offset = (loc >>> 8) * 4096, sectors = loc & 255;
            if (offset == 0 || sectors < 1) throw new IllegalStateException("spawner chunk absent");
            file.seek(offset);
            int length = file.readInt(), type = file.readUnsignedByte();
            if (length < 2 || type < 1 || type > 2) throw new IllegalStateException("invalid spawner chunk");
            byte[] compressed = new byte[length - 1];
            file.readFully(compressed);
            byte[] raw = inflate(compressed, type), patched = replace(raw, tag(entityId), unique), out = deflate(patched, type);
            int next = out.length + 1, need = (next + 4 + 4095) / 4096;
            if (need <= sectors) {
                file.seek(offset); file.writeInt(next); file.writeByte(type); file.write(out);
            } else {
                int start = (int) ((file.length() + 4095) / 4096);
                file.setLength((long) (start + need) * 4096L);
                file.seek((long) start * 4096L); file.writeInt(next); file.writeByte(type); file.write(out);
                file.seek(index * 4L); file.writeInt((start << 8) | need);
            }
        } catch (IOException error) { throw new IllegalStateException("could not retarget spawner", error); }
    }

    private static byte[] tag(String entityId) {
        byte[] name = new byte[] {'E', 'n', 't', 'i', 't', 'y', 'I', 'd'}, value = new byte[entityId.length()];
        for (int i = 0; i < value.length; i++) value[i] = (byte) entityId.charAt(i);
        byte[] out = new byte[13 + value.length];
        out[0] = 8; out[1] = 0; out[2] = 8;
        System.arraycopy(name, 0, out, 3, 8);
        out[11] = 0; out[12] = (byte) value.length;
        System.arraycopy(value, 0, out, 13, value.length);
        return out;
    }

    private static byte[] replace(byte[] raw, byte[] nextId, boolean unique) {
        int at = indexOf(raw);
        if (at < 0) throw new IllegalStateException("MobSpawner EntityId Pig absent");
        byte[] next = new byte[raw.length - PIG.length + nextId.length];
        System.arraycopy(raw, 0, next, 0, at);
        System.arraycopy(nextId, 0, next, at, nextId.length);
        System.arraycopy(raw, at + PIG.length, next, at + nextId.length, raw.length - at - PIG.length);
        if (unique && indexOf(next) >= 0) throw new IllegalStateException("duplicate MobSpawner EntityId");
        return next;
    }

    private static int indexOf(byte[] raw) {
        outer: for (int i = 0; i + PIG.length <= raw.length; i++) {
            for (int j = 0; j < PIG.length; j++) if (raw[i + j] != PIG[j]) continue outer;
            return i;
        }
        return -1;
    }

    private static byte[] inflate(byte[] compressed, int type) throws IOException {
        if (type == 1) {
            try (GZIPInputStream in = new GZIPInputStream(new ByteArrayInputStream(compressed))) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                byte[] buf = new byte[4096]; int n;
                while ((n = in.read(buf)) >= 0) out.write(buf, 0, n);
                return out.toByteArray();
            }
        }
        Inflater inflater = new Inflater();
        inflater.setInput(compressed);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        try {
            while (!inflater.finished()) {
                int n = inflater.inflate(buf);
                if (n == 0) break;
                out.write(buf, 0, n);
            }
        } catch (java.util.zip.DataFormatException error) {
            throw new IOException("spawner chunk inflate failed", error);
        } finally { inflater.end(); }
        return out.toByteArray();
    }

    private static byte[] deflate(byte[] raw, int type) throws IOException {
        if (type == 1) {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            try (GZIPOutputStream out = new GZIPOutputStream(bytes)) { out.write(raw); }
            return bytes.toByteArray();
        }
        Deflater deflater = new Deflater(Deflater.BEST_SPEED);
        deflater.setInput(raw); deflater.finish();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        try { while (!deflater.finished()) out.write(buf, 0, deflater.deflate(buf)); }
        finally { deflater.end(); }
        return out.toByteArray();
    }
}
