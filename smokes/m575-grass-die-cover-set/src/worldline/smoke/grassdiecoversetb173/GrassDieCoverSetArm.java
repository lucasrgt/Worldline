package worldline.smoke.grassdiecoversetb173;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import worldline.api.BlockFace;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.RemoteChunkSnapshot;
import worldline.api.RemoteWorldView;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173WireClient;

/** Raised-stone grass pad and bounded random-tick die-off wait. */
public final class GrassDieCoverSetArm {
  private GrassDieCoverSetArm() {}

  static BlockPosition raise(B173WireClient a, RemoteChunkSnapshot initial, int cx, int cz, int[] column)
      throws Exception {
    BlockPosition top = foundation(initial, cx, cz);
    column[0] = 0;
    a.selectHeldSlot(0);
    while (water(at(initial, BlockFace.UP.adjacent(top), cx, cz).legacyId())) {
      top = place(a, top, BlockFace.UP, 1);
      a.moveAndObserve(0D, 1D, 0D, 1);
      require(++column[0] <= 15, "water column exceeded grass-die-cover fixture");
    }
    for (int lift = 0; lift < 8; lift++) {
      top = place(a, top, BlockFace.UP, 1);
      a.moveAndObserve(0D, 1D, 0D, 1);
      column[0]++;
    }
    return top;
  }

  static BlockPosition[] ring(B173WireClient a, BlockPosition top) throws Exception {
    BlockPosition e = place(a, top, BlockFace.EAST, 1);
    BlockPosition w = place(a, top, BlockFace.WEST, 1);
    BlockPosition n = place(a, top, BlockFace.NORTH, 1);
    BlockPosition s = place(a, top, BlockFace.SOUTH, 1);
    return new BlockPosition[] {e, w, n, s, place(a, e, BlockFace.NORTH, 1), place(a, e, BlockFace.SOUTH, 1),
        place(a, w, BlockFace.NORTH, 1), place(a, w, BlockFace.SOUTH, 1)};
  }

  static BlockPosition[] plant(B173WireClient a, BlockPosition[] support, int id) throws Exception {
    BlockPosition[] out = new BlockPosition[support.length];
    for (int i = 0; i < support.length; i++) {
      out[i] = place(a, support[i], BlockFace.UP, id);
    }
    return out;
  }

  static BlockPosition place(B173WireClient a, BlockPosition support, BlockFace face, int id) throws Exception {
    BlockPosition target = face.adjacent(support);
    a.placeHeldBlock(support, face);
    a.awaitBlock(target, new BlockState(id, 0));
    return target;
  }

  static BlockPosition foundation(RemoteChunkSnapshot c, int cx, int cz) {
    for (int x = 4; x <= 11; x++) {
      for (int z = 4; z <= 11; z++) {
        for (int y = 126; y >= 1; y--) {
          if (c.blockAt(x, y, z).legacyId() == 3 && water(c.blockAt(x, y + 1, z).legacyId()))
            return new BlockPosition(cx * 16 + x, y, cz * 16 + z);
        }
      }
    }
    throw new IllegalStateException("no deterministic grass-die-cover foundation");
  }

  static void waitDie(B173WireClient a, BlockPosition[] grass, BlockPosition[] exposed, BlockPosition covered,
      int window, int windows) throws Exception {
    RemoteWorldView died = worldline.test.WorldlineSmokeAwait.awaitWorld(
        a, v -> id(v, covered) == 3, "covered grass die", window * windows);
    for (BlockPosition g : grass) require(id(died, g) == 2, "source grass 2 died during wait");
    for (BlockPosition p : exposed) require(id(died, p) == 2, "exposed grass 2 died during wait");
  }

  static void persist(RemoteChunkSnapshot after, int cx, int cz, BlockPosition[] grass, BlockPosition[] exposed,
      BlockPosition covered, BlockPosition cover) {
    for (BlockPosition g : grass)
      require(at(after, g, cx, cz).equals(new BlockState(2, 0)), "source grass persist drift");
    for (BlockPosition p : exposed)
      require(at(after, p, cx, cz).equals(new BlockState(2, 0)), "exposed grass persist drift");
    require(at(after, covered, cx, cz).equals(new BlockState(3, 0)), "covered dirt persist drift");
    require(at(after, cover, cx, cz).equals(new BlockState(1, 0)), "cover stone persist drift");
  }

  static String cells(BlockPosition[] p) {
    StringBuilder s = new StringBuilder();
    for (int i = 0; i < p.length; i++) {
      if (i > 0)
        s.append('+');
      s.append(cell(p[i]));
    }
    return s.toString();
  }

  static int id(RemoteWorldView v, BlockPosition p) {
    return v.blockAt(p.x(), p.y(), p.z()).legacyId();
  }

  static BlockState at(RemoteChunkSnapshot c, BlockPosition p, int cx, int cz) {
    return c.blockAt(p.x() - cx * 16, p.y(), p.z() - cz * 16);
  }

  static boolean water(int id) {
    return id == 8 || id == 9;
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

  static String cell(BlockPosition p) {
    return p.x() + ":" + p.y() + ":" + p.z();
  }

  static String token(BlockPosition p, int id, int meta) {
    return cell(p) + ":" + id + ":" + meta;
  }

  static String sha(String s) throws Exception {
    byte[] b = MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));
    StringBuilder v = new StringBuilder();
    for (byte x : b) v.append(String.format("%02x", x & 255));
    return v.toString();
  }

  static void require(boolean v, String m) {
    if (!v)
      throw new IllegalStateException(m);
  }
}
