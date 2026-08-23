package worldline.smoke.rsnorlatchsetb173;

import worldline.api.*;
import worldline.b173server.*;

/** Dual M312 RS-NOR pads, west/south repeaters, dust lines, torches, and buttons. */
final class RsNorLatchSetFixture {
  final BlockPosition body, farUp, q, qbar, set, reset, eFar, eFar2, n, nn, nn1, nn2, nn3, nn4, nn5,
      n9;
  final int column;
  private RsNorLatchSetFixture(BlockPosition body, BlockPosition farUp, BlockPosition q,
      BlockPosition qbar, BlockPosition set, BlockPosition reset, BlockPosition eFar,
      BlockPosition eFar2, BlockPosition n, BlockPosition nn, BlockPosition nn1, BlockPosition nn2,
      BlockPosition nn3, BlockPosition nn4, BlockPosition nn5, BlockPosition n9, int column) {
    this.body = body;
    this.farUp = farUp;
    this.q = q;
    this.qbar = qbar;
    this.set = set;
    this.reset = reset;
    this.eFar = eFar;
    this.eFar2 = eFar2;
    this.n = n;
    this.nn = nn;
    this.nn1 = nn1;
    this.nn2 = nn2;
    this.nn3 = nn3;
    this.nn4 = nn4;
    this.nn5 = nn5;
    this.n9 = n9;
    this.column = column;
  }
  static RsNorLatchSetFixture build(
      B173WireClient actor, RemoteChunkSnapshot initial, int cx, int cz) throws Exception {
    BlockPosition top = foundation(initial, cx, cz);
    int column = 0;
    actor.selectHeldSlot(0);
    while (water(initial.blockAt(local(top.x(), cx), top.y() + 1, local(top.z(), cz)).legacyId())) {
      top = place(actor, top, BlockFace.UP, 1);
      actor.moveAndObserve(0D, 1D, 0D, 1);
      require(++column <= 15, "water column exceeded rs-nor fixture");
    }
    for (int lift = 0; lift < 8; lift++) {
      top = place(actor, top, BlockFace.UP, 1);
      actor.moveAndObserve(0D, 1D, 0D, 1);
      column++;
    }
    actor.moveAndObserve(0D, 0D, 2D, 1);
    actor.moveAndObserve(3D, 0D, 0D, 2);
    BlockPosition west = place(actor, top, BlockFace.WEST, 1),
                  body = place(actor, west, BlockFace.UP, 1),
                  east = place(actor, top, BlockFace.EAST, 1);
    BlockPosition n = place(actor, top, BlockFace.NORTH, 1),
                  nn = place(actor, n, BlockFace.NORTH, 1),
                  nn1 = place(actor, nn, BlockFace.EAST, 1),
                  nn2 = place(actor, nn1, BlockFace.EAST, 1),
                  nn3 = place(actor, nn2, BlockFace.EAST, 1),
                  nn4 = place(actor, nn3, BlockFace.EAST, 1),
                  nn5 = place(actor, nn4, BlockFace.EAST, 1),
                  n9 = place(actor, nn5, BlockFace.SOUTH, 1);
    BlockPosition mid = place(actor, east, BlockFace.EAST, 1),
                  far = place(actor, mid, BlockFace.EAST, 1),
                  farUp = place(actor, far, BlockFace.UP, 1),
                  eFar = place(actor, far, BlockFace.EAST, 1),
                  eFar2 = place(actor, eFar, BlockFace.EAST, 1),
                  eFar3 = place(actor, eFar2, BlockFace.EAST, 1);
    BlockPosition s = place(actor, east, BlockFace.SOUTH, 1),
                  sE = place(actor, s, BlockFace.EAST, 1),
                  resetPad = place(actor, s, BlockFace.SOUTH, 1);
    BlockPosition rptA = BlockFace.UP.adjacent(top), q = BlockFace.NORTH.adjacent(body),
                  qbar = BlockFace.SOUTH.adjacent(farUp);
    BlockPosition set, reset;
    actor.selectHeldSlot(1);
    actor.look(90F, 0F);
    worldline.test.WorldlineSmokeAwait.observe(actor, 2);
    actor.useHeldItemOnBlock(top, BlockFace.UP);
    require(actor.awaitBlock(rptA, new BlockState(93, 3))
                .blockAt(rptA.x(), rptA.y(), rptA.z())
                .equals(new BlockState(93, 3)),
        "west repeater A drift");
    actor.selectHeldSlot(2);
    dust(actor, east);
    dust(actor, s);
    dust(actor, sE);
    actor.selectHeldSlot(3);
    actor.placeHeldBlock(body, BlockFace.NORTH);
    require(actor.awaitBlock(q, new BlockState(76, 4))
                .blockAt(q.x(), q.y(), q.z())
                .equals(new BlockState(76, 4)),
        "Q 76:4 drift");
    actor.placeHeldBlock(farUp, BlockFace.SOUTH);
    require(actor.awaitBlock(qbar, new BlockState(76, 3))
                .blockAt(qbar.x(), qbar.y(), qbar.z())
                .equals(new BlockState(76, 3)),
        "Q-bar 76:3 drift");
    actor.selectHeldSlot(4);
    set = BlockFace.UP.adjacent(eFar3);
    reset = BlockFace.UP.adjacent(resetPad);
    actor.placeHeldBlock(eFar3, BlockFace.UP);
    require(ground(actor, set), "set ground lever drift");
    actor.placeHeldBlock(resetPad, BlockFace.UP);
    require(ground(actor, reset), "reset ground lever drift");
    return new RsNorLatchSetFixture(
        body, farUp, q, qbar, set, reset, eFar, eFar2, n, nn, nn1, nn2, nn3, nn4, nn5, n9, column);
  }
  void armHold(B173WireClient actor) throws Exception {
    actor.moveAndObserve(-4D, 0D, -2D, 2);
    actor.selectHeldSlot(2);
    dust(actor, n);
    dust(actor, nn);
    dust(actor, nn1);
    dust(actor, nn2);
    actor.moveAndObserve(5D, 0D, 0D, 2);
    dust(actor, nn3);
    dust(actor, nn4);
    dust(actor, nn5);
    dust(actor, n9);
    dust(actor, eFar2);
    actor.selectHeldSlot(1);
    actor.look(90F, 0F);
    worldline.test.WorldlineSmokeAwait.observe(actor, 2);
    actor.useHeldItemOnBlock(eFar, BlockFace.UP);
    require(actor.awaitBlock(BlockFace.UP.adjacent(eFar), new BlockState(93, 3))
                .blockAt(BlockFace.UP.adjacent(eFar).x(), BlockFace.UP.adjacent(eFar).y(),
                    BlockFace.UP.adjacent(eFar).z())
                .equals(new BlockState(93, 3)),
        "west repeater B drift");
  }
  private static void dust(B173WireClient a, BlockPosition pad) throws Exception {
    BlockPosition t = BlockFace.UP.adjacent(pad);
    a.useHeldItemOnBlock(pad, BlockFace.UP);
    worldline.test.WorldlineSmokeAwait.awaitBlockMatching(
        a, t, s -> s.legacyId() == 55, "redstone dust", 5);
  }
  private static boolean ground(B173WireClient a, BlockPosition lever) throws Exception {
    worldline.test.WorldlineSmokeAwait.awaitBlockMatching(a, lever,
        s
        -> s.legacyId() == 69 && (s.metadata() == 5 || s.metadata() == 6)
            && (s.metadata() & 8) == 0,
        "ground lever", 5);
    return true;
  }
  private static BlockPosition place(
      B173WireClient a, BlockPosition support, BlockFace face, int id) throws Exception {
    BlockPosition target = face.adjacent(support);
    a.placeHeldBlock(support, face);
    a.awaitBlock(target, new BlockState(id, 0));
    return target;
  }
  private static BlockPosition foundation(RemoteChunkSnapshot q, int cx, int cz) {
    for (int x = 4; x <= 11; x++)
      for (int z = 4; z <= 11; z++)
        for (int y = 126; y >= 1; y--)
          if (q.blockAt(x, y, z).legacyId() == 3 && water(q.blockAt(x, y + 1, z).legacyId()))
            return new BlockPosition(cx * 16 + x, y, cz * 16 + z);
    throw new IllegalStateException("no deterministic rs-nor latch foundation");
  }
  private static boolean water(int id) {
    return id == 8 || id == 9;
  }
  private static int local(int v, int c) {
    return v - c * 16;
  }
  private static void require(boolean v, String m) {
    if (!v)
      throw new IllegalStateException(m);
  }
}
