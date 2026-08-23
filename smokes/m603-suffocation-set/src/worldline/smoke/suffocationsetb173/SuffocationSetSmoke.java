package worldline.smoke.suffocationsetb173;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import worldline.api.BlockFace;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.PlayerPose;
import worldline.api.RemoteChunkSnapshot;
import worldline.api.RemoteIncomingHit;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173PlayerSeed;
import worldline.b173server.B173WireClient;
import worldline.test.WorldlineSmokeAwait;

/** Buries the actor head in falling sand 12 so Packet8 records 1 suffocation damage. */
public final class SuffocationSetSmoke {
  private SuffocationSetSmoke() {}

  public static void main(String[] args) throws Exception {
    if (args.length != 7) {
      throw new IllegalArgumentException(
          "usage: SuffocationSetSmoke server.jar workspace port seed username chunkX chunkZ");
    }
    Path jar = Paths.get(args[0]);
    Path workspace = Paths.get(args[1]);
    int port = Integer.parseInt(args[2]);
    long seed = Long.parseLong(args[3]);
    String user = args[4];
    int chunkX = Integer.parseInt(args[5]);
    int chunkZ = Integer.parseInt(args[6]);
    SuffocationSetSupport.require(
        seed == 17320110707L && user.equals("Suffocate603") && user.length() <= 16, "suffocation-set identity drift");
    Duration timeout = Duration.ofSeconds(180);
    B173DedicatedServer server = new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout);
    try {
      run(server, actor, workspace, user, chunkX, chunkZ, seed);
    } finally {
      actor.close();
      server.close();
    }
  }

  private static void run(B173DedicatedServer server, B173WireClient actor, Path workspace, String user, int chunkX,
      int chunkZ, long seed) throws Exception {
    server.boot();
    B173PlayerSeed.writeInventory(
        workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1}, new int[] {1, 12}, new int[] {64, 8}, new int[] {0, 0}, 20);
    actor.connect();
    PlayerPose pose = actor.synchronizePose();
    SuffocationSetSupport.require(actor.awaitInventory().occupiedSlots() == 2 && actor.awaitHealth(20) == 20,
        "suffocation inventory or health drift");
    RemoteChunkSnapshot initial = actor.awaitRemoteChunk(chunkX, chunkZ).chunkAt(chunkX, chunkZ);
    BlockPosition top = SuffocationSetSupport.foundation(initial, chunkX, chunkZ);
    int column = 0;
    actor.selectHeldSlot(0);
    while (SuffocationSetSupport.water(initial
            .blockAt(
                SuffocationSetSupport.local(top.x(), chunkX), top.y() + 1, SuffocationSetSupport.local(top.z(), chunkZ))
            .legacyId())) {
      top = SuffocationSetSupport.place(actor, top, BlockFace.UP, 1);
      pose = actor.moveAndObserve(0D, 1D, 0D, 1).resulting();
      column++;
      SuffocationSetSupport.require(column <= 15, "water column exceeded suffocation fixture");
    }
    int lift = 0;
    while (lift < 8) {
      top = SuffocationSetSupport.place(actor, top, BlockFace.UP, 1);
      pose = actor.moveAndObserve(0D, 1D, 0D, 1).resulting();
      column++;
      lift++;
    }
    BlockPosition pad = SuffocationSetSupport.place(
        actor, SuffocationSetSupport.place(actor, top, BlockFace.SOUTH, 1), BlockFace.SOUTH, 1);
    BlockPosition tower = SuffocationSetSupport.place(actor, pad, BlockFace.EAST, 1);
    int rise = 0;
    while (rise < 4) {
      tower = SuffocationSetSupport.place(actor, tower, BlockFace.UP, 1);
      rise++;
    }
    pose = SuffocationSetSupport.walk(actor, pose, pad.x() + 0.5D, pad.y() + 1.0D, pad.z() + 0.5D);
    SuffocationSetSupport.require(
        pose.y() >= pad.y() + 0.9D, "pad pose drift pose=" + pose.x() + "," + pose.y() + "," + pose.z());
    SuffocationSetSupport.require(actor.health() == 20, "pre-suffocate health drift: " + actor.health());
    actor.selectHeldSlot(1);
    actor.placeHeldBlock(tower, BlockFace.WEST);
    actor.selectHeldSlot(0);
    tower = SuffocationSetSupport.place(actor, tower, BlockFace.UP, 1);
    actor.selectHeldSlot(1);
    actor.placeHeldBlock(tower, BlockFace.WEST);
    BlockPosition body = new BlockPosition(pad.x(), pad.y() + 1, pad.z());
    BlockPosition head = new BlockPosition(pad.x(), pad.y() + 2, pad.z());
    actor.awaitBlock(body, new BlockState(12, 0));
    actor.awaitBlock(head, new BlockState(12, 0));
    int after =
        WorldlineSmokeAwait.awaitEntity(actor, actor::health, h -> h.intValue() < 20, "suffocation health", 500);
    SuffocationSetSupport.require(after == 19, "suffocation Packet8 health drift: 20->" + after);
    RemoteIncomingHit hit = actor.awaitIncomingHit(after);
    SuffocationSetSupport.require(
        hit.healthBefore() == 20 && hit.healthAfter() == 19 && hit.damage() == 1, "suffocation Packet38/8 drift");
    SuffocationSetSupport.require(actor.health() == 19, "suffocation death is m465 not hurt: " + actor.health());
    SuffocationSetSupport.require(
        WorldlineSmokeAwait.observe(actor, 1).blockAt(head.x(), head.y(), head.z()).legacyId() == 12,
        "suffocation left sand 12 absent");
    actor.close();
    server.save();
    String evidence = "cause=suffocate,column=" + column + ",head=" + head.x() + ":" + head.y() + ":" + head.z()
        + ":12:0,body=" + body.x() + ":" + body.y() + ":" + body.z()
        + ":12:0,health=20->19,packet8=19,status=2,clients=1,"
        + "disconnect=clean";
    String trace = "v1|server=official-b1.7.3|seed=" + seed
        + "|fixture=raised-stone+falling-sand12-head|cause=stand-under-falling-sand12"
        + "|wire=packet38-status2+packet8-health20->19"
        + "|oracle=suffocate-hurt-not-m465-death-not-m307-compound|" + evidence;
    System.out.println("WORLDLINE_M603_SET=" + evidence);
    System.out.println("WORLDLINE_M603_TRACE=" + trace);
    System.out.println("WORLDLINE_M603_SIGNATURE=" + SuffocationSetSupport.sha(trace));
  }
}
