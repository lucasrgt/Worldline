package worldline.m74.client;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import worldline.m74.WorldlineCensusProbe;
import worldline.m74.WorldlineCensusSync;
import worldline.m74.WorldlinePagedBridge;

/** Writes both request/event identities only after the complete bracket seals. */
public final class WorldlineRecoveryFile {
    private static boolean written; private WorldlineRecoveryFile() {} public static boolean written() { return written; }
    public static void write() {
        if (written || !WorldlinePagedBridge.sealed() || !WorldlineRecoveryEvent.valid()) throw new IllegalStateException("invalid M90 write state " + WorldlineRecoveryEvent.diagnostic());
        Path path = Paths.get(System.getProperty("worldline.recovery.file", "")).toAbsolutePath(); try { Files.createDirectories(path.getParent());
            try (DataOutputStream out = new DataOutputStream(Files.newOutputStream(path, StandardOpenOption.CREATE_NEW))) {
                out.writeInt(0x574c3930); out.writeInt(1); out.writeInt(52); out.writeInt(WorldlineCensusProbe.nonce());
                out.writeInt(WorldlineCensusSync.x()); out.writeInt(WorldlineCensusSync.y()); out.writeInt(WorldlineCensusSync.z());
                out.writeInt(WorldlineRecoveryEvent.removeRequest); out.writeInt(WorldlineRecoveryEvent.removeEvent);
                out.writeInt(WorldlineRecoveryEvent.restoreRequest); out.writeInt(WorldlineRecoveryEvent.restoreEvent);
                out.writeInt(WorldlineRecoveryEvent.removeRebuilds); out.writeInt(WorldlineRecoveryEvent.restoreRebuilds);
            }
            byte[] bytes = Files.readAllBytes(path); StringBuilder hash = new StringBuilder(); for (byte value : MessageDigest.getInstance("SHA-256").digest(bytes)) hash.append(String.format("%02x", value & 255));
            written = true; System.out.println("[WorldlineRecovery] complete removeRequest=" + WorldlineRecoveryEvent.removeRequest + " removeEvent=" + WorldlineRecoveryEvent.removeEvent
                    + " restoreRequest=" + WorldlineRecoveryEvent.restoreRequest + " restoreEvent=" + WorldlineRecoveryEvent.restoreEvent + " bytes=" + bytes.length + " sha256=" + hash);
        } catch (IOException | NoSuchAlgorithmException exception) { throw new IllegalStateException("M90 artifact write failed", exception); }
    }
}
