package worldline.m74.client;

import java.io.*;
import java.nio.file.*;
import java.security.*;
import worldline.m74.*;

/** Cold-path serialization of the single disposal/rebuild event. */
public final class WorldlineColdFile {
  private static boolean written;
  private WorldlineColdFile() {
  }
  public static boolean written() {
    return written;
  }
  public static void write() {
    if (!WorldlinePagedBridge.sealed() || written || !WorldlineColdEvent.fired
        || WorldlineColdEvent.pending)
      throw new IllegalStateException("invalid M79 write state");
    Path path = Paths.get(System.getProperty("worldline.cold.file", "")).toAbsolutePath();
    try {
      Files.createDirectories(path.getParent());
      try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(
               Files.newOutputStream(path, StandardOpenOption.CREATE_NEW)))) {
        out.writeInt(0x574c3739);
        out.writeInt(1);
        out.writeInt(68);
        out.writeInt(WorldlineCensusProbe.nonce());
        out.writeInt(WorldlineCensusSync.x());
        out.writeInt(WorldlineCensusSync.y());
        out.writeInt(WorldlineCensusSync.z());
        out.writeInt(WorldlineColdEvent.index);
        out.writeLong(WorldlineColdEvent.disposeNs);
        out.writeInt(WorldlineColdEvent.cachedBefore);
        out.writeInt(WorldlineColdEvent.cachedAfterDispose);
        out.writeInt(WorldlineColdEvent.cachedAfterRebuild);
        out.writeInt(WorldlineColdEvent.compiledBefore);
        out.writeInt(WorldlineColdEvent.compiledAfter);
        out.writeInt(WorldlineColdEvent.deletedBefore);
        out.writeInt(WorldlineColdEvent.deletedAfter);
      }
      byte[] bytes = Files.readAllBytes(path);
      StringBuilder hash = new StringBuilder();
      for (byte b : MessageDigest.getInstance("SHA-256").digest(bytes))
        hash.append(String.format("%02x", b & 255));
      written = true;
      System.out.println("[WorldlineCold] complete eventIndex=" + WorldlineColdEvent.index
          + " disposeNs=" + WorldlineColdEvent.disposeNs + " bytes=" + bytes.length
          + " sha256=" + hash);
    } catch (IOException | NoSuchAlgorithmException e) {
      throw new IllegalStateException("M79 artifact write failed", e);
    }
  }
}
