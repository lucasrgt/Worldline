package worldline.smoke.remainingpaintingmotivesb173;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Places painting item 321 on remaining 4x2, 4x3, and 4x4 west-face walls and correlates Packet25. */
public final class RemainingPaintingMotivesSmoke {
  private RemainingPaintingMotivesSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 8)
      throw new IllegalArgumentException(
          "usage: RemainingPaintingMotivesSmoke server.jar workspace port seed actor observer chunkX chunkZ");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String actorName = a[4], observerName = a[5];
    int cx = Integer.parseInt(a[6]), cz = Integer.parseInt(a[7]);
    Duration timeout = Duration.ofSeconds(90);
    require(
        actorName.length() <= 16 && observerName.length() <= 16 && !actorName.equals(observerName),
        "username drift");
    B173DedicatedServer server =
        new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, actorName, timeout),
                   observer = new B173WireClient("127.0.0.1", port, observerName, timeout);
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, actorName, 4.5D, 60D, 4.5D, new int[] {0, 1},
          new int[] {1, 321}, new int[] {64, 3}, new int[] {0, 0});
      B173PlayerSeed.write(workspace, observerName, 4.5D, 80D, 4.5D);
      actor.connect();
      actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() == 2, "painting inventory drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      BlockPosition top = foundation(initial, cx, cz), walk = top;
      int column = 0;
      actor.selectHeldSlot(0);
      while (
          water(initial.blockAt(local(top.x(), cx), top.y() + 1, local(top.z(), cz)).legacyId())) {
        top = place(actor, top, BlockFace.UP);
        walk = top;
        actor.moveAndObserve(0D, 1D, 0D, 1);
        require(++column <= 15, "water column exceeded remaining-painting-motives fixture");
      }
      for (int lift = 0; lift < 8; lift++) {
        top = place(actor, top, BlockFace.UP);
        walk = top;
        actor.moveAndObserve(0D, 1D, 0D, 1);
        column++;
      }
      BlockPosition[][] wall42 = wall(actor, walk, 4, 2);
      for (int step = 0; step < 4; step++) {
        walk = place(actor, walk, BlockFace.SOUTH);
        actor.moveAndObserve(0D, 0D, 1D, 1);
      }
      BlockPosition[][] wall43 = wall(actor, walk, 4, 3);
      for (int step = 0; step < 4; step++) {
        walk = place(actor, walk, BlockFace.SOUTH);
        actor.moveAndObserve(0D, 0D, 1D, 1);
      }
      BlockPosition[][] wall44 = wall(actor, walk, 4, 4);
      observer.connect();
      observer.synchronizePose();
      observer.awaitRemoteChunk(cx, cz);
      actor.selectHeldSlot(1);
      RemotePaintingSpawn art44 = hang(actor, observer, wall44[1][1], 4, 4);
      for (int step = 0; step < 4; step++)
        actor.moveAndObserve(0D, 0D, -1D, 1);
      RemotePaintingSpawn art43 = hang(actor, observer, wall43[1][1], 4, 3);
      for (int step = 0; step < 4; step++)
        actor.moveAndObserve(0D, 0D, -1D, 1);
      RemotePaintingSpawn art42 = hang(actor, observer, wall42[0][1], 4, 2);
      require(art42.entityId() != art43.entityId() && art43.entityId() != art44.entityId()
              && art42.entityId() != art44.entityId() && art42.direction() == 1
              && art43.direction() == 1 && art44.direction() == 1,
          "remaining painting motive identity drift");
      actor.close();
      observer.close();
      awaitPlayers(server, 0);
      server.save();
      String evidence = "column=" + column + ",wall4x2=" + span(wall42) + ",art4x2=" + pose(art42)
          + ",wall4x3=" + span(wall43) + ",art4x3=" + pose(art43) + ",wall4x4=" + span(wall44)
          + ",art4x4=" + pose(art44)
          + ",sizes=4x2+4x3+4x4,packet25+packet25+packet25,clients=2,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=raised-stone-walls-4x2+4x3+4x4|cause=packet15-item321-4x2+packet15-item321-4x3+packet15-item321-4x4|wire=packet25+packet25+packet25|oracle=two-peer-identical-painting-spawns-remaining-sizes|"
          + evidence;
      System.out.println("WORLDLINE_M430_SET=" + evidence);
      System.out.println("WORLDLINE_M430_TRACE=" + trace);
      System.out.println("WORLDLINE_M430_SIGNATURE=" + sha(trace));
    } finally {
      actor.close();
      observer.close();
      server.close();
    }
  }
  private static RemotePaintingSpawn hang(B173WireClient actor, B173WireClient observer,
      BlockPosition click, int maxW, int maxH) throws Exception {
    actor.look(-90F, 0F);
    actor.useHeldItemOnBlock(click, BlockFace.WEST);
    RemotePaintingSpawn spawn = B173PaintingAccess.await(actor),
                        peer = B173PaintingAccess.await(observer);
    int[] d = dim(spawn.title());
    require(spawn.equals(peer) && spawn.packet() == 25 && spawn.direction() == 1
            && spawn.x() == click.x() && spawn.y() == click.y() && spawn.z() == click.z()
            && spawn.entityId() != actor.state().entityId()
            && spawn.entityId() != observer.state().entityId() && d[0] <= maxW && d[1] <= maxH,
        "peer remaining painting spawn drift");
    return spawn;
  }
  private static BlockPosition[][] wall(B173WireClient a, BlockPosition walk, int w, int h)
      throws Exception {
    return panel(a, place(a, place(a, walk, BlockFace.EAST), BlockFace.UP), BlockFace.SOUTH, w, h);
  }
  private static BlockPosition[][] panel(
      B173WireClient a, BlockPosition first, BlockFace across, int w, int h) throws Exception {
    BlockPosition[][] cells = new BlockPosition[h][w];
    cells[0][0] = first;
    for (int x = 1; x < w; x++)
      cells[0][x] = place(a, cells[0][x - 1], across);
    for (int y = 1; y < h; y++) {
      cells[y][0] = place(a, cells[y - 1][0], BlockFace.UP);
      for (int x = 1; x < w; x++)
        cells[y][x] = place(a, cells[y][x - 1], across);
    }
    return cells;
  }
  private static BlockPosition place(B173WireClient a, BlockPosition support, BlockFace face)
      throws Exception {
    BlockPosition target = face.adjacent(support);
    a.placeHeldBlock(support, face);
    a.awaitBlock(target, new BlockState(1, 0));
    return target;
  }
  private static BlockPosition foundation(RemoteChunkSnapshot q, int cx, int cz) {
    for (int x = 4; x <= 11; x++)
      for (int z = 4; z <= 11; z++)
        for (int y = 126; y >= 1; y--)
          if (q.blockAt(x, y, z).legacyId() == 3 && water(q.blockAt(x, y + 1, z).legacyId()))
            return new BlockPosition(cx * 16 + x, y, cz * 16 + z);
    throw new IllegalStateException("no deterministic remaining-painting-motives foundation");
  }
  private static String span(BlockPosition[][] w) {
    BlockPosition a = w[0][0], b = w[w.length - 1][w[0].length - 1];
    return a.x() + ":" + a.y() + ":" + a.z() + "-" + b.x() + ":" + b.y() + ":" + b.z() + ":1:0";
  }
  private static String pose(RemotePaintingSpawn p) {
    return p.x() + ":" + p.y() + ":" + p.z() + ":dir" + p.direction();
  }
  private static int[] dim(String t) {
    String[] n = {"Kebab", "Aztec", "Alban", "Aztec2", "Bomb", "Plant", "Wasteland", "Pool",
        "Courbet", "Sea", "Sunset", "Creebet", "Wanderer", "Graham", "Match", "Bust", "Stage",
        "Void", "SkullAndRoses", "Fighters", "Pointer", "Pigscene", "BurningSkull", "Skeleton",
        "DonkeyKong"};
    int[] width = {1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 1, 1, 2, 2, 2, 2, 2, 4, 4, 4, 4, 4, 4},
          height = {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 4, 4, 4, 3, 3};
    for (int i = 0; i < n.length; i++)
      if (n[i].equals(t))
        return new int[] {width[i], height[i]};
    throw new IllegalStateException("unknown art");
  }
  private static boolean water(int id) {
    return id == 8 || id == 9;
  }
  private static int local(int v, int c) {
    return v - c * 16;
  }
  private static void awaitPlayers(B173DedicatedServer s, int n) throws Exception {
    long e = System.currentTimeMillis() + 5000;
    while (System.currentTimeMillis() < e) {
      if (s.players().size() == n)
        return;
      Thread.sleep(100);
    }
    throw new IllegalStateException("player count drift");
  }
  private static String sha(String s) throws Exception {
    byte[] b = MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));
    StringBuilder v = new StringBuilder();
    for (byte x : b)
      v.append(String.format("%02x", x & 255));
    return v.toString();
  }
  private static void require(boolean v, String m) {
    if (!v)
      throw new IllegalStateException(m);
  }
}
