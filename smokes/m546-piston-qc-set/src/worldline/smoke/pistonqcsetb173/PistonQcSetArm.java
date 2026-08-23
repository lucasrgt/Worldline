package worldline.smoke.pistonqcsetb173;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import worldline.api.*;
import worldline.b173server.*;

/** West-facing piston with the lever on the block above, not on the piston cell. */
public final class PistonQcSetArm {
  final BlockPosition support, piston, head, above, lever;
  private PistonQcSetArm(
      BlockPosition s, BlockPosition p, BlockPosition h, BlockPosition a, BlockPosition l) {
    support = s;
    piston = p;
    head = h;
    above = a;
    lever = l;
  }
  static PistonQcSetArm place(B173WireClient a, RemoteChunkSnapshot initial, BlockPosition support,
      int cx, int cz) throws Exception {
    BlockPosition piston = BlockFace.UP.adjacent(support), head = BlockFace.WEST.adjacent(piston),
                  above = BlockFace.UP.adjacent(piston), lever = BlockFace.EAST.adjacent(above);
    require(at(initial, piston, cx, cz).legacyId() == 0 && at(initial, head, cx, cz).legacyId() == 0
            && at(initial, above, cx, cz).legacyId() == 0
            && at(initial, lever, cx, cz).legacyId() == 0,
        "piston QC targets were not initial air");
    require(manhattan(piston, lever) > 1, "QC lever must not be adjacent to the piston cell");
    a.look(-90F, 0F);
    a.selectHeldSlot(1);
    a.placeHeldBlock(support, BlockFace.UP);
    BlockState placed =
        worldline.test.WorldlineSmokeAwait.awaitBlock(a, piston, new BlockState(33, 4), 5)
            .blockAt(piston.x(), piston.y(), piston.z());
    require(placed.equals(new BlockState(33, 4)),
        "west piston 33 absent: " + placed + " at " + cell(piston));
    a.selectHeldSlot(0);
    a.placeHeldBlock(piston, BlockFace.UP);
    a.awaitBlock(above, new BlockState(1, 0));
    a.selectHeldSlot(2);
    a.placeHeldBlock(above, BlockFace.EAST);
    require(worldline.test.WorldlineSmokeAwait.awaitBlock(a, lever, new BlockState(69, 1), 5)
                .blockAt(lever.x(), lever.y(), lever.z())
                .equals(new BlockState(69, 1)),
        "lever absent on the above-block");
    return new PistonQcSetArm(support, piston, head, above, lever);
  }
  RemoteWorldView pulse(B173WireClient a, int ticks, BlockState pistonWant, BlockState headWant,
      int leverMeta, String label) throws Exception {
    a.activateBlock(lever, BlockFace.UP);
    RemoteWorldView live = worldline.test.WorldlineSmokeAwait.observe(a, ticks);
    require(live.blockAt(lever.x(), lever.y(), lever.z()).equals(new BlockState(69, leverMeta))
            && live.blockAt(above.x(), above.y(), above.z()).equals(new BlockState(1, 0))
            && live.blockAt(piston.x(), piston.y(), piston.z()).equals(pistonWant)
            && live.blockAt(head.x(), head.y(), head.z()).equals(headWant)
            && !directPower(live, piston),
        label + " absent: " + live.blockAt(piston.x(), piston.y(), piston.z()) + "/"
            + live.blockAt(head.x(), head.y(), head.z()) + "/"
            + live.blockAt(lever.x(), lever.y(), lever.z()));
    return live;
  }
  void persist(RemoteChunkSnapshot after, int cx, int cz, BlockState pistonWant,
      BlockState headWant, String label) {
    require(at(after, lever, cx, cz).equals(new BlockState(69, 1))
            && at(after, above, cx, cz).equals(new BlockState(1, 0))
            && at(after, piston, cx, cz).equals(pistonWant)
            && at(after, head, cx, cz).equals(headWant) && !directPower(after, piston, cx, cz),
        label);
  }
  static BlockPosition raise(B173WireClient a, RemoteChunkSnapshot initial, int cx, int cz,
      int[] column) throws Exception {
    BlockPosition top = foundation(initial, cx, cz);
    column[0] = 0;
    a.selectHeldSlot(0);
    while (water(at(initial, BlockFace.UP.adjacent(top), cx, cz).legacyId())) {
      top = place(a, top, BlockFace.UP, 1);
      a.moveAndObserve(0D, 1D, 0D, 1);
      require(++column[0] <= 15, "water column exceeded piston-qc fixture");
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
    throw new IllegalStateException("no deterministic piston-qc foundation");
  }
  static BlockState at(RemoteChunkSnapshot c, BlockPosition p, int cx, int cz) {
    return c.blockAt(p.x() - cx * 16, p.y(), p.z() - cz * 16);
  }
  static boolean water(int id) {
    return id == 8 || id == 9;
  }
  static boolean directPower(RemoteWorldView w, BlockPosition p) {
    int[][] d = {{0, -1, 0}, {0, 1, 0}, {0, 0, -1}, {0, 0, 1}, {-1, 0, 0}, {1, 0, 0}};
    for (int[] v : d) {
      int id = w.blockAt(p.x() + v[0], p.y() + v[1], p.z() + v[2]).legacyId();
      if (id == 55 || id == 69 || id == 75 || id == 76)
        return true;
    }
    return false;
  }
  static boolean directPower(RemoteChunkSnapshot c, BlockPosition p, int cx, int cz) {
    int[][] d = {{0, -1, 0}, {0, 1, 0}, {0, 0, -1}, {0, 0, 1}, {-1, 0, 0}, {1, 0, 0}};
    for (int[] v : d) {
      int id =
          at(c, new BlockPosition(p.x() + v[0], p.y() + v[1], p.z() + v[2]), cx, cz).legacyId();
      if (id == 55 || id == 69 || id == 75 || id == 76)
        return true;
    }
    return false;
  }
  static int manhattan(BlockPosition a, BlockPosition b) {
    return Math.abs(a.x() - b.x()) + Math.abs(a.y() - b.y()) + Math.abs(a.z() - b.z());
  }
  static String cell(BlockPosition p) {
    return p.x() + ":" + p.y() + ":" + p.z();
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
