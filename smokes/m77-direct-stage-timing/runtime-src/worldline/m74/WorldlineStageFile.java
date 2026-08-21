package worldline.m74;

import java.io.*;
import java.nio.file.*;
import java.security.*;

/** Cold-path sidecar serialization after the aligned M74 window seals. */
public final class WorldlineStageFile {
    private static boolean written;
    private WorldlineStageFile() {}
    public static boolean written() { return written; }
    public static void write() {
        if (!WorldlineFrameCensus.sealed || written) throw new IllegalStateException("invalid M77 write state");
        Path path = Paths.get(System.getProperty("worldline.stage.file", "")).toAbsolutePath(); try {
            Files.createDirectories(path.getParent()); try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(
                    Files.newOutputStream(path, StandardOpenOption.CREATE_NEW)))) {
                out.writeInt(0x574c3737); out.writeInt(1); out.writeInt(44); out.writeInt(32); out.writeInt(WorldlineCensusProbe.nonce());
                out.writeInt(WorldlineCensusSync.x()); out.writeInt(WorldlineCensusSync.y()); out.writeInt(WorldlineCensusSync.z());
                out.writeInt(WorldlineFrameCensus.count); out.writeLong(WorldlineFrameCensus.elapsed());
                for (int i = 0; i < WorldlineFrameCensus.count; i++) { out.writeLong(WorldlineStageTimer.renderer[i]); out.writeLong(WorldlineStageTimer.queue[i]);
                    out.writeLong(WorldlineStageTimer.flush[i]); out.writeShort(WorldlineStageTimer.rendererCalls[i] & 0xffff);
                    out.writeShort(WorldlineStageTimer.queueCalls[i] & 0xffff); out.writeShort(WorldlineStageTimer.flushCalls[i] & 0xffff); out.writeShort(0); }
            }
            byte[] bytes = Files.readAllBytes(path); StringBuilder hash = new StringBuilder();
            for (byte value : MessageDigest.getInstance("SHA-256").digest(bytes)) hash.append(String.format("%02x", value & 0xff));
            written = true; System.out.println("[WorldlineStage] complete samples=" + WorldlineFrameCensus.count + " bytes=" + bytes.length + " sha256=" + hash);
        } catch (IOException | NoSuchAlgorithmException error) { throw new IllegalStateException("M77 artifact write failed", error); }
    }
}
