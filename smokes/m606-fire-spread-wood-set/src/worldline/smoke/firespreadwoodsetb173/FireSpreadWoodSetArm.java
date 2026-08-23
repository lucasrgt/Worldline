package worldline.smoke.firespreadwoodsetb173;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import worldline.api.BlockFace;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.RemoteChunkSnapshot;
import worldline.api.RemoteWorldView;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173WireClient;
import worldline.test.WorldlineSmokeAwait;

/** Raised netherrack-plus-wood pad and bounded random-tick fire-spread wait. */
public final class FireSpreadWoodSetArm {
  private FireSpreadWoodSetArm() {}

  static BlockPosition raise(B173WireClient actor, RemoteChunkSnapshot initial, int cx, int cz, int[] column)
      throws Exception {
    BlockPosition top = foundation(initial, cx, cz);
    column[0] = 0;
    actor.selectHeldSlot(0);
    while (water(at(initial, BlockFace.UP.adjacent(top), cx, cz).legacyId())) {
      top = place(actor, top, BlockFace.UP, 1);
      actor.moveAndObserve(0D, 1D, 0D, 1);
      require(++column[0] <= 15, "water column exceeded fire-spread-wood fixture");
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

  static BlockPosition[] ring(B173WireClient actor, BlockPosition top) throws Exception {
    BlockPosition east = place(actor, top, BlockFace.EAST, 1);
    BlockPosition west = place(actor, top, BlockFace.WEST, 1);
    BlockPosition north = place(actor, top, BlockFace.NORTH, 1);
    BlockPosition south = place(actor, top, BlockFace.SOUTH, 1);
    return new BlockPosition[] {east, west, north, south, place(actor, east, BlockFace.NORTH, 1),
        place(actor, east, BlockFace.SOUTH, 1), place(actor, west, BlockFace.NORTH, 1),
        place(actor, west, BlockFace.SOUTH, 1)};
  }

  static BlockPosition cover(B173WireClient actor, BlockPosition[] ring) throws Exception {
    BlockPosition tower = place(actor, ring[0], BlockFace.EAST, 1);
    int lift = 0;
    while (lift < 5) {
      tower = place(actor, tower, BlockFace.UP, 1);
      lift++;
    }
    BlockPosition east = place(actor, tower, BlockFace.WEST, 1);
    BlockPosition center = place(actor, east, BlockFace.WEST, 1);
    BlockPosition west = place(actor, center, BlockFace.WEST, 1);
    place(actor, east, BlockFace.NORTH, 1);
    place(actor, east, BlockFace.SOUTH, 1);
    place(actor, center, BlockFace.NORTH, 1);
    place(actor, center, BlockFace.SOUTH, 1);
    place(actor, west, BlockFace.NORTH, 1);
    place(actor, west, BlockFace.SOUTH, 1);
    return center;
  }

  static BlockPosition[] fuels(B173WireClient actor, BlockPosition[] support) throws Exception {
    BlockPosition[] out = new BlockPosition[support.length];
    actor.selectHeldSlot(4);
    out[0] = place(actor, support[0], BlockFace.UP, 17);
    actor.selectHeldSlot(3);
    int index = 1;
    while (index < support.length) {
      out[index] = place(actor, support[index], BlockFace.UP, 5);
      index++;
    }
    return out;
  }

  static BlockPosition place(B173WireClient actor, BlockPosition support, BlockFace face, int id) throws Exception {
    BlockPosition target = face.adjacent(support);
    actor.placeHeldBlock(support, face);
    actor.awaitBlock(target, new BlockState(id, 0));
    return target;
  }

  static BlockPosition foundation(RemoteChunkSnapshot chunk, int cx, int cz) {
    int x = 4;
    while (x <= 11) {
      int z = 4;
      while (z <= 11) {
        int y = 126;
        while (y >= 1) {
          if (chunk.blockAt(x, y, z).legacyId() == 3 && water(chunk.blockAt(x, y + 1, z).legacyId())) {
            return new BlockPosition(cx * 16 + x, y, cz * 16 + z);
          }
          y--;
        }
        z++;
      }
      x++;
    }
    throw new IllegalStateException("no deterministic fire-spread-wood foundation");
  }

  static void waitSpread(B173WireClient actor, BlockPosition flame, BlockPosition[] fuels, int window, int windows)
      throws Exception {
    RemoteWorldView spread = WorldlineSmokeAwait.awaitWorld(
        actor, view -> id(view, flame) == 51 && anySpread(view, fuels), "wood plank fire spread", window * windows);
    require(id(spread, flame) == 51, "source fire 51 died during wait");
    int fuelIndex = 0;
    while (fuelIndex < fuels.length) {
      int value = id(spread, fuels[fuelIndex]);
      require(value == 5 || value == 17 || value == 0 || value == 51, "fuel cell drift during wait " + value);
      fuelIndex++;
    }
  }

  static void persist(RemoteChunkSnapshot after, int cx, int cz, BlockPosition rack, BlockPosition flame,
      BlockPosition cover, BlockPosition[] fuels) {
    require(at(after, rack, cx, cz).equals(new BlockState(87, 0)), "netherrack persist drift");
    require(at(after, flame, cx, cz).legacyId() == 51, "source fire persist drift");
    require(at(after, cover, cx, cz).equals(new BlockState(1, 0)), "cover persist drift");
    int fuelIndex = 0;
    while (fuelIndex < fuels.length) {
      int value = at(after, fuels[fuelIndex], cx, cz).legacyId();
      require(value == 5 || value == 17 || value == 0 || value == 51, "fuel persist drift " + value);
      fuelIndex++;
    }
  }

  static boolean anySpread(RemoteWorldView view, BlockPosition[] fuels) {
    int fuelIndex = 0;
    while (fuelIndex < fuels.length) {
      BlockPosition fuel = fuels[fuelIndex];
      if (id(view, new BlockPosition(fuel.x(), fuel.y() + 1, fuel.z())) == 51) {
        return true;
      }
      fuelIndex++;
    }
    return false;
  }

  static int id(RemoteWorldView view, BlockPosition position) {
    return view.blockAt(position.x(), position.y(), position.z()).legacyId();
  }

  static BlockState at(RemoteChunkSnapshot chunk, BlockPosition position, int cx, int cz) {
    return chunk.blockAt(position.x() - cx * 16, position.y(), position.z() - cz * 16);
  }

  static boolean water(int id) {
    return id == 8 || id == 9;
  }

  static void awaitPlayers(B173DedicatedServer server, int n) throws Exception {
    long end = System.currentTimeMillis() + 5000;
    while (System.currentTimeMillis() < end) {
      if (server.players().size() == n) {
        return;
      }
      Thread.sleep(100);
    }
    throw new IllegalStateException("player count drift");
  }

  static String cell(BlockPosition position) {
    return position.x() + ":" + position.y() + ":" + position.z();
  }

  static String token(BlockPosition position, int id, int meta) {
    return cell(position) + ":" + id + ":" + meta;
  }

  static String sha(String value) throws Exception {
    byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
    StringBuilder text = new StringBuilder();
    int index = 0;
    while (index < digest.length) {
      text.append(String.format("%02x", digest[index] & 255));
      index++;
    }
    return text.toString();
  }

  static void require(boolean value, String message) {
    if (!value) {
      throw new IllegalStateException(message);
    }
  }
}
