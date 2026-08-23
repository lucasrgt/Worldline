package worldline.smoke.cactusdamageb173;
import static worldline.b173server.B173FixtureSupport.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Places official cactus on raised sand, then contacts it for Packet8 health drop. */
public final class CactusDamageSmoke {
  private CactusDamageSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 7)
      throw new IllegalArgumentException(
          "usage: CactusDamageSmoke server.jar workspace port seed username chunkX chunkZ");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    int cx = Integer.parseInt(a[5]), cz = Integer.parseInt(a[6]);
    Duration timeout = Duration.ofSeconds(90);
    B173DedicatedServer server =
        new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout), reader = null;
    BlockPosition top, sand, cactus;
    int column;
    PlayerPose pose;
    RemoteIncomingHit hit;
    ServerPlayerState saved;
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1, 2},
          new int[] {1, 12, 81}, new int[] {32, 1, 1}, new int[] {0, 0, 0});
      actor.connect();
      pose = actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() == 3 && actor.awaitHealth(20) == 20,
          "cactus-damage inventory or health drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      top = foundation(initial, cx, cz);
      column = 0;
      actor.selectHeldSlot(0);
      while (
          water(initial.blockAt(local(top.x(), cx), top.y() + 1, local(top.z(), cz)).legacyId())) {
        top = place(actor, top, BlockFace.UP, 1);
        pose = actor.moveAndObserve(0D, 1D, 0D, 1).resulting();
        require(++column <= 15, "water column exceeded cactus-damage fixture");
      }
      for (int lift = 0; lift < 8; lift++) {
        top = place(actor, top, BlockFace.UP, 1);
        pose = actor.moveAndObserve(0D, 1D, 0D, 1).resulting();
        column++;
      }
      actor.selectHeldSlot(1);
      sand = place(actor, top, BlockFace.UP, 12);
      actor.selectHeldSlot(2);
      cactus = place(actor, sand, BlockFace.UP, 81);
      require(actor.sustainTicks(5).blockAt(cactus.x(), cactus.y(), cactus.z()).legacyId() == 81
              && actor.health() == 20,
          "pre-contact cactus fixture drift");
      pose = actor
                 .moveAndObserve((cactus.x() + 0.5D) - pose.x(), cactus.y() - pose.y(),
                     (cactus.z() + 0.5D) - pose.z(), 6)
                 .resulting();
      hit = actor.awaitIncomingHit(19);
      require(hit.healthBefore() == 20 && hit.healthAfter() == 19 && hit.damage() == 1,
          "cactus Packet8/38 health drift");
      pose = actor.moveAndObserve(0D, 2D, 0D, 2).resulting();
      require(actor.health() == 19, "post-leave cactus health drift");
      actor.close();
      awaitPlayers(server, 0);
      server.save();
      saved = server.player(user);
      require(saved.health() == 19, "persisted cactus health drift");
      reader = new B173WireClient("127.0.0.1", port, user, timeout);
      reader.connect();
      reader.synchronizePose();
      RemoteChunkSnapshot after = reader.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      require(
          after.blockAt(local(cactus.x(), cx), cactus.y(), local(cactus.z(), cz)).legacyId() == 81
              && reader.awaitHealth(19) == 19,
          "persisted cactus-damage drift");
      String evidence = "column=" + column + ",sand=" + sand.x() + ":" + sand.y() + ":" + sand.z()
          + ":12:0,cactus=" + cactus.x() + ":" + cactus.y() + ":" + cactus.z()
          + ":81:0,health=20->19,damage=1,status=2,persisted=true,clients=2,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=raised-sand12+cactus81|cause=move-into-cactus-aabb|wire=packet38-status2-before-packet8-health20->19|oracle=cactus-collision-damage|"
          + evidence;
      System.out.println("WORLDLINE_M275_DAMAGE=" + evidence);
      System.out.println("WORLDLINE_M275_TRACE=" + trace);
      System.out.println("WORLDLINE_M275_SIGNATURE=" + sha(trace));
    } finally {
      actor.close();
      if (reader != null)
        reader.close();
      server.close();
    }
  }
  private static BlockPosition foundation(RemoteChunkSnapshot q, int cx, int cz) {
    for (int x = 4; x <= 11; x++)
      for (int z = 4; z <= 11; z++)
        for (int y = 126; y >= 1; y--)
          if (q.blockAt(x, y, z).legacyId() == 3 && water(q.blockAt(x, y + 1, z).legacyId()))
            return new BlockPosition(cx * 16 + x, y, cz * 16 + z);
    throw new IllegalStateException("no deterministic cactus-damage foundation");
  }
  private static void require(boolean v, String m) {
    if (!v)
      throw new IllegalStateException(m);
  }
}
