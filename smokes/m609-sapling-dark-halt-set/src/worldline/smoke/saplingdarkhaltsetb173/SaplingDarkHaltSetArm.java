package worldline.smoke.saplingdarkhaltsetb173;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import worldline.api.*;
import worldline.b173server.*;

/** Raised-stone dirt pad, covered sapling, and bounded random-tick stage wait. */
public final class SaplingDarkHaltSetArm {
  static BlockPosition raise(B173WireClient a, RemoteChunkSnapshot initial, int cx, int cz, int[] column)
      throws Exception {
    BlockPosition top = foundation(initial, cx, cz);
    column[0] = 0;
    a.selectHeldSlot(0);
    while (water(at(initial, BlockFace.UP.adjacent(top), cx, cz).legacyId())) {
      top = place(a, top, BlockFace.UP, 1);
      a.moveAndObserve(0D, 1D, 0D, 1);
      require(++column[0] <= 15, "water column exceeded sapling-dark-halt fixture");
    }
    for (int lift = 0; lift < 8; lift++) {
      top = place(a, top, BlockFace.UP, 1);
      a.moveAndObserve(0D, 1D, 0D, 1);
      column[0]++;
    }
    return top;
  }

  static BlockPosition place(B173WireClient a, BlockPosition support, BlockFace face, int id) throws Exception {
    BlockPosition target = face.adjacent(support);
    a.placeHeldBlock(support, face);
    a.awaitBlock(target, new BlockState(id, 0));
    return target;
  }

  static BlockPosition[] plant(B173WireClient a, BlockPosition[] support, int id) throws Exception {
    BlockPosition[] out = new BlockPosition[support.length];
    for (int i = 0; i < support.length; i++) out[i] = place(a, support[i], BlockFace.UP, id);
    return out;
  }

  static BlockPosition foundation(RemoteChunkSnapshot c, int cx, int cz) {
    for (int x = 4; x <= 11; x++)
      for (int z = 4; z <= 11; z++)
        for (int y = 126; y >= 1; y--)
          if (c.blockAt(x, y, z).legacyId() == 3 && water(c.blockAt(x, y + 1, z).legacyId()))
            return new BlockPosition(cx * 16 + x, y, cz * 16 + z);
    throw new IllegalStateException("no deterministic sapling-dark-halt foundation");
  }

  static void waitStage(B173WireClient a, BlockPosition[] lit, BlockPosition covered, int window, int windows)
      throws Exception {
    RemoteWorldView grown = worldline.test.WorldlineSmokeAwait.awaitWorld(
        a, v -> anyStaged(v, lit) && sapling(v, covered, 0), "lit sapling stage", window * windows);
    require(sapling(grown, covered, 0), "covered sapling 6 left 6:0 during wait");
    for (int i = 0; i < lit.length; i++)
      require(id(grown, lit[i]) == 6 || id(grown, lit[i]) == 17, "lit sapling 6 died during wait");
  }

  static void persist(RemoteChunkSnapshot after, int cx, int cz, BlockPosition[] dirt, BlockPosition[] lit,
      BlockPosition covered, BlockPosition cover) {
    for (int i = 0; i < dirt.length; i++) require(at(after, dirt[i], cx, cz).legacyId() == 3, "dirt persist drift");
    require(at(after, covered, cx, cz).equals(new BlockState(6, 0)), "covered sapling persist drift");
    require(at(after, cover, cx, cz).equals(new BlockState(1, 0)), "cover stone persist drift");
    boolean staged = false;
    for (int i = 0; i < lit.length; i++) {
      BlockState s = at(after, lit[i], cx, cz);
      require(s.legacyId() == 6 || s.legacyId() == 17, "lit sapling persist drift");
      if (s.legacyId() == 17 || (s.legacyId() == 6 && (s.metadata() & 8) != 0))
        staged = true;
    }
    require(staged, "lit sapling stage did not persist");
  }

  static void requirePlanted(RemoteWorldView v, BlockPosition[] lit, BlockPosition covered, BlockPosition cover) {
    StringBuilder s = new StringBuilder(
        "cover=" + id(v, cover) + ":" + meta(v, cover) + ",covered=" + id(v, covered) + ":" + meta(v, covered));
    for (int i = 0; i < lit.length; i++)
      s.append(" lit").append(i).append("=").append(id(v, lit[i])).append(":").append(meta(v, lit[i]));
    require(id(v, cover) == 1 && sapling(v, covered, 0), "covered pad missing: " + s);
    for (int i = 0; i < lit.length; i++) require(id(v, lit[i]) == 6, "lit pad missing: " + s);
  }

  static boolean anyStaged(RemoteWorldView v, BlockPosition[] lit) {
    for (int i = 0; i < lit.length; i++) {
      int n = id(v, lit[i]);
      if (n == 17 || (n == 6 && (meta(v, lit[i]) & 8) != 0))
        return true;
    }
    return false;
  }

  static boolean sapling(RemoteWorldView v, BlockPosition p, int meta) {
    return id(v, p) == 6 && meta(v, p) == meta;
  }

  static int meta(RemoteWorldView v, BlockPosition p) {
    return v.blockAt(p.x(), p.y(), p.z()).metadata();
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
    for (int i = 0; i < b.length; i++) v.append(String.format("%02x", b[i] & 255));
    return v.toString();
  }

  static void require(boolean v, String m) {
    if (!v)
      throw new IllegalStateException(m);
  }
}
