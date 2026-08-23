package worldline.smoke.stickypistonbudsetb173;

import java.nio.file.*;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Official sticky 29 BUD: diagonal-above QC prime, north-stone update, no live direct power. */
public final class StickyPistonBudSetSmoke {
  private StickyPistonBudSetSmoke() {}
  public static void main(String[] a) throws Exception {
    if (a.length != 9)
      throw new IllegalArgumentException("usage: StickyPistonBudSetSmoke server.jar workspace port seed username "
          + "chunkX chunkZ fixtureTicks signalTicks");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    int cx = Integer.parseInt(a[5]), cz = Integer.parseInt(a[6]), fixture = Integer.parseInt(a[7]),
        signal = Integer.parseInt(a[8]);
    Duration timeout = Duration.ofSeconds(90);
    StickyPistonBudSetArm.require(seed == 17320110707L && user.equals("StickyBud613") && user.length() <= 16,
        "sticky-piston-bud-set identity drift");
    B173DedicatedServer server = new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout), reader = null;
    StickyPistonBudSetArm arm;
    int[] column = new int[1];
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1, 2}, new int[] {1, 29, 69},
          new int[] {48, 1, 1}, new int[] {0, 0, 0});
      actor.connect();
      actor.synchronizePose();
      actor.look(-90F, 0F);
      StickyPistonBudSetArm.require(actor.awaitInventory().occupiedSlots() == 3, "sticky-BUD inventory drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      BlockPosition top = StickyPistonBudSetArm.raise(actor, initial, cx, cz, column);
      arm = StickyPistonBudSetArm.place(actor, initial, top, cx, cz);
      actor.selectHeldSlot(3);
      arm.charged(actor, fixture);
      arm.prime(actor, signal);
      arm.trigger(actor, signal);
      RemoteWorldView unpowered = arm.unpower(actor, signal);
      boolean latched = arm.extended(unpowered);
      arm.release(actor, signal, unpowered);
      actor.close();
      StickyPistonBudSetArm.awaitPlayers(server, 0);
      server.save();
      reader = new B173WireClient("127.0.0.1", port, user, timeout);
      reader.connect();
      reader.synchronizePose();
      RemoteChunkSnapshot after = reader.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      arm.persist(after, cx, cz);
      String evidence = "column=" + column[0] + ",primed=29:4,bud-extend=29:4->12,latched="
          + (latched ? "29:12" : "false") + ",bud-pull=29:12->4,piston=" + StickyPistonBudSetArm.cell(arm.piston)
          + ":29:4->12->4,head=" + StickyPistonBudSetArm.cell(arm.head) + ":1:0->34:12->1:0,pushed="
          + StickyPistonBudSetArm.cell(arm.pushed) + ":0:0->1:0->0:0,lever=" + StickyPistonBudSetArm.cell(arm.lever)
          + ":69:3->11->3,trigger=" + StickyPistonBudSetArm.cell(arm.north) + ":1:0,south="
          + StickyPistonBudSetArm.cell(arm.south) + ":0:0,direct-power=false,persisted=true,clients=2,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=sticky29-west+diagonal-above-lever|settle=" + fixture + "+" + signal
          + ("ticks|cause=packet15-north-stone-neighbor-update-not-live-direct-power|effect=official-sticky29-bud-"
              + "extend+latched-unpower+pull|observation=fresh-login-packet51|")
          + evidence;
      System.out.println("WORLDLINE_M613_SET=" + evidence);
      System.out.println("WORLDLINE_M613_TRACE=" + trace);
      System.out.println("WORLDLINE_M613_SIGNATURE=" + StickyPistonBudSetArm.sha(trace));
    } finally {
      actor.close();
      if (reader != null)
        reader.close();
      server.close();
    }
  }
}
