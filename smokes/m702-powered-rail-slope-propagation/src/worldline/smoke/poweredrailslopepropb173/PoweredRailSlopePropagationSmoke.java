package worldline.smoke.poweredrailslopepropb173;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import worldline.api.BlockFace;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.RemoteChunkSnapshot;
import worldline.api.RemoteWorldView;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173PlayerSeed;
import worldline.b173server.B173WireClient;
import worldline.test.WorldlineSmokeAwait;

/** Torch power crosses the powered-rail slope boundary uphill and downhill, then restores. */
public final class PoweredRailSlopePropagationSmoke {
  private PoweredRailSlopePropagationSmoke() {}

  public static void main(String[] args) throws Exception {
    if (args.length != 7)
      throw new IllegalArgumentException(
          "usage: PoweredRailSlopePropagationSmoke server.jar workspace port seed username chunkX chunkZ");
    Path jar = Paths.get(args[0]), workspace = Paths.get(args[1]);
    int port = Integer.parseInt(args[2]);
    long seed = Long.parseLong(args[3]);
    String user = args[4];
    int cx = Integer.parseInt(args[5]), cz = Integer.parseInt(args[6]);
    PoweredRailSlopePropagationArm.require(seed == 17320110707L && user.equals("RailSlope702")
            && user.length() <= 16, "powered-rail-slope-propagation identity drift");
    Duration timeout = Duration.ofSeconds(90);
    B173DedicatedServer server = new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout), reader = null;
    int[] column = new int[1];
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1, 2},
          new int[] {1, 27, 76}, new int[] {32, 4, 1}, new int[] {0, 0, 0});
      actor.connect();
      actor.synchronizePose();
      PoweredRailSlopePropagationArm.require(
          actor.awaitInventory().occupiedSlots() == 3, "slope propagation inventory drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      PoweredRailSlopePropagationArm arm =
          PoweredRailSlopePropagationArm.place(actor, initial, cx, cz, column);
      actor.moveAndObserve(2D, 0D, 0D, 4);
      actor.selectHeldSlot(2);
      actor.placeHeldBlock(BlockFace.DOWN.adjacent(arm.torch), BlockFace.UP);
      WorldlineSmokeAwait.awaitWorld(actor,
          world -> world.blockAt(arm.torch.x(), arm.torch.y(), arm.torch.z()).equals(new BlockState(76, 5))
              && PoweredRailSlopePropagationArm.powered(world, arm.slope)
              && PoweredRailSlopePropagationArm.powered(world, arm.topRail)
              && PoweredRailSlopePropagationArm.powered(world, arm.lowRail)
              && PoweredRailSlopePropagationArm.powered(world, arm.farRail),
          "slope-boundary powered propagation", 40);
      RemoteWorldView poweredView = WorldlineSmokeAwait.observe(actor, 10);
      actor.selectHeldSlot(0);
      actor.look(-90F, 0F);
      actor.beginBreak(arm.torch);
      WorldlineSmokeAwait.observe(actor, 1);
      actor.finishBreak(arm.torch);
      WorldlineSmokeAwait.awaitWorld(actor,
          world -> world.blockAt(arm.torch.x(), arm.torch.y(), arm.torch.z()).equals(new BlockState(0, 0))
              && PoweredRailSlopePropagationArm.unpoweredShape(world, arm.slope, arm.idleSlope)
              && PoweredRailSlopePropagationArm.unpoweredShape(world, arm.topRail, arm.idleTop)
              && PoweredRailSlopePropagationArm.unpoweredShape(world, arm.lowRail, arm.idleLow)
              && PoweredRailSlopePropagationArm.unpoweredShape(world, arm.farRail, arm.idleFar),
          "slope-boundary unpower restoration", 40);
      RemoteWorldView restored = WorldlineSmokeAwait.observe(actor, 2);
      actor.close();
      PoweredRailSlopePropagationArm.awaitPlayers(server, 0);
      server.save();
      reader = new B173WireClient("127.0.0.1", port, user, timeout);
      reader.connect();
      reader.synchronizePose();
      RemoteChunkSnapshot after = reader.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      arm.persist(after, cx, cz);
      String evidence = "column=" + column[0] + ",support="
          + PoweredRailSlopePropagationArm.cell(arm.support) + ":1:0,high="
          + PoweredRailSlopePropagationArm.cell(arm.high) + ":1:0,slope="
          + rail(arm.slope, arm.idleSlope, poweredView, restored) + ",top="
          + rail(arm.topRail, arm.idleTop, poweredView, restored) + ",low="
          + rail(arm.lowRail, arm.idleLow, poweredView, restored) + ",far="
          + rail(arm.farRail, arm.idleFar, poweredView, restored) + ",torch="
          + PoweredRailSlopePropagationArm.cell(arm.torch) + ":76:5->0,persisted=true,clients=2,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=raised-stone+slope27+top27+low27+far27+torch76"
          + "|cause=packet15-item27x4+packet15-torch76+break-torch76"
          + "|wire=packet53-rail27-slope-power-crossing+torch76:5->0"
          + "|oracle=slope-boundary-power-both-directions+fresh-login|" + evidence;
      System.out.println("WORLDLINE_M702_SET=" + evidence);
      System.out.println("WORLDLINE_M702_TRACE=" + trace);
      System.out.println("WORLDLINE_M702_SIGNATURE=" + PoweredRailSlopePropagationArm.sha(trace));
    } finally {
      actor.close();
      if (reader != null) reader.close();
      server.close();
    }
  }

  private static String rail(BlockPosition position, int idle, RemoteWorldView powered, RemoteWorldView restored) {
    return PoweredRailSlopePropagationArm.cell(position) + ":27:" + idle + "->" + meta(powered, position)
        + "->" + meta(restored, position);
  }

  private static int meta(RemoteWorldView view, BlockPosition position) {
    return view.blockAt(position.x(), position.y(), position.z()).metadata();
  }
}
