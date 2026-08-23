package worldline.smoke.saplingdarkhaltsetb173;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Places lit oak saplings 6 plus one covered sapling 6, then waits official random-tick stage. */
public final class SaplingDarkHaltSetSmoke {
  private SaplingDarkHaltSetSmoke() {}

  public static void main(String[] a) throws Exception {
    if (a.length != 9)
      throw new IllegalArgumentException("usage: SaplingDarkHaltSetSmoke server.jar workspace port seed username "
          + "chunkX chunkZ windowTicks haltWindows");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    int cx = Integer.parseInt(a[5]), cz = Integer.parseInt(a[6]);
    int window = Integer.parseInt(a[7]), windows = Integer.parseInt(a[8]);
    SaplingDarkHaltSetArm.require(seed == 17320110707L && user.equals("SapDark609") && user.length() <= 16
            && window >= 1 && window <= 1200 && windows >= 1 && windows <= 40,
        "sapling-dark-halt-set identity drift");
    Duration timeout = Duration.ofMinutes(20);
    B173DedicatedServer server = new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout), reader = null;
    BlockPosition top, east, west, north, south, east2, west2, north2, south2, pillar, cover;
    int[] column = new int[1];
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1, 2}, new int[] {1, 3, 6},
          new int[] {64, 16, 16}, new int[] {0, 0, 0});
      actor.connect();
      actor.synchronizePose();
      SaplingDarkHaltSetArm.require(actor.awaitInventory().occupiedSlots() == 3, "sapling-dark-halt inventory drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      top = SaplingDarkHaltSetArm.raise(actor, initial, cx, cz, column);
      actor.selectHeldSlot(0);
      east = SaplingDarkHaltSetArm.place(actor, top, BlockFace.EAST, 1);
      west = SaplingDarkHaltSetArm.place(actor, top, BlockFace.WEST, 1);
      north = SaplingDarkHaltSetArm.place(actor, top, BlockFace.NORTH, 1);
      south = SaplingDarkHaltSetArm.place(actor, top, BlockFace.SOUTH, 1);
      east2 = SaplingDarkHaltSetArm.place(actor, east, BlockFace.EAST, 1);
      west2 = SaplingDarkHaltSetArm.place(actor, west, BlockFace.WEST, 1);
      north2 = SaplingDarkHaltSetArm.place(actor, north, BlockFace.NORTH, 1);
      south2 = SaplingDarkHaltSetArm.place(actor, south, BlockFace.SOUTH, 1);
      pillar = SaplingDarkHaltSetArm.place(actor, south2, BlockFace.EAST, 1);
      pillar = SaplingDarkHaltSetArm.place(actor, pillar, BlockFace.UP, 1);
      pillar = SaplingDarkHaltSetArm.place(actor, pillar, BlockFace.UP, 1);
      pillar = SaplingDarkHaltSetArm.place(actor, pillar, BlockFace.UP, 1);
      cover = SaplingDarkHaltSetArm.place(actor, pillar, BlockFace.WEST, 1);
      actor.selectHeldSlot(1);
      BlockPosition[] supports = new BlockPosition[] {top, east, west, north, east2, west2, north2, south2};
      BlockPosition[] dirt = SaplingDarkHaltSetArm.plant(actor, supports, 3);
      actor.selectHeldSlot(2);
      BlockPosition[] saplings = SaplingDarkHaltSetArm.plant(actor, dirt, 6);
      BlockPosition covered = saplings[7];
      BlockPosition[] lit = new BlockPosition[] {
          saplings[0], saplings[1], saplings[2], saplings[3], saplings[4], saplings[5], saplings[6]};
      RemoteWorldView planted = worldline.test.WorldlineSmokeAwait.observe(actor, 1);
      SaplingDarkHaltSetArm.requirePlanted(planted, lit, covered, cover);
      SaplingDarkHaltSetArm.waitStage(actor, lit, covered, window, windows);
      actor.close();
      SaplingDarkHaltSetArm.awaitPlayers(server, 0);
      server.save();
      reader = new B173WireClient("127.0.0.1", port, user, timeout);
      reader.connect();
      reader.synchronizePose();
      RemoteChunkSnapshot after = reader.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      SaplingDarkHaltSetArm.persist(after, cx, cz, dirt, lit, covered, cover);
      String evidence = "column=" + column[0] + ",support=" + SaplingDarkHaltSetArm.token(top, 1, 0)
          + ",sapling=6:0,lit=7,covered=" + SaplingDarkHaltSetArm.token(covered, 6, 0)
          + ",cover=" + SaplingDarkHaltSetArm.token(cover, 1, 0)
          + ",lit-stage>=1,dark-stay=true,persisted=true,clients=2,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed + "|fixture=raised-stone+lit-sapling6+covered-sapling6"
          + "|cause=packet15-item6+random-ticks"
          + "|wire=packet53-sapling6-stage+covered-6:0"
          + "|oracle=lit-sapling-stage+dark-sapling-halt+fresh-login|" + evidence;
      System.out.println("WORLDLINE_M609_SET=" + evidence);
      System.out.println("WORLDLINE_M609_TRACE=" + trace);
      System.out.println("WORLDLINE_M609_SIGNATURE=" + SaplingDarkHaltSetArm.sha(trace));
    } finally {
      actor.close();
      if (reader != null)
        reader.close();
      server.close();
    }
  }
}
