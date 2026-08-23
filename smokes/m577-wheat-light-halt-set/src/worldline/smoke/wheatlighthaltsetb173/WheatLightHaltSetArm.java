package worldline.smoke.wheatlighthaltsetb173;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import worldline.api.*;
import worldline.b173server.*;

/** Raised-stone farmland pad and bounded random-tick wheat-age wait. */
public final class WheatLightHaltSetArm {
  static BlockPosition raise(B173WireClient a, RemoteChunkSnapshot initial, int cx, int cz, int[] column)
      throws Exception {
    BlockPosition top = foundation(initial, cx, cz);
    column[0] = 0;
    a.selectHeldSlot(0);
    while (water(at(initial, BlockFace.UP.adjacent(top), cx, cz).legacyId())) {
      top = place(a, top, BlockFace.UP, 1);
      a.moveAndObserve(0D, 1D, 0D, 1);
      require(++column[0] <= 15, "water column exceeded wheat-light-halt fixture");
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

  static void till(B173WireClient a, BlockPosition[] plots) throws Exception {
    for (int i = 0; i < plots.length; i++) {
      a.useHeldItemOnBlock(plots[i], BlockFace.UP);
      a.awaitBlock(plots[i], new BlockState(60, 0));
    }
  }

  static BlockPosition sow(B173WireClient a, BlockPosition soil) throws Exception {
    BlockPosition crop = BlockFace.UP.adjacent(soil);
    a.useHeldItemOnBlock(soil, BlockFace.UP);
    a.awaitBlock(crop, new BlockState(59, 0));
    return crop;
  }

  static BlockPosition foundation(RemoteChunkSnapshot c, int cx, int cz) {
    for (int x = 4; x <= 11; x++)
      for (int z = 4; z <= 11; z++)
        for (int y = 126; y >= 1; y--)
          if (c.blockAt(x, y, z).legacyId() == 3 && water(c.blockAt(x, y + 1, z).legacyId()))
            return new BlockPosition(cx * 16 + x, y, cz * 16 + z);
    throw new IllegalStateException("no deterministic wheat-light-halt foundation");
  }

  static void waitAge(B173WireClient a, BlockPosition[] lit, BlockPosition covered, int window, int windows)
      throws Exception {
    RemoteWorldView grown = worldline.test.WorldlineSmokeAwait.awaitWorld(
        a, v -> anyAged(v, lit) && crop(v, covered, 0), "lit wheat age", window * windows);
    require(crop(grown, covered, 0), "covered wheat 59 aged during wait");
    for (int i = 0; i < lit.length; i++) require(id(grown, lit[i]) == 59, "lit wheat 59 died during wait");
  }

  static void persist(RemoteChunkSnapshot after, int cx, int cz, BlockPosition[] plots, BlockPosition[] lit,
      BlockPosition covered, BlockPosition cover, BlockPosition water) {
    for (int i = 0; i < plots.length; i++)
      require(at(after, plots[i], cx, cz).legacyId() == 60, "farmland persist drift");
    require(at(after, covered, cx, cz).equals(new BlockState(59, 0)), "covered wheat persist drift");
    require(at(after, cover, cx, cz).equals(new BlockState(1, 0)), "cover stone persist drift");
    require(at(after, water, cx, cz).equals(new BlockState(9, 0)), "water persist drift");
    boolean aged = false;
    for (int i = 0; i < lit.length; i++) {
      BlockState s = at(after, lit[i], cx, cz);
      require(s.legacyId() == 59, "lit wheat persist drift");
      if (s.metadata() >= 1)
        aged = true;
    }
    require(aged, "lit wheat age did not persist");
  }

  static boolean crops(RemoteWorldView v, BlockPosition[] lit, BlockPosition covered, BlockPosition cover) {
    if (id(v, cover) != 1 || !crop(v, covered, 0))
      return false;
    for (int i = 0; i < lit.length; i++)
      if (id(v, lit[i]) != 59)
        return false;
    return true;
  }

  static String dump(RemoteWorldView v, BlockPosition[] lit, BlockPosition covered, BlockPosition cover) {
    StringBuilder s = new StringBuilder();
    s.append("cover=")
        .append(id(v, cover))
        .append(",covered=")
        .append(id(v, covered))
        .append(':')
        .append(meta(v, covered));
    for (int i = 0; i < lit.length; i++)
      s.append(",lit").append(i).append('=').append(id(v, lit[i])).append(':').append(meta(v, lit[i]));
    return s.toString();
  }

  static boolean anyAged(RemoteWorldView v, BlockPosition[] lit) {
    for (int i = 0; i < lit.length; i++)
      if (id(v, lit[i]) == 59 && meta(v, lit[i]) >= 1)
        return true;
    return false;
  }

  static boolean crop(RemoteWorldView v, BlockPosition p, int meta) {
    return id(v, p) == 59 && meta(v, p) == meta;
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

  static String cells(BlockPosition[] p) {
    StringBuilder s = new StringBuilder();
    for (int i = 0; i < p.length; i++) {
      if (i > 0)
        s.append('+');
      s.append(cell(p[i]));
    }
    return s.toString();
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
