package worldline.smoke.portalscalesetb173;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import worldline.api.*;
import worldline.b173server.*;

/** Shared portal-scale placement, foundation, and movement helpers. */
final class PortalScaleSetSupport {
  private PortalScaleSetSupport() {
  }

  static BlockPosition place(B173WireClient actor, BlockPosition support, BlockFace face, int id)
      throws Exception {
    BlockPosition target = face.adjacent(support);
    actor.placeHeldBlock(support, face);
    actor.awaitBlock(target, new BlockState(id, 0));
    return target;
  }

  static BlockPosition foundation(RemoteChunkSnapshot chunk, int cx, int cz) {
    for (int x = 4; x <= 10; x++)
      for (int z = 4; z <= 11; z++)
        for (int y = 120; y >= 40; y--) {
          if (support(chunk.blockAt(x, y, z).legacyId()) && clear(chunk, x, y, z))
            return new BlockPosition(cx * 16 + x, y, cz * 16 + z);
        }
    throw new IllegalStateException(
        "no deterministic portal-scale foundation in chunk " + cx + ":" + cz);
  }

  private static boolean clear(RemoteChunkSnapshot chunk, int x, int y, int z) {
    int water = 0;
    while (
        water < 15 && y + 1 + water < 128 && water(chunk.blockAt(x, y + 1 + water, z).legacyId()))
      water++;
    int height = water + 6;
    if (y + height >= 128)
      return false;
    for (int dx = 0; dx < 4; dx++)
      for (int dy = 1; dy <= height; dy++) {
        int id = chunk.blockAt(x + dx, y + dy, z).legacyId();
        if (id != 0 && !water(id))
          return false;
      }
    return true;
  }

  private static boolean support(int id) {
    return id == 1 || id == 2 || id == 3 || id == 12 || id == 13 || id == 24;
  }

  static int sky(RemoteChunkSnapshot chunk) {
    int n = 0;
    for (int x = 0; x < 16; x++)
      for (int z = 0; z < 16; z++)
        for (int y = 0; y < 128; y++)
          if (chunk.skyLightAt(x, y, z) > 0)
            n++;
    return n;
  }

  static boolean water(int id) {
    return id == 8 || id == 9;
  }
  static int local(int v, int c) {
    return v - c * 16;
  }
  static int floor(double v) {
    return (int) Math.floor(v);
  }

  static void awaitPlayers(B173DedicatedServer server, int n) throws Exception {
    long end = System.currentTimeMillis() + 5000;
    while (System.currentTimeMillis() < end) {
      if (server.players().size() == n)
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
