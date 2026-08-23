package worldline.smoke.grassdiecoversetb173;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import worldline.api.BlockFace;
import worldline.api.BlockPosition;
import worldline.api.RemoteChunkSnapshot;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173PlayerSeed;
import worldline.b173server.B173WireClient;

/** Places grass then opaque cover and waits official random-tick Packet53 2->3. */
public final class GrassDieCoverSetSmoke {
  private GrassDieCoverSetSmoke() {}

  public static void main(String[] a) throws Exception {
    if (a.length != 9)
      throw new IllegalArgumentException("usage: GrassDieCoverSetSmoke server.jar workspace port seed username "
          + "chunkX chunkZ windowTicks dieWindows");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    int cx = Integer.parseInt(a[5]), cz = Integer.parseInt(a[6]);
    int window = Integer.parseInt(a[7]), windows = Integer.parseInt(a[8]);
    GrassDieCoverSetArm.require(seed == 17320110707L && user.equals("GrassDie575") && user.length() <= 16 && window >= 1
            && window <= 1200 && windows >= 1 && windows <= 40,
        "grass-die-cover-set identity drift");
    Duration timeout = Duration.ofMinutes(20);
    B173DedicatedServer server = new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout);
    B173WireClient reader = null;
    BlockPosition top, east2, west2, north2, south2, center, litE, litW, litN, covered, cover;
    int[] column = new int[1];
    try {
      server.boot();
      B173PlayerSeed.writeInventory(
          workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1}, new int[] {1, 2}, new int[] {64, 16}, new int[] {0, 0});
      actor.connect();
      actor.synchronizePose();
      GrassDieCoverSetArm.require(actor.awaitInventory().occupiedSlots() == 2, "grass-die-cover inventory drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      top = GrassDieCoverSetArm.raise(actor, initial, cx, cz, column);
      actor.selectHeldSlot(0);
      BlockPosition[] ring = GrassDieCoverSetArm.ring(actor, top);
      east2 = GrassDieCoverSetArm.place(actor, ring[0], BlockFace.EAST, 1);
      west2 = GrassDieCoverSetArm.place(actor, ring[1], BlockFace.WEST, 1);
      north2 = GrassDieCoverSetArm.place(actor, ring[2], BlockFace.NORTH, 1);
      south2 = GrassDieCoverSetArm.place(actor, ring[3], BlockFace.SOUTH, 1);
      actor.selectHeldSlot(1);
      BlockPosition[] grass = GrassDieCoverSetArm.plant(actor, ring, 2);
      center = GrassDieCoverSetArm.place(actor, top, BlockFace.UP, 2);
      litE = GrassDieCoverSetArm.place(actor, east2, BlockFace.UP, 2);
      litW = GrassDieCoverSetArm.place(actor, west2, BlockFace.UP, 2);
      litN = GrassDieCoverSetArm.place(actor, north2, BlockFace.UP, 2);
      covered = GrassDieCoverSetArm.place(actor, south2, BlockFace.UP, 2);
      actor.selectHeldSlot(0);
      cover = GrassDieCoverSetArm.place(actor, covered, BlockFace.UP, 1);
      BlockPosition[] exposed = new BlockPosition[] {center, litE, litW, litN};
      worldline.test.WorldlineSmokeAwait.awaitWorld(actor,
          v
          -> GrassDieCoverSetArm.id(v, center) == 2 && GrassDieCoverSetArm.id(v, covered) == 2
              && GrassDieCoverSetArm.id(v, cover) == 1 && GrassDieCoverSetArm.id(v, grass[0]) == 2,
          "pad cells before random-tick wait", 40);
      GrassDieCoverSetArm.waitDie(actor, grass, exposed, covered, window, windows);
      actor.close();
      GrassDieCoverSetArm.awaitPlayers(server, 0);
      server.save();
      reader = new B173WireClient("127.0.0.1", port, user, timeout);
      reader.connect();
      reader.synchronizePose();
      RemoteChunkSnapshot after = reader.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      GrassDieCoverSetArm.persist(after, cx, cz, grass, exposed, covered, cover);
      String evidence = "column=" + column[0] + ",support=" + GrassDieCoverSetArm.token(top, 1, 0)
          + ",grass-ring=8,source=2:0,exposed=" + GrassDieCoverSetArm.cells(exposed) + ",covered="
          + GrassDieCoverSetArm.token(covered, 3, 0) + ",cover=" + GrassDieCoverSetArm.token(cover, 1, 0)
          + ",die=2->3,exposed-stay=true,persisted=true,clients=2,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=raised-stone+grass2-ring8+exposed-grass2+covered-grass2"
          + "|cause=packet15-item2+item1-cover+random-ticks"
          + "|wire=packet53-grass2-to-dirt3+exposed-grass2"
          + "|oracle=covered-grass-die+lit-grass-stay+fresh-login|" + evidence;
      System.out.println("WORLDLINE_M575_SET=" + evidence);
      System.out.println("WORLDLINE_M575_TRACE=" + trace);
      System.out.println("WORLDLINE_M575_SIGNATURE=" + GrassDieCoverSetArm.sha(trace));
    } finally {
      actor.close();
      if (reader != null)
        reader.close();
      server.close();
    }
  }
}
