package worldline.smoke.doubleextendersetb173;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import worldline.api.*;
import worldline.b173server.*;

/** Raised west-facing sticky-29 plus regular-33 chain from the M142/M367 family. */
public final class DoubleExtenderSetArm {
  final BlockPosition rear, front0, payload0, payload1, payload2, rearLever, frontLever;
  private DoubleExtenderSetArm(BlockPosition r, BlockPosition f, BlockPosition p0, BlockPosition p1,
      BlockPosition p2, BlockPosition rl, BlockPosition fl) {
    rear = r;
    front0 = f;
    payload0 = p0;
    payload1 = p1;
    payload2 = p2;
    rearLever = rl;
    frontLever = fl;
  }
  static DoubleExtenderSetArm build(B173WireClient a, RemoteChunkSnapshot initial,
      BlockPosition support, int cx, int cz) throws Exception {
    BlockPosition rear = BlockFace.UP.adjacent(support), front0 = BlockFace.WEST.adjacent(rear),
                  payload0 = BlockFace.WEST.adjacent(front0);
    BlockPosition payload1 = BlockFace.WEST.adjacent(payload0),
                  payload2 = BlockFace.WEST.adjacent(payload1);
    BlockPosition rearLever = BlockFace.EAST.adjacent(support),
                  westPad = BlockFace.WEST.adjacent(support),
                  frontPad = BlockFace.WEST.adjacent(westPad);
    BlockPosition frontLever = BlockFace.SOUTH.adjacent(frontPad);
    require(air(initial, rear, cx, cz) && air(initial, front0, cx, cz)
            && air(initial, payload0, cx, cz) && air(initial, payload1, cx, cz)
            && air(initial, payload2, cx, cz),
        "double-extender cells were not initial air");
    a.look(-90F, 0F);
    a.selectHeldSlot(0);
    place(a, support, BlockFace.WEST, 1);
    place(a, westPad, BlockFace.WEST, 1);
    a.moveAndObserve(-2D, 0D, 0D, 2);
    a.selectHeldSlot(1);
    a.placeHeldBlock(support, BlockFace.UP);
    worldline.test.WorldlineSmokeAwait.awaitBlock(a, rear, new BlockState(29, 4), 5);
    a.selectHeldSlot(2);
    a.placeHeldBlock(westPad, BlockFace.UP);
    worldline.test.WorldlineSmokeAwait.awaitBlock(a, front0, new BlockState(33, 4), 5);
    a.selectHeldSlot(4);
    a.placeHeldBlock(front0, BlockFace.WEST);
    a.awaitBlock(payload0, new BlockState(4, 0));
    a.selectHeldSlot(3);
    a.placeHeldBlock(support, BlockFace.EAST);
    worldline.test.WorldlineSmokeAwait.awaitBlock(a, rearLever, new BlockState(69, 1), 5);
    a.placeHeldBlock(frontPad, BlockFace.SOUTH);
    worldline.test.WorldlineSmokeAwait.awaitBlock(a, frontLever, new BlockState(69, 3), 5);
    return new DoubleExtenderSetArm(
        rear, front0, payload0, payload1, payload2, rearLever, frontLever);
  }
  RemoteWorldView pulse(B173WireClient a, BlockPosition lever, int ticks, BlockState leverWant,
      String label, BlockPosition[] cells, BlockState[] want) throws Exception {
    a.activateBlock(lever, BlockFace.UP);
    RemoteWorldView live = worldline.test.WorldlineSmokeAwait.awaitWorld(
        a, v -> matches(v, lever, leverWant, cells, want), label, ticks);
    require(
        live.blockAt(lever.x(), lever.y(), lever.z()).equals(leverWant), label + " lever drift");
    StringBuilder seen = new StringBuilder();
    for (int i = 0; i < cells.length; i++) {
      BlockState got = live.blockAt(cells[i].x(), cells[i].y(), cells[i].z());
      if (seen.length() > 0)
        seen.append('/');
      seen.append(got.legacyId()).append(':').append(got.metadata());
      require(got.equals(want[i]), label + " absent: " + seen);
    }
    return live;
  }
  private static boolean matches(RemoteWorldView view, BlockPosition lever, BlockState leverWant,
      BlockPosition[] cells, BlockState[] want) {
    if (!view.blockAt(lever.x(), lever.y(), lever.z()).equals(leverWant))
      return false;
    for (int i = 0; i < cells.length; i++) {
      BlockPosition cell = cells[i];
      if (!view.blockAt(cell.x(), cell.y(), cell.z()).equals(want[i]))
        return false;
    }
    return true;
  }
  void persist(RemoteChunkSnapshot after, int cx, int cz, BlockPosition[] cells, BlockState[] want,
      String label) {
    for (int i = 0; i < cells.length; i++)
      require(at(after, cells[i], cx, cz).equals(want[i]), label + " " + cell(cells[i]));
  }
  static BlockPosition raise(B173WireClient a, RemoteChunkSnapshot initial, int cx, int cz,
      int[] column) throws Exception {
    BlockPosition top = foundation(initial, cx, cz);
    column[0] = 0;
    a.selectHeldSlot(0);
    while (water(at(initial, BlockFace.UP.adjacent(top), cx, cz).legacyId())) {
      top = place(a, top, BlockFace.UP, 1);
      a.moveAndObserve(0D, 1D, 0D, 1);
      require(++column[0] <= 15, "water column exceeded double-extender fixture");
    }
    top = place(a, top, BlockFace.UP, 1);
    a.moveAndObserve(0D, 1D, 2D, 1);
    column[0]++;
    return top;
  }
  static BlockPosition place(B173WireClient a, BlockPosition support, BlockFace face, int id)
      throws Exception {
    BlockPosition target = face.adjacent(support);
    a.placeHeldBlock(support, face);
    a.awaitBlock(target, new BlockState(id, 0));
    return target;
  }
  static BlockPosition foundation(RemoteChunkSnapshot c, int cx, int cz) {
    for (int x = 4; x <= 11; x++)
      for (int z = 4; z <= 11; z++)
        for (int y = 126; y >= 1; y--)
          if (c.blockAt(x, y, z).legacyId() == 3 && water(c.blockAt(x, y + 1, z).legacyId()))
            return new BlockPosition(cx * 16 + x, y, cz * 16 + z);
    throw new IllegalStateException("no deterministic double-extender foundation");
  }
  static BlockState at(RemoteChunkSnapshot c, BlockPosition p, int cx, int cz) {
    return c.blockAt(p.x() - cx * 16, p.y(), p.z() - cz * 16);
  }
  static boolean air(RemoteChunkSnapshot c, BlockPosition p, int cx, int cz) {
    return at(c, p, cx, cz).legacyId() == 0;
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
