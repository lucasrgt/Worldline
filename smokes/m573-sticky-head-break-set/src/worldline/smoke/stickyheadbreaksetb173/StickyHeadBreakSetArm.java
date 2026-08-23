package worldline.smoke.stickyheadbreaksetb173;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import worldline.api.BlockFace;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.RemoteChunkSnapshot;
import worldline.api.RemoteDroppedItem;
import worldline.api.RemoteItemStack;
import worldline.api.RemoteWorldView;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173WireClient;
import worldline.test.WorldlineSmokeAwait;

/** Cloned west sticky-29 arm; Packet14 harvests head 34, not the extended base. */
public final class StickyHeadBreakSetArm {
  final BlockPosition support;
  final BlockPosition piston;
  final BlockPosition head;
  final BlockPosition pushed;
  final BlockPosition lever;

  private StickyHeadBreakSetArm(
      BlockPosition support, BlockPosition piston, BlockPosition head, BlockPosition pushed, BlockPosition lever) {
    this.support = support;
    this.piston = piston;
    this.head = head;
    this.pushed = pushed;
    this.lever = lever;
  }

  static StickyHeadBreakSetArm place(B173WireClient actor, RemoteChunkSnapshot initial, BlockPosition support,
      int chunkX, int chunkZ) throws Exception {
    BlockPosition piston = BlockFace.UP.adjacent(support);
    BlockPosition head = BlockFace.WEST.adjacent(piston);
    BlockPosition pushed = BlockFace.WEST.adjacent(head);
    BlockPosition lever = BlockFace.EAST.adjacent(support);
    require(at(initial, piston, chunkX, chunkZ).legacyId() == 0 && at(initial, head, chunkX, chunkZ).legacyId() == 0
            && at(initial, pushed, chunkX, chunkZ).legacyId() == 0
            && at(initial, lever, chunkX, chunkZ).legacyId() == 0,
        "sticky 29 targets were not initial air");
    actor.look(-90F, 0F);
    actor.selectHeldSlot(1);
    actor.placeHeldBlock(support, BlockFace.UP);
    WorldlineSmokeAwait.awaitBlock(actor, piston, new BlockState(29, 4), 5);
    actor.selectHeldSlot(0);
    actor.placeHeldBlock(piston, BlockFace.WEST);
    actor.awaitBlock(head, new BlockState(1, 0));
    actor.selectHeldSlot(2);
    actor.placeHeldBlock(support, BlockFace.EAST);
    WorldlineSmokeAwait.awaitBlock(actor, lever, new BlockState(69, 1), 5);
    return new StickyHeadBreakSetArm(support, piston, head, pushed, lever);
  }

  RemoteWorldView extend(B173WireClient actor, int ticks) throws Exception {
    actor.selectHeldSlot(4);
    actor.activateBlock(lever, BlockFace.UP);
    RemoteWorldView live = WorldlineSmokeAwait.awaitWorld(actor,
        view
        -> view.blockAt(lever.x(), lever.y(), lever.z()).equals(new BlockState(69, 9))
            && view.blockAt(piston.x(), piston.y(), piston.z()).equals(new BlockState(29, 12))
            && view.blockAt(head.x(), head.y(), head.z()).equals(new BlockState(34, 12))
            && view.blockAt(pushed.x(), pushed.y(), pushed.z()).equals(new BlockState(1, 0)),
        "sticky 29 extension", ticks);
    require(live.blockAt(lever.x(), lever.y(), lever.z()).equals(new BlockState(69, 9))
            && live.blockAt(piston.x(), piston.y(), piston.z()).equals(new BlockState(29, 12))
            && live.blockAt(head.x(), head.y(), head.z()).equals(new BlockState(34, 12))
            && live.blockAt(pushed.x(), pushed.y(), pushed.z()).equals(new BlockState(1, 0)),
        "sticky 29 extend absent: " + live.blockAt(piston.x(), piston.y(), piston.z()) + "/"
            + live.blockAt(head.x(), head.y(), head.z()) + "/" + live.blockAt(pushed.x(), pushed.y(), pushed.z()));
    return live;
  }

  RemoteDroppedItem breakHead(B173WireClient actor, int ticks) throws Exception {
    BlockState air = new BlockState(0, 0);
    RemoteItemStack drop = new RemoteItemStack(29, 1, 0);
    actor.selectHeldSlot(3);
    actor.beginBreak(head);
    WorldlineSmokeAwait.observe(actor, ticks);
    actor.finishBreak(head);
    actor.awaitBlock(head, air);
    actor.awaitBlock(piston, air);
    RemoteDroppedItem item = actor.awaitDroppedItem(drop);
    require(item.item().equals(drop) && item.item().legacyId() == 29 && item.item().count() == 1,
        "Packet21 sticky piston 29 drop absent");
    RemoteWorldView live = WorldlineSmokeAwait.observe(actor, 5);
    require(live.blockAt(piston.x(), piston.y(), piston.z()).equals(air)
            && live.blockAt(head.x(), head.y(), head.z()).equals(air)
            && live.blockAt(pushed.x(), pushed.y(), pushed.z()).equals(new BlockState(1, 0))
            && live.blockAt(lever.x(), lever.y(), lever.z()).equals(new BlockState(69, 9)),
        "sticky-head leftover drift: " + live.blockAt(piston.x(), piston.y(), piston.z()) + "/"
            + live.blockAt(head.x(), head.y(), head.z()));
    return item;
  }

  void persist(RemoteChunkSnapshot after, int chunkX, int chunkZ) {
    require(at(after, piston, chunkX, chunkZ).equals(new BlockState(0, 0))
            && at(after, head, chunkX, chunkZ).equals(new BlockState(0, 0))
            && at(after, pushed, chunkX, chunkZ).equals(new BlockState(1, 0))
            && at(after, lever, chunkX, chunkZ).equals(new BlockState(69, 9)),
        "fresh sticky-head-break leftover drift");
  }

  static BlockPosition raise(B173WireClient actor, RemoteChunkSnapshot initial, int chunkX, int chunkZ, int[] column)
      throws Exception {
    BlockPosition top = foundation(initial, chunkX, chunkZ);
    column[0] = 0;
    actor.selectHeldSlot(0);
    while (water(at(initial, BlockFace.UP.adjacent(top), chunkX, chunkZ).legacyId())) {
      top = place(actor, top, BlockFace.UP, 1);
      actor.moveAndObserve(0D, 1D, 0D, 1);
      require(++column[0] <= 15, "water column exceeded sticky-head-break fixture");
    }
    top = place(actor, top, BlockFace.UP, 1);
    actor.moveAndObserve(0D, 1D, 2D, 1);
    column[0]++;
    return top;
  }

  static BlockPosition place(B173WireClient actor, BlockPosition support, BlockFace face, int id) throws Exception {
    BlockPosition target = face.adjacent(support);
    actor.placeHeldBlock(support, face);
    actor.awaitBlock(target, new BlockState(id, 0));
    return target;
  }

  static BlockPosition foundation(RemoteChunkSnapshot chunk, int chunkX, int chunkZ) {
    int x = 4;
    while (x <= 11) {
      int z = 4;
      while (z <= 11) {
        int y = 126;
        while (y >= 1) {
          if (chunk.blockAt(x, y, z).legacyId() == 3 && water(chunk.blockAt(x, y + 1, z).legacyId())) {
            return new BlockPosition(chunkX * 16 + x, y, chunkZ * 16 + z);
          }
          y--;
        }
        z++;
      }
      x++;
    }
    throw new IllegalStateException("no deterministic sticky-head-break foundation");
  }

  static BlockState at(RemoteChunkSnapshot chunk, BlockPosition position, int chunkX, int chunkZ) {
    return chunk.blockAt(position.x() - chunkX * 16, position.y(), position.z() - chunkZ * 16);
  }

  static boolean water(int id) {
    return id == 8 || id == 9;
  }

  static void awaitPlayers(B173DedicatedServer server, int count) throws Exception {
    long deadline = System.currentTimeMillis() + 5000;
    while (System.currentTimeMillis() < deadline) {
      if (server.players().size() == count) {
        return;
      }
      Thread.sleep(100);
    }
    throw new IllegalStateException("player count drift");
  }

  static String sha(String value) throws Exception {
    byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
    StringBuilder hex = new StringBuilder();
    for (byte octet : digest) {
      hex.append(String.format("%02x", octet & 255));
    }
    return hex.toString();
  }

  static String cell(BlockPosition position) {
    return position.x() + ":" + position.y() + ":" + position.z();
  }

  static void require(boolean value, String message) {
    if (!value) {
      throw new IllegalStateException(message);
    }
  }
}
