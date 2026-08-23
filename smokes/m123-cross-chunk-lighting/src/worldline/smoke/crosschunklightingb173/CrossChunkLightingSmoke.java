package worldline.smoke.crosschunklightingb173;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Places one official edge glowstone and observes light in both chunks. */
public final class CrossChunkLightingSmoke {
  private CrossChunkLightingSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 10)
      throw new IllegalArgumentException(
          "usage: CrossChunkLightingSmoke server.jar workspace port seed username sourceX neighborX chunkZ sourceId settleTicks");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]), sourceX = Integer.parseInt(a[5]),
        neighborX = Integer.parseInt(a[6]), chunkZ = Integer.parseInt(a[7]),
        sourceId = Integer.parseInt(a[8]), settle = Integer.parseInt(a[9]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    require(neighborX == sourceX + 1, "M123 requires east chunk seam");
    Duration timeout = Duration.ofSeconds(90);
    B173DedicatedServer server =
        new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout), reader = null;
    RemoteChunkSnapshot leftBefore, rightBefore, leftAfter, rightAfter;
    BlockPosition support, target;
    int z;
    try {
      server.boot();
      B173PlayerSeed.writeHolding(
          workspace, user, sourceX * 16 + 15.5D, 120D, chunkZ * 16 + 8.5D, sourceId, 1, 0);
      actor.connect();
      PlayerPose pose = actor.synchronizePose();
      RemoteInventoryView inventory = actor.awaitInventory();
      require(inventory.occupiedSlots() == 1
              && inventory.slot(36).item().equals(new RemoteItemStack(sourceId, 1, 0)),
          "source inventory drift");
      RemoteWorldView world = actor.awaitRemoteChunk(sourceX, chunkZ);
      actor.sustainTicks(20);
      world = actor.awaitRemoteChunk(neighborX, chunkZ);
      leftBefore = world.chunkAt(sourceX, chunkZ);
      rightBefore = world.chunkAt(neighborX, chunkZ);
      support = support(leftBefore, rightBefore, sourceX, chunkZ);
      target = BlockFace.UP.adjacent(support);
      z = target.z() - chunkZ * 16;
      pose = actor.moveAndObserve(target.x() + 0.5D - pose.x(), 0D, target.z() + 0.5D - pose.z(), 3)
                 .resulting();
      while (pose.y() > target.y() + 3D) {
        MovementOutcome move = actor.moveAndObserve(0D, -1D, 0D, 1);
        pose = move.resulting();
        require(!move.corrected() || pose.y() <= target.y() + 4D,
            "descent corrected above seam source");
      }
      actor.selectHeldSlot(0);
      actor.placeHeldBlock(support, BlockFace.UP);
      actor.awaitBlock(target, new BlockState(sourceId, 0));
      actor.sustainTicks(settle);
      actor.close();
      awaitPlayers(server, 0);
      server.save();
      reader = new B173WireClient("127.0.0.1", port, user, timeout);
      reader.connect();
      reader.synchronizePose();
      reader.sustainTicks(20);
      RemoteWorldView after = reader.awaitRemoteChunk(sourceX, chunkZ);
      after = reader.awaitRemoteChunk(neighborX, chunkZ);
      leftAfter = after.chunkAt(sourceX, chunkZ);
      rightAfter = after.chunkAt(neighborX, chunkZ);
      require(leftBefore.blockAt(15, target.y(), z).legacyId() != sourceId
              && leftAfter.blockAt(15, target.y(), z).legacyId() == sourceId,
          "edge source transition drift");
    } finally {
      actor.close();
      if (reader != null)
        reader.close();
      server.close();
    }
    Delta leftBlock = delta(leftBefore, leftAfter, false),
          rightBlock = delta(rightBefore, rightAfter, false),
          leftSky = delta(leftBefore, leftAfter, true),
          rightSky = delta(rightBefore, rightAfter, true);
    int y = target.y(), l0 = leftBefore.blockLightAt(15, y, z),
        l1 = leftAfter.blockLightAt(15, y, z), r0 = rightBefore.blockLightAt(0, y, z),
        r1 = rightAfter.blockLightAt(0, y, z);
    BlockState sourceBefore = leftBefore.blockAt(15, y, z),
               neighborBefore = rightBefore.blockAt(0, y, z);
    require(l0 == 0 && l1 == 15 && r0 == 0 && r1 == 12 && water(sourceBefore.legacyId())
            && water(neighborBefore.legacyId()),
        "cross-chunk water-light samples absent " + l0 + "->" + l1 + "/" + r0 + "->" + r1
            + " blocks=" + sourceBefore + "/" + neighborBefore + " deltas=" + leftBlock + "/"
            + rightBlock);
    require(leftBlock.increased > 0 && rightBlock.increased > 0,
        "cross-chunk block-light delta absent");
    String evidence = "source=" + target.x() + ":" + y + ":" + target.z() + ":"
        + sourceBefore.legacyId() + ":" + sourceBefore.metadata() + "->89:0,neighbor="
        + (target.x() + 1) + ":" + y + ":" + target.z() + ":" + neighborBefore.legacyId() + ":"
        + neighborBefore.metadata() + ",samples=0->15/0->12,leftBlock=" + leftBlock
        + ",rightBlock=" + rightBlock + ",leftSky=" + leftSky + ",rightSky=" + rightSky;
    String trace = "v1|server=official-b1.7.3|seed=" + seed + "|seam=chunks-" + sourceX + ":"
        + neighborX + "," + chunkZ
        + "|intervention=packet15-glowstone89-replaces-edge-water|confirmation=packet53|settle="
        + settle + "ticks|observation=fresh-login-packet51-both-chunks|" + evidence
        + "|disconnect=clean";
    System.out.println("WORLDLINE_M123_LIGHT=" + evidence);
    System.out.println("WORLDLINE_M123_TRACE=" + trace);
    System.out.println("WORLDLINE_M123_SIGNATURE=" + sha(trace));
  }
  private static BlockPosition support(
      RemoteChunkSnapshot left, RemoteChunkSnapshot right, int chunkX, int chunkZ) {
    for (int z = 2; z <= 13; z++)
      for (int y = 126; y >= 1; y--)
        if (solid(left.blockAt(15, y, z).legacyId())
            && replaceable(left.blockAt(15, y + 1, z).legacyId())
            && replaceable(right.blockAt(0, y + 1, z).legacyId())
            && left.blockLightAt(15, y + 1, z) == 0 && right.blockLightAt(0, y + 1, z) == 0)
          return new BlockPosition(chunkX * 16 + 15, y, chunkZ * 16 + z);
    throw new IllegalStateException("no deterministic seam source support");
  }
  private static Delta delta(RemoteChunkSnapshot before, RemoteChunkSnapshot after, boolean sky)
      throws Exception {
    MessageDigest d = MessageDigest.getInstance("SHA-256");
    ByteBuffer row = ByteBuffer.allocate(8);
    int changed = 0, increased = 0, decreased = 0, max = 0;
    for (int x = 0; x < 16; x++)
      for (int z = 0; z < 16; z++)
        for (int y = 0; y < 128; y++) {
          int a = sky ? before.skyLightAt(x, y, z) : before.blockLightAt(x, y, z),
              b = sky ? after.skyLightAt(x, y, z) : after.blockLightAt(x, y, z);
          if (a != b) {
            changed++;
            if (b > a)
              increased++;
            else
              decreased++;
            max = Math.max(max, Math.abs(b - a));
            row.clear();
            row.putShort((short) x).putShort((short) y).putShort((short) z).put((byte) a).put(
                (byte) b);
            d.update(row.array());
          }
        }
    return new Delta(changed, increased, decreased, max, hex(d.digest()));
  }
  private static boolean solid(int id) {
    return id != 0 && id != 8 && id != 9 && id != 10 && id != 11 && id != 31 && id != 37 && id != 38
        && id != 39 && id != 40 && id != 78;
  }
  private static boolean replaceable(int id) {
    return id == 0 || id == 8 || id == 9 || id == 10 || id == 11 || id == 78;
  }
  private static boolean water(int id) {
    return id == 8 || id == 9;
  }
  private static void awaitPlayers(B173DedicatedServer s, int n) throws Exception {
    long end = System.currentTimeMillis() + 5000;
    while (System.currentTimeMillis() < end) {
      if (s.players().size() == n)
        return;
      Thread.sleep(100);
    }
    throw new IllegalStateException("player count drift");
  }
  private static String sha(String s) throws Exception {
    return hex(MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8)));
  }
  private static String hex(byte[] b) {
    StringBuilder s = new StringBuilder();
    for (byte v : b)
      s.append(String.format("%02x", v & 255));
    return s.toString();
  }
  private static void require(boolean v, String m) {
    if (!v)
      throw new IllegalStateException(m);
  }
  private static final class Delta {
    final int changed, increased, decreased, max;
    final String hash;
    Delta(int c, int i, int d, int m, String h) {
      changed = c;
      increased = i;
      decreased = d;
      max = m;
      hash = h;
    }
    public String toString() {
      return changed + ":" + increased + ":" + decreased + ":" + max + ":" + hash;
    }
  }
}
