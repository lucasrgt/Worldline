package worldline.smoke.crosschunkredstoneb173;
import static worldline.b173server.B173FixtureSupport.*;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Powers official dust from a lever placed across an east chunk seam. */
public final class CrossChunkRedstoneSmoke {
  private CrossChunkRedstoneSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 10)
      throw new IllegalArgumentException(
          "usage: CrossChunkRedstoneSmoke server.jar workspace port seed username wireX leverX chunkZ fixtureTicks signalTicks");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]), wireX = Integer.parseInt(a[5]),
        leverX = Integer.parseInt(a[6]), cz = Integer.parseInt(a[7]),
        fixtureTicks = Integer.parseInt(a[8]), signalTicks = Integer.parseInt(a[9]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    require(leverX == wireX + 1, "M126 requires east seam");
    Duration timeout = Duration.ofSeconds(90);
    B173DedicatedServer server =
        new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout), reader = null;
    RemoteChunkSnapshot left0, right0, left1, right1;
    BlockPosition foundation, top, lever, wire;
    BlockState leverOff, leverOn, wireOff, wireOn;
    int column;
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, wireX * 16 + 15.5D, 60D, cz * 16 + 5.5D,
          new int[] {0, 1, 2}, new int[] {1, 69, 331}, new int[] {16, 1, 1}, new int[] {0, 0, 0});
      actor.connect();
      actor.synchronizePose();
      actor.look(-90F, 0F);
      require(actor.awaitInventory().occupiedSlots() == 3, "seam wire inventory drift");
      RemoteWorldView initialWorld = actor.awaitRemoteChunk(wireX, cz);
      worldline.test.WorldlineSmokeAwait.observe(actor, 20);
      initialWorld = actor.awaitRemoteChunk(leverX, cz);
      RemoteChunkSnapshot initial = initialWorld.chunkAt(wireX, cz);
      foundation = foundation(initial, wireX, cz);
      top = foundation;
      column = 0;
      actor.selectHeldSlot(0);
      while (water(initial.blockAt(15, top.y() + 1, top.z() - cz * 16).legacyId())) {
        actor.placeHeldBlock(top, BlockFace.UP);
        top = BlockFace.UP.adjacent(top);
        actor.awaitBlock(top, new BlockState(1, 0));
        actor.moveAndObserve(0D, 1D, 0D, 1);
        column++;
        require(column <= 15, "water column exceeded stack");
      }
      actor.placeHeldBlock(top, BlockFace.UP);
      top = BlockFace.UP.adjacent(top);
      actor.awaitBlock(top, new BlockState(1, 0));
      actor.moveAndObserve(0D, 1D, 0D, 1);
      column++;
      wire = BlockFace.UP.adjacent(top);
      lever = BlockFace.EAST.adjacent(top);
      require(
          wire.x() == wireX * 16 + 15 && lever.x() == leverX * 16, "signal fixture missed seam");
      actor.selectHeldSlot(2);
      actor.useHeldItemOnBlock(top, BlockFace.UP);
      wireOff = worldline.test.WorldlineSmokeAwait.observe(actor, 5).blockAt(
          wire.x(), wire.y(), wire.z());
      require(wireOff.equals(new BlockState(55, 0)), "seam wire off drift");
      actor.selectHeldSlot(1);
      actor.placeHeldBlock(top, BlockFace.EAST);
      leverOff = worldline.test.WorldlineSmokeAwait.observe(actor, 5).blockAt(
          lever.x(), lever.y(), lever.z());
      require(leverOff.legacyId() == 69 && leverOff.metadata() < 8, "seam lever off drift");
      actor.selectHeldSlot(3);
      RemoteWorldView before = worldline.test.WorldlineSmokeAwait.observe(actor, fixtureTicks);
      left0 = before.chunkAt(wireX, cz);
      right0 = before.chunkAt(leverX, cz);
      wireOff = left0.blockAt(15, wire.y(), wire.z() - cz * 16);
      leverOff = right0.blockAt(0, lever.y(), lever.z() - cz * 16);
      require(wireOff.equals(new BlockState(55, 0)), "wire powered before seam treatment");
      actor.activateBlock(lever, BlockFace.UP);
      RemoteWorldView live = worldline.test.WorldlineSmokeAwait.observe(actor, signalTicks);
      wireOn = live.blockAt(wire.x(), wire.y(), wire.z());
      leverOn = live.blockAt(lever.x(), lever.y(), lever.z());
      require(wireOn.equals(new BlockState(55, 15)) && leverOn.legacyId() == 69
              && leverOn.metadata() != leverOff.metadata(),
          "cross-chunk signal absent: " + wireOn + " / " + leverOn);
      actor.close();
      awaitPlayers(server, 0);
      server.save();
      reader = new B173WireClient("127.0.0.1", port, user, timeout);
      reader.connect();
      reader.synchronizePose();
      worldline.test.WorldlineSmokeAwait.observe(reader, 20);
      RemoteWorldView after = reader.awaitRemoteChunk(wireX, cz);
      after = reader.awaitRemoteChunk(leverX, cz);
      left1 = after.chunkAt(wireX, cz);
      right1 = after.chunkAt(leverX, cz);
      require(left1.blockAt(15, wire.y(), wire.z() - cz * 16).equals(wireOn)
              && right1.blockAt(0, lever.y(), lever.z() - cz * 16).equals(leverOn),
          "fresh seam signal drift");
    } finally {
      actor.close();
      if (reader != null)
        reader.close();
      server.close();
    }
    StateDelta left = delta(left0, left1), right = delta(right0, right1);
    require(left.changed == 1 && right.changed == 1,
        "seam signal changed unrelated states " + left + " / " + right);
    String evidence = "column=" + column + ",lever=" + lever.x() + ":" + lever.y() + ":" + lever.z()
        + ":" + leverOff.metadata() + "->" + leverOn.metadata() + ",wire=" + wire.x() + ":"
        + wire.y() + ":" + wire.z() + ":" + wireOff.metadata() + "->" + wireOn.metadata()
        + ",left=" + left + ",right=" + right;
    String trace = "v1|server=official-b1.7.3|seed=" + seed
        + "|fixture=stone-edge-column+lever69-across-seam+dust331-wire55|seam=" + wireX + ":"
        + leverX + "," + cz + "|settle=" + fixtureTicks + "+" + signalTicks
        + "ticks|cause=packet15-lever-activate-neighbor|effect=packet53-wire-power-source-chunk|observation=fresh-packet51-both-chunks|"
        + evidence + "|disconnect=clean";
    System.out.println("WORLDLINE_M126_SIGNAL=" + evidence);
    System.out.println("WORLDLINE_M126_TRACE=" + trace);
    System.out.println("WORLDLINE_M126_SIGNATURE=" + sha(trace));
  }
  private static BlockPosition foundation(RemoteChunkSnapshot q, int cx, int cz) {
    for (int z = 3; z <= 12; z++)
      for (int y = 126; y >= 1; y--)
        if (q.blockAt(15, y, z).legacyId() == 3 && water(q.blockAt(15, y + 1, z).legacyId()))
          return new BlockPosition(cx * 16 + 15, y, cz * 16 + z);
    throw new IllegalStateException("no seam redstone foundation");
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
