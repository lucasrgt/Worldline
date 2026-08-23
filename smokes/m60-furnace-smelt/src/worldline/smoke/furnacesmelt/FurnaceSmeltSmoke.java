package worldline.smoke.furnacesmelt;

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
import worldline.api.FurnaceSession;
import worldline.api.MovementOutcome;
import worldline.api.PlayerPose;
import worldline.api.RemoteContainerWindow;
import worldline.api.RemoteFurnaceLoad;
import worldline.api.RemoteFurnaceSmelt;
import worldline.api.RemoteHeldItem;
import worldline.api.RemoteItemStack;
import worldline.api.RemoteWindowClosure;
import worldline.api.RemoteWindowKind;
import worldline.api.RemoteWorldView;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173WireClient;

/** Proves exact sand and coal loading plus a complete official furnace smelt. */
public final class FurnaceSmeltSmoke {
  private static final String TRACE = "v1|server=official-b1.7.3|clients=2|fixture=furnace61"
      + "|open=packet100-furnace-3+packet104-39|mapping=personal36,37-combined30,31"
      + "|load-actions=1,2,3,4-accepted|input=sand12x1|fuel=coal263x1"
      + "|progress=packet105-cook199-burn1600-total1600-reset0-completion1401"
      + "|slots=packet103-input-empty+fuel-empty+glass20x1|world=furnace62"
      + "|close-proof=personal-action1|player-items=0|disconnect=clean";
  private FurnaceSmeltSmoke() {
  }

  public static void main(String[] arguments) throws Exception {
    if (arguments.length != 6)
      throw new IllegalArgumentException(
          "usage: FurnaceSmeltSmoke server.jar workspace port seed actor observer");
    Path jar = Paths.get(arguments[0]), workspace = Paths.get(arguments[1]);
    int port = Integer.parseInt(arguments[2]);
    long seed = Long.parseLong(arguments[3]);
    String actorName = arguments[4], observerName = arguments[5];
    Duration timeout = Duration.ofSeconds(90);
    B173DedicatedServer server = null;
    FurnaceSession actor = null, observer = null;
    RemoteFurnaceLoad load;
    RemoteFurnaceSmelt smelt;
    RemoteWindowClosure closure;
    BlockPosition target;
    try {
      server = new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
      server.boot();
      server.operator(actorName);
      actor = client(port, actorName, timeout);
      actor.connect();
      actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() == 0, "actor inventory was not empty");
      actor.look(0F, 90F);
      PlayerPose pose = acquireFurnace(actor, actorName);
      RemoteWorldView baseline =
          actor.awaitRemoteChunk((int) Math.floor(pose.x()) >> 4, (int) Math.floor(pose.z()) >> 4);
      BlockPosition support = new BlockPosition(
          (int) Math.floor(pose.x()), (int) Math.floor(pose.y()) - 1, (int) Math.floor(pose.z()));
      target = BlockFace.UP.adjacent(support);
      require(baseline.blockAt(support.x(), support.y(), support.z()).legacyId() != 0
              && replaceable(baseline.blockAt(target.x(), target.y(), target.z())),
          "furnace anchor drifted");
      require(!actor.moveAndObserve(0D, 3D, 0D, 3).corrected(), "furnace clearance failed");
      observer = client(port, observerName, timeout);
      observer.connect();
      observer.synchronizePose();
      observer.moveAndObserve(5D, 5D, 0D, 3);
      observer.moveAndObserve(5D, 5D, 0D, 3);
      requirePlayers(server.players(), actorName, observerName);
      observer.awaitRemoteChunk(Math.floorDiv(target.x(), 16), Math.floorDiv(target.z(), 16));
      actor.placeHeldBlock(support, BlockFace.UP);
      BlockState idle = worldline.test.WorldlineSmokeAwait.observe(actor, 5).blockAt(
          target.x(), target.y(), target.z());
      require(idle.legacyId() == 61
              && worldline.test.WorldlineSmokeAwait.observe(observer, 5)
                  .blockAt(target.x(), target.y(), target.z())
                  .equals(idle),
          "placed furnace diverged");
      acquireIngredients(actor, actorName);
      RemoteItemStack sand = new RemoteItemStack(12, 1, 0), coal = new RemoteItemStack(263, 1, 0);
      require(actor.inventory().slot(36).item().equals(sand)
              && actor.inventory().slot(37).item().equals(coal),
          "furnace ingredient seed drifted");
      actor.selectHeldSlot(2);
      RemoteContainerWindow opened = actor.openFurnace(target, BlockFace.UP);
      require(opened.descriptor().kind() == RemoteWindowKind.FURNACE
              && opened.inventory().size() == 39 && opened.inventory().slot(30).item().equals(sand)
              && opened.inventory().slot(31).item().equals(coal)
              && opened.inventory().slot(0).empty() && opened.inventory().slot(1).empty()
              && opened.inventory().slot(2).empty(),
          "furnace open mapping drifted");
      actor.selectHeldSlot(0);
      observer.awaitPeerHeldItem(new RemoteHeldItem(actorName, 12, 0));
      load = actor.loadFurnace(36, 37);
      require(load.inputTakeAction() == 1 && load.inputStoreAction() == 2
              && load.fuelTakeAction() == 3 && load.fuelStoreAction() == 4
              && load.input().equals(sand) && load.fuel().equals(coal)
              && actor.inventory().slot(36).empty() && actor.inventory().slot(37).empty(),
          "accepted furnace load drifted");
      worldline.test.WorldlineSmokeAwait.observe(actor, 5);
      observer.awaitPeerHeldItem(RemoteHeldItem.empty(actorName));
      smelt = actor.awaitFurnaceSmelt();
      require(smelt.output().equals(new RemoteItemStack(20, 1, 0)) && smelt.maximumCook() == 199
              && smelt.maximumBurn() == 1600 && smelt.totalBurn() == 1600
              && smelt.completionBurn() == 1401 && smelt.window().inventory().slot(0).empty()
              && smelt.window().inventory().slot(1).empty(),
          "completed furnace evidence drifted");
      require(worldline.test.WorldlineSmokeAwait.observe(actor, 3)
                      .blockAt(target.x(), target.y(), target.z())
                      .legacyId()
                  == 62
              && worldline.test.WorldlineSmokeAwait.observe(observer, 3)
                      .blockAt(target.x(), target.y(), target.z())
                      .legacyId()
                  == 62,
          "active furnace world state drifted");
      closure = actor.closeWindow();
      require(closure.closedWindow().equals(smelt.window()) && closure.proofAction() == 1,
          "furnace close proof drifted");
      actor.close();
      observer.close();
      awaitPlayers(server, 0);
      server.save();
      require(server.player(actorName).inventoryItems() == 0,
          "furnace player inventory persisted items");
    } finally {
      if (actor != null)
        actor.close();
      if (observer != null)
        observer.close();
      if (server != null)
        server.close();
    }
    System.out.println("WORLDLINE_M60_API=furnace-open,accepted-load,packet105-smelt");
    System.out.println("WORLDLINE_M60_SMELT=actions=" + load.inputTakeAction() + ","
        + load.inputStoreAction() + "," + load.fuelTakeAction() + "," + load.fuelStoreAction()
        + ";cook=" + smelt.maximumCook() + ";burn=" + smelt.maximumBurn() + ";completion="
        + smelt.completionBurn() + ";output=" + smelt.output() + ";close=" + closure.proofAction());
    System.out.println("WORLDLINE_M60_TRACE=" + TRACE);
    System.out.println("WORLDLINE_M60_SIGNATURE=" + sha256(TRACE));
  }

  private static FurnaceSession client(int port, String name, Duration timeout) {
    return new B173WireClient("127.0.0.1", port, name, timeout);
  }
  private static PlayerPose acquireFurnace(FurnaceSession client, String username) {
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
  private static void acquireIngredients(FurnaceSession client, String username) {
    client.sendChat("/give " + username + " 12 1");
    client.sendChat("/give " + username + " 263 1");
    worldline.test.WorldlineSmokeAwait.awaitEntity(client, () -> {
      client.moveAndObserve(0D, -1D, 0D, 2);
      return client.inventory();
    }, inventory -> inventory.occupiedSlots() >= 2, "furnace ingredients", 12);
    worldline.test.WorldlineSmokeAwait.observe(client, 10);
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
