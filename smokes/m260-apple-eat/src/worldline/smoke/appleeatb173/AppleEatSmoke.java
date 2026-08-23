package worldline.smoke.appleeatb173;
import static worldline.b173server.B173FixtureSupport.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Eats official apple 260 through Packet15 air-use and freezes Packet8 heal plus stack consume. */
public final class AppleEatSmoke {
  private AppleEatSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 7)
      throw new IllegalArgumentException(
          "usage: AppleEatSmoke server.jar workspace port seed username chunkX chunkZ");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    int cx = Integer.parseInt(a[5]), cz = Integer.parseInt(a[6]);
    Duration timeout = Duration.ofSeconds(90);
    B173DedicatedServer server =
        new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout), reader = null;
    BlockPosition top;
    int column;
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1},
          new int[] {260, 1}, new int[] {1, 32}, new int[] {0, 0}, 16);
      actor.connect();
      actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() == 2
              && actor.awaitInventory().slot(36).item().equals(new RemoteItemStack(260, 1, 0)),
          "apple inventory drift");
      require(actor.awaitHealth(16) == 16, "seeded apple health drift");
      actor.selectHeldSlot(0);
      actor.useSelectedItemInAir();
      require(actor.awaitHealth(20) == 20, "apple heal drift health=" + actor.health());
      worldline.test.WorldlineSmokeAwait.awaitEntity(actor, actor::inventory,
          v -> v.slot(36).empty() && v.occupiedSlots() == 1, "apple consumption", 20);
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      top = foundation(initial, cx, cz);
      column = 0;
      actor.selectHeldSlot(1);
      while (
          water(initial.blockAt(local(top.x(), cx), top.y() + 1, local(top.z(), cz)).legacyId())) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        require(++column <= 15, "water column exceeded apple fixture");
      }
      for (int lift = 0; lift < 8; lift++) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        column++;
      }
      require(actor.health() == 20, "post-climb apple health drift: " + actor.health());
      actor.close();
      awaitPlayers(server, 0);
      server.save();
      reader = new B173WireClient("127.0.0.1", port, user, timeout);
      reader.connect();
      reader.synchronizePose();
      require(reader.awaitInventory().slot(36).empty()
              && reader.awaitInventory().occupiedSlots() == 1 && reader.awaitHealth(20) == 20,
          "persisted apple eat drift");
      String evidence = "column=" + column + ",support=" + top.x() + ":" + top.y() + ":" + top.z()
          + ":1:0,health=16->20,heal=4,held=260:1:0->empty,persisted=true,clients=2,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=raised-stone+apple260|cause=packet15-direction255-item260|wire=packet8-health16->20+packet103-apple-empty|oracle=apple-heal4-cap20+stack-consume|"
          + evidence;
      System.out.println("WORLDLINE_M260_APPLE=" + evidence);
      System.out.println("WORLDLINE_M260_TRACE=" + trace);
      System.out.println("WORLDLINE_M260_SIGNATURE=" + sha(trace));
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
    throw new IllegalStateException("no deterministic apple foundation");
  }
  private static void require(boolean v, String m) {
    if (!v)
      throw new IllegalStateException(m);
  }
}
