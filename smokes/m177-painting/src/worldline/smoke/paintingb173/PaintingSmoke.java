package worldline.smoke.paintingb173;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Places one official painting on a raised 2x2 stone wall and correlates Packet25. */
public final class PaintingSmoke {
  private PaintingSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 8)
      throw new IllegalArgumentException(
          "usage: PaintingSmoke server.jar workspace port seed actor observer chunkX chunkZ");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String actorName = a[4], observerName = a[5];
    int cx = Integer.parseInt(a[6]), cz = Integer.parseInt(a[7]);
    Duration timeout = Duration.ofSeconds(90);
    B173DedicatedServer server =
        new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, actorName, timeout),
                   observer = new B173WireClient("127.0.0.1", port, observerName, timeout);
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, actorName, 4.5D, 60D, 4.5D, new int[] {0, 1},
          new int[] {1, 321}, new int[] {32, 1}, new int[] {0, 0});
      B173PlayerSeed.write(workspace, observerName, 4.5D, 80D, 4.5D);
      actor.connect();
      actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() == 2, "painting inventory drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      BlockPosition top = foundation(initial, cx, cz);
      int column = 0;
      actor.selectHeldSlot(0);
      while (
          water(initial.blockAt(local(top.x(), cx), top.y() + 1, local(top.z(), cz)).legacyId())) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        require(++column <= 15, "water column exceeded painting fixture");
      }
      for (int lift = 0; lift < 8; lift++) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        column++;
      }
      BlockPosition support = place(actor, top, BlockFace.EAST, 1),
                    south = place(actor, support, BlockFace.UP, 1),
                    north = place(actor, south, BlockFace.SOUTH, 1),
                    southTop = place(actor, south, BlockFace.UP, 1);
      place(actor, north, BlockFace.UP, 1);
      observer.connect();
      observer.synchronizePose();
      observer.awaitRemoteChunk(cx, cz);
      actor.selectHeldSlot(1);
      actor.look(-90F, 0F);
      actor.useHeldItemOnBlock(south, BlockFace.WEST);
      RemotePaintingSpawn spawn = B173PaintingAccess.await(actor),
                          peer = B173PaintingAccess.await(observer);
      require(spawn.equals(peer) && spawn.entityId() != actor.state().entityId()
              && spawn.entityId() != observer.state().entityId(),
          "peer painting spawn drift");
      actor.close();
      observer.close();
      awaitPlayers(server, 0);
      server.save();
      String evidence = "column=" + column + ",wall=" + south.x() + ":" + south.y() + ":"
          + south.z() + "-" + southTop.x() + ":" + southTop.y() + ":" + north.z()
          + ":1:0,painting=" + spawn.x() + ":" + spawn.y() + ":" + spawn.z() + ":dir"
          + spawn.direction() + ",shared-title+shared-id,packet25,clients=2,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=raised-2x2-stone-wall|cause=packet15-item321-west|wire=packet25|oracle=two-peer-identical-painting-spawn|"
          + evidence;
      System.out.println("WORLDLINE_M177_PAINTING=" + evidence);
      System.out.println("WORLDLINE_M177_TRACE=" + trace);
      System.out.println("WORLDLINE_M177_SIGNATURE=" + sha(trace));
    } finally {
      actor.close();
      observer.close();
      server.close();
    }
  }
  private static BlockPosition place(
      B173WireClient a, BlockPosition support, BlockFace face, int id) throws Exception {
    BlockPosition target = face.adjacent(support);
    a.placeHeldBlock(support, face);
    a.awaitBlock(target, new BlockState(id, 0));
    return target;
  }
  private static BlockPosition foundation(RemoteChunkSnapshot q, int cx, int cz) {
    for (int x = 4; x <= 11; x++)
      for (int z = 4; z <= 11; z++)
        for (int y = 126; y >= 1; y--)
          if (q.blockAt(x, y, z).legacyId() == 3 && water(q.blockAt(x, y + 1, z).legacyId()))
            return new BlockPosition(cx * 16 + x, y, cz * 16 + z);
    throw new IllegalStateException("no deterministic painting foundation");
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
