package worldline.b173server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/** Semantic gzip-NBT access to the persisted Beta 1.7.3 world Time long. */
public final class B173LevelDatTime {
    private B173LevelDatTime() {}

    public static long read(Path path) {
        Object value = worldInfo(readTree(path)).entries.get("Time");
        if (!(value instanceof Long)) throw new IllegalStateException("level.dat Time is not an NBT long");
        return (Long) value;
    }

    public static void patch(Path path, long time) {
        B173Nbt.Compound root = readTree(path);
        B173Nbt.Compound info = worldInfo(root);
        Object old = info.entries.get("Time");
        if (!(old instanceof Long)) throw new IllegalStateException("level.dat Time is not an NBT long");
        info.entries.put("Time", time);
        writeTree(path, root);
    }

    private static B173Nbt.Compound readTree(Path path) {
        try (DataInputStream input = new DataInputStream(
                new GZIPInputStream(Files.newInputStream(path)))) {
            return B173Nbt.read(input);
        } catch (IOException error) {
            throw new IllegalStateException("could not read level.dat", error);
        }
    }

    private static void writeTree(Path path, B173Nbt.Compound root) {
        try (DataOutputStream output = new DataOutputStream(
                new GZIPOutputStream(Files.newOutputStream(path)))) {
            B173Nbt.write(output, root);
        } catch (IOException error) {
            throw new IllegalStateException("could not write level.dat", error);
        }
    }

    private static B173Nbt.Compound worldInfo(B173Nbt.Compound root) {
        if (root.entries.containsKey("Time")) return root;
        Object data = root.entries.get("Data");
        if (data instanceof B173Nbt.Compound) return (B173Nbt.Compound) data;
        throw new IllegalStateException("level.dat has no world-info compound");
    }
}
