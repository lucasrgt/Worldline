package worldline.smoke.portalsearchradiussetb173;

import java.nio.file.Path;
import java.time.Duration;
import worldline.api.BlockFace;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.RemoteChunkSnapshot;
import worldline.api.RemoteWorldView;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173PlayerSeed;
import worldline.b173server.B173WireClient;

/** M382 4x5 obsidian 49 frame plus flint-and-steel 259 portal 90. */
final class PortalSearchRadiusKit {
  static final int SEARCH = 128;
  static final int CREATE = 16;

  private PortalSearchRadiusKit() {}

  static BlockPosition place(B173WireClient actor, BlockPosition support, BlockFace face, int id) throws Exception {
    BlockPosition target = face.adjacent(support);
    actor.placeHeldBlock(support, face);
    actor.awaitBlock(target, new BlockState(id, 0));
    return target;
  }

  static BlockPosition spawnFoundation(RemoteChunkSnapshot chunk, int chunkX, int chunkZ) {
    for (int x = 4; x <= 10; x++)
      for (int z = 4; z <= 11; z++)
        for (int y = 126; y >= 1; y--)
          if (chunk.blockAt(x, y, z).legacyId() == 3 && water(chunk.blockAt(x, y + 1, z).legacyId()))
            return new BlockPosition(chunkX * 16 + x, y, chunkZ * 16 + z);
    throw new IllegalStateException("no deterministic portal-search-radius spawn foundation");
  }

  static BlockPosition farFoundation(RemoteChunkSnapshot chunk, int chunkX, int chunkZ) {
    for (int x = 4; x <= 10; x++)
      for (int z = 4; z <= 11; z++)
        for (int y = 120; y >= 40; y--) {
          int id = chunk.blockAt(x, y, z).legacyId();
          if ((id == 1 || id == 2 || id == 3 || id == 12 || id == 13 || id == 24) && clear(chunk, x, y, z))
            return new BlockPosition(chunkX * 16 + x, y, chunkZ * 16 + z);
        }
    throw new IllegalStateException("no deterministic portal-search-radius far foundation");
  }

  private static boolean clear(RemoteChunkSnapshot chunk, int x, int y, int z) {
    int column = 0;
    while (column < 15 && y + 1 + column < 128 && water(chunk.blockAt(x, y + 1 + column, z).legacyId())) column++;
    int height = column + 6;
    if (y + height >= 128)
      return false;
    for (int dx = 0; dx < 4; dx++)
      for (int dy = 1; dy <= height; dy++) {
        int id = chunk.blockAt(x + dx, y + dy, z).legacyId();
        if (id != 0 && !water(id))
          return false;
      }
    return true;
  }

  static Raised raise(B173WireClient actor, RemoteChunkSnapshot chunk, int chunkX, int chunkZ, BlockPosition anchor)
      throws Exception {
    int column = 0;
    actor.selectHeldSlot(0);
    while (water(chunk.blockAt(local(anchor.x(), chunkX), anchor.y() + 1, local(anchor.z(), chunkZ)).legacyId())) {
      anchor = place(actor, anchor, BlockFace.UP, 1);
      actor.moveAndObserve(0D, 1D, 0D, 1);
      require(++column <= 15, "water column exceeded portal-search-radius fixture");
    }
    anchor = place(actor, anchor, BlockFace.UP, 1);
    actor.moveAndObserve(0D, 1D, 0D, 1);
    return new Raised(anchor, column + 1);
  }

  static Frame east(B173WireClient actor, BlockPosition support) throws Exception {
    actor.selectHeldSlot(1);
    BlockPosition bottom = place(actor, support, BlockFace.UP, 49);
    BlockPosition cursor = bottom;
    for (int index = 0; index < 3; index++) cursor = place(actor, cursor, BlockFace.EAST, 49);
    BlockPosition left = bottom, right = cursor;
    for (int index = 0; index < 4; index++) {
      left = place(actor, left, BlockFace.UP, 49);
      right = place(actor, right, BlockFace.UP, 49);
      actor.moveAndObserve(0D, 1D, 0D, 1);
    }
    cursor = left;
    for (int index = 0; index < 2; index++) cursor = place(actor, cursor, BlockFace.EAST, 49);
    return new Frame(bottom);
  }

  static void light(B173WireClient actor, Frame frame, int settle) throws Exception {
    actor.selectHeldSlot(2);
    actor.useHeldItemOnBlock(new BlockPosition(frame.bottom.x() + 1, frame.bottom.y(), frame.bottom.z()), BlockFace.UP);
    RemoteWorldView active = worldline.test.WorldlineSmokeAwait.observe(actor, settle);
    int portals = 0;
    for (int y = 1; y <= 3; y++)
      for (int x = 1; x <= 2; x++) {
        BlockState state = active.blockAt(frame.bottom.x() + x, frame.bottom.y() + y, frame.bottom.z());
        require(state.legacyId() == 90, "portal 90 absent at " + frame.bottom + " got " + state);
        portals++;
      }
    require(portals == 6, "portal interior drift");
  }

  static void seed(Path workspace, String user, double x, double y, double z, int dimension) {
    B173PlayerSeed.writeInventory(workspace, user, x, y, z, dimension, new int[] {0, 1, 2}, new int[] {1, 49, 259},
        new int[] {64, 14, 1}, new int[] {0, 0, 0});
  }

  static B173WireClient relog(B173WireClient actor, B173DedicatedServer server, Path workspace, String user,
      Duration timeout, double x, double y, double z, int dimension) throws Exception {
    actor.close();
    PortalSearchRadiusScan.awaitPlayers(server, 0);
    server.save();
    seed(workspace, user, x, y, z, dimension);
    B173WireClient next = new B173WireClient("127.0.0.1", server.state().port(), user, timeout);
    next.connect();
    return next;
  }

  static boolean water(int id) {
    return id == 8 || id == 9;
  }
  static int local(int value, int chunk) {
    return value - chunk * 16;
  }
  static void require(boolean condition, String message) {
    if (!condition)
      throw new IllegalStateException(message);
  }

  static final class Raised {
    final BlockPosition top;
    final int column;
    Raised(BlockPosition top, int column) {
      this.top = top;
      this.column = column;
    }
  }

  static final class Frame {
    final BlockPosition bottom;
    Frame(BlockPosition bottom) {
      this.bottom = bottom;
    }
    double enterX() {
      return bottom.x() + 1.5D;
    }
    double enterY() {
      return bottom.y() + 1D;
    }
    double enterZ() {
      return bottom.z() + 0.5D;
    }
    String source() {
      return bottom.x() + ":" + bottom.y() + ":" + bottom.z();
    }
  }
}
