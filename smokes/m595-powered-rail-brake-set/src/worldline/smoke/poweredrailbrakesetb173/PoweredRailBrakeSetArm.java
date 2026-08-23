package worldline.smoke.poweredrailbrakesetb173;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import worldline.api.BlockFace;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.RemoteChunkSnapshot;
import worldline.api.RemoteWorldView;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173WireClient;
import worldline.test.WorldlineAwait;

/** Raised north-south powered-rail run with a beyond detector used as the pass sensor. */
public final class PoweredRailBrakeSetArm {
  final BlockPosition support;
  final BlockPosition wall;
  final BlockPosition bumper;
  final BlockPosition launch;
  final BlockPosition mid;
  final BlockPosition beyond;
  final BlockPosition torch;

  private PoweredRailBrakeSetArm(BlockPosition support, BlockPosition wall, BlockPosition bumper, BlockPosition launch,
      BlockPosition mid, BlockPosition beyond, BlockPosition torch) {
    this.support = support;
    this.wall = wall;
    this.bumper = bumper;
    this.launch = launch;
    this.mid = mid;
    this.beyond = beyond;
    this.torch = torch;
  }

  static BlockPosition raise(B173WireClient actor, RemoteChunkSnapshot initial, int chunkX, int chunkZ, int[] column)
      throws Exception {
    BlockPosition top = foundation(initial, chunkX, chunkZ);
    column[0] = 0;
    actor.selectHeldSlot(0);
    while (water(at(initial, BlockFace.UP.adjacent(top), chunkX, chunkZ).legacyId())) {
      top = place(actor, top, BlockFace.UP, 1);
      actor.moveAndObserve(0D, 1D, 0D, 1);
      require(++column[0] <= 15, "water column exceeded powered-rail-brake fixture");
    }
    int lift = 0;
    while (lift < 8) {
      top = place(actor, top, BlockFace.UP, 1);
      actor.moveAndObserve(0D, 1D, 0D, 1);
      column[0]++;
      lift++;
    }
    return top;
  }

  static PoweredRailBrakeSetArm place(B173WireClient actor, BlockPosition top) throws Exception {
    BlockPosition northPad = place(actor, top, BlockFace.NORTH, 1);
    BlockPosition wall = place(actor, northPad, BlockFace.UP, 1);
    BlockPosition midPad = place(actor, top, BlockFace.SOUTH, 1);
    BlockPosition beyondPad = place(actor, midPad, BlockFace.SOUTH, 1);
    BlockPosition bumperPad = place(actor, beyondPad, BlockFace.SOUTH, 1);
    BlockPosition bumper = place(actor, bumperPad, BlockFace.UP, 1);
    BlockPosition eastPad = place(actor, top, BlockFace.EAST, 1);
    actor.selectHeldSlot(1);
    BlockPosition launch = BlockFace.UP.adjacent(top);
    actor.placeHeldBlock(top, BlockFace.UP);
    actor.awaitBlock(launch, new BlockState(27, 0));
    BlockPosition mid = BlockFace.UP.adjacent(midPad);
    actor.placeHeldBlock(midPad, BlockFace.UP);
    actor.awaitBlock(mid, new BlockState(27, 0));
    actor.selectHeldSlot(2);
    BlockPosition beyond = BlockFace.UP.adjacent(beyondPad);
    actor.placeHeldBlock(beyondPad, BlockFace.UP);
    actor.awaitBlock(beyond, new BlockState(28, 0));
    require(launch.x() == mid.x() && mid.x() == beyond.x() && mid.z() == launch.z() + 1 && beyond.z() == launch.z() + 2
            && wall.z() == launch.z() - 1 && bumper.z() == beyond.z() + 1,
        "north-south powered-rail-brake track drift");
    return new PoweredRailBrakeSetArm(top, wall, bumper, launch, mid, beyond, BlockFace.UP.adjacent(eastPad));
  }

  void persist(RemoteChunkSnapshot after, int chunkX, int chunkZ) {
    require(at(after, launch, chunkX, chunkZ).equals(new BlockState(27, 0))
            && (at(after, launch, chunkX, chunkZ).metadata() & 8) == 0
            && at(after, mid, chunkX, chunkZ).equals(new BlockState(27, 0))
            && (at(after, mid, chunkX, chunkZ).metadata() & 8) == 0
            && at(after, beyond, chunkX, chunkZ).equals(new BlockState(28, 0))
            && (at(after, beyond, chunkX, chunkZ).metadata() & 8) == 0
            && at(after, torch, chunkX, chunkZ).equals(new BlockState(0, 0))
            && at(after, wall, chunkX, chunkZ).equals(new BlockState(1, 0))
            && at(after, bumper, chunkX, chunkZ).equals(new BlockState(1, 0)),
        "persisted powered-rail-brake drift");
  }

  static BlockPosition place(B173WireClient actor, BlockPosition support, BlockFace face, int id) throws Exception {
    BlockPosition target = face.adjacent(support);
    actor.placeHeldBlock(support, face);
    actor.awaitBlock(target, new BlockState(id, 0));
    return target;
  }

  static BlockPosition foundation(RemoteChunkSnapshot chunk, int chunkX, int chunkZ) {
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
    throw new IllegalStateException("no deterministic powered-rail-brake foundation");
  }

  static BlockState at(RemoteChunkSnapshot chunk, BlockPosition position, int chunkX, int chunkZ) {
    return chunk.blockAt(position.x() - chunkX * 16, position.y(), position.z() - chunkZ * 16);
  }

  static boolean unpowered(RemoteWorldView world, BlockPosition rail) {
    BlockState state = world.blockAt(rail.x(), rail.y(), rail.z());
    return state.legacyId() == 27 && (state.metadata() & 8) == 0;
  }

  static boolean powered(RemoteWorldView world, BlockPosition rail) {
    BlockState state = world.blockAt(rail.x(), rail.y(), rail.z());
    return state.legacyId() == 27 && (state.metadata() & 8) != 0;
  }

  static boolean idleDetector(RemoteWorldView world, BlockPosition detector) {
    BlockState state = world.blockAt(detector.x(), detector.y(), detector.z());
    return state.equals(new BlockState(28, 0)) && (state.metadata() & 8) == 0;
  }

  static boolean water(int id) {
    return id == 8 || id == 9;
  }

  static String cell(BlockPosition position) {
    return position.x() + ":" + position.y() + ":" + position.z();
  }

  static void awaitPlayers(B173DedicatedServer server, int count) {
    new WorldlineAwait(40).awaitEntity(
        server::players, (List<String> players) -> players.size() == count, "player count");
  }

  static String sha(String value) throws Exception {
    byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
    StringBuilder hex = new StringBuilder();
    int index = 0;
    while (index < digest.length) {
      hex.append(String.format("%02x", digest[index] & 255));
      index++;
    }
    return hex.toString();
  }

  static void require(boolean value, String message) {
    if (!value) {
      throw new IllegalStateException(message);
    }
  }
}
