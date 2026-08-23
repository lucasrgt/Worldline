package worldline.smoke.nativemulti;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.Collections;
import worldline.api.MultiplayerConnection;
import worldline.api.PlayableMultiplayerSession;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173WireClient;

/** Couples a real multiplayer session to a native offscreen Minecraft render. */
public final class NativeMultiplayerSmoke {
  private NativeMultiplayerSmoke() {
  }

  public static void main(String[] arguments) throws Exception {
    if (arguments.length != 5)
      throw new IllegalArgumentException(
          "usage: NativeMultiplayerSmoke server.jar workspace port seed username");
    Path jar = Paths.get(arguments[0]), workspace = Paths.get(arguments[1]);
    int port = Integer.parseInt(arguments[2]);
    long seed = Long.parseLong(arguments[3]);
    String username = arguments[4];
    Duration timeout = Duration.ofSeconds(90);
    B173DedicatedServer server = new B173DedicatedServer(jar, workspace, port, seed, timeout);
    PlayableMultiplayerSession client = new B173WireClient("127.0.0.1", port, username, timeout);
    String frame;
    try {
      server.boot();
      client.connect();
      require(
          server.players().equals(Collections.singletonList(username)), "player presence drifted");
      client.synchronizePose();
      frame = MultiplayerFrameRenderer.render(
          client.state().connection() == MultiplayerConnection.CONNECTED);
    } finally {
      client.close();
      server.close();
    }
    String trace = "v1|server=official-b1.7.3|session=protocol14|pose=synchronized"
        + "|renderer=minecraft-tessellator|context=pbuffer|display=false|frame=" + frame;
    System.out.println("WORLDLINE_M26_API=server,session,pose,native-render,frame");
    System.out.println("WORLDLINE_M26_FRAME=" + frame);
    System.out.println("WORLDLINE_M26_TRACE=" + trace);
    System.out.println("WORLDLINE_M26_SIGNATURE=" + sha256(trace));
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
