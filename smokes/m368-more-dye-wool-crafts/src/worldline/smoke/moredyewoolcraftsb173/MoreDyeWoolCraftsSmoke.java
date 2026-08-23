package worldline.smoke.moredyewoolcraftsb173;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Crafts yellow, orange, and pink wool from white wool 35:0 plus dyes M315 did not hash. */
public final class MoreDyeWoolCraftsSmoke {
  private MoreDyeWoolCraftsSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 7)
      throw new IllegalArgumentException(
          "usage: MoreDyeWoolCraftsSmoke server.jar workspace port seed username chunkX chunkZ");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    int cx = Integer.parseInt(a[5]), cz = Integer.parseInt(a[6]);
    Duration timeout = Duration.ofSeconds(90);
    B173MoreDyeWoolCraft.verify();
    B173DedicatedServer server =
        new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout), reader = null;
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 72D, 4.5D, new int[] {0, 1, 2, 3, 4, 5},
          new int[] {35, 35, 35, 351, 351, 351}, new int[] {1, 1, 1, 1, 1, 1},
          new int[] {0, 0, 0, 11, 14, 9});
      actor.connect();
      actor.synchronizePose();
      RemoteInventoryView inv = actor.awaitInventory();
      require(inv.occupiedSlots() == 6 && inv.slot(36).item().equals(new RemoteItemStack(35, 1, 0))
              && inv.slot(39).item().equals(new RemoteItemStack(351, 1, 11))
              && inv.slot(40).item().equals(new RemoteItemStack(351, 1, 14))
              && inv.slot(41).item().equals(new RemoteItemStack(351, 1, 9)),
          "white wool and more-dye inventory drift");
      actor.awaitRemoteChunk(cx, cz);
      B173MoreDyeWoolCraft.apply(actor);
      require(actor.inventory().slot(36).item().equals(new RemoteItemStack(35, 1, 4))
              && actor.inventory().slot(37).item().equals(new RemoteItemStack(35, 1, 1))
              && actor.inventory().slot(38).item().equals(new RemoteItemStack(35, 1, 6))
              && actor.inventory().occupiedSlots() == 3,
          "live more dyed-wool 2x2 results drifted");
      actor.close();
      awaitPlayers(server, 0);
      server.save();
      reader = new B173WireClient("127.0.0.1", port, user, timeout);
      reader.connect();
      reader.synchronizePose();
      RemoteInventoryView after = reader.awaitInventory();
      require(after.occupiedSlots() == 3
              && after.slot(36).item().equals(new RemoteItemStack(35, 1, 4))
              && after.slot(37).item().equals(new RemoteItemStack(35, 1, 1))
              && after.slot(38).item().equals(new RemoteItemStack(35, 1, 6)),
          "persisted more dyed-wool crafts drifted");
      String evidence =
          "wool=35:0,dyes=351:11+351:14+351:9,results=35:4+35:1+35:6,grid=2x2,actions=18,persisted=true,clients=2,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=white-wool35:0+dyes351-more|cause=packet102-window0-2x2-shapeless|wire=packet106-accepted|oracle=three-new-wool-damages+fresh-login|"
          + evidence;
      System.out.println("WORLDLINE_M368_CRAFT=" + evidence);
      System.out.println("WORLDLINE_M368_TRACE=" + trace);
      System.out.println("WORLDLINE_M368_SIGNATURE=" + sha(trace));
    } finally {
      actor.close();
      if (reader != null)
        reader.close();
      server.close();
    }
  }
  private static void awaitPlayers(B173DedicatedServer s, int n) throws Exception {
    long e = System.currentTimeMillis() + 5000;
    while (System.currentTimeMillis() < e) {
      if (s.players().size() == n)
        return;
      Thread.sleep(100);
    }
    throw new IllegalStateException("player count drift");
  }
  private static String sha(String s) throws Exception {
    byte[] b = MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));
    StringBuilder v = new StringBuilder();
    for (byte x : b)
      v.append(String.format("%02x", x & 255));
    return v.toString();
  }
  private static void require(boolean v, String m) {
    if (!v)
      throw new IllegalStateException(m);
  }
}
