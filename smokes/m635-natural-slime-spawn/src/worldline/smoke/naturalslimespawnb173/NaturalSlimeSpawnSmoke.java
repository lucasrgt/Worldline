package worldline.smoke.naturalslimespawnb173;
import static worldline.b173server.B173FixtureSupport.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173NaturalSpawnRoom;
import worldline.b173server.B173PlayerSeed;
import worldline.b173server.B173SlimeTouchAccess;
import worldline.b173server.B173WireClient;
import worldline.testkit.NaturalSlimeSpawnFixture;

/** Freezes a natural slime spawn in one formula-selected Beta 1.7.3 chunk. */
public final class NaturalSlimeSpawnSmoke {
  private NaturalSlimeSpawnSmoke() {}
  public static void main(String[] args) throws Exception {
    if (args.length != 7)
      throw new IllegalArgumentException(
          "usage: NaturalSlimeSpawnSmoke server.jar workspace port seed username chunkX chunkZ");
    Path jar = Paths.get(args[0]), workspace = Paths.get(args[1]);
    int port = Integer.parseInt(args[2]);
    long seed = Long.parseLong(args[3]);
    String user = args[4];
    int cx = Integer.parseInt(args[5]), cz = Integer.parseInt(args[6]);
    require(seed == 17320110707L && cx == 98 && cz == 43
            && NaturalSlimeSpawnFixture.qualifyingChunks(seed, cx - 3, cx + 3, cz - 3, cz + 3)
                == 16,
        "natural slime fixture drift");
    Duration timeout = Duration.ofSeconds(360);
    prepare(jar, workspace, port, seed, user, cx, cz, timeout);
    B173NaturalSpawnRoom.prepare(workspace, seed, cx, cz, 3, 60, 1);
    B173DedicatedServer server =
        B173DedicatedServer.monsters(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient client = new B173WireClient("127.0.0.1", port, user, timeout);
    try {
      server.boot();
      B173PlayerSeed.write(workspace, user, cx * 16D + 8.5D, 120D, cz * 16D + 8.5D);
      client.connect();
      client.synchronizePose();
      client.awaitRemoteChunk(cx, cz);
      NaturalSlimeSpawnFixture.Evidence evidence = NaturalSlimeSpawnFixture.await(seed, cx - 3,
          cx + 3, cz - 3, cz + 3, 4800, attempt -> B173SlimeTouchAccess.pollNatural(client));
      client.close();
      awaitPlayers(server, 0);
      server.save();
      String signal = "seed=" + evidence.seed()
          + ",matrix=95:101:40:46,slime-chunks=" + evidence.qualifyingChunks()
          + ",geometry=matrix-solid-below60+3xrooms14x14x4-under16,type=55,y<16"
          + ",formula=verified,natural=no-spawner,bounded<=4800,replicas=2,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|fixture=official-generated+saved+7x7-solid-below60"
          + "+16-slime-room-stacks3x14x14x4-under16+no-spawner+surface-observer"
          + "|action=restart-monsters+load-formula-selected-chunk+bounded-4800-natural-packet24"
          + "|observation=type55+formula-selected-chunk+y<16|oracle=natural-slime-spawn|" + signal;
      System.out.println("WORLDLINE_M635_SET=" + signal);
      System.out.println("WORLDLINE_M635_TRACE=" + trace);
      System.out.println("WORLDLINE_M635_SIGNATURE=" + sha(trace));
    } finally {
      client.close();
      server.close();
    }
  }
  private static void prepare(Path jar, Path workspace, int port, long seed, String user, int cx,
      int cz, Duration timeout) throws Exception {
    B173DedicatedServer server =
        new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient client = new B173WireClient("127.0.0.1", port, user, timeout);
    try {
      server.boot();
      B173PlayerSeed.write(workspace, user, cx * 16D + 8.5D, 120D, cz * 16D + 8.5D);
      client.connect();
      client.synchronizePose();
      for (int x = cx - 3; x <= cx + 3; x++)
        for (int z = cz - 3; z <= cz + 3; z++) client.awaitRemoteChunk(x, z);
      client.close();
      awaitPlayers(server, 0);
      server.save();
    } finally {
      client.close();
      server.close();
    }
  }
  private static void require(boolean value, String message) {
    if (!value)
      throw new IllegalStateException(message);
  }
}
