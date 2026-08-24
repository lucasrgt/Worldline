package worldline.smoke.mapdatacontentb173;

import static worldline.b173server.B173FixtureSupport.awaitPlayers;
import static worldline.b173server.B173FixtureSupport.sha;
import static worldline.test.WorldlineSmokeAwait.observe;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import worldline.api.BlockPosition;
import worldline.api.RemoteMapContent;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173LevelDatSpawn;
import worldline.b173server.B173MapDataAccess;
import worldline.b173server.B173PlayerSeed;
import worldline.b173server.B173WireClient;
import worldline.testkit.MapDataContentFixture;

/** Reconstructs and freezes the fixed-seed Packet131 map color grid. */
public final class MapDataContentSmoke {
  private static final String LOADER = "MapLoad632";
  private static final String ACTOR = "MapData632";
  private MapDataContentSmoke() { }

  public static void main(String[] a) throws Exception {
    if (a.length != 4) throw new IllegalArgumentException(
        "usage: MapDataContentSmoke server.jar workspace port seed");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    Duration timeout = Duration.ofSeconds(120);
    B173DedicatedServer creator = new B173DedicatedServer(
        jar, workspace, port, seed, timeout, 3, true);
    B173DedicatedServer server = null;
    B173WireClient loader = new B173WireClient("127.0.0.1", port, LOADER, timeout);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, ACTOR, timeout);
    try {
      creator.boot();
      creator.close();
      B173LevelDatSpawn.patch(workspace.resolve("world/level.dat"), 4, 60, 4);
      server = new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
      server.boot();
      B173PlayerSeed.writeInventory(workspace, LOADER, 4.5D, 60D, 4.5D,
          new int[] {0}, new int[] {1}, new int[] {32}, new int[] {0});
      B173PlayerSeed.writeInventory(workspace, ACTOR, 4.5D, 60D, 4.5D,
          new int[] {0, 1}, new int[] {1, 358}, new int[] {32, 1}, new int[] {0, 0});
      loader.connect();
      loader.synchronizePose();
      require(loader.awaitInventory().occupiedSlots() == 1, "loader inventory seed drift");
      require(loader.awaitRemoteWorld(49).loadedChunks() == 49,
          "map region load drift");
      observe(loader, 200);
      actor.connect();
      B173MapDataAccess.begin(actor);
      actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() == 2, "map inventory seed drift");
      actor.selectHeldSlot(1);
      RemoteMapContent content = B173MapDataAccess.observe(actor, 800);
      BlockPosition position = new BlockPosition(4, 60, 4);
      MapDataContentFixture.Evidence evidence = MapDataContentFixture.observe(
          seed, position, content);
      String signal = "seed=" + seed + ",pos=4:60:4,map=358:0,columns="
          + evidence.columns() + ",nonzero=" + evidence.nonZero() + ",palette="
          + evidence.palette() + ",sha256=" + evidence.colorsSha256()
          + ",clients=2,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=spawn-pinned-4:60:4+loader+stationary-map-player+held-map358:0"
          + "|cause=create-world+stopped-level-dat-spawn-patch"
          + "+loader-await-49-chunks+settle-200+map-player-login"
          + "+packet16-select-map"
          + "+bounded-packet10-heartbeats"
          + "|wire=packet131-markers+packet131-color-columns"
          + "|oracle=converged-128x128-color-grid|" + signal;
      require(evidence.seed() == seed && evidence.position().equals(position)
              && evidence.columns() == 128, "map content fixture evidence drifted");
      actor.close();
      loader.close();
      awaitPlayers(server, 0);
      System.out.println("WORLDLINE_M632_MAP=" + signal);
      System.out.println("WORLDLINE_M632_TRACE=" + trace);
      System.out.println("WORLDLINE_M632_SIGNATURE=" + sha(trace));
    } finally {
      actor.close();
      loader.close();
      if (server != null) server.close();
      creator.close();
    }
  }

  private static void require(boolean value, String message) {
    if (!value) throw new IllegalStateException(message);
  }
}
