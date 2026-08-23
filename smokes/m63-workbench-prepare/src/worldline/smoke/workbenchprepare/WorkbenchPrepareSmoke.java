package worldline.smoke.workbenchprepare;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.BlockFace;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.MovementOutcome;
import worldline.api.PlayerPose;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteItemStack;
import worldline.api.RemoteWorkbenchPreparation;
import worldline.api.RemoteWorldView;
import worldline.api.WorkbenchPreparationSession;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173WireClient;
import worldline.b173server.B173WorkbenchPacketFixture;

/** Proves byte-exact clicks with an ACK-correlated local three-wide model. */
public final class WorkbenchPrepareSmoke {
  private static final String TRACE = "v1|server=official-b1.7.3|servers=1|clients=1"
      + "|fixture=workbench58+planks5x3|window=Crafting-declared9-total46"
      + "|wire-fixture=packet102-slots1,2,3-button1-actions2,3,4-shiftfalse-null"
      + "|actions=1-left-take+2,3,4-right-place-accepted|grid-model=empty-1-12-123"
      + "|cursor-model=empty-3-2-1-empty|intermediate-model=pressure72x1"
      + "|prepared-model=slabs44x3:2|personal36=planks-empty"
      + "|close-nonempty=rejected-local|disconnect=clean";
  private WorkbenchPrepareSmoke() {
  }

  public static void main(String[] arguments) throws Exception {
    if (arguments.length != 5)
      throw new IllegalArgumentException(
          "usage: WorkbenchPrepareSmoke server.jar workspace port seed actor");
    Path jar = Paths.get(arguments[0]), workspace = Paths.get(arguments[1]);
    int port = Integer.parseInt(arguments[2]);
    long seed = Long.parseLong(arguments[3]);
    String actorName = arguments[4];
    Duration timeout = Duration.ofSeconds(90);
    B173DedicatedServer server =
        new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    WorkbenchPreparationSession actor = new B173WireClient("127.0.0.1", port, actorName, timeout);
    RemoteWorkbenchPreparation result;
    try {
      B173WorkbenchPacketFixture.verify();
      server.boot();
      server.operator(actorName);
      actor.connect();
      actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() == 0, "actor inventory was not empty");
      actor.look(0F, 90F);
      PlayerPose pose = acquire(actor, actorName, 58, 1);
      RemoteWorldView baseline =
          actor.awaitRemoteChunk((int) Math.floor(pose.x()) >> 4, (int) Math.floor(pose.z()) >> 4);
      BlockPosition support = placement(baseline, pose);
      BlockPosition target = BlockFace.UP.adjacent(support);
      int workbench = find(actor.inventory(), new RemoteItemStack(58, 1, 0));
      require(workbench >= 36, "workbench seed drifted");
      actor.selectHeldSlot(workbench - 36);
      actor.placeHeldBlock(support, BlockFace.UP);
      require(actor.awaitBlock(target, new BlockState(58, 0))
                  .blockAt(target.x(), target.y(), target.z())
                  .equals(new BlockState(58, 0)),
          "placed workbench drifted");
      worldline.test.WorldlineSmokeAwait.observe(actor, 5);
      acquire(actor, actorName, 5, 3);
      RemoteItemStack planks = new RemoteItemStack(5, 3, 0);
      require(actor.inventory().occupiedSlots() == 1
              && actor.inventory().slot(36).item().equals(planks)
              && actor.inventory().slot(37).empty(),
          "plank seed drifted");
      actor.selectHeldSlot(1);
      require(actor.openWorkbench(target, BlockFace.UP).inventory().slot(37).item().equals(planks),
          "workbench plank mapping drifted");
      result = actor.prepareWorkbenchSlabs(36);
      RemoteItemStack one = new RemoteItemStack(5, 1, 0);
      require(result.takeAction() == 1 && result.firstAction() == 2 && result.secondAction() == 3
              && result.thirdAction() == 4
              && result.twoWide().slot(0).item().equals(new RemoteItemStack(72, 1, 0))
              && result.prepared().slot(0).item().equals(new RemoteItemStack(44, 3, 2))
              && result.prepared().slot(1).item().equals(one)
              && result.prepared().slot(2).item().equals(one)
              && result.prepared().slot(3).item().equals(one)
              && result.oneWide().slot(1).item().equals(one)
              && result.cursorAfterTake().count() == 3 && result.cursorAfterFirst().count() == 2
              && result.cursorAfterSecond().count() == 1 && result.cursorEmptyAfterThird()
              && actor.inventory().equals(result.personalAfter()),
          "workbench preparation drifted");
      boolean closeRejected = false;
      try {
        actor.closeWindow();
      } catch (IllegalStateException expected) {
        closeRejected = true;
      }
      require(closeRejected, "nonempty workbench close was not rejected");
      actor.close();
      awaitPlayers(server, 0);
    } finally {
      actor.close();
      server.close();
    }
    System.out.println("WORLDLINE_M63_API=workbench-three-wide,right-place,modeled-result");
    System.out.println("WORLDLINE_M63_PREPARE=actions=" + result.takeAction() + ","
        + result.firstAction() + "," + result.secondAction() + "," + result.thirdAction() + ";two="
        + result.intermediateResult() + ";three=" + result.modeledResult() + ";close=rejected");
    System.out.println("WORLDLINE_M63_TRACE=" + TRACE);
    System.out.println("WORLDLINE_M63_SIGNATURE=" + sha256(TRACE));
  }

  private static PlayerPose acquire(
      WorkbenchPreparationSession client, String username, int id, int count) {
    int occupied = client.inventory().occupiedSlots() + 1;
    for (int step = 0; step < 10; step++)
      client.moveAndObserve(0D, 5D, 0D, 3);
    client.sendChat("/give " + username + " " + id + " " + count);
    worldline.test.WorldlineSmokeAwait.awaitEntity(client, () -> {
      client.moveAndObserve(0D, -1D, 0D, 1);
      return client.inventory();
    }, inventory -> inventory.occupiedSlots() >= occupied, "workbench ingredient grant", 100);
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
    throw new IllegalStateException("nearby workbench placement absent");
  }
  private static int find(RemoteInventoryView view, RemoteItemStack expected) {
    for (int slot = 9; slot <= 44; slot++)
      if (!view.slot(slot).empty() && view.slot(slot).item().equals(expected))
        return slot;
    return -1;
  }
  private static boolean replaceable(BlockState state) {
    int id = state.legacyId();
    return id == 0 || id == 8 || id == 9 || id == 78;
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
