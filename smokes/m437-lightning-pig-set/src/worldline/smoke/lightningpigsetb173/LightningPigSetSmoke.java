package worldline.smoke.lightningpigsetb173;
import static worldline.b173server.B173FixtureSupport.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Overworld Packet24 type 90 pig plus Nether type 57 pigman contrast. No Packet23 lightning tracker. */
public final class LightningPigSetSmoke {
  private LightningPigSetSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 7)
      throw new IllegalArgumentException(
          "usage: LightningPigSetSmoke server.jar workspace port seed username chunkX chunkZ");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    int cx = Integer.parseInt(a[5]), cz = Integer.parseInt(a[6]);
    Duration timeout = Duration.ofSeconds(180);
    require(seed == 17320110707L && user.equals("LightPig437") && user.length() <= 16,
        "lightning-pig-set identity drift");
    B173DedicatedServer server =
        B173LightningPigAccess.overworld(jar, workspace, port, seed, timeout);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout);
    BlockPosition top, pigSpawner = null, netherTop, pigmanSpawner = null;
    int column = 0;
    RemoteMobSpawn pig, pigman;
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1, 2},
          new int[] {1, 2, 52}, new int[] {32, 48, 1}, new int[] {0, 0, 0});
      actor.connect();
      actor.synchronizePose();
      require(actor.dimension() == 0 && actor.awaitInventory().occupiedSlots() == 3,
          "overworld pig inventory or dimension drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      top = B173LightningPigAccess.overworldFoundation(initial, cx, cz);
      actor.selectHeldSlot(0);
      while (B173LightningPigAccess.water(initial
              .blockAt(B173LightningPigAccess.local(top.x(), cx), top.y() + 1,
                  B173LightningPigAccess.local(top.z(), cz))
              .legacyId())) {
        top = B173LightningPigAccess.place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        require(++column <= 15, "water column exceeded lightning-pig fixture");
      }
      for (int lift = 0; lift < 8; lift++) {
        top = B173LightningPigAccess.place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        column++;
      }
      actor.selectHeldSlot(1);
      B173LightningPigAccess.grassPlatform(actor, top);
      actor.selectHeldSlot(2);
      pigSpawner = B173LightningPigAccess.place(actor, top, BlockFace.UP, 52);
      pig = actor.awaitMobSpawn(90);
      require(pig.legacyType() == 90 && pig.entityId() != actor.state().entityId()
              && pig.legacyType() != 57,
          "pig Packet24 type 90 identity drift");
      actor.close();
      B173LightningPigAccess.awaitPlayers(server, 0);
      server.save();
    } finally {
      actor.close();
      server.close();
    }
    require(pigSpawner != null, "overworld pig spawner absent");
    Thread.sleep(1000L);
    server = B173LightningPigAccess.nether(jar, workspace, port, seed, timeout);
    B173WireClient scout = new B173WireClient("127.0.0.1", port, user, timeout);
    actor = new B173WireClient("127.0.0.1", port, user, timeout);
    try {
      server.boot();
      B173PlayerSeed.writeDimension(workspace, user, 4.5D, 64D, 4.5D, -1);
      scout.connect();
      scout.synchronizePose();
      require(scout.dimension() == -1 && scout.awaitDimension(-1) == -1,
          "nether scout dimension drift");
      RemoteChunkSnapshot nether = scout.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      require(
          B173LightningPigAccess.count(nether, 87) > 0 && B173LightningPigAccess.sky(nether) == 0,
          "nether terrain identity drift");
      netherTop = B173LightningPigAccess.netherFoundation(nether, cx, cz);
      scout.close();
      B173LightningPigAccess.awaitPlayers(server, 0);
      B173PlayerSeed.writeInventory(workspace, user, netherTop.x() + 0.5D, netherTop.y() + 1.0D,
          netherTop.z() + 0.5D, -1, new int[] {0, 1}, new int[] {87, 52}, new int[] {32, 1},
          new int[] {0, 0});
      actor.connect();
      require(actor.synchronizePose() != null && actor.dimension() == -1
              && actor.awaitInventory().occupiedSlots() == 2,
          "nether pigman inventory or dimension drift");
      actor.awaitRemoteChunk(cx, cz);
      actor.look(0F, 0F);
      actor.selectHeldSlot(0);
      B173LightningPigAccess.pad(actor, netherTop);
      PlayerPose pose = actor.moveAndObserve(0D, 0D, -1.5D, 4).resulting();
      require(Math.abs(pose.x() - (netherTop.x() + 0.5D)) < 3D
              && Math.abs(pose.y() - (netherTop.y() + 1.0D)) < 4D,
          "actor missed nether pigman fixture");
      actor.selectHeldSlot(1);
      pigmanSpawner = B173LightningPigAccess.place(actor, netherTop, BlockFace.UP, 52);
      worldline.test.WorldlineSmokeAwait.observe(actor, 5);
      actor.close();
      B173LightningPigAccess.awaitPlayers(server, 0);
      server.save();
    } finally {
      scout.close();
      actor.close();
      server.close();
    }
    require(pigmanSpawner != null, "nether pigman spawner absent");
    Thread.sleep(1000L);
    B173LightningPigAccess.retargetPigman(workspace, pigmanSpawner);
    server = B173LightningPigAccess.nether(jar, workspace, port, seed, timeout);
    actor = new B173WireClient("127.0.0.1", port, user, timeout);
    try {
      server.boot();
      actor.connect();
      actor.synchronizePose();
      require(actor.dimension() == -1 && actor.awaitInventory().occupiedSlots() >= 1,
          "nether pigman reload drift");
      pigman = B173LightningPigAccess.near(actor, 57, pigmanSpawner);
      require(pigman.legacyType() == 57 && pigman.entityId() != actor.state().entityId()
              && pigman.legacyType() != 90 && pigman.legacyType() != 54,
          "pigman Packet24 type 57 identity drift");
      actor.close();
      B173LightningPigAccess.awaitPlayers(server, 0);
      server.save();
      String evidence =
          "dimension=0+-1,overworld-spawner=" + B173LightningPigAccess.cell(pigSpawner)
          + ",nether-spawner=" + B173LightningPigAccess.cell(pigmanSpawner)
          + ",entityid=Pig+PigZombie,mobs=type90+type57,column=" + column
          + ",platform=7x7-48grass,packet23=absent,packet71=unclaimed,not-m411-pork,clients=4,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|profile=overworld-animals+allow-nether-true+spawn-monsters-true|entry=overworld-item1+item2+item52+prelogin-player-nbt-dimension-minus-one+item87+item52|fixture=raised-7x7-grass-platform+spawner52+nether-netherrack87+spawner52|cause=official-spawner-pig+nbt-entityid-pigzombie|wire=packet24-type90+packet24-type57|oracle=overworld-pig90-nether-pigman57-contrast-not-weather-lightning-not-packet23-not-m411-pork|"
          + evidence;
      System.out.println("WORLDLINE_M437_SET=" + evidence);
      System.out.println("WORLDLINE_M437_TRACE=" + trace);
      System.out.println("WORLDLINE_M437_SIGNATURE=" + sha(trace));
    } finally {
      actor.close();
      server.close();
    }
  }
  private static void require(boolean v, String m) {
    if (!v)
      throw new IllegalStateException(m);
  }
}
