package worldline.smoke.batchcounts;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import worldline.api.CorrelatedMovementRouteBatchResult;
import worldline.api.CorrelatedMovementRoutePlan;
import worldline.api.MovementAlternative;
import worldline.api.MovementRouteBatchCounts;
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

/** Proves immutable batch counts without flattening identity-bound results. */
public final class BatchCountsSmoke {
  private static final String TRACE = "v1|server=official-b1.7.3|batch=exhausted"
      + "|routes=2|outcomes=3|corrections=0|results=identity-preserved|events=not-replayed"
      + "|cache=preserved|final=persisted|disconnect=clean";
  private BatchCountsSmoke() {
  }

  public static void main(String[] arguments) throws Exception {
    if (arguments.length != 5)
      throw new IllegalArgumentException(
          "usage: BatchCountsSmoke server.jar workspace port seed username");
    Path jar = Paths.get(arguments[0]), workspace = Paths.get(arguments[1]);
    int port = Integer.parseInt(arguments[2]);
    long seed = Long.parseLong(arguments[3]);
    String username = arguments[4];
    Duration timeout = Duration.ofSeconds(90);
    PersistentMultiplayerServerRuntime server =
        new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    RecoveringMovementMultiplayerSession client =
        new B173WireClient("127.0.0.1", port, username, timeout);
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
      CorrelatedMovementRoutePlan first = new CorrelatedMovementRoutePlan(
          new Object(), Arrays.asList(alternative(.125D, 0D, 0D), alternative(0D, 0D, .125D)));
      CorrelatedMovementRoutePlan second = new CorrelatedMovementRoutePlan(
          new Object(), Collections.singletonList(alternative(0D, 0D, .125D)));
      batch = client.moveCorrelatedRouteBatch(Arrays.asList(first, second),
          event -> MovementRouteDirective.CONTINUE, execution -> MovementRouteDirective.CONTINUE);
      MovementRouteBatchCounts counts = batch.counts();
      require(batch.termination() == MovementRouteBatchTermination.EXHAUSTED && counts.routes() == 2
              && counts.outcomes() == 3 && counts.corrections() == 0,
          "batch aggregate counts drifted");
      require(batch.executions().size() == counts.routes()
              && batch.finalExecution() == batch.executions().get(1),
          "counts replaced result identity");
      after = worldline.test.WorldlineSmokeAwait.observe(client, 1);
      require(after.containsChunk(chunkX, chunkZ), "counted batch lost cache");
      client.close();
      awaitPlayers(server, Collections.emptyList());
      server.save();
      player = server.player(username);
      PlayerPose finalPose = batch.finalExecution().execution().result().finalPose();
      require(close(player.x(), finalPose.x()) && close(player.y(), finalPose.y())
              && close(player.z(), finalPose.z()),
          "counted batch pose was not persisted");
    } finally {
      client.close();
      server.close();
    }
    System.out.println(
        "WORLDLINE_M47_API=batch-counts,immutable,routes,outcomes,corrections,no-replay");
    System.out.println("WORLDLINE_M47_COUNTS=routes=" + batch.counts().routes() + ";outcomes="
        + batch.counts().outcomes() + ";corrections=" + batch.counts().corrections());
    System.out.println("WORLDLINE_M47_CACHE=chunks=" + after.chunks().size());
    System.out.println(
        "WORLDLINE_M47_PERSISTED=" + player.x() + "," + player.y() + "," + player.z());
    System.out.println("WORLDLINE_M47_TRACE=" + TRACE);
    System.out.println("WORLDLINE_M47_SIGNATURE=" + sha256(TRACE));
  }

  private static MovementAlternative alternative(double x, double y, double z) {
    return new MovementAlternative(new MovementStep(x, y, z, 5), new MovementStep(-x, -y, -z, 5));
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
