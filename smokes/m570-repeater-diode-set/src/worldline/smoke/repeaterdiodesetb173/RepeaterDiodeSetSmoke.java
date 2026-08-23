package worldline.smoke.repeaterdiodesetb173;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.RemoteChunkSnapshot;
import worldline.api.RemoteWorldView;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173PlayerSeed;
import worldline.b173server.B173WireClient;

/** Official repeater 93 conducts west and isolates the reverse input path. */
public final class RepeaterDiodeSetSmoke {
  private RepeaterDiodeSetSmoke() {}

  public static void main(String[] arguments) throws Exception {
    if (arguments.length != 9) {
      throw new IllegalArgumentException("usage: RepeaterDiodeSetSmoke server.jar workspace "
          + "port seed username chunkX chunkZ fixtureTicks signalTicks");
    }
    Path jar = Paths.get(arguments[0]);
    Path workspace = Paths.get(arguments[1]);
    int port = Integer.parseInt(arguments[2]);
    long seed = Long.parseLong(arguments[3]);
    String user = arguments[4];
    int chunkX = Integer.parseInt(arguments[5]);
    int chunkZ = Integer.parseInt(arguments[6]);
    int fixture = Integer.parseInt(arguments[7]);
    int signal = Integer.parseInt(arguments[8]);
    Duration timeout = Duration.ofSeconds(90);
    RepeaterDiodeSetArm.require(
        user.equals("RptDiode570") && user.length() <= 16 && seed == 17320110707L, "repeater-diode identity drift");
    B173DedicatedServer server = new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout);
    B173WireClient reader = null;
    RepeaterDiodeSetArm arm;
    int[] column = new int[1];
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1, 2, 3},
          new int[] {1, 356, 331, 69}, new int[] {48, 1, 2, 2}, new int[] {0, 0, 0, 0});
      actor.connect();
      actor.synchronizePose();
      RepeaterDiodeSetArm.require(actor.awaitInventory().occupiedSlots() == 4, "repeater-diode inventory drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(chunkX, chunkZ).chunkAt(chunkX, chunkZ);
      BlockPosition top = RepeaterDiodeSetArm.raise(actor, initial, chunkX, chunkZ, column);
      arm = RepeaterDiodeSetArm.place(actor, initial, top, chunkX, chunkZ);
      actor.selectHeldSlot(4);
      RemoteWorldView settled = worldline.test.WorldlineSmokeAwait.observe(actor, fixture);
      RepeaterDiodeSetArm.require(
          settled.blockAt(arm.repeater.x(), arm.repeater.y(), arm.repeater.z()).equals(new BlockState(93, 3))
              && settled.blockAt(arm.in.x(), arm.in.y(), arm.in.z()).equals(new BlockState(55, 0))
              && settled.blockAt(arm.out.x(), arm.out.y(), arm.out.z()).equals(new BlockState(55, 0)),
          "repeater-diode precondition drift");
      arm.reverse(actor, signal);
      arm.forward(actor, signal);
      actor.close();
      RepeaterDiodeSetArm.awaitPlayers(server, 0);
      server.save();
      reader = new B173WireClient("127.0.0.1", port, user, timeout);
      reader.connect();
      reader.synchronizePose();
      RemoteChunkSnapshot after = reader.awaitRemoteChunk(chunkX, chunkZ).chunkAt(chunkX, chunkZ);
      arm.persist(after, chunkX, chunkZ);
      String evidence = "column=" + column[0] + ",support=" + RepeaterDiodeSetArm.cell(arm.support) + ":1:0,repeater="
          + RepeaterDiodeSetArm.cell(arm.repeater) + ":93:3->94:3,facing=3,delay=1,reverse=rpt=93:3+in=55:0+out=55:15,"
          + "forward=rpt=94:3+in=55:15+out=55:15,isolated=true,persisted=94:3,"
          + "clients=2,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=raised-west-repeater93+in-dust55+out-dust55+south-levers|settle=" + fixture + "+" + signal
          + "ticks|cause=packet15-reverse-lever-then-forward-lever|"
          + "effect=official-repeater-diode-forward-conduction+reverse-isolation|"
          + "observation=fresh-login-packet51|" + evidence;
      System.out.println("WORLDLINE_M570_DIODE=" + evidence);
      System.out.println("WORLDLINE_M570_TRACE=" + trace);
      System.out.println("WORLDLINE_M570_SIGNATURE=" + RepeaterDiodeSetArm.sha(trace));
    } finally {
      actor.close();
      if (reader != null) {
        reader.close();
      }
      server.close();
    }
  }
}
