package worldline.smoke.skeletonbonesetb173;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Kills spawned skeleton Packet24 type 51, collects Packet21 bone 352, mills 351x3:15, and bonemeals wheat 59:7. */
public final class SkeletonBoneSetSmoke {
  private static final RemoteItemStack BONE = new RemoteItemStack(352, 1, 0),
                                       MEAL = new RemoteItemStack(351, 3, 15);
  private SkeletonBoneSetSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 7)
      throw new IllegalArgumentException(
          "usage: SkeletonBoneSetSmoke server.jar workspace port seed username chunkX chunkZ");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    int cx = Integer.parseInt(a[5]), cz = Integer.parseInt(a[6]);
    require(seed == 17320110707L && user.equals("SkelBone422") && user.length() <= 16,
        "skeleton-bone identity drift");
    B173BoneMealCraft.verify();
    Duration timeout = Duration.ofSeconds(180);
    B173DedicatedServer server =
        B173DedicatedServer.monsters(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout);
    BlockPosition top, spawner, farm, wheat;
    int column;
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D,
          new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8}, new int[] {1, 2, 52, 276, 320, 85, 290, 295, 352},
          new int[] {32, 48, 1, 1, 8, 24, 1, 8, 1}, new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0});
      actor.connect();
      actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() == 9, "skeleton-bone inventory drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      top = foundation(initial, cx, cz);
      column = 0;
      actor.selectHeldSlot(0);
      while (
          water(initial.blockAt(local(top.x(), cx), top.y() + 1, local(top.z(), cz)).legacyId())) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        require(++column <= 15, "water column exceeded skeleton-bone fixture");
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
      arena(actor, top, 5);
      actor.selectHeldSlot(2);
      spawner = place(actor, top, BlockFace.UP, 52);
      worldline.test.WorldlineSmokeAwait.observe(actor, 5);
      actor.close();
      awaitPlayers(server, 0);
      server.save();
    } finally {
      actor.close();
      server.close();
    }
    Thread.sleep(1000L);
    B173SpawnerSeed.entity(workspace, spawner, "Skeleton");
    B173PlayerSeed.writeInventory(workspace, user, top.x() + 0.5D, top.y() + 1.0D, top.z() + 0.5D,
        new int[] {0, 1, 2, 3, 4, 8}, new int[] {276, 320, 322, 290, 295, 352},
        new int[] {1, 8, 4, 1, 8, 1}, new int[] {0, 0, 0, 0, 0, 0});
    server = B173DedicatedServer.monsters(jar, workspace, port, seed, timeout, 3, true);
    actor = new B173WireClient("127.0.0.1", port, user, timeout);
    try {
      server.boot();
      actor.connect();
      actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() == 6, "skeleton-bone reload inventory drift");
      B173BoneMealCraft.apply(actor);
      require(same(actor.inventory(), 44, MEAL), "crafted bone meal 351x3:15 absent");
      farm = new BlockPosition(top.x() - 1, top.y(), top.z());
      step(actor, farm.x() + 0.5D, farm.y() + 1.0D, farm.z() + 0.5D);
      int hoe = find(actor.inventory(), 290);
      require(hoe >= 36, "hoe 290 lost");
      actor.selectHeldSlot(hoe - 36);
      actor.look(90F, 90F);
      actor.useHeldItemOnBlock(farm, BlockFace.UP);
      actor.awaitBlock(farm, new BlockState(60, 0));
      int seedSlot = find(actor.inventory(), 295);
      require(seedSlot >= 36, "seeds 295 lost");
      actor.selectHeldSlot(seedSlot - 36);
      wheat = BlockFace.UP.adjacent(farm);
      actor.useHeldItemOnBlock(farm, BlockFace.UP);
      actor.awaitBlock(wheat, new BlockState(59, 0));
      int meal = find(actor.inventory(), 351);
      require(
          meal >= 36 && same(actor.inventory(), meal, MEAL), "bone meal 351:15 lost before use");
      actor.selectHeldSlot(meal - 36);
      actor.useHeldItemOnBlock(wheat, BlockFace.UP);
      require(age(actor, wheat, 59, 7, 40), "bonemeal wheat 59:0->59:7 absent");
      RemoteDroppedItem bone = null;
      int kills = 0;
      go(actor, spawner);
      server.setTime(14000L);
      while (bone == null) {
        require(++kills <= 8, "official skeleton bone drop absent after bounded kills");
        RemoteMobSpawn skeleton = near(actor, 51, spawner, top);
        require(skeleton.legacyType() == 51 && skeleton.entityId() != actor.state().entityId()
                && skeleton.legacyType() != 90,
            "skeleton Packet24 identity drift");
        kill(actor, skeleton, top);
        bone = worldline.test.WorldlineSmokeAwait.awaitEntityOrNull(actor,
            () -> actor.peekDroppedItem(BONE), value -> value != null, "skeleton bone drop", 20);
      }
      require(bone.item().legacyId() == 352 && bone.item().legacyId() != 262,
          "skeleton Packet21 bone id drift");
      actor.close();
      awaitPlayers(server, 0);
      server.save();
      String evidence = "column=" + column
          + ",platform=7x7-48grass,arena=fence85-24,spawner=" + cell(spawner)
          + ",entityid=Skeleton,mob=type51,night=14000,sword=276,drop=packet21-352,craft=351x3:15,wheat="
          + wheat.x() + ":" + wheat.y() + ":" + wheat.z()
          + ":59:0->59:7,kills<=8,clients=1,disconnect=clean";
      String trace = "v2|server=official-b1.7.3|seed=" + seed
          + "|fixture=raised-7x7-grass-platform+fence85-arena+spawner52+wheat59|cause=nbt-entityid-skeleton+time-14000+diamond-sword-packet7+packet102-bone352-to-351x3:15+packet15-bonemeal351:15|wire=packet24-type51+packet38-status3+packet29+packet21-352+packet53-crops59:7|oracle=skeleton-bone-drop-and-bonemeal-craft-use|"
          + evidence;
      System.out.println("WORLDLINE_M422_BONE=" + evidence);
      System.out.println("WORLDLINE_M422_TRACE=" + trace);
      System.out.println("WORLDLINE_M422_SIGNATURE=" + sha(trace));
    } finally {
      actor.close();
      server.close();
    }
  }
  private static RemoteMobSpawn near(
      B173WireClient a, int type, BlockPosition p, BlockPosition top) {
    for (int n = 0; n < 32; n++) {
      RemoteMobSpawn s = a.awaitMobSpawn(type);
      double dx = s.x() - (top.x() + 0.5D), dz = s.z() - (top.z() + 0.5D);
      if (Math.abs(dx) <= 2.5D && Math.abs(dz) <= 2.5D && Math.abs(s.y() - p.y()) <= 2D)
        return s;
    }
    throw new IllegalStateException("arena-contained skeleton type " + type + " absent");
  }
  private static void go(B173WireClient a, BlockPosition p) {
    step(a, p.x() + 0.5D, p.y() + 1.0D, p.z() - 1.5D);
  }
  private static void kill(B173WireClient a, RemoteMobSpawn spawn, BlockPosition top) {
    int entity = spawn.entityId();
    heal(a);
    chase(a, spawn.x(), spawn.z(), top);
    for (int hit = 0; hit < 4; hit++) {
      if (B173ShearsAccess.peekDeath(a, entity) != null)
        break;
      strike(a, entity);
    }
    for (int hit = 0; hit < 8 && B173ShearsAccess.peekDeath(a, entity) == null; hit++) {
      RemoteMobMovement m = a.awaitMobMovement(entity);
      heal(a);
      chase(a, m.toX(), m.toZ(), top);
      strike(a, entity);
    }
    RemoteMobDeath death = a.awaitMobDeath(entity);
    require(death.entityId() == entity && death.hurtObserved(), "skeleton death drift");
  }
  private static void chase(B173WireClient a, double x, double z, BlockPosition top) {
    double boundedX = clamp(x, top.x() - 2.5D, top.x() + 3.5D);
    double boundedZ = clamp(z - 1.5D, top.z() - 2.5D, top.z() + 3.5D);
    step(a, boundedX, top.y() + 1.0D, boundedZ);
  }
  private static void step(B173WireClient a, double x, double y, double z) {
    for (int n = 0; n < 32; n++) {
      heal(a);
      PlayerPose here = a.moveAndObserve(0D, 0D, 0D, 1).resulting();
      double dx = x - here.x(), dz = z - here.z(), dist = Math.sqrt(dx * dx + dz * dz);
      if (dist <= 3.5D)
        return;
      double s = Math.min(1D, 9.0D / dist);
      a.moveAndObserve(dx * s, 0D, dz * s, 2);
    }
    throw new IllegalStateException("actor could not reach skeleton-bone target");
  }
  private static void strike(B173WireClient a, int entity) {
    heal(a);
    int sword = find(a.inventory(), 276);
    require(sword >= 36, "diamond sword lost");
    a.selectHeldSlot(sword - 36);
    a.attackMob(entity);
    worldline.test.WorldlineSmokeAwait.observe(a, 20);
    heal(a);
  }
  private static void heal(B173WireClient a) {
    int h = a.health();
    if (h == 0)
      throw new IllegalStateException("actor died during skeleton bone");
    if (h >= 20)
      return;
    int food = find(a.inventory(), 322);
    if (food < 36)
      food = find(a.inventory(), 320);
    if (food < 36)
      return;
    a.selectHeldSlot(food - 36);
    a.useSelectedItemInAir();
    worldline.test.WorldlineSmokeAwait.observe(a, 5);
  }
  private static int find(RemoteInventoryView view, int id) {
    for (int slot = 36; slot <= 44; slot++)
      if (!view.slot(slot).empty() && view.slot(slot).item().legacyId() == id)
        return slot;
    return -1;
  }
  private static boolean same(RemoteInventoryView view, int slot, RemoteItemStack item) {
    return slot >= 0 && !view.slot(slot).empty() && view.slot(slot).item().equals(item);
  }
  private static boolean age(B173WireClient a, BlockPosition p, int id, int meta, int windows)
      throws Exception {
    return worldline.test.WorldlineSmokeAwait.awaitBlockOrNull(
               a, p, new BlockState(id, meta), windows * 5)
        != null;
  }
  private static void arena(B173WireClient a, BlockPosition top, int slot) throws Exception {
    a.selectHeldSlot(slot);
    for (int x = -3; x <= 3; x++) {
      place(a, new BlockPosition(top.x() + x, top.y(), top.z() - 3), BlockFace.UP, 85);
      place(a, new BlockPosition(top.x() + x, top.y(), top.z() + 3), BlockFace.UP, 85);
    }
    for (int z = -2; z <= 2; z++) {
      place(a, new BlockPosition(top.x() - 3, top.y(), top.z() + z), BlockFace.UP, 85);
      place(a, new BlockPosition(top.x() + 3, top.y(), top.z() + z), BlockFace.UP, 85);
    }
  }
  private static BlockPosition place(
      B173WireClient a, BlockPosition support, BlockFace face, int id) throws Exception {
    BlockPosition target = face.adjacent(support);
    a.placeHeldBlock(support, face);
    a.awaitBlock(target, new BlockState(id, 0));
    return target;
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
    throw new IllegalStateException("no deterministic skeleton-bone foundation");
  }
  private static String cell(BlockPosition p) {
    return p.x() + ":" + p.y() + ":" + p.z() + ":52:0";
  }
  private static double clamp(double v, double lo, double hi) {
    return Math.max(lo, Math.min(hi, v));
  }
  private static boolean water(int id) {
    return id == 8 || id == 9;
  }
  private static int local(int v, int c) {
    return v - c * 16;
  }
  private static void awaitPlayers(B173DedicatedServer s, int n) throws Exception {
    long e = System.currentTimeMillis() + 5000;
    while (System.currentTimeMillis() < e) {
      if (s.players().size() == n)
        return;
      Thread.sleep(100);
    }
    throw new IllegalStateException("player count drift");
  }
  private static String sha(String s) throws Exception {
    byte[] b = MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));
    StringBuilder v = new StringBuilder();
    for (byte x : b)
      v.append(String.format("%02x", x & 255));
    return v.toString();
  }
  private static void require(boolean v, String m) {
    if (!v)
      throw new IllegalStateException(m);
  }
}
