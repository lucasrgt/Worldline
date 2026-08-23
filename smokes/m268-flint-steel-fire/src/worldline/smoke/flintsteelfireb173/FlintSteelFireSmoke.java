package worldline.smoke.flintsteelfireb173;
import static worldline.b173server.B173FixtureSupport.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Places official flint-and-steel fire 51:0 on raised stone and freezes persist or decay. */
public final class FlintSteelFireSmoke {
  private FlintSteelFireSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 7)
      throw new IllegalArgumentException(
          "usage: FlintSteelFireSmoke server.jar workspace port seed username chunkX chunkZ");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    int cx = Integer.parseInt(a[5]), cz = Integer.parseInt(a[6]);
    Duration timeout = Duration.ofSeconds(90);
    B173DedicatedServer server =
        new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout), reader = null;
    BlockPosition top, flame;
    int column;
    BlockState placed, held, fresh;
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1},
          new int[] {1, 259}, new int[] {32, 1}, new int[] {0, 0});
      actor.connect();
      actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() == 2, "flint-steel inventory drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      top = foundation(initial, cx, cz);
      column = 0;
      actor.selectHeldSlot(0);
      while (
          water(initial.blockAt(local(top.x(), cx), top.y() + 1, local(top.z(), cz)).legacyId())) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        require(++column <= 15, "water column exceeded flint-steel fixture");
      }
      for (int lift = 0; lift < 8; lift++) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        column++;
      }
      actor.selectHeldSlot(1);
      flame = BlockFace.UP.adjacent(top);
      actor.useHeldItemOnBlock(top, BlockFace.UP);
      placed = new BlockState(51, 0);
      actor.awaitBlock(flame, placed);
      held = worldline.test.WorldlineSmokeAwait.observe(actor, 5).blockAt(
          flame.x(), flame.y(), flame.z());
      for (int wait = 5; held.equals(placed) && wait < 80; wait++)
        held = worldline.test.WorldlineSmokeAwait.observe(actor, 1).blockAt(
            flame.x(), flame.y(), flame.z());
      require(
          held.equals(placed) || held.equals(new BlockState(0, 0)), "flint-steel fire cell drift");
      actor.close();
      awaitPlayers(server, 0);
      server.save();
      reader = new B173WireClient("127.0.0.1", port, user, timeout);
      reader.connect();
      reader.synchronizePose();
      RemoteChunkSnapshot after = reader.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      fresh = after.blockAt(local(flame.x(), cx), flame.y(), local(flame.z(), cz));
      require(after.blockAt(local(top.x(), cx), top.y(), local(top.z(), cz))
                  .equals(new BlockState(1, 0))
              && (fresh.equals(placed) || fresh.equals(new BlockState(0, 0))),
          "fresh flint-steel fire cell drift");
      String evidence = "column=" + column + ",support=" + top.x() + ":" + top.y() + ":" + top.z()
          + ":1:0,fire=" + flame.x() + ":" + flame.y() + ":" + flame.z()
          + ":51:0,fresh=fire-or-air,clients=2,disconnect=clean";
      String trace = "v2|server=official-b1.7.3|seed=" + seed
          + "|fixture=raised-stone+flintsteel259|cause=packet15-item259|wire=packet53-fire51:0|oracle=live-place+bounded-live-fire-or-air+fresh-login-fire-or-air|nonclaim=stone-fire-persistence|"
          + evidence;
      System.out.println("WORLDLINE_M268_FIRE=" + evidence);
      System.out.println("WORLDLINE_M268_TRACE=" + trace);
      System.out.println("WORLDLINE_M268_SIGNATURE=" + sha(trace));
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
    throw new IllegalStateException("no deterministic flint-steel foundation");
  }
  private static void require(boolean v, String m) {
    if (!v)
      throw new IllegalStateException(m);
  }
}
