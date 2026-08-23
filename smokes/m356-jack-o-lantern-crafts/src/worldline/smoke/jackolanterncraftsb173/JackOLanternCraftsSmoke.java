package worldline.smoke.jackolanterncraftsb173;
import static worldline.b173server.B173FixtureSupport.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Crafts jack-o-lantern 91 from pumpkin 86 plus torch 50, then places remaining 86 and crafted 91. */
public final class JackOLanternCraftsSmoke {
  private JackOLanternCraftsSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 7)
      throw new IllegalArgumentException(
          "usage: JackOLanternCraftsSmoke server.jar workspace port seed username chunkX chunkZ");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    int cx = Integer.parseInt(a[5]), cz = Integer.parseInt(a[6]);
    Duration timeout = Duration.ofSeconds(90);
    require(user.length() <= 16, "username exceeds 16");
    B173DedicatedServer server =
        new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout), reader = null;
    BlockPosition top, pumpkin, lantern;
    int column;
    BlockState pumpkinPlaced, lanternPlaced;
    RemoteWorldView live;
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1, 2},
          new int[] {1, 86, 50}, new int[] {32, 2, 1}, new int[] {0, 0, 0});
      actor.connect();
      actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() == 3, "jack-o-lantern inventory seed drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      top = foundation(initial, cx, cz);
      column = 0;
      actor.selectHeldSlot(0);
      while (
          water(initial.blockAt(local(top.x(), cx), top.y() + 1, local(top.z(), cz)).legacyId())) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        require(++column <= 15, "water column exceeded jack-o-lantern fixture");
      }
      for (int lift = 0; lift < 8; lift++) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        column++;
      }
      require(actor.inventory().slot(37).item().equals(B173JackOLanternCrafts.PUMPKINS)
              && actor.inventory().slot(38).item().equals(B173JackOLanternCrafts.TORCH),
          "pumpkin 86 plus torch 50 seed drift");
      B173JackOLanternCrafts.apply(actor);
      require(B173JackOLanternCrafts.stored(actor.inventory()),
          "crafted jack-o-lantern 91 inventory drift");
      actor.selectHeldSlot(1);
      actor.look(-90F, 0F);
      pumpkin = BlockFace.UP.adjacent(top);
      actor.placeHeldBlock(top, BlockFace.UP);
      pumpkinPlaced = new BlockState(86, 1);
      actor.awaitBlock(pumpkin, pumpkinPlaced);
      lantern = BlockFace.UP.adjacent(pumpkin);
      actor.selectHeldSlot(2);
      actor.placeHeldBlock(pumpkin, BlockFace.UP);
      lanternPlaced = new BlockState(91, 1);
      actor.awaitBlock(lantern, lanternPlaced);
      live = worldline.test.WorldlineSmokeAwait.observe(actor, 5);
      require(live.blockAt(pumpkin.x(), pumpkin.y(), pumpkin.z()).equals(pumpkinPlaced)
              && live.blockAt(lantern.x(), lantern.y(), lantern.z()).equals(lanternPlaced),
          "west pumpkin 86 or jack-o-lantern 91 facing drift");
      actor.close();
      awaitPlayers(server, 0);
      server.save();
      require(server.player(user).inventoryItems() == 1, "jack-o-lantern persistence count drift");
      reader = new B173WireClient("127.0.0.1", port, user, timeout);
      reader.connect();
      reader.synchronizePose();
      RemoteChunkSnapshot after = reader.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      require(after.blockAt(local(top.x(), cx), top.y(), local(top.z(), cz))
                  .equals(new BlockState(1, 0))
              && after.blockAt(local(pumpkin.x(), cx), pumpkin.y(), local(pumpkin.z(), cz))
                  .equals(pumpkinPlaced)
              && after.blockAt(local(lantern.x(), cx), lantern.y(), local(lantern.z(), cz))
                  .equals(lanternPlaced),
          "persisted pumpkin 86 or jack-o-lantern 91 drift");
      String evidence = "column=" + column + ",support=" + top.x() + ":" + top.y() + ":" + top.z()
          + ":1:0,pumpkin=" + pumpkin.x() + ":" + pumpkin.y() + ":" + pumpkin.z()
          + ":86:1,jackolantern=" + lantern.x() + ":" + lantern.y() + ":" + lantern.z()
          + ":91:1,look=-90:0,recipe=86+50->91,persisted=true,clients=2,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=raised-stone+pumpkin86x2+torch50|cause=packet102-window0-pumpkin-over-torch+packet15-item86+item91+look-90|wire=result91+packet53-pumpkin86:1+jackolantern91:1|oracle=craft-output+look-facing-metadata+fresh-login|"
          + evidence;
      System.out.println("WORLDLINE_M356_CRAFTS=" + evidence);
      System.out.println("WORLDLINE_M356_TRACE=" + trace);
      System.out.println("WORLDLINE_M356_SIGNATURE=" + sha(trace));
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
    throw new IllegalStateException("no deterministic jack-o-lantern foundation");
  }
  private static void require(boolean v, String m) {
    if (!v)
      throw new IllegalStateException(m);
  }
}
