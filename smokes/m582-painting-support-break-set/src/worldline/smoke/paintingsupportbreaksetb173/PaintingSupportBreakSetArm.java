package worldline.smoke.paintingsupportbreaksetb173;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import worldline.api.*;
import worldline.b173server.*;

/** Raised 2x2 stone wall, Packet14 support break, and Packet29/Packet21 waits. */
public final class PaintingSupportBreakSetArm {
  private PaintingSupportBreakSetArm() {}
  static BlockPosition raise(B173WireClient a, RemoteChunkSnapshot initial, int cx, int cz, int[] column)
      throws Exception {
    BlockPosition top = foundation(initial, cx, cz);
    column[0] = 0;
    a.selectHeldSlot(0);
    while (water(at(initial, BlockFace.UP.adjacent(top), cx, cz).legacyId())) {
      top = place(a, top, BlockFace.UP, 1);
      a.moveAndObserve(0D, 1D, 0D, 1);
      require(++column[0] <= 15, "water column exceeded painting-support-break fixture");
    }
    for (int lift = 0; lift < 8; lift++) {
      top = place(a, top, BlockFace.UP, 1);
      a.moveAndObserve(0D, 1D, 0D, 1);
      column[0]++;
    }
    return top;
  }
  static BlockPosition[] wall(B173WireClient a, BlockPosition top) throws Exception {
    BlockPosition support = place(a, top, BlockFace.EAST, 1), south = place(a, support, BlockFace.UP, 1),
                  north = place(a, south, BlockFace.SOUTH, 1), southTop = place(a, south, BlockFace.UP, 1);
    place(a, north, BlockFace.UP, 1);
    return new BlockPosition[] {south, southTop, north};
  }
  static void harvest(B173WireClient a, BlockPosition support) throws Exception {
    a.selectHeldSlot(2);
    a.beginBreak(support);
    worldline.test.WorldlineSmokeAwait.observe(a, 20);
    a.finishBreak(support);
    a.awaitBlock(support, new BlockState(0, 0));
  }
  static RemoteDroppedItem drop(B173WireClient a, RemoteItemStack expected) {
    return worldline.test.WorldlineSmokeAwait.awaitEntity(a,
        ()
            -> a.peekDroppedItem(expected),
        value -> value != null && value.item().equals(expected), "painting Packet21 drop", 40);
  }
  static int gone(B173WireClient a, int entity) {
    return worldline.test.WorldlineSmokeAwait
        .awaitEntity(a,
            () -> B173PaintingAccess.peekDestroy(a, entity), value -> value != null, "painting Packet29 destroy", 160)
        .intValue();
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
    throw new IllegalStateException("no deterministic painting-support-break foundation");
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
    for (byte x : b) v.append(String.format("%02x", x & 255));
    return v.toString();
  }
  static void require(boolean v, String m) {
    if (!v)
      throw new IllegalStateException(m);
  }
}
