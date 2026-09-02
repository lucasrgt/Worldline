package worldline.smoke.axelogbreaksb173;
import static worldline.b173server.B173FixtureSupport.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;
import worldline.testkit.*;
import worldline.testapi.*;

/** Stone axe 275 fully breaks oak 17:0, spruce 17:1, and birch 17:2, each dropping the matching log item. */
public final class AxeLogBreaksSmoke {
  private static final RemoteItemStack OAK = new RemoteItemStack(17, 1, 0),
                                       SPRUCE = new RemoteItemStack(17, 1, 1),
                                       BIRCH = new RemoteItemStack(17, 1, 2),
                                       AXE = new RemoteItemStack(275, 1, 0);
  private static final BlockState AIR = new BlockState(0, 0);
  private AxeLogBreaksSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 7)
      throw new IllegalArgumentException(
          "usage: AxeLogBreaksSmoke server.jar workspace port seed username chunkX chunkZ");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    int cx = Integer.parseInt(a[5]), cz = Integer.parseInt(a[6]);
    Duration timeout = Duration.ofSeconds(90);
    require(user.length() <= 16, "username exceeds 16");
    B173DedicatedServer server =
        new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout);
    BlockPosition top, oak, spruce, birch;
    int column;
    RemoteDroppedItem dropOak, dropSpruce, dropBirch;
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1, 2, 3, 4},
          new int[] {1, 17, 17, 17, 275}, new int[] {32, 1, 1, 1, 1}, new int[] {0, 0, 1, 2, 0});
      actor.connect();
      actor.synchronizePose();
      RemoteInventoryView inventory = actor.awaitInventory();
      require(inventory.occupiedSlots() == 5 && inventory.slot(40).item().equals(AXE)
              && inventory.slot(37).item().equals(OAK) && inventory.slot(38).item().equals(SPRUCE)
              && inventory.slot(39).item().equals(BIRCH),
          "axe-log-breaks inventory drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      top = foundation(initial, cx, cz);
      column = 0;
      actor.selectHeldSlot(0);
      while (
          water(initial.blockAt(local(top.x(), cx), top.y() + 1, local(top.z(), cz)).legacyId())) {
        top = place(actor, top, BlockFace.UP, 1, 0);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        require(++column <= 15, "water column exceeded axe-log-breaks fixture");
      }
      for (int lift = 0; lift < 8; lift++) {
        top = place(actor, top, BlockFace.UP, 1, 0);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        column++;
      }
      actor.selectHeldSlot(1);
      oak = place(actor, top, BlockFace.UP, 17, 0);
      require(
          actor.sustainTicks(5).blockAt(oak.x(), oak.y(), oak.z()).equals(new BlockState(17, 0)),
          "live oak log 17:0 drift");
      actor.selectHeldSlot(4);
      harvest(actor, oak, 20);
      actor.awaitBlock(oak, AIR);
      dropOak = actor.awaitDroppedItem(OAK);
      require(dropOak.item().equals(OAK) && dropOak.item().damage() == 0,
          "stone-axe Packet21 oak 17:0 drop absent");
      actor.selectHeldSlot(2);
      spruce = place(actor, top, BlockFace.UP, 17, 1);
      require(actor.sustainTicks(5)
                  .blockAt(spruce.x(), spruce.y(), spruce.z())
                  .equals(new BlockState(17, 1)),
          "live spruce log 17:1 drift");
      actor.selectHeldSlot(4);
      harvest(actor, spruce, 20);
      actor.awaitBlock(spruce, AIR);
      dropSpruce = actor.awaitDroppedItem(SPRUCE);
      require(dropSpruce.item().equals(SPRUCE) && dropSpruce.item().damage() == 1
              && dropSpruce.entityId() != dropOak.entityId(),
          "stone-axe Packet21 spruce 17:1 drop absent");
      actor.selectHeldSlot(3);
      birch = place(actor, top, BlockFace.UP, 17, 2);
      require(actor.sustainTicks(5)
                  .blockAt(birch.x(), birch.y(), birch.z())
                  .equals(new BlockState(17, 2)),
          "live birch log 17:2 drift");
      actor.selectHeldSlot(4);
      harvest(actor, birch, 20);
      actor.awaitBlock(birch, AIR);
      dropBirch = actor.awaitDroppedItem(BIRCH);
      require(dropBirch.item().equals(BIRCH) && dropBirch.item().damage() == 2
              && dropBirch.entityId() != dropSpruce.entityId(),
          "stone-axe Packet21 birch 17:2 drop absent");
      java.util.List<BlockCellTransition> transitions = java.util.Arrays.asList(
          new BlockCellTransition(oak, new BlockState(17, 0), AIR),
          new BlockCellTransition(spruce, new BlockState(17, 1), AIR),
          new BlockCellTransition(birch, new BlockState(17, 2), AIR));
      java.util.List<RemoteItemStack> drops = java.util.Arrays.asList(
          dropOak.item(), dropSpruce.item(), dropBirch.item());
      require(BlockBreakDropFixture.execute("b1.7.3:block/017", "stateful-log", false, 275,
              transitions, transitions, BlockLifecycleDropMatrix.exact(drops), drops)
                  .subject().equals("b1.7.3:block/017"),
          "public log break/drop evidence drift");
      String evidence = "column=" + column + ",support=" + top.x() + ":" + top.y() + ":" + top.z()
          + ":1:0,oak=" + oak.x() + ":" + oak.y() + ":" + oak.z()
          + ":17:0->0:0,spruce=" + spruce.x() + ":" + spruce.y() + ":" + spruce.z()
          + ":17:1->0:0,birch=" + birch.x() + ":" + birch.y() + ":" + birch.z()
          + ":17:2->0:0,axe=275,drop=packet21-17:1:0+packet21-17:1:1+packet21-17:1:2,clients=1,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=raised-stone+oak17:0+spruce17:1+birch17:2|cause=packet14-stoneaxe275|wire=packet53-air+packet21-id17|oracle=stone-axe-log-drops-17:0-17:1-17:2|"
          + evidence;
      System.out.println("WORLDLINE_M301_AXE=" + evidence);
      System.out.println("WORLDLINE_M301_TRACE=" + trace);
      System.out.println("WORLDLINE_M301_SIGNATURE=" + sha(trace));
    } finally {
      actor.close();
      server.close();
    }
  }
  private static void harvest(B173WireClient a, BlockPosition target, int ticks) {
    a.beginBreak(target);
    a.sustainTicks(ticks);
    a.finishBreak(target);
  }
  private static BlockPosition place(
      B173WireClient a, BlockPosition support, BlockFace face, int id, int meta) throws Exception {
    BlockPosition target = face.adjacent(support);
    a.placeHeldBlock(support, face);
    a.awaitBlock(target, new BlockState(id, meta));
    return target;
  }
  private static BlockPosition foundation(RemoteChunkSnapshot q, int cx, int cz) {
    for (int x = 4; x <= 11; x++)
      for (int z = 4; z <= 11; z++)
        for (int y = 126; y >= 1; y--)
          if (q.blockAt(x, y, z).legacyId() == 3 && water(q.blockAt(x, y + 1, z).legacyId()))
            return new BlockPosition(cx * 16 + x, y, cz * 16 + z);
    throw new IllegalStateException("no deterministic axe-log-breaks foundation");
  }
  private static void require(boolean v, String m) {
    if (!v)
      throw new IllegalStateException(m);
  }
}
