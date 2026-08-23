package worldline.smoke.cakeeatb173;
import static worldline.b173server.B173FixtureSupport.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Places official cake item 354 and eats one BlockCake slice through Packet15. */
public final class CakeEatSmoke {
  private CakeEatSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 7)
      throw new IllegalArgumentException(
          "usage: CakeEatSmoke server.jar workspace port seed username chunkX chunkZ");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    int cx = Integer.parseInt(a[5]), cz = Integer.parseInt(a[6]);
    Duration timeout = Duration.ofSeconds(90);
    B173DedicatedServer server =
        new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout), reader = null;
    BlockPosition top, cake;
    int column;
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1},
          new int[] {1, 354}, new int[] {32, 1}, new int[] {0, 0}, 17);
      actor.connect();
      actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() == 2, "cake inventory drift");
      require(actor.awaitHealth(17) == 17, "seeded cake health drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      top = foundation(initial, cx, cz);
      column = 0;
      actor.selectHeldSlot(0);
      while (
          water(initial.blockAt(local(top.x(), cx), top.y() + 1, local(top.z(), cz)).legacyId())) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        require(++column <= 15, "water column exceeded cake fixture");
      }
      for (int lift = 0; lift < 8; lift++) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        column++;
      }
      cake = BlockFace.UP.adjacent(top);
      actor.selectHeldSlot(1);
      actor.useHeldItemOnBlock(top, BlockFace.UP);
      require(actor.awaitBlock(cake, new BlockState(92, 0))
                  .blockAt(cake.x(), cake.y(), cake.z())
                  .equals(new BlockState(92, 0)),
          "placed BlockCake 92:0 absent");
      actor.selectHeldSlot(2);
      actor.activateBlock(cake, BlockFace.UP);
      require(actor.awaitBlock(cake, new BlockState(92, 1))
                  .blockAt(cake.x(), cake.y(), cake.z())
                  .equals(new BlockState(92, 1)),
          "BlockCake bite 92:1 absent");
      require(actor.awaitHealth(20) == 20, "cake bite health drift");
      actor.close();
      awaitPlayers(server, 0);
      server.save();
      reader = new B173WireClient("127.0.0.1", port, user, timeout);
      reader.connect();
      reader.synchronizePose();
      RemoteChunkSnapshot after = reader.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      require(after.blockAt(local(cake.x(), cx), cake.y(), local(cake.z(), cz))
                  .equals(new BlockState(92, 1))
              && reader.awaitHealth(20) == 20,
          "persisted cake bite drift");
      String evidence = "column=" + column + ",support=" + top.x() + ":" + top.y() + ":" + top.z()
          + ":1:0,cake=" + cake.x() + ":" + cake.y() + ":" + cake.z()
          + ":92:0->1,health=17->20,heal=3,bites=6,persisted=true,clients=2,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=raised-stone+blockcake92|cause=packet15-item354-place+empty-hand-packet15-bite|wire=packet53-cake92:0->1+packet8-health17->20|oracle=blockcake-one-bite-metadata+health|"
          + evidence;
      System.out.println("WORLDLINE_M160_CAKE=" + evidence);
      System.out.println("WORLDLINE_M160_TRACE=" + trace);
      System.out.println("WORLDLINE_M160_SIGNATURE=" + sha(trace));
    } finally {
      actor.close();
      if (reader != null)
        reader.close();
      server.close();
    }
  }
  private static BlockPosition foundation(RemoteChunkSnapshot q, int cx, int cz) {
    for (int x = 4; x <= 11; x++)
      for (int z = 4; z <= 11; z++)
        for (int y = 126; y >= 1; y--)
          if (q.blockAt(x, y, z).legacyId() == 3 && water(q.blockAt(x, y + 1, z).legacyId()))
            return new BlockPosition(cx * 16 + x, y, cz * 16 + z);
    throw new IllegalStateException("no deterministic cake foundation");
  }
  private static void require(boolean v, String m) {
    if (!v)
      throw new IllegalStateException(m);
  }
}
