package worldline.smoke.farmlanddrysetb173;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import worldline.api.BlockFace;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.RemoteChunkSnapshot;
import worldline.api.RemoteWorldView;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173WireClient;

/** Raised-stone isolated dry farmland pad and bounded random-tick dry wait. */
public final class FarmlandDrySetArm {
  private FarmlandDrySetArm() {}

  static BlockPosition raise(B173WireClient a, RemoteChunkSnapshot initial, int cx, int cz, int[] column)
      throws Exception {
    BlockPosition top = foundation(initial, cx, cz);
    column[0] = 0;
    a.selectHeldSlot(0);
    while (water(at(initial, BlockFace.UP.adjacent(top), cx, cz).legacyId())) {
      top = place(a, top, BlockFace.UP, 1);
      a.moveAndObserve(0D, 1D, 0D, 1);
      require(++column[0] <= 15, "water column exceeded farmland-dry fixture");
    }
    for (int lift = 0; lift < 8; lift++) {
      top = place(a, top, BlockFace.UP, 1);
      a.moveAndObserve(0D, 1D, 0D, 1);
      column[0]++;
    }
    return top;
  }

  static BlockPosition roof(B173WireClient a, BlockPosition south) throws Exception {
    BlockPosition east = place(a, south, BlockFace.EAST, 1);
    BlockPosition pillar = place(a, east, BlockFace.UP, 1);
    pillar = place(a, pillar, BlockFace.UP, 1);
    pillar = place(a, pillar, BlockFace.UP, 1);
    return place(a, pillar, BlockFace.WEST, 1);
  }

  static BlockPosition place(B173WireClient a, BlockPosition support, BlockFace face, int id) throws Exception {
    BlockPosition target = face.adjacent(support);
    a.placeHeldBlock(support, face);
    a.awaitBlock(target, new BlockState(id, 0));
    return target;
  }

  static void till(B173WireClient a, BlockPosition dirt) throws Exception {
    a.useHeldItemOnBlock(dirt, BlockFace.UP);
    a.awaitBlock(dirt, new BlockState(60, 0));
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
    throw new IllegalStateException("no deterministic farmland-dry foundation");
  }

  static void waitDry(B173WireClient a, BlockPosition farm, BlockPosition cover, int window, int windows)
      throws Exception {
    RemoteWorldView dried = worldline.test.WorldlineSmokeAwait.awaitWorld(
        a, v -> id(v, farm) == 3, "unhydrated farmland dry", window * windows);
    require(id(dried, cover) == 1, "rain roof 1 lost during wait");
  }

  static void persist(RemoteChunkSnapshot after, int cx, int cz, BlockPosition top, BlockPosition farm,
      BlockPosition cover, BlockState dirt) {
    require(at(after, top, cx, cz).equals(new BlockState(1, 0)) && at(after, farm, cx, cz).equals(dirt)
            && at(after, cover, cx, cz).equals(new BlockState(1, 0)),
        "persisted farmland-dry dirt 3:0 drift");
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
