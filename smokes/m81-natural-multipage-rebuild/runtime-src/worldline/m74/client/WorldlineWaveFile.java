package worldline.m74.client;

import java.io.*;
import java.nio.file.*;
import java.security.*;
import worldline.m74.*;

/** Writes the request/event identity only after the complete bracket seals. */
public final class WorldlineWaveFile {
  private static boolean written;
  private WorldlineWaveFile() {
  }
  public static boolean written() {
    return written;
  }
  public static void write() {
    if (written || !WorldlinePagedBridge.sealed() || !WorldlineWaveEvent.valid())
      throw new IllegalStateException("invalid M81 write state " + WorldlineWaveEvent.diagnostic());
    Path p = Paths.get(System.getProperty("worldline.wave.file", "")).toAbsolutePath();
    try {
      Files.createDirectories(p.getParent());
      try (DataOutputStream o =
               new DataOutputStream(Files.newOutputStream(p, StandardOpenOption.CREATE_NEW))) {
        o.writeInt(0x574c3831);
        o.writeInt(1);
        o.writeInt(40);
        o.writeInt(WorldlineCensusProbe.nonce());
        o.writeInt(WorldlineCensusSync.x());
        o.writeInt(WorldlineCensusSync.y());
        o.writeInt(WorldlineCensusSync.z());
        o.writeInt(WorldlineWaveEvent.requestIndex);
        o.writeInt(WorldlineWaveEvent.eventIndex);
        o.writeInt(2);
      }
      byte[] b = Files.readAllBytes(p);
      StringBuilder h = new StringBuilder();
      for (byte v : MessageDigest.getInstance("SHA-256").digest(b))
        h.append(String.format("%02x", v & 255));
      written = true;
      System.out.println("[WorldlineWave] complete requestIndex=" + WorldlineWaveEvent.requestIndex
          + " eventIndex=" + WorldlineWaveEvent.eventIndex + " targets=2 bytes=" + b.length
          + " sha256=" + h);
    } catch (IOException | NoSuchAlgorithmException e) {
      throw new IllegalStateException("M81 artifact write failed", e);
    }
  }
}
