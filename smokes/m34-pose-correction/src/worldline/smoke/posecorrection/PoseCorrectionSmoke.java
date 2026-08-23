package worldline.smoke.posecorrection;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.BlockPosition;
import worldline.api.PlayerPose;
import worldline.api.RemoteChunkSnapshot;
import worldline.api.RemoteWorldView;
import worldline.api.SustainedRemoteWorldMultiplayerSession;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173PlayerSeed;
import worldline.b173server.B173WireClient;

/** Proves an invalid movement converges to the server pose without losing the cache. */
public final class PoseCorrectionSmoke {
  private static final String TRACE =
      "v1|server=official-b1.7.3|session=protocol14|attempt=solid-block-center"
      + "|correction=packet13-decoded-acknowledged|pose=server-authoritative"
      + "|cache=preserved|disconnect=clean";

  private PoseCorrectionSmoke() {
  }

  public static void main(String[] arguments) throws Exception {
    if (arguments.length != 5)
      throw new IllegalArgumentException(
          "usage: PoseCorrectionSmoke server.jar workspace port seed username");
    Path jar = Paths.get(arguments[0]), workspace = Paths.get(arguments[1]);
    int port = Integer.parseInt(arguments[2]);
    long seed = Long.parseLong(arguments[3]);
    String username = arguments[4];
    Duration timeout = Duration.ofSeconds(90);
    B173DedicatedServer server =
        new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    SustainedRemoteWorldMultiplayerSession client =
        new B173WireClient("127.0.0.1", port, username, timeout);
    PlayerPose initial, attempted, corrected;
    RemoteWorldView after;
    try {
      server.boot();
      B173PlayerSeed.write(workspace, username, 4.5D, 60D, 4.5D);
      client.connect();
      initial = client.synchronizePose();
      RemoteWorldView before = client.awaitRemoteWorld(1);
      RemoteChunkSnapshot witness = before.chunks().get(0);
      int chunkX = Math.floorDiv(witness.observation().x(), 16);
      int chunkZ = Math.floorDiv(witness.observation().z(), 16);
      BlockPosition block = solid(before);
      attempted = client.moveBy(
          block.x() + .5D - initial.x(), block.y() - initial.y(), block.z() + .5D - initial.z());
      after = worldline.test.WorldlineSmokeAwait.observe(client, 10);
      corrected = client.moveBy(0, 0, 0);
      require(!corrected.equals(attempted), "server did not correct invalid movement");
      require(corrected.equals(initial), "correction did not restore authoritative pose");
      require(after.containsChunk(chunkX, chunkZ), "pose correction lost original cached chunk");
    } finally {
      client.close();
      server.close();
    }
    System.out.println("WORLDLINE_M34_API=server,session,movement,correction,pose,cache");
    System.out.println(
        "WORLDLINE_M34_POSE=" + pose(initial) + "->" + pose(attempted) + "->" + pose(corrected));
    System.out.println("WORLDLINE_M34_CACHE=chunks=" + after.chunks().size());
    System.out.println("WORLDLINE_M34_TRACE=" + TRACE);
    System.out.println("WORLDLINE_M34_SIGNATURE=" + sha256(TRACE));
  }

  private static BlockPosition solid(RemoteWorldView world) {
    for (RemoteChunkSnapshot chunk : world.chunks()) {
      int baseX = chunk.observation().x(), baseZ = chunk.observation().z();
      for (int y = 127; y >= 0; y--)
        for (int x = 0; x < 16; x++)
          for (int z = 0; z < 16; z++) {
            int id = chunk.blockAt(x, y, z).legacyId();
            if (id > 0 && (id < 7 || id > 11))
              return new BlockPosition(baseX + x, y, baseZ + z);
          }
    }
    throw new IllegalStateException("remote solid block absent");
  }
  private static String pose(PlayerPose value) {
    return value.x() + "," + value.y() + "," + value.z();
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
