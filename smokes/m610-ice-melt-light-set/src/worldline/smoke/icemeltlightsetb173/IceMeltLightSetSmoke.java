package worldline.smoke.icemeltlightsetb173;

import java.nio.file.*;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Places ice 79 beside floor torch 50:5 and waits official block-light melt to water. */
public final class IceMeltLightSetSmoke {
  private IceMeltLightSetSmoke() {}
  public static void main(String[] a) throws Exception {
    if (a.length != 9)
      throw new IllegalArgumentException(
          "usage: IceMeltLightSetSmoke server.jar workspace port seed username chunkX chunkZ windowTicks meltWindows");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    int cx = Integer.parseInt(a[5]), cz = Integer.parseInt(a[6]), window = Integer.parseInt(a[7]),
        windows = Integer.parseInt(a[8]);
    IceMeltLightSetArm.require(seed == 17320110707L && user.equals("IceMelt610") && user.length() <= 16 && window >= 1
            && window <= 1200 && windows >= 1 && windows <= 8,
        "ice-melt-light identity drift");
    Duration timeout = Duration.ofMinutes(20);
    B173DedicatedServer server = new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout), reader = null;
    BlockPosition top, east, ice, torch;
    int[] column = new int[1];
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1, 2}, new int[] {1, 79, 50},
          new int[] {32, 1, 1}, new int[] {0, 0, 0});
      actor.connect();
      actor.synchronizePose();
      IceMeltLightSetArm.require(actor.awaitInventory().occupiedSlots() == 3, "ice-melt-light inventory drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      top = IceMeltLightSetArm.raise(actor, initial, cx, cz, column);
      east = IceMeltLightSetArm.place(actor, top, BlockFace.EAST, 1);
      actor.selectHeldSlot(1);
      ice = IceMeltLightSetArm.place(actor, top, BlockFace.UP, 79);
      IceMeltLightSetArm.require(worldline.test.WorldlineSmokeAwait.observe(actor, 5)
                                     .blockAt(ice.x(), ice.y(), ice.z())
                                     .equals(IceMeltLightSetArm.ICE),
          "live ice melt-source drift");
      actor.selectHeldSlot(2);
      torch = BlockFace.UP.adjacent(east);
      actor.placeHeldBlock(east, BlockFace.UP);
      actor.awaitBlock(torch, IceMeltLightSetArm.TORCH);
      IceMeltLightSetArm.waitMelt(actor, ice, torch, window, windows);
      actor.close();
      IceMeltLightSetArm.awaitPlayers(server, 0);
      server.save();
      reader = new B173WireClient("127.0.0.1", port, user, timeout);
      reader.connect();
      reader.synchronizePose();
      RemoteChunkSnapshot after = reader.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      BlockState leftover = IceMeltLightSetArm.persist(after, cx, cz, top, east, ice, torch);
      String evidence = "column=" + column[0] + ",support=" + IceMeltLightSetArm.cell(top, 1, 0)
          + ",east=" + IceMeltLightSetArm.cell(east, 1, 0) + ",ice=" + IceMeltLightSetArm.cell(ice, 79, 0) + "->"
          + leftover.legacyId() + ":" + leftover.metadata() + ",torch=" + IceMeltLightSetArm.cell(torch, 50, 5)
          + ",persisted=true,clients=2,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + ("|fixture=raised-stone+ice79-torch-adjacent-melt|cause=packet15-item79+packet15-item50|wire=packet53-"
              + "ice79:0->water")
          + leftover.legacyId() + ":" + leftover.metadata()
          + "+packet53-torch50:5|oracle=official-block-light-random-tick-melt+fresh-login|" + evidence;
      System.out.println("WORLDLINE_M610_SET=" + evidence);
      System.out.println("WORLDLINE_M610_TRACE=" + trace);
      System.out.println("WORLDLINE_M610_SIGNATURE=" + IceMeltLightSetArm.sha(trace));
    } finally {
      actor.close();
      if (reader != null)
        reader.close();
      server.close();
    }
  }
}
