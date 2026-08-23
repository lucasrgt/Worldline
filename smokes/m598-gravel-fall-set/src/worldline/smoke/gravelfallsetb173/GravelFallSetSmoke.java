package worldline.smoke.gravelfallsetb173;
import static worldline.b173server.B173FixtureSupport.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.BlockFace;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.RemoteChunkSnapshot;
import worldline.api.RemoteObjectSpawn;
import worldline.api.RemoteWorldView;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173PlayerSeed;
import worldline.b173server.B173WireClient;
import worldline.test.WorldlineSmokeAwait;

/** Removes one support and observes the official falling-gravel entity land as gravel. */
public final class GravelFallSetSmoke {
  private GravelFallSetSmoke() {}

  public static void main(String[] arguments) throws Exception {
    if (arguments.length != 9) {
      throw new IllegalArgumentException("usage: GravelFallSetSmoke server.jar workspace port "
          + "seed username chunkX chunkZ fixtureTicks gravityTicks");
    }
    Path jar = Paths.get(arguments[0]);
    Path workspace = Paths.get(arguments[1]);
    int port = Integer.parseInt(arguments[2]);
    long seed = Long.parseLong(arguments[3]);
    String username = arguments[4];
    int chunkX = Integer.parseInt(arguments[5]);
    int chunkZ = Integer.parseInt(arguments[6]);
    int fixtureTicks = Integer.parseInt(arguments[7]);
    int gravityTicks = Integer.parseInt(arguments[8]);
    Duration timeout = Duration.ofSeconds(90);
    require(seed == 17320110707L && username.equals("GravelFall598") && username.length() <= 16,
        "gravel-fall identity drift");
    B173DedicatedServer server = new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, username, timeout);
    B173WireClient reader = null;
    RemoteObjectSpawn fall = null;
    BlockPosition support = null;
    BlockPosition gravel = null;
    int column = 0;
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, username, 4.5D, 60D, 4.5D, new int[] {0, 1}, new int[] {1, 13},
          new int[] {16, 1}, new int[] {0, 0});
      actor.connect();
      actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() == 2, "gravel-fall inventory drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(chunkX, chunkZ).chunkAt(chunkX, chunkZ);
      support = foundation(initial, chunkX, chunkZ);
      actor.selectHeldSlot(0);
      while (
          water(initial.blockAt(local(support.x(), chunkX), support.y() + 1, local(support.z(), chunkZ)).legacyId())) {
        support = place(actor, support, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        column++;
        require(column <= 15, "water column exceeded gravel-fall fixture");
      }
      support = place(actor, support, BlockFace.UP, 1);
      actor.moveAndObserve(0D, 1D, 0D, 1);
      column++;
      gravel = BlockFace.UP.adjacent(support);
      require(initial.blockAt(local(gravel.x(), chunkX), gravel.y(), local(gravel.z(), chunkZ)).legacyId() == 0,
          "gravel target was not initial air");
      actor.selectHeldSlot(1);
      actor.placeHeldBlock(support, BlockFace.UP);
      actor.awaitBlock(gravel, new BlockState(13, 0));
      actor.selectHeldSlot(2);
      actor.moveAndObserve(0D, -2D, 0D, 2);
      RemoteChunkSnapshot before = WorldlineSmokeAwait.observe(actor, fixtureTicks).chunkAt(chunkX, chunkZ);
      require(before.blockAt(local(support.x(), chunkX), support.y(), local(support.z(), chunkZ))
                  .equals(new BlockState(1, 0)),
          "stable gravel support fixture drift");
      require(before.blockAt(local(gravel.x(), chunkX), gravel.y(), local(gravel.z(), chunkZ))
                  .equals(new BlockState(13, 0)),
          "stable gravel 13:0 fixture drift");
      actor.beginBreak(support);
      Thread.sleep(3000L);
      actor.finishBreak(support);
      BlockState opened =
          actor.awaitBlock(support, new BlockState(0, 0)).blockAt(support.x(), support.y(), support.z());
      fall = actor.awaitObjectSpawn(71);
      require(opened.equals(new BlockState(0, 0)) && fall.type() == 71 && fall.type() != 70
              && fall.entityId() != actor.state().entityId(),
          "falling-gravel Packet23 type drift type=" + fall.type());
      int wait = Math.max(8, gravityTicks);
      WorldlineSmokeAwait.awaitBlock(actor, gravel, new BlockState(0, 0), wait);
      RemoteWorldView live = WorldlineSmokeAwait.awaitBlock(actor, support, new BlockState(13, 0), wait);
      WorldlineSmokeAwait.observe(actor, Math.max(1, gravityTicks));
      require(live.blockAt(support.x(), support.y(), support.z()).equals(new BlockState(13, 0))
              && live.blockAt(gravel.x(), gravel.y(), gravel.z()).equals(new BlockState(0, 0)),
          "gravel did not land as 13:0 after entity " + fall.type());
      actor.close();
      awaitPlayers(server, 0);
      server.save();
      reader = new B173WireClient("127.0.0.1", port, username, timeout);
      reader.connect();
      reader.synchronizePose();
      RemoteChunkSnapshot after = reader.awaitRemoteChunk(chunkX, chunkZ).chunkAt(chunkX, chunkZ);
      require(after.blockAt(local(support.x(), chunkX), support.y(), local(support.z(), chunkZ))
                  .equals(new BlockState(13, 0)),
          "fresh landed gravel 13:0 drift");
      require(
          after.blockAt(local(gravel.x(), chunkX), gravel.y(), local(gravel.z(), chunkZ)).equals(new BlockState(0, 0)),
          "fresh cleared upper gravel drift");
      require(after.blockAt(local(support.x(), chunkX), support.y(), local(support.z(), chunkZ)).legacyId() != 12,
          "landed sand instead of gravel");
    } finally {
      actor.close();
      if (reader != null) {
        reader.close();
      }
      server.close();
    }
    require(fall != null && support != null && gravel != null, "gravel-fall evidence missing");
    String evidence = "column=" + column + ",lower=" + cell(support) + ":1:0->13:0,upper=" + cell(gravel)
        + ":13:0->0:0,entity-type=" + fall.type() + ",packet23=" + fall.type()
        + ",persisted=true,clients=2,disconnect=clean";
    String trace = "v1|server=official-b1.7.3|seed=" + seed + "|fixture=stone-column+supported-gravel13|settle="
        + fixtureTicks + "+" + gravityTicks + "ticks|cause=packet14-remove-support|confirmation=packet53-air"
        + "|effect=official-falling-gravel-entity-land"
        + "|observation=packet23-type-observed+live-packet53+fresh-login-packet51|" + evidence;
    System.out.println("WORLDLINE_M598_SET=" + evidence);
    System.out.println("WORLDLINE_M598_TRACE=" + trace);
    System.out.println("WORLDLINE_M598_SIGNATURE=" + sha(trace));
  }

  private static BlockPosition foundation(RemoteChunkSnapshot chunk, int chunkX, int chunkZ) {
    int x = 4;
    while (x <= 11) {
      int z = 4;
      while (z <= 11) {
        int y = 126;
        while (y >= 1) {
          if (chunk.blockAt(x, y, z).legacyId() == 3 && water(chunk.blockAt(x, y + 1, z).legacyId())) {
            return new BlockPosition(chunkX * 16 + x, y, chunkZ * 16 + z);
          }
          y--;
        }
        z++;
      }
      x++;
    }
    throw new IllegalStateException("no deterministic gravel-fall foundation");
  }

  private static String cell(BlockPosition position) {
    return position.x() + ":" + position.y() + ":" + position.z();
  }

  private static void require(boolean value, String message) {
    if (!value) {
      throw new IllegalStateException(message);
    }
  }
}
