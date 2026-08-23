package worldline.smoke.closablesb173;
import static worldline.b173server.B173FixtureSupport.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Places official wooden door 324 and trapdoor 96, opens both, then closes both and reloads. */
public final class ClosablesSmoke {
  private ClosablesSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 7)
      throw new IllegalArgumentException(
          "usage: ClosablesSmoke server.jar workspace port seed username chunkX chunkZ");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    int cx = Integer.parseInt(a[5]), cz = Integer.parseInt(a[6]);
    Duration timeout = Duration.ofSeconds(90);
    B173DedicatedServer server =
        new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout), reader = null;
    BlockPosition top, lower, upper, trap;
    int column;
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1, 2},
          new int[] {1, 324, 96}, new int[] {32, 1, 1}, new int[] {0, 0, 0});
      actor.connect();
      actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() == 3, "closables inventory drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      top = foundation(initial, cx, cz);
      column = 0;
      actor.selectHeldSlot(0);
      while (
          water(initial.blockAt(local(top.x(), cx), top.y() + 1, local(top.z(), cz)).legacyId())) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        require(++column <= 15, "water column exceeded closables fixture");
      }
      for (int lift = 0; lift < 8; lift++) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        column++;
      }
      lower = BlockFace.UP.adjacent(top);
      upper = BlockFace.UP.adjacent(lower);
      require(initial.blockAt(local(lower.x(), cx), lower.y(), local(lower.z(), cz)).legacyId() == 0
              && initial.blockAt(local(upper.x(), cx), upper.y(), local(upper.z(), cz)).legacyId()
                  == 0,
          "door cells were not initial air");
      actor.look(-90F, 0F);
      actor.selectHeldSlot(1);
      actor.useHeldItemOnBlock(top, BlockFace.UP);
      awaitDoor(actor, lower, upper, 0, 8);
      actor.selectHeldSlot(3);
      actor.activateBlock(lower, BlockFace.UP);
      awaitDoor(actor, lower, upper, 4, 12);
      actor.activateBlock(lower, BlockFace.UP);
      awaitDoor(actor, lower, upper, 0, 8);
      trap = BlockFace.EAST.adjacent(top);
      actor.selectHeldSlot(2);
      actor.placeHeldBlock(top, BlockFace.EAST);
      require(actor.awaitBlock(trap, new BlockState(96, 3))
                  .blockAt(trap.x(), trap.y(), trap.z())
                  .equals(new BlockState(96, 3)),
          "placed closed trapdoor 96:3 absent");
      actor.selectHeldSlot(3);
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
      require(after.blockAt(local(lower.x(), cx), lower.y(), local(lower.z(), cz))
                  .equals(new BlockState(64, 0))
              && after.blockAt(local(upper.x(), cx), upper.y(), local(upper.z(), cz))
                  .equals(new BlockState(64, 8)),
          "persisted closed wooden door drift");
      require(after.blockAt(local(trap.x(), cx), trap.y(), local(trap.z(), cz))
                  .equals(new BlockState(96, 3)),
          "persisted trapdoor closed drift");
      String evidence = "column=" + column + ",support=" + top.x() + ":" + top.y() + ":" + top.z()
          + ":1:0,lower=" + lower.x() + ":" + lower.y() + ":" + lower.z() + ":64:0->4->0,upper="
          + upper.x() + ":" + upper.y() + ":" + upper.z() + ":64:8->12->8,trap=" + trap.x() + ":"
          + trap.y() + ":" + trap.z() + ":96:3->7->3,persisted=true,clients=2,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=raised-stone+woodendoor64+trapdoor96-east|cause=packet15-item324-place+empty-hand-packet15-open+empty-hand-packet15-close+packet15-item96-place+empty-hand-packet15-open-then-close|wire=packet53-door64:0/8->4/12->0/8+packet53-trapdoor96:3->7->3|oracle=woodendoor-close+trapdoor-close+fresh-login|"
          + evidence;
      System.out.println("WORLDLINE_M306_CLOSABLES=" + evidence);
      System.out.println("WORLDLINE_M306_TRACE=" + trace);
      System.out.println("WORLDLINE_M306_SIGNATURE=" + sha(trace));
    } finally {
      actor.close();
      if (reader != null)
        reader.close();
      server.close();
    }
  }
  private static void awaitDoor(B173WireClient a, BlockPosition lower, BlockPosition upper,
      int lowMeta, int highMeta) throws Exception {
    RemoteWorldView v = a.awaitBlock(lower, new BlockState(64, lowMeta));
    if (!v.blockAt(upper.x(), upper.y(), upper.z()).equals(new BlockState(64, highMeta)))
      v = a.awaitBlock(upper, new BlockState(64, highMeta));
    BlockState low = v.blockAt(lower.x(), lower.y(), lower.z()),
               high = v.blockAt(upper.x(), upper.y(), upper.z());
    require(low.equals(new BlockState(64, lowMeta)) && high.equals(new BlockState(64, highMeta)),
        "wooden door cells " + low + " / " + high);
  }
  private static BlockPosition foundation(RemoteChunkSnapshot q, int cx, int cz) {
    for (int x = 4; x <= 11; x++)
      for (int z = 4; z <= 11; z++)
        for (int y = 126; y >= 1; y--)
          if (q.blockAt(x, y, z).legacyId() == 3 && water(q.blockAt(x, y + 1, z).legacyId()))
            return new BlockPosition(cx * 16 + x, y, cz * 16 + z);
    throw new IllegalStateException("no deterministic closables foundation");
  }
  private static void require(boolean v, String m) {
    if (!v)
      throw new IllegalStateException(m);
  }
}
