package worldline.smoke.redstoneoredustdropb173;
import static worldline.b173server.B173FixtureSupport.*;

import java.nio.file.*;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;
import worldline.testkit.BoundedAttempts;

/** Breaks placed redstone ore 73 with iron pick 257, freezing removal and bounded dust membership. */
public final class RedstoneOreDustDropSmoke {
  private static final BlockState AIR = new BlockState(0, 0);
  private static final BlockState ORE = new BlockState(73, 0);
  private static final RemoteItemStack DUST_ONE = new RemoteItemStack(331, 1, 0);
  private RedstoneOreDustDropSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 7)
      throw new IllegalArgumentException(
          "usage: RedstoneOreDustDropSmoke server.jar workspace port seed username chunkX chunkZ");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    int cx = Integer.parseInt(a[5]), cz = Integer.parseInt(a[6]);
    Duration timeout = Duration.ofSeconds(90);
    B173DedicatedServer server = OfficialServerBootstrap.start(
        jar, workspace, port, seed, timeout);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout);
    BlockPosition top, ore;
    int column;
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D,
          new int[] {0, 1, 2}, new int[] {1, 73, 257}, new int[] {64, 1, 1},
          new int[] {0, 0, 0});
      actor.connect();
      actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() == 3, "redstone-ore-dust-drop inventory drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      top = foundation(initial, cx, cz);
      column = 0;
      actor.selectHeldSlot(0);
      while (
          water(initial.blockAt(local(top.x(), cx), top.y() + 1, local(top.z(), cz)).legacyId())) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        require(++column <= 15, "water column exceeded redstone-ore-dust-drop fixture");
      }
      top = raiseColumn(actor, top, 8);
      column += 8;
      actor.selectHeldSlot(1);
      ore = place(actor, top, BlockFace.UP, ORE);
      RemoteWorldView live = worldline.test.WorldlineSmokeAwait.observe(actor, 5);
      require(live.blockAt(ore.x(), ore.y(), ore.z()).equals(ORE), "live redstone ore drift");
      actor.selectHeldSlot(2);
      actor.beginBreak(ore);
      worldline.test.WorldlineSmokeAwait.observe(actor, 20);
      actor.finishBreak(ore);
      actor.awaitBlock(ore, AIR);
      RemoteDroppedItem dust = BoundedAttempts.until(40, attempt -> {
            worldline.test.WorldlineSmokeAwait.observe(actor, 1);
            return actor.peekDroppedItem(DUST_ONE);
          }, seen -> seen != null).value();
      require(dust.item().equals(DUST_ONE), "Packet21 count-one redstone dust drift");
      String evidence = "column=" + column + ",support=" + top.x() + ":" + top.y() + ":" + top.z()
          + ":1:0,ore=" + ore.x() + ":" + ore.y() + ":" + ore.z() + ":73:0->0:0,pick=iron257"
          + ",dust=packet21-331x1,wait=bounded<=40,clients=1,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=raised-stone-column+redstone-ore73|cause=packet14-ironpick257-full-break"
          + "|wire=packet53-air+packet21-331x1"
          + "|oracle=redstone-ore-dust-drop-not-m229-place-not-m571-glow|" + evidence;
      System.out.println("WORLDLINE_M743_SET=" + evidence);
      System.out.println("WORLDLINE_M743_TRACE=" + trace);
      System.out.println("WORLDLINE_M743_SIGNATURE=" + sha(trace));
    } finally {
      actor.close();
      server.close();
    }
  }
  private static BlockPosition foundation(RemoteChunkSnapshot q, int cx, int cz) {
    for (int x = 4; x <= 11; x++)
      for (int z = 4; z <= 11; z++)
        for (int y = 126; y >= 1; y--)
          if (q.blockAt(x, y, z).legacyId() == 3 && water(q.blockAt(x, y + 1, z).legacyId()))
            return new BlockPosition(cx * 16 + x, y, cz * 16 + z);
    throw new IllegalStateException("no deterministic redstone-ore-dust-drop foundation");
  }
  private static void require(boolean v, String m) {
    if (!v)
      throw new IllegalStateException(m);
  }
}
