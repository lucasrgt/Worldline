package worldline.smoke.repeaterdelaysetb173;
import static worldline.b173server.B173FixtureSupport.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Places official repeater 356 as 93 and Packet15-tunes delay bits through 1-4 ticks. */
public final class RepeaterDelaySetSmoke {
  private RepeaterDelaySetSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 7)
      throw new IllegalArgumentException(
          "usage: RepeaterDelaySetSmoke server.jar workspace port seed username chunkX chunkZ");
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
    BlockPosition top, repeater;
    BlockState d1, d2, d3, d4, held;
    int column;
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1},
          new int[] {1, 356}, new int[] {32, 1}, new int[] {0, 0});
      actor.connect();
      actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() == 2, "repeater delay-set inventory drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      top = foundation(initial, cx, cz);
      column = 0;
      actor.selectHeldSlot(0);
      while (
          water(initial.blockAt(local(top.x(), cx), top.y() + 1, local(top.z(), cz)).legacyId())) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        require(++column <= 15, "water column exceeded repeater delay-set fixture");
      }
      for (int lift = 0; lift < 8; lift++) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        column++;
      }
      actor.moveAndObserve(0D, 0D, 2D, 1);
      repeater = BlockFace.UP.adjacent(top);
      actor.look(90F, 0F);
      worldline.test.WorldlineSmokeAwait.observe(actor, 2);
      actor.selectHeldSlot(1);
      actor.useHeldItemOnBlock(top, BlockFace.UP);
      d1 = actor.awaitBlock(repeater, new BlockState(93, 3))
               .blockAt(repeater.x(), repeater.y(), repeater.z());
      require(d1.equals(new BlockState(93, 3)), "west 1-tick unpowered repeater drift: " + d1);
      actor.selectHeldSlot(2);
      d2 = tune(actor, repeater, 7);
      d3 = tune(actor, repeater, 11);
      d4 = tune(actor, repeater, 15);
      require(d2.equals(new BlockState(93, 7)) && d3.equals(new BlockState(93, 11))
              && d4.equals(new BlockState(93, 15)) && delay(d1) == 1 && delay(d2) == 2
              && delay(d3) == 3 && delay(d4) == 4,
          "repeater delay bits drift: " + d1 + " / " + d2 + " / " + d3 + " / " + d4);
      held = worldline.test.WorldlineSmokeAwait.observe(actor, 5).blockAt(
          repeater.x(), repeater.y(), repeater.z());
      require(
          held.equals(new BlockState(93, 15)), "4-tick repeater did not stay unpowered: " + held);
      actor.close();
      awaitPlayers(server, 0);
      server.save();
      reader = new B173WireClient("127.0.0.1", port, user, timeout);
      reader.connect();
      reader.synchronizePose();
      RemoteChunkSnapshot after = reader.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      BlockState persisted =
          after.blockAt(local(repeater.x(), cx), repeater.y(), local(repeater.z(), cz));
      require(persisted.equals(new BlockState(93, 15)) && delay(persisted) == 4,
          "persisted 4-tick repeater drift: " + persisted);
      String evidence = "column=" + column + ",support=" + top.x() + ":" + top.y() + ":" + top.z()
          + ":1:0,repeater=" + repeater.x() + ":" + repeater.y() + ":" + repeater.z()
          + ":93:3->7->11->15,facing=3,delay=1->2->3->4,look=90:0,persisted=93:15,clients=2,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=raised-west-line+repeater93|cause=packet15-item356-place+empty-hand-packet15-tune|wire=packet53-repeater93:3->7->11->15|oracle=delay-bits-1-4+fresh-login-93:15|"
          + evidence;
      System.out.println("WORLDLINE_M341_DELAY=" + evidence);
      System.out.println("WORLDLINE_M341_TRACE=" + trace);
      System.out.println("WORLDLINE_M341_SIGNATURE=" + sha(trace));
    } finally {
      actor.close();
      if (reader != null)
        reader.close();
      server.close();
    }
  }
  private static BlockState tune(B173WireClient a, BlockPosition repeater, int meta)
      throws Exception {
    a.activateBlock(repeater, BlockFace.UP);
    BlockState live = a.awaitBlock(repeater, new BlockState(93, meta))
                          .blockAt(repeater.x(), repeater.y(), repeater.z());
    require(live.equals(new BlockState(93, meta)) && delay(live) == ((meta >> 2) & 3) + 1,
        "Packet15 delay tune drift: " + live);
    return live;
  }
  private static int delay(BlockState s) {
    require(s.legacyId() == 93 || s.legacyId() == 94, "repeater id drift: " + s);
    return ((s.metadata() >> 2) & 3) + 1;
  }
  private static BlockPosition foundation(RemoteChunkSnapshot q, int cx, int cz) {
    for (int x = 4; x <= 11; x++)
      for (int z = 4; z <= 11; z++)
        for (int y = 126; y >= 1; y--)
          if (q.blockAt(x, y, z).legacyId() == 3 && water(q.blockAt(x, y + 1, z).legacyId()))
            return new BlockPosition(cx * 16 + x, y, cz * 16 + z);
    throw new IllegalStateException("no deterministic repeater delay-set foundation");
  }
  private static void require(boolean v, String m) {
    if (!v)
      throw new IllegalStateException(m);
  }
}
