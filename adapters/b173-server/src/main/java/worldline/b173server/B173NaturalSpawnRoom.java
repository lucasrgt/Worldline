package worldline.b173server;

import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/** Creates a deterministic air room in an official generated Beta region chunk. */
public final class B173NaturalSpawnRoom {
    private static final int SECTOR = 4096;
    private B173NaturalSpawnRoom() { }

    public static void prepare(Path workspace, long seed, int centerX, int centerZ,
            int radius, int solidY, int floorY) {
        if (workspace == null || radius < 0 || radius > 4 || solidY < floorY + 4
                || solidY > 120 || floorY < 1)
            throw new IllegalArgumentException("invalid natural spawn room");
        for (int cx = centerX - radius; cx <= centerX + radius; cx++)
            for (int cz = centerZ - radius; cz <= centerZ + radius; cz++)
                shape(workspace, cx, cz, solidY, floorY,
                        B173SlimeTouchAccess.slimeChunk(seed, cx, cz));
    }

    private static void shape(Path workspace, int chunkX, int chunkZ, int solidY,
            int floorY, boolean room) {
        Path region = workspace.resolve("world/region/r." + Math.floorDiv(chunkX, 32) + "."
                + Math.floorDiv(chunkZ, 32) + ".mcr");
        try {
            byte[] file = Files.readAllBytes(region);
            int index = (chunkX & 31) + (chunkZ & 31) * 32, header = index * 4;
            int sector = unsigned24(file, header), sectors = file[header + 3] & 255;
            require(sector >= 2 && sectors >= 1, "generated chunk is absent from region");
            int offset = sector * SECTOR, length = integer(file, offset);
            require(length > 1 && length <= sectors * SECTOR - 4 && file[offset + 4] == 2,
                    "unsupported Beta region chunk encoding");
            byte[] raw = inflate(Arrays.copyOfRange(file, offset + 5, offset + 4 + length));
            int blocks = blocks(raw);
            for (int x = 0; x < 16; x++) for (int z = 0; z < 16; z++)
                for (int y = 1; y <= solidY; y++) raw[blocks + blockIndex(x, y, z)] = 1;
            if (room) for (int floor = floorY; floor + 4 <= Math.min(solidY, 15); floor += 5)
                for (int x = 1; x < 15; x++) for (int z = 1; z < 15; z++)
                    for (int y = floor + 1; y <= floor + 4; y++)
                        raw[blocks + blockIndex(x, y, z)] = 0;
            byte[] compressed = deflate(raw); int nextLength = compressed.length + 1;
            int required = Math.floorDiv(nextLength + 4 + SECTOR - 1, SECTOR);
            if (required > sectors) {
                require(file.length % SECTOR == 0 && required <= 255,
                        "region cannot allocate carved chunk");
                sector = file.length / SECTOR; sectors = required;
                file = Arrays.copyOf(file, file.length + sectors * SECTOR);
                putLocation(file, header, sector, sectors); offset = sector * SECTOR;
            }
            Arrays.fill(file, offset, offset + sectors * SECTOR, (byte) 0);
            putInteger(file, offset, nextLength); file[offset + 4] = 2;
            System.arraycopy(compressed, 0, file, offset + 5, compressed.length);
            Path temporary = Files.createTempFile(region.getParent(), "worldline-room-", ".mcr");
            Files.write(temporary, file);
            Files.move(temporary, region, StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);
        } catch (Exception error) {
            throw new IllegalStateException("could not prepare natural spawn geometry", error);
        }
    }

    private static int blocks(byte[] raw) {
        byte[] name = {'B', 'l', 'o', 'c', 'k', 's'};
        for (int at = 0; at <= raw.length - 13 - 32768; at++) {
            if (raw[at] != 7 || raw[at + 1] != 0 || raw[at + 2] != name.length) continue;
            boolean equal = true;
            for (int n = 0; n < name.length; n++) equal &= raw[at + 3 + n] == name[n];
            if (equal && integer(raw, at + 9) == 32768) return at + 13;
        }
        throw new IllegalStateException("chunk Blocks byte array absent");
    }

    private static byte[] inflate(byte[] compressed) throws Exception {
        Inflater inflater = new Inflater(); inflater.setInput(compressed);
        ByteArrayOutputStream output = new ByteArrayOutputStream(96_000); byte[] buffer = new byte[8192];
        try {
            while (!inflater.finished()) {
                int count = inflater.inflate(buffer);
                if (count == 0) throw new IllegalStateException("chunk inflate made no progress");
                output.write(buffer, 0, count);
            }
            return output.toByteArray();
        } finally { inflater.end(); }
    }

    private static byte[] deflate(byte[] raw) {
        Deflater deflater = new Deflater(Deflater.BEST_SPEED); deflater.setInput(raw); deflater.finish();
        ByteArrayOutputStream output = new ByteArrayOutputStream(raw.length); byte[] buffer = new byte[8192];
        try {
            while (!deflater.finished()) output.write(buffer, 0, deflater.deflate(buffer));
            return output.toByteArray();
        } finally { deflater.end(); }
    }

    private static int blockIndex(int x, int y, int z) { return x << 11 | z << 7 | y; }
    private static int unsigned24(byte[] value, int at) {
        return (value[at] & 255) << 16 | (value[at + 1] & 255) << 8 | value[at + 2] & 255;
    }
    private static int integer(byte[] value, int at) {
        return (value[at] & 255) << 24 | (value[at + 1] & 255) << 16
                | (value[at + 2] & 255) << 8 | value[at + 3] & 255;
    }
    private static void putInteger(byte[] value, int at, int number) {
        value[at] = (byte) (number >>> 24); value[at + 1] = (byte) (number >>> 16);
        value[at + 2] = (byte) (number >>> 8); value[at + 3] = (byte) number;
    }
    private static void putLocation(byte[] value, int at, int sector, int sectors) {
        value[at] = (byte) (sector >>> 16); value[at + 1] = (byte) (sector >>> 8);
        value[at + 2] = (byte) sector; value[at + 3] = (byte) sectors;
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
