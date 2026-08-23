package worldline.smoke.trapdoorb173;
import static worldline.b173server.B173FixtureSupport.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Places official trapdoor 96 against a raised stone face and toggles it open then closed. */
public final class TrapdoorSmoke {
  private TrapdoorSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 7)
      throw new IllegalArgumentException(
          "usage: TrapdoorSmoke server.jar workspace port seed username chunkX chunkZ");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    int cx = Integer.parseInt(a[5]), cz = Integer.parseInt(a[6]);
    Duration timeout = Duration.ofSeconds(90);
    B173DedicatedServer server =
        new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout), reader = null;
    BlockPosition top, trap;
    int column;
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1},
          new int[] {1, 96}, new int[] {32, 1}, new int[] {0, 0});
      actor.connect();
      actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() == 2, "trapdoor inventory drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      top = foundation(initial, cx, cz);
      column = 0;
      actor.selectHeldSlot(0);
      while (
          water(initial.blockAt(local(top.x(), cx), top.y() + 1, local(top.z(), cz)).legacyId())) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        require(++column <= 15, "water column exceeded trapdoor fixture");
      }
      for (int lift = 0; lift < 8; lift++) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        column++;
      }
      trap = BlockFace.EAST.adjacent(top);
      actor.selectHeldSlot(1);
      actor.placeHeldBlock(top, BlockFace.EAST);
      require(actor.awaitBlock(trap, new BlockState(96, 3))
                  .blockAt(trap.x(), trap.y(), trap.z())
                  .equals(new BlockState(96, 3)),
          "placed trapdoor 96:3 absent");
      actor.selectHeldSlot(2);
      actor.activateBlock(trap, BlockFace.EAST);
      require(actor.awaitBlock(trap, new BlockState(96, 7))
                  .blockAt(trap.x(), trap.y(), trap.z())
                  .equals(new BlockState(96, 7)),
          "open trapdoor 96:7 absent");
      actor.activateBlock(trap, BlockFace.EAST);
      require(actor.awaitBlock(trap, new BlockState(96, 3))
                  .blockAt(trap.x(), trap.y(), trap.z())
                  .equals(new BlockState(96, 3)),
          "closed trapdoor 96:3 absent");
      actor.close();
      awaitPlayers(server, 0);
      server.save();
      reader = new B173WireClient("127.0.0.1", port, user, timeout);
      reader.connect();
      reader.synchronizePose();
      RemoteChunkSnapshot after = reader.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      require(after.blockAt(local(trap.x(), cx), trap.y(), local(trap.z(), cz))
                  .equals(new BlockState(96, 3)),
          "persisted trapdoor closed drift");
      String evidence = "column=" + column + ",support=" + top.x() + ":" + top.y() + ":" + top.z()
          + ":1:0,trap=" + trap.x() + ":" + trap.y() + ":" + trap.z()
          + ":96:3->7->3,persisted=true,clients=2,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=raised-stone+trapdoor96-east|cause=packet15-item96-place+empty-hand-packet15-toggle|wire=packet53-trapdoor96:3->7->3|oracle=live-toggle+fresh-login-closed-trapdoor|"
          + evidence;
      System.out.println("WORLDLINE_M163_TRAP=" + evidence);
      System.out.println("WORLDLINE_M163_TRACE=" + trace);
      System.out.println("WORLDLINE_M163_SIGNATURE=" + sha(trace));
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
    throw new IllegalStateException("no deterministic trapdoor foundation");
  }
  private static void require(boolean v, String m) {
    if (!v)
      throw new IllegalStateException(m);
  }
}
