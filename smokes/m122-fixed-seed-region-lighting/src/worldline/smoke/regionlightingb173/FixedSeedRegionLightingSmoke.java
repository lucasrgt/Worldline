package worldline.smoke.regionlightingb173;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Observes both vanilla light planes across one exact 3x3 region. */
public final class FixedSeedRegionLightingSmoke {
  private FixedSeedRegionLightingSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 10)
      throw new IllegalArgumentException(
          "usage: FixedSeedRegionLightingSmoke server.jar workspace port seed username minX maxX minZ maxZ settleTicks");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    int minX = Integer.parseInt(a[5]), maxX = Integer.parseInt(a[6]), minZ = Integer.parseInt(a[7]),
        maxZ = Integer.parseInt(a[8]), settle = Integer.parseInt(a[9]);
    require(maxX - minX == 2 && maxZ - minZ == 2, "M122 requires exact 3x3 region");
    Duration timeout = Duration.ofSeconds(90);
    B173DedicatedServer first =
                            new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true),
                        second = null;
    B173WireClient loader = new B173WireClient("127.0.0.1", port, user, timeout), reader = null;
    RemoteWorldView world = null;
    try {
      first.boot();
      B173PlayerSeed.write(workspace, user, 8.5D, 120D, 8.5D);
      loader.connect();
      loader.synchronizePose();
      for (int x = minX; x <= maxX; x++)
        for (int z = minZ; z <= maxZ; z++)
          world = await(loader, x, z);
      loader.sustainTicks(settle);
      loader.close();
      awaitPlayers(first, 0);
      first.save();
      first.close();
      B173PlayerSeed.write(workspace, user, 8.5D, 120D, 8.5D);
      second = new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
      second.boot();
      reader = new B173WireClient("127.0.0.1", port, user, timeout);
      reader.connect();
      reader.synchronizePose();
      world = reader.sustainTicks(20);
      for (int x = minX; x <= maxX; x++)
        for (int z = minZ; z <= maxZ; z++)
          world = await(reader, x, z);
      verify(world, minX, maxX, minZ, maxZ);
    } finally {
      loader.close();
      if (reader != null)
        reader.close();
      first.close();
      if (second != null)
        second.close();
    }
    Plane block = plane(world, minX, maxX, minZ, maxZ, false),
          sky = plane(world, minX, maxX, minZ, maxZ, true);
    String evidence = "region=" + minX + ":" + maxX + ":" + minZ + ":" + maxZ
        + ",chunks=9,samples=" + block.samples + ",block=" + block.hash + ",sky=" + sky.hash
        + ",blockHist=" + block.histogram + ",skyHist=" + sky.histogram;
    String trace = "v1|server=official-b1.7.3|seed=" + seed + "|region=3x3-absolute-chunks-" + minX
        + ":" + maxX + ":" + minZ + ":" + maxZ + "|settle=" + settle
        + "ticks+clean-restart+fresh-packet51|samples=" + block.samples + "|block=" + block.hash
        + "|blockHist=" + block.histogram + "|sky=" + sky.hash + "|skyHist=" + sky.histogram
        + "|decode=packet51-nibbles-xzy|disconnect=clean";
    System.out.println("WORLDLINE_M122_LIGHT=" + evidence);
    System.out.println("WORLDLINE_M122_TRACE=" + trace);
    System.out.println("WORLDLINE_M122_SIGNATURE=" + sha(trace));
  }
  private static Plane plane(RemoteWorldView w, int minX, int maxX, int minZ, int maxZ, boolean sky)
      throws Exception {
    MessageDigest d = MessageDigest.getInstance("SHA-256");
    int[] count = new int[16];
    int samples = 0;
    for (int cx = minX; cx <= maxX; cx++)
      for (int cz = minZ; cz <= maxZ; cz++) {
        RemoteChunkSnapshot q = w.chunkAt(cx, cz);
        for (int x = 0; x < 16; x++)
          for (int z = 0; z < 16; z++)
            for (int y = 0; y < 128; y++) {
              int value = sky ? q.skyLightAt(x, y, z) : q.blockLightAt(x, y, z);
              d.update((byte) value);
              count[value]++;
              samples++;
            }
      }
    StringBuilder h = new StringBuilder();
    for (int i = 0; i < 16; i++) {
      if (i > 0)
        h.append(';');
      h.append(i).append(':').append(count[i]);
    }
    return new Plane(samples, hex(d.digest()), h.toString());
  }
  private static void verify(RemoteWorldView w, int minX, int maxX, int minZ, int maxZ) {
    require(w != null, "region absent");
    for (int x = minX; x <= maxX; x++)
      for (int z = minZ; z <= maxZ; z++) {
        RemoteChunkSnapshot q = w.chunkAt(x, z);
        require(q.observation().x() == x * 16 && q.observation().z() == z * 16
                && q.observation().y() == 0 && q.observation().width() == 16
                && q.observation().height() == 128 && q.observation().depth() == 16
                && q.blockCount() == 32768,
            "light chunk drift " + x + "," + z);
      }
  }
  private static RemoteWorldView await(B173WireClient c, int x, int z) {
    try {
      return c.awaitRemoteChunk(x, z);
    } catch (RuntimeException e) {
      throw new IllegalStateException("light chunk unavailable " + x + "," + z, e);
    }
  }
  private static void awaitPlayers(B173DedicatedServer s, int n) throws Exception {
    long end = System.currentTimeMillis() + 5000;
    while (System.currentTimeMillis() < end) {
      if (s.players().size() == n)
        return;
      Thread.sleep(100);
    }
    throw new IllegalStateException("player count drift");
  }
  private static String sha(String s) throws Exception {
    return hex(MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8)));
  }
  private static String hex(byte[] b) {
    StringBuilder s = new StringBuilder();
    for (byte v : b)
      s.append(String.format("%02x", v & 255));
    return s.toString();
  }
  private static void require(boolean v, String m) {
    if (!v)
      throw new IllegalStateException(m);
  }
  private static final class Plane {
    final int samples;
    final String hash, histogram;
    Plane(int s, String h, String i) {
      samples = s;
      hash = h;
      histogram = i;
    }
  }
}
