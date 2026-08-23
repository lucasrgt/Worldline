package worldline.m74.client;

import java.io.*;
import java.nio.file.*;
import java.security.*;
import worldline.m74.*;

/** Cold-path serialization of timing plus exact page-path counters. */
public final class WorldlinePagedFile {
  private static boolean written;
  private WorldlinePagedFile() {
  }
  public static boolean written() {
    return written;
  }
  public static void write() {
    if (!WorldlinePagedBridge.sealed() || written)
      throw new IllegalStateException("invalid M97 write state");
    Path path = Paths.get(System.getProperty("worldline.paged.file", "")).toAbsolutePath();
    try {
      Files.createDirectories(path.getParent());
      try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(
               Files.newOutputStream(path, StandardOpenOption.CREATE_NEW)))) {
        out.writeInt(0x574c3937);
        out.writeInt(1);
        out.writeInt(44);
        out.writeInt(56);
        out.writeInt(WorldlineCensusProbe.nonce());
        out.writeInt(WorldlineCensusSync.x());
        out.writeInt(WorldlineCensusSync.y());
        out.writeInt(WorldlineCensusSync.z());
        out.writeInt(WorldlinePagedBridge.count());
        out.writeLong(WorldlinePagedBridge.elapsed());
        for (int i = 0; i < WorldlinePagedBridge.count(); i++) {
          out.writeLong(WorldlinePagedTimer.renderer[i]);
          out.writeLong(WorldlinePagedTimer.queue[i]);
          out.writeLong(WorldlinePagedTimer.flush[i]);
          out.writeShort(WorldlinePagedTimer.rendererCalls[i] & 0xffff);
          out.writeShort(WorldlinePagedTimer.queueCalls[i] & 0xffff);
          out.writeShort(WorldlinePagedTimer.flushCalls[i] & 0xffff);
          out.writeShort(0);
          out.writeInt(WorldlinePagedTimer.queued[i]);
          out.writeInt(WorldlinePagedTimer.pageCalls[i]);
          out.writeInt(WorldlinePagedTimer.direct[i]);
          out.writeInt(WorldlinePagedTimer.rebuilds[i]);
          out.writeInt(WorldlinePagedTimer.cached[i]);
          out.writeInt(WorldlinePagedTimer.evicted[i]);
        }
      }
      byte[] bytes = Files.readAllBytes(path);
      StringBuilder hash = new StringBuilder();
      for (byte b : MessageDigest.getInstance("SHA-256").digest(bytes))
        hash.append(String.format("%02x", b & 255));
      written = true;
      System.out.println("[WorldlinePaged] complete samples=" + WorldlinePagedBridge.count()
          + " bytes=" + bytes.length + " sha256=" + hash);
    } catch (IOException | NoSuchAlgorithmException e) {
      throw new IllegalStateException("M97 artifact write failed", e);
    }
  }
}
