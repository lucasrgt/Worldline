package worldline.smoke.stickyheadbreaksetb173;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.RemoteChunkSnapshot;
import worldline.api.RemoteDroppedItem;
import worldline.api.RemoteWorldView;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173PlayerSeed;
import worldline.b173server.B173WireClient;
import worldline.test.WorldlineSmokeAwait;
import worldline.testkit.*;

/** Official sticky 29 extend then Packet14-break of head 34, freezing leftover base cleanup. */
public final class StickyHeadBreakSetSmoke {
  private StickyHeadBreakSetSmoke() {}

  public static void main(String[] arguments) throws Exception {
    if (arguments.length != 9) {
      throw new IllegalArgumentException("usage: StickyHeadBreakSetSmoke server.jar workspace port seed username "
          + "chunkX chunkZ fixtureTicks signalTicks");
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
    StickyHeadBreakSetArm.require(seed == 17320110707L && user.equals("StkHeadBrk573") && user.length() <= 16,
        "sticky-head-break identity drift");
    B173DedicatedServer server = new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout);
    B173WireClient reader = null;
    StickyHeadBreakSetArm arm;
    int[] column = new int[1];
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1, 2, 3},
          new int[] {1, 29, 69, 257}, new int[] {32, 1, 1, 1}, new int[] {0, 0, 0, 0});
      actor.connect();
      actor.synchronizePose();
      actor.look(-90F, 0F);
      StickyHeadBreakSetArm.require(actor.awaitInventory().occupiedSlots() == 4, "sticky-head-break inventory drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(chunkX, chunkZ).chunkAt(chunkX, chunkZ);
      BlockPosition top = StickyHeadBreakSetArm.raise(actor, initial, chunkX, chunkZ, column);
      arm = StickyHeadBreakSetArm.place(actor, initial, top, chunkX, chunkZ);
      RemoteWorldView settled = WorldlineSmokeAwait.observe(actor, fixture);
      StickyHeadBreakSetArm.require(
          settled.blockAt(arm.piston.x(), arm.piston.y(), arm.piston.z()).equals(new BlockState(29, 4))
              && settled.blockAt(arm.head.x(), arm.head.y(), arm.head.z()).equals(new BlockState(1, 0))
              && settled.blockAt(arm.pushed.x(), arm.pushed.y(), arm.pushed.z()).equals(new BlockState(0, 0)),
          "sticky 29 precondition drift");
      arm.extend(actor, signal);
      RemoteDroppedItem drop = arm.breakHead(actor, signal);
      actor.close();
      StickyHeadBreakSetArm.awaitPlayers(server, 0);
      server.save();
      reader = new B173WireClient("127.0.0.1", port, user, timeout);
      reader.connect();
      reader.synchronizePose();
      RemoteChunkSnapshot after = reader.awaitRemoteChunk(chunkX, chunkZ).chunkAt(chunkX, chunkZ);
      arm.persist(after, chunkX, chunkZ);
      java.util.List<BlockCellTransition> transitions = java.util.Arrays.asList(
          new BlockCellTransition(arm.piston, new BlockState(29, 12), new BlockState(0, 0)),
          new BlockCellTransition(arm.head, new BlockState(34, 12), new BlockState(0, 0)));
      java.util.List<worldline.api.RemoteItemStack> drops = java.util.Arrays.asList(drop.item());
      StickyHeadBreakSetArm.require(BlockBreakDropFixture.execute("b1.7.3:block/029",
              "coupled-sticky-piston", true, 257, transitions, transitions,
              BlockLifecycleDropMatrix.exact(drops), drops).subject()
                  .equals("b1.7.3:block/029"),
          "public sticky-piston break/drop evidence drift");
      String evidence = "column=" + column[0] + ",extend=29:4->12,head-break=34:12->0,base-left=29:12->0,piston="
          + StickyHeadBreakSetArm.cell(arm.piston) + ":29:4->12->0,head=" + StickyHeadBreakSetArm.cell(arm.head)
          + ":1:0->34:12->0:0,pushed=" + StickyHeadBreakSetArm.cell(arm.pushed)
          + ":0:0->1:0->1:0,lever=" + StickyHeadBreakSetArm.cell(arm.lever)
          + ":69:1->9,drops=packet21-29,persisted=true,clients=2,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed + "|fixture=sticky29-west-extended|settle=" + fixture
          + "+" + signal + "ticks|cause=packet15-lever-activate+packet14-ironpick257-head34"
          + "|effect=official-extended-sticky-head-break+base29-removed"
          + "|observation=fresh-login-packet51|" + evidence;
      System.out.println("WORLDLINE_M573_SET=" + evidence);
      System.out.println("WORLDLINE_M573_TRACE=" + trace);
      System.out.println("WORLDLINE_M573_SIGNATURE=" + StickyHeadBreakSetArm.sha(trace));
    } finally {
      actor.close();
      if (reader != null) {
        reader.close();
      }
      server.close();
    }
  }
}
