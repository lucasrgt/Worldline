package worldline.smoke.wheatlighthaltsetb173;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Arrays;
import worldline.api.*;
import worldline.b173server.*;
import worldline.testkit.*;

/** Places lit wheat 59 plus covered wheat 59, then waits official random-tick age. */
public final class WheatLightHaltSetSmoke {
  private WheatLightHaltSetSmoke() {}

  public static void main(String[] a) throws Exception {
    if (a.length != 9)
      throw new IllegalArgumentException(
          "usage: WheatLightHaltSetSmoke server.jar workspace port seed username chunkX chunkZ windowTicks ageWindows");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    int cx = Integer.parseInt(a[5]), cz = Integer.parseInt(a[6]);
    int window = Integer.parseInt(a[7]), windows = Integer.parseInt(a[8]);
    WheatLightHaltSetArm.require(seed == 17320110707L && user.equals("WheatHalt577") && user.length() <= 16
            && window >= 1 && window <= 1200 && windows >= 1 && windows <= 40,
        "wheat-light-halt-set identity drift");
    Duration timeout = Duration.ofMinutes(20);
    B173DedicatedServer server = new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout), reader = null;
    BlockPosition top, east, west, north, south, east2, west2, north2, south2, waterPad, water;
    BlockPosition dirtE, dirtW, dirtN, dirtS, wheatE, wheatW, wheatN, wheatS, cover;
    int[] column = new int[1];
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1, 2, 3, 4},
          new int[] {1, 3, 9, 290, 295}, new int[] {64, 16, 8, 1, 8}, new int[] {0, 0, 0, 0, 0});
      actor.connect();
      actor.synchronizePose();
      WheatLightHaltSetArm.require(actor.awaitInventory().occupiedSlots() == 5, "wheat-light-halt inventory drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      top = WheatLightHaltSetArm.raise(actor, initial, cx, cz, column);
      actor.selectHeldSlot(0);
      east = WheatLightHaltSetArm.place(actor, top, BlockFace.EAST, 1);
      west = WheatLightHaltSetArm.place(actor, top, BlockFace.WEST, 1);
      north = WheatLightHaltSetArm.place(actor, top, BlockFace.NORTH, 1);
      south = WheatLightHaltSetArm.place(actor, top, BlockFace.SOUTH, 1);
      east2 = WheatLightHaltSetArm.place(actor, east, BlockFace.EAST, 1);
      west2 = WheatLightHaltSetArm.place(actor, west, BlockFace.WEST, 1);
      north2 = WheatLightHaltSetArm.place(actor, north, BlockFace.NORTH, 1);
      south2 = WheatLightHaltSetArm.place(actor, south, BlockFace.SOUTH, 1);
      waterPad = WheatLightHaltSetArm.place(actor, east, BlockFace.NORTH, 1);
      actor.selectHeldSlot(1);
      dirtE = WheatLightHaltSetArm.place(actor, east2, BlockFace.UP, 3);
      dirtW = WheatLightHaltSetArm.place(actor, west2, BlockFace.UP, 3);
      dirtN = WheatLightHaltSetArm.place(actor, north2, BlockFace.UP, 3);
      dirtS = WheatLightHaltSetArm.place(actor, south2, BlockFace.UP, 3);
      actor.selectHeldSlot(2);
      water = WheatLightHaltSetArm.place(actor, waterPad, BlockFace.UP, 9);
      actor.selectHeldSlot(0);
      BlockPosition post = WheatLightHaltSetArm.place(actor, south2, BlockFace.SOUTH, 1);
      post = WheatLightHaltSetArm.place(actor, post, BlockFace.UP, 1);
      post = WheatLightHaltSetArm.place(actor, post, BlockFace.UP, 1);
      post = WheatLightHaltSetArm.place(actor, post, BlockFace.UP, 1);
      cover = WheatLightHaltSetArm.place(actor, post, BlockFace.NORTH, 1);
      actor.selectHeldSlot(3);
      BlockPosition[] plots = new BlockPosition[] {dirtE, dirtW, dirtN, dirtS};
      WheatLightHaltSetArm.till(actor, plots);
      actor.selectHeldSlot(4);
      wheatE = WheatLightHaltSetArm.sow(actor, dirtE);
      wheatW = WheatLightHaltSetArm.sow(actor, dirtW);
      wheatN = WheatLightHaltSetArm.sow(actor, dirtN);
      wheatS = WheatLightHaltSetArm.sow(actor, dirtS);
      BlockPosition[] lit = new BlockPosition[] {wheatE, wheatW, wheatN};
      RemoteWorldView planted = worldline.test.WorldlineSmokeAwait.observe(actor, 5);
      WheatLightHaltSetArm.require(WheatLightHaltSetArm.crops(planted, lit, wheatS, cover),
          "pad cells missing before random-tick wait " + WheatLightHaltSetArm.dump(planted, lit, wheatS, cover));
      WheatLightHaltSetArm.waitAge(actor, lit, wheatS, window, windows);
      actor.close();
      WheatLightHaltSetArm.awaitPlayers(server, 0);
      server.save();
      reader = new B173WireClient("127.0.0.1", port, user, timeout);
      reader.connect();
      reader.synchronizePose();
      RemoteChunkSnapshot after = reader.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      WheatLightHaltSetArm.persist(after, cx, cz, plots, lit, wheatS, cover, water);
      BlockTickPolicyScenario scenario = new BlockTickPolicyScenario("wheat-random-growth",
          "b1.7.3:block/059", Arrays.asList("plant-growth"), false,
          BlockTickPolicyMechanism.RANDOM_BLOCK, "59:0-planted", "metadata-increased", true);
      BlockTickPolicyObservation observation = new BlockTickPolicyObservation(scenario.id(),
          scenario.mechanism(), scenario.initial(), scenario.effect(), scenario.persisted());
      String tickContract = WheatLightHaltSetArm.sha(BlockTickPolicyFixture.canonical(
          BlockTickPolicyFixture.execute(Arrays.asList(scenario), Arrays.asList(observation))));
      String evidence = "column=" + column[0] + ",support=" + WheatLightHaltSetArm.token(top, 1, 0)
          + ",water=" + WheatLightHaltSetArm.token(water, 9, 0) + ",hoe=290,seeds=295,wheat=59,lit="
          + WheatLightHaltSetArm.cells(lit) + ",covered=" + WheatLightHaltSetArm.token(wheatS, 59, 0)
          + ",cover=" + WheatLightHaltSetArm.token(cover, 1, 0)
          + ",lit-age>=1,dark-stay=true,persisted=true,testkit=" + tickContract
          + ",clients=2,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=raised-stone+hydrated-farmland60+lit-wheat59+covered-wheat59"
          + "|cause=packet15-hoe290+seeds295+random-ticks"
          + "|wire=packet53-crops59-age+covered-59:0"
          + "|oracle=lit-wheat-age+dark-wheat-halt+fresh-login|" + evidence;
      System.out.println("WORLDLINE_M577_SET=" + evidence);
      System.out.println("WORLDLINE_M577_TRACE=" + trace);
      System.out.println("WORLDLINE_M577_SIGNATURE=" + WheatLightHaltSetArm.sha(trace));
    } finally {
      actor.close();
      if (reader != null)
        reader.close();
      server.close();
    }
  }
}
