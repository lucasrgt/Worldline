package worldline.smoke.torchsupportbreakb173;
import static worldline.b173server.B173FixtureSupport.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import worldline.api.BlockFace;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.RemoteChunkSnapshot;
import worldline.api.RemoteDroppedItem;
import worldline.api.RemoteItemStack;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173PlayerSeed;
import worldline.b173server.B173WireClient;
import worldline.b173server.OfficialServerBootstrap;
import worldline.testkit.TorchSupportBreakFixture;

/** Packet14-breaks the support of an official wall torch and freezes its air-drop lifecycle. */
public final class TorchSupportBreakSmoke {
  private static final RemoteItemStack TORCH_ITEM = new RemoteItemStack(50, 1, 0);
  private static final BlockState AIR = new BlockState(0, 0);
  private static final BlockState STONE = new BlockState(1, 0);
  private static final BlockState WALL_TORCH = new BlockState(50, 1);

  private TorchSupportBreakSmoke() {}

  public static void main(String[] arguments) throws Exception {
    if (arguments.length != 7)
      throw new IllegalArgumentException(
          "usage: TorchSupportBreakSmoke server.jar workspace port seed username chunkX chunkZ");
    Path jar = Paths.get(arguments[0]);
    Path workspace = Paths.get(arguments[1]);
    int port = Integer.parseInt(arguments[2]);
    long seed = Long.parseLong(arguments[3]);
    String user = arguments[4];
    int chunkX = Integer.parseInt(arguments[5]);
    int chunkZ = Integer.parseInt(arguments[6]);
    require(seed == 17320110707L && "TorchBrk763".equals(user) && user.length() <= 16,
        "torch-support-break identity drift");
    Duration timeout = Duration.ofSeconds(180);
    B173DedicatedServer server = OfficialServerBootstrap.start(
        jar, workspace, port, seed, timeout);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout);
    B173WireClient reader = null;
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1, 2},
          new int[] {1, 50, 257}, new int[] {48, 8, 1}, new int[] {0, 0, 0});
      actor.connect();
      actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() == 3, "torch-support-break inventory drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(chunkX, chunkZ).chunkAt(chunkX, chunkZ);
      BlockPosition top = foundation(initial, chunkX, chunkZ);
      int column = 0;
      actor.selectHeldSlot(0);
      while (water(above(initial, top, chunkX, chunkZ))) {
        top = place(actor, top, BlockFace.UP, STONE);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        require(++column <= 15, "water column exceeded torch-support-break fixture");
      }
      while (column < 17) {
        top = place(actor, top, BlockFace.UP, STONE);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        column++;
      }
      actor.selectHeldSlot(1);
      BlockPosition torch = place(actor, top, BlockFace.EAST, WALL_TORCH);
      actor.selectHeldSlot(2);
      actor.beginBreak(top);
      worldline.test.WorldlineSmokeAwait.observe(actor, 20);
      actor.finishBreak(top);
      actor.awaitBlock(top, AIR);
      BlockState popped = worldline.test.WorldlineSmokeAwait.awaitBlockMatching(
          actor, torch, AIR::equals, "wall-torch pop to air", 40);
      RemoteDroppedItem drop = actor.awaitDroppedItem(TORCH_ITEM);
      require(drop.item().equals(TORCH_ITEM), "torch-support-break Packet21 drift");
      actor.close();
      awaitPlayers(server, 0);
      server.save();
      reader = new B173WireClient("127.0.0.1", port, user, timeout);
      reader.connect();
      reader.synchronizePose();
      RemoteChunkSnapshot after = reader.awaitRemoteChunk(chunkX, chunkZ).chunkAt(chunkX, chunkZ);
      BlockState persisted = after.blockAt(local(torch.x(), chunkX), torch.y(), local(torch.z(), chunkZ));
      TorchSupportBreakFixture.observe(WALL_TORCH, STONE, popped, drop.item(), persisted);
      String cells = "column=" + column + ",support=" + cell(top) + ":1:0->0:0"
          + ",torch=" + cell(torch) + ":50:1->0:0"
          + ",drop=packet21-50x1,persisted=true,clients=2,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=raised-stone-column+east-wall-torch50:1"
          + "|cause=packet15-item50-east-face+packet14-ironpick257-support"
          + "|wire=packet53-50:1->air+packet21-50"
          + "|oracle=torch-support-break-not-floor-not-wash-not-burnout|" + cells;
      System.out.println("WORLDLINE_M763_SET=" + cells);
      System.out.println("WORLDLINE_M763_TRACE=" + trace);
      System.out.println("WORLDLINE_M763_SIGNATURE=" + sha(trace));
      System.out.flush();
    } finally {
      actor.close();
      if (reader != null)
        reader.close();
      server.close();
    }
  }

  private static BlockPosition foundation(RemoteChunkSnapshot chunk, int chunkX, int chunkZ) {
    int x = 4;
    while (x <= 11) {
      int z = 4;
      while (z <= 11) {
        int y = 126;
        while (y >= 1) {
          if (chunk.blockAt(x, y, z).legacyId() == 3 && water(chunk.blockAt(x, y + 1, z).legacyId()))
            return new BlockPosition(chunkX * 16 + x, y, chunkZ * 16 + z);
          y--;
        }
        z++;
      }
      x++;
    }
    throw new IllegalStateException("no deterministic torch-support-break foundation");
  }

  private static int above(RemoteChunkSnapshot chunk, BlockPosition top, int chunkX, int chunkZ) {
    return chunk.blockAt(local(top.x(), chunkX), top.y() + 1, local(top.z(), chunkZ)).legacyId();
  }

  private static String cell(BlockPosition position) {
    return position.x() + ":" + position.y() + ":" + position.z();
  }

  private static void require(boolean value, String message) {
    if (!value)
      throw new IllegalStateException(message);
  }
}
