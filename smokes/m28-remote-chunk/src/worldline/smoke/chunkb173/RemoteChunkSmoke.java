package worldline.smoke.chunkb173;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.ChunkMultiplayerSession;
import worldline.api.RemoteChunkObservation;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173WireClient;

/** Proves one bounded native remote chunk envelope reaches the neutral API. */
public final class RemoteChunkSmoke {
  private static final String TRACE = "v1|server=official-b1.7.3|session=protocol14"
      + "|pose=synchronized|chunk=packet51|origin=observed|dimensions=16x128x16"
      + "|payload=bounded|disconnect=clean";

  private RemoteChunkSmoke() {
  }

  public static void main(String[] arguments) throws Exception {
    if (arguments.length != 5)
      throw new IllegalArgumentException(
          "usage: RemoteChunkSmoke server.jar workspace port seed username");
    Path jar = Paths.get(arguments[0]), workspace = Paths.get(arguments[1]);
    int port = Integer.parseInt(arguments[2]);
    long seed = Long.parseLong(arguments[3]);
    String username = arguments[4];
    Duration timeout = Duration.ofSeconds(90);
    B173DedicatedServer server = new B173DedicatedServer(jar, workspace, port, seed, timeout);
    ChunkMultiplayerSession client = new B173WireClient("127.0.0.1", port, username, timeout);
    RemoteChunkObservation chunk;
    try {
      server.boot();
      client.connect();
      client.synchronizePose();
      chunk = client.awaitChunk();
      require(chunk.width() == 16 && chunk.height() == 128 && chunk.depth() == 16,
          "official server sent a non-full chunk envelope");
      require(chunk.payloadBytes() > 0 && chunk.payloadBytes() <= 4_000_000,
          "chunk payload escaped bounds");
    } finally {
      client.close();
      server.close();
    }
    System.out.println("WORLDLINE_M28_API=server,session,pose,inbound-pump,remote-chunk");
    System.out.println("WORLDLINE_M28_CHUNK=" + chunk.x() + "," + chunk.y() + "," + chunk.z() + ","
        + chunk.width() + "x" + chunk.height() + "x" + chunk.depth()
        + ",bytes=" + chunk.payloadBytes());
    System.out.println("WORLDLINE_M28_TRACE=" + TRACE);
    System.out.println("WORLDLINE_M28_SIGNATURE=" + sha256(TRACE));
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
