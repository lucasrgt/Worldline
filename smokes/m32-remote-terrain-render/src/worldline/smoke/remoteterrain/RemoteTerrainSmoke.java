package worldline.smoke.remoteterrain;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.PlayerPose;
import worldline.api.RemoteWorldView;
import worldline.api.SustainedRemoteWorldMultiplayerSession;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173WireClient;

/** Proves sustained protocol-14 cache state drives mapped native terrain geometry. */
public final class RemoteTerrainSmoke {
  private static final String TRACE = "v1|server=official-b1.7.3|session=protocol14"
      + "|heartbeat=40ticks|chunks=multi|render=remote-slice-native-tessellator"
      + "|update=packet53|authority=server-observed|display=false";

  private RemoteTerrainSmoke() {
  }

  public static void main(String[] arguments) throws Exception {
    if (arguments.length != 7)
      throw new IllegalArgumentException(
          "usage: RemoteTerrainSmoke server.jar workspace port seed username ticks minimumChunks");
    Path jar = Paths.get(arguments[0]), workspace = Paths.get(arguments[1]);
    int port = Integer.parseInt(arguments[2]), ticks = Integer.parseInt(arguments[5]);
    int minimumChunks = Integer.parseInt(arguments[6]);
    long seed = Long.parseLong(arguments[3]);
    String username = arguments[4];
    Duration timeout = Duration.ofSeconds(90);
    B173DedicatedServer server =
        new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    SustainedRemoteWorldMultiplayerSession client =
        new B173WireClient("127.0.0.1", port, username, timeout);
    BlockPosition target;
    BlockState state;
    RemoteTerrainFrame.Frame before, after;
    int chunks;
    try {
      server.boot();
      server.operator(username);
      client.connect();
      PlayerPose pose = client.synchronizePose();
      client.awaitRemoteChunk((int) Math.floor(pose.x()) >> 4, (int) Math.floor(pose.z()) >> 4);
      RemoteWorldView world = worldline.test.WorldlineSmokeAwait.observe(client, ticks);
      chunks = world.chunks().size();
      require(chunks >= minimumChunks, "sustained cache remained too small: " + chunks);
      target = target(world, pose);
      state = world.blockAt(target.x(), target.y(), target.z());
      before = RemoteTerrainFrame.render(world, target);
      Thread.sleep(1000L);
      client.beginBreak(target);
      Thread.sleep(3000L);
      client.finishBreak(target);
      RemoteWorldView changed = client.awaitBlock(target, new BlockState(0, 0));
      after = RemoteTerrainFrame.render(changed, target);
      require(before.targetPixel != RemoteTerrainFrame.BACKGROUND
              && after.targetPixel == RemoteTerrainFrame.BACKGROUND,
          "server update did not reach target render pixel");
      require(!before.hash.equals(after.hash), "remote terrain frame did not change");
    } finally {
      client.close();
      server.close();
    }
    System.out.println("WORLDLINE_M32_API=server,session,heartbeat,cache,dig,terrain-render,frame");
    System.out.println("WORLDLINE_M32_HEARTBEAT=ticks=" + ticks + ",chunks=" + chunks);
    System.out.println("WORLDLINE_M32_CHANGE=" + target.x() + "," + target.y() + "," + target.z()
        + "," + state.legacyId() + ":" + state.metadata() + "->0:0");
    System.out.println("WORLDLINE_M32_FRAMES=" + before.hash + "->" + after.hash);
    System.out.println("WORLDLINE_M32_COVERAGE=" + before.coverage + "->" + after.coverage);
    System.out.println("WORLDLINE_M32_DISPLAY_CREATED=false");
    System.out.println("WORLDLINE_M32_RENDERER=" + RemoteTerrainFrame.provenance());
    System.out.println("WORLDLINE_M32_TRACE=" + TRACE);
    System.out.println("WORLDLINE_M32_SIGNATURE=" + sha256(TRACE));
  }

  private static BlockPosition target(RemoteWorldView world, PlayerPose pose) {
    BlockPosition soft = target(world, pose, false),
                  any = soft == null ? target(world, pose, true) : soft;
    if (any != null)
      return any;
    throw new IllegalStateException("no nearby breakable block");
  }
  private static BlockPosition target(RemoteWorldView world, PlayerPose pose, boolean broad) {
    int centerX = (int) Math.floor(pose.x()), centerY = (int) Math.floor(pose.y());
    int centerZ = (int) Math.floor(pose.z());
    for (int y = centerY; y >= centerY - 6; y--)
      for (int radius = 0; radius <= 4; radius++)
        for (int x = centerX - radius; x <= centerX + radius; x++)
          for (int z = centerZ - radius; z <= centerZ + radius; z++) {
            if (!world.containsChunk(Math.floorDiv(x, 16), Math.floorDiv(z, 16)))
              continue;
            double dx = x + .5D - pose.x(), dy = y + .5D - pose.y(), dz = z + .5D - pose.z();
            if (dx * dx + dy * dy + dz * dz > 35D)
              continue;
            int id = world.blockAt(x, y, z).legacyId();
            if (broad ? id > 0 && (id < 7 || id > 11) : id == 2 || id == 3)
              return new BlockPosition(x, y, z);
          }
    return null;
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
