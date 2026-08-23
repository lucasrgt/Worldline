package worldline.smoke.remainingpickbreaksb173;
import static worldline.b173server.B173FixtureSupport.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Holds gold pick 285 and diamond pick 278 and fully breaks mossy cobble 48, gold ore 14, and obsidian 49. */
public final class RemainingPickBreaksSmoke {
  private static final RemoteItemStack MOSSY = new RemoteItemStack(48, 1, 0),
                                       GOLD = new RemoteItemStack(14, 1, 0),
                                       OBSIDIAN = new RemoteItemStack(49, 1, 0);
  private static final BlockState AIR = new BlockState(0, 0);
  private RemainingPickBreaksSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 7)
      throw new IllegalArgumentException(
          "usage: RemainingPickBreaksSmoke server.jar workspace port seed username chunkX chunkZ");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    int cx = Integer.parseInt(a[5]), cz = Integer.parseInt(a[6]);
    require(seed == 17320110707L && user.equals("PickBreak375") && user.length() <= 16,
        "remaining-pick-breaks identity drift");
    Duration timeout = Duration.ofSeconds(90);
    B173DedicatedServer server =
        new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout);
    BlockPosition top, mossy, gold, obsidian;
    int column;
    RemoteDroppedItem mossyDrop, goldDrop, obsidianDrop;
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1, 2, 3, 4, 5},
          new int[] {1, 48, 14, 49, 285, 278}, new int[] {32, 1, 1, 1, 1, 1},
          new int[] {0, 0, 0, 0, 0, 0});
      actor.connect();
      actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() == 6, "remaining-pick-breaks inventory drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      top = foundation(initial, cx, cz);
      column = 0;
      actor.selectHeldSlot(0);
      while (
          water(initial.blockAt(local(top.x(), cx), top.y() + 1, local(top.z(), cz)).legacyId())) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        require(++column <= 15, "water column exceeded remaining-pick-breaks fixture");
      }
      for (int lift = 0; lift < 8; lift++) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        column++;
      }
      actor.selectHeldSlot(1);
      mossy = place(actor, top, BlockFace.UP, 48);
      actor.selectHeldSlot(2);
      gold = place(actor, top, BlockFace.EAST, 14);
      actor.selectHeldSlot(3);
      obsidian = place(actor, gold, BlockFace.UP, 49);
      RemoteWorldView live = worldline.test.WorldlineSmokeAwait.observe(actor, 5);
      require(live.blockAt(mossy.x(), mossy.y(), mossy.z()).equals(new BlockState(48, 0))
              && live.blockAt(gold.x(), gold.y(), gold.z()).equals(new BlockState(14, 0))
              && live.blockAt(obsidian.x(), obsidian.y(), obsidian.z())
                  .equals(new BlockState(49, 0)),
          "live remaining-pick family drift");
      actor.selectHeldSlot(4);
      mossyDrop = harvest(actor, mossy, 10, MOSSY);
      actor.selectHeldSlot(5);
      goldDrop = harvest(actor, gold, 20, GOLD);
      obsidianDrop = harvest(actor, obsidian, 60, OBSIDIAN);
      require(mossyDrop.item().legacyId() == 48 && goldDrop.item().legacyId() == 14
              && obsidianDrop.item().legacyId() == 49 && mossyDrop.item().count() == 1
              && goldDrop.item().count() == 1 && obsidianDrop.item().count() == 1,
          "Packet21 remaining-pick family drops absent");
      String evidence = "column=" + column + ",support=" + top.x() + ":" + top.y() + ":" + top.z()
          + ":1:0,mossy=" + mossy.x() + ":" + mossy.y() + ":" + mossy.z()
          + ":48:0->0:0,gold=" + gold.x() + ":" + gold.y() + ":" + gold.z()
          + ":14:0->0:0,obsidian=" + obsidian.x() + ":" + obsidian.y() + ":" + obsidian.z()
          + ":49:0->0:0,picks=285+278,drops=packet21-48+14+49,clients=1,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=raised-stone+mossy48+ore14+obsidian49|cause=packet14-goldpick285+diamondpick278|wire=packet53-air+packet21-id48+14+49|oracle=remaining-pick-breaks-family|"
          + evidence;
      System.out.println("WORLDLINE_M375_SET=" + evidence);
      System.out.println("WORLDLINE_M375_TRACE=" + trace);
      System.out.println("WORLDLINE_M375_SIGNATURE=" + sha(trace));
    } finally {
      actor.close();
      server.close();
    }
  }
  private static RemoteDroppedItem harvest(
      B173WireClient a, BlockPosition target, int ticks, RemoteItemStack item) throws Exception {
    a.beginBreak(target);
    worldline.test.WorldlineSmokeAwait.observe(a, ticks);
    a.finishBreak(target);
    a.awaitBlock(target, AIR);
    RemoteDroppedItem drop = a.awaitDroppedItem(item);
    require(drop.item().equals(item) && drop.item().legacyId() == item.legacyId()
            && drop.item().count() == 1,
        "Packet21 " + item.legacyId() + " drop absent");
    return drop;
  }
  private static BlockPosition foundation(RemoteChunkSnapshot q, int cx, int cz) {
    for (int x = 4; x <= 11; x++)
      for (int z = 4; z <= 11; z++)
        for (int y = 126; y >= 1; y--)
          if (q.blockAt(x, y, z).legacyId() == 3 && water(q.blockAt(x, y + 1, z).legacyId()))
            return new BlockPosition(cx * 16 + x, y, cz * 16 + z);
    throw new IllegalStateException("no deterministic remaining-pick-breaks foundation");
  }
  private static void require(boolean v, String m) {
    if (!v)
      throw new IllegalStateException(m);
  }
}
