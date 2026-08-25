package worldline.smoke.tntchainb173;
import static worldline.b173server.B173FixtureSupport.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import worldline.api.BlockFace;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.RemoteChunkSnapshot;
import worldline.api.RemoteExplosion;
import worldline.api.RemoteObjectSpawn;
import worldline.api.RemoteWorldView;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173PlayerSeed;
import worldline.b173server.B173WireClient;
import worldline.testkit.TntChainFixture;

/** Qualifies one official TNT explosion priming an adjacent charge. */
public final class TntChainSmoke {
  private TntChainSmoke() {}
  public static void main(String[] args) throws Exception {
    if (args.length != 7)
      throw new IllegalArgumentException(
          "usage: TntChainSmoke server.jar workspace port seed username chunkX chunkZ");
    Path jar = Paths.get(args[0]), workspace = Paths.get(args[1]);
    int port = Integer.parseInt(args[2]);
    long seed = Long.parseLong(args[3]);
    String user = args[4];
    int cx = Integer.parseInt(args[5]), cz = Integer.parseInt(args[6]);
    Duration timeout = Duration.ofMinutes(6);
    B173DedicatedServer server =
        new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout), reader = null;
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1, 2},
          new int[] {1, 46, 259}, new int[] {64, 2, 1}, new int[] {0, 0, 0});
      actor.connect();
      actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() == 3, "TNT chain inventory drift");
      RemoteChunkSnapshot chunk = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      BlockPosition top = foundation(chunk, cx, cz);
      actor.selectHeldSlot(0);
      int column = 0;
      while (water(chunk.blockAt(local(top.x(), cx), top.y() + 1, local(top.z(), cz)).legacyId())) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        require(++column <= 15, "water column exceeded TNT chain fixture");
      }
      for (int lift = 0; lift < 6; lift++) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        column++;
      }
      BlockPosition east = place(actor, top, BlockFace.EAST, 1);
      actor.selectHeldSlot(1);
      BlockPosition first = place(actor, top, BlockFace.UP, 46);
      BlockPosition second = place(actor, east, BlockFace.UP, 46);
      actor.selectHeldSlot(2);
      actor.useHeldItemOnBlock(first, BlockFace.UP);
      RemoteObjectSpawn direct = actor.awaitObjectSpawn(50);
      actor.moveAndObserve(8D, 0D, 0D, 4);
      RemoteExplosion directExplosion = actor.awaitExplosion();
      RemoteObjectSpawn chained = actor.awaitObjectSpawn(50);
      RemoteExplosion chainedExplosion = actor.awaitExplosion();
      RemoteWorldView after = worldline.test.WorldlineSmokeAwait.observe(actor, 2);
      TntChainFixture.Evidence evidence =
          TntChainFixture.observe(first, second, direct, directExplosion, chained, chainedExplosion,
              after.blockAt(first.x(), first.y(), first.z()),
              after.blockAt(second.x(), second.y(), second.z()));
      actor.close();
      awaitPlayers(server, 0);
      server.save();
      reader = new B173WireClient("127.0.0.1", port, user, timeout);
      reader.connect();
      reader.synchronizePose();
      RemoteWorldView persisted = reader.awaitRemoteChunk(cx, cz);
      require(persisted.blockAt(first.x(), first.y(), first.z()).equals(new BlockState(0, 0))
              && persisted.blockAt(second.x(), second.y(), second.z()).equals(new BlockState(0, 0)),
          "TNT chain persistence drift");
      require(evidence.primedObjects() == 2, "TNT chain normalized evidence drift");
      String signal = "charges=2,adjacent=true,direct=packet23:50,chain=packet23:50"
          + ",explosions=2xstrength4,both-air=true,persisted=true,replicas=2,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|fixture=raised-adjacent-tnt46x2+flint259"
          + "|action=packet15-prime-first|observation=packet23-type50x2+packet60-strength4x2"
          + "|oracle=first-explosion-primes-second|" + signal;
      System.out.println("WORLDLINE_M637_SET=" + signal);
      System.out.println("WORLDLINE_M637_TRACE=" + trace);
      System.out.println("WORLDLINE_M637_SIGNATURE=" + sha(trace));
    } finally {
      actor.close();
      if (reader != null)
        reader.close();
      server.close();
    }
  }
  private static BlockPosition foundation(RemoteChunkSnapshot chunk, int cx, int cz) {
    for (int x = 4; x <= 10; x++)
      for (int z = 4; z <= 11; z++)
        for (int y = 126; y >= 1; y--)
          if (chunk.blockAt(x, y, z).legacyId() == 3
              && water(chunk.blockAt(x, y + 1, z).legacyId()))
            return new BlockPosition(cx * 16 + x, y, cz * 16 + z);
    throw new IllegalStateException("no deterministic TNT chain foundation");
  }
  private static void require(boolean value, String message) {
    if (!value)
      throw new IllegalStateException(message);
  }
}
