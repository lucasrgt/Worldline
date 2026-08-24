package worldline.smoke.crosslanematrixb173;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import worldline.api.RemoteChunkSnapshot;
import worldline.api.RemoteWorldView;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173PlayerSeed;
import worldline.b173server.B173WireClient;

/** Captures a bounded fixed-seed terrain matrix from the official server. */
public final class CrossLaneSeedMatrixSmoke {
  private static final Duration TIMEOUT = Duration.ofSeconds(120);

  private CrossLaneSeedMatrixSmoke() {
  }

  public static void main(String[] arguments) throws Exception {
    if (arguments.length != 6)
      throw new IllegalArgumentException(
          "usage: CrossLaneSeedMatrixSmoke server.jar workspace port seeds chunks usernamePrefix");
    Path jar = Paths.get(arguments[0]), workspace = Paths.get(arguments[1]);
    int port = Integer.parseInt(arguments[2]);
    long[] seeds = seeds(arguments[3]);
    int[][] chunks = chunks(arguments[4]);
    require(seeds.length >= 2 && chunks.length >= 2, "matrix must span multiple seeds and chunks");
    List<String> cases = new ArrayList<>();
    for (int index = 0; index < seeds.length; index++)
      captureSeed(jar, workspace.resolve("seed-" + index), port, seeds[index],
          arguments[5] + index, chunks, cases);
    String matrix = sha256(String.join("\n", cases) + "\n");
    String signal = "seeds=" + seeds.length + ",chunks=" + chunks.length + ",cases="
        + cases.size() + ",matrix=" + matrix + ",clients=" + seeds.length + ",disconnect=clean";
    String trace = "v1|server=official-b1.7.3|fixture=fresh-world-per-seed|matrix="
        + seeds.length + "x" + chunks.length + "|seeds=" + arguments[3] + "|chunks="
        + arguments[4] + "|oracle=nonair+solid-occupancy|protocol=14|disconnect=clean";
    Files.write(workspace.resolve("matrix-evidence.txt"),
        (String.join("\n", cases) + "\n" + signal + "\n").getBytes(StandardCharsets.UTF_8));
    System.out.println("WORLDLINE_M634_CASE_COUNT=" + cases.size());
    System.out.println("WORLDLINE_M634_MATRIX=" + signal);
    System.out.println("WORLDLINE_M634_TRACE=" + trace);
    System.out.println("WORLDLINE_M634_SIGNATURE=" + sha256(trace));
  }

  private static void captureSeed(Path jar, Path workspace, int port, long seed, String username,
      int[][] chunks, List<String> cases) throws Exception {
    B173DedicatedServer server = new B173DedicatedServer(jar, workspace, port, seed, TIMEOUT, 3, true);
    B173WireClient client = new B173WireClient("127.0.0.1", port, username, TIMEOUT);
    try {
      server.boot();
      B173PlayerSeed.write(workspace, username, 8.5D, 120.0D, 8.5D);
      client.connect();
      client.synchronizePose();
      for (int[] coordinate : chunks) {
        RemoteWorldView world = client.awaitRemoteChunk(coordinate[0], coordinate[1]);
        RemoteChunkSnapshot chunk = world.chunkAt(coordinate[0], coordinate[1]);
        cases.add(caseRow(seed, chunk, coordinate[0], coordinate[1]));
      }
    } finally {
      client.close();
      server.close();
    }
  }

  private static String caseRow(long seed, RemoteChunkSnapshot chunk, int chunkX, int chunkZ)
      throws Exception {
    require(chunk.observation().x() == chunkX * 16 && chunk.observation().z() == chunkZ * 16
            && chunk.observation().width() == 16 && chunk.observation().height() == 128
            && chunk.observation().depth() == 16,
        "target chunk shape/origin drift");
    MessageDigest occupancy = MessageDigest.getInstance("SHA-256");
    for (int x = 0; x < 16; x++)
      for (int z = 0; z < 16; z++) {
        for (int y = 0; y < 128; y++)
          occupancy.update((byte) (solid(chunk.blockAt(x, y, z).legacyId()) ? 1 : 0));
        require(top(chunk, x, z) >= 0, "empty generated column");
      }
    return "seed=" + seed + ",chunk=" + chunkX + ":" + chunkZ + ",nonair="
        + chunk.nonAirBlocks() + ",solid=" + hex(occupancy.digest());
  }

  private static int top(RemoteChunkSnapshot chunk, int x, int z) {
    for (int y = 127; y >= 0; y--)
      if (chunk.blockAt(x, y, z).legacyId() != 0)
        return y;
    return -1;
  }

  private static boolean solid(int id) {
    return id != 0 && id != 8 && id != 9 && id != 10 && id != 11;
  }

  private static long[] seeds(String value) {
    String[] rows = value.split(";", -1);
    long[] result = new long[rows.length];
    for (int index = 0; index < rows.length; index++)
      result[index] = Long.parseLong(rows[index]);
    return result;
  }

  private static int[][] chunks(String value) {
    String[] rows = value.split(";", -1);
    int[][] result = new int[rows.length][2];
    for (int index = 0; index < rows.length; index++) {
      String[] coordinate = rows[index].split(":", -1);
      require(coordinate.length == 2, "invalid chunk coordinate");
      result[index][0] = Integer.parseInt(coordinate[0]);
      result[index][1] = Integer.parseInt(coordinate[1]);
    }
    return result;
  }

  private static String sha256(String value) throws Exception {
    return hex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
  }

  private static String hex(byte[] value) {
    StringBuilder result = new StringBuilder();
    for (byte item : value)
      result.append(String.format("%02x", item & 255));
    return result.toString();
  }

  private static void require(boolean value, String message) {
    if (!value)
      throw new IllegalStateException(message);
  }
}
