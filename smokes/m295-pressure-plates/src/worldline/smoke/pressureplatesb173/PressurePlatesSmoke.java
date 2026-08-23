package worldline.smoke.pressureplatesb173;
import static worldline.b173server.B173FixtureSupport.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Official wooden 72 and stone 70 plates: place, stand-to-power, step-off, persist unpowered. */
public final class PressurePlatesSmoke {
  private PressurePlatesSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 7)
      throw new IllegalArgumentException(
          "usage: PressurePlatesSmoke server.jar workspace port seed username chunkX chunkZ");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    int cx = Integer.parseInt(a[5]), cz = Integer.parseInt(a[6]);
    Duration timeout = Duration.ofSeconds(90);
    B173DedicatedServer server =
        new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout), reader = null;
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1, 2},
          new int[] {1, 72, 70}, new int[] {32, 1, 1}, new int[] {0, 0, 0});
      actor.connect();
      PlayerPose pose = actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() == 3, "plate inventory drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      BlockPosition top = foundation(initial, cx, cz);
      int column = 0;
      actor.selectHeldSlot(0);
      while (
          water(initial.blockAt(local(top.x(), cx), top.y() + 1, local(top.z(), cz)).legacyId())) {
        top = place(actor, top, BlockFace.UP, 1);
        pose = actor.moveAndObserve(0D, 1D, 0D, 1).resulting();
        require(++column <= 15, "water column exceeded plates fixture");
      }
      for (int lift = 0; lift < 8; lift++) {
        top = place(actor, top, BlockFace.UP, 1);
        pose = actor.moveAndObserve(0D, 1D, 0D, 1).resulting();
        column++;
      }
      BlockPosition pad = place(actor, top, BlockFace.EAST, 1);
      actor.selectHeldSlot(1);
      BlockPosition wood = place(actor, top, BlockFace.UP, 72);
      BlockState woodOff = worldline.test.WorldlineSmokeAwait.observe(actor, 1).blockAt(
          wood.x(), wood.y(), wood.z());
      require(woodOff.equals(new BlockState(72, 0)), "placed wooden plate 72:0 absent: " + woodOff);
      actor.selectHeldSlot(2);
      BlockPosition stone = place(actor, pad, BlockFace.UP, 70);
      BlockState stoneOff = worldline.test.WorldlineSmokeAwait.observe(actor, 1).blockAt(
          stone.x(), stone.y(), stone.z());
      require(
          stoneOff.equals(new BlockState(70, 0)), "placed stone plate 70:0 absent: " + stoneOff);
      actor.moveAndObserve(
          wood.x() + 0.5D - pose.x(), wood.y() - pose.y(), wood.z() + 0.5D - pose.z(), 5);
      BlockState woodOn =
          actor.awaitBlock(wood, new BlockState(72, 1)).blockAt(wood.x(), wood.y(), wood.z());
      require(woodOn.equals(new BlockState(72, 1)), "powered wooden plate 72:1 absent: " + woodOn);
      pose = actor.moveAndObserve(2D, 0D, 0D, 2).resulting();
      BlockState woodUp =
          actor.awaitBlock(wood, new BlockState(72, 0)).blockAt(wood.x(), wood.y(), wood.z());
      require(woodUp.equals(new BlockState(72, 0)), "wooden plate unpower 72:0 absent: " + woodUp);
      actor.moveAndObserve(
          stone.x() + 0.5D - pose.x(), stone.y() - pose.y(), stone.z() + 0.5D - pose.z(), 5);
      BlockState stoneOn =
          actor.awaitBlock(stone, new BlockState(70, 1)).blockAt(stone.x(), stone.y(), stone.z());
      require(stoneOn.equals(new BlockState(70, 1)), "powered stone plate 70:1 absent: " + stoneOn);
      actor.moveAndObserve(-2D, 0D, 0D, 2);
      BlockState stoneUp =
          actor.awaitBlock(stone, new BlockState(70, 0)).blockAt(stone.x(), stone.y(), stone.z());
      require(stoneUp.equals(new BlockState(70, 0)), "stone plate unpower 70:0 absent: " + stoneUp);
      actor.close();
      awaitPlayers(server, 0);
      server.save();
      reader = new B173WireClient("127.0.0.1", port, user, timeout);
      reader.connect();
      reader.synchronizePose();
      RemoteChunkSnapshot after = reader.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      require(after.blockAt(local(wood.x(), cx), wood.y(), local(wood.z(), cz))
                  .equals(new BlockState(72, 0)),
          "persisted wooden plate drift");
      require(after.blockAt(local(stone.x(), cx), stone.y(), local(stone.z(), cz))
                  .equals(new BlockState(70, 0)),
          "persisted stone plate drift");
      String evidence = "column=" + column + ",support=" + top.x() + ":" + top.y() + ":" + top.z()
          + ":1:0,pad=" + pad.x() + ":" + pad.y() + ":" + pad.z() + ":1:0,wood=" + wood.x() + ":"
          + wood.y() + ":" + wood.z() + ":72:0->1->0,stone=" + stone.x() + ":" + stone.y() + ":"
          + stone.z() + ":70:0->1->0,persisted=72:0+70:0,clients=2,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=raised-stone+plate72+plate70|cause=moveAndObserve-on-each-plate|wire=packet53-72:0->1->0+70:0->1->0|oracle=live-power-both-families+deterministic-unpower+fresh-login-unpowered|"
          + evidence;
      System.out.println("WORLDLINE_M295_PLATES=" + evidence);
      System.out.println("WORLDLINE_M295_TRACE=" + trace);
      System.out.println("WORLDLINE_M295_SIGNATURE=" + sha(trace));
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
    throw new IllegalStateException("no deterministic plates foundation");
  }
  private static void require(boolean v, String m) {
    if (!v)
      throw new IllegalStateException(m);
  }
}
