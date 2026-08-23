package worldline.smoke.crosschunkdoorrecoveryb173;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Closes an official iron door from a lever placed across an east chunk seam. */
public final class CrossChunkIronDoorRecoverySmoke {
  private CrossChunkIronDoorRecoverySmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 10)
      throw new IllegalArgumentException(
          "usage: CrossChunkIronDoorRecoverySmoke server.jar workspace port seed username doorX leverX chunkZ fixtureTicks signalTicks");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]), doorX = Integer.parseInt(a[5]),
        leverX = Integer.parseInt(a[6]), cz = Integer.parseInt(a[7]),
        fixtureTicks = Integer.parseInt(a[8]), signalTicks = Integer.parseInt(a[9]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    require(leverX == doorX + 1, "M129 requires east seam");
    Duration timeout = Duration.ofSeconds(90);
    B173DedicatedServer server =
        new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout), witness = null,
                   reader = null;
    RemoteChunkSnapshot leftOff, rightOff, leftOn, rightOn, leftFinal, rightFinal;
    BlockPosition stone, bottom, top, lever;
    BlockState leverOff, leverOn, leverFinal, bottomOff, bottomOn, bottomFinal, topOff, topOn,
        topFinal;
    int column;
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, doorX * 16 + 15.5D, 60D, cz * 16 + 5.5D,
          new int[] {0, 1, 2}, new int[] {1, 69, 330}, new int[] {16, 1, 1}, new int[] {0, 0, 0});
      actor.connect();
      actor.synchronizePose();
      actor.look(-90F, 0F);
      require(actor.awaitInventory().occupiedSlots() == 3, "seam door reset inventory drift");
      RemoteWorldView initialWorld = actor.awaitRemoteChunk(doorX, cz);
      worldline.test.WorldlineSmokeAwait.observe(actor, 20);
      initialWorld = actor.awaitRemoteChunk(leverX, cz);
      RemoteChunkSnapshot initial = initialWorld.chunkAt(doorX, cz);
      stone = foundation(initial, doorX, cz);
      column = 0;
      actor.selectHeldSlot(0);
      while (water(initial.blockAt(15, stone.y() + 1, stone.z() - cz * 16).legacyId())) {
        actor.placeHeldBlock(stone, BlockFace.UP);
        stone = BlockFace.UP.adjacent(stone);
        actor.awaitBlock(stone, new BlockState(1, 0));
        actor.moveAndObserve(0D, 1D, 0D, 1);
        column++;
        require(column <= 15, "water column exceeded stack");
      }
      actor.placeHeldBlock(stone, BlockFace.UP);
      stone = BlockFace.UP.adjacent(stone);
      actor.awaitBlock(stone, new BlockState(1, 0));
      actor.moveAndObserve(0D, 1D, 0D, 1);
      column++;
      bottom = BlockFace.UP.adjacent(stone);
      top = BlockFace.UP.adjacent(bottom);
      lever = BlockFace.EAST.adjacent(stone);
      require(bottom.x() == doorX * 16 + 15 && lever.x() == leverX * 16,
          "door reset fixture missed seam");
      actor.selectHeldSlot(2);
      actor.useHeldItemOnBlock(stone, BlockFace.UP);
      RemoteWorldView placed = worldline.test.WorldlineSmokeAwait.observe(actor, 5);
      bottomOff = placed.blockAt(bottom.x(), bottom.y(), bottom.z());
      topOff = placed.blockAt(top.x(), top.y(), top.z());
      require(bottomOff.equals(new BlockState(71, 0)) && topOff.equals(new BlockState(71, 8)),
          "seam door off drift");
      actor.selectHeldSlot(1);
      actor.placeHeldBlock(stone, BlockFace.EAST);
      leverOff = worldline.test.WorldlineSmokeAwait.observe(actor, 5).blockAt(
          lever.x(), lever.y(), lever.z());
      require(leverOff.equals(new BlockState(69, 1)), "seam lever off drift");
      actor.selectHeldSlot(3);
      RemoteWorldView baseline = worldline.test.WorldlineSmokeAwait.observe(actor, fixtureTicks);
      leftOff = baseline.chunkAt(doorX, cz);
      rightOff = baseline.chunkAt(leverX, cz);
      actor.activateBlock(lever, BlockFace.UP);
      RemoteWorldView opened = worldline.test.WorldlineSmokeAwait.observe(actor, signalTicks);
      leverOn = opened.blockAt(lever.x(), lever.y(), lever.z());
      bottomOn = opened.blockAt(bottom.x(), bottom.y(), bottom.z());
      topOn = opened.blockAt(top.x(), top.y(), top.z());
      require(leverOn.equals(new BlockState(69, 9)) && bottomOn.equals(new BlockState(71, 4))
              && topOn.equals(new BlockState(71, 12)),
          "cross-chunk open precondition absent");
      actor.close();
      awaitPlayers(server, 0);
      server.save();
      witness = new B173WireClient("127.0.0.1", port, user, timeout);
      witness.connect();
      witness.synchronizePose();
      worldline.test.WorldlineSmokeAwait.observe(witness, 20);
      RemoteWorldView powered = witness.awaitRemoteChunk(doorX, cz);
      powered = witness.awaitRemoteChunk(leverX, cz);
      leftOn = powered.chunkAt(doorX, cz);
      rightOn = powered.chunkAt(leverX, cz);
      require(leftOn.blockAt(15, bottom.y(), bottom.z() - cz * 16).equals(bottomOn)
              && leftOn.blockAt(15, top.y(), top.z() - cz * 16).equals(topOn)
              && rightOn.blockAt(0, lever.y(), lever.z() - cz * 16).equals(leverOn),
          "fresh open precondition drift");
      witness.selectHeldSlot(3);
      witness.activateBlock(lever, BlockFace.UP);
      RemoteWorldView closed = worldline.test.WorldlineSmokeAwait.observe(witness, signalTicks);
      leverFinal = closed.blockAt(lever.x(), lever.y(), lever.z());
      bottomFinal = closed.blockAt(bottom.x(), bottom.y(), bottom.z());
      topFinal = closed.blockAt(top.x(), top.y(), top.z());
      require(
          leverFinal.equals(leverOff) && bottomFinal.equals(bottomOff) && topFinal.equals(topOff),
          "cross-chunk door recovery absent: " + leverFinal + " / " + bottomFinal + " / "
              + topFinal);
      witness.close();
      awaitPlayers(server, 0);
      server.save();
      reader = new B173WireClient("127.0.0.1", port, user, timeout);
      reader.connect();
      reader.synchronizePose();
      worldline.test.WorldlineSmokeAwait.observe(reader, 20);
      RemoteWorldView after = reader.awaitRemoteChunk(doorX, cz);
      after = reader.awaitRemoteChunk(leverX, cz);
      leftFinal = after.chunkAt(doorX, cz);
      rightFinal = after.chunkAt(leverX, cz);
      require(leftFinal.blockAt(15, bottom.y(), bottom.z() - cz * 16).equals(bottomFinal)
              && leftFinal.blockAt(15, top.y(), top.z() - cz * 16).equals(topFinal)
              && rightFinal.blockAt(0, lever.y(), lever.z() - cz * 16).equals(leverFinal),
          "fresh closed seam door drift");
    } finally {
      actor.close();
      if (witness != null)
        witness.close();
      if (reader != null)
        reader.close();
      server.close();
    }
    StateDelta left = delta(leftOn, leftFinal), right = delta(rightOn, rightFinal),
               leftResidual = delta(leftOff, leftFinal),
               rightResidual = delta(rightOff, rightFinal);
    require(left.changed == 2 && right.changed == 1,
        "seam door reset changed unrelated states " + left + " / " + right);
    require(leftResidual.changed == 0 && rightResidual.changed == 0,
        "seam door reset did not restore baseline " + leftResidual + " / " + rightResidual);
    String evidence = "column=" + column + ",lever=" + lever.x() + ":" + lever.y() + ":" + lever.z()
        + ":" + leverOn.metadata() + "->" + leverFinal.metadata() + ",door=" + bottom.x() + ":"
        + bottom.y() + ":" + bottom.z() + ":" + bottomOn.metadata() + "->" + bottomFinal.metadata()
        + ",top=" + topOn.metadata() + "->" + topFinal.metadata() + ",left=" + left
        + ",right=" + right + ",residual=" + leftResidual.changed + ":" + rightResidual.changed;
    String trace = "v1|server=official-b1.7.3|seed=" + seed
        + "|fixture=stone-edge-column+lever69-across-seam+iron-door-item330-block71|seam=" + doorX
        + ":" + leverX + "," + cz + "|settle=" + fixtureTicks + "+" + signalTicks
        + "ticks|precondition=fresh-lever69:9+door71:4,12|cause=packet15-lever-deactivate-neighbor|effect=packet53-two-block-iron-door-close-source-chunk|observation=fresh-packet51-both-chunks+exact-baseline-recovery|"
        + evidence + "|disconnect=clean";
    System.out.println("WORLDLINE_M129_DOOR=" + evidence);
    System.out.println("WORLDLINE_M129_TRACE=" + trace);
    System.out.println("WORLDLINE_M129_SIGNATURE=" + sha(trace));
  }
  private static BlockPosition foundation(RemoteChunkSnapshot q, int cx, int cz) {
    for (int z = 3; z <= 12; z++)
      for (int y = 126; y >= 1; y--)
        if (q.blockAt(15, y, z).legacyId() == 3 && water(q.blockAt(15, y + 1, z).legacyId()))
          return new BlockPosition(cx * 16 + 15, y, cz * 16 + z);
    throw new IllegalStateException("no seam door recovery foundation");
  }
  private static StateDelta delta(RemoteChunkSnapshot a, RemoteChunkSnapshot b) throws Exception {
    MessageDigest md = MessageDigest.getInstance("SHA-256");
    ByteBuffer row = ByteBuffer.allocate(10);
    int n = 0;
    for (int x = 0; x < 16; x++)
      for (int z = 0; z < 16; z++)
        for (int y = 0; y < 128; y++)
          if (!a.blockAt(x, y, z).equals(b.blockAt(x, y, z))) {
            BlockState p = a.blockAt(x, y, z), q = b.blockAt(x, y, z);
            n++;
            row.clear();
            row.putShort((short) x)
                .putShort((short) y)
                .putShort((short) z)
                .put((byte) p.legacyId())
                .put((byte) p.metadata())
                .put((byte) q.legacyId())
                .put((byte) q.metadata());
            md.update(row.array());
          }
    return new StateDelta(n, hex(md.digest()));
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
  private static final class StateDelta {
    final int changed;
    final String hash;
    StateDelta(int n, String h) {
      changed = n;
      hash = h;
    }
    public String toString() {
      return changed + ":" + hash;
    }
  }
}
