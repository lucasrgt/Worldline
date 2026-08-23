package worldline.smoke.eggthrowb173;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Throws official egg 344 from a raised stone platform and correlates Packet23 type 62. */
public final class EggThrowSmoke {
  private EggThrowSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 8)
      throw new IllegalArgumentException(
          "usage: EggThrowSmoke server.jar workspace port seed actor observer chunkX chunkZ");
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
    BlockPosition top;
    int column, thrower;
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, actorName, 4.5D, 60D, 4.5D, new int[] {0, 1},
          new int[] {1, 344}, new int[] {32, 1}, new int[] {0, 0});
      B173PlayerSeed.write(workspace, observerName, 4.5D, 80D, 4.5D);
      actor.connect();
      actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() == 2, "egg inventory drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      top = foundation(initial, cx, cz);
      column = 0;
      actor.selectHeldSlot(0);
      while (
          water(initial.blockAt(local(top.x(), cx), top.y() + 1, local(top.z(), cz)).legacyId())) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        require(++column <= 15, "water column exceeded egg fixture");
      }
      for (int lift = 0; lift < 8; lift++) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        column++;
      }
      observer.connect();
      observer.synchronizePose();
      observer.awaitRemoteChunk(cx, cz);
      actor.selectHeldSlot(1);
      actor.look(0F, 0F);
      actor.useSelectedItemInAir();
      RemoteObjectSpawn spawn = actor.awaitObjectSpawn(62), peer = observer.awaitObjectSpawn(62);
      thrower = spawn.throwerId();
      require(spawn.equals(peer) && spawn.type() == 62
              && spawn.entityId() != actor.state().entityId()
              && spawn.entityId() != observer.state().entityId(),
          "peer egg spawn drift");
      require(thrower == 0 || thrower == actor.state().entityId(),
          "egg thrower drift: thrower=" + thrower + ",actor=" + actor.state().entityId());
      require(Math.abs(spawn.x() - (top.x() + 0.5D)) <= 2D
              && Math.abs(spawn.z() - (top.z() + 0.5D)) <= 2D,
          "egg packet pose escaped platform pose=" + spawn.x() + ":" + spawn.y() + ":" + spawn.z()
              + ",support=" + top);
      actor.close();
      observer.close();
      awaitPlayers(server, 0);
      server.save();
      String throwerToken = thrower == 0 ? "thrower0" : "thrower=actor";
      String evidence = "column=" + column + ",support=" + top.x() + ":" + top.y() + ":" + top.z()
          + ":1:0,egg=type62+shared-id+" + throwerToken + "+fixed" + spawn.fixedX() + ":"
          + spawn.fixedY() + ":" + spawn.fixedZ() + ",clients=2,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=raised-stone|cause=packet15-dir255-egg344|wire=packet23-type62+"
          + throwerToken + "|oracle=two-peer-identical-egg-object|" + evidence;
      System.out.println("WORLDLINE_M169_EGG=" + evidence);
      System.out.println("WORLDLINE_M169_TRACE=" + trace);
      System.out.println("WORLDLINE_M169_SIGNATURE=" + sha(trace));
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
    throw new IllegalStateException("no deterministic egg foundation");
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
