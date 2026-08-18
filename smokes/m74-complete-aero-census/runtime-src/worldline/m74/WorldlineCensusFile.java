package worldline.m74;

import java.io.*;
import java.nio.file.*;

/** Cold-path binary serialization after the measured window is sealed. */
public final class WorldlineCensusFile {
    private WorldlineCensusFile() {}
    public static void write() {
        if (!WorldlineFrameCensus.sealed || WorldlineFrameCensus.written) throw new IllegalStateException("invalid M74 write state");
        Path path = Paths.get(System.getProperty("worldline.census.file", "")).toAbsolutePath(); try {
            Files.createDirectories(path.getParent()); try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(path, StandardOpenOption.CREATE_NEW)))) {
                out.writeInt(0x574c3734); out.writeInt(1); out.writeInt(28); out.writeInt(mode().equals("present") ? 16 : 0);
                out.writeInt(Integer.getInteger("worldline.census.nonce", 0)); out.writeInt(WorldlineCensusSync.x()); out.writeInt(WorldlineCensusSync.y()); out.writeInt(WorldlineCensusSync.z());
                out.writeInt(WorldlineFrameCensus.MIN); out.writeLong(WorldlineFrameCensus.MIN_NS); out.writeInt(WorldlineFrameCensus.count); out.writeLong(WorldlineFrameCensus.elapsed());
                for (int i = 0; i < WorldlineFrameCensus.count; i++) { out.writeLong(WorldlineFrameCensus.delta[i]); out.writeInt(WorldlineFrameCensus.renders[i]);
                    out.writeInt(WorldlineFrameCensus.lists[i]); out.writeInt(WorldlineFrameCensus.visible[i]); out.writeInt(WorldlineFrameCensus.calls[i]);
                    out.writeShort(WorldlineFrameCensus.state[i] & 0xffff); out.writeShort(WorldlineFrameCensus.masks[i] & 0xffff); }
            } WorldlineFrameCensus.written = true;
        } catch (IOException error) { throw new IllegalStateException("M74 artifact write failed", error); }
    }
    private static String mode() { return System.getProperty("worldline.census.mode", ""); }
}
