package worldline.smoke.redstoneoreglowsetb173;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Clicks and steps on placed redstone ore 73, then proves official random-tick fade. */
public final class RedstoneOreGlowSetSmoke {
  private RedstoneOreGlowSetSmoke() {}

  public static void main(String[] args) throws Exception {
    if (args.length != 8)
      throw new IllegalArgumentException(
          "usage: RedstoneOreGlowSetSmoke server.jar workspace port seed username chunkX chunkZ fadeTicks");
    Path jar = Paths.get(args[0]), workspace = Paths.get(args[1]);
    int port = Integer.parseInt(args[2]);
    long seed = Long.parseLong(args[3]);
    String user = args[4];
    int cx = Integer.parseInt(args[5]), cz = Integer.parseInt(args[6]);
    int fade = Integer.parseInt(args[7]);
    RedstoneOreGlowSetArm.require(
        seed == 17320110707L && user.equals("RsOreGlow571") && user.length() <= 16 && fade >= 1 && fade <= 1200,
        "redstone-ore-glow-set identity drift");
    Duration timeout = Duration.ofMinutes(20);
    B173DedicatedServer server = new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout), reader = null;
    int[] column = new int[1];
    try {
      server.boot();
      B173PlayerSeed.writeInventory(
          workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1}, new int[] {1, 73}, new int[] {32, 1}, new int[] {0, 0});
      actor.connect();
      actor.synchronizePose();
      actor.look(-90F, 0F);
      RedstoneOreGlowSetArm.require(actor.awaitInventory().occupiedSlots() == 2, "redstone-ore-glow inventory drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      BlockPosition top = RedstoneOreGlowSetArm.raise(actor, initial, cx, cz, column);
      PlayerPose pose = actor.moveAndObserve(0D, 0D, 0D, 1).resulting();
      BlockPosition perch = BlockFace.WEST.adjacent(top);
      BlockPosition ore = BlockFace.EAST.adjacent(top);
      RedstoneOreGlowSetArm.require(RedstoneOreGlowSetArm.at(initial, perch, cx, cz).legacyId() == 0
              && RedstoneOreGlowSetArm.at(initial, ore, cx, cz).legacyId() == 0,
          "ore targets were not initial air");
      RedstoneOreGlowSetArm.place(actor, top, BlockFace.WEST, 1);
      actor.selectHeldSlot(1);
      RedstoneOreGlowSetArm.place(actor, top, BlockFace.EAST, 73);
      RedstoneOreGlowSetArm.awaitOre(actor, ore, RedstoneOreGlowSetArm.UNLIT, 20, "placed unlit redstone ore 73:0");
      actor.selectHeldSlot(2);
      pose = RedstoneOreGlowSetArm.walk(actor, pose, perch.x() + 0.5D, perch.y() + 1.0D, perch.z() + 0.5D);
      actor.activateBlock(ore, BlockFace.WEST);
      RedstoneOreGlowSetArm.awaitOre(actor, ore, RedstoneOreGlowSetArm.GLOW, 40, "click glow 73:0->74:0");
      worldline.test.WorldlineSmokeAwait.observe(actor, fade);
      actor.close();
      RedstoneOreGlowSetArm.awaitPlayers(server, 0);
      server.save();
      actor = new B173WireClient("127.0.0.1", port, user, timeout);
      actor.connect();
      pose = actor.synchronizePose();
      RedstoneOreGlowSetArm.require(actor.awaitRemoteChunk(cx, cz)
                                        .chunkAt(cx, cz)
                                        .blockAt(ore.x() - cx * 16, ore.y(), ore.z() - cz * 16)
                                        .equals(RedstoneOreGlowSetArm.UNLIT),
          "click-dark persist 73:0 drift");
      pose = RedstoneOreGlowSetArm.walk(actor, pose, ore.x() + 0.5D, ore.y() + 1.0D, ore.z() + 0.5D);
      RedstoneOreGlowSetArm.stepOn(actor, ore);
      pose = actor.moveAndObserve(0D, 0D, 0D, 1).resulting();
      RedstoneOreGlowSetArm.walk(actor, pose, perch.x() + 0.5D, perch.y() + 1.0D, perch.z() + 0.5D);
      worldline.test.WorldlineSmokeAwait.observe(actor, fade);
      actor.close();
      RedstoneOreGlowSetArm.awaitPlayers(server, 0);
      server.save();
      reader = new B173WireClient("127.0.0.1", port, user, timeout);
      reader.connect();
      reader.synchronizePose();
      RemoteChunkSnapshot after = reader.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      RedstoneOreGlowSetArm.require(RedstoneOreGlowSetArm.at(after, top, cx, cz).equals(new BlockState(1, 0))
              && RedstoneOreGlowSetArm.at(after, perch, cx, cz).equals(new BlockState(1, 0))
              && RedstoneOreGlowSetArm.at(after, ore, cx, cz).equals(RedstoneOreGlowSetArm.UNLIT),
          "persisted unlit redstone ore 73:0 drift");
      String evidence = "column=" + column[0] + ",support=" + RedstoneOreGlowSetArm.cell(top)
          + ":1:0,perch=" + RedstoneOreGlowSetArm.cell(perch) + ":1:0,ore=" + RedstoneOreGlowSetArm.cell(ore)
          + ":73:0,click=73:0->74:0,"
          + "click-dark=74:0->73:0,step=73:0->74:0,step-dark=74:0->73:0,persisted=true,"
          + "clients=3,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed + "|fixture=raised-stone+ore73-east-floor+west-perch"
          + "|cause=empty-hand-packet15-activate+moveAndObserve-on-ore+random-ticks"
          + "|wire=packet53-ore73:0->74:0->73:0"
          + "|oracle=click-glow+step-glow+random-tick-darken+fresh-login-unlit73:0|" + evidence;
      System.out.println("WORLDLINE_M571_SET=" + evidence);
      System.out.println("WORLDLINE_M571_TRACE=" + trace);
      System.out.println("WORLDLINE_M571_SIGNATURE=" + RedstoneOreGlowSetArm.sha(trace));
    } finally {
      actor.close();
      if (reader != null)
        reader.close();
      server.close();
    }
  }
}
