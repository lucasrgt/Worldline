package worldline.smoke.grassspreadsetb173;

import java.nio.file.*;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Places an 8-grass ring around lit dirt 3 plus covered dirt 3, then waits official random-tick Packet53 3->2. */
public final class GrassSpreadSetSmoke {
  private GrassSpreadSetSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 9)
      throw new IllegalArgumentException(
          "usage: GrassSpreadSetSmoke server.jar workspace port seed username chunkX chunkZ windowTicks spreadWindows");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    int cx = Integer.parseInt(a[5]), cz = Integer.parseInt(a[6]), window = Integer.parseInt(a[7]),
        windows = Integer.parseInt(a[8]);
    GrassSpreadSetArm.require(seed == 17320110707L && user.equals("GrassSprd566")
            && user.length() <= 16 && window >= 1 && window <= 1200 && windows >= 1
            && windows <= 40,
        "grass-spread-set identity drift");
    Duration timeout = Duration.ofMinutes(20);
    B173DedicatedServer server =
        new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout), reader = null;
    BlockPosition top, east2, west2, north2, south2, center, litE, litW, litN, covered, cover;
    int[] column = new int[1];
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1, 2},
          new int[] {1, 2, 3}, new int[] {64, 16, 16}, new int[] {0, 0, 0});
      actor.connect();
      actor.synchronizePose();
      GrassSpreadSetArm.require(
          actor.awaitInventory().occupiedSlots() == 3, "grass-spread inventory drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      top = GrassSpreadSetArm.raise(actor, initial, cx, cz, column);
      actor.selectHeldSlot(0);
      BlockPosition[] ring = GrassSpreadSetArm.ring(actor, top);
      east2 = GrassSpreadSetArm.place(actor, ring[0], BlockFace.EAST, 1);
      west2 = GrassSpreadSetArm.place(actor, ring[1], BlockFace.WEST, 1);
      north2 = GrassSpreadSetArm.place(actor, ring[2], BlockFace.NORTH, 1);
      south2 = GrassSpreadSetArm.place(actor, ring[3], BlockFace.SOUTH, 1);
      actor.selectHeldSlot(1);
      BlockPosition[] grass = GrassSpreadSetArm.plant(actor, ring, 2);
      actor.selectHeldSlot(2);
      center = GrassSpreadSetArm.place(actor, top, BlockFace.UP, 3);
      litE = GrassSpreadSetArm.place(actor, east2, BlockFace.UP, 3);
      litW = GrassSpreadSetArm.place(actor, west2, BlockFace.UP, 3);
      litN = GrassSpreadSetArm.place(actor, north2, BlockFace.UP, 3);
      covered = GrassSpreadSetArm.place(actor, south2, BlockFace.UP, 3);
      actor.selectHeldSlot(0);
      cover = GrassSpreadSetArm.place(actor, covered, BlockFace.UP, 1);
      BlockPosition[] lit = new BlockPosition[] {center, litE, litW, litN};
      RemoteWorldView placed = worldline.test.WorldlineSmokeAwait.observe(actor, 5);
      GrassSpreadSetArm.require(GrassSpreadSetArm.id(placed, center) == 3
              && GrassSpreadSetArm.id(placed, covered) == 3
              && GrassSpreadSetArm.id(placed, cover) == 1
              && GrassSpreadSetArm.id(placed, grass[0]) == 2,
          "pad cells missing before random-tick wait");
      GrassSpreadSetArm.waitSpread(actor, grass, lit, covered, window, windows);
      actor.close();
      GrassSpreadSetArm.awaitPlayers(server, 0);
      server.save();
      reader = new B173WireClient("127.0.0.1", port, user, timeout);
      reader.connect();
      reader.synchronizePose();
      RemoteChunkSnapshot after = reader.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      GrassSpreadSetArm.persist(after, cx, cz, grass, lit, covered, cover);
      String evidence = "column=" + column[0] + ",support=" + GrassSpreadSetArm.token(top, 1, 0)
          + ",grass-ring=8,source=2:0,lit=" + GrassSpreadSetArm.cells(lit)
          + ",covered=" + GrassSpreadSetArm.token(covered, 3, 0)
          + ",cover=" + GrassSpreadSetArm.token(cover, 1, 0)
          + ",spread=3->2,covered-stay=true,persisted=true,clients=2,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=raised-stone+grass2-ring8+lit-dirt3+covered-dirt3|cause=packet15-item2+item3+random-ticks|wire=packet53-dirt3-to-grass2+covered-dirt3|oracle=lit-dirt-spread+dark-dirt-stay+fresh-login|"
          + evidence;
      System.out.println("WORLDLINE_M566_SET=" + evidence);
      System.out.println("WORLDLINE_M566_TRACE=" + trace);
      System.out.println("WORLDLINE_M566_SIGNATURE=" + GrassSpreadSetArm.sha(trace));
    } finally {
      actor.close();
      if (reader != null)
        reader.close();
      server.close();
    }
  }
}
