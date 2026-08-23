package worldline.smoke.explosionplayerhurtsetb173;
import static worldline.b173server.B173FixtureSupport.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** TNT Packet60 strength 4 and creeper Packet60 strength 3 each drop Packet8 health; the actor survives. */
public final class ExplosionPlayerHurtSetSmoke {
  private ExplosionPlayerHurtSetSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 7)
      throw new IllegalArgumentException(
          "usage: ExplosionPlayerHurtSetSmoke server.jar workspace port seed username chunkX chunkZ");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    int cx = Integer.parseInt(a[5]), cz = Integer.parseInt(a[6]);
    require(seed == 17320110707L && user.equals("ExplHurt464") && user.length() <= 16,
        "explosion-player-hurt-set identity drift");
    Duration timeout = Duration.ofSeconds(180);
    B173DedicatedServer server =
        B173DedicatedServer.monsters(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout);
    BlockPosition top, tnt, anchor, spawner;
    int column, tntHealth, creeperHealth;
    RemoteExplosion explosion;
    PlayerPose pose;
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1, 2, 3, 4, 5},
          new int[] {1, 46, 259, 322, 52, 49}, new int[] {64, 1, 1, 2, 1, 4},
          new int[] {0, 0, 0, 0, 0, 0});
      actor.connect();
      pose = actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() >= 6 && actor.awaitHealth(20) == 20,
          "explosion-player-hurt-set inventory or health drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      top = foundation(initial, cx, cz);
      column = 0;
      actor.selectHeldSlot(0);
      while (
          water(initial.blockAt(local(top.x(), cx), top.y() + 1, local(top.z(), cz)).legacyId())) {
        top = place(actor, top, BlockFace.UP, 1);
        pose = actor.moveAndObserve(0D, 1D, 0D, 1).resulting();
        require(++column <= 15, "water column exceeded explosion-player-hurt-set fixture");
      }
      for (int lift = 0; lift < 8; lift++) {
        top = place(actor, top, BlockFace.UP, 1);
        pose = actor.moveAndObserve(0D, 1D, 0D, 1).resulting();
        column++;
      }
      pad(actor, top);
      actor.selectHeldSlot(5);
      anchor = place(actor, new BlockPosition(top.x() - 2, top.y(), top.z()), BlockFace.WEST, 49);
      actor.selectHeldSlot(4);
      spawner = place(actor, new BlockPosition(top.x(), top.y(), top.z() - 1), BlockFace.UP, 52);
      actor.close();
      awaitPlayers(server, 0);
      server.save();
    } finally {
      actor.close();
      server.close();
    }
    B173SpawnerSeed.entity(workspace, spawner, "Creeper");
    B173PlayerSeed.writeInventory(workspace, user, top.x() + 3.2D, top.y() + 1.0D, top.z() + 0.5D,
        new int[] {0, 1, 2, 3, 4, 5}, new int[] {1, 46, 259, 322, 52, 49},
        new int[] {32, 1, 1, 2, 1, 4}, new int[] {0, 0, 0, 0, 0, 0});
    Thread.sleep(1000L);
    server = B173DedicatedServer.monsters(jar, workspace, port, seed, timeout, 3, true);
    actor = new B173WireClient("127.0.0.1", port, user, timeout);
    try {
      server.boot();
      actor.connect();
      pose = actor.synchronizePose();
      require(actor.awaitHealth(20) == 20, "creeper-phase health drift health=" + actor.health());
      server.setTime(14000L);
      pose = actor.moveAndObserve(0D, 0D, 0D, 2).resulting();
      RemoteMobSpawn creeper = actor.awaitMobSpawn(50);
      require(creeper.legacyType() == 50 && creeper.entityId() != actor.state().entityId(),
          "creeper Packet24 type50 identity drift");
      int before = actor.health();
      explosion = actor.awaitExplosion();
      require(
          explosion.strength() == 3F, "creeper Packet60 strength drift: " + explosion.strength());
      creeperHealth = probe(actor, before);
      if (creeperHealth >= before) {
        explosion = actor.awaitExplosion();
        require(explosion.strength() == 3F, "second creeper Packet60 strength drift");
        creeperHealth = hurt(actor, before, "creeper");
      } else
        require(creeperHealth > 0, "creeper Packet8 lethal");
      heal(actor);
      server.setTime(1000L);
      actor.awaitBlock(anchor, new BlockState(49, 0));
      pose = actor.moveAndObserve(0D, 0D, 0D, 1).resulting();
      double ax = (anchor.x() + 2.5D) - pose.x(), az = (anchor.z() + 0.5D) - pose.z();
      require(Math.abs(ax) <= 9D && Math.abs(az) <= 9D, "tnt-anchor move exceeded cap 9");
      pose = actor.moveAndObserve(ax, 0D, az, 4).resulting();
      actor.selectHeldSlot(1);
      tnt = place(actor, anchor, BlockFace.UP, 46);
      actor.selectHeldSlot(2);
      actor.useHeldItemOnBlock(tnt, BlockFace.UP);
      pose = actor.moveAndObserve(3D, 0D, 0D, 4).resulting();
      explosion = strength(actor, 4F);
      tntHealth = hurt(actor, 20, "tnt");
      require(tntHealth < 20 && tntHealth > 0 && creeperHealth > 0 && creeperHealth < 20,
          "explosion Packet8 survive drift");
      actor.close();
      awaitPlayers(server, 0);
      server.save();
      String evidence = "column=" + column + ",support=" + top.x() + ":" + top.y() + ":" + top.z()
          + ":1:0,tnt=46+flint259+packet60-strength4+packet8,creeper=type50+packet60-strength3+packet8,gapple=322,survived=true,clients=2,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=raised-stone-pad+obsidian-anchor+tnt46+flint259+gapple322+creeper-spawner52|cause=nbt-entityid-creeper+time-14000+proximity-fuse+packet15-ignite+stand-3|wire=packet24-type50+packet60-strength3+packet8+packet60-strength4+packet8|oracle=player-hurt-from-explosions-tnt4+creeper3-not-crater-not-bed5|"
          + evidence;
      System.out.println("WORLDLINE_M464_SET=" + evidence);
      System.out.println("WORLDLINE_M464_TRACE=" + trace);
      System.out.println("WORLDLINE_M464_SIGNATURE=" + sha(trace));
    } finally {
      actor.close();
      server.close();
    }
  }
  private static void pad(B173WireClient a, BlockPosition t) throws Exception {
    a.selectHeldSlot(0);
    for (int r = 1; r <= 2; r++) {
      for (int z = -r + 1; z < r; z++) {
        place(a, new BlockPosition(t.x() - r + 1, t.y(), t.z() + z), BlockFace.WEST, 1);
        place(a, new BlockPosition(t.x() + r - 1, t.y(), t.z() + z), BlockFace.EAST, 1);
      }
      for (int x = -r + 1; x < r; x++) {
        place(a, new BlockPosition(t.x() + x, t.y(), t.z() - r + 1), BlockFace.NORTH, 1);
        place(a, new BlockPosition(t.x() + x, t.y(), t.z() + r - 1), BlockFace.SOUTH, 1);
      }
      place(a, new BlockPosition(t.x() - r, t.y(), t.z() - r + 1), BlockFace.NORTH, 1);
      place(a, new BlockPosition(t.x() - r, t.y(), t.z() + r - 1), BlockFace.SOUTH, 1);
      place(a, new BlockPosition(t.x() + r, t.y(), t.z() - r + 1), BlockFace.NORTH, 1);
      place(a, new BlockPosition(t.x() + r, t.y(), t.z() + r - 1), BlockFace.SOUTH, 1);
    }
  }
  private static RemoteExplosion strength(B173WireClient a, float s) throws Exception {
    for (int n = 0; n < 8; n++) {
      RemoteExplosion e = a.awaitExplosion();
      if (e.strength() == s)
        return e;
    }
    throw new IllegalStateException("explosion strength " + s + " absent");
  }
  private static int probe(B173WireClient a, int before) throws Exception {
    return worldline.test.WorldlineSmokeAwait.awaitEntity(
        a, a::health, h -> h < before, "explosion health damage", 12);
  }
  private static int hurt(B173WireClient a, int before, String name) throws Exception {
    int after = probe(a, before);
    require(after < before && after > 0, name + " Packet8 health drift: " + before + "->" + after);
    return after;
  }
  private static void heal(B173WireClient a) throws Exception {
    if (a.health() == 20)
      return;
    a.selectHeldSlot(3);
    a.useSelectedItemInAir();
    worldline.test.WorldlineSmokeAwait.awaitEntity(
        a, a::health, h -> h >= 20, "golden apple heal", 20);
    require(a.health() == 20, "golden apple Packet8 heal drift health=" + a.health());
  }
  private static BlockPosition foundation(RemoteChunkSnapshot q, int cx, int cz) {
    for (int x = 4; x <= 11; x++)
      for (int z = 4; z <= 11; z++)
        for (int y = 126; y >= 1; y--)
          if (q.blockAt(x, y, z).legacyId() == 3 && water(q.blockAt(x, y + 1, z).legacyId()))
            return new BlockPosition(cx * 16 + x, y, cz * 16 + z);
    throw new IllegalStateException("no deterministic explosion-player-hurt-set foundation");
  }
  private static void require(boolean v, String m) {
    if (!v)
      throw new IllegalStateException(m);
  }
}
