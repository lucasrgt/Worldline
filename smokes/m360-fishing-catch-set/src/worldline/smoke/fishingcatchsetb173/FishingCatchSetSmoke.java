package worldline.smoke.fishingcatchsetb173;
import static worldline.b173server.B173FixtureSupport.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Casts official fishing rod 346 (Packet23 type 90) and reels an official Packet21 catch. */
public final class FishingCatchSetSmoke {
  private static final RemoteItemStack FISH = new RemoteItemStack(349, 1, 0);
  private FishingCatchSetSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 7)
      throw new IllegalArgumentException(
          "usage: FishingCatchSetSmoke server.jar workspace port seed username chunkX chunkZ");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    int cx = Integer.parseInt(a[5]), cz = Integer.parseInt(a[6]);
    Duration timeout = Duration.ofSeconds(300);
    B173DedicatedServer server =
        new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout);
    BlockPosition top, pool;
    int column;
    RemoteObjectSpawn hook;
    RemoteDroppedItem catchDrop;
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1, 2},
          new int[] {1, 9, 346}, new int[] {48, 16, 1}, new int[] {0, 0, 0});
      actor.connect();
      actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() == 3, "fishing-catch inventory drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      top = foundation(initial, cx, cz);
      column = 0;
      actor.selectHeldSlot(0);
      while (
          fluid(initial.blockAt(local(top.x(), cx), top.y() + 1, local(top.z(), cz)).legacyId())) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        require(++column <= 15, "water column exceeded fishing-catch fixture");
      }
      for (int lift = 0; lift < 8; lift++) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        column++;
      }
      pad(actor, top);
      actor.selectHeldSlot(1);
      pool = fill(actor, top);
      require(fluid(worldline.test.WorldlineSmokeAwait.observe(actor, 5)
                      .blockAt(pool.x(), pool.y(), pool.z())
                      .legacyId()),
          "still-water fishing pool drift");
      actor.selectHeldSlot(2);
      actor.look(0F, 40F);
      actor.useSelectedItemInAir();
      hook = actor.awaitObjectSpawn(90);
      require(hook.type() == 90 && hook.entityId() != actor.state().entityId(),
          "fishing-hook Packet23 type 90 absent");
      catchDrop = reel(actor, hook);
      require(
          catchDrop != null && catchDrop.item().legacyId() == 349 && catchDrop.item().count() == 1,
          "fishing catch Packet21 349 absent");
      actor.close();
      awaitPlayers(server, 0);
      server.save();
      String evidence = "column=" + column + ",support=" + top.x() + ":" + top.y() + ":" + top.z()
          + ":1:0,water=" + pool.x() + ":" + pool.y() + ":" + pool.z()
          + ":9:0,hook=type90,catch=349,rod=346,clients=1,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=raised-stone+still-water-dock9|cause=packet15-dir255-rod346+reel|wire=packet23-type90+packet21-349|oracle=hook-then-official-catch|"
          + evidence;
      System.out.println("WORLDLINE_M360_CATCH=" + evidence);
      System.out.println("WORLDLINE_M360_TRACE=" + trace);
      System.out.println("WORLDLINE_M360_SIGNATURE=" + sha(trace));
    } finally {
      actor.close();
      server.close();
    }
  }
  private static RemoteDroppedItem reel(B173WireClient a, RemoteObjectSpawn hook) throws Exception {
    require(hook != null && hook.type() == 90, "fishing-hook identity drift");
    for (int attempt = 0; attempt < 120; attempt++) {
      worldline.test.WorldlineSmokeAwait.observe(a, 40);
      a.useSelectedItemInAir();
      RemoteDroppedItem drop = worldline.test.WorldlineSmokeAwait.awaitEntityOrNull(
          a, () -> a.peekDroppedItem(FISH), value -> value != null, "fishing catch", 8);
      if (drop != null)
        return drop;
      a.useSelectedItemInAir();
      RemoteObjectSpawn recast = a.awaitObjectSpawn(90);
      require(recast.type() == 90 && recast.entityId() != a.state().entityId(),
          "fishing-hook recast Packet23 type 90 absent");
    }
    throw new IllegalStateException("fishing catch Packet21 349 absent after bounded recasts");
  }
  private static void pad(B173WireClient a, BlockPosition top) throws Exception {
    for (int dz = 1; dz <= 3; dz++) {
      BlockPosition row =
          place(a, new BlockPosition(top.x(), top.y(), top.z() + dz - 1), BlockFace.SOUTH, 1);
      place(a, row, BlockFace.WEST, 1);
      place(a, row, BlockFace.EAST, 1);
    }
  }
  private static BlockPosition fill(B173WireClient a, BlockPosition top) throws Exception {
    BlockPosition center = null;
    for (int dz = 1; dz <= 3; dz++)
      for (int dx = -1; dx <= 1; dx++) {
        BlockPosition cell =
            place(a, new BlockPosition(top.x() + dx, top.y(), top.z() + dz), BlockFace.UP, 9);
        if (dx == 0 && dz == 2)
          center = cell;
      }
    require(center != null, "fishing pool center absent");
    return center;
  }
  private static BlockPosition foundation(RemoteChunkSnapshot q, int cx, int cz) {
    for (int x = 4; x <= 11; x++)
      for (int z = 4; z <= 11; z++)
        for (int y = 126; y >= 1; y--)
          if (q.blockAt(x, y, z).legacyId() == 3 && fluid(q.blockAt(x, y + 1, z).legacyId()))
            return new BlockPosition(cx * 16 + x, y, cz * 16 + z);
    throw new IllegalStateException("no deterministic fishing-catch foundation");
  }
  private static boolean fluid(int id) {
    return id == 8 || id == 9;
  }
  private static void require(boolean v, String m) {
    if (!v)
      throw new IllegalStateException(m);
  }
}
