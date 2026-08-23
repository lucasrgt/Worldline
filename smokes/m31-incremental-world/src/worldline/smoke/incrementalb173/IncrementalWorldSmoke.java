package worldline.smoke.incrementalb173;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.IncrementalRemoteWorldMultiplayerSession;
import worldline.api.PlayerPose;
import worldline.api.RemoteWorldView;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173WireClient;

/** Proves an official accepted dig arrives as a server-authoritative cache update. */
public final class IncrementalWorldSmoke {
  private static final String TRACE = "v1|server=official-b1.7.3|session=protocol14|op=explicit"
      + "|intent=begin-finish-break|update=packet53|cache=immutable-replacement"
      + "|authority=server-observed|disconnect=clean";

  private IncrementalWorldSmoke() {
  }

  public static void main(String[] arguments) throws Exception {
    if (arguments.length != 5)
      throw new IllegalArgumentException(
          "usage: IncrementalWorldSmoke server.jar workspace port seed username");
    Path jar = Paths.get(arguments[0]), workspace = Paths.get(arguments[1]);
    int port = Integer.parseInt(arguments[2]);
    long seed = Long.parseLong(arguments[3]);
    String username = arguments[4];
    Duration timeout = Duration.ofSeconds(90);
    B173DedicatedServer server =
        new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    IncrementalRemoteWorldMultiplayerSession client =
        new B173WireClient("127.0.0.1", port, username, timeout);
    BlockPosition target;
    BlockState before;
    RemoteWorldView after;
    try {
      server.boot();
      server.operator(username);
      client.connect();
      PlayerPose pose = client.synchronizePose();
      RemoteWorldView initial =
          client.awaitRemoteChunk((int) Math.floor(pose.x()) >> 4, (int) Math.floor(pose.z()) >> 4);
      target = target(initial, pose);
      before = initial.blockAt(target.x(), target.y(), target.z());
      Thread.sleep(1000L);
      client.beginBreak(target);
      Thread.sleep(3000L);
      client.finishBreak(target);
      after = client.awaitBlock(target, new BlockState(0, 0));
      require(after.blockAt(target.x(), target.y(), target.z()).legacyId() == 0,
          "official dig did not produce air in the remote cache");
    } finally {
      client.close();
      server.close();
    }
    System.out.println("WORLDLINE_M31_API=server,op,session,cache,dig-intent,block-update");
    System.out.println("WORLDLINE_M31_CHANGE=" + target.x() + "," + target.y() + "," + target.z()
        + "," + before.legacyId() + ":" + before.metadata() + "->0:0");
    System.out.println("WORLDLINE_M31_TRACE=" + TRACE);
    System.out.println("WORLDLINE_M31_SIGNATURE=" + sha256(TRACE));
  }

  private static BlockPosition target(RemoteWorldView world, PlayerPose pose) {
    BlockPosition soft = target(world, pose, false);
    return soft != null ? soft : requireTarget(world, pose);
  }

  private static BlockPosition requireTarget(RemoteWorldView world, PlayerPose pose) {
    BlockPosition stone = target(world, pose, true);
    if (stone != null)
      return stone;
    worldline.api.RemoteChunkObservation region = world.chunks().get(0).observation();
    throw new IllegalStateException("no nearby breakable block; pose=" + pose.x() + "," + pose.y()
        + "," + pose.z() + " chunk=" + region.x() + "," + region.z());
  }

  private static BlockPosition target(RemoteWorldView world, PlayerPose pose, boolean stone) {
    int centerX = (int) Math.floor(pose.x()), centerY = (int) Math.floor(pose.y());
    int centerZ = (int) Math.floor(pose.z());
    for (int y = centerY; y >= centerY - 5; y--)
      for (int radius = 0; radius <= 4; radius++) {
        for (int x = centerX - radius; x <= centerX + radius; x++) {
          for (int z = centerZ - radius; z <= centerZ + radius; z++) {
            if (!world.containsChunk(Math.floorDiv(x, 16), Math.floorDiv(z, 16)))
              continue;
            double dx = x + 0.5D - pose.x(), dy = y + 0.5D - pose.y();
            double dz = z + 0.5D - pose.z();
            if (dx * dx + dy * dy + dz * dz > 25.0D)
              continue;
            int id = world.blockAt(x, y, z).legacyId();
            if (stone ? id > 0 && (id < 7 || id > 11) : id == 2 || id == 3)
              return new BlockPosition(x, y, z);
          }
        }
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
