package worldline.smoke.pistonbudsetb173;

import java.nio.file.*;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Official piston 33 BUD pulse: neighbor place, one extend, self-clear retract. */
public final class PistonBudSetSmoke {
  private PistonBudSetSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 9)
      throw new IllegalArgumentException(
          "usage: PistonBudSetSmoke server.jar workspace port seed username chunkX chunkZ fixtureTicks signalTicks");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    int cx = Integer.parseInt(a[5]), cz = Integer.parseInt(a[6]), fixture = Integer.parseInt(a[7]),
        signal = Integer.parseInt(a[8]);
    Duration timeout = Duration.ofSeconds(90);
    PistonBudSetArm.require(
        user.equals("PistonBud548") && user.length() <= 16 && seed == 17320110707L,
        "actor username drift");
    B173DedicatedServer server =
        new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout), reader = null;
    PistonBudSetArm arm;
    int[] column = new int[1];
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1, 2},
          new int[] {1, 33, 76}, new int[] {48, 1, 1}, new int[] {0, 0, 0});
      actor.connect();
      actor.synchronizePose();
      actor.look(-90F, 0F);
      PistonBudSetArm.require(
          actor.awaitInventory().occupiedSlots() == 3, "piston-BUD inventory drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      BlockPosition top = PistonBudSetArm.raise(actor, initial, cx, cz, column);
      arm = PistonBudSetArm.place(actor, initial, top, cx, cz);
      actor.selectHeldSlot(3);
      RemoteWorldView settled = actor.sustainTicks(fixture);
      arm.charged(settled);
      arm.pulse(actor, signal);
      actor.close();
      PistonBudSetArm.awaitPlayers(server, 0);
      server.save();
      reader = new B173WireClient("127.0.0.1", port, user, timeout);
      reader.connect();
      reader.synchronizePose();
      RemoteChunkSnapshot after = reader.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      arm.persist(after, cx, cz);
      String mid = arm.pulsePiston.legacyId() + ":" + arm.pulsePiston.metadata(),
             midHead = arm.pulseHead.legacyId() + ":" + arm.pulseHead.metadata();
      String evidence = "column=" + column[0] + ",bud-pulse=33:4->" + mid
          + "->33:4,piston=" + PistonBudSetArm.cell(arm.piston) + ":33:4->" + mid
          + "->33:4,head=" + PistonBudSetArm.cell(arm.head) + ":1:0->" + midHead + "->0:0,pushed="
          + PistonBudSetArm.cell(arm.pushed) + ":0:0->1:0,torch=" + PistonBudSetArm.cell(arm.torch)
          + ":0:0,power=none,persisted=true,clients=2,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=piston33-west+payload-stone|settle=" + fixture + "+" + signal
          + "ticks|cause=packet15-neighbor-torch76-on-payload|effect=official-piston33-bud-pulse-not-lever-not-qc-hold|observation=fresh-login-packet51|"
          + evidence;
      System.out.println("WORLDLINE_M548_SET=" + evidence);
      System.out.println("WORLDLINE_M548_TRACE=" + trace);
      System.out.println("WORLDLINE_M548_SIGNATURE=" + PistonBudSetArm.sha(trace));
    } finally {
      actor.close();
      if (reader != null)
        reader.close();
      server.close();
    }
  }
}
