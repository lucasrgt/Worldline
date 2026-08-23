package worldline.smoke.multiplayerb173;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import worldline.api.MultiplayerConnection;
import worldline.api.MultiplayerServerRuntime;
import worldline.api.MultiplayerSession;
import worldline.api.MultiplayerState;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173WireClient;

/** Proves protocol-14 login, server-side presence, and clean disconnect. */
public final class MultiplayerWireSmoke {
  private static final String TRACE =
      "v1|protocol=14|handshake=offline|login=accepted|players=1|disconnect=clean";

  private MultiplayerWireSmoke() {
  }

  public static void main(String[] arguments) throws Exception {
    if (arguments.length != 5)
      throw new IllegalArgumentException(
          "usage: MultiplayerWireSmoke server.jar workspace port seed username");
    Path jar = Paths.get(arguments[0]);
    Path workspace = Paths.get(arguments[1]);
    int port = Integer.parseInt(arguments[2]);
    long seed = Long.parseLong(arguments[3]);
    String username = arguments[4];
    Duration timeout = Duration.ofSeconds(90);
    MultiplayerServerRuntime server = new B173DedicatedServer(jar, workspace, port, seed, timeout);
    MultiplayerSession client = new B173WireClient("127.0.0.1", port, username, timeout);
    MultiplayerState connected;
    try {
      server.boot();
      client.connect();
      connected = client.state();
      require(connected.connection() == MultiplayerConnection.CONNECTED && connected.entityId() >= 0
              && connected.protocolVersion() == 14,
          "login state drift");
      awaitPlayers(server, Collections.singletonList(username), 5000L);
      client.close();
      awaitPlayers(server, Collections.emptyList(), 5000L);
    } finally {
      client.close();
      server.close();
    }
    require(client.state().connection() == MultiplayerConnection.DISCONNECTED,
        "disconnect state drift");
    System.out.println("WORLDLINE_M22_API=server,session,connect,state,players,close");
    System.out.println("WORLDLINE_M22_SOURCE="
        + B173WireClient.class.getProtectionDomain().getCodeSource().getLocation());
    System.out.println("WORLDLINE_M22_ENTITY=" + connected.entityId());
    System.out.println("WORLDLINE_M22_TRACE=" + TRACE);
    System.out.println("WORLDLINE_M22_SIGNATURE=" + sha256(TRACE));
  }

  private static void awaitPlayers(MultiplayerServerRuntime server, List<String> expected,
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
