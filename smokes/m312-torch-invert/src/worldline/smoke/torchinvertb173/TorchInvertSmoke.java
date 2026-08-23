package worldline.smoke.torchinvertb173;
import static worldline.b173server.B173FixtureSupport.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Places official redstone torch 76 on an unpowered wall, then inverts it to unlit 75. */
public final class TorchInvertSmoke {
  private TorchInvertSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 7)
      throw new IllegalArgumentException(
          "usage: TorchInvertSmoke server.jar workspace port seed username chunkX chunkZ");
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
    BlockPosition top, east, west, body, repeater, lever, torch;
    int column;
    BlockState on, off;
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1, 2, 3},
          new int[] {1, 356, 69, 76}, new int[] {32, 1, 1, 1}, new int[] {0, 0, 0, 0});
      actor.connect();
      actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() == 4, "torch invert inventory drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      top = foundation(initial, cx, cz);
      column = 0;
      actor.selectHeldSlot(0);
      while (
          water(initial.blockAt(local(top.x(), cx), top.y() + 1, local(top.z(), cz)).legacyId())) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        require(++column <= 15, "water column exceeded torch invert fixture");
      }
      for (int lift = 0; lift < 8; lift++) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        column++;
      }
      actor.moveAndObserve(0D, 0D, 2D, 1);
      east = place(actor, top, BlockFace.EAST, 1);
      west = place(actor, top, BlockFace.WEST, 1);
      body = place(actor, west, BlockFace.UP, 1);
      repeater = BlockFace.UP.adjacent(top);
      lever = BlockFace.UP.adjacent(east);
      torch = BlockFace.NORTH.adjacent(body);
      actor.selectHeldSlot(3);
      actor.placeHeldBlock(body, BlockFace.NORTH);
      on = new BlockState(76, 4);
      require(actor.awaitBlock(torch, on).blockAt(torch.x(), torch.y(), torch.z()).equals(on)
              && !on.equals(new BlockState(76, 5)),
          "live north torch 76:4 drift");
      actor.look(90F, 0F);
      worldline.test.WorldlineSmokeAwait.observe(actor, 2);
      actor.selectHeldSlot(1);
      actor.useHeldItemOnBlock(top, BlockFace.UP);
      require(actor.awaitBlock(repeater, new BlockState(93, 3))
                  .blockAt(repeater.x(), repeater.y(), repeater.z())
                  .equals(new BlockState(93, 3)),
          "west repeater drift");
      actor.selectHeldSlot(2);
      actor.placeHeldBlock(east, BlockFace.UP);
      require(worldline.test.WorldlineSmokeAwait.observe(actor, 5)
                  .blockAt(lever.x(), lever.y(), lever.z())
                  .legacyId()
              == 69,
          "input lever drift");
      actor.selectHeldSlot(4);
      worldline.test.WorldlineSmokeAwait.observe(actor, 5);
      actor.activateBlock(lever, BlockFace.UP);
      off = new BlockState(75, 4);
      require(actor.awaitBlock(torch, off).blockAt(torch.x(), torch.y(), torch.z()).equals(off),
          "live invert 75:4 drift");
      actor.close();
      awaitPlayers(server, 0);
      server.save();
      reader = new B173WireClient("127.0.0.1", port, user, timeout);
      reader.connect();
      reader.synchronizePose();
      RemoteChunkSnapshot after = reader.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      BlockState placed = after.blockAt(local(torch.x(), cx), torch.y(), local(torch.z(), cz));
      require(after.blockAt(local(top.x(), cx), top.y(), local(top.z(), cz))
                  .equals(new BlockState(1, 0))
              && after.blockAt(local(body.x(), cx), body.y(), local(body.z(), cz))
                  .equals(new BlockState(1, 0))
              && placed.equals(off) && placed.legacyId() == 75 && placed.metadata() == 4
              && !placed.equals(on) && !placed.equals(new BlockState(76, 5)),
          "persisted inverted redstone torch drift");
      String evidence = "column=" + column + ",support=" + top.x() + ":" + top.y() + ":" + top.z()
          + ":1:0,torch=" + torch.x() + ":" + torch.y() + ":" + torch.z()
          + ":76:4->75:4,persisted=true,clients=2,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=raised-stone+inverter+torch76->75|cause=packet15-item76-then-powered-block|wire=packet53-torch76:4->torch75:4|oracle=live-on+live-off+fresh-login|"
          + evidence;
      System.out.println("WORLDLINE_M312_INVERT=" + evidence);
      System.out.println("WORLDLINE_M312_TRACE=" + trace);
      System.out.println("WORLDLINE_M312_SIGNATURE=" + sha(trace));
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
    throw new IllegalStateException("no deterministic torch invert foundation");
  }
  private static void require(boolean v, String m) {
    if (!v)
      throw new IllegalStateException(m);
  }
}
