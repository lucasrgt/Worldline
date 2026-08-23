package worldline.smoke.meleepursuitsetb173;
import static worldline.b173server.B173FixtureSupport.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Observes Packet24 types 54 and 51 both moving toward the actor pose in one session. */
public final class MeleePursuitSetSmoke {
  private MeleePursuitSetSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 7)
      throw new IllegalArgumentException(
          "usage: MeleePursuitSetSmoke server.jar workspace port seed username chunkX chunkZ");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    int cx = Integer.parseInt(a[5]), cz = Integer.parseInt(a[6]);
    require(seed == 17320110707L && user.equals("Pursuit455") && user.length() <= 16,
        "melee-pursuit-set identity drift");
    Duration timeout = Duration.ofSeconds(180);
    B173DedicatedServer server =
        B173DedicatedServer.monsters(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout);
    BlockPosition top, first, second;
    int column;
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1, 2, 3, 4},
          new int[] {1, 2, 52, 52, 322}, new int[] {32, 48, 1, 1, 8}, new int[] {0, 0, 0, 0, 0});
      actor.connect();
      actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() == 5, "melee-pursuit inventory drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      top = foundation(initial, cx, cz);
      column = 0;
      actor.selectHeldSlot(0);
      while (
          water(initial.blockAt(local(top.x(), cx), top.y() + 1, local(top.z(), cz)).legacyId())) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        require(++column <= 15, "water column exceeded melee-pursuit fixture");
      }
      for (int lift = 0; lift < 8; lift++) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        column++;
      }
      actor.selectHeldSlot(1);
      for (int r = 1; r <= 3; r++) {
        for (int z = -r + 1; z < r; z++) {
          grass(actor, new BlockPosition(top.x() - r + 1, top.y(), top.z() + z), BlockFace.WEST);
          grass(actor, new BlockPosition(top.x() + r - 1, top.y(), top.z() + z), BlockFace.EAST);
        }
        for (int x = -r + 1; x < r; x++) {
          grass(actor, new BlockPosition(top.x() + x, top.y(), top.z() - r + 1), BlockFace.NORTH);
          grass(actor, new BlockPosition(top.x() + x, top.y(), top.z() + r - 1), BlockFace.SOUTH);
        }
        grass(actor, new BlockPosition(top.x() - r, top.y(), top.z() - r + 1), BlockFace.NORTH);
        grass(actor, new BlockPosition(top.x() - r, top.y(), top.z() + r - 1), BlockFace.SOUTH);
        grass(actor, new BlockPosition(top.x() + r, top.y(), top.z() - r + 1), BlockFace.NORTH);
        grass(actor, new BlockPosition(top.x() + r, top.y(), top.z() + r - 1), BlockFace.SOUTH);
      }
      actor.selectHeldSlot(2);
      first = place(actor, top, BlockFace.UP, 52);
      actor.selectHeldSlot(3);
      second = place(actor, first, BlockFace.EAST, 52);
      actor.sustainTicks(5);
      actor.close();
      awaitPlayers(server, 0);
      server.save();
    } finally {
      actor.close();
      server.close();
    }
    Thread.sleep(1000L);
    B173SpawnerSeed.entity(workspace, first, "Zombie");
    B173SpawnerSeed.entity(workspace, second, "Skeleton");
    server = B173DedicatedServer.monsters(jar, workspace, port, seed, timeout, 3, true);
    actor = new B173WireClient("127.0.0.1", port, user, timeout);
    try {
      server.boot();
      actor.connect();
      actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() >= 1, "melee-pursuit reload inventory drift");
      B173MeleePursuit.heal(actor);
      stand(actor, first);
      B173MeleePursuit.heal(actor);
      server.setTime(14000L);
      RemoteMobSpawn zombie = B173MeleePursuit.near(actor, 54, first),
                     skeleton = B173MeleePursuit.near(actor, 51, second);
      require(zombie.legacyType() == 54 && skeleton.legacyType() == 51
              && zombie.entityId() != actor.state().entityId()
              && skeleton.entityId() != actor.state().entityId()
              && zombie.entityId() != skeleton.entityId() && zombie.legacyType() != 90
              && skeleton.legacyType() != 90,
          "hostile Packet24 identity drift");
      RemoteMobMovement zMove = B173MeleePursuit.toward(actor, zombie.entityId()),
                        sMove = B173MeleePursuit.toward(actor, skeleton.entityId());
      require(zMove.entityId() == zombie.entityId() && sMove.entityId() == skeleton.entityId()
              && (zMove.packetId() == 31 || zMove.packetId() == 33 || zMove.packetId() == 34)
              && (sMove.packetId() == 31 || sMove.packetId() == 33 || sMove.packetId() == 34),
          "pursuit packet drift");
      require(actor.health() > 0, "actor died during melee pursuit");
      actor.close();
      awaitPlayers(server, 0);
      server.save();
      String evidence = "column=" + column + ",platform=7x7-48grass,spawners=" + cell(first) + "+"
          + cell(second)
          + ",entityid=Zombie+Skeleton,mobs=type54+type51,night=14000,apple=322,pursuit=toward-pose,move-cap=9,clients=1,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=raised-7x7-grass-platform+two-spawner52|cause=nbt-entityid-zombie+skeleton+time-14000|wire=packet24-type54+packet24-type51+packet31-or33-or34-toward-pose|oracle=zombie-and-skeleton-melee-pursuit-toward-actor-pose|"
          + evidence;
      System.out.println("WORLDLINE_M455_SET=" + evidence);
      System.out.println("WORLDLINE_M455_TRACE=" + trace);
      System.out.println("WORLDLINE_M455_SIGNATURE=" + sha(trace));
    } finally {
      actor.close();
      server.close();
    }
  }
  private static void stand(B173WireClient a, BlockPosition p) {
    approach(a, p.x() - 2.5D, p.y(), p.z() - 2.5D, 1.5D);
  }
  private static void approach(B173WireClient a, double x, double y, double z, double reach) {
    for (int step = 0; step < 16; step++) {
      B173MeleePursuit.heal(a);
      PlayerPose here = a.moveAndObserve(0D, 0D, 0D, 1).resulting();
      double dx = x - here.x(), dy = y - here.y(), dz = z - here.z(),
             dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
      if (dist <= reach)
        return;
      double s = Math.min(1D, 9.0D / dist);
      a.moveAndObserve(dx * s, dy * s, dz * s, 2);
    }
  }
  private static void grass(B173WireClient a, BlockPosition support, BlockFace face)
      throws Exception {
    place(a, support, face, 2);
  }
  private static BlockPosition foundation(RemoteChunkSnapshot q, int cx, int cz) {
    for (int x = 4; x <= 11; x++)
      for (int z = 4; z <= 11; z++)
        for (int y = 126; y >= 1; y--)
          if (q.blockAt(x, y, z).legacyId() == 3 && water(q.blockAt(x, y + 1, z).legacyId()))
            return new BlockPosition(cx * 16 + x, y, cz * 16 + z);
    throw new IllegalStateException("no deterministic melee-pursuit foundation");
  }
  private static String cell(BlockPosition p) {
    return p.x() + ":" + p.y() + ":" + p.z() + ":52:0";
  }
  private static void require(boolean v, String m) {
    if (!v)
      throw new IllegalStateException(m);
  }
}
