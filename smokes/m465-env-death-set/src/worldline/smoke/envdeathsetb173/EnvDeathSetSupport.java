package worldline.smoke.envdeathsetb173;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Shared official-server helpers for the M465 environmental-death SET. */
final class EnvDeathSetSupport {
  private EnvDeathSetSupport() {
  }
  static final class Sites {
    final int column;
    final BlockPosition top, lava, lower, upper, head, body, pad, tower, safe;
    Sites(int c, BlockPosition t, BlockPosition l, BlockPosition lo, BlockPosition u,
        BlockPosition h, BlockPosition b, BlockPosition p, BlockPosition tw, BlockPosition s) {
      column = c;
      top = t;
      lava = l;
      lower = lo;
      upper = u;
      head = h;
      body = b;
      pad = p;
      tower = tw;
      safe = s;
    }
  }
  static B173WireClient open(int port, String user, Duration timeout) throws Exception {
    B173WireClient a = new B173WireClient("127.0.0.1", port, user, timeout);
    a.connect();
    a.synchronizePose();
    return a;
  }
  static Sites build(B173WireClient actor, int cx, int cz) throws Exception {
    RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
    BlockPosition top = foundation(initial, cx, cz);
    int column = 0;
    actor.selectHeldSlot(0);
    PlayerPose pose = actor.moveAndObserve(0D, 0D, 0D, 1).resulting();
    while (water(initial.blockAt(local(top.x(), cx), top.y() + 1, local(top.z(), cz)).legacyId())) {
      top = place(actor, top, BlockFace.UP, 1);
      pose = actor.moveAndObserve(0D, 1D, 0D, 1).resulting();
      require(++column <= 15, "water column exceeded env-death fixture");
    }
    for (int lift = 0; lift < 8; lift++) {
      top = place(actor, top, BlockFace.UP, 1);
      pose = actor.moveAndObserve(0D, 1D, 0D, 1).resulting();
      column++;
    }
    BlockPosition lavaFloor =
        place(actor, place(actor, top, BlockFace.NORTH, 1), BlockFace.NORTH, 1);
    for (BlockFace wall : new BlockFace[] {BlockFace.NORTH, BlockFace.EAST, BlockFace.WEST})
      place(actor, place(actor, lavaFloor, wall, 1), BlockFace.UP, 1);
    BlockPosition pad = place(actor, place(actor, top, BlockFace.SOUTH, 1), BlockFace.SOUTH, 1),
                  tower = place(actor, pad, BlockFace.EAST, 1);
    for (int n = 0; n < 4; n++)
      tower = place(actor, tower, BlockFace.UP, 1);
    for (BlockFace wall :
        new BlockFace[] {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST}) {
      BlockPosition w = place(actor, place(actor, top, wall, 1), BlockFace.UP, 1);
      place(actor, w, BlockFace.UP, 1);
    }
    BlockPosition safe =
        place(actor, new BlockPosition(top.x() + 1, top.y(), top.z()), BlockFace.EAST, 1);
    while (pose.y() > top.y() + 1.01D)
      pose = actor.moveAndObserve(0D, -1D, 0D, 1).resulting();
    BlockPosition lava = BlockFace.UP.adjacent(lavaFloor);
    walk(actor, top.x() + 0.5D, top.y() + 3.0D, top.z() - 0.5D);
    actor.selectHeldSlot(2);
    actor.look(180F, 70F);
    actor.useHeldItemOnBlock(lavaFloor, BlockFace.UP);
    actor.useSelectedItemInAir();
    actor.awaitBlock(lava, new BlockState(11, 0));
    walk(actor, top.x() + 0.5D, top.y() + 3.0D, top.z() + 0.5D);
    walk(actor, top.x() + 0.5D, top.y() + 1.0D, top.z() + 0.5D);
    actor.selectHeldSlot(3);
    BlockPosition lower = place(actor, top, BlockFace.UP, 9),
                  upper = place(actor, new BlockPosition(top.x(), top.y() + 2, top.z() - 1),
                      BlockFace.SOUTH, 9);
    require(water(worldline.test.WorldlineSmokeAwait.observe(actor, 5)
                    .blockAt(lower.x(), lower.y(), lower.z())
                    .legacyId())
            && water(worldline.test.WorldlineSmokeAwait.observe(actor, 1)
                    .blockAt(upper.x(), upper.y(), upper.z())
                    .legacyId())
            && actor.health() == 20,
        "pre-drown fixture drift");
    return new Sites(column, top, lava, lower, upper,
        new BlockPosition(pad.x(), pad.y() + 2, pad.z()),
        new BlockPosition(pad.x(), pad.y() + 1, pad.z()), pad, tower, safe);
  }
  static void dieZero(B173WireClient a, String name) throws Exception {
    int start = a.health();
    if (start == 20) {
      int mid = worldline.test.WorldlineSmokeAwait.awaitEntity(
          a, a::health, h -> h < 20, name + " first damage", 1200);
      require(mid < 20, name + " Packet8 drop absent: " + mid);
      if (mid >= 0) {
        RemoteIncomingHit hit = a.awaitIncomingHit(mid);
        require(hit.healthBefore() == 20 && hit.healthAfter() == mid && hit.damage() == 20 - mid,
            name + " Packet38/8 drift");
      }
    } else
      require(start < 20, name + " health drift before death wait: " + start);
    worldline.test.WorldlineSmokeAwait.awaitEntity(
        a, a::health, h -> h <= 0, name + " death", 1200);
    require(a.health() <= 0, name + " Packet8 health 0 absent: " + a.health());
  }
  static PlayerPose walk(B173WireClient a, double x, double y, double z) throws Exception {
    PlayerPose here = a.moveAndObserve(0D, 0D, 0D, 1).resulting();
    for (int n = 0; n < 32; n++) {
      double dx = x - here.x(), dy = y - here.y(), dz = z - here.z(),
             dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
      if (dist <= 0.05D)
        return here;
      int eye = worldline.test.WorldlineSmokeAwait.observe(a, 1)
                    .blockAt((int) Math.floor(here.x()), (int) Math.floor(here.y() + 1.62D),
                        (int) Math.floor(here.z()))
                    .legacyId();
      double s = Math.min(1D, (water(eye) ? 2D : 9D) / Math.max(dist, 1.0E-9D));
      here = a.moveAndObserve(dx * s, dy * s, dz * s, 1).resulting();
    }
    throw new IllegalStateException("env-death walk failed");
  }
  static BlockPosition place(B173WireClient a, BlockPosition support, BlockFace face, int id)
      throws Exception {
    BlockPosition target = face.adjacent(support);
    a.placeHeldBlock(support, face);
    a.awaitBlock(target, new BlockState(id, 0));
    return target;
  }
  static BlockPosition foundation(RemoteChunkSnapshot q, int cx, int cz) {
    for (int x = 4; x <= 11; x++)
      for (int z = 4; z <= 11; z++)
        for (int y = 126; y >= 1; y--)
          if (q.blockAt(x, y, z).legacyId() == 3 && water(q.blockAt(x, y + 1, z).legacyId()))
            return new BlockPosition(cx * 16 + x, y, cz * 16 + z);
    throw new IllegalStateException("no deterministic env-death foundation");
  }
  static boolean water(int id) {
    return id == 8 || id == 9;
  }
  static int local(int v, int c) {
    return v - c * 16;
  }
  static void awaitPlayers(B173DedicatedServer s, int n) throws Exception {
    long e = System.currentTimeMillis() + 5000;
    while (System.currentTimeMillis() < e) {
      if (s.players().size() == n)
        return;
      Thread.sleep(100);
    }
    throw new IllegalStateException("player count drift");
  }
  static String sha(String s) throws Exception {
    byte[] b = MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));
    StringBuilder v = new StringBuilder();
    for (byte x : b)
      v.append(String.format("%02x", x & 255));
    return v.toString();
  }
  static void require(boolean v, String m) {
    if (!v)
      throw new IllegalStateException(m);
  }
}
