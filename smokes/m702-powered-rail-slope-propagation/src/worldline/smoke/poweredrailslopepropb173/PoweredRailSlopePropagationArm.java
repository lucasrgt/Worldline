package worldline.smoke.poweredrailslopepropb173;

import static worldline.b173server.B173FixtureSupport.place;
import static worldline.b173server.B173FixtureSupport.water;

import worldline.api.BlockFace;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.RemoteChunkSnapshot;
import worldline.api.RemoteWorldView;
import worldline.b173server.B173WireClient;

/** Raised powered-rail run crossing one north slope: torch power must cross it both ways. */
public final class PoweredRailSlopePropagationArm {
  final BlockPosition support, high, slope, topRail, lowRail, farRail, torch;
  final int idleSlope, idleTop, idleLow, idleFar;

  private PoweredRailSlopePropagationArm(BlockPosition support, BlockPosition high, BlockPosition slope,
      BlockPosition topRail, BlockPosition lowRail, BlockPosition farRail, BlockPosition torch,
      int idleSlope, int idleTop, int idleLow, int idleFar) {
    this.support = support;
    this.high = high;
    this.slope = slope;
    this.topRail = topRail;
    this.lowRail = lowRail;
    this.farRail = farRail;
    this.torch = torch;
    this.idleSlope = idleSlope;
    this.idleTop = idleTop;
    this.idleLow = idleLow;
    this.idleFar = idleFar;
  }

  static PoweredRailSlopePropagationArm build(B173WireClient actor, RemoteChunkSnapshot initial,
      int cx, int cz, int[] column) throws Exception {
    BlockPosition top = raise(actor, initial, cx, cz, column);
    BlockPosition north = place(actor, top, BlockFace.NORTH, 1);
    BlockPosition high = place(actor, north, BlockFace.UP, 1);
    BlockPosition south = place(actor, top, BlockFace.SOUTH, 1);
    BlockPosition south2 = place(actor, south, BlockFace.SOUTH, 1);
    BlockPosition east = place(actor, top, BlockFace.EAST, 1);
    place(actor, east, BlockFace.EAST, 1);
    actor.selectHeldSlot(1);
    BlockPosition lowRail = railOnFloor(actor, south);
    BlockPosition farRail = railOnFloor(actor, south2);
    BlockPosition topRail = railOnFloor(actor, high);
    BlockPosition slope = BlockFace.UP.adjacent(top);
    actor.placeHeldBlock(top, BlockFace.UP);
    BlockState idleSlope = worldline.test.WorldlineSmokeAwait.awaitBlockMatching(
        actor, slope, state -> state.legacyId() == 27 && sloped(state.metadata()), "sloped powered rail", 20);
    RemoteWorldView idle = worldline.test.WorldlineSmokeAwait.observe(actor, 2);
    require(unpowered(idle, slope) && unpowered(idle, lowRail) && unpowered(idle, farRail)
            && unpowered(idle, topRail) && sloped(idle.blockAt(slope.x(), slope.y(), slope.z()).metadata()),
        "idle rails drifted before the torch");
    require(topRail.y() == slope.y() + 1 && topRail.z() == slope.z() - 1 && lowRail.z() == slope.z() + 1
            && farRail.z() == slope.z() + 2 && high.z() == slope.z() - 1,
        "north-south slope-boundary track drift");
    return new PoweredRailSlopePropagationArm(top, high, slope, topRail, lowRail, farRail,
        BlockFace.UP.adjacent(east), idleSlope.metadata(),
        meta(idle, topRail), meta(idle, lowRail), meta(idle, farRail));
  }

  static boolean powered(RemoteWorldView world, BlockPosition rail) {
    BlockState state = world.blockAt(rail.x(), rail.y(), rail.z());
    return state.legacyId() == 27 && (state.metadata() & 8) != 0;
  }

  static boolean unpoweredShape(RemoteWorldView world, BlockPosition rail, int shape) {
    BlockState state = world.blockAt(rail.x(), rail.y(), rail.z());
    return state.legacyId() == 27 && (state.metadata() & 8) == 0 && (state.metadata() & 7) == shape;
  }

  void persist(RemoteChunkSnapshot after, int cx, int cz) {
    require(at(after, support, cx, cz).equals(new BlockState(1, 0)), "support persist drift");
    require(at(after, high, cx, cz).equals(new BlockState(1, 0)), "high persist drift");
    require(railPersist(after, slope, idleSlope) && railPersist(after, topRail, idleTop)
            && railPersist(after, lowRail, idleLow) && railPersist(after, farRail, idleFar),
        "persisted rail shape drift");
    require(at(after, torch, cx, cz).equals(new BlockState(0, 0)), "torch persist drift");
  }

  static String cell(BlockPosition position) {
    return position.x() + ":" + position.y() + ":" + position.z();
  }

  static void require(boolean value, String message) {
    if (!value) throw new IllegalStateException(message);
  }

  private static boolean railPersist(RemoteChunkSnapshot after, BlockPosition rail, int shape) {
    BlockState state = after.blockAt(rail.x(), rail.y(), rail.z());
    return state.legacyId() == 27 && (state.metadata() & 7) == shape && (state.metadata() & 8) == 0;
  }

  private static int meta(RemoteWorldView world, BlockPosition position) {
    return world.blockAt(position.x(), position.y(), position.z()).metadata();
  }

  private static boolean unpowered(RemoteWorldView world, BlockPosition rail) {
    return (world.blockAt(rail.x(), rail.y(), rail.z()).metadata() & 8) == 0;
  }

  private static boolean sloped(int metadata) {
    int shape = metadata & 7;
    return shape >= 2 && shape <= 5;
  }

  private static BlockPosition raise(B173WireClient actor, RemoteChunkSnapshot initial, int cx, int cz,
      int[] column) throws Exception {
    BlockPosition top = foundation(initial, cx, cz);
    column[0] = 0;
    actor.selectHeldSlot(0);
    while (water(at(initial, BlockFace.UP.adjacent(top), cx, cz).legacyId())) {
      top = place(actor, top, BlockFace.UP, 1);
      actor.moveAndObserve(0D, 1D, 0D, 1);
      require(++column[0] <= 15, "water column exceeded slope propagation fixture");
    }
    for (int lift = 0; lift < 8; lift++) {
      top = place(actor, top, BlockFace.UP, 1);
      actor.moveAndObserve(0D, 1D, 0D, 1);
      column[0]++;
    }
    return top;
  }

  private static BlockPosition railOnFloor(B173WireClient actor, BlockPosition supportBlock) throws Exception {
    BlockPosition target = BlockFace.UP.adjacent(supportBlock);
    actor.placeHeldBlock(supportBlock, BlockFace.UP);
    worldline.test.WorldlineSmokeAwait.awaitBlockMatching(
        actor, target, state -> state.legacyId() == 27, "powered rail 27", 10);
    return target;
  }

  private static BlockPosition foundation(RemoteChunkSnapshot chunk, int cx, int cz) {
    for (int x = 4; x <= 11; x++)
      for (int z = 4; z <= 11; z++)
        for (int y = 126; y >= 1; y--)
          if (chunk.blockAt(x, y, z).legacyId() == 3 && water(chunk.blockAt(x, y + 1, z).legacyId()))
            return new BlockPosition(cx * 16 + x, y, cz * 16 + z);
    throw new IllegalStateException("no deterministic slope propagation foundation");
  }

  private static BlockState at(RemoteChunkSnapshot chunk, BlockPosition position, int cx, int cz) {
    return chunk.blockAt(position.x() - cx * 16, position.y(), position.z() - cz * 16);
  }

}
