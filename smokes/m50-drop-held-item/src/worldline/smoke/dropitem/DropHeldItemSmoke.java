package worldline.smoke.dropitem;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import worldline.api.DropItemMultiplayerSession;
import worldline.api.RemoteHeldItem;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteItemStack;
import worldline.api.ServerPlayerState;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173PlayerSeed;
import worldline.b173server.B173WireClient;

/** Proves a dropped held item through local and independent peer updates. */
public final class DropHeldItemSmoke {
  private static final String TRACE = "v1|server=official-b1.7.3|clients=2|initial=stone"
      + "|action=drop-current|wire=packet14-status4|local=packet103-empty|peer=packet5-empty"
      + "|persisted=0|disconnect=clean";
  private DropHeldItemSmoke() {
  }

  public static void main(String[] arguments) throws Exception {
    if (arguments.length != 6)
      throw new IllegalArgumentException(
          "usage: DropHeldItemSmoke server.jar workspace port seed actor observer");
    Path jar = Paths.get(arguments[0]), workspace = Paths.get(arguments[1]);
    int port = Integer.parseInt(arguments[2]);
    long seed = Long.parseLong(arguments[3]);
    String actorName = arguments[4], observerName = arguments[5];
    Duration timeout = Duration.ofSeconds(90);
    B173DedicatedServer server =
        new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    DropItemMultiplayerSession actor = client(port, actorName, timeout);
    DropItemMultiplayerSession observer = client(port, observerName, timeout);
    RemoteInventoryView before, after;
    RemoteHeldItem empty;
    ServerPlayerState player;
    try {
      server.boot();
      B173PlayerSeed.writeHolding(workspace, actorName, 4.5D, 60D, 4.5D, 1, 1, 0);
      B173PlayerSeed.write(workspace, observerName, 4.5D, 80D, 4.5D);
      actor.connect();
      actor.synchronizePose();
      before = actor.awaitInventory();
      require(before.occupiedSlots() == 1
              && before.slot(36).item().equals(new RemoteItemStack(1, 1, 0)),
          "held seed drifted");
      observer.connect();
      observer.synchronizePose();
      requirePlayers(server.players(), actorName, observerName);
      observer.awaitPeerHeldItem(new RemoteHeldItem(actorName, 1, 0));
      actor.dropHeldItem();
      after = worldline.test.WorldlineSmokeAwait.awaitEntity(
          actor, actor::inventory, view -> view.occupiedSlots() == 0, "dropped local slot", 10);
      require(after.occupiedSlots() == 0 && after.slot(36).empty(),
          "local dropped slot did not become empty");
      empty = observer.awaitPeerHeldItem(RemoteHeldItem.empty(actorName));
      require(empty.empty(), "peer hand not empty");
      actor.close();
      observer.close();
      awaitPlayers(server, 0);
      server.save();
      player = server.player(actorName);
      require(player.inventoryItems() == 0, "dropped inventory was persisted as occupied");
    } finally {
      actor.close();
      observer.close();
      server.close();
    }
    System.out.println(
        "WORLDLINE_M50_API=drop-held,packet14,local-slot-empty,peer-empty,server-authoritative");
    System.out.println("WORLDLINE_M50_LOCAL=before=" + before.slot(36).item() + ";after=empty");
    System.out.println("WORLDLINE_M50_PEER=" + empty);
    System.out.println("WORLDLINE_M50_PERSISTED=items=" + player.inventoryItems());
    System.out.println("WORLDLINE_M50_TRACE=" + TRACE);
    System.out.println("WORLDLINE_M50_SIGNATURE=" + sha256(TRACE));
  }

  private static DropItemMultiplayerSession client(int port, String name, Duration timeout) {
    return new B173WireClient("127.0.0.1", port, name, timeout);
  }
  private static void requirePlayers(List<String> players, String first, String second) {
    Set<String> expected = new HashSet<>();
    expected.add(first);
    expected.add(second);
    require(players.size() == 2 && new HashSet<>(players).equals(expected),
        "two-player presence drifted");
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
