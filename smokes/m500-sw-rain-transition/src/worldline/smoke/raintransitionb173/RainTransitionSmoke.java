package worldline.smoke.raintransitionb173;
import static worldline.b173server.B173FixtureSupport.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.RemoteRainStart;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173LevelDatWeather;
import worldline.b173server.B173Packet71SkipCheck;
import worldline.b173server.B173RainAccess;
import worldline.b173server.B173WireClient;

/** Proves the official dry-to-rain transition broadcasts Packet70Bed reason 1 and records the save-order oracle. */
public final class RainTransitionSmoke {
  private RainTransitionSmoke() {
  }

  public static void main(String[] a) throws Exception {
    if (a.length != 6)
      throw new IllegalArgumentException(
          "usage: RainTransitionSmoke server.jar workspace port seed username rainTicks");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    int rainTicks = Integer.parseInt(a[5]);
    Duration timeout = Duration.ofSeconds(180);
    require(user.length() <= 16 && rainTicks > 0, "invalid rain transition seed");
    B173Packet71SkipCheck.verify();

    B173DedicatedServer creator = new B173DedicatedServer(jar, workspace, port, seed, timeout);
    creator.boot();
    creator.close();
    Path level = workspace.resolve("world/level.dat");
    B173LevelDatWeather.Weather before = B173LevelDatWeather.read(level);
    require(before.seed() == seed, "fresh world seed drift");

    B173LevelDatWeather.patch(level, rainTicks, false, 60000, false);
    B173LevelDatWeather.Weather patched = B173LevelDatWeather.read(level);
    require(!patched.raining() && patched.rainTime() == rainTicks && !patched.thundering(),
        "dry patch drift");
    require(patched.seed() == before.seed() && patched.time() == before.time()
            && patched.spawnX() == before.spawnX() && patched.spawnY() == before.spawnY()
            && patched.spawnZ() == before.spawnZ(),
        "level.dat patch corrupted world identity");

    B173DedicatedServer server = new B173DedicatedServer(jar, workspace, port, seed, timeout);
    B173WireClient client = new B173WireClient("127.0.0.1", port, user, timeout);
    try {
      server.boot();
      client.connect();
      client.synchronizePose();
      require(
          client.dimension() == 0 && client.awaitDimension(0) == 0, "overworld dimension drift");
      B173RainAccess.arm(client);
      RemoteRainStart start = B173RainAccess.awaitRainStart(client);
      require(start.packetId() == 70 && start.reason() == 1 && start.dryBefore()
              && start.rainingAfter(),
          "rain start observation drift");
      client.close();
      awaitPlayers(server, 0);
      server.save();
      server.close();
    } finally {
      client.close();
      server.close();
    }

    Path levelOld = workspace.resolve("world/level.dat_old");
    require(Files.isRegularFile(levelOld), "overworld snapshot level.dat_old absent");
    B173LevelDatWeather.Weather old = B173LevelDatWeather.read(levelOld);
    B173LevelDatWeather.Weather canonical = B173LevelDatWeather.read(level);
    require(old.raining() && !old.thundering(), "overworld snapshot weather drift");
    require(old.rainTime() >= 12000 && old.rainTime() <= 23999,
        "overworld snapshot reseeded rainTime drift: " + old.rainTime());
    require(
        !canonical.raining() && canonical.rainTime() == rainTicks, "canonical dry snapshot drift");
    require(old.seed() == before.seed() && old.spawnX() == before.spawnX()
            && old.spawnY() == before.spawnY() && old.spawnZ() == before.spawnZ(),
        "overworld snapshot identity drift");
    require(canonical.seed() == before.seed() && canonical.spawnX() == before.spawnX()
            && canonical.spawnY() == before.spawnY() && canonical.spawnZ() == before.spawnZ(),
        "canonical snapshot identity drift");

    String evidence =
        "dimension=0,live=packet70-reason1,old-snapshot=raining,canonical=dry-original-countdown,"
        + "save-order=overworld-then-secondary,thundering=false,identity=seed-spawn-preserved,"
        + "clients=1,disconnect=clean";
    String trace = "v1|server=official-b1.7.3|seed=" + seed
        + "|profile=overworld|entry=patch-level-dat-dry-rainTime-" + rainTicks + "+thunder-off"
        + "|fixture=overworld-dry-countdown|cause=worldserver-updateweather-dry-to-rain"
        + "|wire=packet70-reason1-begin-rain|oracle=dry-before-raining-after|" + evidence;
    System.out.println("WORLDLINE_M500SW_RAIN=" + evidence);
    System.out.println("WORLDLINE_M500SW_TRACE=" + trace);
    System.out.println("WORLDLINE_M500SW_SIGNATURE=" + sha(trace));
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

  private static void require(boolean v, String m) {
    if (!v)
      throw new IllegalStateException(m);
  }
}
