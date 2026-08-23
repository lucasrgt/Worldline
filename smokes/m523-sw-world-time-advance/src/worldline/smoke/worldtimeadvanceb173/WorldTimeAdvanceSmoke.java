package worldline.smoke.worldtimeadvanceb173;
import static worldline.b173server.B173FixtureSupport.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173LevelDatTime;
import worldline.b173server.B173WireClient;

/** Contrasts persisted official-server time with and without bounded protocol heartbeats. */
public final class WorldTimeAdvanceSmoke {
  private WorldTimeAdvanceSmoke() {
  }

  public static void main(String[] a) throws Exception {
    if (a.length != 7)
      throw new IllegalArgumentException(
          "usage: WorldTimeAdvanceSmoke server.jar workspace port seed username heartbeats targetTime");
    Path jar = Paths.get(a[0]);
    Path workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    int heartbeats = Integer.parseInt(a[5]);
    long target = Long.parseLong(a[6]);
    Duration timeout = Duration.ofSeconds(120);
    require(target > Integer.MAX_VALUE && heartbeats >= 40, "invalid time fixture");

    B173DedicatedServer seedServer =
        new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true, true);
    try {
      seedServer.boot();
    } finally {
      seedServer.close();
    }
    Path level = workspace.resolve("world/level.dat");
    B173LevelDatTime.patch(level, target);
    long baseline = B173LevelDatTime.read(level);
    require(baseline == target, "signed-long fixture drift: " + baseline);

    B173DedicatedServer ticking =
        new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true, true);
    B173WireClient client = new B173WireClient("127.0.0.1", port, user, timeout);
    try {
      ticking.boot();
      require(ticking.state().worldTime() == baseline, "restart did not preserve baseline");
      client.connect();
      client.synchronizePose();
      client.sustainTicks(heartbeats);
      client.close();
      awaitPlayers(ticking, 0);
      ticking.save();
    } finally {
      client.close();
      ticking.close();
    }
    long advanced = B173LevelDatTime.read(level);
    long liveDelta = advanced - baseline;
    require(liveDelta >= heartbeats - 10L && liveDelta <= heartbeats + 200L,
        "heartbeat time delta outside bound: " + liveDelta);

    B173DedicatedServer control =
        new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true, true);
    try {
      control.boot();
      require(control.state().worldTime() == advanced, "second restart did not preserve time");
      control.save();
    } finally {
      control.close();
    }
    long finalTime = B173LevelDatTime.read(level);
    long controlDelta = finalTime - advanced;
    require(controlDelta >= 0L && controlDelta < liveDelta,
        "no-heartbeat control was not smaller: " + controlDelta);

    String signal = "persisted=signed-long,restart=preserved,profile=overworld+nether,heartbeats="
        + heartbeats + ",advance=bounded,no-heartbeat=smaller,save=clean,clients=1";
    String trace = "v1|server=official-b1.7.3|seed=" + seed + "|time=above-int32"
        + "|positive=protocol14-heartbeats-" + heartbeats + "|negative=reload-no-heartbeats|"
        + signal;
    System.out.println("WORLDLINE_M523_SET=" + signal);
    System.out.println("WORLDLINE_M523_TRACE=" + trace);
    System.out.println("WORLDLINE_M523_SIGNATURE=" + sha(trace));
  }

  private static void awaitPlayers(B173DedicatedServer server, int expected) throws Exception {
    long deadline = System.nanoTime() + Duration.ofSeconds(5).toNanos();
    do {
      if (server.players().size() == expected)
        return;
      Thread.sleep(100L);
    } while (System.nanoTime() < deadline);
    throw new IllegalStateException("server player count did not reach " + expected);
  }
  private static void require(boolean value, String message) {
    if (!value)
      throw new IllegalStateException(message);
  }
}
