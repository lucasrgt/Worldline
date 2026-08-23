package worldline.m74.client;

import java.io.*;
import java.nio.file.*;
import java.security.*;
import worldline.m74.WorldlineCensusProbe;
import worldline.m74.WorldlineCensusSync;
import worldline.m74.WorldlinePagedBridge;

/** Writes eviction evidence plus twelve request/event/index identities. */
public final class WorldlineRecoveryFile {
  private static boolean written;
  private WorldlineRecoveryFile() {
  }
  public static boolean written() {
    return written;
  }
  public static void write() {
    if (written || !WorldlinePagedBridge.sealed() || !WorldlineRecoveryEvent.valid())
      throw new IllegalStateException(
          "invalid M94 write state " + WorldlineRecoveryEvent.diagnostic());
    Path path = Paths.get(System.getProperty("worldline.recovery.file", "")).toAbsolutePath();
    try {
      Files.createDirectories(path.getParent());
      try (DataOutputStream out =
               new DataOutputStream(Files.newOutputStream(path, StandardOpenOption.CREATE_NEW))) {
        out.writeInt(0x574c3934);
        out.writeInt(1);
        out.writeInt(184);
        out.writeInt(WorldlineCensusProbe.nonce());
        out.writeInt(WorldlineCensusSync.x());
        out.writeInt(WorldlineCensusSync.y());
        out.writeInt(WorldlineCensusSync.z());
        out.writeInt(WorldlineRecoveryEvent.expiry);
        out.writeInt(WorldlineRecoveryEvent.expiredBefore);
        out.writeInt(WorldlineRecoveryEvent.expiredAfter);
        for (int i = 0; i < 12; i++) {
          out.writeInt(WorldlineRecoveryEvent.requests[i]);
          out.writeInt(WorldlineRecoveryEvent.events[i]);
          out.writeInt(WorldlineRecoveryEvent.indices[i]);
        }
      }
      byte[] bytes = Files.readAllBytes(path);
      StringBuilder hash = new StringBuilder();
      for (byte value : MessageDigest.getInstance("SHA-256").digest(bytes))
        hash.append(String.format("%02x", value & 255));
      written = true;
      System.out.println("[WorldlineRecovery] complete " + WorldlineRecoveryEvent.diagnostic()
          + " bytes=" + bytes.length + " sha256=" + hash);
    } catch (IOException | NoSuchAlgorithmException e) {
      throw new IllegalStateException("M94 artifact write failed", e);
    }
  }
}
