package worldline.smoke.ghastfireballpunchsetb173;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import worldline.api.BlockFace;
import worldline.api.BlockPosition;
import worldline.api.RemoteChunkSnapshot;
import worldline.api.RemoteMobSpawn;
import worldline.api.RemoteWorldView;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173EntityVelocity;
import worldline.b173server.B173GhastFireballPunch;
import worldline.b173server.B173PlayerSeed;
import worldline.b173server.B173SpawnerSeed;
import worldline.b173server.B173WireClient;
import worldline.test.WorldlineSmokeAwait;

/** Nether ghast Packet24 56 plus Packet23 type 63, then Packet7 punch Packet28 look-up redirect. */
public final class GhastFireballPunchSetSmoke {
  private GhastFireballPunchSetSmoke() {}

  public static void main(String[] args) throws Exception {
    if (args.length != 7)
      throw new IllegalArgumentException(
          "usage: GhastFireballPunchSetSmoke server.jar workspace port seed username chunkX chunkZ");
    Path jar = Paths.get(args[0]);
    Path workspace = Paths.get(args[1]);
    int port = Integer.parseInt(args[2]);
    long seed = Long.parseLong(args[3]);
    String user = args[4];
    int cx = Integer.parseInt(args[5]);
    int cz = Integer.parseInt(args[6]);
    Duration timeout = Duration.ofSeconds(180);
    GhastFireballPunchWorld.require(
        user.length() <= 16 && user.equals("GhastPunch590") && seed == 17320110707L, "username or seed drift");
    B173DedicatedServer server = B173DedicatedServer.netherMonsters(jar, workspace, port, seed, timeout);
    B173WireClient scout = new B173WireClient("127.0.0.1", port, user, timeout);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout);
    BlockPosition land;
    BlockPosition top;
    BlockPosition spawner;
    int pads;
    int cobbles;
    int clients = 2;
    try {
      server.boot();
      B173PlayerSeed.writeDimension(workspace, user, 4.5D, 64D, 4.5D, -1);
      scout.connect();
      scout.synchronizePose();
      GhastFireballPunchWorld.require(
          scout.dimension() == -1 && scout.awaitDimension(-1) == -1, "nether scout dimension drift");
      RemoteChunkSnapshot initial = scout.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      GhastFireballPunchWorld.require(
          GhastFireballPunchWorld.count(initial, 87) > 0 && GhastFireballPunchWorld.sky(initial) == 0,
          "nether terrain identity drift");
      land = GhastFireballPunchWorld.landing(initial, cx, cz);
      scout.close();
      GhastFireballPunchWorld.awaitPlayers(server, 0);
      GhastFireballPunchWorld.inventory(workspace, user, land);
      actor.connect();
      GhastFireballPunchWorld.require(
          actor.synchronizePose() != null && actor.dimension() == -1 && actor.awaitInventory().occupiedSlots() == 4,
          "nether ghast-punch inventory or dimension drift");
      int cavernCx = cx + 2;
      int cavernCz = cz - 1;
      top = GhastFireballPunchWorld.cavern(
          GhastFireballPunchWorld.load(actor, land, cavernCx, cavernCz), cavernCx, cavernCz);
      if (top.x() != land.x() || top.y() != land.y() || top.z() != land.z()) {
        actor.close();
        GhastFireballPunchWorld.awaitPlayers(server, 0);
        GhastFireballPunchWorld.inventory(workspace, user, top);
        actor = new B173WireClient("127.0.0.1", port, user, timeout);
        actor.connect();
        GhastFireballPunchWorld.require(
            actor.synchronizePose() != null && actor.dimension() == -1 && actor.awaitInventory().occupiedSlots() == 4,
            "nether cavern relog drift");
        clients++;
      }
      RemoteWorldView live = actor.awaitRemoteChunk(Math.floorDiv(top.x(), 16), Math.floorDiv(top.z(), 16));
      int[] placed = GhastFireballPunchWorld.pad(actor, live, top);
      pads = placed[0];
      cobbles = placed[1];
      live = actor.sustainTicks(1);
      actor.look(0F, 60F);
      actor.selectHeldSlot(2);
      spawner = GhastFireballPunchWorld.place(actor, GhastFireballPunchWorld.site(live, top), BlockFace.UP, 52);
      actor.close();
      GhastFireballPunchWorld.awaitPlayers(server, 0);
      server.save();
    } finally {
      scout.close();
      actor.close();
      server.close();
    }
    Thread.sleep(1000L);
    B173SpawnerSeed.ghast(workspace, spawner);
    server = B173DedicatedServer.netherMonsters(jar, workspace, port, seed, timeout);
    actor = new B173WireClient("127.0.0.1", port, user, timeout);
    try {
      server.boot();
      actor.connect();
      GhastFireballPunchWorld.require(
          actor.synchronizePose() != null && actor.dimension() == -1, "nether ghast-punch reload dimension drift");
      actor.awaitRemoteChunk(Math.floorDiv(top.x(), 16), Math.floorDiv(top.z(), 16));
      GhastFireballPunchWorld.require(actor.awaitHealth(20) == 20, "pre-punch Packet8 drift");
      actor.look(0F, 0F);
      actor.moveAndObserve(0D, 0D, 0D, 2);
      WorldlineSmokeAwait.observe(actor, 2);
      RemoteMobSpawn ghast = actor.awaitMobSpawn(56);
      GhastFireballPunchWorld.require(
          ghast.legacyType() == 56 && ghast.entityId() != actor.state().entityId(), "ghast Packet24 identity drift");
      actor.selectHeldSlot(3);
      B173EntityVelocity punch = B173GhastFireballPunch.awaitRedirect(actor, ghast);
      GhastFireballPunchWorld.require(
          punch != null && B173GhastFireballPunch.redirectedUp(punch), "ghast fireball punch Packet28 redirect drift");
      actor.close();
      GhastFireballPunchWorld.awaitPlayers(server, 0);
      server.save();
      String evidence = "dimension=-1,support=" + top.x() + ":" + top.y() + ":" + top.z() + ":87,pads=" + pads
          + ",cobble-pads=" + cobbles + ",spawner=" + spawner.x() + ":" + spawner.y() + ":" + spawner.z()
          + ":52:0,entityid=Ghast,ghast=type56,fireball=type63,thrower=ghast,"
          + "punch=packet28-look-up,redirect=up,not-m410-spawn-only,not-m459-hit,"
          + "packet23-known=absent,clients=" + clients + ",disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed + "|profile=allow-nether-true+spawn-monsters-true"
          + "|entry=prelogin-player-nbt-dimension-minus-one+item87+item4+item52+item268"
          + "|fixture=nether-netherrack87-platform+cobble4-pad+spawner52-ghast"
          + "|cause=nbt-entityid-ghast+ghast-los+packet7-fireball-punch"
          + "|wire=packet24-type56+packet23-type63-thrower-ghast+packet28-look-up"
          + "|oracle=nether-ghast-fireball-punch-not-m410-spawn-only-not-m459-hit|" + evidence;
      System.out.println("WORLDLINE_M590_PUNCH=" + evidence);
      System.out.println("WORLDLINE_M590_TRACE=" + trace);
      System.out.println("WORLDLINE_M590_SIGNATURE=" + GhastFireballPunchWorld.sha(trace));
    } finally {
      actor.close();
      server.close();
    }
  }
}
