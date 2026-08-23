package worldline.m74.client;

import java.io.*;
import java.nio.file.*;
import java.security.*;
import worldline.m74.*;

/** Writes the topology/request/event identity only after census seal. */
public final class WorldlineTopologyFile {
  private static boolean written;
  private WorldlineTopologyFile() {
  }
  public static boolean written() {
    return written;
  }
  public static void write() {
    if (written || !WorldlinePagedBridge.sealed() || !WorldlineTopologyEvent.valid())
      throw new IllegalStateException(
          "invalid M84 write state " + WorldlineTopologyEvent.diagnostic());
    Path p = Paths.get(System.getProperty("worldline.topology.file", "")).toAbsolutePath();
    try {
      Files.createDirectories(p.getParent());
      try (DataOutputStream o =
               new DataOutputStream(Files.newOutputStream(p, StandardOpenOption.CREATE_NEW))) {
        o.writeInt(0x574c3834);
        o.writeInt(1);
        o.writeInt(44);
        o.writeInt(WorldlineCensusProbe.nonce());
        o.writeInt(WorldlineCensusSync.x());
        o.writeInt(WorldlineCensusSync.y());
        o.writeInt(WorldlineCensusSync.z());
        o.writeInt(WorldlineTopologyEvent.requestIndex);
        o.writeInt(WorldlineTopologyEvent.eventIndex);
        o.writeInt(Integer.getInteger("worldline.topology.code", 0));
        o.writeInt(WorldlineTopologyEvent.expectedRebuilds());
      }
      byte[] b = Files.readAllBytes(p);
      StringBuilder h = new StringBuilder();
      for (byte v : MessageDigest.getInstance("SHA-256").digest(b))
        h.append(String.format("%02x", v & 255));
      written = true;
      System.out.println("[WorldlineTopology] complete requestIndex="
          + WorldlineTopologyEvent.requestIndex + " eventIndex=" + WorldlineTopologyEvent.eventIndex
          + " topology=" + Integer.getInteger("worldline.topology.code", 0) + " targets=3 rebuilds="
          + WorldlineTopologyEvent.expectedRebuilds() + " bytes=" + b.length + " sha256=" + h);
    } catch (IOException | NoSuchAlgorithmException e) {
      throw new IllegalStateException("M84 artifact write failed", e);
    }
  }
}
