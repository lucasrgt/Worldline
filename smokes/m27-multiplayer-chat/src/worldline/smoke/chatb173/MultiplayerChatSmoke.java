package worldline.smoke.chatb173;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import worldline.api.ChatMultiplayerSession;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173WireClient;

/** Proves one native chat broadcast between two simultaneous clients. */
public final class MultiplayerChatSmoke {
  private MultiplayerChatSmoke() {
  }

  public static void main(String[] arguments) throws Exception {
    if (arguments.length != 7)
      throw new IllegalArgumentException(
          "usage: MultiplayerChatSmoke server.jar workspace port seed sender receiver message");
    Path jar = Paths.get(arguments[0]), workspace = Paths.get(arguments[1]);
    int port = Integer.parseInt(arguments[2]);
    long seed = Long.parseLong(arguments[3]);
    String senderName = arguments[4], receiverName = arguments[5], message = arguments[6];
    Duration timeout = Duration.ofSeconds(90);
    B173DedicatedServer server = new B173DedicatedServer(jar, workspace, port, seed, timeout);
    ChatMultiplayerSession sender = new B173WireClient("127.0.0.1", port, senderName, timeout);
    ChatMultiplayerSession receiver = new B173WireClient("127.0.0.1", port, receiverName, timeout);
    String broadcast;
    try {
      server.boot();
      sender.connect();
      sender.synchronizePose();
      receiver.connect();
      receiver.synchronizePose();
      requirePlayers(server.players(), senderName, receiverName);
      sender.sendChat(message);
      broadcast = receiver.awaitChat();
      require(broadcast.equals("<" + senderName + "> " + message),
          "native chat broadcast drifted: " + broadcast);
    } finally {
      sender.close();
      receiver.close();
      server.close();
    }
    String trace = "v1|server=official-b1.7.3|clients=" + senderName + "," + receiverName
        + "|presence=2|sender=" + senderName + "|message=" + message + "|receiver=" + receiverName
        + "|broadcast=" + broadcast + "|disconnect=clean";
    System.out.println(
        "WORLDLINE_M27_API=server,two-sessions,pose,send-chat,inbound-pump,receive-chat");
    System.out.println("WORLDLINE_M27_BROADCAST=" + broadcast);
    System.out.println("WORLDLINE_M27_TRACE=" + trace);
    System.out.println("WORLDLINE_M27_SIGNATURE=" + sha256(trace));
  }

  private static void requirePlayers(List<String> players, String first, String second) {
    Set<String> expected = new HashSet<>();
    expected.add(first);
    expected.add(second);
    require(players.size() == 2 && new HashSet<>(players).equals(expected),
        "two-player presence drifted: " + players);
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
