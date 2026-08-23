package worldline.smoke.dyemixcraftsb173;
import static worldline.b173server.B173FixtureSupport.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Mixes orange, purple, and lime dyes in the personal 2x2 grid. */
public final class DyeMixCraftsSmoke {
  private DyeMixCraftsSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 7)
      throw new IllegalArgumentException(
          "usage: DyeMixCraftsSmoke server.jar workspace port seed username chunkX chunkZ");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    int cx = Integer.parseInt(a[5]), cz = Integer.parseInt(a[6]);
    Duration timeout = Duration.ofSeconds(90);
    require(user.length() <= 16, "username exceeds 16");
    B173DyeMixCraft.verify();
    B173DedicatedServer server =
        new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout), reader = null;
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 72D, 4.5D, new int[] {0, 1, 2, 3, 4, 5},
          new int[] {351, 351, 351, 351, 351, 351}, new int[] {1, 1, 1, 1, 1, 1},
          new int[] {1, 11, 1, 4, 2, 15});
      actor.connect();
      actor.synchronizePose();
      RemoteInventoryView inv = actor.awaitInventory();
      require(inv.occupiedSlots() == 6 && inv.slot(36).item().equals(new RemoteItemStack(351, 1, 1))
              && inv.slot(37).item().equals(new RemoteItemStack(351, 1, 11))
              && inv.slot(38).item().equals(new RemoteItemStack(351, 1, 1))
              && inv.slot(39).item().equals(new RemoteItemStack(351, 1, 4))
              && inv.slot(40).item().equals(new RemoteItemStack(351, 1, 2))
              && inv.slot(41).item().equals(new RemoteItemStack(351, 1, 15)),
          "red yellow red lapis green and bonemeal inventory drift");
      actor.awaitRemoteChunk(cx, cz);
      B173DyeMixCraft.apply(actor);
      requireStored(actor.inventory());
      actor.close();
      awaitPlayers(server, 0);
      server.save();
      reader = new B173WireClient("127.0.0.1", port, user, timeout);
      reader.connect();
      reader.synchronizePose();
      requireStored(reader.awaitInventory());
      String evidence =
          "inputs=351:1+351:11+351:1+351:4+351:2+351:15,results=351x2:14+351x2:5+351x2:10,grid=2x2,actions=18,persisted=true,clients=2,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=red351:1+yellow351:11+red351:1+lapis351:4+green351:2+bonemeal351:15|cause=packet102-window0-2x2-shapeless|wire=packet106-accepted|oracle=three-mixed-dye-damages+fresh-login|"
          + evidence;
      System.out.println("WORLDLINE_M348_CRAFT=" + evidence);
      System.out.println("WORLDLINE_M348_TRACE=" + trace);
      System.out.println("WORLDLINE_M348_SIGNATURE=" + sha(trace));
    } finally {
      actor.close();
      if (reader != null)
        reader.close();
      server.close();
    }
  }
  private static void requireStored(RemoteInventoryView view) {
    require(view.occupiedSlots() == 3
            && view.slot(36).item().equals(new RemoteItemStack(351, 2, 14)) && view.slot(37).empty()
            && view.slot(38).item().equals(new RemoteItemStack(351, 2, 5)) && view.slot(39).empty()
            && view.slot(40).item().equals(new RemoteItemStack(351, 2, 10))
            && view.slot(41).empty(),
        "persisted dye-mix crafts drifted");
  }
  private static void require(boolean v, String m) {
    if (!v)
      throw new IllegalStateException(m);
  }
}
