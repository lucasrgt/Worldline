package worldline.smoke.goldenappleeatb173;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Eats official golden apple 322 through Packet15 air-use and freezes Packet8 full heal. */
public final class GoldenAppleEatSmoke {
  private GoldenAppleEatSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 7)
      throw new IllegalArgumentException(
          "usage: GoldenAppleEatSmoke server.jar workspace port seed username chunkX chunkZ");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    int cx = Integer.parseInt(a[5]), cz = Integer.parseInt(a[6]);
    Duration timeout = Duration.ofSeconds(90);
    B173DedicatedServer server =
        new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout), reader = null;
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0},
          new int[] {322}, new int[] {1}, new int[] {0}, 10);
      actor.connect();
      actor.synchronizePose();
      RemoteInventoryView before = actor.awaitInventory();
      require(before.occupiedSlots() == 1
              && before.slot(36).item().equals(new RemoteItemStack(322, 1, 0)),
          "golden apple inventory drift");
      require(actor.awaitHealth(10) == 10, "seeded golden apple health drift");
      actor.selectHeldSlot(0);
      actor.useSelectedItemInAir();
      require(
          actor.awaitHealth(20) == 20, "golden apple Packet8 heal drift health=" + actor.health());
      RemoteInventoryView consumed = worldline.test.WorldlineSmokeAwait.awaitEntity(
          actor, actor::inventory, value -> value.slot(36).empty(), "golden apple consumption", 40);
      require(
          consumed.slot(36).empty() && actor.health() == 20, "golden apple stack consume drift");
      actor.close();
      awaitPlayers(server, 0);
      server.save();
      reader = new B173WireClient("127.0.0.1", port, user, timeout);
      reader.connect();
      reader.synchronizePose();
      RemoteInventoryView after = reader.awaitInventory();
      worldline.test.WorldlineSmokeAwait.observe(reader, 5);
      require(reader.health() == 20 && after.slot(36).empty() && after.occupiedSlots() == 0,
          "persisted golden apple eat drift health=" + reader.health());
      String evidence = "chunk=" + cx + ":" + cz
          + ",health=10->20,heal=20,item=322:1:0->empty,persisted=true,clients=2,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=seeded-item322+health10|cause=packet15-direction255-item322|wire=packet8-health10->20+packet103-slot36-empty|oracle=golden-apple-full-heal+stack-consume+fresh-login|"
          + evidence;
      System.out.println("WORLDLINE_M261_GOLDEN=" + evidence);
      System.out.println("WORLDLINE_M261_TRACE=" + trace);
      System.out.println("WORLDLINE_M261_SIGNATURE=" + sha(trace));
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
