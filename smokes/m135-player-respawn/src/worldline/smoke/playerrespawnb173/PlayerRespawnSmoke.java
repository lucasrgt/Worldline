package worldline.smoke.playerrespawnb173;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Executes vanilla void death and a same-dimension Packet9 respawn. */
public final class PlayerRespawnSmoke {
  private PlayerRespawnSmoke() {
  }
  public static void main(String[] args) throws Exception {
    if (args.length != 6)
      throw new IllegalArgumentException(
          "usage: PlayerRespawnSmoke server.jar workspace port seed username fixtureTicks");
    Path jar = Paths.get(args[0]), workspace = Paths.get(args[1]);
    int port = Integer.parseInt(args[2]);
    long seed = Long.parseLong(args[3]);
    String username = args[4];
    int fixtureTicks = Integer.parseInt(args[5]);
    Duration timeout = Duration.ofSeconds(90);
    B173DedicatedServer server =
        new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    RespawnSession actor = new B173WireClient("127.0.0.1", port, username, timeout);
    RemoteRespawn respawn;
    PlayerPose afterPose;
    RemoteChunkSnapshot chunk;
    try {
      server.boot();
      B173PlayerSeed.write(workspace, username, 8.5D, -80D, 8.5D);
      actor.connect();
      actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() == 0 && actor.awaitHealth(20) == 20,
          "player baseline drift");
      worldline.test.WorldlineSmokeAwait.observe(actor, fixtureTicks);
      require(actor.health() <= 0, "vanilla void death health absent: " + actor.health());
      respawn = actor.respawn();
      require(respawn.equals(new RemoteRespawn(0, 0, 20)) && actor.dimension() == 0
              && actor.health() == 20,
          "respawn result drift");
      worldline.test.WorldlineSmokeAwait.observe(actor, 1);
      afterPose = actor.moveAndObserve(0D, 0D, 0D, 1).resulting();
      RemoteWorldView world = worldline.test.WorldlineSmokeAwait.observe(actor, 20);
      chunk = world.chunkAt(floor(afterPose.x()) >> 4, floor(afterPose.z()) >> 4);
      require(sky(chunk) > 0 && actor.inventory().occupiedSlots() == 0,
          "respawn world/inventory drift");
      actor.close();
      awaitPlayers(server, 0);
      server.save();
      ServerPlayerState saved = server.player(username);
      require(saved.dimension() == 0 && saved.health() == 20 && saved.inventoryItems() == 0,
          "respawn persistence drift");
    } finally {
      actor.close();
      server.close();
    }
    String evidence =
        "health=20->nonpositive->20,dimension=0,packet9=09:00,chunk=loaded,sky=positive,inventory=empty,persisted=20";
    String trace = "v1|server=official-b1.7.3|seed=" + seed
        + "|death=seeded-below-world+vanilla-void-damage+packet8-nonpositive|request=packet9-signed-dimension-zero|response=fresh-same-dimension-packet9-epoch+packet8-health20|world=corrected-pose+packet51-overworld|"
        + evidence + "|disconnect=clean";
    System.out.println("WORLDLINE_M135_RESPAWN=" + evidence);
    System.out.println("WORLDLINE_M135_TRACE=" + trace);
    System.out.println("WORLDLINE_M135_SIGNATURE=" + sha(trace));
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
