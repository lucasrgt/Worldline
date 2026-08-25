package worldline.smoke.sleepquorumb173;
import static worldline.b173server.B173FixtureSupport.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.function.Predicate;
import worldline.api.BlockFace;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.PlayerPose;
import worldline.api.RemoteBedUse;
import worldline.api.RemoteChunkSnapshot;
import worldline.api.RemoteWorldView;
import worldline.b173server.B173BedAccess;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173LevelDatTime;
import worldline.b173server.B173PlayerSeed;
import worldline.b173server.B173WireClient;
import worldline.test.WorldlineSmokeAwait;
import worldline.testkit.SleepQuorumFixture;

/**
 * Freezes the Beta 1.7.3 multiplayer sleep quorum: with two Overworld players
 * and two valid beds at night, a solo sleeper holds night for a bounded window,
 * the completing sleeper triggers morning, and both sleepers wake validly.
 */
public final class SleepQuorumSmoke {
  private static final long SEED = 17320110707L;
  private static final int NIGHT = 18000, HOLD_TICKS = 400, PROBE_TICKS = 50;
  private static final Predicate<BlockState> FOOT =
      state -> state.legacyId() == 26 && state.metadata() < 8;
  private static final Predicate<BlockState> HEAD_FREE =
      state -> state.legacyId() == 26 && state.metadata() >= 8 && state.metadata() < 12;
  private static final Predicate<BlockState> HEAD_OCCUPIED =
      state -> state.legacyId() == 26 && state.metadata() >= 12;

  private SleepQuorumSmoke() {}
  public static void main(String[] arguments) throws Exception {
    if (arguments.length != 6)
      throw new IllegalArgumentException(
          "usage: SleepQuorumSmoke server.jar workspace port seed firstName secondName");
    Path jar = Paths.get(arguments[0]);
    Path workspace = Paths.get(arguments[1]);
    int port = Integer.parseInt(arguments[2]);
    require(Long.parseLong(arguments[3]) == SEED, "fixture seed drift");
    String firstName = arguments[4], secondName = arguments[5];
    Duration timeout = Duration.ofSeconds(90);
    B173DedicatedServer server =
        new B173DedicatedServer(jar, workspace, port, SEED, timeout, 3, true);
    B173WireClient first = new B173WireClient("127.0.0.1", port, firstName, timeout),
        second = new B173WireClient("127.0.0.1", port, secondName, timeout);
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, firstName, 4.5D, 60D, 4.5D,
          new int[] {0, 1}, new int[] {1, 355}, new int[] {32, 1}, new int[] {0, 0});
      first.connect();
      PlayerPose pose = first.synchronizePose();
      RemoteChunkSnapshot terrain = first.awaitRemoteChunk(0, 0).chunkAt(0, 0);
      BlockPosition baseFirst = foundation(terrain);
      BlockPosition baseSecond = secondFoundation(terrain, baseFirst);
      BlockPosition topFirst = raise(first, pose, baseFirst, terrain);
      Bed bedFirst = placeBed(first, topFirst, null);
      B173PlayerSeed.writeInventory(workspace, secondName, baseSecond.x() + 0.5D,
          60D, baseSecond.z() + 0.5D,
          new int[] {0, 1}, new int[] {1, 355}, new int[] {32, 1}, new int[] {0, 0});
      second.connect();
      pose = second.synchronizePose();
      RemoteChunkSnapshot later = second.awaitRemoteChunk(0, 0).chunkAt(0, 0);
      BlockPosition topSecond = raise(second, pose, baseSecond, later);
      Bed bedSecond = placeBed(second, topSecond, topFirst);
      server.setTime(NIGHT);
      WorldlineSmokeAwait.observe(first, 20);
      WorldlineSmokeAwait.observe(second, 20);
      first.selectHeldSlot(2);
      first.activateBlock(bedFirst.foot(), BlockFace.UP);
      require(matches(B173BedAccess.await(first), first, bedFirst.head()),
          "first Packet17 sleep drift");
      WorldlineSmokeAwait.awaitBlockMatching(first, bedFirst.head(), HEAD_OCCUPIED,
          "occupied first head", 400);
      require(matches(B173BedAccess.await(second), first, bedFirst.head()),
          "peer did not receive the solo sleeper broadcast");
      SleepQuorumFixture.Evidence evidence =
          SleepQuorumFixture.await(2, HOLD_TICKS, PROBE_TICKS,
              tick -> HEAD_OCCUPIED.test(
                  WorldlineSmokeAwait.observe(first, PROBE_TICKS).blockAt(
                      bedFirst.head().x(), bedFirst.head().y(), bedFirst.head().z())),
              () -> {
                try { return complete(second, first, bedSecond, bedFirst); }
                catch (RuntimeException error) { throw error; }
                catch (Exception error) { throw new IllegalStateException("completion failed", error); }
              });
      RemoteWorldView peer = WorldlineSmokeAwait.observe(second, PROBE_TICKS);
      require(FOOT.test(peer.blockAt(bedSecond.foot().x(), bedSecond.foot().y(), bedSecond.foot().z()))
              && HEAD_FREE.test(peer.blockAt(bedSecond.head().x(), bedSecond.head().y(), bedSecond.head().z())),
          "second sleeper drifted during hold");
      first.close();
      second.close();
      awaitPlayers(server, 0);
      server.save();
      long persisted = B173LevelDatTime.read(workspace.resolve("world/level.dat"));
      require(persisted >= NIGHT && persisted % 24000L < 12000L,
          "persisted time is not morning: " + persisted);
      require(evidence.wokenSleepers() == 2 && evidence.expectedSleepers() == 2,
          "quorum evidence drift");
      String signal = "players=2,beds=2,night=" + NIGHT + ",hold<=" + HOLD_TICKS + "ticks"
          + ",open-quorum=no-skip,completed-quorum=morning,wake=both"
          + ",morning=persisted-day-range,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + SEED
          + "|fixture=ocean-floor-dual-pillars+dirt-beds-item355"
          + "|action=night-set+solo-packet17+bounded-hold+second-packet17-quorum"
          + "|observation=open-hold-no-skip+completion-morning+dual-wake-clear-heads"
          + "|oracle=multiplayer-sleep-quorum|" + signal;
      System.out.println("WORLDLINE_M643_SET=" + signal);
      System.out.println("WORLDLINE_M643_TRACE=" + trace);
      System.out.println("WORLDLINE_M643_SIGNATURE=" + sha(trace));
    } finally {
      first.close();
      second.close();
      server.close();
    }
  }

  private static int complete(B173WireClient second, B173WireClient first, Bed bedSecond,
      Bed bedFirst) throws Exception {
    second.selectHeldSlot(2);
    second.activateBlock(bedSecond.foot(), BlockFace.UP);
    require(matches(B173BedAccess.await(second), second, bedSecond.head()),
        "second Packet17 sleep drift");
    int woken = 0;
    WorldlineSmokeAwait.awaitBlockMatching(second, bedSecond.head(), HEAD_FREE,
        "second wake", 800);
    woken++;
    WorldlineSmokeAwait.awaitBlockMatching(first, bedFirst.head(), HEAD_FREE,
        "first wake", 800);
    woken++;
    require(HEAD_FREE.test(WorldlineSmokeAwait.observe(second, 20).blockAt(
            bedFirst.head().x(), bedFirst.head().y(), bedFirst.head().z())),
        "second view of first wake drifted");
    require(HEAD_FREE.test(WorldlineSmokeAwait.observe(first, 20).blockAt(
            bedSecond.head().x(), bedSecond.head().y(), bedSecond.head().z())),
        "first view of second wake drifted");
    return woken;
  }

  private static boolean matches(RemoteBedUse use, B173WireClient client, BlockPosition head) {
    return use.entityId() == client.state().entityId() && use.unused() == 0
        && use.x() == head.x() && use.y() == head.y() && use.z() == head.z()
        && use.packet70() == RemoteBedUse.NO_PACKET70;
  }

  private static BlockPosition foundation(RemoteChunkSnapshot terrain) {
    for (int x = 4; x <= 11; x++)
      for (int z = 4; z <= 11; z++)
        for (int y = 126; y >= 1; y--)
          if (terrain.blockAt(x, y, z).legacyId() == 3
              && water(terrain.blockAt(x, y + 1, z).legacyId()))
            return new BlockPosition(x, y, z);
    throw new IllegalStateException("no deterministic first bed foundation");
  }

  private static BlockPosition secondFoundation(RemoteChunkSnapshot terrain, BlockPosition first) {
    for (int x = 4; x <= 11; x++)
      for (int z = 4; z <= 11; z++)
        for (int y = 126; y >= 1; y--)
          if (terrain.blockAt(x, y, z).legacyId() == 3
              && water(terrain.blockAt(x, y + 1, z).legacyId())) {
            BlockPosition candidate = new BlockPosition(x, y, z);
            if (chebyshev(candidate, first) >= 3) return candidate;
          }
    throw new IllegalStateException("no deterministic second bed foundation");
  }

  private static BlockPosition raise(B173WireClient actor, PlayerPose pose,
      BlockPosition base, RemoteChunkSnapshot terrain) throws Exception {
    BlockPosition top = base;
    int column = 0;
    while (water(terrain.blockAt(local(top.x(), 0), top.y() + 1, local(top.z(), 0)).legacyId())) {
      top = place(actor, top, BlockFace.UP, 1);
      pose = actor.moveAndObserve(0D, 1D, 0D, 1).resulting();
      require(++column <= 15, "water column exceeded quorum fixture");
    }
    for (int lift = 0; lift < 8; lift++) {
      top = place(actor, top, BlockFace.UP, 1);
      pose = actor.moveAndObserve(0D, 1D, 0D, 1).resulting();
    }
    pose = actor.moveAndObserve(top.x() + 0.5D - pose.x(), top.y() + 1.0D - pose.y(),
        top.z() + 0.5D - pose.z(), 8).resulting();
    return top;
  }

  private static Bed placeBed(B173WireClient actor, BlockPosition top, BlockPosition otherTop)
      throws Exception {
    BlockFace[] faces = {BlockFace.SOUTH, BlockFace.WEST, BlockFace.NORTH, BlockFace.EAST};
    float[] yaws = {0F, 90F, 180F, 270F};
    RemoteWorldView view = WorldlineSmokeAwait.observe(actor, 2);
    for (int index = 0; index < faces.length; index++) {
      BlockPosition support = faces[index].adjacent(top);
      BlockPosition headCell = BlockFace.UP.adjacent(support);
      if (support.equals(otherTop) || view.blockAt(support.x(), support.y(), support.z()).legacyId() != 0
          || view.blockAt(headCell.x(), headCell.y(), headCell.z()).legacyId() != 0)
        continue;
      place(actor, top, faces[index], 1);
      actor.look(yaws[index], 0F);
      WorldlineSmokeAwait.observe(actor, 2);
      actor.selectHeldSlot(1);
      actor.useHeldItemOnBlock(top, BlockFace.UP);
      actor.selectHeldSlot(0);
      BlockPosition foot = BlockFace.UP.adjacent(top);
      BlockPosition head = faces[index].adjacent(foot);
      WorldlineSmokeAwait.awaitBlockMatching(actor, foot, FOOT, "bed foot", 400);
      WorldlineSmokeAwait.awaitBlockMatching(actor, head, HEAD_FREE, "bed head", 400);
      return new Bed(foot, head);
    }
    throw new IllegalStateException("no free bed orientation");
  }

  private static int chebyshev(BlockPosition a, BlockPosition b) {
    return Math.max(Math.abs(a.x() - b.x()), Math.abs(a.z() - b.z()));
  }
  private static void require(boolean value, String message) {
    if (!value) throw new IllegalStateException(message);
  }

  private static final class Bed {
    final BlockPosition foot, head;
    Bed(BlockPosition foot, BlockPosition head) {
      this.foot = foot;
      this.head = head;
    }
    BlockPosition foot() { return foot; }
    BlockPosition head() { return head; }
  }
}
