package worldline.smoke.bonemealwheatb173;
import static worldline.b173server.B173FixtureSupport.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import worldline.api.BlockFace;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.RemoteChunkSnapshot;
import worldline.api.RemoteItemStack;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173PlayerSeed;
import worldline.b173server.B173WireClient;
import worldline.testkit.BonemealWheatFixture;

/** Qualifies Packet15 bonemeal maturing planted Beta wheat. */
public final class BonemealWheatSmoke {
  private static final RemoteItemStack MEAL = new RemoteItemStack(351, 1, 15);
  private BonemealWheatSmoke() {}
  public static void main(String[] args) throws Exception {
    if (args.length != 7)
      throw new IllegalArgumentException(
          "usage: BonemealWheatSmoke server.jar workspace port seed username chunkX chunkZ");
    Path jar = Paths.get(args[0]), workspace = Paths.get(args[1]);
    int port = Integer.parseInt(args[2]);
    long seed = Long.parseLong(args[3]);
    String user = args[4];
    int cx = Integer.parseInt(args[5]), cz = Integer.parseInt(args[6]);
    Duration timeout = Duration.ofMinutes(8);
    B173DedicatedServer server =
        new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout), reader = null;
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1, 2, 3, 4},
          new int[] {1, 3, 290, 295, 351}, new int[] {64, 8, 1, 8, 2}, new int[] {0, 0, 0, 0, 15});
      actor.connect();
      actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() == 5, "wheat inventory drift");
      RemoteChunkSnapshot chunk = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      BlockPosition top = foundation(chunk, cx, cz);
      int column = 0;
      actor.selectHeldSlot(0);
      while (water(chunk.blockAt(local(top.x(), cx), top.y() + 1, local(top.z(), cz)).legacyId())) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        require(++column <= 15, "water column exceeded wheat fixture");
      }
      for (int lift = 0; lift < 8; lift++) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        column++;
      }
      actor.selectHeldSlot(1);
      BlockPosition dirt = place(actor, top, BlockFace.UP, 3);
      actor.selectHeldSlot(2);
      actor.useHeldItemOnBlock(dirt, BlockFace.UP);
      actor.awaitBlock(dirt, new BlockState(60, 0));
      BlockPosition wheat = BlockFace.UP.adjacent(dirt);
      actor.selectHeldSlot(3);
      actor.useHeldItemOnBlock(dirt, BlockFace.UP);
      actor.awaitBlock(wheat, new BlockState(59, 0));
      BlockState before = observe(actor, wheat);
      actor.selectHeldSlot(4);
      actor.useHeldItemOnBlock(wheat, BlockFace.UP);
      actor.awaitBlock(wheat, new BlockState(59, 7));
      BlockState after = observe(actor, wheat);
      actor.close();
      awaitPlayers(server, 0);
      server.save();
      reader = new B173WireClient("127.0.0.1", port, user, timeout);
      reader.connect();
      reader.synchronizePose();
      reader.awaitRemoteChunk(cx, cz);
      BlockState persisted = observe(reader, wheat);
      BonemealWheatFixture.Evidence evidence =
          BonemealWheatFixture.observe(before, MEAL, after, persisted);
      require(evidence.after().equals(evidence.persisted()), "wheat evidence persistence drift");
      String signal = "wheat=59:0->59:7,farmland=60:0,bonemeal=351:15,packet=15"
          + ",persisted=true,replicas=2,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|fixture=raised-farmland60+wheat59:0"
          + "|action=packet15-bonemeal351:15|observation=packet53-wheat59:7+fresh-login"
          + "|oracle=bonemeal-wheat-maturity|" + signal;
      System.out.println("WORLDLINE_M636_SET=" + signal);
      System.out.println("WORLDLINE_M636_TRACE=" + trace);
      System.out.println("WORLDLINE_M636_SIGNATURE=" + sha(trace));
    } finally {
      actor.close();
      if (reader != null)
        reader.close();
      server.close();
    }
  }
  private static BlockPosition foundation(RemoteChunkSnapshot chunk, int cx, int cz) {
    for (int x = 4; x <= 11; x++)
      for (int z = 4; z <= 11; z++)
        for (int y = 126; y >= 1; y--)
          if (chunk.blockAt(x, y, z).legacyId() == 3
              && water(chunk.blockAt(x, y + 1, z).legacyId()))
            return new BlockPosition(cx * 16 + x, y, cz * 16 + z);
    throw new IllegalStateException("no deterministic wheat foundation");
  }
  private static BlockState observe(B173WireClient client, BlockPosition position) {
    return worldline.test.WorldlineSmokeAwait.observe(client, 5).blockAt(
        position.x(), position.y(), position.z());
  }
  private static void require(boolean value, String message) {
    if (!value)
      throw new IllegalStateException(message);
  }
}
