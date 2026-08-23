package worldline.smoke.minecartspawnb173;
import static worldline.b173server.B173FixtureSupport.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Places official rail 66 then minecart 328 and correlates Packet23 type 10 across two peers. */
public final class MinecartSpawnSmoke {
  private MinecartSpawnSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 8)
      throw new IllegalArgumentException(
          "usage: MinecartSpawnSmoke server.jar workspace port seed actor observer chunkX chunkZ");
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
    BlockPosition top, rail;
    int column;
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, actorName, 4.5D, 60D, 4.5D, new int[] {0, 1, 2},
          new int[] {1, 66, 328}, new int[] {32, 1, 1}, new int[] {0, 0, 0});
      B173PlayerSeed.write(workspace, observerName, 4.5D, 80D, 4.5D);
      actor.connect();
      actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() == 3, "minecart inventory drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      top = foundation(initial, cx, cz);
      column = 0;
      actor.selectHeldSlot(0);
      while (
          water(initial.blockAt(local(top.x(), cx), top.y() + 1, local(top.z(), cz)).legacyId())) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        require(++column <= 15, "water column exceeded minecart fixture");
      }
      for (int lift = 0; lift < 8; lift++) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        column++;
      }
      actor.selectHeldSlot(1);
      rail = place(actor, top, BlockFace.UP, 66);
      observer.connect();
      observer.synchronizePose();
      observer.awaitRemoteChunk(cx, cz);
      actor.selectHeldSlot(2);
      actor.useHeldItemOnBlock(rail, BlockFace.UP);
      RemoteObjectSpawn first = actor.awaitObjectSpawn(10), peer = observer.awaitObjectSpawn(10);
      require(first.equals(peer) && first.entityId() != actor.state().entityId()
              && first.entityId() != observer.state().entityId(),
          "peer minecart identity drift");
      require(first.type() == 10 && first.throwerId() == 0 && first.velocityX() == 0
              && first.velocityY() == 0 && first.velocityZ() == 0
              && first.fixedX() == rail.x() * 32 + 16 && first.fixedY() == rail.y() * 32 + 27
              && first.fixedZ() == rail.z() * 32 + 16,
          "minecart packet bounds drift: type=" + first.type() + ",thrower=" + first.throwerId()
              + ",fixed=" + first.fixedX() + ":" + first.fixedY() + ":" + first.fixedZ()
              + ",rail=" + rail);
      actor.close();
      observer.close();
      awaitPlayers(server, 0);
      server.save();
      String evidence = "column=" + column + ",rail=" + rail.x() + ":" + rail.y() + ":" + rail.z()
          + ":66:0,cart=type10+shared-positive-id+thrower0+fixed" + first.fixedX() + ":"
          + first.fixedY() + ":" + first.fixedZ() + ",clients=2,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=raised-rail66|cause=packet15-minecart328|wire=packet23-type10+thrower0|oracle=two-peer-identical-minecart-object|"
          + evidence;
      System.out.println("WORLDLINE_M155_CART=" + evidence);
      System.out.println("WORLDLINE_M155_TRACE=" + trace);
      System.out.println("WORLDLINE_M155_SIGNATURE=" + sha(trace));
    } finally {
      actor.close();
      observer.close();
      server.close();
    }
  }
  private static BlockPosition foundation(RemoteChunkSnapshot q, int cx, int cz) {
    for (int x = 4; x <= 11; x++)
      for (int z = 4; z <= 11; z++)
        for (int y = 126; y >= 1; y--)
          if (q.blockAt(x, y, z).legacyId() == 3 && water(q.blockAt(x, y + 1, z).legacyId()))
            return new BlockPosition(cx * 16 + x, y, cz * 16 + z);
    throw new IllegalStateException("no deterministic minecart foundation");
  }
  private static void require(boolean v, String m) {
    if (!v)
      throw new IllegalStateException(m);
  }
}
