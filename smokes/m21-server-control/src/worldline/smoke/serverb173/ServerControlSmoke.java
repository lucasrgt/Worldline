package worldline.smoke.serverb173;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.ServerLifecycle;
import worldline.api.ServerState;
import worldline.b173server.B173DedicatedServer;

/** Exercises neutral command/save/state control over one official server. */
public final class ServerControlSmoke {
  private static final String TRACE =
      "v1|version=Beta 1.7.3|boot=running|command=time-set|save=persisted|shutdown=clean";

  private ServerControlSmoke() {
  }

  public static void main(String[] arguments) throws Exception {
    if (arguments.length != 5)
      throw new IllegalArgumentException(
          "usage: ServerControlSmoke server.jar workspace port seed targetTime");
    Path jar = Paths.get(arguments[0]);
    Path workspace = Paths.get(arguments[1]);
    int port = Integer.parseInt(arguments[2]);
    long seed = Long.parseLong(arguments[3]);
    long target = Long.parseLong(arguments[4]);
    B173DedicatedServer runtime =
        new B173DedicatedServer(jar, workspace, port, seed, Duration.ofSeconds(90));
    ServerState saved;
    try {
      runtime.boot();
      ServerState running = runtime.state();
      require(running.lifecycle() == ServerLifecycle.RUNNING && !running.onlineMode(),
          "boot state drift");
      runtime.setTime(target);
      runtime.save();
      saved = runtime.state();
      require(saved.completedSaves() == 1, "save count drift");
      require(saved.worldTime() >= target && saved.worldTime() < target + 200L,
          "persisted time outside bounded tick window: " + saved.worldTime());
    } finally {
      runtime.close();
    }
    ServerState stopped = runtime.state();
    require(stopped.lifecycle() == ServerLifecycle.STOPPED, "stop state drift");
    System.out.println("WORLDLINE_M21_API=boot,set-time,save,state,close");
    System.out.println("WORLDLINE_M21_SOURCE="
        + B173DedicatedServer.class.getProtectionDomain().getCodeSource().getLocation());
    System.out.println("WORLDLINE_M21_TIME=" + saved.worldTime());
    System.out.println("WORLDLINE_M21_TRACE=" + TRACE);
    System.out.println("WORLDLINE_M21_SIGNATURE=" + sha256(TRACE));
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
