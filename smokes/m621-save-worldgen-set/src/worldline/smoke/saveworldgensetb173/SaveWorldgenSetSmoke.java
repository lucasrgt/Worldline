package worldline.smoke.saveworldgensetb173;
import static worldline.b173server.B173FixtureSupport.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.RemoteWorldView;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173PlayerSeed;
import worldline.b173server.B173WireClient;

/** Proves one replay-stable McRegion frame set and bounded fixed-seed worldgen census. */
public final class SaveWorldgenSetSmoke {
  private SaveWorldgenSetSmoke() {}

  public static void main(String[] args) throws Exception {
    if (args.length != 10)
      throw new IllegalArgumentException(
          "usage: SaveWorldgenSetSmoke server.jar workspace port seed username minX maxX minZ maxZ settleTicks");
    Path jar = Paths.get(args[0]), workspace = Paths.get(args[1]);
    int port = Integer.parseInt(args[2]);
    long seed = Long.parseLong(args[3]);
    String user = args[4];
    int minX = Integer.parseInt(args[5]), maxX = Integer.parseInt(args[6]);
    int minZ = Integer.parseInt(args[7]), maxZ = Integer.parseInt(args[8]);
    int settle = Integer.parseInt(args[9]);
    require(seed == 17320110707L && maxX - minX == 2 && maxZ - minZ == 2,
        "save/worldgen fixture drift");
    WorldgenCensus first = capture(jar, workspace, port, seed, user, minX, maxX, minZ, maxZ, settle);
    RegionFrame.Summary saved = RegionFrame.inspect(workspace.resolve("world/region"), minX, maxX, minZ, maxZ);
    WorldgenCensus replay = capture(jar, workspace, port, seed, user, minX, maxX, minZ, maxZ, 0);
    RegionFrame.Summary resaved = RegionFrame.inspect(workspace.resolve("world/region"), minX, maxX, minZ, maxZ);
    require(first.replayEquals(replay), "stable geology changed across clean save/restart");
    require(saved.equals(resaved) && saved.frames() == 9 && saved.zlib() == 9,
        "McRegion frame topology changed across restart");
    System.out.println("WORLDLINE_M621_CENSUS=" + first.describe());
    require(first.surfaceFamilies() >= 2, "fixed region lacks multiple surface families");
    require(first.caveAir() > 0, "fixed region lacks bounded subsurface air");
    require(first.oreBlocks() > 0 && first.oreVeins() >= 3, "fixed region lacks ore components");
    String signal = "region=31:33:31:33,chunks=9,mcr=9-valid-zlib-nbt,"
        + "restart=geology-equal,biomes=surface-families>=2,caves=subsurface-air>0,"
        + "ores=blocks>0+veins>=3,clients=2,disconnect=clean";
    String trace = "v1|server=official-b1.7.3|seed=17320110707|region=3x3-absolute-chunks-31:33:31:33"
        + "|save=mcregion-location+timestamp+sector-bounds+zlib-nbt-root"
        + "|replay=pre-save-vs-clean-restart-geology-equal"
        + "|worldgen=surface-families-at-least2+subsurface-air+ore-components"
        + "|oracle=save-chunk-worldgen-set|clients=2,disconnect=clean";
    System.out.println("WORLDLINE_M621_SET=" + signal);
    System.out.println("WORLDLINE_M621_TRACE=" + trace);
    System.out.println("WORLDLINE_M621_SIGNATURE=" + sha(trace));
  }

  private static WorldgenCensus capture(Path jar, Path workspace, int port, long seed, String user,
      int minX, int maxX, int minZ, int maxZ, int settle) throws Exception {
    Duration timeout = Duration.ofSeconds(120);
    B173DedicatedServer server = new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient client = new B173WireClient("127.0.0.1", port, user, timeout);
    try {
      server.boot();
      double spawnX = ((minX + maxX) / 2) * 16D + 8.5D;
      double spawnZ = ((minZ + maxZ) / 2) * 16D + 8.5D;
      B173PlayerSeed.write(workspace, user, spawnX, 120D, spawnZ);
      client.connect();
      client.synchronizePose();
      RemoteWorldView world = null;
      for (int x = minX; x <= maxX; x++)
        for (int z = minZ; z <= maxZ; z++) world = client.awaitRemoteChunk(x, z);
      if (settle > 0) world = client.sustainTicks(settle);
      WorldgenCensus census = WorldgenCensus.measure(world, minX, maxX, minZ, maxZ);
      client.close();
      awaitPlayers(server, 0);
      server.save();
      return census;
    } finally {
      client.close();
      server.close();
    }
  }



  private static void require(boolean value, String message) {
    if (!value) throw new IllegalStateException(message);
  }
}
