package worldline.smoke.torchwashsetb173;
import static worldline.b173server.B173FixtureSupport.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.BlockFace;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.PlayerPose;
import worldline.api.RemoteChunkSnapshot;
import worldline.api.RemoteDroppedItem;
import worldline.api.RemoteItemStack;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173PlayerSeed;
import worldline.b173server.B173WireClient;

/** Places still water beside a floor torch and proves flowing water pops Packet21 torch 50. */
public final class TorchWashSetSmoke {
  private static final RemoteItemStack TORCH_ITEM = new RemoteItemStack(50, 1, 0);
  private static final BlockState STONE = new BlockState(1, 0);
  private static final BlockState DIRT = new BlockState(3, 0);
  private static final BlockState AIR = new BlockState(0, 0);
  private static final BlockState FLOOR_TORCH = new BlockState(50, 5);
  private static final BlockState STILL_WATER = new BlockState(9, 0);

  private TorchWashSetSmoke() {}

  public static void main(String[] arguments) throws Exception {
    if (arguments.length != 7)
      throw new IllegalArgumentException(
          "usage: TorchWashSetSmoke server.jar workspace port seed username chunkX chunkZ");
    Path jar = Paths.get(arguments[0]);
    Path workspace = Paths.get(arguments[1]);
    int port = Integer.parseInt(arguments[2]);
    long seed = Long.parseLong(arguments[3]);
    String user = arguments[4];
    int chunkX = Integer.parseInt(arguments[5]);
    int chunkZ = Integer.parseInt(arguments[6]);
    require(seed == 17320110707L && "TorchWash599".equals(user) && user.length() <= 16, "torch-wash identity drift");
    Duration timeout = Duration.ofSeconds(180);
    B173DedicatedServer server = new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout);
    B173WireClient reader = null;
    BlockPosition top;
    BlockPosition west;
    BlockPosition east;
    BlockPosition torch;
    BlockPosition source;
    BlockPosition gate;
    int column;
    BlockState washed;
    RemoteDroppedItem drop;
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1, 2, 3}, new int[] {1, 50, 9, 3},
          new int[] {48, 1, 1, 1}, new int[] {0, 0, 0, 0});
      actor.connect();
      actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() == 4, "torch-wash inventory drift");
      actor.awaitRemoteChunk(-1, 0);
      actor.awaitRemoteChunk(1, 0);
      actor.awaitRemoteChunk(0, -1);
      actor.awaitRemoteChunk(0, 1);
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(chunkX, chunkZ).chunkAt(chunkX, chunkZ);
      top = foundation(initial, chunkX, chunkZ);
      column = 0;
      actor.selectHeldSlot(0);
      while (water(above(initial, top, chunkX, chunkZ))) {
        top = place(actor, top, BlockFace.UP, STONE);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        require(++column <= 15, "water column exceeded torch-wash fixture");
      }
      int lift = 0;
      while (lift < 8) {
        top = place(actor, top, BlockFace.UP, STONE);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        column++;
        lift++;
      }
      west = place(actor, top, BlockFace.WEST, STONE);
      east = place(actor, top, BlockFace.EAST, STONE);
      BlockPosition north = place(actor, top, BlockFace.NORTH, STONE);
      BlockPosition south = place(actor, top, BlockFace.SOUTH, STONE);
      place(actor, west, BlockFace.NORTH, STONE);
      place(actor, west, BlockFace.SOUTH, STONE);
      place(actor, east, BlockFace.NORTH, STONE);
      place(actor, east, BlockFace.SOUTH, STONE);
      place(actor, north, BlockFace.UP, STONE);
      place(actor, south, BlockFace.UP, STONE);
      actor.selectHeldSlot(1);
      torch = place(actor, west, BlockFace.UP, FLOOR_TORCH);
      actor.selectHeldSlot(3);
      gate = place(actor, east, BlockFace.UP, DIRT);
      actor.selectHeldSlot(2);
      source = place(actor, top, BlockFace.UP, STILL_WATER);
      PlayerPose pose = actor.moveAndObserve(0D, 0D, 0D, 1).resulting();
      actor.moveAndObserve(top.x() + 0.5D - pose.x(), top.y() + 1.1D - pose.y(), top.z() + 0.5D - pose.z(), 8);
      worldline.test.WorldlineSmokeAwait.observe(actor, 20);
      actor.beginBreak(gate);
      worldline.test.WorldlineSmokeAwait.observe(actor, 20);
      actor.finishBreak(gate);
      actor.awaitBlock(gate, AIR);
      washed = worldline.test.WorldlineSmokeAwait.awaitBlockMatching(
          actor, torch, state -> water(state.legacyId()), "washed torch water", 40);
      drop = actor.awaitDroppedItem(TORCH_ITEM);
      require(water(washed.legacyId()) && washed.legacyId() != 50 && drop.item().equals(TORCH_ITEM)
              && drop.item().count() == 1,
          "torch-wash Packet21 absent");
      actor.close();
      awaitPlayers(server, 0);
      server.save();
      reader = new B173WireClient("127.0.0.1", port, user, timeout);
      reader.connect();
      reader.synchronizePose();
      RemoteChunkSnapshot after = reader.awaitRemoteChunk(chunkX, chunkZ).chunkAt(chunkX, chunkZ);
      BlockState persisted = after.blockAt(local(torch.x(), chunkX), torch.y(), local(torch.z(), chunkZ));
      require(water(persisted.legacyId()) && persisted.legacyId() != 50, "persisted washed torch drift");
      String evidence = "column=" + column + ",platform=3x3,support=" + cell(top, 1, 0) + ",torch=" + cell(torch, 50, 5)
          + "->" + washed.legacyId() + ":" + washed.metadata() + ",source=" + cell(source, 9, 0)
          + ",gate=" + cell(gate, 3, 0) + "->0:0"
          + ",drop=packet21-50x1,persisted=true,clients=2,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=raised-3x3-stone+torch50:5+adjacent-water9+dirt-gate"
          + "|cause=packet15-item9+packet15-item50+packet14-open-gate-flow"
          + "|wire=packet53-torch50:5->water+packet21-50"
          + "|oracle=torch-wash-not-place-not-faces|" + evidence;
      System.out.println("WORLDLINE_M599_WASH=" + evidence);
      System.out.println("WORLDLINE_M599_TRACE=" + trace);
      System.out.println("WORLDLINE_M599_SIGNATURE=" + sha(trace));
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
          if (chunk.blockAt(x, y, z).legacyId() == 3 && water(chunk.blockAt(x, y + 1, z).legacyId())) {
            return new BlockPosition(chunkX * 16 + x, y, chunkZ * 16 + z);
          }
          y--;
        }
        z++;
      }
      x++;
    }
    throw new IllegalStateException("no deterministic torch-wash foundation");
  }

  private static int above(RemoteChunkSnapshot chunk, BlockPosition top, int chunkX, int chunkZ) {
    return chunk.blockAt(local(top.x(), chunkX), top.y() + 1, local(top.z(), chunkZ)).legacyId();
  }

  private static String cell(BlockPosition position, int id, int metadata) {
    return position.x() + ":" + position.y() + ":" + position.z() + ":" + id + ":" + metadata;
  }

  private static void require(boolean value, String message) {
    if (!value)
      throw new IllegalStateException(message);
  }
}
