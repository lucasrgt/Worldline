package worldline.smoke.farmlanddrysetb173;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import worldline.api.BlockFace;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.RemoteChunkSnapshot;
import worldline.api.RemoteWorldView;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173PlayerSeed;
import worldline.b173server.B173WireClient;

/** Hoes isolated dirt into dry farmland 60:0 under a rain roof, then waits 60->3. */
public final class FarmlandDrySetSmoke {
  private FarmlandDrySetSmoke() {}

  public static void main(String[] a) throws Exception {
    if (a.length != 9)
      throw new IllegalArgumentException("usage: FarmlandDrySetSmoke server.jar workspace port seed username "
          + "chunkX chunkZ windowTicks dryWindows");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    int cx = Integer.parseInt(a[5]), cz = Integer.parseInt(a[6]);
    int window = Integer.parseInt(a[7]), windows = Integer.parseInt(a[8]);
    FarmlandDrySetArm.require(seed == 17320110707L && user.equals("FarmDry608") && user.length() <= 16 && window >= 1
            && window <= 1200 && windows >= 1 && windows <= 40,
        "farmland-dry-set identity drift");
    Duration timeout = Duration.ofMinutes(20);
    B173DedicatedServer server = new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout);
    B173WireClient reader = null;
    BlockPosition top, south, farm, cover;
    int[] column = new int[1];
    BlockState dry = new BlockState(60, 0), dirt = new BlockState(3, 0);
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1, 2}, new int[] {1, 3, 290},
          new int[] {64, 1, 1}, new int[] {0, 0, 0});
      actor.connect();
      actor.synchronizePose();
      FarmlandDrySetArm.require(actor.awaitInventory().occupiedSlots() == 3, "farmland-dry inventory drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      top = FarmlandDrySetArm.raise(actor, initial, cx, cz, column);
      actor.selectHeldSlot(0);
      south = FarmlandDrySetArm.place(actor, top, BlockFace.SOUTH, 1);
      cover = FarmlandDrySetArm.roof(actor, south);
      actor.selectHeldSlot(1);
      farm = FarmlandDrySetArm.place(actor, south, BlockFace.UP, 3);
      actor.selectHeldSlot(2);
      FarmlandDrySetArm.till(actor, farm);
      RemoteWorldView placed = worldline.test.WorldlineSmokeAwait.observe(actor, 5);
      FarmlandDrySetArm.require(
          placed.blockAt(farm.x(), farm.y(), farm.z()).equals(dry) && FarmlandDrySetArm.id(placed, cover) == 1,
          "live farmland 60:0 or rain roof drift");
      FarmlandDrySetArm.waitDry(actor, farm, cover, window, windows);
      actor.close();
      FarmlandDrySetArm.awaitPlayers(server, 0);
      server.save();
      reader = new B173WireClient("127.0.0.1", port, user, timeout);
      reader.connect();
      reader.synchronizePose();
      RemoteChunkSnapshot after = reader.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      FarmlandDrySetArm.persist(after, cx, cz, top, farm, cover, dirt);
      String evidence = "column=" + column[0] + ",support=" + FarmlandDrySetArm.token(top, 1, 0)
          + ",cell=" + FarmlandDrySetArm.token(farm, 3, 0) + ",cover=" + FarmlandDrySetArm.token(cover, 1, 0)
          + ",hoe=290,farmland=60:0,dry=60->3,persisted=true,clients=2,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=raised-dry-farmland60+rain-roof|cause=random-ticks"
          + "|wire=packet53-farmland60-to-dirt3"
          + "|oracle=live-dry-60->3+fresh-login-dirt3:0|" + evidence;
      System.out.println("WORLDLINE_M608_DRY=" + evidence);
      System.out.println("WORLDLINE_M608_TRACE=" + trace);
      System.out.println("WORLDLINE_M608_SIGNATURE=" + FarmlandDrySetArm.sha(trace));
    } finally {
      actor.close();
      if (reader != null)
        reader.close();
      server.close();
    }
  }
}
