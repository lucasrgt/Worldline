package worldline.smoke.extendedheadbreaksetb173;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import worldline.api.*;
import worldline.b173server.*;

/** Cloned west-facing piston+stone+lever arm from the M367/M142 family. */
public final class ExtendedHeadBreakSetArm {
  final BlockPosition support, piston, head, pushed, lever;
  private ExtendedHeadBreakSetArm(
      BlockPosition s, BlockPosition p, BlockPosition h, BlockPosition u, BlockPosition l) {
    support = s;
    piston = p;
    head = h;
    pushed = u;
    lever = l;
  }
  static ExtendedHeadBreakSetArm place(B173WireClient a, RemoteChunkSnapshot initial,
      BlockPosition support, int cx, int cz) throws Exception {
    BlockPosition piston = BlockFace.UP.adjacent(support), head = BlockFace.WEST.adjacent(piston),
                  pushed = BlockFace.WEST.adjacent(head), lever = BlockFace.EAST.adjacent(support);
    require(at(initial, piston, cx, cz).legacyId() == 0 && at(initial, head, cx, cz).legacyId() == 0
            && at(initial, pushed, cx, cz).legacyId() == 0
            && at(initial, lever, cx, cz).legacyId() == 0,
        "piston 33 targets were not initial air");
    a.look(-90F, 0F);
    a.selectHeldSlot(1);
    a.placeHeldBlock(support, BlockFace.UP);
    worldline.test.WorldlineSmokeAwait.awaitBlock(a, piston, new BlockState(33, 4), 5);
    a.selectHeldSlot(0);
    a.placeHeldBlock(piston, BlockFace.WEST);
    a.awaitBlock(head, new BlockState(1, 0));
    a.selectHeldSlot(2);
    a.placeHeldBlock(support, BlockFace.EAST);
    worldline.test.WorldlineSmokeAwait.awaitBlock(a, lever, new BlockState(69, 1), 5);
    return new ExtendedHeadBreakSetArm(support, piston, head, pushed, lever);
  }
  RemoteWorldView extend(B173WireClient a, int ticks) throws Exception {
    a.selectHeldSlot(4);
    a.activateBlock(lever, BlockFace.UP);
    RemoteWorldView live = worldline.test.WorldlineSmokeAwait.awaitWorld(a,
        v
        -> v.blockAt(lever.x(), lever.y(), lever.z()).equals(new BlockState(69, 9))
            && v.blockAt(piston.x(), piston.y(), piston.z()).equals(new BlockState(33, 12))
            && v.blockAt(head.x(), head.y(), head.z()).equals(new BlockState(34, 4))
            && v.blockAt(pushed.x(), pushed.y(), pushed.z()).equals(new BlockState(1, 0)),
        "piston 33 extension", ticks);
    require(live.blockAt(lever.x(), lever.y(), lever.z()).equals(new BlockState(69, 9))
            && live.blockAt(piston.x(), piston.y(), piston.z()).equals(new BlockState(33, 12))
            && live.blockAt(head.x(), head.y(), head.z()).equals(new BlockState(34, 4))
            && live.blockAt(pushed.x(), pushed.y(), pushed.z()).equals(new BlockState(1, 0)),
        "piston 33 extend absent: " + live.blockAt(piston.x(), piston.y(), piston.z()) + "/"
            + live.blockAt(head.x(), head.y(), head.z()) + "/"
            + live.blockAt(pushed.x(), pushed.y(), pushed.z()));
    return live;
  }
  RemoteDroppedItem breakBase(B173WireClient a, int ticks) throws Exception {
    BlockState air = new BlockState(0, 0);
    RemoteItemStack drop = new RemoteItemStack(33, 1, 0);
    a.selectHeldSlot(3);
    a.beginBreak(piston);
    worldline.test.WorldlineSmokeAwait.observe(a, ticks);
    a.finishBreak(piston);
    a.awaitBlock(piston, air);
    a.awaitBlock(head, air);
    RemoteDroppedItem item = a.awaitDroppedItem(drop);
    require(item.item().equals(drop) && item.item().legacyId() == 33 && item.item().count() == 1,
        "Packet21 piston 33 drop absent");
    RemoteWorldView live = worldline.test.WorldlineSmokeAwait.observe(a, 5);
    require(live.blockAt(piston.x(), piston.y(), piston.z()).equals(air)
            && live.blockAt(head.x(), head.y(), head.z()).equals(air)
            && live.blockAt(pushed.x(), pushed.y(), pushed.z()).equals(new BlockState(1, 0))
            && live.blockAt(lever.x(), lever.y(), lever.z()).equals(new BlockState(69, 9)),
        "extended-base leftover drift: " + live.blockAt(piston.x(), piston.y(), piston.z()) + "/"
            + live.blockAt(head.x(), head.y(), head.z()));
    return item;
  }
  void persist(RemoteChunkSnapshot after, int cx, int cz) {
    require(at(after, piston, cx, cz).equals(new BlockState(0, 0))
            && at(after, head, cx, cz).equals(new BlockState(0, 0))
            && at(after, pushed, cx, cz).equals(new BlockState(1, 0))
            && at(after, lever, cx, cz).equals(new BlockState(69, 9)),
        "fresh extended-head-break leftover drift");
  }
  static BlockPosition raise(B173WireClient a, RemoteChunkSnapshot initial, int cx, int cz,
      int[] column) throws Exception {
    BlockPosition top = foundation(initial, cx, cz);
    column[0] = 0;
    a.selectHeldSlot(0);
    while (water(at(initial, BlockFace.UP.adjacent(top), cx, cz).legacyId())) {
      top = place(a, top, BlockFace.UP, 1);
      a.moveAndObserve(0D, 1D, 0D, 1);
      require(++column[0] <= 15, "water column exceeded extended-head-break fixture");
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
    throw new IllegalStateException("no deterministic extended-head-break foundation");
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
