package worldline.smoke.cookieeatb173;
import static worldline.b173server.B173FixtureSupport.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Eats official cookie 357 through Packet15 air-use and freezes Packet8 heal plus stack consume. */
public final class CookieEatSmoke {
  private CookieEatSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 7)
      throw new IllegalArgumentException(
          "usage: CookieEatSmoke server.jar workspace port seed username chunkX chunkZ");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    int cx = Integer.parseInt(a[5]), cz = Integer.parseInt(a[6]);
    Duration timeout = Duration.ofSeconds(90);
    B173DedicatedServer server =
        new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout), reader = null;
    RemoteInventoryView inv;
    RemoteItemStack cookie = new RemoteItemStack(357, 1, 0);
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0},
          new int[] {357}, new int[] {1}, new int[] {0}, 19);
      actor.connect();
      actor.synchronizePose();
      inv = actor.awaitInventory();
      require(
          inv.occupiedSlots() == 1 && inv.slot(36).item().equals(cookie), "cookie inventory drift");
      require(actor.awaitHealth(19) == 19, "seeded cookie health drift");
      actor.awaitRemoteChunk(cx, cz);
      actor.selectHeldSlot(0);
      require(actor.inventory().slot(36).item().equals(cookie) && actor.health() == 19,
          "pre-eat cookie fixture drift");
      actor.look(0F, 0F);
      actor.useSelectedItemInAir();
      require(actor.awaitHealth(20) == 20, "cookie eat health drift");
      worldline.test.WorldlineSmokeAwait.awaitEntity(
          actor, actor::inventory, v -> v.slot(36).empty(), "cookie consumption", 20);
      actor.close();
      awaitPlayers(server, 0);
      server.save();
      reader = new B173WireClient("127.0.0.1", port, user, timeout);
      reader.connect();
      reader.synchronizePose();
      require(reader.awaitHealth(20) == 20 && reader.awaitInventory().slot(36).empty(),
          "persisted cookie eat drift");
      String evidence =
          "health=19->20,heal=1,cookie=357:1->empty,persisted=true,clients=2,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=cookie357|cause=packet15-dir255-item357|wire=packet8-health19->20+packet103-cookie-empty|oracle=itemfood-cookie-heal1+stack-consume|"
          + evidence;
      System.out.println("WORLDLINE_M262_COOKIE=" + evidence);
      System.out.println("WORLDLINE_M262_TRACE=" + trace);
      System.out.println("WORLDLINE_M262_SIGNATURE=" + sha(trace));
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
