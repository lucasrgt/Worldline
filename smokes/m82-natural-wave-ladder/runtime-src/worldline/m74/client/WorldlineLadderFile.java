package worldline.m74.client;

import java.io.*;
import java.nio.file.*;
import java.security.*;
import worldline.m74.*;

/** Writes the cardinality/request/event identity only after census seal. */
public final class WorldlineLadderFile {
  private static boolean written;
  private WorldlineLadderFile() {
  }
  public static boolean written() {
    return written;
  }
  public static void write() {
    if (written || !WorldlinePagedBridge.sealed() || !WorldlineLadderEvent.valid())
      throw new IllegalStateException(
          "invalid M82 write state " + WorldlineLadderEvent.diagnostic());
    Path p = Paths.get(System.getProperty("worldline.ladder.file", "")).toAbsolutePath();
    try {
      Files.createDirectories(p.getParent());
      try (DataOutputStream o =
               new DataOutputStream(Files.newOutputStream(p, StandardOpenOption.CREATE_NEW))) {
        o.writeInt(0x574c3832);
        o.writeInt(1);
        o.writeInt(44);
        o.writeInt(WorldlineCensusProbe.nonce());
        o.writeInt(WorldlineCensusSync.x());
        o.writeInt(WorldlineCensusSync.y());
        o.writeInt(WorldlineCensusSync.z());
        o.writeInt(WorldlineLadderEvent.requestIndex);
        o.writeInt(WorldlineLadderEvent.eventIndex);
        o.writeInt(Integer.getInteger("worldline.ladder.targets", 0));
        o.writeInt(WorldlineLadderEvent.expectedRebuilds());
      }
      byte[] b = Files.readAllBytes(p);
      StringBuilder h = new StringBuilder();
      for (byte v : MessageDigest.getInstance("SHA-256").digest(b))
        h.append(String.format("%02x", v & 255));
      written = true;
      System.out.println("[WorldlineLadder] complete requestIndex="
          + WorldlineLadderEvent.requestIndex + " eventIndex=" + WorldlineLadderEvent.eventIndex
          + " targets=" + Integer.getInteger("worldline.ladder.targets", 0) + " rebuilds="
          + WorldlineLadderEvent.expectedRebuilds() + " bytes=" + b.length + " sha256=" + h);
    } catch (IOException | NoSuchAlgorithmException e) {
      throw new IllegalStateException("M82 artifact write failed", e);
    }
  }
}
