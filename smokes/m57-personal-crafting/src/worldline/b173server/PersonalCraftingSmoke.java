package worldline.b173server;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import worldline.api.RemoteHeldItem;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteItemStack;
import worldline.api.RemotePersonalCraft;
import worldline.api.RemotePersonalTransaction;
import worldline.api.RemoteRejectedTransaction;
import worldline.api.ServerPlayerState;

/** Proves the bounded personal log-to-planks 2x2 recipe against the official server. */
public final class PersonalCraftingSmoke {
  private static final String TRACE = "v1|server=official-b1.7.3|clients=2|window=0|grid=2x2"
      + "|recipe=log17-to-planks5x4|slots=36,1,0,36|actions=1,2,3,4,5-rejected,6"
      + "|acks=packet106-accepted|matrix=empty-log-empty|result=empty-planks4-empty"
      + "|cursor=empty-log-empty-planks4-empty-planks4-empty|audit=packet104+packet103"
      + "|peer=log-planks-empty-planks|persisted=1|disconnect=clean";
  private PersonalCraftingSmoke() {
  }

  public static void main(String[] arguments) throws Exception {
    if (arguments.length != 6)
      throw new IllegalArgumentException(
          "usage: PersonalCraftingSmoke server.jar workspace port seed actor observer");
    Path jar = Paths.get(arguments[0]), workspace = Paths.get(arguments[1]);
    int port = Integer.parseInt(arguments[2]);
    long seed = Long.parseLong(arguments[3]);
    String actorName = arguments[4], observerName = arguments[5];
    Duration timeout = Duration.ofSeconds(90);
    B173DedicatedServer server =
        new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = client(port, actorName, timeout),
                   observer = client(port, observerName, timeout);
    RemotePersonalCraft craft;
    RemoteRejectedTransaction audit;
    RemotePersonalTransaction restored;
    ServerPlayerState player;
    try {
      server.boot();
      server.save();
      server.operator(actorName);
      B173LevelDatWeather.Weather world =
          B173LevelDatWeather.read(workspace.resolve("world/level.dat"));
      double x = world.spawnX() + 0.5D, y = world.spawnY() + 20D, z = world.spawnZ() + 0.5D;
      B173PlayerSeed.writeHolding(workspace, actorName, x, y, z, 17, 1, 0);
      B173PlayerSeed.write(workspace, observerName, x + 3D, y, z);
      actor.connect();
      actor.synchronizePose();
      actor.look(0F, 90F);
      RemoteItemStack log = new RemoteItemStack(17, 1, 0), planks = new RemoteItemStack(5, 4, 0);
      RemoteInventoryView initial = actor.awaitInventory();
      require(initial.occupiedSlots() == 1 && initial.slot(36).item().equals(log)
              && emptyCraft(initial),
          "craft seed drifted");
      observer.connect();
      observer.synchronizePose();
      requirePlayers(server.players(), actorName, observerName);
      observer.awaitPeerHeldItem(new RemoteHeldItem(actorName, 17, 0));
      craft = actor.craftPersonal2x2(36);
      require(craft.takeAction() == 1 && craft.placeAction() == 2 && craft.resultAction() == 3
              && craft.storeAction() == 4 && craft.ingredient().equals(log)
              && craft.output().equals(planks),
          "craft transaction identity drifted");
      require(craft.matrix().slot(36).empty() && craft.matrix().slot(1).item().equals(log)
              && craft.matrix().slot(0).item().equals(planks),
          "craft matrix transition drifted");
      require(craft.crafted().slot(36).empty() && emptyCraft(craft.crafted())
              && craft.after().slot(36).item().equals(planks) && emptyCraft(craft.after())
              && actor.inventory().equals(craft.after()),
          "craft result transition drifted");
      require(initial.slot(36).item().equals(log) && craft.matrix().slot(0).item().equals(planks),
          "craft snapshots mutated");
      worldline.test.WorldlineSmokeAwait.observe(actor, 5);
      observer.awaitPeerHeldItem(new RemoteHeldItem(actorName, 5, 0));
      audit = actor.rejectedTakeProbe(36);
      require(audit.actionId() == 5 && audit.authoritative().slot(36).empty()
              && emptyCraft(audit.authoritative()) && audit.cursorAfter().equals(planks),
          "authoritative crafted-state audit drifted");
      worldline.test.WorldlineSmokeAwait.observe(actor, 5);
      observer.awaitPeerHeldItem(RemoteHeldItem.empty(actorName));
      restored = actor.clickPersonalSlot(36);
      require(restored.actionId() == 6 && restored.after().slot(36).item().equals(planks)
              && restored.cursorAfterEmpty(),
          "post-audit restore drifted");
      worldline.test.WorldlineSmokeAwait.observe(actor, 5);
      observer.awaitPeerHeldItem(new RemoteHeldItem(actorName, 5, 0));
      actor.close();
      observer.close();
      awaitPlayers(server, 0);
      server.save();
      player = server.player(actorName);
      require(player.inventoryItems() == 1, "crafted inventory persistence drifted");
    } finally {
      actor.close();
      observer.close();
      server.close();
    }
    System.out.println("WORLDLINE_M57_API=personal-2x2-craft,log-to-planks,four-accepted-actions");
    System.out.println("WORLDLINE_M57_CRAFT=actions=" + craft.takeAction() + ","
        + craft.placeAction() + "," + craft.resultAction() + "," + craft.storeAction() + ","
        + audit.actionId() + "," + restored.actionId() + ";ingredient=" + craft.ingredient()
        + ";output=" + craft.output()
        + ";audit=grid-empty,cursor-planks;items=" + player.inventoryItems());
    System.out.println("WORLDLINE_M57_TRACE=" + TRACE);
    System.out.println("WORLDLINE_M57_SIGNATURE=" + sha256(TRACE));
  }

  private static B173WireClient client(int port, String name, Duration timeout) {
    return new B173WireClient("127.0.0.1", port, name, timeout);
  }
  private static boolean emptyCraft(RemoteInventoryView view) {
    for (int slot = 0; slot < 5; slot++)
      if (!view.slot(slot).empty())
        return false;
    return true;
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
