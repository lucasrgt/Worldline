package worldline.smoke.saplinggrowthsetb173;
import static worldline.b173server.B173FixtureSupport.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Bonemeal-grows oak 6:0, spruce 6:1, and birch 6:2 into matching log 17 damages. */
public final class SaplingGrowthSetSmoke {
  private SaplingGrowthSetSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 9)
      throw new IllegalArgumentException(
          "usage: SaplingGrowthSetSmoke server.jar workspace port seed username chunkX chunkZ windowTicks growthWindows");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    int cx = Integer.parseInt(a[5]), cz = Integer.parseInt(a[6]), window = Integer.parseInt(a[7]),
        windows = Integer.parseInt(a[8]);
    Duration timeout = Duration.ofMinutes(10);
    require(user.length() <= 16 && window >= 1 && windows >= 1, "sapling-growth-set arguments");
    B173DedicatedServer server =
        new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout), reader = null;
    BlockPosition top, east, south, oakDirt, spruceDirt, birchDirt, oak, spruce, birch;
    int column;
    PlayerPose pose;
    BlockState oakLog, spruceLog, birchLog;
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1, 2, 3, 4, 5},
          new int[] {1, 3, 6, 6, 6, 351}, new int[] {64, 16, 4, 4, 4, 64},
          new int[] {0, 0, 0, 1, 2, 15});
      actor.connect();
      pose = actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() == 6, "sapling-growth-set inventory drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      top = foundation(initial, cx, cz);
      column = 0;
      actor.selectHeldSlot(0);
      while (
          water(initial.blockAt(local(top.x(), cx), top.y() + 1, local(top.z(), cz)).legacyId())) {
        top = place(actor, top, BlockFace.UP, 1);
        pose = actor.moveAndObserve(0D, 1D, 0D, 1).resulting();
        require(++column <= 15, "water column exceeded sapling-growth-set fixture");
      }
      for (int lift = 0; lift < 8; lift++) {
        top = place(actor, top, BlockFace.UP, 1);
        pose = actor.moveAndObserve(0D, 1D, 0D, 1).resulting();
        column++;
      }
      pose = go(actor, pose, top.x() + 0.5D, top.y() + 3.0D, top.z() + 0.5D);
      east = span(actor, top, 1D, 0D, BlockFace.EAST, 5);
      actor.selectHeldSlot(1);
      spruceDirt = place(actor, east, BlockFace.UP, 3);
      actor.selectHeldSlot(3);
      spruce = plant(actor, spruceDirt, 1);
      pose = go(actor, pose, spruce.x() + 0.5D, top.y() + 7.0D, spruce.z() + 0.5D);
      actor.selectHeldSlot(5);
      grow(actor, spruce, 1, window, windows);
      pose = go(actor, pose, top.x() + 0.5D, top.y() + 3.0D, top.z() + 0.5D);
      actor.selectHeldSlot(0);
      south = span(actor, top, 0D, 1D, BlockFace.SOUTH, 5);
      actor.selectHeldSlot(1);
      birchDirt = place(actor, south, BlockFace.UP, 3);
      actor.selectHeldSlot(4);
      birch = plant(actor, birchDirt, 2);
      pose = go(actor, pose, birch.x() + 0.5D, top.y() + 7.0D, birch.z() + 0.5D);
      actor.selectHeldSlot(5);
      grow(actor, birch, 2, window, windows);
      pose = go(actor, pose, top.x() + 0.5D, top.y() + 3.0D, top.z() + 0.5D);
      actor.selectHeldSlot(1);
      oakDirt = place(actor, top, BlockFace.UP, 3);
      actor.selectHeldSlot(2);
      oak = plant(actor, oakDirt, 0);
      pose = go(actor, pose, oak.x() + 0.5D, top.y() + 7.0D, oak.z() + 0.5D);
      actor.selectHeldSlot(5);
      grow(actor, oak, 0, window, windows);
      RemoteWorldView live = worldline.test.WorldlineSmokeAwait.observe(actor, 10);
      oakLog = at(live, oak);
      spruceLog = at(live, spruce);
      birchLog = at(live, birch);
      require(wood(oakLog, 0) && wood(spruceLog, 1) && wood(birchLog, 2),
          "live sapling-to-log drift: oak=" + oakLog + " spruce=" + spruceLog
              + " birch=" + birchLog);
      actor.close();
      awaitPlayers(server, 0);
      server.save();
      reader = new B173WireClient("127.0.0.1", port, user, timeout);
      reader.connect();
      reader.synchronizePose();
      RemoteChunkSnapshot after = reader.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      require(wood(at(after, oak, cx, cz), 0) && wood(at(after, spruce, cx, cz), 1)
              && wood(at(after, birch, cx, cz), 2),
          "persisted sapling-growth-set drift");
      String evidence = "column=" + column + ",oak=" + cell(oak)
          + ":6:0->17:0,spruce=" + cell(spruce) + ":6:1->17:1,birch=" + cell(birch)
          + ":6:2->17:2,bonemeal=351:15,saplings=6:0+6:1+6:2,logs=17:0+17:1+17:2,persisted=true,clients=2,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=raised-dirt3+sapling6:0+6:1+6:2|cause=packet15-bonemeal351:15|wire=packet53-log17:0+17:1+17:2|oracle=bonemeal-oak-spruce-birch-root-logs+fresh-login|"
          + evidence;
      System.out.println("WORLDLINE_M339_SET=" + evidence);
      System.out.println("WORLDLINE_M339_TRACE=" + trace);
      System.out.println("WORLDLINE_M339_SIGNATURE=" + sha(trace));
    } finally {
      actor.close();
      if (reader != null)
        reader.close();
      server.close();
    }
  }
  private static BlockPosition plant(B173WireClient a, BlockPosition dirt, int meta)
      throws Exception {
    BlockPosition sapling = BlockFace.UP.adjacent(dirt);
    a.placeHeldBlock(dirt, BlockFace.UP);
    a.awaitBlock(sapling, new BlockState(6, meta));
    return sapling;
  }
  private static BlockPosition span(B173WireClient a, BlockPosition from, double dx, double dz,
      BlockFace face, int n) throws Exception {
    BlockPosition p = from;
    for (int i = 0; i < n; i++) {
      p = place(a, p, face, 1);
      a.moveAndObserve(dx, 0D, dz, 1);
    }
    return p;
  }
  private static PlayerPose go(B173WireClient a, PlayerPose p, double x, double y, double z) {
    for (int i = 0; i < 16
        && (Math.abs(p.x() - x) > 0.4D || Math.abs(p.y() - y) > 0.4D || Math.abs(p.z() - z) > 0.4D);
        i++)
      p = a.moveAndObserve(clamp(x - p.x()), clamp(y - p.y()), clamp(z - p.z()), 1).resulting();
    return p;
  }
  private static double clamp(double v) {
    return v > 1D ? 1D : v < -1D ? -1D : v;
  }
  private static void grow(B173WireClient a, BlockPosition root, int meta, int window, int windows)
      throws Exception {
    BlockState grown = worldline.test.WorldlineSmokeAwait.awaitCheckedEntityOrNull(a, () -> {
      a.useHeldItemOnBlock(root, BlockFace.UP);
      return at(worldline.test.WorldlineSmokeAwait.observe(a, 4), root);
    }, s -> wood(s, meta), "bonemeal sapling " + meta, 48);
    if (grown == null)
      worldline.test.WorldlineSmokeAwait.awaitBlockMatching(
          a, root, s -> wood(s, meta), "random-tick sapling " + meta, window * windows);
  }
  private static boolean sapling(BlockState s, int meta) {
    return s.legacyId() == 6 && (s.metadata() & 3) == meta;
  }
  private static boolean wood(BlockState s, int meta) {
    return s.legacyId() == 17 && (s.metadata() & 3) == meta;
  }
  private static BlockPosition foundation(RemoteChunkSnapshot q, int cx, int cz) {
    for (int x = 4; x <= 11; x++)
      for (int z = 4; z <= 11; z++)
        for (int y = 126; y >= 1; y--)
          if (q.blockAt(x, y, z).legacyId() == 3 && water(q.blockAt(x, y + 1, z).legacyId()))
            return new BlockPosition(cx * 16 + x, y, cz * 16 + z);
    throw new IllegalStateException("no deterministic sapling-growth-set foundation");
  }
  private static BlockState at(RemoteWorldView v, BlockPosition p) {
    return v.blockAt(p.x(), p.y(), p.z());
  }
  private static BlockState at(RemoteChunkSnapshot q, BlockPosition p, int cx, int cz) {
    return q.blockAt(local(p.x(), cx), p.y(), local(p.z(), cz));
  }
  private static String cell(BlockPosition p) {
    return p.x() + ":" + p.y() + ":" + p.z();
  }
  private static void require(boolean v, String m) {
    if (!v)
      throw new IllegalStateException(m);
  }
}
