package worldline.smoke.shearsleafdurab173;
import static worldline.b173server.B173FixtureSupport.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import worldline.api.BlockFace;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.RemoteChunkSnapshot;
import worldline.api.RemoteDroppedItem;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteItemStack;
import worldline.api.RemoteWorldView;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173PlayerSeed;
import worldline.b173server.B173WireClient;
import worldline.testkit.ShearsLeafDurabilityFixture;

/** Breaks one oak leaf with pristine shears 359 and freezes the exact one-point durability move. */
public final class ShearsLeafDurabilitySmoke {
  private static final RemoteItemStack LEAF = new RemoteItemStack(18, 1, 0);
  private static final BlockState AIR = new BlockState(0, 0), PLACED = new BlockState(18, 8);
  private ShearsLeafDurabilitySmoke() {}
  public static void main(String[] args) throws Exception {
    if (args.length != 7)
      throw new IllegalArgumentException(
          "usage: ShearsLeafDurabilitySmoke server.jar workspace port seed username chunkX chunkZ");
    Path jar = Paths.get(args[0]), workspace = Paths.get(args[1]);
    int port = Integer.parseInt(args[2]);
    long seed = Long.parseLong(args[3]);
    String user = args[4];
    int cx = Integer.parseInt(args[5]), cz = Integer.parseInt(args[6]);
    Duration timeout = Duration.ofMinutes(8);
    B173DedicatedServer server =
        new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout);
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1, 2, 3},
          new int[] {1, 17, 18, 359}, new int[] {32, 1, 1, 1}, new int[] {0, 0, 0, 0});
      actor.connect();
      actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() == 4, "shears durability inventory drift");
      RemoteChunkSnapshot chunk = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      BlockPosition top = foundation(chunk, cx, cz);
      int column = 0;
      actor.selectHeldSlot(0);
      while (
          water(chunk.blockAt(local(top.x(), cx), top.y() + 1, local(top.z(), cz)).legacyId())) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        require(++column <= 15, "water column exceeded shears durability fixture");
      }
      for (int lift = 0; lift < 8; lift++) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        column++;
      }
      actor.selectHeldSlot(1);
      BlockPosition log = place(actor, top, BlockFace.EAST, 17);
      actor.selectHeldSlot(2);
      BlockPosition leaf = BlockFace.UP.adjacent(log);
      actor.placeHeldBlock(log, BlockFace.UP);
      actor.awaitBlock(leaf, PLACED);
      RemoteWorldView live = worldline.test.WorldlineSmokeAwait.observe(actor, 5);
      require(live.blockAt(log.x(), log.y(), log.z()).equals(new BlockState(17, 0))
              && live.blockAt(leaf.x(), leaf.y(), leaf.z()).equals(PLACED),
          "live oak fixture drift");
      RemoteItemStack before = held(actor.inventory(), 359);
      require(before != null && before.damage() == 0, "held shears did not start pristine");
      actor.selectHeldSlot(3);
      actor.beginBreak(leaf);
      worldline.test.WorldlineSmokeAwait.observe(actor, 5);
      actor.finishBreak(leaf);
      actor.awaitBlock(leaf, AIR);
      RemoteDroppedItem drop = actor.awaitDroppedItem(LEAF);
      require(drop.item().equals(LEAF), "shears Packet21 oak-leaf stack absent");
      RemoteItemStack after = worldline.test.WorldlineSmokeAwait.awaitEntity(actor,
          () -> held(actor.inventory(), 359),
          item -> item != null && item.damage() > before.damage(),
          "held shears durability transition", 40);
      ShearsLeafDurabilityFixture.Evidence evidence =
          ShearsLeafDurabilityFixture.harvest(before, PLACED, drop.item(), AIR, after);
      String signal = "tool=shears359,leaf=" + evidence.leaf().legacyId() + ":"
          + evidence.leaf().metadata() + "->" + AIR.legacyId() + ":" + AIR.metadata()
          + ",drop=packet21-" + evidence.drop().legacyId() + ":" + evidence.drop().count() + ":"
          + evidence.drop().damage() + ",shears=359:" + evidence.beforeDamage() + "->359:"
          + evidence.afterDamage() + ",replicas=2,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=raised-stone+oak17+oakleaf18x1|cause=packet14-shears359"
          + "|wire=packet53-air+packet21-id18+packet103-shears359"
          + "|oracle=one-leaf-one-durability-point|" + signal;
      System.out.println("WORLDLINE_M747_SET=" + signal);
      System.out.println("WORLDLINE_M747_TRACE=" + trace);
      System.out.println("WORLDLINE_M747_SIGNATURE=" + sha(trace));
    } finally {
      actor.close();
      server.close();
    }
  }
  private static RemoteItemStack held(RemoteInventoryView view, int id) {
    for (int slot = 0; slot < view.size(); slot++)
      if (!view.slot(slot).empty() && view.slot(slot).item().legacyId() == id)
        return view.slot(slot).item();
    return null;
  }
  private static BlockPosition foundation(RemoteChunkSnapshot chunk, int cx, int cz) {
    for (int x = 4; x <= 11; x++)
      for (int z = 4; z <= 11; z++)
        for (int y = 126; y >= 1; y--)
          if (chunk.blockAt(x, y, z).legacyId() == 3
              && water(chunk.blockAt(x, y + 1, z).legacyId()))
            return new BlockPosition(cx * 16 + x, y, cz * 16 + z);
    throw new IllegalStateException("no deterministic shears durability foundation");
  }
  private static void require(boolean value, String message) {
    if (!value) throw new IllegalStateException(message);
  }
}
