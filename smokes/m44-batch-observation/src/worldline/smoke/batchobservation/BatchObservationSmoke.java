package worldline.smoke.batchobservation;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import worldline.api.CorrelatedMovementRouteBatchEvent;
import worldline.api.CorrelatedMovementRouteBatchResult;
import worldline.api.CorrelatedMovementRoutePlan;
import worldline.api.MovementAlternative;
import worldline.api.MovementAttemptKind;
import worldline.api.MovementRouteBatchTermination;
import worldline.api.MovementRouteDirective;
import worldline.api.MovementStep;
import worldline.api.PersistentMultiplayerServerRuntime;
import worldline.api.PlayerPose;
import worldline.api.RecoveringMovementMultiplayerSession;
import worldline.api.RemoteWorldView;
import worldline.api.ServerPlayerState;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173WireClient;

/** Proves caller-thread batch route indexes preserve embedded event indexes. */
public final class BatchObservationSmoke {
  private static final String TRACE = "v1|server=official-b1.7.3|batch=two-routes-exhausted"
      + "|events=0/0:0:primary,1/0:0:primary|correlation=identity|thread=caller|indexes=stable"
      + "|cache=preserved|final=persisted|disconnect=clean";
  private BatchObservationSmoke() {
  }

  public static void main(String[] arguments) throws Exception {
    if (arguments.length != 5)
      throw new IllegalArgumentException(
          "usage: BatchObservationSmoke server.jar workspace port seed username");
    Path jar = Paths.get(arguments[0]), workspace = Paths.get(arguments[1]);
    int port = Integer.parseInt(arguments[2]);
    long seed = Long.parseLong(arguments[3]);
    String username = arguments[4];
    Duration timeout = Duration.ofSeconds(90);
    PersistentMultiplayerServerRuntime server =
        new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    RecoveringMovementMultiplayerSession client =
        new B173WireClient("127.0.0.1", port, username, timeout);
    Object first = new Object(), second = new Object();
    Thread caller = Thread.currentThread();
    List<CorrelatedMovementRouteBatchEvent> events = new ArrayList<>();
    CorrelatedMovementRouteBatchResult batch;
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
      CorrelatedMovementRoutePlan x = plan(first, .125D, 0D, 0D);
      CorrelatedMovementRoutePlan z = plan(second, 0D, 0D, .125D);
      batch = client.moveCorrelatedRouteBatch(Arrays.asList(x, z),
          event
          -> MovementRouteDirective.CONTINUE,
          execution -> MovementRouteDirective.CONTINUE, event -> {
            require(Thread.currentThread() == caller, "batch observer changed thread");
            require(event.routeIndex() == events.size(), "batch event index was not immediate");
            events.add(event);
          });
      require(batch.executions().size() == 2
              && batch.termination() == MovementRouteBatchTermination.EXHAUSTED,
          "observed batch did not exhaust two routes");
      require(indexed(events.get(0), 0, first) && indexed(events.get(1), 1, second),
          "batch or embedded route indexes drifted");
      after = worldline.test.WorldlineSmokeAwait.observe(client, 1);
      require(after.containsChunk(chunkX, chunkZ), "observed batch lost cache");
      client.close();
      awaitPlayers(server, Collections.emptyList());
      server.save();
      player = server.player(username);
      PlayerPose finalPose = batch.finalExecution().execution().result().finalPose();
      require(close(player.x(), finalPose.x()) && close(player.y(), finalPose.y())
              && close(player.z(), finalPose.z()),
          "observed batch pose was not persisted");
    } finally {
      client.close();
      server.close();
    }
    System.out.println(
        "WORLDLINE_M44_API=batch-observer,synchronous,route-index,embedded-index,identity");
    System.out.println(
        "WORLDLINE_M44_EVENTS=" + describe(events) + ";batch=" + batch.termination());
    System.out.println("WORLDLINE_M44_CACHE=chunks=" + after.chunks().size());
    System.out.println(
        "WORLDLINE_M44_PERSISTED=" + player.x() + "," + player.y() + "," + player.z());
    System.out.println("WORLDLINE_M44_TRACE=" + TRACE);
    System.out.println("WORLDLINE_M44_SIGNATURE=" + sha256(TRACE));
  }

  private static CorrelatedMovementRoutePlan plan(
      Object correlation, double x, double y, double z) {
    MovementAlternative alternative =
        new MovementAlternative(new MovementStep(x, y, z, 5), new MovementStep(-x, -y, -z, 5));
    return new CorrelatedMovementRoutePlan(correlation, Collections.singletonList(alternative));
  }
  private static boolean indexed(
      CorrelatedMovementRouteBatchEvent value, int index, Object correlation) {
    return value.routeIndex() == index && value.event().correlation() == correlation
        && value.event().event().alternativeIndex() == 0
        && value.event().event().outcomeIndex() == 0
        && value.event().event().kind() == MovementAttemptKind.PRIMARY;
  }
  private static String describe(List<CorrelatedMovementRouteBatchEvent> events) {
    StringBuilder value = new StringBuilder();
    for (CorrelatedMovementRouteBatchEvent event : events) {
      if (value.length() > 0)
        value.append(',');
      value.append(event.routeIndex())
          .append('/')
          .append(event.event().event().alternativeIndex())
          .append(':')
          .append(event.event().event().outcomeIndex())
          .append(':')
          .append(event.event().event().kind());
    }
    return value.toString();
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
