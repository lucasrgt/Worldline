package worldline.smoke.stickypistonbudsetb173;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import worldline.api.*;
import worldline.b173server.*;

/** West sticky 29 arm whose diagonal-above lever QC-primes without extending. */
public final class StickyPistonBudSetArm {
  final BlockPosition support, piston, head, pushed, power, lever, north, south, above;
  private StickyPistonBudSetArm(BlockPosition s, BlockPosition p, BlockPosition h, BlockPosition u, BlockPosition o,
      BlockPosition l, BlockPosition n, BlockPosition t, BlockPosition a) {
    support = s;
    piston = p;
    head = h;
    pushed = u;
    power = o;
    lever = l;
    north = n;
    south = t;
    above = a;
  }
  static StickyPistonBudSetArm place(
      B173WireClient a, RemoteChunkSnapshot initial, BlockPosition support, int cx, int cz) throws Exception {
    BlockPosition piston = BlockFace.UP.adjacent(support), head = BlockFace.WEST.adjacent(piston),
                  pushed = BlockFace.WEST.adjacent(head), above = BlockFace.UP.adjacent(piston),
                  east = BlockFace.EAST.adjacent(piston), power = BlockFace.UP.adjacent(east),
                  lever = BlockFace.SOUTH.adjacent(power), north = BlockFace.NORTH.adjacent(piston),
                  south = BlockFace.SOUTH.adjacent(piston);
    require(at(initial, piston, cx, cz).legacyId() == 0 && at(initial, head, cx, cz).legacyId() == 0
            && at(initial, pushed, cx, cz).legacyId() == 0 && at(initial, above, cx, cz).legacyId() == 0
            && at(initial, east, cx, cz).legacyId() == 0 && at(initial, power, cx, cz).legacyId() == 0
            && at(initial, lever, cx, cz).legacyId() == 0 && at(initial, north, cx, cz).legacyId() == 0
            && at(initial, south, cx, cz).legacyId() == 0,
        "sticky BUD targets were not initial air");
    require(manhattan(piston, lever) == 3 && lever.y() == piston.y() + 1 && lever.z() != piston.z(),
        "lever must be diagonal-above, not M547 east-of-qc");
    a.look(-90F, 0F);
    a.selectHeldSlot(1);
    a.placeHeldBlock(support, BlockFace.UP);
    BlockState placed = worldline.test.WorldlineSmokeAwait.awaitBlock(a, piston, new BlockState(29, 4), 5)
                            .blockAt(piston.x(), piston.y(), piston.z());
    require(placed.equals(new BlockState(29, 4)), "west sticky 29 absent: " + placed + " at " + cell(piston));
    a.selectHeldSlot(0);
    a.placeHeldBlock(piston, BlockFace.WEST);
    a.awaitBlock(head, new BlockState(1, 0));
    a.placeHeldBlock(piston, BlockFace.EAST);
    a.awaitBlock(east, new BlockState(1, 0));
    a.placeHeldBlock(east, BlockFace.UP);
    a.awaitBlock(power, new BlockState(1, 0));
    a.look(0F, 0F);
    a.selectHeldSlot(2);
    a.placeHeldBlock(power, BlockFace.SOUTH);
    require(worldline.test.WorldlineSmokeAwait.awaitBlock(a, lever, new BlockState(69, 3), 5)
                .blockAt(lever.x(), lever.y(), lever.z())
                .equals(new BlockState(69, 3)),
        "diagonal-above lever absent");
    return new StickyPistonBudSetArm(support, piston, head, pushed, power, lever, north, south, above);
  }
  RemoteWorldView charged(B173WireClient a, int ticks) throws Exception {
    RemoteWorldView live = worldline.test.WorldlineSmokeAwait.observe(a, ticks);
    require(retracted(live) && live.blockAt(lever.x(), lever.y(), lever.z()).equals(new BlockState(69, 3))
            && air(live, south) && air(live, north) && air(live, above) && !directPower(live),
        "BUD precondition drift: " + state(live));
    return live;
  }
  RemoteWorldView prime(B173WireClient a, int ticks) throws Exception {
    a.activateBlock(lever, BlockFace.UP);
    RemoteWorldView live = worldline.test.WorldlineSmokeAwait.observe(a, ticks);
    require(live.blockAt(lever.x(), lever.y(), lever.z()).equals(new BlockState(69, 11)) && retracted(live)
            && air(live, south) && !directPower(live),
        "diagonal lever caused immediate QC (M547), not BUD: " + state(live));
    return live;
  }
  RemoteWorldView trigger(B173WireClient a, int ticks) throws Exception {
    PlayerPose pose = a.moveAndObserve(0D, 0D, 0D, 1).resulting();
    pose = a.moveAndObserve(piston.x() + 0.5D - pose.x(), piston.y() + 2D - pose.y(), piston.z() + 0.5D - pose.z(), 2)
               .resulting();
    a.look(180F, 45F);
    a.selectHeldSlot(0);
    a.placeHeldBlock(piston, BlockFace.NORTH);
    RemoteWorldView live =
        worldline.test.WorldlineSmokeAwait.awaitWorld(a, v -> extended(v), "sticky BUD extension", Math.max(ticks, 24));
    require(extended(live) && live.blockAt(north.x(), north.y(), north.z()).equals(new BlockState(1, 0))
            && air(live, south) && live.blockAt(lever.x(), lever.y(), lever.z()).equals(new BlockState(69, 11))
            && !directPower(live),
        "BUD was not observed (no neighbor-update extend): " + state(live));
    return live;
  }
  RemoteWorldView unpower(B173WireClient a, int ticks) throws Exception {
    a.selectHeldSlot(3);
    a.activateBlock(lever, BlockFace.UP);
    RemoteWorldView live = worldline.test.WorldlineSmokeAwait.observe(a, ticks);
    require(live.blockAt(lever.x(), lever.y(), lever.z()).equals(new BlockState(69, 3)) && air(live, south)
            && !directPower(live),
        "unpower lost the diagonal lever: " + state(live));
    if (extended(live))
      return live;
    require(retracted(live), "unpower neither latched nor pulled: " + state(live));
    return live;
  }
  RemoteWorldView release(B173WireClient a, int ticks, RemoteWorldView live) throws Exception {
    if (retracted(live))
      return live;
    a.selectHeldSlot(0);
    a.placeHeldBlock(piston, BlockFace.UP);
    live = worldline.test.WorldlineSmokeAwait.awaitWorld(a, v -> retracted(v), "sticky BUD pull", Math.max(ticks, 24));
    require(retracted(live) && live.blockAt(above.x(), above.y(), above.z()).equals(new BlockState(1, 0))
            && air(live, south) && !directPower(live),
        "BUD-latched sticky 29 did not pull after neighbor update: " + state(live));
    return live;
  }
  void persist(RemoteChunkSnapshot after, int cx, int cz) {
    require(at(after, piston, cx, cz).equals(new BlockState(29, 4))
            && at(after, head, cx, cz).equals(new BlockState(1, 0))
            && at(after, pushed, cx, cz).equals(new BlockState(0, 0))
            && at(after, north, cx, cz).equals(new BlockState(1, 0))
            && at(after, south, cx, cz).equals(new BlockState(0, 0))
            && at(after, lever, cx, cz).equals(new BlockState(69, 3)) && !directPower(after, piston, cx, cz),
        "fresh sticky BUD pull drift");
  }
  boolean extended(RemoteWorldView v) {
    return v.blockAt(piston.x(), piston.y(), piston.z()).equals(new BlockState(29, 12))
        && v.blockAt(head.x(), head.y(), head.z()).equals(new BlockState(34, 12))
        && v.blockAt(pushed.x(), pushed.y(), pushed.z()).equals(new BlockState(1, 0));
  }
  boolean retracted(RemoteWorldView v) {
    return v.blockAt(piston.x(), piston.y(), piston.z()).equals(new BlockState(29, 4))
        && v.blockAt(head.x(), head.y(), head.z()).equals(new BlockState(1, 0))
        && v.blockAt(pushed.x(), pushed.y(), pushed.z()).equals(new BlockState(0, 0));
  }
  static boolean air(RemoteWorldView v, BlockPosition p) {
    return v.blockAt(p.x(), p.y(), p.z()).equals(new BlockState(0, 0));
  }
  static BlockPosition raise(B173WireClient a, RemoteChunkSnapshot initial, int cx, int cz, int[] column)
      throws Exception {
    BlockPosition top = foundation(initial, cx, cz);
    column[0] = 0;
    a.selectHeldSlot(0);
    while (water(at(initial, BlockFace.UP.adjacent(top), cx, cz).legacyId())) {
      top = place(a, top, BlockFace.UP, 1);
      a.moveAndObserve(0D, 1D, 0D, 1);
      require(++column[0] <= 15, "water column exceeded sticky-BUD fixture");
    }
    top = place(a, top, BlockFace.UP, 1);
    a.moveAndObserve(0D, 1D, 2D, 1);
    column[0]++;
    return top;
  }
  static BlockPosition place(B173WireClient a, BlockPosition support, BlockFace face, int id) throws Exception {
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
    throw new IllegalStateException("no deterministic sticky-BUD foundation");
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
  boolean directPower(RemoteWorldView w) {
    return directPower(w, piston);
  }
  static boolean directPower(RemoteChunkSnapshot c, BlockPosition p, int cx, int cz) {
    int[][] d = {{0, -1, 0}, {0, 1, 0}, {0, 0, -1}, {0, 0, 1}, {-1, 0, 0}, {1, 0, 0}};
    for (int[] v : d) {
      int id = at(c, new BlockPosition(p.x() + v[0], p.y() + v[1], p.z() + v[2]), cx, cz).legacyId();
      if (id == 55 || id == 69 || id == 75 || id == 76)
        return true;
    }
    return false;
  }
  static int manhattan(BlockPosition a, BlockPosition b) {
    return Math.abs(a.x() - b.x()) + Math.abs(a.y() - b.y()) + Math.abs(a.z() - b.z());
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
    for (byte x : b) v.append(String.format("%02x", x & 255));
    return v.toString();
  }
  static String cell(BlockPosition p) {
    return p.x() + ":" + p.y() + ":" + p.z();
  }
  String state(RemoteWorldView v) {
    if (v == null)
      return "null";
    return "piston=" + v.blockAt(piston.x(), piston.y(), piston.z()) + " head="
        + v.blockAt(head.x(), head.y(), head.z()) + " pushed=" + v.blockAt(pushed.x(), pushed.y(), pushed.z())
        + " lever=" + v.blockAt(lever.x(), lever.y(), lever.z()) + " north="
        + v.blockAt(north.x(), north.y(), north.z()) + " south=" + v.blockAt(south.x(), south.y(), south.z())
        + " above=" + v.blockAt(above.x(), above.y(), above.z());
  }
  static void require(boolean v, String m) {
    if (!v)
      throw new IllegalStateException(m);
  }
}
