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
import worldline.api.RemoteDroppedItem;
import worldline.api.RemoteItemStack;

/** Smoke-local Packet29 wait plus McRegion EntityItem Age rewrite. */
public final class B173ItemDespawn {
    private static final byte[] AGE = new byte[] {2, 0, 3, 'A', 'g', 'e'};
    private static final RemoteItemStack COBBLE = new RemoteItemStack(4, 1, 0);

    private B173ItemDespawn() {}

    public static void await(B173WireClient actor, RemoteDroppedItem item) {
        for (int n = 0; n < 8; n++) {
            if (actor.channel().inbound().itemCollected(item))
                throw new IllegalStateException("Packet22 collection is not item despawn");
            if (held(actor)) throw new IllegalStateException("dropped cobble was collected");
            if (finished(actor, item)) return;
            actor.sustainTicks(80);
        }
        throw new IllegalStateException("Packet29 item despawn absent");
    }

    public static boolean finished(B173WireClient actor, RemoteDroppedItem item) {
        return actor.channel().inbound().itemDespawned(item)
                && !actor.channel().inbound().itemCollected(item);
    }

    public static boolean held(B173WireClient actor) {
        for (int slot = 0; slot <= 44; slot++)
            if (!actor.inventory().slot(slot).empty() && actor.inventory().slot(slot).item().equals(COBBLE))
                return true;
        return false;
    }

    public static void age(Path serverDirectory, int x, int z, int age) {
        if (serverDirectory == null || age < 0 || age >= 6000)
            throw new IllegalArgumentException("invalid item age seed");
        Path root = serverDirectory.toAbsolutePath().normalize();
        int cx = x >> 4, cz = z >> 4;
        Path region = root.resolve("world/region/r." + (cx >> 5) + "." + (cz >> 5) + ".mcr").normalize();
        if (!region.startsWith(root) || !Files.isRegularFile(region))
            throw new IllegalStateException("item region absent");
        try (RandomAccessFile file = new RandomAccessFile(region.toFile(), "rw")) {
            int index = (cx & 31) + (cz & 31) * 32;
            file.seek(index * 4L);
            int loc = file.readInt(), offset = (loc >>> 8) * 4096, sectors = loc & 255;
            if (offset == 0 || sectors < 1) throw new IllegalStateException("item chunk absent");
            file.seek(offset);
            int length = file.readInt(), type = file.readUnsignedByte();
            if (length < 2 || type < 1 || type > 2) throw new IllegalStateException("invalid item chunk");
            byte[] compressed = new byte[length - 1];
            file.readFully(compressed);
            byte[] raw = inflate(compressed, type), patched = replace(raw, age), out = deflate(patched, type);
            int next = out.length + 1, need = (next + 4 + 4095) / 4096;
            if (need <= sectors) {
                file.seek(offset); file.writeInt(next); file.writeByte(type); file.write(out);
            } else {
                int start = (int) ((file.length() + 4095) / 4096);
                file.setLength((long) (start + need) * 4096L);
                file.seek((long) start * 4096L); file.writeInt(next); file.writeByte(type); file.write(out);
                file.seek(index * 4L); file.writeInt((start << 8) | need);
            }
        } catch (IOException error) { throw new IllegalStateException("could not age dropped item", error); }
    }

    private static byte[] replace(byte[] raw, int age) {
        int at = -1, found = 0;
        outer: for (int i = 0; i + AGE.length + 2 <= raw.length; i++) {
            for (int j = 0; j < AGE.length; j++) if (raw[i + j] != AGE[j]) continue outer;
            at = i; found++;
        }
        if (found != 1) throw new IllegalStateException("EntityItem Age tag count " + found);
        raw[at + AGE.length] = (byte) (age >> 8);
        raw[at + AGE.length + 1] = (byte) age;
        return raw;
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
            throw new IOException("item chunk inflate failed", error);
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
