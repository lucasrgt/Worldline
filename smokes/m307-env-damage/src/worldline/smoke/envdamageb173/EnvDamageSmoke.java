package worldline.smoke.envdamageb173;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Drowns, suffocates, then stands in lava so Packet38/8 records three environmental hurts. */
public final class EnvDamageSmoke {
  private EnvDamageSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 7)
      throw new IllegalArgumentException(
          "usage: EnvDamageSmoke server.jar workspace port seed username chunkX chunkZ");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    int cx = Integer.parseInt(a[5]), cz = Integer.parseInt(a[6]);
    require(seed == 17320110707L && user.equals("EnvDamage307") && user.length() <= 16,
        "env-damage identity drift");
    Duration timeout = Duration.ofSeconds(180);
    B173DedicatedServer server =
        new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout), reader = null;
    BlockPosition top, pad, tower, body, head, lavaFloor, lava, lower, upper;
    int column;
    PlayerPose pose;
    RemoteIncomingHit hit;
    int afterDrown, afterWall, afterLava;
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1, 2, 3},
          new int[] {1, 12, 327, 9}, new int[] {64, 4, 1, 8}, new int[] {0, 0, 0, 0});
      actor.connect();
      pose = actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() == 4 && actor.awaitHealth(20) == 20,
          "env-damage inventory or health drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      top = foundation(initial, cx, cz);
      column = 0;
      actor.selectHeldSlot(0);
      while (
          water(initial.blockAt(local(top.x(), cx), top.y() + 1, local(top.z(), cz)).legacyId())) {
        top = place(actor, top, BlockFace.UP, 1);
        pose = actor.moveAndObserve(0D, 1D, 0D, 1).resulting();
        require(++column <= 15, "water column exceeded env-damage fixture");
      }
      for (int lift = 0; lift < 8; lift++) {
        top = place(actor, top, BlockFace.UP, 1);
        pose = actor.moveAndObserve(0D, 1D, 0D, 1).resulting();
        column++;
      }
      lavaFloor = place(actor, place(actor, top, BlockFace.NORTH, 1), BlockFace.NORTH, 1);
      for (BlockFace wall : new BlockFace[] {BlockFace.NORTH, BlockFace.EAST, BlockFace.WEST})
        place(actor, place(actor, lavaFloor, wall, 1), BlockFace.UP, 1);
      pad = place(actor, place(actor, top, BlockFace.SOUTH, 1), BlockFace.SOUTH, 1);
      tower = place(actor, pad, BlockFace.EAST, 1);
      for (int n = 0; n < 4; n++)
        tower = place(actor, tower, BlockFace.UP, 1);
      for (BlockFace wall :
          new BlockFace[] {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST}) {
        BlockPosition w = place(actor, place(actor, top, wall, 1), BlockFace.UP, 1);
        place(actor, w, BlockFace.UP, 1);
      }
      while (pose.y() > top.y() + 1.01D)
        pose = actor.moveAndObserve(0D, -1D, 0D, 1).resulting();
      actor.selectHeldSlot(3);
      lower = place(actor, top, BlockFace.UP, 9);
      upper =
          place(actor, new BlockPosition(top.x(), top.y() + 2, top.z() - 1), BlockFace.SOUTH, 9);
      require(water(worldline.test.WorldlineSmokeAwait.observe(actor, 5)
                      .blockAt(lower.x(), lower.y(), lower.z())
                      .legacyId())
              && water(worldline.test.WorldlineSmokeAwait.observe(actor, 1)
                      .blockAt(upper.x(), upper.y(), upper.z())
                      .legacyId())
              && actor.health() == 20,
          "pre-drown fixture drift");
      afterDrown = dropTo(actor, 20, 18, "drowning");
      hit = actor.awaitIncomingHit(afterDrown);
      require(hit.healthBefore() == 20 && hit.healthAfter() == afterDrown && hit.damage() == 2,
          "drowning Packet8/38 health drift");
      pose = actor.moveAndObserve(0D, 3D, 0D, 4).resulting();
      pose = actor.moveAndObserve(pad.x() + 0.5D - pose.x(), 0D, pad.z() + 0.5D - pose.z(), 4)
                 .resulting();
      pose = actor.moveAndObserve(0D, (pad.y() + 1.0D) - pose.y(), 0D, 4).resulting();
      require(pose.y() >= pad.y() + 0.9D,
          "post-drown pose drift pose=" + pose.x() + "," + pose.y() + "," + pose.z());
      int preWall = actor.health();
      require(preWall > 1 && preWall <= 20, "pre-suffocate health drift: " + preWall);
      actor.selectHeldSlot(1);
      actor.placeHeldBlock(tower, BlockFace.WEST);
      actor.selectHeldSlot(0);
      tower = place(actor, tower, BlockFace.UP, 1);
      actor.selectHeldSlot(1);
      actor.placeHeldBlock(tower, BlockFace.WEST);
      body = new BlockPosition(pad.x(), pad.y() + 1, pad.z());
      head = new BlockPosition(pad.x(), pad.y() + 2, pad.z());
      actor.awaitBlock(body, new BlockState(12, 0));
      actor.awaitBlock(head, new BlockState(12, 0));
      pose = actor
                 .moveAndObserve(pad.x() + 0.5D - pose.x(), (pad.y() + 1.0D) - pose.y(),
                     pad.z() + 0.5D - pose.z(), 3)
                 .resulting();
      afterWall = dropTo(actor, preWall, preWall - 1, "suffocation");
      pose = actor.moveAndObserve(0D, 2D, 0D, 2).resulting();
      lava = BlockFace.UP.adjacent(lavaFloor);
      pose = actor
                 .moveAndObserve(top.x() + 0.5D - pose.x(), (top.y() + 3.0D) - pose.y(),
                     (top.z() - 0.5D) - pose.z(), 8)
                 .resulting();
      actor.selectHeldSlot(2);
      actor.look(180F, 70F);
      actor.useHeldItemOnBlock(lavaFloor, BlockFace.UP);
      actor.useSelectedItemInAir();
      actor.awaitBlock(lava, new BlockState(11, 0));
      int preLava = actor.health();
      pose = actor
                 .moveAndObserve(
                     lava.x() + 0.5D - pose.x(), lava.y() - pose.y(), lava.z() + 0.5D - pose.z(), 2)
                 .resulting();
      afterLava = dropTo(actor, preLava, preLava - 4, "lava");
      pose = actor.moveAndObserve(0D, 2D, 0D, 2).resulting();
      require(actor.health() == afterLava, "post-lava health drift: " + actor.health());
      actor.close();
      awaitPlayers(server, 0);
      server.save();
      require(server.player(user).health() == afterLava, "persisted env-damage health drift");
      reader = new B173WireClient("127.0.0.1", port, user, timeout);
      reader.connect();
      reader.synchronizePose();
      require(reader.awaitHealth(afterLava) == afterLava, "fresh-login env-damage health drift");
      String evidence = "causes=drown+suffocate+lava,column=" + column + ",water=" + lower.x() + ":"
          + lower.y() + ":" + lower.z() + "+" + upper.x() + ":" + upper.y() + ":" + upper.z()
          + ",head=" + head.x() + ":" + head.y() + ":" + head.z() + ":12:0,lava=" + lava.x() + ":"
          + lava.y() + ":" + lava.z() + ":11:0,health=20->" + afterDrown + "->" + preWall + "->"
          + afterWall + "->" + afterLava + ",status=2,persisted=true,clients=2,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=raised-stone+two-still-water+falling-sand12+still-lava11|cause=submerged-eye-air-deplete+stand-under-falling-sand12+stand-in-lava|wire=packet38-status2+packet8-health20->"
          + afterDrown + "/" + preWall + "->" + afterWall + "->" + afterLava
          + "|oracle=drown+suffocate+lava-drops+fresh-login|" + evidence;
      System.out.println("WORLDLINE_M307_DAMAGE=" + evidence);
      System.out.println("WORLDLINE_M307_TRACE=" + trace);
      System.out.println("WORLDLINE_M307_SIGNATURE=" + sha(trace));
    } finally {
      actor.close();
      if (reader != null)
        reader.close();
      server.close();
    }
  }
  private static int dropTo(B173WireClient a, int before, int expect, String name)
      throws Exception {
    int after = worldline.test.WorldlineSmokeAwait.awaitEntity(
        a, a::health, value -> value < before, name + " health", 500);
    require(after == expect, name + " Packet8 health drift: " + before + "->" + after);
    return after;
  }
  private static BlockPosition place(
      B173WireClient a, BlockPosition support, BlockFace face, int id) throws Exception {
    BlockPosition target = face.adjacent(support);
    a.placeHeldBlock(support, face);
    a.awaitBlock(target, new BlockState(id, 0));
    return target;
  }
  private static BlockPosition foundation(RemoteChunkSnapshot q, int cx, int cz) {
    for (int x = 4; x <= 11; x++)
      for (int z = 4; z <= 11; z++)
        for (int y = 126; y >= 1; y--)
          if (q.blockAt(x, y, z).legacyId() == 3 && water(q.blockAt(x, y + 1, z).legacyId()))
            return new BlockPosition(cx * 16 + x, y, cz * 16 + z);
    throw new IllegalStateException("no deterministic env-damage foundation");
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
