package worldline.smoke.undeadsunburnsetb173;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import worldline.api.BlockFace;
import worldline.api.BlockPosition;
import worldline.api.RemoteChunkSnapshot;
import worldline.api.RemoteMobSpawn;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173PlayerSeed;
import worldline.b173server.B173SpawnerDelay;
import worldline.b173server.B173SpawnerSeed;
import worldline.b173server.B173UndeadSunBurn;
import worldline.b173server.B173WireClient;
import worldline.test.WorldlineSmokeAwait;

/** Official zombie burns in open daylight and stays unburned at night and under a roof. */
public final class UndeadSunBurnSetSmoke {
  private UndeadSunBurnSetSmoke() {}

  public static void main(String[] args) throws Exception {
    if (args.length != 7) {
      throw new IllegalArgumentException(
          "usage: UndeadSunBurnSetSmoke server.jar workspace port seed username chunkX chunkZ");
    }
    Path jar = Paths.get(args[0]);
    Path workspace = Paths.get(args[1]);
    int port = Integer.parseInt(args[2]);
    long seed = Long.parseLong(args[3]);
    String user = args[4];
    int chunkX = Integer.parseInt(args[5]);
    int chunkZ = Integer.parseInt(args[6]);
    UndeadSunBurnSetSupport.require(
        seed == 17320110707L && user.equals("SunBurn619") && user.length() <= 16, "undead-sun-burn identity drift");
    int[] openColumn = new int[1];
    int[] coverColumn = new int[1];
    BlockPosition open = arm(jar, workspace.resolve("open"), port, seed, user, chunkX, chunkZ, false, openColumn);
    BlockPosition cover = arm(jar, workspace.resolve("cover"), port + 1, seed, user, chunkX, chunkZ, true, coverColumn);
    String evidence = "column=" + openColumn[0] + ",platform=7x7-48grass,open=" + UndeadSunBurnSetSupport.cell(open)
        + ",cover=" + UndeadSunBurnSetSupport.cell(cover)
        + ",entityid=Zombie,night=14000,day=6000,night-fire=0,day-open=type54-flags1,"
        + "day-cover=type54-flags0,clients=4,disconnect=clean";
    String trace = "v1|server=official-b1.7.3|seed=" + seed
        + "|fixture=raised-7x7-grass-platform+spawner52+5x5-center-roof"
        + "|cause=nbt-entityid-zombie+nbt-delay-1+time-14000-then-6000"
        + "|wire=packet40-flags0-night+packet40-flags1-day-open+packet40-flags0-day-cover"
        + "|oracle=undead-sun-burn-not-m435-natural|" + evidence;
    System.out.println("WORLDLINE_M619_SET=" + evidence);
    System.out.println("WORLDLINE_M619_TRACE=" + trace);
    System.out.println("WORLDLINE_M619_SIGNATURE=" + UndeadSunBurnSetSupport.sha(trace));
  }

  private static BlockPosition arm(Path jar, Path workspace, int port, long seed, String user, int chunkX, int chunkZ,
      boolean roofed, int[] column) throws Exception {
    Duration timeout = Duration.ofSeconds(180);
    BlockPosition spawner;
    B173DedicatedServer server = B173DedicatedServer.monsters(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout);
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1, 2}, new int[] {1, 2, 52},
          new int[] {64, 48, 1}, new int[] {0, 0, 0});
      actor.connect();
      actor.synchronizePose();
      UndeadSunBurnSetSupport.require(actor.awaitInventory().occupiedSlots() == 3, "undead-sun-burn inventory drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(chunkX, chunkZ).chunkAt(chunkX, chunkZ);
      BlockPosition top = UndeadSunBurnSetSupport.raise(actor, initial, chunkX, chunkZ, column);
      UndeadSunBurnSetSupport.grassPlatform(actor, top);
      actor.selectHeldSlot(2);
      spawner = UndeadSunBurnSetSupport.place(actor, top, BlockFace.UP, 52);
      if (roofed)
        UndeadSunBurnSetSupport.centerRoof(actor, top);
      WorldlineSmokeAwait.observe(actor, 5);
      actor.close();
      UndeadSunBurnSetSupport.awaitPlayers(server, 0);
      server.save();
    } finally {
      actor.close();
      server.close();
    }
    Thread.sleep(1000L);
    B173SpawnerSeed.entity(workspace, spawner, "Zombie");
    B173SpawnerDelay.delay(workspace, spawner, (short) 1);
    B173PlayerSeed.writeInventory(workspace, user, spawner.x() + 0.5D, spawner.y() + 1.1D, spawner.z() + 0.5D,
        new int[] {0, 1, 2}, new int[] {1, 2, 52}, new int[] {64, 48, 1}, new int[] {0, 0, 0});
    server = B173DedicatedServer.monsters(jar, workspace, port, seed, timeout, 3, true);
    actor = new B173WireClient("127.0.0.1", port, user, timeout);
    try {
      observe(server, actor, spawner, roofed);
    } finally {
      actor.close();
      server.close();
    }
    return spawner;
  }

  private static void observe(B173DedicatedServer server, B173WireClient actor, BlockPosition spawner, boolean roofed)
      throws Exception {
    server.boot();
    actor.connect();
    actor.synchronizePose();
    UndeadSunBurnSetSupport.require(
        actor.awaitInventory().occupiedSlots() >= 1, "undead-sun-burn reload inventory drift");
    server.setTime(14000L);
    RemoteMobSpawn spawn = UndeadSunBurnSetSupport.awaitPad(actor, spawner, roofed ? 2.1D : 3.5D);
    UndeadSunBurnSetSupport.require(spawn.legacyType() == 54 && spawn.entityId() != actor.state().entityId()
            && spawn.legacyType() != 90 && (spawn.flags() & 1) == 0,
        "undead Packet24 identity drift");
    WorldlineSmokeAwait.observe(actor, 10);
    UndeadSunBurnSetSupport.require(!B173UndeadSunBurn.onFire(actor, spawn), "undead Packet40 fire present at night");
    server.setTime(6000L);
    if (roofed) {
      WorldlineSmokeAwait.observe(actor, 80);
      UndeadSunBurnSetSupport.require(
          !B173UndeadSunBurn.onFire(actor, spawn), "covered daylight Packet40 fire flag present");
    } else {
      B173UndeadSunBurn.awaitFire(actor, spawn);
      UndeadSunBurnSetSupport.require(
          B173UndeadSunBurn.onFire(actor, spawn), "open daylight Packet40 fire flag absent");
    }
    actor.close();
    UndeadSunBurnSetSupport.awaitPlayers(server, 0);
    server.save();
  }
}
