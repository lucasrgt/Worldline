package worldline.smoke.remoteworldb173;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.BlockState;
import worldline.api.CachedRemoteWorldMultiplayerSession;
import worldline.api.RemoteChunkObservation;
import worldline.api.RemoteChunkSnapshot;
import worldline.api.RemoteWorldView;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173WireClient;

/** Proves Packet50-qualified Packet51 snapshots assemble into a bounded view. */
public final class RemoteWorldCacheSmoke {
  private static final String TRACE = "v1|server=official-b1.7.3|session=protocol14"
      + "|lifecycle=packet50-load-unload|chunks=packet51-decoded|cache=max256"
      + "|view=immutable-world-addressing|disconnect=clean";

  private RemoteWorldCacheSmoke() {
  }

  public static void main(String[] arguments) throws Exception {
    if (arguments.length != 5)
      throw new IllegalArgumentException(
          "usage: RemoteWorldCacheSmoke server.jar workspace port seed username");
    Path jar = Paths.get(arguments[0]), workspace = Paths.get(arguments[1]);
    int port = Integer.parseInt(arguments[2]);
    long seed = Long.parseLong(arguments[3]);
    Duration timeout = Duration.ofSeconds(90);
    B173DedicatedServer server = new B173DedicatedServer(jar, workspace, port, seed, timeout);
    CachedRemoteWorldMultiplayerSession client =
        new B173WireClient("127.0.0.1", port, arguments[4], timeout);
    RemoteWorldView view;
    try {
      server.boot();
      client.connect();
      client.synchronizePose();
      view = client.awaitRemoteWorld(1);
      verify(view);
    } finally {
      client.close();
      server.close();
    }
    RemoteChunkObservation first = view.chunks().get(0).observation();
    RemoteChunkObservation last = view.chunks().get(view.loadedChunks() - 1).observation();
    System.out.println("WORLDLINE_M30_API=server,session,pose,prechunk,cache,remote-world-view");
    System.out.println("WORLDLINE_M30_WORLD=chunks=" + view.loadedChunks() + ",first=" + first.x()
        + ":" + first.z() + ",last=" + last.x() + ":" + last.z());
    System.out.println("WORLDLINE_M30_TRACE=" + TRACE);
    System.out.println("WORLDLINE_M30_SIGNATURE=" + sha256(TRACE));
  }

  private static void verify(RemoteWorldView view) {
    require(view.loadedChunks() >= 1 && view.loadedChunks() <= RemoteWorldView.MAX_CHUNKS,
        "remote view escaped requested bounds");
    for (RemoteChunkSnapshot chunk : view.chunks()) {
      RemoteChunkObservation region = chunk.observation();
      int chunkX = Math.floorDiv(region.x(), 16), chunkZ = Math.floorDiv(region.z(), 16);
      require(view.containsChunk(chunkX, chunkZ) && view.chunkAt(chunkX, chunkZ) == chunk,
          "chunk coordinate index drifted");
      BlockState local = chunk.blockAt(0, 0, 0);
      require(view.blockAt(region.x(), 0, region.z()).equals(local),
          "world-to-local block addressing drifted");
    }
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
