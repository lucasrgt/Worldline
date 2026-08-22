package worldline.smoke.portalscalesetb173;

import worldline.api.PlayerPose;

/** Proves Packet9 dimension -1 pose matches official 8:1 portal scale, quantized. */
final class PortalScaleSetScale {
 static final int FACTOR = 8;
 static final int RADIUS = 128;

 private PortalScaleSetScale(){}

 static String prove(PlayerPose entry, PlayerPose dest, int column, int sky) {
  PortalScaleSetSupport.require(sky == 0, "portal-scale destination is not decoded Nether terrain");
  int srcX = PortalScaleSetSupport.floor(entry.x()), srcY = PortalScaleSetSupport.floor(entry.y());
  int srcZ = PortalScaleSetSupport.floor(entry.z());
  int expectedX = PortalScaleSetSupport.floor(entry.x() / FACTOR);
  int expectedZ = PortalScaleSetSupport.floor(entry.z() / FACTOR);
  int destX = PortalScaleSetSupport.floor(dest.x()), destZ = PortalScaleSetSupport.floor(dest.z());
  int dx = Math.abs(destX - expectedX), dz = Math.abs(destZ - expectedZ);
  PortalScaleSetSupport.require(dx <= RADIUS && dz <= RADIUS,
    "Packet9 pose missed quantized 8:1 mapping dest=" + destX + ":" + destZ + " expected=" + expectedX + ":" + expectedZ);
  PortalScaleSetSupport.require(Math.abs(destX - srcX) > RADIUS && Math.abs(destZ - srcZ) > RADIUS,
    "Packet9 pose looks like 1:1 not 8:1 dest=" + destX + ":" + destZ + " source=" + srcX + ":" + srcZ);
  return "dimension=0->-1,scale=8,source=" + srcX + ":" + srcY + ":" + srcZ + ",expected=" + expectedX + ":" + expectedZ
    + ",quantized=true,within=" + RADIUS + ",not1to1=true,sky=0,column=" + column + ",packet9=0->-1";
 }
}
