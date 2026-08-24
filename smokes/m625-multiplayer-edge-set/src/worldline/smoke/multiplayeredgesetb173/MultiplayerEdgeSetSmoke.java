package worldline.smoke.multiplayeredgesetb173;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import worldline.api.PlayerPose;
import worldline.api.ServerPlayerState;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173FixtureSupport;
import worldline.b173server.B173PlayerSeed;
import worldline.b173server.B173WireClient;

/** Proves ordered presence, isolated disconnect, reconnect, and restart persistence. */
public final class MultiplayerEdgeSetSmoke {
  private MultiplayerEdgeSetSmoke() { }

  public static void main(String[] a) throws Exception {
    if (a.length != 6)
      throw new IllegalArgumentException(
          "usage: MultiplayerEdgeSetSmoke server.jar workspace port seed alpha beta");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String alpha = a[4], beta = a[5];
    Duration timeout = Duration.ofSeconds(120);
    B173DedicatedServer server = new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient first = client(port, alpha, timeout), peer = client(port, beta, timeout),
        reconnect = null;
    PlayerPose moved;
    int firstEntity, reconnectEntity;
    ServerPlayerState persisted;
    try {
      server.boot();
      B173PlayerSeed.write(workspace, alpha, 4.5D, 120D, 4.5D);
      B173PlayerSeed.write(workspace, beta, 6.5D, 120D, 4.5D);
      first.connect();
      first.synchronizePose();
      firstEntity = first.state().entityId();
      peer.connect();
      peer.synchronizePose();
      awaitOrder(server, alpha, beta);
      moved = first.moveAndObserve(0.5D, 0D, 0D, 2).resulting();
      first.close();
      awaitOrder(server, beta);
      require(peer.state().entityId() >= 0, "remaining peer lost its session");
      server.save();
      persisted = server.player(alpha);
      require(close(persisted, moved), "disconnect pose did not persist");
      reconnect = client(port, alpha, timeout);
      reconnect.connect();
      PlayerPose resumed = reconnect.synchronizePose();
      reconnectEntity = reconnect.state().entityId();
      require(reconnectEntity != firstEntity && close(persisted, resumed),
          "same-user reconnect identity or pose drifted");
      awaitOrder(server, beta, alpha);
      reconnect.close();
      peer.close();
      B173FixtureSupport.awaitPlayers(server, 0);
      server.save();
    } finally {
      first.close();
      peer.close();
      if (reconnect != null) reconnect.close();
      server.close();
    }
    B173WireClient beta2 = client(port, beta, timeout), alpha2 = client(port, alpha, timeout);
    server = new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    try {
      server.boot();
      beta2.connect();
      beta2.synchronizePose();
      alpha2.connect();
      PlayerPose restarted = alpha2.synchronizePose();
      awaitOrder(server, beta, alpha);
      require(close(persisted, restarted), "restart pose did not persist");
      beta2.close();
      awaitOrder(server, alpha);
      alpha2.close();
      B173FixtureSupport.awaitPlayers(server, 0);
    } finally {
      beta2.close();
      alpha2.close();
      server.close();
    }
    String evidence = "orders=Alpha>Beta+Beta>Alpha,disconnect=isolated,reconnect=same-user-new-entity,"
        + "persistence=disconnect+restart,clients=5,servers=2";
    String trace = "v1|server=official-b1.7.3|seed=" + seed + "|" + evidence
        + "|entity-renewed=" + (firstEntity != reconnectEntity) + "|disconnect=clean";
    System.out.println("WORLDLINE_M625_SET=" + evidence);
    System.out.println("WORLDLINE_M625_TRACE=" + trace);
    System.out.println("WORLDLINE_M625_SIGNATURE=" + B173FixtureSupport.sha(trace));
  }

  private static B173WireClient client(int port, String user, Duration timeout) {
    return new B173WireClient("127.0.0.1", port, user, timeout);
  }

  private static void awaitOrder(B173DedicatedServer server, String... expected) throws Exception {
    List<String> order = Arrays.asList(expected);
    long deadline = System.currentTimeMillis() + 5_000L;
    while (System.currentTimeMillis() < deadline) {
      if (server.players().equals(order)) return;
      Thread.sleep(100L);
    }
    throw new IllegalStateException("player order drifted: " + server.players());
  }

  private static boolean close(ServerPlayerState state, PlayerPose pose) {
    return Math.abs(state.x() - pose.x()) <= 0.01D && Math.abs(state.y() - pose.y()) <= 0.01D
        && Math.abs(state.z() - pose.z()) <= 0.01D;
  }

  private static void require(boolean value, String message) {
    if (!value) throw new IllegalStateException(message);
  }
}
