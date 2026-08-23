package worldline.smoke.dyefamilycraftsb173;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Crafts bone meal, rose red, dandelion yellow, and gray dye in the personal 2x2 grid. */
public final class DyeFamilyCraftsSmoke {
  private DyeFamilyCraftsSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 7)
      throw new IllegalArgumentException(
          "usage: DyeFamilyCraftsSmoke server.jar workspace port seed username chunkX chunkZ");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    int cx = Integer.parseInt(a[5]), cz = Integer.parseInt(a[6]);
    Duration timeout = Duration.ofSeconds(90);
    require(user.length() <= 16, "username exceeds 16");
    B173DyeFamilyCraft.verify();
    B173DedicatedServer server =
        new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout), reader = null;
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 72D, 4.5D, new int[] {0, 1, 2, 3, 4},
          new int[] {352, 38, 37, 351, 351}, new int[] {1, 1, 1, 1, 1}, new int[] {0, 0, 0, 0, 15});
      actor.connect();
      actor.synchronizePose();
      RemoteInventoryView inv = actor.awaitInventory();
      require(inv.occupiedSlots() == 5 && inv.slot(36).item().equals(new RemoteItemStack(352, 1, 0))
              && inv.slot(37).item().equals(new RemoteItemStack(38, 1, 0))
              && inv.slot(38).item().equals(new RemoteItemStack(37, 1, 0))
              && inv.slot(39).item().equals(new RemoteItemStack(351, 1, 0))
              && inv.slot(40).item().equals(new RemoteItemStack(351, 1, 15)),
          "bone rose dandelion ink and bonemeal inventory drift");
      actor.awaitRemoteChunk(cx, cz);
      B173DyeFamilyCraft.apply(actor);
      requireStored(actor.inventory());
      actor.close();
      awaitPlayers(server, 0);
      server.save();
      reader = new B173WireClient("127.0.0.1", port, user, timeout);
      reader.connect();
      reader.synchronizePose();
      requireStored(reader.awaitInventory());
      String evidence =
          "inputs=352:0+38:0+37:0+351:0+351:15,results=351x3:15+351x2:1+351x2:11+351x2:8,grid=2x2,actions=18,persisted=true,clients=2,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=bone352+rose38+dandelion37+ink351:0+bonemeal351:15|cause=packet102-window0-2x2-shapeless|wire=packet106-accepted|oracle=four-dye-damages+fresh-login|"
          + evidence;
      System.out.println("WORLDLINE_M328_CRAFT=" + evidence);
      System.out.println("WORLDLINE_M328_TRACE=" + trace);
      System.out.println("WORLDLINE_M328_SIGNATURE=" + sha(trace));
    } finally {
      actor.close();
      if (reader != null)
        reader.close();
      server.close();
    }
  }
  private static void requireStored(RemoteInventoryView view) {
    require(view.occupiedSlots() == 4
            && view.slot(36).item().equals(new RemoteItemStack(351, 3, 15))
            && view.slot(37).item().equals(new RemoteItemStack(351, 2, 1))
            && view.slot(38).item().equals(new RemoteItemStack(351, 2, 11))
            && view.slot(39).item().equals(new RemoteItemStack(351, 2, 8)) && view.slot(40).empty(),
        "persisted dye-family crafts drifted");
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
