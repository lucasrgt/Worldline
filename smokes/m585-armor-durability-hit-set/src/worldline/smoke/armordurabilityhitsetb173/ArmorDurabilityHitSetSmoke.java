package worldline.smoke.armordurabilityhitsetb173;
import static worldline.b173server.B173FixtureSupport.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.RemoteIncomingHit;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteItemStack;
import worldline.api.RemoteMobSpawn;
import worldline.b173server.B173ArmorDurabilityAccess;
import worldline.b173server.B173ArmorDurabilityAccess.Pad;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173PlayerSeed;
import worldline.b173server.B173SpawnerSeed;
import worldline.b173server.B173WireClient;
import worldline.test.WorldlineSmokeAwait;

/** Worn iron chestplate 307 in slot 6 takes Packet103 damage after one zombie melee. */
public final class ArmorDurabilityHitSetSmoke {
  private ArmorDurabilityHitSetSmoke() {}

  public static void main(String[] a) throws Exception {
    if (a.length != 7) {
      throw new IllegalArgumentException(
          "usage: ArmorDurabilityHitSetSmoke server.jar workspace port seed username chunkX chunkZ");
    }
    Path jar = Paths.get(a[0]);
    Path workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    int cx = Integer.parseInt(a[5]);
    int cz = Integer.parseInt(a[6]);
    require(seed == 17320110707L && user.equals("ArmorDur585") && user.length() <= 16, "actor identity drift");
    Duration timeout = Duration.ofSeconds(300);
    Pad pad = build(jar, workspace, port, seed, user, timeout, cx, cz);
    B173SpawnerSeed.entity(workspace, pad.spawner, "Zombie");
    Thread.sleep(1000L);
    B173PlayerSeed.writeInventory(workspace, user, pad.top.x() + 0.5D, pad.top.y() + 1.0D, pad.top.z() + 0.5D,
        new int[] {0, 1, 102}, new int[] {322, 320, 307}, new int[] {8, 8, 1}, new int[] {0, 0, 0});
    B173DedicatedServer server = B173DedicatedServer.difficulty(jar, workspace, port, seed, timeout, 2);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout);
    try {
      server.boot();
      actor.connect();
      actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() == 3 && actor.awaitHealth(20) == 20,
          "armor-durability reload inventory or health drift");
      RemoteItemStack before = B173ArmorDurabilityAccess.worn(actor);
      require(before.legacyId() == 307 && before.damage() == 0 && before.count() == 1,
          "iron chestplate was not undamaged in slot 6");
      server.setTime(14000L);
      B173ArmorDurabilityAccess.go(actor, pad.spawner);
      RemoteMobSpawn zombie = B173ArmorDurabilityAccess.near(actor, pad.spawner);
      require(zombie.legacyType() == 54 && zombie.legacyType() != 90, "zombie Packet24 identity drift");
      double[] at = {zombie.x(), zombie.y(), zombie.z()};
      RemoteIncomingHit hit = B173ArmorDurabilityAccess.absorb(actor, zombie.entityId(), at);
      RemoteInventoryView worn = WorldlineSmokeAwait.awaitEntity(
          actor, actor::inventory, view -> damaged(view, before.damage()), "iron chestplate durability", 40);
      RemoteItemStack after = worn.slot(B173ArmorDurabilityAccess.SLOT).item();
      require(
          after.legacyId() == 307 && after.damage() > before.damage(), "worn iron chestplate did not lose durability");
      actor.close();
      awaitPlayers(server, 0);
      server.save();
      String evidence = "column=" + pad.column
          + ",platform=7x7-48grass,spawner=" + B173ArmorDurabilityAccess.cell(pad.spawner)
          + ",entityid=Zombie,mob=type54,night=14000,armor=307,slot=6,before=" + before.damage()
          + ",after=" + after.damage() + ",hit=20->" + hit.healthAfter() + ":" + hit.damage()
          + ",food=322+320,wire=packet24-type54+packet8+packet103,not-m451-reduction,"
          + "not-craft,not-equip-only,not-m66-pvp,clients=2,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=raised-7x7-grass-platform+spawner52+nbt-slot102-iron-chestplate307"
          + "|cause=nbt-entityid-zombie+time-14000+worn-iron-chestplate"
          + "|wire=packet24-type54+packet38-status2+packet8-health+packet103-slot6"
          + "|oracle=zombie-melee-armor-durability-hit-not-reduction-not-pvp-not-craft-not-equip-only|" + evidence;
      System.out.println("WORLDLINE_M585_HIT=" + evidence);
      System.out.println("WORLDLINE_M585_TRACE=" + trace);
      System.out.println("WORLDLINE_M585_SIGNATURE=" + sha(trace));
    } finally {
      actor.close();
      server.close();
    }
  }

  private static Pad build(Path jar, Path workspace, int port, long seed, String user, Duration timeout, int cx, int cz)
      throws Exception {
    B173DedicatedServer server = new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout);
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1, 2}, new int[] {1, 2, 52},
          new int[] {64, 48, 1}, new int[] {0, 0, 0});
      actor.connect();
      actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() == 3 && actor.awaitHealth(20) == 20,
          "armor-durability inventory or health drift");
      Pad pad = B173ArmorDurabilityAccess.raise(actor, cx, cz);
      actor.close();
      awaitPlayers(server, 0);
      server.save();
      return pad;
    } finally {
      actor.close();
      server.close();
    }
  }

  private static boolean damaged(RemoteInventoryView view, int before) {
    if (view == null || view.size() <= B173ArmorDurabilityAccess.SLOT) {
      return false;
    }
    if (view.slot(B173ArmorDurabilityAccess.SLOT).empty()) {
      return false;
    }
    RemoteItemStack item = view.slot(B173ArmorDurabilityAccess.SLOT).item();
    return item.legacyId() == 307 && item.damage() > before;
  }

  private static void require(boolean v, String m) {
    if (!v) {
      throw new IllegalStateException(m);
    }
  }
}
