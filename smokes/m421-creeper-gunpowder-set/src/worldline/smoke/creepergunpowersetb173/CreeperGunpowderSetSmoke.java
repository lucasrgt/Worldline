package worldline.smoke.creepergunpowersetb173;
import static worldline.b173server.B173FixtureSupport.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Kills spawned creeper Packet24 type 50 for Packet21 gunpowder 289, then workbench-crafts TNT 46. */
public final class CreeperGunpowderSetSmoke {
  private static final RemoteItemStack GUNPOWDER = new RemoteItemStack(289, 1, 0);
  private static final BlockState BENCH = new BlockState(58, 0);
  private CreeperGunpowderSetSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 7)
      throw new IllegalArgumentException(
          "usage: CreeperGunpowderSetSmoke server.jar workspace port seed username chunkX chunkZ");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    int cx = Integer.parseInt(a[5]), cz = Integer.parseInt(a[6]);
    require(seed == 17320110707L && user.equals("CreeprGp421") && user.length() <= 16
            && B173CreeperGunpowderCrafts.TNT == 46,
        "creeper-gunpowder identity drift");
    Duration timeout = Duration.ofSeconds(180);
    B173DedicatedServer server =
        B173DedicatedServer.monsters(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout);
    BlockPosition top, spawner, bench;
    int column;
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D,
          new int[] {0, 1, 2, 3, 4, 5, 6, 7}, new int[] {1, 2, 58, 12, 289, 52, 276, 320},
          new int[] {32, 48, 1, 4, 5, 1, 1, 8}, new int[] {0, 0, 0, 0, 0, 0, 0, 0});
      actor.connect();
      actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() == 8, "creeper-gunpowder inventory drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      top = foundation(initial, cx, cz);
      column = 0;
      actor.selectHeldSlot(0);
      while (
          water(initial.blockAt(local(top.x(), cx), top.y() + 1, local(top.z(), cz)).legacyId())) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        require(++column <= 15, "water column exceeded creeper-gunpowder fixture");
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
      actor.selectHeldSlot(0);
      for (int n = 0; n < 4; n++)
        actor.moveAndObserve(-1D, 0D, 0D, 2);
      BlockPosition west = new BlockPosition(top.x() - 3, top.y(), top.z());
      west = place(actor, west, BlockFace.WEST, 1);
      actor.moveAndObserve(-1D, 0D, 0D, 2);
      west = place(actor, west, BlockFace.WEST, 1);
      actor.moveAndObserve(-1D, 0D, 0D, 2);
      west = place(actor, west, BlockFace.WEST, 1);
      actor.moveAndObserve(-1D, 0D, 0D, 2);
      west = place(actor, west, BlockFace.WEST, 1);
      actor.selectHeldSlot(2);
      bench = place(actor, west, BlockFace.UP, 58);
      for (int n = 0; n < 8; n++)
        actor.moveAndObserve(1D, 0D, 0D, 2);
      actor.selectHeldSlot(5);
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
    B173SpawnerSeed.entity(workspace, spawner, "Creeper");
    server = B173DedicatedServer.monsters(jar, workspace, port, seed, timeout, 3, true);
    actor = new B173WireClient("127.0.0.1", port, user, timeout);
    try {
      server.boot();
      actor.connect();
      actor.synchronizePose();
      require(
          actor.awaitInventory().occupiedSlots() >= 1, "creeper-gunpowder reload inventory drift");
      RemoteDroppedItem powder = null;
      int kills = 0;
      far(actor, spawner.x() + 0.5D, spawner.y(), spawner.z() + 0.5D);
      server.setTime(14000L);
      while (powder == null) {
        require(++kills <= 8, "official creeper gunpowder drop absent after bounded kills");
        RemoteMobSpawn creeper = near(actor, 50, spawner);
        require(creeper.legacyType() == 50 && creeper.entityId() != actor.state().entityId()
                && creeper.legacyType() != 90,
            "creeper Packet24 identity drift");
        kill(actor, creeper);
        far(actor, spawner.x() + 0.5D, spawner.y(), spawner.z() + 0.5D);
        powder = worldline.test.WorldlineSmokeAwait.awaitEntityOrNull(actor,
            ()
                -> actor.peekDroppedItem(GUNPOWDER),
            value -> value != null, "creeper gunpowder", 20);
      }
      require(powder.item().legacyId() == 289, "creeper Packet21 gunpowder id drift");
      require(worldline.test.WorldlineSmokeAwait.observe(actor, 2)
                  .blockAt(bench.x(), bench.y(), bench.z())
                  .equals(BENCH),
          "workbench 58 destroyed by creeper fuse");
      step(actor, bench.x() + 0.5D, bench.y() + 1.0D, spawner.z() - 6.0D);
      step(actor, bench.x() + 0.5D, bench.y() + 1.0D, bench.z() + 0.5D);
      int hand = empty(actor.inventory());
      require(hand >= 36, "empty hand absent");
      actor.selectHeldSlot(hand - 36);
      actor.openWorkbench(bench, BlockFace.UP);
      require(B173CreeperGunpowderCrafts.apply(actor) == 46 && has(actor.inventory(), 46)
              && !has(actor.inventory(), 12),
          "TNT 46 craft from 289+12 drifted");
      actor.closeWindow();
      require(worldline.test.WorldlineSmokeAwait.observe(actor, 2)
                  .blockAt(bench.x(), bench.y(), bench.z())
                  .equals(BENCH)
              && !placed(actor, bench),
          "TNT 46 was placed instead of crafted");
      actor.close();
      awaitPlayers(server, 0);
      server.save();
      String evidence = "column=" + column + ",platform=7x7-48grass,spawner=" + cell(spawner, 52)
          + ",workbench=" + cell(bench, 58)
          + ",entityid=Creeper,mob=type50,night=14000,sword=276,drop=packet21-289,craft=46-from-289+12,kills<=8,clients=2,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=raised-7x7-grass-platform+spawner52+workbench58+sand12x4+gunpowder289x5|cause=nbt-entityid-creeper+time-14000+diamond-sword-packet7+packet102-workbench-tnt46|wire=packet24-type50+packet38-status3+packet29+packet21-289+packet106-accepted+packet200-craft-stat|oracle=creeper-gunpowder-drop-and-tnt-craft-not-explode3-not-place46|"
          + evidence;
      System.out.println("WORLDLINE_M421_SET=" + evidence);
      System.out.println("WORLDLINE_M421_TRACE=" + trace);
      System.out.println("WORLDLINE_M421_SIGNATURE=" + sha(trace));
    } finally {
      actor.close();
      server.close();
    }
  }
  private static RemoteMobSpawn near(B173WireClient a, int type, BlockPosition p) {
    for (int n = 0; n < 32; n++) {
      RemoteMobSpawn s = a.awaitMobSpawn(type);
      double dx = s.x() - (p.x() + 0.5D), dz = s.z() - (p.z() + 0.5D);
      if (dx * dx + dz * dz <= 100D && Math.abs(s.y() - p.y()) <= 6D)
        return s;
    }
    throw new IllegalStateException("nearby creeper type " + type + " absent");
  }
  private static void kill(B173WireClient a, RemoteMobSpawn spawn) {
    int entity = spawn.entityId();
    double x = spawn.x(), y = spawn.y(), z = spawn.z();
    far(a, x, y, z);
    for (int hit = 0; hit < 16; hit++) {
      if (B173ShearsAccess.peekDeath(a, entity) != null)
        break;
      reach(a, x, y, z);
      strike(a, entity);
      far(a, x, y, z);
      worldline.test.WorldlineSmokeAwait.observe(a, 20);
      heal(a);
      if (B173ShearsAccess.peekDeath(a, entity) != null)
        break;
      RemoteMobMovement m = a.awaitMobMovement(entity);
      x = m.toX();
      y = m.toY();
      z = m.toZ();
    }
    RemoteMobDeath death = a.awaitMobDeath(entity);
    require(death.entityId() == entity && death.hurtObserved() && a.health() > 0,
        "creeper death drift");
  }
  private static void far(B173WireClient a, double x, double y, double z) {
    step(a, x, y, z - 8.5D);
  }
  private static void reach(B173WireClient a, double x, double y, double z) {
    step(a, x, y, z - 5.2D);
  }
  private static void step(B173WireClient a, double x, double y, double z) {
    for (int n = 0; n < 16; n++) {
      heal(a);
      PlayerPose here = a.moveAndObserve(0D, 0D, 0D, 1).resulting();
      double dx = x - here.x(), dy = y - here.y(), dz = z - here.z(),
             dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
      if (dist <= 1.5D)
        return;
      double s = Math.min(1D, 9.0D / dist);
      a.moveAndObserve(dx * s, dy * s, dz * s, 2);
    }
  }
  private static void strike(B173WireClient a, int entity) {
    heal(a);
    int sword = find(a.inventory(), 276);
    require(sword >= 36, "diamond sword lost");
    a.selectHeldSlot(sword - 36);
    a.attackMob(entity);
  }
  private static void heal(B173WireClient a) {
    int h = a.health();
    if (h == 0)
      throw new IllegalStateException("actor died during creeper gunpowder");
    if (h >= 20)
      return;
    int food = find(a.inventory(), 320);
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
  private static int empty(RemoteInventoryView view) {
    for (int slot = 36; slot <= 44; slot++)
      if (view.slot(slot).empty())
        return slot;
    return -1;
  }
  private static boolean has(RemoteInventoryView view, int id) {
    for (int s = 9; s <= 44; s++)
      if (!view.slot(s).empty() && view.slot(s).item().legacyId() == id)
        return true;
    return false;
  }
  private static boolean placed(B173WireClient a, BlockPosition t) {
    RemoteWorldView v = worldline.test.WorldlineSmokeAwait.observe(a, 1);
    for (int dx = -8; dx <= 8; dx++)
      for (int dz = -8; dz <= 8; dz++)
        for (int dy = 0; dy <= 2; dy++)
          if (v.blockAt(t.x() + dx, t.y() + dy, t.z() + dz).legacyId() == 46)
            return true;
    return false;
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
    throw new IllegalStateException("no deterministic creeper-gunpowder foundation");
  }
  private static String cell(BlockPosition p, int id) {
    return p.x() + ":" + p.y() + ":" + p.z() + ":" + id + ":0";
  }
  private static void require(boolean v, String m) {
    if (!v)
      throw new IllegalStateException(m);
  }
}
