package worldline.smoke.shovelsoftbreaksb173;
import static worldline.b173server.B173FixtureSupport.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Places dirt 3, sand 12, gravel 13, and clay 82 on raised stone, breaks each with gold shovel 284, and freezes Packet21 drops. */
public final class ShovelSoftBreaksSmoke {
  private static final RemoteItemStack DIRT = new RemoteItemStack(3, 1, 0),
                                       SAND = new RemoteItemStack(12, 1, 0),
                                       GRAVEL = new RemoteItemStack(13, 1, 0),
                                       FLINT = new RemoteItemStack(318, 1, 0),
                                       CLAY_BALL = new RemoteItemStack(337, 1, 0),
                                       SHOVEL = new RemoteItemStack(284, 1, 0);
  private static final BlockState AIR = new BlockState(0, 0);
  private ShovelSoftBreaksSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 7)
      throw new IllegalArgumentException(
          "usage: ShovelSoftBreaksSmoke server.jar workspace port seed username chunkX chunkZ");
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
    BlockPosition top;
    int column;
    String dirt, sand, gravel, clay;
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1, 2, 3, 4, 5},
          new int[] {1, 3, 12, 13, 82, 284}, new int[] {32, 1, 1, 1, 1, 1},
          new int[] {0, 0, 0, 0, 0, 0});
      actor.connect();
      actor.synchronizePose();
      RemoteInventoryView inventory = actor.awaitInventory();
      require(inventory.occupiedSlots() == 6 && inventory.slot(41).item().equals(SHOVEL),
          "shovel-soft-breaks inventory drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      top = foundation(initial, cx, cz);
      column = 0;
      actor.selectHeldSlot(0);
      while (
          water(initial.blockAt(local(top.x(), cx), top.y() + 1, local(top.z(), cz)).legacyId())) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        require(++column <= 15, "water column exceeded shovel-soft-breaks fixture");
      }
      for (int lift = 0; lift < 8; lift++) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        column++;
      }
      dirt = harvest(actor, top, 1, 5, 3, DIRT);
      sand = harvest(actor, top, 2, 5, 12, SAND);
      gravel = harvest(actor, top, 3, 5, 13, GRAVEL);
      clay = harvest(actor, top, 4, 5, 82, CLAY_BALL);
      String evidence = "column=" + column + ",support=" + top.x() + ":" + top.y() + ":" + top.z()
          + ":1:0," + dirt + "," + sand + "," + gravel + "," + clay
          + ",shovel=284,clients=1,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=raised-stone+dirt3+sand12+gravel13+clay82|cause=packet14-goldshovel284|wire=packet53-air+packet21-id3+packet21-id12+packet21-id13+packet21-id337|oracle=shovel-soft-breaks-drops+cells-3-12-13-82-to-0|"
          + evidence;
      System.out.println("WORLDLINE_M302_SHOVEL=" + evidence);
      System.out.println("WORLDLINE_M302_TRACE=" + trace);
      System.out.println("WORLDLINE_M302_SIGNATURE=" + sha(trace));
    } finally {
      actor.close();
      server.close();
    }
  }
  private static String harvest(B173WireClient a, BlockPosition support, int placeSlot,
      int shovelSlot, int id, RemoteItemStack expected) throws Exception {
    a.selectHeldSlot(placeSlot);
    BlockPosition cell = place(a, support, BlockFace.UP, id);
    require(a.sustainTicks(5).blockAt(cell.x(), cell.y(), cell.z()).equals(new BlockState(id, 0)),
        "live block " + id + " drift");
    a.selectHeldSlot(shovelSlot);
    a.beginBreak(cell);
    a.sustainTicks(5);
    a.finishBreak(cell);
    a.awaitBlock(cell, AIR);
    RemoteDroppedItem drop = a.peekDroppedItem(expected);
    if (drop == null && id == 13)
      drop = a.peekDroppedItem(FLINT);
    if (drop == null)
      drop = a.awaitDroppedItem(expected);
    require(drop.item().legacyId() > 0 && drop.item().count() >= 1
            && a.sustainTicks(1).blockAt(cell.x(), cell.y(), cell.z()).equals(AIR),
        "Packet21 drop or cell " + id + "->0 absent");
    String name = id == 3 ? "dirt" : id == 12 ? "sand" : id == 13 ? "gravel" : "clay";
    return name + "=" + cell.x() + ":" + cell.y() + ":" + cell.z() + ":" + id
        + ":0->0:0,drop=packet21-" + drop.item().legacyId() + ":" + drop.item().count() + ":"
        + drop.item().damage();
  }
  private static BlockPosition foundation(RemoteChunkSnapshot q, int cx, int cz) {
    for (int x = 4; x <= 11; x++)
      for (int z = 4; z <= 11; z++)
        for (int y = 126; y >= 1; y--)
          if (q.blockAt(x, y, z).legacyId() == 3 && water(q.blockAt(x, y + 1, z).legacyId()))
            return new BlockPosition(cx * 16 + x, y, cz * 16 + z);
    throw new IllegalStateException("no deterministic shovel-soft-breaks foundation");
  }
  private static void require(boolean v, String m) {
    if (!v)
      throw new IllegalStateException(m);
  }
}
