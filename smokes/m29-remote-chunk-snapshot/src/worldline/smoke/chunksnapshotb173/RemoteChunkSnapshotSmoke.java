package worldline.smoke.chunksnapshotb173;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.BlockState;
import worldline.api.RemoteChunkObservation;
import worldline.api.RemoteChunkSnapshot;
import worldline.api.RemoteWorldMultiplayerSession;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173WireClient;

/** Proves one real native chunk is strictly inflated into neutral block state. */
public final class RemoteChunkSnapshotSmoke {
  private static final String TRACE = "v1|server=official-b1.7.3|session=protocol14"
      + "|chunk=packet51|inflate=zlib-exact|layout=xzy-four-plane"
      + "|view=coordinate-addressable|oracle=mapped-nibble-array|disconnect=clean";

  private RemoteChunkSnapshotSmoke() {
  }

  public static void main(String[] arguments) throws Exception {
    if (arguments.length != 5)
      throw new IllegalArgumentException(
          "usage: RemoteChunkSnapshotSmoke server.jar workspace port seed username");
    Path jar = Paths.get(arguments[0]), workspace = Paths.get(arguments[1]);
    int port = Integer.parseInt(arguments[2]);
    long seed = Long.parseLong(arguments[3]);
    Duration timeout = Duration.ofSeconds(90);
    B173DedicatedServer server = new B173DedicatedServer(jar, workspace, port, seed, timeout);
    RemoteWorldMultiplayerSession client =
        new B173WireClient("127.0.0.1", port, arguments[4], timeout);
    RemoteChunkSnapshot snapshot;
    try {
      server.boot();
      client.connect();
      client.synchronizePose();
      snapshot = client.awaitChunkSnapshot();
      verify(snapshot);
    } finally {
      client.close();
      server.close();
    }
    RemoteChunkObservation region = snapshot.observation();
    System.out.println("WORLDLINE_M29_API=server,session,pose,inflate,remote-world-view");
    System.out.println("WORLDLINE_M29_CHUNK=" + region.x() + "," + region.y() + "," + region.z()
        + ",blocks=" + snapshot.blockCount() + ",nonair=" + snapshot.nonAirBlocks()
        + ",content=" + contentHash(snapshot));
    System.out.println("WORLDLINE_M29_TRACE=" + TRACE);
    System.out.println("WORLDLINE_M29_SIGNATURE=" + sha256(TRACE.getBytes(StandardCharsets.UTF_8)));
  }

  private static void verify(RemoteChunkSnapshot snapshot) {
    RemoteChunkObservation region = snapshot.observation();
    require(region.width() == 16 && region.height() == 128 && region.depth() == 16,
        "official server sent a non-full chunk");
    require(snapshot.blockCount() == 32768 && snapshot.nonAirBlocks() > 0
            && snapshot.nonAirBlocks() < snapshot.blockCount(),
        "implausible decoded block census");
    for (int x = 0; x < 16; x++)
      for (int z = 0; z < 16; z++) {
        BlockState floor = snapshot.blockAt(x, 0, z);
        require(floor.legacyId() >= 0 && floor.metadata() >= 0, "invalid floor block");
        require(snapshot.blockLightAt(x, 64, z) <= 15 && snapshot.skyLightAt(x, 127, z) <= 15,
            "invalid light nibble");
      }
  }

  private static String contentHash(RemoteChunkSnapshot snapshot) throws Exception {
    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    ByteBuffer sample = ByteBuffer.allocate(4);
    for (int x = 0; x < 16; x++)
      for (int z = 0; z < 16; z++)
        for (int y = 0; y < 128; y++) {
          BlockState block = snapshot.blockAt(x, y, z);
          sample.clear();
          sample.put((byte) block.legacyId())
              .put((byte) block.metadata())
              .put((byte) snapshot.blockLightAt(x, y, z))
              .put((byte) snapshot.skyLightAt(x, y, z));
          digest.update(sample.array());
        }
    return sha256(digest.digest());
  }

  private static String sha256(byte[] value) throws Exception {
    byte[] bytes = MessageDigest.getInstance("SHA-256").digest(value);
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
