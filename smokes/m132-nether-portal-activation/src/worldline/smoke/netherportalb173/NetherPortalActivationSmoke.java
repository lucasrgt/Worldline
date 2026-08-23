package worldline.smoke.netherportalb173;
import static worldline.b173server.B173FixtureSupport.*;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.*;
import worldline.api.*;
import worldline.b173server.*;

/** Builds and ignites one official obsidian portal frame through protocol 14. */
public final class NetherPortalActivationSmoke {
  private NetherPortalActivationSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 9)
      throw new IllegalArgumentException(
          "usage: NetherPortalActivationSmoke server.jar workspace port seed username chunkX chunkZ fixtureTicks portalTicks");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    int cx = Integer.parseInt(a[5]), cz = Integer.parseInt(a[6]),
        fixtureTicks = Integer.parseInt(a[7]), portalTicks = Integer.parseInt(a[8]);
    Duration timeout = Duration.ofSeconds(90);
    B173DedicatedServer server =
        new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout), reader = null;
    RemoteChunkSnapshot before, after;
    BlockPosition anchor, bottom;
    List<BlockPosition> frame = new ArrayList<>(), inside = new ArrayList<>();
    int column, metadata = -1;
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1, 2},
          new int[] {1, 49, 259}, new int[] {16, 14, 1}, new int[] {0, 0, 0});
      actor.connect();
      actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() == 3, "portal inventory drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      anchor = foundation(initial, cx, cz);
      column = 0;
      actor.selectHeldSlot(0);
      while (water(initial.blockAt(local(anchor.x(), cx), anchor.y() + 1, local(anchor.z(), cz))
              .legacyId())) {
        anchor = place(actor, anchor, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        column++;
        require(column <= 15, "water column exceeded fixture stack");
      }
      anchor = place(actor, anchor, BlockFace.UP, 1);
      actor.moveAndObserve(0D, 1D, 0D, 1);
      column++;
      actor.selectHeldSlot(1);
      bottom = place(actor, anchor, BlockFace.UP, 49);
      frame.add(bottom);
      BlockPosition p = bottom;
      for (int i = 0; i < 3; i++) {
        p = place(actor, p, BlockFace.EAST, 49);
        frame.add(p);
      }
      BlockPosition left = bottom, right = p;
      for (int i = 0; i < 4; i++) {
        left = place(actor, left, BlockFace.UP, 49);
        right = place(actor, right, BlockFace.UP, 49);
        frame.add(left);
        frame.add(right);
      }
      p = left;
      for (int i = 0; i < 2; i++) {
        p = place(actor, p, BlockFace.EAST, 49);
        frame.add(p);
      }
      for (int y = 1; y <= 3; y++)
        for (int x = 1; x <= 2; x++)
          inside.add(new BlockPosition(bottom.x() + x, bottom.y() + y, bottom.z()));
      require(frame.size() == 14 && inside.size() == 6, "portal geometry drift");
      RemoteWorldView framed = worldline.test.WorldlineSmokeAwait.observe(actor, fixtureTicks);
      before = framed.chunkAt(cx, cz);
      for (BlockPosition q : frame)
        require(framed.blockAt(q.x(), q.y(), q.z()).equals(new BlockState(49, 0)),
            "obsidian frame drift at " + q);
      for (BlockPosition q : inside)
        require(framed.blockAt(q.x(), q.y(), q.z()).equals(new BlockState(0, 0)),
            "portal interior was not empty at " + q);
      actor.selectHeldSlot(2);
      actor.useHeldItemOnBlock(
          new BlockPosition(bottom.x() + 1, bottom.y(), bottom.z()), BlockFace.UP);
      RemoteWorldView active = worldline.test.WorldlineSmokeAwait.observe(actor, portalTicks);
      for (BlockPosition q : inside) {
        BlockState s = active.blockAt(q.x(), q.y(), q.z());
        require(s.legacyId() == 90, "portal activation absent at " + q + ": " + s);
        if (metadata < 0)
          metadata = s.metadata();
        else
          require(metadata == s.metadata(), "portal metadata drift");
      }
      actor.close();
      awaitPlayers(server, 0);
      server.save();
      reader = new B173WireClient("127.0.0.1", port, user, timeout);
      reader.connect();
      reader.synchronizePose();
      after = reader.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      for (BlockPosition q : frame)
        require(
            after.blockAt(local(q.x(), cx), q.y(), local(q.z(), cz)).equals(new BlockState(49, 0)),
            "fresh frame drift at " + q);
      for (BlockPosition q : inside)
        require(after.blockAt(local(q.x(), cx), q.y(), local(q.z(), cz))
                    .equals(new BlockState(90, metadata)),
            "fresh portal drift at " + q);
    } finally {
      actor.close();
      if (reader != null)
        reader.close();
      server.close();
    }
    StateDelta d = delta(before, after, inside, cx, cz);
    require(d.changed == 6, "portal interior transition drift: " + d);
    String bounds = bottom.x() + ":" + bottom.y() + ":" + bottom.z() + "-" + (bottom.x() + 3) + ":"
        + (bottom.y() + 4) + ":" + bottom.z();
    String evidence = "column=" + column + ",frame=" + bounds
        + ",obsidian=14,interior=6,portal=90:" + metadata + ",states=" + d;
    String trace = "v1|server=official-b1.7.3|seed=" + seed
        + "|profile=allow-nether-true|fixture=stone-column+obsidian49-frame4x5|construction=packet15-fourteen-blocks|baseline=six-air-cells|cause=packet15-flint-and-steel259|effect=official-portal-block90-six-cells|observation=live-packet53+fresh-login-packet51|oracle=frame-stable+six-interior-transitions|"
        + evidence + "|disconnect=clean";
    System.out.println("WORLDLINE_M132_PORTAL=" + evidence);
    System.out.println("WORLDLINE_M132_TRACE=" + trace);
    System.out.println("WORLDLINE_M132_SIGNATURE=" + sha(trace));
  }
  private static BlockPosition foundation(RemoteChunkSnapshot q, int cx, int cz) {
    for (int x = 4; x <= 10; x++)
      for (int z = 4; z <= 11; z++)
        for (int y = 126; y >= 1; y--)
          if (q.blockAt(x, y, z).legacyId() == 3 && water(q.blockAt(x, y + 1, z).legacyId()))
            return new BlockPosition(cx * 16 + x, y, cz * 16 + z);
    throw new IllegalStateException("no deterministic portal foundation");
  }
  private static StateDelta delta(RemoteChunkSnapshot a, RemoteChunkSnapshot b,
      List<BlockPosition> cells, int cx, int cz) throws Exception {
    MessageDigest md = MessageDigest.getInstance("SHA-256");
    ByteBuffer row = ByteBuffer.allocate(10);
    int n = 0;
    for (BlockPosition v : cells) {
      int x = local(v.x(), cx), y = v.y(), z = local(v.z(), cz);
      BlockState p = a.blockAt(x, y, z), q = b.blockAt(x, y, z);
      if (!p.equals(q)) {
        n++;
        row.clear();
        row.putShort((short) v.x())
            .putShort((short) y)
            .putShort((short) v.z())
            .put((byte) p.legacyId())
            .put((byte) p.metadata())
            .put((byte) q.legacyId())
            .put((byte) q.metadata());
        md.update(row.array());
      }
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
