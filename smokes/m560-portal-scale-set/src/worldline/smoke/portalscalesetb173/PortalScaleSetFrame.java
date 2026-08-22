package worldline.smoke.portalscalesetb173;

import worldline.api.*;
import worldline.b173server.B173WireClient;

/** Builds the M382 4x5 obsidian frame and ignites six portal 90 cells. */
final class PortalScaleSetFrame {
 final BlockPosition bottom;
 final int column;
 final PlayerPose pose;

 PortalScaleSetFrame(BlockPosition bottom, int column, PlayerPose pose) {
  this.bottom = bottom; this.column = column; this.pose = pose;
 }

 static PortalScaleSetFrame build(B173WireClient actor, RemoteChunkSnapshot initial, int cx, int cz,
   BlockPosition anchor, PlayerPose pose, int portalTicks) throws Exception {
  int column = 0;
  actor.selectHeldSlot(0);
  while (PortalScaleSetSupport.water(initial.blockAt(PortalScaleSetSupport.local(anchor.x(), cx),
    anchor.y() + 1, PortalScaleSetSupport.local(anchor.z(), cz)).legacyId())) {
   anchor = PortalScaleSetSupport.place(actor, anchor, BlockFace.UP, 1);
   pose = actor.moveAndObserve(0D, 1D, 0D, 1).resulting();
   PortalScaleSetSupport.require(++column <= 15, "water column exceeded portal-scale fixture");
  }
  anchor = PortalScaleSetSupport.place(actor, anchor, BlockFace.UP, 1);
  pose = actor.moveAndObserve(0D, 1D, 0D, 1).resulting();
  column++;
  actor.selectHeldSlot(1);
  BlockPosition bottom = PortalScaleSetSupport.place(actor, anchor, BlockFace.UP, 49), p = bottom;
  for (int i = 0; i < 3; i++) p = PortalScaleSetSupport.place(actor, p, BlockFace.EAST, 49);
  pose = actor.moveAndObserve(bottom.x() + 1.5D - pose.x(), 0D, 0D, 1).resulting();
  BlockPosition left = bottom, right = p;
  for (int i = 0; i < 4; i++) {
   left = PortalScaleSetSupport.place(actor, left, BlockFace.UP, 49);
   right = PortalScaleSetSupport.place(actor, right, BlockFace.UP, 49);
   pose = actor.moveAndObserve(0D, 1D, 0D, 1).resulting();
  }
  p = left;
  for (int i = 0; i < 2; i++) p = PortalScaleSetSupport.place(actor, p, BlockFace.EAST, 49);
  actor.selectHeldSlot(2);
  actor.useHeldItemOnBlock(new BlockPosition(bottom.x() + 1, bottom.y(), bottom.z()), BlockFace.UP);
  RemoteWorldView active = actor.sustainTicks(portalTicks);
  int portals = 0;
  for (int y = 1; y <= 3; y++) for (int x = 1; x <= 2; x++)
   if (active.blockAt(bottom.x() + x, bottom.y() + y, bottom.z()).legacyId() == 90) portals++;
  PortalScaleSetSupport.require(portals == 6 && actor.dimension() == 0, "portal-scale frame did not ignite in dimension 0");
  return new PortalScaleSetFrame(bottom, column, pose);
 }
}
