package worldline.smoke.animaldropssetb173;
import static worldline.b173server.B173FixtureSupport.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Kills official cow 92 and chicken 93 from rewritten spawners and observes Packet21 leather 334 plus feather 288. */
public final class AnimalDropsSetSmoke {
  private static final RemoteItemStack LEATHER = new RemoteItemStack(334, 1, 0),
                                       FEATHER = new RemoteItemStack(288, 1, 0);
  private AnimalDropsSetSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 7)
      throw new IllegalArgumentException(
          "usage: AnimalDropsSetSmoke server.jar workspace port seed username chunkX chunkZ");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    int cx = Integer.parseInt(a[5]), cz = Integer.parseInt(a[6]);
    require(seed == 17320110707L && user.equals("AnimDrop389") && user.length() <= 16,
        "animal-drops-set identity drift");
    Duration timeout = Duration.ofSeconds(180);
    B173DedicatedServer server =
        B173DedicatedServer.animals(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout);
    BlockPosition top, cow, chicken;
    int column;
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1, 2, 3},
          new int[] {1, 2, 52, 276}, new int[] {32, 48, 2, 1}, new int[] {0, 0, 0, 0});
      actor.connect();
      actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() == 4, "animal-drops-set inventory drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      top = foundation(initial, cx, cz);
      column = 0;
      actor.selectHeldSlot(0);
      while (
          water(initial.blockAt(local(top.x(), cx), top.y() + 1, local(top.z(), cz)).legacyId())) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        require(++column <= 15, "water column exceeded animal-drops-set fixture");
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
      cow = place(actor, top, BlockFace.UP, 52);
      chicken = place(actor, cow, BlockFace.EAST, 52);
      worldline.test.WorldlineSmokeAwait.observe(actor, 5);
      actor.close();
      awaitPlayers(server, 0);
      server.save();
    } finally {
      actor.close();
      server.close();
    }
    Thread.sleep(1000L);
    B173SpawnerSeed.cowAndChicken(workspace, cow);
    server = B173DedicatedServer.animals(jar, workspace, port, seed, timeout, 3, true);
    actor = new B173WireClient("127.0.0.1", port, user, timeout);
    try {
      server.boot();
      actor.connect();
      actor.synchronizePose();
      require(
          actor.awaitInventory().occupiedSlots() >= 1, "animal-drops-set reload inventory drift");
      RemoteDroppedItem leather = killUntil(actor, 92, LEATHER),
                        feather = killUntil(actor, 93, FEATHER);
      require(leather.item().equals(LEATHER) && leather.item().legacyId() == 334,
          "cow Packet21 leather 334 absent");
      require(feather.item().equals(FEATHER) && feather.item().legacyId() == 288,
          "chicken Packet21 feather 288 absent");
      actor.close();
      awaitPlayers(server, 0);
      server.save();
      String evidence = "column=" + column + ",platform=7x7-48grass,cow=" + cow.x() + ":" + cow.y()
          + ":" + cow.z() + ":52:0,chicken=" + chicken.x() + ":" + chicken.y() + ":" + chicken.z()
          + ":52:0,mobs=type92+type93,death=packet7-sword276+packet38-status3+packet29,drops=packet21-334+packet21-288,kills<=8,clients=1,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=raised-7x7-grass-platform+cow-spawner52+chicken-spawner52|cause=official-diamond-sword-packet7|wire=packet24-type92+packet24-type93+packet38-status3+packet29+packet21-leather334+packet21-feather288|oracle=cow-leather-and-chicken-feather-drops|"
          + evidence;
      System.out.println("WORLDLINE_M389_SET=" + evidence);
      System.out.println("WORLDLINE_M389_TRACE=" + trace);
      System.out.println("WORLDLINE_M389_SIGNATURE=" + sha(trace));
    } finally {
      actor.close();
      server.close();
    }
  }
  private static RemoteDroppedItem killUntil(B173WireClient a, int type, RemoteItemStack drop) {
    RemoteDroppedItem found = null;
    int kills = 0;
    while (found == null) {
      require(++kills <= 8, "official animal drop absent after bounded kills");
      RemoteMobSpawn spawn = a.awaitMobSpawn(type);
      require(spawn.legacyType() == type && spawn.entityId() != a.state().entityId(),
          "animal Packet24 identity drift");
      int entity = spawn.entityId();
      approach(a, spawn.x(), spawn.y() + 1.0D, spawn.z() - 1.5D);
      for (int hit = 0; hit < 8; hit++) {
        if (B173ShearsAccess.peekDeath(a, entity) != null)
          break;
        int sword = find(a.inventory(), 276);
        require(sword >= 36, "diamond sword lost");
        a.selectHeldSlot(sword - 36);
        a.attackMob(entity);
        worldline.test.WorldlineSmokeAwait.observe(a, 20);
      }
      RemoteMobDeath death = a.awaitMobDeath(entity);
      require(death.entityId() == entity && death.deathStatus() == 3 && death.destroyPacket() == 29,
          "animal Packet38/29 death drift");
      found = worldline.test.WorldlineSmokeAwait.awaitEntityOrNull(
          a, () -> a.peekDroppedItem(drop), value -> value != null, "animal drop", 20);
    }
    require(found.item().equals(drop), "animal Packet21 drop drift");
    return found;
  }
  private static int find(RemoteInventoryView view, int id) {
    for (int slot = 36; slot <= 44; slot++)
      if (!view.slot(slot).empty() && view.slot(slot).item().legacyId() == id)
        return slot;
    return -1;
  }
  private static void approach(B173WireClient a, double x, double y, double z) {
    for (int step = 0; step < 16; step++) {
      PlayerPose here = a.moveAndObserve(0D, 0D, 0D, 1).resulting();
      double dx = x - here.x(), dy = y - here.y(), dz = z - here.z(),
             dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
      if (dist <= 2.5D)
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
    throw new IllegalStateException("no deterministic animal-drops-set foundation");
  }
  private static void require(boolean v, String m) {
    if (!v)
      throw new IllegalStateException(m);
  }
}
