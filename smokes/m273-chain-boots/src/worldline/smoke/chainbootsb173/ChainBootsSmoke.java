package worldline.smoke.chainbootsb173;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.RemoteArmorPiece;
import worldline.api.RemoteArmorSlot;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteItemStack;
import worldline.api.ServerPlayerState;
import worldline.b173server.B173ChainBootsClick;
import worldline.b173server.B173ChainBootsPacketFixture;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173PlayerSeed;
import worldline.b173server.B173WireClient;

/** Equips official chain boots 305 into window slot 8 and proves peer Packet5 slot 1. */
public final class ChainBootsSmoke {
  private static final RemoteItemStack BOOTS = B173ChainBootsClick.BOOTS;
  private static final String TRACE =
      "v1|server=official-b1.7.3|seed=17320110707|fixture=chain-boots305"
      + "|window0=8|actions=1,2-accepted|cursor=empty-after-pair|packet5=1:305|damage=0"
      + "|distinct=leather301|restart=window0+packet5-bootstrap|persisted=1|clients=4|disconnect=clean";
  private ChainBootsSmoke() {
  }

  public static void main(String[] arguments) throws Exception {
    if (arguments.length != 6)
      throw new IllegalArgumentException(
          "usage: ChainBootsSmoke server.jar workspace port seed actor observer");
    Path jar = Paths.get(arguments[0]), workspace = Paths.get(arguments[1]);
    int port = Integer.parseInt(arguments[2]);
    long seed = Long.parseLong(arguments[3]);
    require(seed == 17320110707L, "chain boots seed drifted");
    String actorName = arguments[4], observerName = arguments[5];
    Duration timeout = Duration.ofSeconds(90);
    B173ChainBootsPacketFixture.verify();
    require(BOOTS.legacyId() == 305 && BOOTS.legacyId() != 301, "chain boots identity drifted");
    B173DedicatedServer server =
        new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, actorName, timeout);
    B173WireClient observer = new B173WireClient("127.0.0.1", port, observerName, timeout);
    B173WireClient restored = null, witness = null;
    ServerPlayerState persisted;
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, actorName, 4.5D, 70D, 4.5D, new int[] {0},
          new int[] {305}, new int[] {1}, new int[] {0});
      B173PlayerSeed.write(workspace, observerName, 4.5D, 70D, 4.5D);
      actor.connect();
      actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() == 1
              && actor.inventory().slot(36).item().equals(BOOTS),
          "chain boots source drifted");
      observer.connect();
      observer.synchronizePose();
      observer.moveAndObserve(0D, 10D, 0D, 3);
      B173ChainBootsClick.apply(actor, 36);
      requireArmor(actor.inventory());
      RemoteArmorPiece peer =
          observer.awaitPeerArmor(new RemoteArmorPiece(actorName, RemoteArmorSlot.BOOTS, 305, 0));
      require(
          peer.slot() == RemoteArmorSlot.BOOTS && peer.legacyId() == 305 && peer.legacyId() != 301,
          "peer Packet5 chain boots drifted");
      actor.close();
      observer.close();
      awaitPlayers(server, 0);
      server.save();
      persisted = server.player(actorName);
      require(persisted.inventoryItems() == 1, "chain boots persistence count drifted");
      restored = new B173WireClient("127.0.0.1", port, actorName, timeout);
      witness = new B173WireClient("127.0.0.1", port, observerName, timeout);
      restored.connect();
      restored.synchronizePose();
      requireArmor(restored.awaitInventory());
      witness.connect();
      witness.synchronizePose();
      witness.awaitPeerArmor(new RemoteArmorPiece(actorName, RemoteArmorSlot.BOOTS, 305, 0));
      restored.close();
      witness.close();
      awaitPlayers(server, 0);
      server.save();
      require(server.player(actorName).inventoryItems() == 1, "restored chain boots count drifted");
      String evidence =
          "actions=1,2;window=8:305;packet5=1:305;distinct=301;items=" + persisted.inventoryItems();
      System.out.println("WORLDLINE_M273_BOOTS=" + evidence);
      System.out.println("WORLDLINE_M273_TRACE=" + TRACE);
      System.out.println("WORLDLINE_M273_SIGNATURE=" + sha256(TRACE));
    } finally {
      actor.close();
      observer.close();
      if (restored != null)
        restored.close();
      if (witness != null)
        witness.close();
      server.close();
    }
  }

  private static void requireArmor(RemoteInventoryView view) {
    require(!view.slot(8).empty() && view.slot(8).item().equals(BOOTS) && view.slot(36).empty()
            && view.slot(8).item().legacyId() != 301,
        "chain boots window drifted");
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
