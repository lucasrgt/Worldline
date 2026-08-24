package worldline.smoke.dungeongenerationcensusb173;
import static worldline.b173server.B173FixtureSupport.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Arrays;
import worldline.api.*;
import worldline.b173server.*;
import worldline.testkit.DungeonGenerationFixture;

/** Freezes one fixed-seed dungeon/spawner/loot census through the official server. */
public final class DungeonGenerationCensusSmoke {
  private DungeonGenerationCensusSmoke() {}
  public static void main(String[] args) throws Exception {
    if (args.length != 10) throw new IllegalArgumentException(
        "usage: DungeonGenerationCensusSmoke server.jar workspace port seed username minX maxX minZ maxZ settleTicks");
    Path jar = Paths.get(args[0]);
    Path workspace = Paths.get(args[1]);
    int port = Integer.parseInt(args[2]);
    long seed = Long.parseLong(args[3]);
    String user = args[4];
    int minX = Integer.parseInt(args[5]);
    int maxX = Integer.parseInt(args[6]);
    int minZ = Integer.parseInt(args[7]);
    int maxZ = Integer.parseInt(args[8]);
    int settle = Integer.parseInt(args[9]);
    require(seed == 17320110707L && minX == -5 && maxX == 5 && minZ == -5 && maxZ == 5,
        "dungeon generation fixture drift");
    DungeonGenerationFixture.Evidence evidence = generate(jar, workspace, port, seed, user,
        minX, maxX, minZ, maxZ, settle);
    Loot loot = openLoot(jar, workspace, port, seed, user, evidence);
    String signal = "region=-5:5:-5:5,chunks=121,dungeon=spawner+linked-chest,"
        + "loot=nonempty-packet100,replicas=2,disconnect=clean";
    String trace = "v1|server=official-b1.7.3|seed=17320110707|region=11x11-chunks"
        + "|generation=populated-dungeon-spawner52+linked-chest54"
        + "|census=spawners" + evidence.spawners().size() + "+linked-chests"
        + evidence.linkedChests().size() + "+sha256:" + evidence.digest()
        + "|selected=" + cell(evidence.selectedChest()) + "|loot=" + loot.signature
        + "|oracle=fixed-seed-dungeon-generation-census|clients=2,disconnect=clean";
    Files.write(workspace.resolve("worldline-m626-evidence.txt"),
        Arrays.asList(trace, sha(trace)), StandardCharsets.UTF_8);
    System.out.println("WORLDLINE_M626_CENSUS=spawners=" + evidence.spawners().size()
        + ",linkedChests=" + evidence.linkedChests().size() + ",digest=" + evidence.digest());
    System.out.println("WORLDLINE_M626_SET=" + signal);
    System.out.println("WORLDLINE_M626_TRACE=" + trace);
    System.out.println("WORLDLINE_M626_SIGNATURE=" + sha(trace));
  }

  private static DungeonGenerationFixture.Evidence generate(Path jar, Path workspace, int port,
      long seed, String user, int minX, int maxX, int minZ, int maxZ, int settle) throws Exception {
    Duration timeout = Duration.ofSeconds(180);
    B173DedicatedServer server = new B173DedicatedServer(jar, workspace, port, seed, timeout, 5, true);
    B173WireClient client = new B173WireClient("127.0.0.1", port, user, timeout);
    try {
      server.boot();
      B173PlayerSeed.write(workspace, user, 8.5D, 120D, 8.5D);
      client.connect();
      client.synchronizePose();
      RemoteWorldView world = null;
      for (int x = minX; x <= maxX; x++) for (int z = minZ; z <= maxZ; z++)
        world = client.awaitRemoteChunk(x, z);
      if (settle > 0) world = client.sustainTicks(settle);
      DungeonGenerationFixture.Evidence result = DungeonGenerationFixture.observe(
          world, minX, maxX, minZ, maxZ);
      client.close();
      awaitPlayers(server, 0);
      server.save();
      return result;
    } finally {
      client.close();
      server.close();
    }
  }

  private static Loot openLoot(Path jar, Path workspace, int port, long seed, String user,
      DungeonGenerationFixture.Evidence evidence) throws Exception {
    BlockPosition stand = evidence.standingPosition();
    BlockPosition chest = evidence.selectedChest();
    B173PlayerSeed.write(workspace, user, stand.x() + .5D, stand.y(), stand.z() + .5D);
    Duration timeout = Duration.ofSeconds(120);
    B173DedicatedServer server = new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient client = new B173WireClient("127.0.0.1", port, user, timeout);
    try {
      server.boot();
      client.connect();
      client.synchronizePose();
      client.awaitInventory();
      client.awaitRemoteChunk(Math.floorDiv(chest.x(), 16), Math.floorDiv(chest.z(), 16));
      RemoteContainerWindow window = client.openChest(chest, BlockFace.UP);
      require(window.descriptor().kind() == RemoteWindowKind.CHEST
          && window.descriptor().containerSlots() == 27, "generated loot chest window drift");
      StringBuilder items = new StringBuilder();
      int occupied = 0;
      for (int slot = 0; slot < 27; slot++) if (!window.inventory().slot(slot).empty()) {
        RemoteItemStack item = window.inventory().slot(slot).item();
        occupied++;
        items.append(slot).append(':').append(item.legacyId()).append('x').append(item.count())
            .append(':').append(item.damage()).append(',');
      }
      require(occupied > 0, "generated dungeon chest has no loot");
      return new Loot(occupied + "slots-" + sha(items.toString()));
    } finally {
      client.close();
      server.close();
    }
  }

  private static String cell(BlockPosition value) {
    return value.x() + ":" + value.y() + ":" + value.z();
  }
  private static void require(boolean value, String message) {
    if (!value) throw new IllegalStateException(message);
  }
  private static final class Loot {
    final String signature;
    Loot(String value) { signature = value; }
  }
}
