package worldline.smoke.dispenserqcsetb173;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import worldline.api.*;
import worldline.b173server.*;

/** Raised west dispenser plus QC stone-above and top-lever power, cloned from M153/M367. */
public final class DispenserQcSetArm {
  static final RemoteItemStack COBBLE = new RemoteItemStack(4, 1, 0);
  final BlockPosition support, disp, qc, lever;
  final int leverOff, leverOn;
  private DispenserQcSetArm(
      BlockPosition s, BlockPosition d, BlockPosition q, BlockPosition l, int off, int on) {
    support = s;
    disp = d;
    qc = q;
    lever = l;
    leverOff = off;
    leverOn = on;
  }
  static BlockPosition raise(B173WireClient a, RemoteChunkSnapshot initial, int cx, int cz,
      int[] column) throws Exception {
    BlockPosition top = foundation(initial, cx, cz);
    column[0] = 0;
    a.selectHeldSlot(0);
    while (water(at(initial, BlockFace.UP.adjacent(top), cx, cz).legacyId())) {
      top = place(a, top, BlockFace.UP, 1);
      a.moveAndObserve(0D, 1D, 0D, 1);
      require(++column[0] <= 15, "water column exceeded dispenser-qc fixture");
    }
    for (int lift = 0; lift < 8; lift++) {
      top = place(a, top, BlockFace.UP, 1);
      a.moveAndObserve(0D, 1D, 0D, 1);
      column[0]++;
    }
    return top;
  }
  static DispenserQcSetArm assemble(B173WireClient a, BlockPosition support) throws Exception {
    a.selectHeldSlot(1);
    BlockPosition disp = place(a, support, BlockFace.UP, 23);
    worldline.test.WorldlineSmokeAwait.awaitBlock(a, disp, new BlockState(23, 4), 5);
    a.selectHeldSlot(1);
    RemoteContainerWindow opened = B173DispenserWindows.open(a, disp, BlockFace.UP);
    require(opened.descriptor().kind() == RemoteWindowKind.DISPENSER
            && "Trap".equals(opened.descriptor().title()) && opened.inventory().size() == 45
            && opened.inventory().slot(0).empty()
            && opened.inventory().slot(39).item().equals(COBBLE),
        "dispenser-qc open mapping drifted");
    RemoteDispenserLoad load = B173DispenserWindows.load(a, 39, 0);
    require(load.takeAction() == 1 && load.storeAction() == 2 && load.stack().equals(COBBLE)
            && a.inventory().slot(39).empty(),
        "accepted cobble load drifted");
    a.closeWindow();
    a.selectHeldSlot(0);
    BlockPosition eastLow = place(a, support, BlockFace.EAST, 1);
    BlockPosition eastMid = place(a, eastLow, BlockFace.UP, 1);
    BlockPosition eastHigh = place(a, eastMid, BlockFace.UP, 1);
    BlockPosition qc = place(a, eastHigh, BlockFace.WEST, 1);
    require(qc.equals(BlockFace.UP.adjacent(disp)), "qc stone is not the block above dispenser");
    a.selectHeldSlot(2);
    BlockPosition lever = BlockFace.UP.adjacent(qc);
    a.placeHeldBlock(qc, BlockFace.UP);
    RemoteWorldView placed = worldline.test.WorldlineSmokeAwait.awaitWorld(a, v -> {
      BlockState state = v.blockAt(lever.x(), lever.y(), lever.z());
      return state.legacyId() == 69 && (state.metadata() & 8) == 0;
    }, "qc top lever", 5);
    BlockState leverState = placed.blockAt(lever.x(), lever.y(), lever.z());
    require(leverState.legacyId() == 69 && (leverState.metadata() & 8) == 0, "qc top lever drift");
    require(placed.blockAt(eastLow.x(), eastLow.y(), eastLow.z()).legacyId() == 1
            && placed.blockAt(disp.x() + 1, disp.y(), disp.z()).legacyId() == 1,
        "collapsed to M153/M333 adjacent-power lever");
    return new DispenserQcSetArm(
        support, disp, qc, lever, leverState.metadata(), leverState.metadata() | 8);
  }
  RemoteDroppedItem pulse(B173WireClient a) throws Exception {
    a.activateBlock(lever, BlockFace.UP);
    RemoteWorldView live = worldline.test.WorldlineSmokeAwait.awaitWorld(a,
        v
        -> v.blockAt(lever.x(), lever.y(), lever.z()).equals(new BlockState(69, leverOn))
            && v.blockAt(disp.x(), disp.y(), disp.z()).equals(new BlockState(23, 4))
            && v.blockAt(qc.x(), qc.y(), qc.z()).equals(new BlockState(1, 0)),
        "qc lever pulse", 10);
    require(live.blockAt(lever.x(), lever.y(), lever.z()).equals(new BlockState(69, leverOn))
            && live.blockAt(disp.x(), disp.y(), disp.z()).equals(new BlockState(23, 4))
            && live.blockAt(qc.x(), qc.y(), qc.z()).equals(new BlockState(1, 0)),
        "qc lever pulse drift");
    RemoteDroppedItem drop = a.awaitDroppedItem(COBBLE);
    require(drop.item().equals(COBBLE) && drop.item().count() == 1,
        "dispenser-qc Packet21 cobble 4 absent");
    return drop;
  }
  void remain(B173WireClient a) throws Exception {
    RemoteContainerWindow remain = B173DispenserWindows.open(a, disp, BlockFace.WEST);
    require(remain.inventory().slot(0).empty(), "official qc ejected stack drift");
    a.closeWindow();
  }
  static BlockPosition place(B173WireClient a, BlockPosition support, BlockFace face, int id)
      throws Exception {
    BlockPosition target = face.adjacent(support);
    a.placeHeldBlock(support, face);
    a.awaitBlock(target, id == 23 ? new BlockState(23, 4) : new BlockState(id, 0));
    return target;
  }
  static BlockPosition foundation(RemoteChunkSnapshot q, int cx, int cz) {
    for (int x = 4; x <= 11; x++)
      for (int z = 4; z <= 11; z++)
        for (int y = 126; y >= 1; y--)
          if (q.blockAt(x, y, z).legacyId() == 3 && water(q.blockAt(x, y + 1, z).legacyId()))
            return new BlockPosition(cx * 16 + x, y, cz * 16 + z);
    throw new IllegalStateException("no deterministic dispenser-qc foundation");
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
  static String cell(BlockPosition p) {
    return p.x() + ":" + p.y() + ":" + p.z();
  }
  static void require(boolean v, String m) {
    if (!v)
      throw new IllegalStateException(m);
  }
}
