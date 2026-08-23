package worldline.smoke.itemstackmergesetb173;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import worldline.api.RemoteDroppedItem;
import worldline.api.RemoteItemStack;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173ItemStackMerge;
import worldline.b173server.B173PlayerSeed;
import worldline.b173server.B173WireClient;

/** Two identical Packet21 stones in contact remain two live stacks. */
public final class ItemStackMergeSetSmoke {
  private static final RemoteItemStack STONE = new RemoteItemStack(1, 1, 0);
  private static final String EVIDENCE = "drops=2,item=1x1:0,live=2,destroyed=0,"
      + "collected=0,merged=false,contact=30,clients=1,disconnect=clean";
  private static final String TRACE = "v1|server=official-b1.7.3|seed=17320110707|"
      + "fixture=two-stone-1x1-look-down|cause=packet14-status4-twice|"
      + "wire=packet21x2-count1|oracle=item-stack-merge-absent-not-despawn-age|" + EVIDENCE;

  private ItemStackMergeSetSmoke() {}

  public static void main(String[] arguments) throws Exception {
    if (arguments.length != 5)
      throw new IllegalArgumentException("usage: ItemStackMergeSetSmoke server.jar workspace port seed username");
    Path jar = Paths.get(arguments[0]), workspace = Paths.get(arguments[1]);
    int port = Integer.parseInt(arguments[2]);
    long seed = Long.parseLong(arguments[3]);
    String user = arguments[4];
    B173ItemStackMerge.require(
        seed == 17320110707L && user.equals("ItemMerge581") && user.length() <= 16, "item-stack-merge identity drift");
    Duration timeout = Duration.ofSeconds(90);
    B173DedicatedServer server = new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout);
    try {
      server.boot();
      B173PlayerSeed.writeInventory(
          workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1}, new int[] {1, 1}, new int[] {1, 1}, new int[] {0, 0});
      actor.connect();
      actor.synchronizePose();
      B173ItemStackMerge.require(actor.awaitInventory().occupiedSlots() == 2, "item-stack-merge inventory seed drift");
      actor.selectHeldSlot(0);
      actor.look(0F, 90F);
      actor.dropHeldItem();
      RemoteDroppedItem first = actor.awaitDroppedItem(STONE);
      B173ItemStackMerge.require(
          first.item().equals(STONE) && first.item().count() == 1, "first Packet21 stone 1x1 absent");
      worldline.test.WorldlineSmokeAwait.awaitEntity(
          actor, actor::inventory, view -> view.slot(36).empty(), "first dropped slot", 10);
      actor.selectHeldSlot(1);
      actor.dropHeldItem();
      RemoteDroppedItem second = worldline.test.WorldlineSmokeAwait.awaitEntity(actor,
          ()
              -> actor.peekDroppedItem(STONE),
          drop -> drop != null && drop.entityId() != first.entityId(), "second Packet21 stone", 20);
      B173ItemStackMerge.require(
          second.item().equals(STONE) && second.item().count() == 1 && second.entityId() != first.entityId(),
          "second Packet21 stone 1x1 absent");
      worldline.test.WorldlineSmokeAwait.awaitEntity(
          actor, actor::inventory, view -> view.occupiedSlots() == 0, "both dropped slots", 10);
      double dx = second.x() - first.x(), dz = second.z() - first.z();
      B173ItemStackMerge.require(dx * dx + dz * dz <= 1D, "dropped stones were not in contact range");
      worldline.test.WorldlineSmokeAwait.observe(actor, 30);
      B173ItemStackMerge.require(B173ItemStackMerge.live(actor, STONE) == 2
              && !B173ItemStackMerge.destroyed(actor, first.entityId())
              && !B173ItemStackMerge.destroyed(actor, second.entityId())
              && !B173ItemStackMerge.collected(actor, first.entityId())
              && !B173ItemStackMerge.collected(actor, second.entityId()) && first.item().count() == 1
              && second.item().count() == 1 && actor.inventory().occupiedSlots() == 0,
          "item-stack-merge Packet21 absorb drift");
      actor.close();
      B173ItemStackMerge.awaitPlayers(server, 0);
      server.save();
      System.out.println("WORLDLINE_M581_SET=" + EVIDENCE);
      System.out.println("WORLDLINE_M581_TRACE=" + TRACE);
      System.out.println("WORLDLINE_M581_SIGNATURE=" + B173ItemStackMerge.sha(TRACE));
    } finally {
      actor.close();
      server.close();
    }
  }
}
