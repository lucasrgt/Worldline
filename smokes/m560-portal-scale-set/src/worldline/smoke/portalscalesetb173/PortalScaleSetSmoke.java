package worldline.smoke.portalscalesetb173;

import java.nio.file.*;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Travels one far Overworld portal and proves Packet9 pose follows official 8:1 scale. */
public final class PortalScaleSetSmoke {
 private PortalScaleSetSmoke(){}

 public static void main(String[] a) throws Exception {
  if (a.length != 9) throw new IllegalArgumentException(
    "usage: PortalScaleSetSmoke server.jar workspace port seed username chunkX chunkZ portalTicks travelTicks");
  Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
  int port = Integer.parseInt(a[2]); long seed = Long.parseLong(a[3]); String user = a[4];
  int cx = Integer.parseInt(a[5]), cz = Integer.parseInt(a[6]);
  int portalTicks = Integer.parseInt(a[7]), travelTicks = Integer.parseInt(a[8]);
  PortalScaleSetSupport.require(seed == 17320110707L && user.equals("PortalScale560") && user.length() <= 16
    && cx == 20 && cz == 20, "portal-scale-set identity drift");
  Duration timeout = Duration.ofSeconds(180);
  B173DedicatedServer server = new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true, true);
  B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout), builder = null;
  String evidence;
  try {
   server.boot();
   B173PlayerSeed.writeInventory(workspace, user, cx * 16 + 4.5D, 70D, cz * 16 + 4.5D,
     new int[]{0, 1, 2}, new int[]{1, 49, 259}, new int[]{64, 14, 1}, new int[]{0, 0, 0});
   actor.connect();
   actor.synchronizePose();
   PortalScaleSetSupport.require(actor.awaitInventory().occupiedSlots() == 3 && actor.dimension() == 0,
     "portal-scale inventory or dimension drift");
   RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
   BlockPosition anchor = PortalScaleSetSupport.foundation(initial, cx, cz);
   actor.close();
   PortalScaleSetSupport.awaitPlayers(server, 0);
   server.save();
   B173PlayerSeed.writeInventory(workspace, user, anchor.x() + 0.5D, anchor.y() + 4.5D, anchor.z() + 0.5D,
     new int[]{0, 1, 2}, new int[]{1, 49, 259}, new int[]{64, 14, 1}, new int[]{0, 0, 0});
   builder = new B173WireClient("127.0.0.1", port, user, timeout);
   builder.connect();
   PlayerPose pose = builder.synchronizePose();
   PortalScaleSetSupport.require(builder.dimension() == 0 && builder.awaitInventory().occupiedSlots() == 3,
     "relocated portal-scale inventory or dimension drift");
   builder.awaitRemoteChunk(cx, cz);
   PortalScaleSetFrame frame = PortalScaleSetFrame.build(builder, initial, cx, cz, anchor, pose, portalTicks);
   pose = frame.pose;
   pose = builder.moveAndObserve(frame.bottom.x() + 1.5D - pose.x(), frame.bottom.y() + 1D - pose.y(),
     frame.bottom.z() + 0.5D - pose.z(), 1).resulting();
   PortalScaleSetSupport.require(builder.dimension() == 0, "dimension changed before portal residence");
   PlayerPose entry = pose;
   worldline.test.WorldlineSmokeAwait.observe(builder,travelTicks);
   PortalScaleSetSupport.require(builder.awaitDimension(-1) == -1, "official Packet9 0->-1 absent");
   PlayerPose dest = builder.moveAndObserve(0D, 0D, 0D, 1).resulting();
   RemoteWorldView world = worldline.test.WorldlineSmokeAwait.observe(builder,20);
   int dcx = PortalScaleSetSupport.floor(dest.x()) >> 4, dcz = PortalScaleSetSupport.floor(dest.z()) >> 4;
   RemoteChunkSnapshot nether = world.chunkAt(dcx, dcz);
   evidence = PortalScaleSetScale.prove(entry, dest, frame.column, PortalScaleSetSupport.sky(nether));
   builder.close();
   PortalScaleSetSupport.awaitPlayers(server, 0);
   server.save();
   PortalScaleSetSupport.require(server.player(user).dimension() == -1, "scaled player dimension was not persisted");
  } finally { actor.close(); if (builder != null) builder.close(); server.close(); }
  String trace = "v1|server=official-b1.7.3|seed=" + seed
    + "|profile=allow-nether-true|fixture=far-chunk20-obsidian49-frame4x5+flintsteel259"
    + "|construction=packet15-fourteen-obsidian49+packet15-flint-259|entry=packet11-inside-portal90"
    + "|residence=" + travelTicks + "ticks|transition=server-packet9-0-to-minus1"
    + "|oracle=packet13-pose-quantized-8-to-1-not-nether-exists-not-m132-m133-m134-m382|" + evidence
    + "|disconnect=clean";
  System.out.println("WORLDLINE_M560_SET=" + evidence);
  System.out.println("WORLDLINE_M560_TRACE=" + trace);
  System.out.println("WORLDLINE_M560_SIGNATURE=" + PortalScaleSetSupport.sha(trace));
 }
}
