package worldline.smoke.watercobblesetb173;
import static worldline.b173server.B173FixtureSupport.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.*;
import worldline.api.*;
import worldline.b173server.*;

/** FLOWING lava 10 plus water 9 hardens to cobble 4 in two official flow geometries. */
public final class WaterCobbleSetSmoke {
  private WaterCobbleSetSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 9)
      throw new IllegalArgumentException(
          "usage: WaterCobbleSetSmoke server.jar workspace port seed username chunkX chunkZ fixtureTicks flowTicks");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    int cx = Integer.parseInt(a[5]), cz = Integer.parseInt(a[6]),
        fixtureTicks = Integer.parseInt(a[7]), flowTicks = Integer.parseInt(a[8]);
    require(seed == 17320110707L && user.equals("WatCob415") && user.length() <= 16,
        "water-cobble-set identity drift");
    Duration timeout = Duration.ofSeconds(180);
    B173DedicatedServer server =
        new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout), reader = null;
    BlockState air = new BlockState(0, 0), cobble = new BlockState(4, 0);
    int column;
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1, 2, 3, 4},
          new int[] {1, 1, 3, 11, 9}, new int[] {64, 64, 4, 2, 2}, new int[] {0, 0, 0, 0, 0});
      actor.connect();
      PlayerPose pose = actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() == 5, "water-cobble-set inventory drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      BlockPosition top = foundation(initial, cx, cz);
      column = 0;
      actor.selectHeldSlot(0);
      while (
          water(initial.blockAt(local(top.x(), cx), top.y() + 1, local(top.z(), cz)).legacyId())) {
        top = place(actor, top, BlockFace.UP, 1);
        pose = actor.moveAndObserve(0D, 1D, 0D, 1).resulting();
        require(++column <= 15, "water column exceeded water-cobble-set fixture");
      }
      for (int lift = 0; lift < 8; lift++) {
        top = place(actor, top, BlockFace.UP, 1);
        pose = actor.moveAndObserve(0D, 1D, 0D, 1).resulting();
        column++;
      }
      actor.selectHeldSlot(1);
      BlockPosition row = top;
      for (int step = 0; step < 2; step++) {
        row = place(actor, row, BlockFace.SOUTH, 1);
        pose = actor.moveAndObserve(0D, 0D, 1D, 1).resulting();
      }
      BlockPosition[] east = trench(actor, row);
      for (int step = 0; step < 2; step++) {
        row = place(actor, row, BlockFace.SOUTH, 1);
        pose = actor.moveAndObserve(0D, 0D, 1D, 1).resulting();
      }
      BlockPosition[] south = trench(actor, row);
      while (pose.z() > top.z() + 0.6D)
        pose = actor.moveAndObserve(0D, 0D, -1D, 1).resulting();
      actor.moveAndObserve(0D, -2D, 0D, 2);
      actor.selectHeldSlot(2);
      place(actor, BlockFace.DOWN.adjacent(east[1]), BlockFace.UP, 3);
      place(actor, BlockFace.DOWN.adjacent(east[2]), BlockFace.UP, 3);
      place(actor, BlockFace.DOWN.adjacent(south[1]), BlockFace.UP, 3);
      place(actor, BlockFace.DOWN.adjacent(south[2]), BlockFace.UP, 3);
      actor.selectHeldSlot(3);
      place(actor, BlockFace.DOWN.adjacent(east[0]), BlockFace.UP, 11);
      place(actor, BlockFace.DOWN.adjacent(south[0]), BlockFace.UP, 11);
      worldline.test.WorldlineSmokeAwait.observe(actor, fixtureTicks);
      require(open(actor, east[1]).equals(air), "east dirt-gate air absent");
      BlockState eastFlow = settle(actor, east[1], flowTicks);
      require(open(actor, south[1]).equals(air), "south dirt-gate air absent");
      BlockState southFlow = settle(actor, south[1], flowTicks);
      require(flowingLava(eastFlow) && flowingLava(southFlow),
          "flowing lava 10/11-meta absent: " + eastFlow + " / " + southFlow);
      actor.selectHeldSlot(4);
      require(open(actor, east[2]).equals(air), "east water-pocket air absent");
      place(actor, BlockFace.DOWN.adjacent(east[2]), BlockFace.UP, 9);
      require(open(actor, south[2]).equals(air), "south water-pocket air absent");
      place(actor, BlockFace.DOWN.adjacent(south[2]), BlockFace.UP, 9);
      actor.awaitBlock(east[1], cobble);
      actor.awaitBlock(south[1], cobble);
      RemoteWorldView live = worldline.test.WorldlineSmokeAwait.observe(actor, 5);
      BlockState eC = live.blockAt(east[1].x(), east[1].y(), east[1].z()),
                 sC = live.blockAt(south[1].x(), south[1].y(), south[1].z()),
                 eS = live.blockAt(east[0].x(), east[0].y(), east[0].z()),
                 sS = live.blockAt(south[0].x(), south[0].y(), south[0].z()),
                 eW = live.blockAt(east[2].x(), east[2].y(), east[2].z()),
                 sW = live.blockAt(south[2].x(), south[2].y(), south[2].z());
      require(eC.equals(cobble) && sC.equals(cobble), "live cobble drift " + eC + " / " + sC);
      require(lavaSource(eS) && lavaSource(sS), "live source drift " + eS + " / " + sS);
      require(water(eW.legacyId()) && water(sW.legacyId()), "live water drift " + eW + " / " + sW);
      require(actor.health() == 20, "live health drift " + actor.health());
      actor.close();
      awaitPlayers(server, 0);
      server.save();
      reader = new B173WireClient("127.0.0.1", port, user, timeout);
      reader.connect();
      reader.synchronizePose();
      RemoteChunkSnapshot after = reader.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      BlockState peC = after.blockAt(local(east[1].x(), cx), east[1].y(), local(east[1].z(), cz)),
                 psC =
                     after.blockAt(local(south[1].x(), cx), south[1].y(), local(south[1].z(), cz)),
                 peS = after.blockAt(local(east[0].x(), cx), east[0].y(), local(east[0].z(), cz)),
                 psS =
                     after.blockAt(local(south[0].x(), cx), south[0].y(), local(south[0].z(), cz)),
                 peW = after.blockAt(local(east[2].x(), cx), east[2].y(), local(east[2].z(), cz)),
                 psW =
                     after.blockAt(local(south[2].x(), cx), south[2].y(), local(south[2].z(), cz));
      require(peC.equals(cobble) && psC.equals(cobble), "fresh cobble drift " + peC + " / " + psC);
      require(lavaSource(peS) && lavaSource(psS), "fresh source drift " + peS + " / " + psS);
      require(
          water(peW.legacyId()) && water(psW.legacyId()), "fresh water drift " + peW + " / " + psW);
      String evidence = "column=" + column + ",east-source=" + east[0].x() + ":" + east[0].y() + ":"
          + east[0].z() + ":11:0,east-flow=" + east[1].x() + ":" + east[1].y() + ":" + east[1].z()
          + ":3:0->0:0->" + eastFlow.legacyId() + ":" + eastFlow.metadata()
          + "->4:0,east-water=" + east[2].x() + ":" + east[2].y() + ":" + east[2].z()
          + ":9:0,south-source=" + south[0].x() + ":" + south[0].y() + ":" + south[0].z()
          + ":11:0,south-flow=" + south[1].x() + ":" + south[1].y() + ":" + south[1].z()
          + ":3:0->0:0->" + southFlow.legacyId() + ":" + southFlow.metadata()
          + "->4:0,south-water=" + south[2].x() + ":" + south[2].y() + ":" + south[2].z()
          + ":9:0,flowing-lava=10,moving=10:0,water=9,cobble=4,persisted=true,clients=2,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=raised-stone-paired-trenches+seeded-still-lava11+dirt-gates3|settle="
          + fixtureTicks + "+" + flowTicks
          + "ticks|cause=packet14-open-flow-cells-then-packet15-still-water9-beside-flowing-lava|wire=packet53-air+packet53-flowing-lava10-as-11:2+packet53-water9+packet53-cobble4|oracle=two-cell-flowing-lava10-plus-water-to-cobble4-not-obsidian49|"
          + evidence;
      System.out.println("WORLDLINE_M415_SET=" + evidence);
      System.out.println("WORLDLINE_M415_TRACE=" + trace);
      System.out.println("WORLDLINE_M415_SIGNATURE=" + sha(trace));
    } finally {
      actor.close();
      if (reader != null)
        reader.close();
      try {
        server.close();
      } catch (RuntimeException e) {
        if (e.getMessage() == null
            || (!e.getMessage().contains("pipe") && !e.getMessage().contains("PIPE")
                && !e.getMessage().contains("EOF")))
          throw e;
      }
    }
  }
  private static BlockPosition[] trench(B173WireClient a, BlockPosition top) throws Exception {
    BlockPosition west = place(a, top, BlockFace.WEST, 1), east = place(a, top, BlockFace.EAST, 1),
                  east2 = place(a, east, BlockFace.EAST, 1),
                  east3 = place(a, east2, BlockFace.EAST, 1);
    List<BlockPosition> floor = new ArrayList<>(Arrays.asList(west, top, east, east2, east3));
    for (BlockPosition p : new ArrayList<>(floor)) {
      floor.add(place(a, p, BlockFace.NORTH, 1));
      floor.add(place(a, p, BlockFace.SOUTH, 1));
    }
    for (BlockPosition p : floor) {
      int dx = p.x() - top.x(), dz = p.z() - top.z();
      if (dx == -1 || dx == 3 || dz == -1 || dz == 1)
        place(a, p, BlockFace.UP, 1);
    }
    return new BlockPosition[] {
        BlockFace.UP.adjacent(top), BlockFace.UP.adjacent(east), BlockFace.UP.adjacent(east2)};
  }
  private static BlockState open(B173WireClient a, BlockPosition target) throws Exception {
    a.beginBreak(target);
    Thread.sleep(3000L);
    a.finishBreak(target);
    return a.awaitBlock(target, new BlockState(0, 0)).blockAt(target.x(), target.y(), target.z());
  }
  private static BlockState settle(B173WireClient a, BlockPosition cell, int budget)
      throws Exception {
    return worldline.test.WorldlineSmokeAwait.awaitBlockMatching(
        a, cell, WaterCobbleSetSmoke::flowingLava, "flowing lava", budget);
  }
  private static boolean flowingLava(BlockState s) {
    return (s.legacyId() == 10 || s.legacyId() == 11) && s.metadata() > 0 && s.metadata() <= 4;
  }
  private static boolean lavaSource(BlockState s) {
    return (s.legacyId() == 10 || s.legacyId() == 11) && s.metadata() == 0;
  }
  private static BlockPosition foundation(RemoteChunkSnapshot q, int cx, int cz) {
    for (int x = 4; x <= 11; x++)
      for (int z = 4; z <= 11; z++)
        for (int y = 126; y >= 1; y--)
          if (q.blockAt(x, y, z).legacyId() == 3 && water(q.blockAt(x, y + 1, z).legacyId()))
            return new BlockPosition(cx * 16 + x, y, cz * 16 + z);
    throw new IllegalStateException("no deterministic water-cobble-set foundation");
  }
  private static void require(boolean v, String m) {
    if (!v)
      throw new IllegalStateException(m);
  }
}
