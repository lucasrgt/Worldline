package worldline.b173server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/** Exact stopped-world spawn patch for deterministic official-server fixtures. */
public final class B173LevelDatSpawn {
    private B173LevelDatSpawn() { }

    public static void patch(Path path, int x, int y, int z) {
        if (path == null || y < 0 || y > 127)
            throw new IllegalArgumentException("invalid level spawn patch");
        B173Nbt.Compound root = read(path);
        B173Nbt.Compound info = worldInfo(root);
        requireInt(info, "SpawnX"); requireInt(info, "SpawnY"); requireInt(info, "SpawnZ");
        info.entries.put("SpawnX", x);
        info.entries.put("SpawnY", y);
        info.entries.put("SpawnZ", z);
        write(path, root);
    }

    private static B173Nbt.Compound read(Path path) {
        try (DataInputStream input = new DataInputStream(
                new GZIPInputStream(Files.newInputStream(path)))) {
            return B173Nbt.read(input);
        } catch (IOException error) {
            throw new IllegalStateException("could not read level.dat spawn", error);
        }
    }

    private static void write(Path path, B173Nbt.Compound root) {
        try (DataOutputStream output = new DataOutputStream(
                new GZIPOutputStream(Files.newOutputStream(path)))) {
            B173Nbt.write(output, root);
        } catch (IOException error) {
            throw new IllegalStateException("could not write level.dat spawn", error);
        }
    }

    private static B173Nbt.Compound worldInfo(B173Nbt.Compound root) {
        if (root.entries.containsKey("RandomSeed")) return root;
        Object data = root.entries.get("Data");
        if (data instanceof B173Nbt.Compound) return (B173Nbt.Compound) data;
        throw new IllegalStateException("level.dat has no world-info compound");
    }

    private static void requireInt(B173Nbt.Compound info, String name) {
        if (!(info.entries.get(name) instanceof Integer))
            throw new IllegalStateException("level.dat has no int " + name);
    }
}
