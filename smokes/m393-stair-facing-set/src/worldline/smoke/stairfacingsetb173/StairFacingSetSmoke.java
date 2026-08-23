package worldline.smoke.stairfacingsetb173;
import static worldline.b173server.B173FixtureSupport.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Places oak stairs 53 and cobble stairs 67 with two look-yaw facings each as one SET. */
public final class StairFacingSetSmoke {
  private StairFacingSetSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 7)
      throw new IllegalArgumentException(
          "usage: StairFacingSetSmoke server.jar workspace port seed username chunkX chunkZ");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    int cx = Integer.parseInt(a[5]), cz = Integer.parseInt(a[6]);
    require(seed == 17320110707L && user.equals("StairFace393") && user.length() <= 16,
        "stair-facing-set identity drift");
    Duration timeout = Duration.ofSeconds(90);
    B173DedicatedServer server =
        new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout), reader = null;
    BlockPosition top, pad1, pad2, pad3, oakEast, oakWest, cobbleEast, cobbleWest;
    int column;
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1, 2},
          new int[] {1, 53, 67}, new int[] {48, 4, 4}, new int[] {0, 0, 0});
      actor.connect();
      actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() == 3, "stair-facing-set inventory drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      top = foundation(initial, cx, cz);
      column = 0;
      actor.selectHeldSlot(0);
      while (
          water(initial.blockAt(local(top.x(), cx), top.y() + 1, local(top.z(), cz)).legacyId())) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        require(++column <= 15, "water column exceeded stair-facing-set fixture");
      }
      for (int lift = 0; lift < 8; lift++) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        column++;
      }
      pad1 = place(actor, top, BlockFace.EAST, 1);
      pad2 = place(actor, pad1, BlockFace.EAST, 1);
      pad3 = place(actor, pad2, BlockFace.EAST, 1);
      actor.selectHeldSlot(1);
      oakEast = stair(actor, top, 53, 0, -90F);
      oakWest = stair(actor, pad1, 53, 1, 90F);
      actor.selectHeldSlot(2);
      cobbleEast = stair(actor, pad2, 67, 0, -90F);
      cobbleWest = stair(actor, pad3, 67, 1, 90F);
      actor.close();
      awaitPlayers(server, 0);
      server.save();
      reader = new B173WireClient("127.0.0.1", port, user, timeout);
      reader.connect();
      reader.synchronizePose();
      RemoteChunkSnapshot after = reader.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      require(after.blockAt(local(top.x(), cx), top.y(), local(top.z(), cz))
                  .equals(new BlockState(1, 0))
              && after.blockAt(local(oakEast.x(), cx), oakEast.y(), local(oakEast.z(), cz))
                  .equals(new BlockState(53, 0))
              && after.blockAt(local(oakWest.x(), cx), oakWest.y(), local(oakWest.z(), cz))
                  .equals(new BlockState(53, 1))
              && after.blockAt(local(cobbleEast.x(), cx), cobbleEast.y(), local(cobbleEast.z(), cz))
                  .equals(new BlockState(67, 0))
              && after.blockAt(local(cobbleWest.x(), cx), cobbleWest.y(), local(cobbleWest.z(), cz))
                  .equals(new BlockState(67, 1)),
          "persisted stair-facing-set drift");
      String evidence = "column=" + column + ",support=" + top.x() + ":" + top.y() + ":" + top.z()
          + ":1:0,oak=" + oakEast.x() + ":" + oakEast.y() + ":" + oakEast.z() + ":53:0+"
          + oakWest.x() + ":" + oakWest.y() + ":" + oakWest.z() + ":53:1,cobble=" + cobbleEast.x()
          + ":" + cobbleEast.y() + ":" + cobbleEast.z() + ":67:0+" + cobbleWest.x() + ":"
          + cobbleWest.y() + ":" + cobbleWest.z()
          + ":67:1,look=-90+90,persisted=true,clients=2,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=raised-stone+oakstairs53+cobblestairs67|cause=packet15-item53+look-90+look90+packet15-item67+look-90+look90|wire=packet53-oakstairs53:0+53:1+cobblestairs67:0+67:1|oracle=look-facing-metadata-set+fresh-login|"
          + evidence;
      System.out.println("WORLDLINE_M393_SET=" + evidence);
      System.out.println("WORLDLINE_M393_TRACE=" + trace);
      System.out.println("WORLDLINE_M393_SIGNATURE=" + sha(trace));
    } finally {
      actor.close();
      if (reader != null)
        reader.close();
      server.close();
    }
  }
  private static BlockPosition stair(
      B173WireClient a, BlockPosition support, int id, int meta, float yaw) throws Exception {
    BlockPosition target = BlockFace.UP.adjacent(support);
    a.look(yaw, 0F);
    a.placeHeldBlock(support, BlockFace.UP);
    a.awaitBlock(target, new BlockState(id, meta));
    require(a.sustainTicks(5)
                .blockAt(target.x(), target.y(), target.z())
                .equals(new BlockState(id, meta)),
        "live stairs " + id + ":" + meta + " facing drift");
    return target;
  }
  private static BlockPosition foundation(RemoteChunkSnapshot q, int cx, int cz) {
    for (int x = 4; x <= 11; x++)
      for (int z = 4; z <= 11; z++)
        for (int y = 126; y >= 1; y--)
          if (q.blockAt(x, y, z).legacyId() == 3 && water(q.blockAt(x, y + 1, z).legacyId()))
            return new BlockPosition(cx * 16 + x, y, cz * 16 + z);
    throw new IllegalStateException("no deterministic stair-facing-set foundation");
  }
  private static void require(boolean v, String m) {
    if (!v)
      throw new IllegalStateException(m);
  }
}
