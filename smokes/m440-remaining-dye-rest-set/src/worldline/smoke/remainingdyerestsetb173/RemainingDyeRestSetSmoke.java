package worldline.smoke.remainingdyerestsetb173;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Mixes remaining light-gray and magenta dyes in the personal 2x2 grid. */
public final class RemainingDyeRestSetSmoke {
  private RemainingDyeRestSetSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 7)
      throw new IllegalArgumentException(
          "usage: RemainingDyeRestSetSmoke server.jar workspace port seed username chunkX chunkZ");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    int cx = Integer.parseInt(a[5]), cz = Integer.parseInt(a[6]);
    Duration timeout = Duration.ofSeconds(90);
    require(user.length() <= 16, "username exceeds 16");
    B173RemainingDyeRest.verify();
    B173DedicatedServer server =
        new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout), reader = null;
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 72D, 4.5D,
          new int[] {0, 1, 2, 3, 4, 5, 6}, new int[] {351, 351, 351, 351, 351, 351, 351},
          new int[] {1, 1, 1, 1, 1, 1, 1}, new int[] {0, 15, 15, 8, 15, 5, 9});
      actor.connect();
      actor.synchronizePose();
      RemoteInventoryView inv = actor.awaitInventory();
      require(inv.occupiedSlots() == 7 && inv.slot(36).item().equals(new RemoteItemStack(351, 1, 0))
              && inv.slot(37).item().equals(new RemoteItemStack(351, 1, 15))
              && inv.slot(38).item().equals(new RemoteItemStack(351, 1, 15))
              && inv.slot(39).item().equals(new RemoteItemStack(351, 1, 8))
              && inv.slot(40).item().equals(new RemoteItemStack(351, 1, 15))
              && inv.slot(41).item().equals(new RemoteItemStack(351, 1, 5))
              && inv.slot(42).item().equals(new RemoteItemStack(351, 1, 9)),
          "ink bonemeal gray purple and pink inventory drift");
      actor.awaitRemoteChunk(cx, cz);
      B173RemainingDyeRest.apply(actor);
      requireStored(actor.inventory());
      actor.close();
      awaitPlayers(server, 0);
      server.save();
      reader = new B173WireClient("127.0.0.1", port, user, timeout);
      reader.connect();
      reader.synchronizePose();
      requireStored(reader.awaitInventory());
      String evidence =
          "inputs=351:0+351:15+351:15+351:8+351:15+351:5+351:9,results=351x3:7+351x2:7+351x2:13,grid=2x2,actions=20,persisted=true,clients=2,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=ink351:0+bonemeal351:15+gray351:8+purple351:5+pink351:9|cause=packet102-window0-2x2-shapeless|wire=packet106-accepted|oracle=three-remaining-dye-rest-damages+fresh-login|"
          + evidence;
      System.out.println("WORLDLINE_M440_CRAFT=" + evidence);
      System.out.println("WORLDLINE_M440_TRACE=" + trace);
      System.out.println("WORLDLINE_M440_SIGNATURE=" + sha(trace));
    } finally {
      actor.close();
      if (reader != null)
        reader.close();
      server.close();
    }
  }
  private static void requireStored(RemoteInventoryView view) {
    require(view.occupiedSlots() == 3 && view.slot(36).item().equals(new RemoteItemStack(351, 3, 7))
            && view.slot(37).empty() && view.slot(38).empty()
            && view.slot(39).item().equals(new RemoteItemStack(351, 2, 7)) && view.slot(40).empty()
            && view.slot(41).item().equals(new RemoteItemStack(351, 2, 13))
            && view.slot(42).empty(),
        "persisted remaining-dye-rest crafts drifted");
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
