package worldline.b173server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/** Public gzip-NBT level.dat weather patcher that preserves every unrelated tag value-semantically. */
public final class B173LevelDatWeather {
    private B173LevelDatWeather() {}

    public static final class Weather {
        private final boolean raining, thundering;
        private final int rainTime, thunderTime;
        private final long seed, time;
        private final int spawnX, spawnY, spawnZ;
        Weather(boolean raining, int rainTime, boolean thundering, int thunderTime,
                long seed, long time, int spawnX, int spawnY, int spawnZ) {
            this.raining = raining; this.rainTime = rainTime; this.thundering = thundering;
            this.thunderTime = thunderTime; this.seed = seed; this.time = time;
            this.spawnX = spawnX; this.spawnY = spawnY; this.spawnZ = spawnZ;
        }
        public boolean raining() { return raining; }
        public int rainTime() { return rainTime; }
        public boolean thundering() { return thundering; }
        public int thunderTime() { return thunderTime; }
        public long seed() { return seed; }
        public long time() { return time; }
        public int spawnX() { return spawnX; }
        public int spawnY() { return spawnY; }
        public int spawnZ() { return spawnZ; }
    }

    public static void patch(Path path, int rainTime, boolean raining, int thunderTime, boolean thundering) {
        if (path == null || rainTime <= 0 || thunderTime <= 0)
            throw new IllegalArgumentException("invalid weather patch");
        B173Nbt.Compound root = readTree(path);
        B173Nbt.Compound info = worldInfo(root);
        info.entries.put("raining", (byte) (raining ? 1 : 0));
        info.entries.put("rainTime", rainTime);
        info.entries.put("thundering", (byte) (thundering ? 1 : 0));
        info.entries.put("thunderTime", thunderTime);
        writeTree(path, root);
    }

    public static Weather read(Path path) {
        B173Nbt.Compound info = worldInfo(readTree(path));
        return new Weather(flag(info, "raining"), intValue(info, "rainTime"),
                flag(info, "thundering"), intValue(info, "thunderTime"),
                longValue(info, "RandomSeed"), longValue(info, "Time"),
                intValue(info, "SpawnX"), intValue(info, "SpawnY"), intValue(info, "SpawnZ"));
    }

    private static B173Nbt.Compound readTree(Path path) {
        try (DataInputStream input = new DataInputStream(new GZIPInputStream(Files.newInputStream(path)))) {
            return B173Nbt.read(input);
        } catch (IOException error) { throw new IllegalStateException("could not read level.dat", error); }
    }

    private static void writeTree(Path path, B173Nbt.Compound root) {
        try (DataOutputStream output = new DataOutputStream(
                new GZIPOutputStream(Files.newOutputStream(path)))) {
            B173Nbt.write(output, root);
        } catch (IOException error) { throw new IllegalStateException("could not write level.dat", error); }
    }

    private static B173Nbt.Compound worldInfo(B173Nbt.Compound root) {
        if (root.entries.containsKey("RandomSeed")) return root;
        Object data = root.entries.get("Data");
        if (data instanceof B173Nbt.Compound) return (B173Nbt.Compound) data;
        throw new IllegalStateException("level.dat has no world-info compound");
    }

    private static boolean flag(B173Nbt.Compound info, String name) {
        return ((Number) require(info, name)).byteValue() != 0;
    }

    private static int intValue(B173Nbt.Compound info, String name) {
        return ((Number) require(info, name)).intValue();
    }

    private static long longValue(B173Nbt.Compound info, String name) {
        return ((Number) require(info, name)).longValue();
    }

    private static Object require(B173Nbt.Compound info, String name) {
        Object value = info.entries.get(name);
        if (value == null) throw new IllegalStateException("level.dat has no " + name + " tag");
        return value;
    }
}
