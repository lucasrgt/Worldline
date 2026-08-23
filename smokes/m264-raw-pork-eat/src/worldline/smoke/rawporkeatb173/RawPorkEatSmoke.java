package worldline.smoke.rawporkeatb173;
import static worldline.b173server.B173FixtureSupport.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Eats official raw porkchop 319 through Packet15 air-use and freezes Packet8 heal plus stack consume. */
public final class RawPorkEatSmoke {
  private RawPorkEatSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 7)
      throw new IllegalArgumentException(
          "usage: RawPorkEatSmoke server.jar workspace port seed username chunkX chunkZ");
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
    RemoteItemStack pork = new RemoteItemStack(319, 1, 0);
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0},
          new int[] {319}, new int[] {1}, new int[] {0}, 17);
      actor.connect();
      actor.synchronizePose();
      inv = actor.awaitInventory();
      require(
          inv.occupiedSlots() == 1 && inv.slot(36).item().equals(pork), "raw pork inventory drift");
      require(actor.awaitHealth(17) == 17, "seeded raw pork health drift");
      actor.awaitRemoteChunk(cx, cz);
      actor.selectHeldSlot(0);
      require(actor.inventory().slot(36).item().equals(pork) && actor.health() == 17,
          "pre-eat raw pork fixture drift");
      actor.look(0F, 0F);
      actor.useSelectedItemInAir();
      require(actor.awaitHealth(20) == 20, "raw pork eat health drift");
      worldline.test.WorldlineSmokeAwait.awaitEntity(
          actor, actor::inventory, v -> v.slot(36).empty(), "raw pork consumption", 20);
      actor.close();
      awaitPlayers(server, 0);
      server.save();
      reader = new B173WireClient("127.0.0.1", port, user, timeout);
      reader.connect();
      reader.synchronizePose();
      require(reader.awaitHealth(20) == 20 && reader.awaitInventory().slot(36).empty(),
          "persisted raw pork eat drift");
      String evidence =
          "health=17->20,heal=3,pork=319:1->0,persisted=true,clients=2,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=rawpork319|cause=packet15-dir255-item319|wire=packet8-health17->20+packet103-rawpork-empty|oracle=itemfood-rawpork-heal3+stack-consume|"
          + evidence;
      System.out.println("WORLDLINE_M264_PORK=" + evidence);
      System.out.println("WORLDLINE_M264_TRACE=" + trace);
      System.out.println("WORLDLINE_M264_SIGNATURE=" + sha(trace));
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
