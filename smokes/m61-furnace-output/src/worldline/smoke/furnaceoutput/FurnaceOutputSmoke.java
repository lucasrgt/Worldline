package worldline.smoke.furnaceoutput;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import worldline.api.BlockFace;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.FurnaceOutputSession;
import worldline.api.MovementOutcome;
import worldline.api.PlayerPose;
import worldline.api.RemoteContainerWindow;
import worldline.api.RemoteFurnaceExtraction;
import worldline.api.RemoteFurnaceSmelt;
import worldline.api.RemoteHeldItem;
import worldline.api.RemoteItemStack;
import worldline.api.RemoteWindowClosure;
import worldline.api.RemoteWorldView;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173WireClient;

/** Proves furnace glass extraction, crafted-stat side effect, and restart persistence. */
public final class FurnaceOutputSmoke {
  private static final String TRACE = "v1|server=official-b1.7.3|servers=2|clients=3"
      + "|fixture=furnace61+sand12+coal263|load-actions=1,2,3,4"
      + "|smelt=glass20x1+cook199+burn1600+completion1401"
      + "|output-actions=5,6-accepted|stat=packet200-16842772x1"
      + "|cursor=empty-glass-empty|output2=glass-empty|personal36=empty-glass"
      + "|peer=empty-glass|close-proof=personal-action1|restart=clean-new-server"
      + "|reopen=personal36-glass+furnace-owned-empty|player-items=1|disconnect=clean";
  private FurnaceOutputSmoke() {
  }

  public static void main(String[] arguments) throws Exception {
    if (arguments.length != 6)
      throw new IllegalArgumentException(
          "usage: FurnaceOutputSmoke server.jar workspace port seed actor observer");
    Path jar = Paths.get(arguments[0]), workspace = Paths.get(arguments[1]);
    int port = Integer.parseInt(arguments[2]);
    long seed = Long.parseLong(arguments[3]);
    String actorName = arguments[4], observerName = arguments[5];
    Duration timeout = Duration.ofSeconds(90);
    B173DedicatedServer first = null, second = null;
    FurnaceOutputSession actor = null, observer = null, reopened = null;
    RemoteFurnaceExtraction extraction;
    RemoteWindowClosure firstClose, secondClose;
    BlockPosition target;
    try {
      first = server(jar, workspace, port, seed, timeout);
      first.boot();
      first.operator(actorName);
      actor = client(port, actorName, timeout);
      actor.connect();
      actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() == 0, "actor inventory was not empty");
      actor.look(0F, 90F);
      PlayerPose pose = acquireFurnace(actor, actorName);
      pose = acquireIngredients(actor, actorName);
      RemoteWorldView baseline =
          actor.awaitRemoteChunk((int) Math.floor(pose.x()) >> 4, (int) Math.floor(pose.z()) >> 4);
      BlockPosition support = placement(baseline, pose);
      target = BlockFace.UP.adjacent(support);
      require(baseline.blockAt(support.x(), support.y(), support.z()).legacyId() != 0
              && replaceable(baseline.blockAt(target.x(), target.y(), target.z())),
          "furnace output anchor drifted");
      observer = client(port, observerName, timeout);
      observer.connect();
      observer.synchronizePose();
      observer.moveAndObserve(5D, 5D, 0D, 3);
      observer.moveAndObserve(5D, 5D, 0D, 3);
      requirePlayers(first.players(), actorName, observerName);
      actor.placeHeldBlock(support, BlockFace.UP);
      BlockState idle = worldline.test.WorldlineSmokeAwait.observe(actor, 5).blockAt(
          target.x(), target.y(), target.z());
      require(idle.legacyId() == 61, "placed output furnace diverged");
      RemoteItemStack sand = new RemoteItemStack(12, 1, 0), coal = new RemoteItemStack(263, 1, 0);
      int sandSlot = find(actor.inventory(), sand), coalSlot = find(actor.inventory(), coal);
      require(actor.inventory().slot(36).empty() && sandSlot >= 37 && coalSlot >= 37,
          "output ingredient seed drifted: " + actor.inventory().slots());
      actor.selectHeldSlot(0);
      RemoteContainerWindow opened = actor.openFurnace(target, BlockFace.UP);
      require(opened.inventory().slot(sandSlot - 6).item().equals(sand)
              && opened.inventory().slot(coalSlot - 6).item().equals(coal),
          "output furnace mapping drifted");
      actor.selectHeldSlot(sandSlot - 36);
      observer.awaitPeerHeldItem(new RemoteHeldItem(actorName, 12, 0));
      actor.loadFurnace(sandSlot, coalSlot);
      worldline.test.WorldlineSmokeAwait.observe(actor, 5);
      observer.awaitPeerHeldItem(RemoteHeldItem.empty(actorName));
      RemoteFurnaceSmelt smelt = actor.awaitFurnaceSmelt();
      require(smelt.output().equals(new RemoteItemStack(20, 1, 0)), "output smelt drifted");
      actor.selectHeldSlot(0);
      extraction = actor.takeFurnaceOutput(36);
      require(extraction.takeAction() == 5 && extraction.storeAction() == 6
              && extraction.craftedCount() == 1
              && extraction.before().equals(smelt.window().inventory())
              && extraction.after().slot(2).empty()
              && extraction.after().slot(30).item().equals(extraction.stack())
              && actor.inventory().slot(36).item().equals(extraction.stack()),
          "furnace extraction drifted");
      worldline.test.WorldlineSmokeAwait.observe(actor, 5);
      observer.awaitPeerHeldItem(new RemoteHeldItem(actorName, 20, 0));
      firstClose = actor.closeWindow();
      require(firstClose.closedWindow().inventory().equals(extraction.after())
              && firstClose.proofAction() == 1,
          "extraction close proof drifted");
      actor.close();
      observer.close();
      awaitPlayers(first, 0);
      first.save();
      require(first.player(actorName).inventoryItems() == 1,
          "extracted player inventory did not persist");
      first.close();
      first = null;
      second = server(jar, workspace, port, seed, timeout);
      second.boot();
      second.operator(actorName);
      reopened = client(port, actorName, timeout);
      reopened.connect();
      reopened.synchronizePose();
      require(reopened.awaitInventory().occupiedSlots() == 1
              && reopened.inventory().slot(36).item().equals(extraction.stack()),
          "restarted glass inventory drifted");
      reopened.selectHeldSlot(1);
      RemoteContainerWindow persisted = reopened.openFurnace(target, BlockFace.UP);
      require(persisted.inventory().slot(0).empty() && persisted.inventory().slot(1).empty()
              && persisted.inventory().slot(2).empty()
              && persisted.inventory().slot(30).item().equals(extraction.stack()),
          "restarted furnace output removal drifted");
      secondClose = reopened.closeWindow();
      require(secondClose.proofAction() == 1, "restarted furnace close drifted");
      reopened.close();
      awaitPlayers(second, 0);
      second.save();
      require(
          second.player(actorName).inventoryItems() == 1, "restarted extracted inventory drifted");
    } finally {
      if (actor != null)
        actor.close();
      if (observer != null)
        observer.close();
      if (reopened != null)
        reopened.close();
      if (first != null)
        first.close();
      if (second != null)
        second.close();
    }
    System.out.println("WORLDLINE_M61_API=furnace-output,packet200-stat,restart-persistence");
    System.out.println("WORLDLINE_M61_OUTPUT=actions=" + extraction.takeAction() + ","
        + extraction.storeAction() + ";crafted=" + extraction.craftedCount()
        + ";stack=" + extraction.stack() + ";close=" + firstClose.proofAction()
        + ";close2=" + secondClose.proofAction());
    System.out.println("WORLDLINE_M61_TRACE=" + TRACE);
    System.out.println("WORLDLINE_M61_SIGNATURE=" + sha256(TRACE));
  }

  private static B173DedicatedServer server(
      Path jar, Path workspace, int port, long seed, Duration timeout) {
    return new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
  }
  private static FurnaceOutputSession client(int port, String name, Duration timeout) {
    return new B173WireClient("127.0.0.1", port, name, timeout);
  }
  private static PlayerPose acquireFurnace(FurnaceOutputSession client, String username) {
    for (int step = 0; step < 10; step++)
      client.moveAndObserve(0D, 5D, 0D, 3);
    client.sendChat("/give " + username + " 61 1");
    worldline.test.WorldlineSmokeAwait.awaitEntity(client, () -> {
      client.moveAndObserve(0D, -1D, 0D, 1);
      return client.inventory();
    }, inventory -> inventory.occupiedSlots() >= 1, "furnace grant", 100);
    worldline.test.WorldlineSmokeAwait.observe(client, 10);
    MovementOutcome settled = null;
    for (int step = 0; step < 100; step++) {
      settled = client.moveAndObserve(0D, -1D, 0D, 2);
      if (settled.corrected())
        break;
    }
    require(settled != null && settled.corrected(), "ground settlement correction absent");
    return settled.resulting();
  }
  private static PlayerPose acquireIngredients(FurnaceOutputSession client, String username) {
    for (int step = 0; step < 10; step++)
      client.moveAndObserve(0D, 5D, 0D, 3);
    client.sendChat("/give " + username + " 12 1");
    client.sendChat("/give " + username + " 263 1");
    worldline.test.WorldlineSmokeAwait.awaitEntity(client, () -> {
      client.moveAndObserve(0D, -1D, 0D, 1);
      return client.inventory();
    }, inventory -> inventory.occupiedSlots() >= 3, "furnace ingredients", 100);
    worldline.test.WorldlineSmokeAwait.observe(client, 10);
    MovementOutcome settled = null;
    for (int step = 0; step < 100; step++) {
      settled = client.moveAndObserve(0D, -1D, 0D, 2);
      if (settled.corrected())
        break;
    }
    require(settled != null && settled.corrected(), "ingredient ground settlement absent");
    return settled.resulting();
  }
  private static BlockPosition placement(RemoteWorldView view, PlayerPose pose) {
    int x = (int) Math.floor(pose.x()), y = (int) Math.floor(pose.y()),
        z = (int) Math.floor(pose.z());
    for (int radius = 2; radius <= 5; radius++)
      for (int dx = -radius; dx <= radius; dx++)
        for (int dz = -radius; dz <= radius; dz++)
          if (Math.max(Math.abs(dx), Math.abs(dz)) == radius)
            for (int dy = 3; dy >= -5; dy--) {
              BlockPosition support = new BlockPosition(x + dx, y + dy, z + dz);
              BlockPosition target = BlockFace.UP.adjacent(support);
              try {
                if (support.y() >= 0 && target.y() < 128
                    && view.blockAt(support.x(), support.y(), support.z()).legacyId() != 0
                    && replaceable(view.blockAt(target.x(), target.y(), target.z())))
                  return support;
              } catch (IllegalArgumentException absent) {
              }
            }
    throw new IllegalStateException("nearby furnace placement absent");
  }
  private static int find(worldline.api.RemoteInventoryView view, RemoteItemStack expected) {
    for (int slot = 9; slot <= 44; slot++)
      if (!view.slot(slot).empty() && view.slot(slot).item().equals(expected))
        return slot;
    return -1;
  }
  private static boolean replaceable(BlockState state) {
    int id = state.legacyId();
    return id == 0 || id == 8 || id == 9 || id == 78;
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
