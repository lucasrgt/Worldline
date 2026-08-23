package worldline.smoke.remainingdetectorrailb173;
import static worldline.b173server.B173FixtureSupport.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Places detector rail 28:0 then occupies it with minecart 328 as Packet23 type 10 / 28:8. */
public final class RemainingDetectorRailSmoke {
  private RemainingDetectorRailSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 7)
      throw new IllegalArgumentException(
          "usage: RemainingDetectorRailSmoke server.jar workspace port seed username chunkX chunkZ");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    int cx = Integer.parseInt(a[5]), cz = Integer.parseInt(a[6]);
    require(seed == 17320110707L && user.equals("DetRail402") && user.length() <= 16,
        "remaining-detector-rail identity drift");
    Duration timeout = Duration.ofSeconds(90);
    B173DedicatedServer server =
        new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout), reader = null;
    BlockPosition top, detector;
    int column;
    BlockState idle, live;
    RemoteObjectSpawn cart;
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1, 2},
          new int[] {1, 28, 328}, new int[] {32, 1, 1}, new int[] {0, 0, 0});
      actor.connect();
      actor.synchronizePose();
      require(
          actor.awaitInventory().occupiedSlots() == 3, "remaining-detector-rail inventory drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      top = foundation(initial, cx, cz);
      column = 0;
      actor.selectHeldSlot(0);
      while (
          water(initial.blockAt(local(top.x(), cx), top.y() + 1, local(top.z(), cz)).legacyId())) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        require(++column <= 15, "water column exceeded remaining-detector-rail fixture");
      }
      for (int lift = 0; lift < 8; lift++) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        column++;
      }
      actor.selectHeldSlot(1);
      detector = BlockFace.UP.adjacent(top);
      actor.placeHeldBlock(top, BlockFace.UP);
      idle = new BlockState(28, 0);
      actor.awaitBlock(detector, idle);
      require(idle.legacyId() == 28 && idle.legacyId() != 27 && idle.legacyId() != 66
              && (idle.metadata() & 8) == 0
              && actor.sustainTicks(5)
                  .blockAt(detector.x(), detector.y(), detector.z())
                  .equals(idle),
          "live unpowered detector rail drift");
      actor.selectHeldSlot(2);
      actor.useHeldItemOnBlock(detector, BlockFace.UP);
      cart = actor.awaitObjectSpawn(10);
      live = new BlockState(28, 8);
      require(cart.type() == 10 && cart.type() != 1 && cart.type() != 11 && cart.type() != 12
              && cart.throwerId() == 0 && cart.velocityX() == 0 && cart.velocityY() == 0
              && cart.velocityZ() == 0 && cart.fixedX() == detector.x() * 32 + 16
              && cart.fixedY() == detector.y() * 32 + 27 && cart.fixedZ() == detector.z() * 32 + 16,
          "minecart packet23 type10 spawn bounds drift");
      require(actor.awaitBlock(detector, live)
                  .blockAt(detector.x(), detector.y(), detector.z())
                  .equals(live)
              && (live.metadata() & 8) != 0 && !live.equals(idle)
              && actor.sustainTicks(5)
                  .blockAt(detector.x(), detector.y(), detector.z())
                  .equals(live),
          "live occupied detector rail drift");
      actor.close();
      awaitPlayers(server, 0);
      server.save();
      reader = new B173WireClient("127.0.0.1", port, user, timeout);
      reader.connect();
      reader.synchronizePose();
      reader.awaitBlock(detector, live);
      RemoteChunkSnapshot after = reader.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      require(after.blockAt(local(top.x(), cx), top.y(), local(top.z(), cz))
                  .equals(new BlockState(1, 0))
              && after.blockAt(local(detector.x(), cx), detector.y(), local(detector.z(), cz))
                  .equals(live)
              && (after.blockAt(local(detector.x(), cx), detector.y(), local(detector.z(), cz))
                         .metadata()
                     & 8)
                  != 0,
          "persisted remaining-detector-rail drift");
      String evidence = "column=" + column + ",support=" + top.x() + ":" + top.y() + ":" + top.z()
          + ":1:0,detector=" + detector.x() + ":" + detector.y() + ":" + detector.z()
          + ":28:" + idle.metadata() + "->" + live.metadata() + ",cart=type10+thrower0+fixed"
          + cart.fixedX() + ":" + cart.fixedY() + ":" + cart.fixedZ()
          + ",persisted=true,clients=2,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=raised-stone+detector28+minecart328|cause=packet15-item28+packet15-minecart328|wire=packet23-type10+thrower0+packet53-detector28:0->8|oracle=unpowered-then-occupied-detector+fresh-login|"
          + evidence;
      System.out.println("WORLDLINE_M402_SET=" + evidence);
      System.out.println("WORLDLINE_M402_TRACE=" + trace);
      System.out.println("WORLDLINE_M402_SIGNATURE=" + sha(trace));
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
    throw new IllegalStateException("no deterministic remaining-detector-rail foundation");
  }
  private static void require(boolean v, String m) {
    if (!v)
      throw new IllegalStateException(m);
  }
}
