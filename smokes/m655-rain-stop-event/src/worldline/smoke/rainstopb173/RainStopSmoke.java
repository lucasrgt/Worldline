package worldline.smoke.rainstopb173;

import static worldline.b173server.B173FixtureSupport.awaitPlayers;
import static worldline.b173server.B173FixtureSupport.sha;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import worldline.api.RemoteRainStop;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173LevelDatWeather;
import worldline.b173server.B173RainAccess;
import worldline.b173server.B173WireClient;
import worldline.testkit.RainStopFixture;

/** Proves live official Packet70 reason 2 closes a previously observed raining state. */
public final class RainStopSmoke {
  private RainStopSmoke() { }

  public static void main(String[] arguments) throws Exception {
    if (arguments.length != 6)
      throw new IllegalArgumentException(
          "usage: RainStopSmoke server.jar workspace port seed username rainTicks");
    Path jar = Paths.get(arguments[0]);
    Path workspace = Paths.get(arguments[1]);
    int port = Integer.parseInt(arguments[2]);
    long seed = Long.parseLong(arguments[3]);
    String username = arguments[4];
    int rainTicks = Integer.parseInt(arguments[5]);
    Duration timeout = Duration.ofSeconds(180);
    require(username.length() <= 16 && rainTicks > 0, "invalid rain stop fixture");
    B173DedicatedServer creator = new B173DedicatedServer(jar, workspace, port, seed, timeout);
    creator.boot();
    creator.close();
    Path level = workspace.resolve("world/level.dat");
    B173LevelDatWeather.Weather created = B173LevelDatWeather.read(level);
    require(created.seed() == seed, "fresh world seed drift");
    B173LevelDatWeather.patch(level, rainTicks, true, 60000, false);
    B173LevelDatWeather.Weather patched = B173LevelDatWeather.read(level);
    require(patched.raining() && patched.rainTime() == rainTicks && !patched.thundering(),
        "raining patch drift");
    require(sameIdentity(created, patched), "level.dat patch corrupted world identity");

    B173DedicatedServer server = new B173DedicatedServer(jar, workspace, port, seed, timeout);
    B173WireClient client = new B173WireClient("127.0.0.1", port, username, timeout);
    try {
      server.boot();
      client.connect();
      client.synchronizePose();
      require(client.dimension() == 0 && client.awaitDimension(0) == 0,
          "overworld dimension drift");
      B173RainAccess.armStop(client);
      RemoteRainStop stop = RainStopFixture.observe(B173RainAccess.awaitRainStop(client));
      require(stop.packetId() == 70 && stop.reason() == 2
              && stop.rainingBefore() && stop.dryAfter(),
          "rain stop observation drift");
      RainStopFixture.compare(new RemoteRainStop(70, 2, true, true), stop);
      client.close();
      awaitPlayers(server, 0);
      server.close();
    } finally {
      client.close();
      server.close();
    }

    String evidence = "dimension=0,bootstrap=packet70-reason1,live=packet70-reason2,"
        + "state=raining-before-dry-after,thundering=false,identity=seed-spawn-preserved,"
        + "clients=1,disconnect=clean";
    String trace = "v1|server=official-b1.7.3|seed=" + seed
        + "|profile=overworld|entry=patch-level-dat-raining-rainTime-" + rainTicks
        + "+thunder-off|fixture=overworld-raining-countdown"
        + "|cause=worldserver-updateweather-rain-to-dry"
        + "|bootstrap=packet70-reason1-raining|wire=packet70-reason2-end-rain"
        + "|oracle=raining-before-dry-after|" + evidence;
    System.out.println("WORLDLINE_M655_RAIN_STOP=" + evidence);
    System.out.println("WORLDLINE_M655_TRACE=" + trace);
    System.out.println("WORLDLINE_M655_SIGNATURE=" + sha(trace));
  }

  private static boolean sameIdentity(B173LevelDatWeather.Weather first,
      B173LevelDatWeather.Weather second) {
    return first.seed() == second.seed() && first.time() == second.time()
        && first.spawnX() == second.spawnX() && first.spawnY() == second.spawnY()
        && first.spawnZ() == second.spawnZ();
  }

  private static void require(boolean value, String message) {
    if (!value)
      throw new IllegalStateException(message);
  }
}
