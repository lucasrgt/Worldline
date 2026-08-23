package worldline.smoke.chunktraversal;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.Set;
import worldline.api.PlayerPose;
import worldline.api.RemoteChunkSnapshot;
import worldline.api.RemoteWorldView;
import worldline.api.SustainedRemoteWorldMultiplayerSession;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173WireClient;

/** Proves deliberate boundary traversal drives Packet50 cache and native frame transitions. */
public final class ChunkTraversalSmoke {
  private static final String TRACE = "v1|server=official-b1.7.3|session=protocol14"
      + "|movement=quarter-block-up8-east-two-boundaries|heartbeat=pre40-step-post40"
      + "|lifecycle=packet50-unload-load|render=cache-chunk-map-native-tessellator|display=false";

  private ChunkTraversalSmoke() {
  }

  public static void main(String[] arguments) throws Exception {
    if (arguments.length != 6)
      throw new IllegalArgumentException(
          "usage: ChunkTraversalSmoke server.jar workspace port seed username ticks");
    Path jar = Paths.get(arguments[0]), workspace = Paths.get(arguments[1]);
    int port = Integer.parseInt(arguments[2]), ticks = Integer.parseInt(arguments[5]);
    long seed = Long.parseLong(arguments[3]);
    String username = arguments[4];
    Duration timeout = Duration.ofSeconds(90);
    B173DedicatedServer server =
        new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    SustainedRemoteWorldMultiplayerSession client =
        new B173WireClient("127.0.0.1", port, username, timeout);
    int steps, removedX, removedZ, addedX, addedZ;
    RemoteWorldView before, after;
    ChunkLifecycleFrame.Frame first, second;
    PlayerPose pose;
    try {
      server.boot();
      client.connect();
      pose = client.synchronizePose();
      int startChunkX = (int) Math.floor(pose.x()) >> 4;
      client.awaitRemoteChunk(startChunkX, (int) Math.floor(pose.z()) >> 4);
      before = worldline.test.WorldlineSmokeAwait.observe(client, ticks);
      double targetX = (startChunkX + 2) * 16 + 1.5D;
      steps = 0;
      for (int rise = 0; rise < 32; rise++) {
        pose = client.moveBy(0, 0.25D, 0);
        worldline.test.WorldlineSmokeAwait.observe(client, 1);
        steps++;
      }
      while (pose.x() + 0.000001D < targetX) {
        double delta = Math.min(0.25D, targetX - pose.x());
        pose = client.moveBy(delta, 0, 0);
        worldline.test.WorldlineSmokeAwait.observe(client, 1);
        if (++steps > 176)
          throw new IllegalStateException("boundary traversal exceeded step bound");
      }
      after = worldline.test.WorldlineSmokeAwait.observe(client, ticks);
      require(((int) Math.floor(pose.x()) >> 4) == startChunkX + 2,
          "client pose did not cross two chunk boundaries");
      Set<Long> removed = difference(before, after), added = difference(after, before);
      require(!removed.isEmpty() && !added.isEmpty(), "Packet50 unload/load transition absent");
      long removedKey = removed.iterator().next(), addedKey = added.iterator().next();
      removedX = x(removedKey);
      removedZ = z(removedKey);
      addedX = x(addedKey);
      addedZ = z(addedKey);
      int originX = Math.min(minX(before), minX(after)),
          originZ = Math.min(minZ(before), minZ(after));
      require(Math.max(maxX(before), maxX(after)) - originX < 12
              && Math.max(maxZ(before), maxZ(after)) - originZ < 12,
          "chunk-map span exceeded");
      first = ChunkLifecycleFrame.render(before, originX, originZ);
      second = ChunkLifecycleFrame.render(after, originX, originZ);
      require(first.chunkPixel(removedX, removedZ) == ChunkLifecycleFrame.LOADED
              && second.chunkPixel(removedX, removedZ) == ChunkLifecycleFrame.BACKGROUND,
          "unloaded chunk pixel did not clear");
      require(first.chunkPixel(addedX, addedZ) == ChunkLifecycleFrame.BACKGROUND
              && second.chunkPixel(addedX, addedZ) == ChunkLifecycleFrame.LOADED,
          "loaded chunk pixel did not appear");
      require(!first.hash.equals(second.hash), "chunk lifecycle frame did not change");
    } finally {
      client.close();
      server.close();
    }
    System.out.println(
        "WORLDLINE_M33_API=server,session,movement,heartbeat,cache-lifecycle,native-render");
    System.out.println("WORLDLINE_M33_TRAVERSAL=steps=" + steps
        + ",chunks=" + before.chunks().size() + "->" + after.chunks().size());
    System.out.println("WORLDLINE_M33_LIFECYCLE=removed=" + removedX + ":" + removedZ
        + ",added=" + addedX + ":" + addedZ);
    System.out.println("WORLDLINE_M33_FRAMES=" + first.hash + "->" + second.hash);
    System.out.println("WORLDLINE_M33_DISPLAY_CREATED=false");
    System.out.println("WORLDLINE_M33_RENDERER=" + ChunkLifecycleFrame.provenance());
    System.out.println("WORLDLINE_M33_TRACE=" + TRACE);
    System.out.println("WORLDLINE_M33_SIGNATURE=" + sha256(TRACE));
  }

  private static Set<Long> difference(RemoteWorldView left, RemoteWorldView right) {
    Set<Long> result = keys(left);
    result.removeAll(keys(right));
    return result;
  }
  private static Set<Long> keys(RemoteWorldView world) {
    Set<Long> result = new LinkedHashSet<>();
    for (RemoteChunkSnapshot chunk : world.chunks())
      result.add(key(chunk.observation().x() >> 4, chunk.observation().z() >> 4));
    return result;
  }
  private static int minX(RemoteWorldView world) {
    return world.chunks().stream().mapToInt(chunk -> chunk.observation().x() >> 4).min().getAsInt();
  }
  private static int maxX(RemoteWorldView world) {
    return world.chunks().stream().mapToInt(chunk -> chunk.observation().x() >> 4).max().getAsInt();
  }
  private static int minZ(RemoteWorldView world) {
    return world.chunks().stream().mapToInt(chunk -> chunk.observation().z() >> 4).min().getAsInt();
  }
  private static int maxZ(RemoteWorldView world) {
    return world.chunks().stream().mapToInt(chunk -> chunk.observation().z() >> 4).max().getAsInt();
  }
  private static long key(int x, int z) {
    return (long) x << 32 ^ z & 0xffffffffL;
  }
  private static int x(long key) {
    return (int) (key >> 32);
  }
  private static int z(long key) {
    return (int) key;
  }
  private static String sha256(String value) throws Exception {
    byte[] bytes =
        MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
    StringBuilder result = new StringBuilder();
    for (byte item : bytes)
      result.append(String.format("%02x", item & 255));
    return result.toString();
  }
  private static void require(boolean condition, String message) {
    if (!condition)
      throw new IllegalStateException(message);
  }
}
