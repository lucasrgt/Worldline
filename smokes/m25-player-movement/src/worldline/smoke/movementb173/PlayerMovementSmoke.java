package worldline.smoke.movementb173;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import worldline.api.PersistentMultiplayerServerRuntime;
import worldline.api.PlayableMultiplayerSession;
import worldline.api.PlayerPose;
import worldline.api.ServerPlayerState;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173WireClient;

/** Proves a deliberate protocol-14 movement persists through the official server. */
public final class PlayerMovementSmoke {
  private static final String TRACE = "v1|login=accepted|position=acknowledged"
      + "|move=0.125,0.0,0.0|server=accepted|logout=saved|position=persisted";

  private PlayerMovementSmoke() {
  }

  public static void main(String[] arguments) throws Exception {
    if (arguments.length != 8)
      throw new IllegalArgumentException(
          "usage: PlayerMovementSmoke server.jar workspace port seed username dx dy dz");
    Path jar = Paths.get(arguments[0]), workspace = Paths.get(arguments[1]);
    int port = Integer.parseInt(arguments[2]);
    long seed = Long.parseLong(arguments[3]);
    String username = arguments[4];
    double dx = Double.parseDouble(arguments[5]), dy = Double.parseDouble(arguments[6]);
    double dz = Double.parseDouble(arguments[7]);
    Duration timeout = Duration.ofSeconds(90);
    PersistentMultiplayerServerRuntime server =
        new B173DedicatedServer(jar, workspace, port, seed, timeout);
    PlayableMultiplayerSession client = new B173WireClient("127.0.0.1", port, username, timeout);
    PlayerPose initial, target;
    ServerPlayerState player;
    try {
      server.boot();
      client.connect();
      awaitPlayers(server, Collections.singletonList(username), 5000L);
      initial = client.synchronizePose();
      target = client.moveBy(dx, dy, dz);
      require(close(target.x(), initial.x() + dx) && close(target.y(), initial.y() + dy)
              && close(target.z(), initial.z() + dz),
          "client movement target drifted");
      Thread.sleep(300L);
      client.close();
      awaitPlayers(server, Collections.emptyList(), 5000L);
      server.save();
      player = server.player(username);
      require(close(target.x(), player.x()) && close(target.y(), player.y())
              && close(target.z(), player.z()),
          "official server did not persist target position");
    } finally {
      client.close();
      server.close();
    }
    System.out.println("WORLDLINE_M25_API=login,position-ack,move,logout,save,player-position");
    System.out.println("WORLDLINE_M25_SOURCE="
        + B173WireClient.class.getProtectionDomain().getCodeSource().getLocation());
    System.out.println("WORLDLINE_M25_INITIAL=" + pose(initial));
    System.out.println("WORLDLINE_M25_TARGET=" + pose(target));
    System.out.println(
        "WORLDLINE_M25_PERSISTED=" + player.x() + "," + player.y() + "," + player.z());
    System.out.println("WORLDLINE_M25_TRACE=" + TRACE);
    System.out.println("WORLDLINE_M25_SIGNATURE=" + sha256(TRACE));
  }

  private static void awaitPlayers(PersistentMultiplayerServerRuntime server, List<String> expected,
      long timeout) throws InterruptedException {
    long deadline = System.currentTimeMillis() + timeout;
    while (System.currentTimeMillis() < deadline) {
      if (server.players().equals(expected))
        return;
      Thread.sleep(100L);
    }
    throw new IllegalStateException(
        "player list did not become " + expected + ": " + server.players());
  }
  private static boolean close(double first, double second) {
    return Math.abs(first - second) < 0.000001D;
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
