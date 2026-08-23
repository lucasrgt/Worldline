package worldline.smoke.ironhelmetb173;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.RemoteArmorPiece;
import worldline.api.RemoteArmorSlot;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteItemStack;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173IronHelmetClick;
import worldline.b173server.B173IronHelmetPacketFixture;
import worldline.b173server.B173PlayerSeed;
import worldline.b173server.B173WireClient;

/** Equips iron helmet 306 through window 0 and correlates peer Packet5 slot 4. */
public final class IronHelmetSmoke {
  private static final RemoteItemStack HELMET = B173IronHelmetClick.HELMET;
  private static final String SIGNAL =
      "window=5:306,packet5=4:306,item!=298,actions=1,2,persisted=true,clients=4,disconnect=clean";
  private IronHelmetSmoke() {
  }

  public static void main(String[] arguments) throws Exception {
    if (arguments.length != 6)
      throw new IllegalArgumentException(
          "usage: IronHelmetSmoke server.jar workspace port seed actor observer");
    Path jar = Paths.get(arguments[0]), workspace = Paths.get(arguments[1]);
    int port = Integer.parseInt(arguments[2]);
    long seed = Long.parseLong(arguments[3]);
    String actorName = arguments[4], observerName = arguments[5];
    Duration timeout = Duration.ofSeconds(90);
    B173IronHelmetPacketFixture.verify();
    require(HELMET.legacyId() == 306 && HELMET.legacyId() != 298 && HELMET.count() == 1
            && HELMET.damage() == 0,
        "iron helmet identity drifted");
    B173DedicatedServer server =
        new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = client(port, actorName, timeout),
                   observer = client(port, observerName, timeout);
    B173WireClient restored = null, witness = null;
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, actorName, 4.5D, 72D, 4.5D, new int[] {0},
          new int[] {306}, new int[] {1}, new int[] {0});
      B173PlayerSeed.write(workspace, observerName, 4.5D, 72D, 4.5D);
      actor.connect();
      actor.synchronizePose();
      require(
          actor.awaitInventory().occupiedSlots() == 1, "actor inventory was not the iron helmet");
      int source = find(actor.inventory(), HELMET);
      require(source == 36, "iron helmet source drifted");
      observer.connect();
      observer.synchronizePose();
      observer.moveAndObserve(0D, 0D, 0D, 3);
      B173IronHelmetClick.apply(actor, source);
      requireArmor(actor.inventory());
      RemoteArmorPiece peer =
          observer.awaitPeerArmor(new RemoteArmorPiece(actorName, RemoteArmorSlot.HELMET, 306, 0));
      require(peer.slot() == RemoteArmorSlot.HELMET && peer.legacyId() == 306
              && peer.legacyId() != 298 && peer.slot().containerSlot() == 5
              && peer.slot().equipmentSlot() == 4 && peer.damage() == 0,
          "peer iron helmet drifted");
      actor.close();
      observer.close();
      awaitPlayers(server, 0);
      server.save();
      require(
          server.player(actorName).inventoryItems() == 1, "iron helmet persistence count drifted");
      restored = client(port, actorName, timeout);
      witness = client(port, observerName, timeout);
      restored.connect();
      restored.synchronizePose();
      requireArmor(restored.awaitInventory());
      witness.connect();
      witness.synchronizePose();
      witness.awaitPeerArmor(new RemoteArmorPiece(actorName, RemoteArmorSlot.HELMET, 306, 0));
      restored.close();
      witness.close();
      awaitPlayers(server, 0);
      server.save();
      require(server.player(actorName).inventoryItems() == 1, "restored iron helmet count drifted");
    } finally {
      actor.close();
      observer.close();
      if (restored != null)
        restored.close();
      if (witness != null)
        witness.close();
      server.close();
    }
    String trace = "v1|server=official-b1.7.3|seed=" + seed
        + "|fixture=iron-helmet306|cause=packet102-window0-slot36-to-5"
        + "|wire=packet5-slot4-item306|oracle=live-equip+peer+fresh-login|" + SIGNAL;
    System.out.println("WORLDLINE_M270_API=iron-helmet,personal-transactions,peer-packet5,restart");
    System.out.println("WORLDLINE_M270_HELM=" + SIGNAL);
    System.out.println("WORLDLINE_M270_TRACE=" + trace);
    System.out.println("WORLDLINE_M270_SIGNATURE=" + sha256(trace));
  }

  private static B173WireClient client(int port, String name, Duration timeout) {
    return new B173WireClient("127.0.0.1", port, name, timeout);
  }
  private static int find(RemoteInventoryView view, RemoteItemStack expected) {
    for (int slot = 9; slot <= 44; slot++)
      if (!view.slot(slot).empty() && view.slot(slot).item().equals(expected))
        return slot;
    return -1;
  }
  private static void requireArmor(RemoteInventoryView view) {
    require(!view.slot(5).empty() && view.slot(5).item().equals(HELMET) && view.slot(36).empty()
            && view.occupiedSlots() == 1,
        "iron helmet window drifted");
  }
  private static void awaitPlayers(B173DedicatedServer server, int count)
      throws InterruptedException {
    long deadline = System.currentTimeMillis() + 5000L;
    while (System.currentTimeMillis() < deadline) {
      if (server.players().size() == count)
        return;
      Thread.sleep(100L);
    }
    throw new IllegalStateException("player count did not become " + count);
  }
  private static String sha256(String value) throws Exception {
    byte[] bytes =
        MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
    StringBuilder result = new StringBuilder();
    for (byte item : bytes)
      result.append(String.format("%02x", item & 255));
    return result.toString();
  }
  private static void require(boolean condition, String message) {
    if (!condition)
      throw new IllegalStateException(message);
  }
}
