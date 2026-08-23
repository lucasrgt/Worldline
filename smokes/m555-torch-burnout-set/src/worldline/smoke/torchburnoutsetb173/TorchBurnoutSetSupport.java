package worldline.smoke.torchburnoutsetb173;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import worldline.api.*;
import worldline.b173server.*;

/** Smoke-local raised-stone helpers cloned from the M312 invert fixture. */
final class TorchBurnoutSetSupport {
  static final BlockState AIR = new BlockState(0, 0);
  static final BlockState WOOL = new BlockState(35, 0);

  private TorchBurnoutSetSupport() {
  }

  static BlockPosition place(B173WireClient actor, BlockPosition support, BlockFace face, int id)
      throws Exception {
    BlockPosition target = face.adjacent(support);
    actor.placeHeldBlock(support, face);
    actor.awaitBlock(target, new BlockState(id, 0));
    return target;
  }

  static BlockPosition foundation(RemoteChunkSnapshot chunk, int cx, int cz) {
    for (int x = 4; x <= 11; x++)
      for (int z = 4; z <= 11; z++)
        for (int y = 126; y >= 1; y--)
          if (chunk.blockAt(x, y, z).legacyId() == 3
              && water(chunk.blockAt(x, y + 1, z).legacyId()))
            return new BlockPosition(cx * 16 + x, y, cz * 16 + z);
    throw new IllegalStateException("no deterministic torch burnout foundation");
  }

  static BlockPosition raise(B173WireClient actor, RemoteChunkSnapshot initial, int cx, int cz,
      int[] column) throws Exception {
    BlockPosition top = foundation(initial, cx, cz);
    column[0] = 0;
    actor.selectHeldSlot(0);
    while (water(initial.blockAt(local(top.x(), cx), top.y() + 1, local(top.z(), cz)).legacyId())) {
      top = place(actor, top, BlockFace.UP, 1);
      actor.moveAndObserve(0D, 1D, 0D, 1);
      require(++column[0] <= 15, "water column exceeded torch burnout fixture");
    }
    for (int lift = 0; lift < 8; lift++) {
      top = place(actor, top, BlockFace.UP, 1);
      actor.moveAndObserve(0D, 1D, 0D, 1);
      column[0]++;
    }
    return top;
  }

  static RemoteWorldView pokePlace(B173WireClient actor, BlockPosition torch, BlockPosition cell)
      throws Exception {
    actor.selectHeldSlot(4);
    actor.placeHeldBlock(torch, BlockFace.WEST);
    actor.awaitBlock(cell, WOOL);
    actor.selectHeldSlot(7);
    return actor.sustainTicks(6);
  }

  static RemoteWorldView pokeBreak(B173WireClient actor, BlockPosition cell) throws Exception {
    actor.selectHeldSlot(5);
    actor.beginBreak(cell);
    actor.sustainTicks(1);
    actor.finishBreak(cell);
    actor.awaitBlock(cell, AIR);
    actor.selectHeldSlot(7);
    return actor.sustainTicks(1);
  }

  static RemoteWorldView dropSand(B173WireClient actor, BlockPosition torch) throws Exception {
    actor.selectHeldSlot(6);
    actor.placeHeldBlock(torch, BlockFace.NORTH);
    actor.selectHeldSlot(7);
    return actor.sustainTicks(4);
  }

  static boolean water(int id) {
    return id == 8 || id == 9;
  }

  static int local(int value, int chunk) {
    return value - chunk * 16;
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

  static String sha(String text) throws Exception {
    byte[] digest =
        MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8));
    StringBuilder hex = new StringBuilder();
    for (byte b : digest)
      hex.append(String.format("%02x", b & 255));
    return hex.toString();
  }

  static void require(boolean value, String message) {
    if (!value)
      throw new IllegalStateException(message);
  }
}
