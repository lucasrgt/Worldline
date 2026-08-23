package worldline.smoke.batchterminal;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import worldline.api.CorrelatedMovementRouteBatchExecution;
import worldline.api.CorrelatedMovementRoutePlan;
import worldline.api.MovementAlternative;
import worldline.api.MovementRouteBatchTerminalKind;
import worldline.api.MovementRouteDirective;
import worldline.api.MovementStep;
import worldline.api.PersistentMultiplayerServerRuntime;
import worldline.api.PlayerPose;
import worldline.api.RecoveringMovementMultiplayerSession;
import worldline.api.RemoteWorldView;
import worldline.api.ServerPlayerState;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173WireClient;

/** Proves exact terminal events for every bounded batch return boundary. */
public final class BatchTerminalSmoke {
  private static final String TRACE = "v1|server=official-b1.7.3"
      + "|terminal=event@0/0:0:primary,after-route@0/0:0:primary,exhausted@0/0:0:primary"
      + "|events=identity-bound|unsent=preserved|cache=preserved|final=persisted|disconnect=clean";
  private BatchTerminalSmoke() {
  }

  public static void main(String[] arguments) throws Exception {
    if (arguments.length != 5)
      throw new IllegalArgumentException(
          "usage: BatchTerminalSmoke server.jar workspace port seed username");
    Path jar = Paths.get(arguments[0]), workspace = Paths.get(arguments[1]);
    int port = Integer.parseInt(arguments[2]);
    long seed = Long.parseLong(arguments[3]);
    String username = arguments[4];
    Duration timeout = Duration.ofSeconds(90);
    PersistentMultiplayerServerRuntime server =
        new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    RecoveringMovementMultiplayerSession client =
        new B173WireClient("127.0.0.1", port, username, timeout);
    CorrelatedMovementRouteBatchExecution event, afterRoute, exhausted;
    RemoteWorldView after;
    ServerPlayerState player;
    try {
      server.boot();
      client.connect();
      awaitPlayers(server, Collections.singletonList(username));
      PlayerPose initial = client.synchronizePose();
      int chunkX = floor(initial.x()) >> 4;
      int chunkZ = floor(initial.z()) >> 4;
      client.awaitRemoteChunk(chunkX, chunkZ);
      worldline.test.WorldlineSmokeAwait.observe(client, 5);
      CorrelatedMovementRoutePlan eventPlan = new CorrelatedMovementRoutePlan(
          new Object(), Arrays.asList(alternative(.125D, 0D, 0D), alternative(0D, 0D, .125D)));
      event =
          client.moveCorrelatedRouteBatchExecutionUntilEvent(Collections.singletonList(eventPlan),
              value
              -> MovementRouteDirective.CONTINUE,
              value -> MovementRouteDirective.CONTINUE, value -> MovementRouteDirective.STOP);
      afterRoute = client.moveCorrelatedRouteBatchExecutionUntilEvent(
          Collections.singletonList(plan(0D, 0D, .125D)),
          value
          -> MovementRouteDirective.CONTINUE,
          value -> MovementRouteDirective.STOP, value -> MovementRouteDirective.CONTINUE);
      exhausted = client.moveCorrelatedRouteBatchExecutionUntilEvent(
          Collections.singletonList(plan(-.125D, 0D, 0D)),
          value
          -> MovementRouteDirective.CONTINUE,
          value -> MovementRouteDirective.CONTINUE, value -> MovementRouteDirective.CONTINUE);
      require(terminal(event, MovementRouteBatchTerminalKind.EVENT)
              && event.result().finalExecution().execution().result().outcomes().size() == 1,
          "event terminal summary drifted");
      require(terminal(afterRoute, MovementRouteBatchTerminalKind.AFTER_ROUTE),
          "after-route terminal summary drifted");
      require(terminal(exhausted, MovementRouteBatchTerminalKind.EXHAUSTED),
          "exhausted terminal summary drifted");
      after = worldline.test.WorldlineSmokeAwait.observe(client, 1);
      require(after.containsChunk(chunkX, chunkZ), "terminal batches lost cache");
      client.close();
      awaitPlayers(server, Collections.emptyList());
      server.save();
      player = server.player(username);
      PlayerPose finalPose = exhausted.result().finalExecution().execution().result().finalPose();
      require(close(player.x(), finalPose.x()) && close(player.y(), finalPose.y())
              && close(player.z(), finalPose.z()),
          "terminal batch pose was not persisted");
    } finally {
      client.close();
      server.close();
    }
    System.out.println("WORLDLINE_M46_API=batch-execution,terminal-kind,exact-event,identity");
    System.out.println("WORLDLINE_M46_TERMINALS=" + describe(event) + "," + describe(afterRoute)
        + "," + describe(exhausted));
    System.out.println("WORLDLINE_M46_CACHE=chunks=" + after.chunks().size());
    System.out.println(
        "WORLDLINE_M46_PERSISTED=" + player.x() + "," + player.y() + "," + player.z());
    System.out.println("WORLDLINE_M46_TRACE=" + TRACE);
    System.out.println("WORLDLINE_M46_SIGNATURE=" + sha256(TRACE));
  }

  private static CorrelatedMovementRoutePlan plan(double x, double y, double z) {
    return new CorrelatedMovementRoutePlan(
        new Object(), Collections.singletonList(alternative(x, y, z)));
  }
  private static MovementAlternative alternative(double x, double y, double z) {
    return new MovementAlternative(new MovementStep(x, y, z, 5), new MovementStep(-x, -y, -z, 5));
  }
  private static boolean terminal(
      CorrelatedMovementRouteBatchExecution value, MovementRouteBatchTerminalKind kind) {
    return value.terminalKind() == kind
        && value.terminalEvent().routeIndex() == value.result().executions().size() - 1
        && value.terminalEvent().event() == value.result().finalExecution().terminalEvent();
  }
  private static String describe(CorrelatedMovementRouteBatchExecution value) {
    return value.terminalKind() + "@" + value.terminalEvent().routeIndex() + "/"
        + value.terminalEvent().event().event().alternativeIndex() + ":"
        + value.terminalEvent().event().event().outcomeIndex() + ":"
        + value.terminalEvent().event().event().kind();
  }
  private static void awaitPlayers(PersistentMultiplayerServerRuntime server, List<String> expected)
      throws InterruptedException {
    long deadline = System.currentTimeMillis() + 5000L;
    while (System.currentTimeMillis() < deadline) {
      if (server.players().equals(expected))
        return;
      Thread.sleep(100L);
    }
    throw new IllegalStateException("player list did not become " + expected);
  }
  private static int floor(double value) {
    return (int) Math.floor(value);
  }
  private static boolean close(double a, double b) {
    return Math.abs(a - b) < .000001D;
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
