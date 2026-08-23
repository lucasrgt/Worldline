package worldline.smoke.remainingwoolcraftsb173;
import static worldline.b173server.B173FixtureSupport.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Crafts magenta, light blue, and lime wool from white wool 35:0 plus dyes M315 and M368 did not hash. */
public final class RemainingWoolCraftsSmoke {
  private RemainingWoolCraftsSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 7)
      throw new IllegalArgumentException(
          "usage: RemainingWoolCraftsSmoke server.jar workspace port seed username chunkX chunkZ");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    int cx = Integer.parseInt(a[5]), cz = Integer.parseInt(a[6]);
    Duration timeout = Duration.ofSeconds(90);
    require(user.length() <= 16, "username exceeds 16");
    B173RemainingWoolCraft.verify();
    B173DedicatedServer server =
        new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout), reader = null;
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 72D, 4.5D, new int[] {0, 1, 2, 3, 4, 5},
          new int[] {35, 35, 35, 351, 351, 351}, new int[] {1, 1, 1, 1, 1, 1},
          new int[] {0, 0, 0, 13, 12, 10});
      actor.connect();
      actor.synchronizePose();
      RemoteInventoryView inv = actor.awaitInventory();
      require(inv.occupiedSlots() == 6 && inv.slot(36).item().equals(new RemoteItemStack(35, 1, 0))
              && inv.slot(39).item().equals(new RemoteItemStack(351, 1, 13))
              && inv.slot(40).item().equals(new RemoteItemStack(351, 1, 12))
              && inv.slot(41).item().equals(new RemoteItemStack(351, 1, 10)),
          "white wool and remaining-dye inventory drift");
      actor.awaitRemoteChunk(cx, cz);
      B173RemainingWoolCraft.apply(actor);
      require(actor.inventory().slot(36).item().equals(new RemoteItemStack(35, 1, 2))
              && actor.inventory().slot(37).item().equals(new RemoteItemStack(35, 1, 3))
              && actor.inventory().slot(38).item().equals(new RemoteItemStack(35, 1, 5))
              && actor.inventory().occupiedSlots() == 3,
          "live remaining dyed-wool 2x2 results drifted");
      actor.close();
      awaitPlayers(server, 0);
      server.save();
      reader = new B173WireClient("127.0.0.1", port, user, timeout);
      reader.connect();
      reader.synchronizePose();
      RemoteInventoryView after = reader.awaitInventory();
      require(after.occupiedSlots() == 3
              && after.slot(36).item().equals(new RemoteItemStack(35, 1, 2))
              && after.slot(37).item().equals(new RemoteItemStack(35, 1, 3))
              && after.slot(38).item().equals(new RemoteItemStack(35, 1, 5)),
          "persisted remaining dyed-wool crafts drifted");
      String evidence =
          "wool=35:0,dyes=351:13+351:12+351:10,results=35:2+35:3+35:5,grid=2x2,actions=18,persisted=true,clients=2,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=white-wool35:0+dyes351-remaining|cause=packet102-window0-2x2-shapeless|wire=packet106-accepted|oracle=three-remaining-wool-damages+fresh-login|"
          + evidence;
      System.out.println("WORLDLINE_M396_CRAFT=" + evidence);
      System.out.println("WORLDLINE_M396_TRACE=" + trace);
      System.out.println("WORLDLINE_M396_SIGNATURE=" + sha(trace));
    } finally {
      actor.close();
      if (reader != null)
        reader.close();
      server.close();
    }
  }
  private static void require(boolean v, String m) {
    if (!v)
      throw new IllegalStateException(m);
  }
}
