package worldline.smoke.remainingspongeglassiceb173;
import static worldline.b173server.B173FixtureSupport.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Places official sponge 19, glass 20, and ice 79 together as one transparent/odd-solid family. */
public final class RemainingSpongeGlassIceSmoke {
  private RemainingSpongeGlassIceSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 7)
      throw new IllegalArgumentException(
          "usage: RemainingSpongeGlassIceSmoke server.jar workspace port seed username chunkX chunkZ");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    int cx = Integer.parseInt(a[5]), cz = Integer.parseInt(a[6]);
    require(seed == 17320110707L && user.equals("SpongeGls434") && user.length() <= 16,
        "remaining-sponge-glass-ice identity drift");
    Duration timeout = Duration.ofSeconds(90);
    B173DedicatedServer server =
        new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout), reader = null;
    BlockPosition top, west, east, sponge, glass, ice;
    int column;
    BlockState spongePlaced = new BlockState(19, 0), glassPlaced = new BlockState(20, 0),
               icePlaced = new BlockState(79, 0);
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1, 2, 3},
          new int[] {1, 19, 20, 79}, new int[] {32, 1, 1, 1}, new int[] {0, 0, 0, 0});
      actor.connect();
      actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() == 4,
          "remaining-sponge-glass-ice inventory drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      top = foundation(initial, cx, cz);
      column = 0;
      actor.selectHeldSlot(0);
      while (
          water(initial.blockAt(local(top.x(), cx), top.y() + 1, local(top.z(), cz)).legacyId())) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        require(++column <= 15, "water column exceeded remaining-sponge-glass-ice fixture");
      }
      for (int lift = 0; lift < 8; lift++) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        column++;
      }
      west = place(actor, top, BlockFace.WEST, 1);
      east = place(actor, top, BlockFace.EAST, 1);
      actor.selectHeldSlot(1);
      sponge = BlockFace.UP.adjacent(top);
      actor.placeHeldBlock(top, BlockFace.UP);
      actor.awaitBlock(sponge, spongePlaced);
      actor.selectHeldSlot(2);
      glass = BlockFace.UP.adjacent(west);
      actor.placeHeldBlock(west, BlockFace.UP);
      actor.awaitBlock(glass, glassPlaced);
      actor.selectHeldSlot(3);
      ice = BlockFace.UP.adjacent(east);
      actor.placeHeldBlock(east, BlockFace.UP);
      actor.awaitBlock(ice, icePlaced);
      RemoteWorldView live = worldline.test.WorldlineSmokeAwait.observe(actor, 5);
      require(live.blockAt(sponge.x(), sponge.y(), sponge.z()).equals(spongePlaced)
              && live.blockAt(glass.x(), glass.y(), glass.z()).equals(glassPlaced)
              && live.blockAt(ice.x(), ice.y(), ice.z()).equals(icePlaced),
          "live remaining-sponge-glass-ice drift");
      actor.close();
      awaitPlayers(server, 0);
      server.save();
      reader = new B173WireClient("127.0.0.1", port, user, timeout);
      reader.connect();
      reader.synchronizePose();
      RemoteChunkSnapshot after = reader.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      require(after.blockAt(local(top.x(), cx), top.y(), local(top.z(), cz))
                  .equals(new BlockState(1, 0))
              && after.blockAt(local(west.x(), cx), west.y(), local(west.z(), cz))
                  .equals(new BlockState(1, 0))
              && after.blockAt(local(east.x(), cx), east.y(), local(east.z(), cz))
                  .equals(new BlockState(1, 0))
              && after.blockAt(local(sponge.x(), cx), sponge.y(), local(sponge.z(), cz))
                  .equals(spongePlaced)
              && after.blockAt(local(glass.x(), cx), glass.y(), local(glass.z(), cz))
                  .equals(glassPlaced)
              && after.blockAt(local(ice.x(), cx), ice.y(), local(ice.z(), cz)).equals(icePlaced),
          "persisted remaining-sponge-glass-ice drift");
      String evidence = "column=" + column + ",support=" + top.x() + ":" + top.y() + ":" + top.z()
          + ":1:0,sponge=" + sponge.x() + ":" + sponge.y() + ":" + sponge.z()
          + ":19:0,west=" + west.x() + ":" + west.y() + ":" + west.z() + ":1:0,glass=" + glass.x()
          + ":" + glass.y() + ":" + glass.z() + ":20:0,east=" + east.x() + ":" + east.y() + ":"
          + east.z() + ":1:0,ice=" + ice.x() + ":" + ice.y() + ":" + ice.z()
          + ":79:0,persisted=true,clients=2,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=raised-stone+sponge19+glass20+ice79|cause=packet15-item19+packet15-item20+packet15-item79|wire=packet53-sponge19:0+packet53-glass20:0+packet53-ice79:0|oracle=live-transparent-odd-solid-place19+20+79+fresh-login|"
          + evidence;
      System.out.println("WORLDLINE_M434_SET=" + evidence);
      System.out.println("WORLDLINE_M434_TRACE=" + trace);
      System.out.println("WORLDLINE_M434_SIGNATURE=" + sha(trace));
    } finally {
      actor.close();
      if (reader != null)
        reader.close();
      server.close();
    }
  }
  private static BlockPosition foundation(RemoteChunkSnapshot q, int cx, int cz) {
    for (int x = 4; x <= 11; x++)
      for (int z = 4; z <= 11; z++)
        for (int y = 126; y >= 1; y--)
          if (q.blockAt(x, y, z).legacyId() == 3 && water(q.blockAt(x, y + 1, z).legacyId()))
            return new BlockPosition(cx * 16 + x, y, cz * 16 + z);
    throw new IllegalStateException("no deterministic remaining-sponge-glass-ice foundation");
  }
  private static void require(boolean v, String m) {
    if (!v)
      throw new IllegalStateException(m);
  }
}
