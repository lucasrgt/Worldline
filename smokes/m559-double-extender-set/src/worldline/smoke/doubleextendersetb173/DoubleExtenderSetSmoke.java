package worldline.smoke.doubleextendersetb173;

import java.nio.file.*;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Sequences official sticky 29 then regular 33 so one cobble payload travels two cells. */
public final class DoubleExtenderSetSmoke {
  private DoubleExtenderSetSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 9)
      throw new IllegalArgumentException(
          "usage: DoubleExtenderSetSmoke server.jar workspace port seed username chunkX chunkZ fixtureTicks signalTicks");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    int cx = Integer.parseInt(a[5]), cz = Integer.parseInt(a[6]), fixture = Integer.parseInt(a[7]),
        signal = Integer.parseInt(a[8]);
    Duration timeout = Duration.ofSeconds(90);
    DoubleExtenderSetArm.require(user.length() <= 16, "username exceeds 16");
    B173DedicatedServer server =
        new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout), reader = null;
    int[] column = new int[1];
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1, 2, 3, 4},
          new int[] {1, 29, 33, 69, 4}, new int[] {32, 1, 1, 2, 1}, new int[] {0, 0, 0, 0, 0});
      actor.connect();
      actor.synchronizePose();
      actor.look(-90F, 0F);
      DoubleExtenderSetArm.require(
          actor.awaitInventory().occupiedSlots() == 5, "double-extender inventory drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      BlockPosition top = DoubleExtenderSetArm.raise(actor, initial, cx, cz, column);
      DoubleExtenderSetArm arm = DoubleExtenderSetArm.build(actor, initial, top, cx, cz);
      actor.selectHeldSlot(5);
      RemoteWorldView settled = worldline.test.WorldlineSmokeAwait.observe(actor, fixture);
      DoubleExtenderSetArm.require(
          settled.blockAt(arm.rear.x(), arm.rear.y(), arm.rear.z()).equals(new BlockState(29, 4))
              && settled.blockAt(arm.front0.x(), arm.front0.y(), arm.front0.z())
                  .equals(new BlockState(33, 4))
              && settled.blockAt(arm.payload0.x(), arm.payload0.y(), arm.payload0.z())
                  .equals(new BlockState(4, 0))
              && settled.blockAt(arm.payload1.x(), arm.payload1.y(), arm.payload1.z())
                  .equals(new BlockState(0, 0))
              && settled.blockAt(arm.payload2.x(), arm.payload2.y(), arm.payload2.z())
                  .equals(new BlockState(0, 0)),
          "double-extender precondition drift");
      arm.pulse(actor, arm.rearLever, signal, new BlockState(69, 9), "sticky 29 first cell",
          new BlockPosition[] {arm.rear, arm.front0, arm.payload0, arm.payload1, arm.payload2},
          new BlockState[] {new BlockState(29, 12), new BlockState(34, 12), new BlockState(33, 4),
              new BlockState(4, 0), new BlockState(0, 0)});
      arm.pulse(actor, arm.frontLever, signal, new BlockState(69, 11), "piston 33 second cell",
          new BlockPosition[] {arm.rear, arm.front0, arm.payload0, arm.payload1, arm.payload2},
          new BlockState[] {new BlockState(29, 12), new BlockState(34, 12), new BlockState(33, 12),
              new BlockState(34, 4), new BlockState(4, 0)});
      actor.close();
      DoubleExtenderSetArm.awaitPlayers(server, 0);
      server.save();
      reader = new B173WireClient("127.0.0.1", port, user, timeout);
      reader.connect();
      reader.synchronizePose();
      RemoteChunkSnapshot after = reader.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      arm.persist(after, cx, cz,
          new BlockPosition[] {arm.rear, arm.front0, arm.payload0, arm.payload1, arm.payload2,
              arm.rearLever, arm.frontLever},
          new BlockState[] {new BlockState(29, 12), new BlockState(34, 12), new BlockState(33, 12),
              new BlockState(34, 4), new BlockState(4, 0), new BlockState(69, 9),
              new BlockState(69, 11)},
          "fresh double-extender drift");
      String evidence = "column=" + column[0]
          + ",cells=2,sequenced=29-then-33,rear=29:4->12,front=33:4->12,rear-cell="
          + DoubleExtenderSetArm.cell(arm.rear)
          + ":29:12,front-from=" + DoubleExtenderSetArm.cell(arm.front0)
          + ",front-to=" + DoubleExtenderSetArm.cell(arm.payload0)
          + ":33:12,payload=" + DoubleExtenderSetArm.cell(arm.payload0) + "->"
          + DoubleExtenderSetArm.cell(arm.payload1) + "->" + DoubleExtenderSetArm.cell(arm.payload2)
          + ":4:0,sticky-head=" + DoubleExtenderSetArm.cell(arm.front0)
          + ":34:12,piston-head=" + DoubleExtenderSetArm.cell(arm.payload1)
          + ":34:4,persisted=true,clients=2,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=sticky29-west+piston33-west+payload-cobble4+sequenced-levers|settle="
          + fixture + "+" + signal
          + "ticks|cause=packet15-rear-lever-then-front-lever|effect=official-double-extender-two-cell-payload|observation=fresh-login-packet51|"
          + evidence;
      System.out.println("WORLDLINE_M559_SET=" + evidence);
      System.out.println("WORLDLINE_M559_TRACE=" + trace);
      System.out.println("WORLDLINE_M559_SIGNATURE=" + DoubleExtenderSetArm.sha(trace));
    } finally {
      actor.close();
      if (reader != null)
        reader.close();
      server.close();
    }
  }
}
