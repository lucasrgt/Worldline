package worldline.smoke.netherrespawnb173;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Dies in the official Nether and respawns into the Overworld. */
public final class NetherDeathRespawnSmoke {
  private NetherDeathRespawnSmoke() {
  }
  public static void main(String[] args) throws Exception {
    if (args.length != 6)
      throw new IllegalArgumentException(
          "usage: NetherDeathRespawnSmoke server.jar workspace port seed username fixtureTicks");
    Path jar = Paths.get(args[0]), workspace = Paths.get(args[1]);
    int port = Integer.parseInt(args[2]);
    long seed = Long.parseLong(args[3]);
    String username = args[4];
    int fixtureTicks = Integer.parseInt(args[5]);
    Duration timeout = Duration.ofSeconds(90);
    B173DedicatedServer server =
        new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true, true);
    RespawnSession actor = new B173WireClient("127.0.0.1", port, username, timeout);
    RemoteRespawn respawn;
    RemoteChunkSnapshot nether, overworld;
    try {
      server.boot();
      B173PlayerSeed.writeDimension(workspace, username, 8.5D, -80D, 8.5D, -1);
      actor.connect();
      actor.synchronizePose();
      require(actor.dimension() == -1 && actor.awaitInventory().occupiedSlots() == 0
              && actor.awaitHealth(20) == 20,
          "Nether player baseline drift");
      nether = actor.awaitRemoteChunk(0, 0).chunkAt(0, 0);
      require(count(nether, 87) > 0 && sky(nether) == 0, "Nether source chunk drift");
      worldline.test.WorldlineSmokeAwait.observe(actor, fixtureTicks);
      require(actor.health() <= 0, "Nether void death absent: " + actor.health());
      respawn = actor.respawn();
      require(respawn.equals(new RemoteRespawn(-1, 0, 0, 20)) && actor.dimension() == 0,
          "cross-dimension respawn drift");
      worldline.test.WorldlineSmokeAwait.observe(actor, 1);
      PlayerPose pose = actor.moveAndObserve(0D, 0D, 0D, 1).resulting();
      RemoteWorldView world = worldline.test.WorldlineSmokeAwait.observe(actor, 20);
      overworld = world.chunkAt(floor(pose.x()) >> 4, floor(pose.z()) >> 4);
      require(sky(overworld) > 0 && actor.inventory().occupiedSlots() == 0,
          "Overworld respawn view drift");
      for (RemoteChunkSnapshot chunk : world.chunks())
        require(sky(chunk) > 0, "stale Nether chunk survived respawn");
      actor.close();
      awaitPlayers(server, 0);
      server.save();
      ServerPlayerState saved = server.player(username);
      require(saved.dimension() == 0 && saved.health() == 20 && saved.inventoryItems() == 0,
          "Nether respawn persistence drift");
    } finally {
      actor.close();
      server.close();
    }
    String evidence =
        "dimensions=-1->0,health=20->nonpositive->20,request=09:ff,nether=netherrack+sky0,overworld=sky-positive,cache=overworld-only,inventory=empty,persisted=0:20";
    String trace = "v1|server=official-b1.7.3|seed=" + seed
        + "|profile=allow-nether-true|entry=player-nbt-dimension-minus-one-y-minus-80|death=vanilla-void-damage|request=packet9-signed-minus-one|response=fresh-packet9-zero+packet8-health20|cache=dimension-change-reset+corrected-overworld-chunks|"
        + evidence + "|disconnect=clean";
    System.out.println("WORLDLINE_M136_RESPAWN=" + evidence);
    System.out.println("WORLDLINE_M136_TRACE=" + trace);
    System.out.println("WORLDLINE_M136_SIGNATURE=" + sha(trace));
  }
  private static int count(RemoteChunkSnapshot q, int id) {
    int n = 0;
    for (int x = 0; x < 16; x++)
      for (int z = 0; z < 16; z++)
        for (int y = 0; y < 128; y++)
          if (q.blockAt(x, y, z).legacyId() == id)
            n++;
    return n;
  }
  private static int sky(RemoteChunkSnapshot q) {
    int n = 0;
    for (int x = 0; x < 16; x++)
      for (int z = 0; z < 16; z++)
        for (int y = 0; y < 128; y++)
          if (q.skyLightAt(x, y, z) > 0)
            n++;
    return n;
  }
  private static int floor(double value) {
    return (int) Math.floor(value);
  }
  private static void awaitPlayers(B173DedicatedServer server, int count) throws Exception {
    long end = System.currentTimeMillis() + 5000;
    while (System.currentTimeMillis() < end) {
      if (server.players().size() == count)
        return;
      Thread.sleep(100);
    }
    throw new IllegalStateException("player count drift");
  }
  private static String sha(String value) throws Exception {
    byte[] bytes =
        MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
    StringBuilder result = new StringBuilder();
    for (byte item : bytes)
      result.append(String.format("%02x", item & 255));
    return result.toString();
  }
  private static void require(boolean value, String message) {
    if (!value)
      throw new IllegalStateException(message);
  }
}
