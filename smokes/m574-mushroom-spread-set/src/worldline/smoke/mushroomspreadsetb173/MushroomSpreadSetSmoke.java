package worldline.smoke.mushroomspreadsetb173;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import worldline.api.BlockFace;
import worldline.api.BlockPosition;
import worldline.api.RemoteChunkSnapshot;
import worldline.api.RemoteWorldView;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173PlayerSeed;
import worldline.b173server.B173WireClient;

/** Places striped brown 39 and red 40, then waits Packet53 air-to-mushroom. */
public final class MushroomSpreadSetSmoke {
  private MushroomSpreadSetSmoke() {}

  public static void main(String[] a) throws Exception {
    if (a.length != 9) {
      throw new IllegalArgumentException("usage: MushroomSpreadSetSmoke server.jar workspace "
          + "port seed username chunkX chunkZ windowTicks spreadWindows");
    }
    Path jar = Paths.get(a[0]);
    Path workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    int cx = Integer.parseInt(a[5]);
    int cz = Integer.parseInt(a[6]);
    int window = Integer.parseInt(a[7]);
    int windows = Integer.parseInt(a[8]);
    MushroomSpreadSetArm.require(seed == 17320110707L && user.equals("MushSprd574") && user.length() <= 16
            && window >= 1 && window <= 1200 && windows >= 1 && windows <= 80,
        "mushroom-spread-set identity drift");
    Duration timeout = Duration.ofMinutes(45);
    B173DedicatedServer server = new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout);
    B173WireClient reader = null;
    BlockPosition top;
    BlockPosition glass;
    BlockPosition glassAir;
    BlockPosition cover;
    int[] column = new int[1];
    int[] used = new int[1];
    BlockPosition[] sources = new BlockPosition[15];
    BlockPosition[] targets = new BlockPosition[9];
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1, 2, 3, 4, 5, 6},
          new int[] {1, 1, 1, 1, 39, 40, 20}, new int[] {64, 64, 64, 64, 32, 32, 8}, new int[] {0, 0, 0, 0, 0, 0, 0});
      actor.connect();
      actor.synchronizePose();
      MushroomSpreadSetArm.require(actor.awaitInventory().occupiedSlots() == 7, "mushroom-spread inventory drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      top = MushroomSpreadSetArm.raise(actor, initial, cx, cz, column, used);
      glass = MushroomSpreadSetArm.glassFloor(actor, top, used);
      MushroomSpreadSetArm.walls(actor, top, used);
      cover = MushroomSpreadSetArm.roof(actor, top, used);
      glassAir = BlockFace.UP.adjacent(glass);
      MushroomSpreadSetArm.plant(actor, top, sources, targets);
      RemoteWorldView placed = worldline.test.WorldlineSmokeAwait.observe(actor, 5);
      MushroomSpreadSetArm.require(MushroomSpreadSetArm.id(placed, sources[0]) == 39
              && MushroomSpreadSetArm.id(placed, glassAir) == 0 && MushroomSpreadSetArm.id(placed, cover) == 1,
          "pad cells missing before random-tick wait");
      MushroomSpreadSetWait.waitSpread(actor, sources, targets, glassAir, window, windows);
      actor.close();
      MushroomSpreadSetArm.awaitPlayers(server, 0);
      server.save();
      reader = new B173WireClient("127.0.0.1", port, user, timeout);
      reader.connect();
      reader.synchronizePose();
      RemoteChunkSnapshot after = reader.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      MushroomSpreadSetWait.persist(after, cx, cz, sources, targets, glass, glassAir, cover);
      String evidence = "column=" + column[0] + ",support=" + MushroomSpreadSetArm.token(top, 1, 0)
          + ",pad=7x7,sources=15,brown=9,"
          + "red=6,targets=9,glass=" + MushroomSpreadSetArm.token(glass, 20, 0)
          + ",roof=" + MushroomSpreadSetArm.token(cover, 1, 0)
          + ",spread=air->39/40,glass-stay=true,persisted=true,clients=2,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=raised-stone+dark-7x7-pad+mushroom39+mushroom40+glass20"
          + "|cause=packet15-item39+item40+random-ticks"
          + "|wire=packet53-air-to-mushroom39/40+glass-air"
          + "|oracle=dark-opaque-spread+glass-stay+fresh-login|" + evidence;
      System.out.println("WORLDLINE_M574_SET=" + evidence);
      System.out.println("WORLDLINE_M574_TRACE=" + trace);
      System.out.println("WORLDLINE_M574_SIGNATURE=" + MushroomSpreadSetArm.sha(trace));
    } finally {
      actor.close();
      if (reader != null)
        reader.close();
      server.close();
    }
  }
}
