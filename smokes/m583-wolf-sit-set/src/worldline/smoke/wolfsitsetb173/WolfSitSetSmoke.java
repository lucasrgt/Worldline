package worldline.smoke.wolfsitsetb173;
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
import worldline.api.RemoteMobSpawn;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173PlayerSeed;
import worldline.b173server.B173ShearsAccess;
import worldline.b173server.B173WireClient;
import worldline.b173server.B173WolfAccess;
import worldline.test.WorldlineSmokeAwait;

/** Observes Packet24 wolf type 95, tames with bone 352, then Packet7 stick 280 sit and stand. */
public final class WolfSitSetSmoke {
  private WolfSitSetSmoke() {}

  public static void main(String[] arguments) throws Exception {
    if (arguments.length != 7)
      throw new IllegalArgumentException(
          "usage: WolfSitSetSmoke server.jar workspace port seed username chunkX chunkZ");
    Path jar = Paths.get(arguments[0]);
    Path workspace = Paths.get(arguments[1]);
    int port = Integer.parseInt(arguments[2]);
    long seed = Long.parseLong(arguments[3]);
    String user = arguments[4];
    int chunkX = Integer.parseInt(arguments[5]);
    int chunkZ = Integer.parseInt(arguments[6]);
    require(seed == 17320110707L && user.equals("WolfSit583") && user.length() <= 16, "wolf-sit-set identity drift");
    Duration timeout = Duration.ofSeconds(180);
    B173DedicatedServer server = B173DedicatedServer.animals(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout);
    BlockPosition top;
    BlockPosition spawner;
    int column;
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1, 2, 3, 4},
          new int[] {1, 2, 52, 352, 280}, new int[] {32, 48, 1, 64, 1}, new int[] {0, 0, 0, 0, 0});
      actor.connect();
      actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() == 5, "wolf-sit-set inventory drift");
      WorldlineSmokeAwait.observe(actor, 5);
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(chunkX, chunkZ).chunkAt(chunkX, chunkZ);
      top = foundation(initial, chunkX, chunkZ);
      column = 0;
      actor.selectHeldSlot(0);
      while (water(initial.blockAt(local(top.x(), chunkX), top.y() + 1, local(top.z(), chunkZ)).legacyId())) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        require(++column <= 15, "water column exceeded wolf-sit-set fixture");
      }
      int lift = 0;
      while (lift < 8) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        column++;
        lift++;
      }
      actor.selectHeldSlot(1);
      platform(actor, top);
      actor.selectHeldSlot(2);
      spawner = place(actor, top, BlockFace.UP, 52);
      WorldlineSmokeAwait.observe(actor, 5);
      actor.close();
      awaitPlayers(server, 0);
      server.save();
    } finally {
      actor.close();
      server.close();
    }
    Thread.sleep(1000L);
    B173WolfAccess.retarget(workspace, spawner);
    server = B173DedicatedServer.animals(jar, workspace, port, seed, timeout, 3, true);
    actor = new B173WireClient("127.0.0.1", port, user, timeout);
    try {
      server.boot();
      actor.connect();
      actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() >= 3, "wolf-sit-set reload inventory drift");
      WorldlineSmokeAwait.observe(actor, 5);
      RemoteMobSpawn wolf = actor.awaitMobSpawn(95);
      require(wolf.legacyType() == 95 && wolf.entityId() != actor.state().entityId(),
          "wolf Packet24 type 95 identity drift");
      int tame = B173WolfAccess.tame(actor, wolf);
      require(tame == 7, "wolf Packet38 status 7 tame drift");
      require(B173ShearsAccess.peekDeath(actor, wolf.entityId()) == null, "Packet38 status 3 death after bone tame");
      int seated = B173WolfAccess.awaitTamedSit(actor, wolf.entityId());
      require((seated & 5) == 5, "wolf Packet40 tamed sitting flag drift after tame");
      int standing = B173WolfAccess.stand(actor, wolf);
      require((standing & 4) != 0 && (standing & 1) == 0, "wolf Packet40 standing flag drift after unsit");
      int sitting = B173WolfAccess.sit(actor, wolf);
      require((sitting & 5) == 5, "wolf Packet40 sitting flag drift after owner sit");
      int stood = B173WolfAccess.stand(actor, wolf);
      require((stood & 4) != 0 && (stood & 1) == 0, "wolf Packet40 standing flag drift after owner stand");
      require(B173ShearsAccess.peekDeath(actor, wolf.entityId()) == null, "Packet38 status 3 death after sit-stand");
      actor.close();
      awaitPlayers(server, 0);
      server.save();
      String evidence = "column=" + column + ",platform=7x7-48grass,spawner=" + spawner.x() + ":" + spawner.y() + ":"
          + spawner.z() + ":52:0,mob=type95,bone=352,tame=packet38-status7,"
          + "sit=packet7-button0+packet40-sit,stand=packet7-button0+packet40-stand,"
          + "held=280,clients=1,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed + "|fixture=raised-7x7-grass-platform+wolf-spawner52"
          + "|cause=packet7-button0-bone352+packet7-button0-stick280-sit"
          + "+packet7-button0-stick280-stand"
          + "|wire=packet24-type95+packet38-status7+packet40-index16-sit"
          + "+packet40-index16-stand"
          + "|oracle=tamed-wolf-type95-owner-sit-stand-not-m449-anger-not-m468-assist|" + evidence;
      System.out.println("WORLDLINE_M583_SET=" + evidence);
      System.out.println("WORLDLINE_M583_TRACE=" + trace);
      System.out.println("WORLDLINE_M583_SIGNATURE=" + sha(trace));
    } finally {
      actor.close();
      server.close();
    }
  }

  private static void platform(B173WireClient actor, BlockPosition top) throws Exception {
    int radius = 1;
    while (radius <= 3) {
      int z = -radius + 1;
      while (z < radius) {
        grass(actor, new BlockPosition(top.x() - radius + 1, top.y(), top.z() + z), BlockFace.WEST);
        grass(actor, new BlockPosition(top.x() + radius - 1, top.y(), top.z() + z), BlockFace.EAST);
        z++;
      }
      int x = -radius + 1;
      while (x < radius) {
        grass(actor, new BlockPosition(top.x() + x, top.y(), top.z() - radius + 1), BlockFace.NORTH);
        grass(actor, new BlockPosition(top.x() + x, top.y(), top.z() + radius - 1), BlockFace.SOUTH);
        x++;
      }
      grass(actor, new BlockPosition(top.x() - radius, top.y(), top.z() - radius + 1), BlockFace.NORTH);
      grass(actor, new BlockPosition(top.x() - radius, top.y(), top.z() + radius - 1), BlockFace.SOUTH);
      grass(actor, new BlockPosition(top.x() + radius, top.y(), top.z() - radius + 1), BlockFace.NORTH);
      grass(actor, new BlockPosition(top.x() + radius, top.y(), top.z() + radius - 1), BlockFace.SOUTH);
      radius++;
    }
  }

  private static void grass(B173WireClient actor, BlockPosition support, BlockFace face) throws Exception {
    place(actor, support, face, 2);
  }

  private static BlockPosition foundation(RemoteChunkSnapshot chunk, int chunkX, int chunkZ) {
    int x = 4;
    while (x <= 11) {
      int z = 4;
      while (z <= 11) {
        int y = 126;
        while (y >= 1) {
          if (chunk.blockAt(x, y, z).legacyId() == 3 && water(chunk.blockAt(x, y + 1, z).legacyId()))
            return new BlockPosition(chunkX * 16 + x, y, chunkZ * 16 + z);
          y--;
        }
        z++;
      }
      x++;
    }
    throw new IllegalStateException("no deterministic wolf-sit-set foundation");
  }

  private static void require(boolean value, String message) {
    if (!value)
      throw new IllegalStateException(message);
  }
}
