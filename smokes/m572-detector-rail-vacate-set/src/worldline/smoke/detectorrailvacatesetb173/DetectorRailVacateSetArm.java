package worldline.smoke.detectorrailvacatesetb173;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import worldline.api.BlockFace;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.RemoteChunkSnapshot;
import worldline.api.RemoteWorldView;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173WireClient;

/** Raised sloped detector 28 with a lower landing rail so a cart can occupy then leave. */
public final class DetectorRailVacateSetArm {
  final BlockPosition support, high, detector, landing, landing2;

  private DetectorRailVacateSetArm(BlockPosition support, BlockPosition high, BlockPosition detector,
      BlockPosition landing, BlockPosition landing2) {
    this.support = support;
    this.high = high;
    this.detector = detector;
    this.landing = landing;
    this.landing2 = landing2;
  }

  static DetectorRailVacateSetArm place(B173WireClient actor, RemoteChunkSnapshot initial, int cx, int cz, int[] column)
      throws Exception {
    BlockPosition top = raise(actor, initial, cx, cz, column);
    BlockPosition north = place(actor, top, BlockFace.NORTH, 1);
    BlockPosition high = place(actor, north, BlockFace.UP, 1);
    BlockPosition south = place(actor, top, BlockFace.SOUTH, 1);
    BlockPosition south2 = place(actor, south, BlockFace.SOUTH, 1);
    BlockPosition east = place(actor, top, BlockFace.EAST, 1);
    place(actor, east, BlockFace.EAST, 1);
    actor.selectHeldSlot(2);
    placeId(actor, high, BlockFace.UP, 66);
    BlockPosition landing = placeId(actor, south, BlockFace.UP, 66);
    BlockPosition landing2 = placeId(actor, south2, BlockFace.UP, 66);
    actor.selectHeldSlot(1);
    BlockPosition detector = BlockFace.UP.adjacent(top);
    actor.placeHeldBlock(top, BlockFace.UP);
    BlockState idle = worldline.test.WorldlineSmokeAwait.awaitBlockMatching(
        actor, detector, state -> state.legacyId() == 28 && slope(state.metadata()), "sloped detector 28", 20);
    require(idle.legacyId() == 28 && slope(idle.metadata()) && (idle.metadata() & 8) == 0,
        "idle sloped detector drift " + idle + " at " + cell(detector));
    require(id(actor, landing) == 66 && id(actor, landing2) == 66, "landing rail 66 absent before occupy");
    actor.moveAndObserve(2D, 0D, 0D, 4);
    return new DetectorRailVacateSetArm(top, high, detector, landing, landing2);
  }

  BlockState occupy(B173WireClient actor) {
    return worldline.test.WorldlineSmokeAwait.awaitBlockMatching(
        actor, detector, state -> state.legacyId() == 28 && (state.metadata() & 8) != 0, "occupied detector 28", 40);
  }

  BlockState vacate(B173WireClient actor) {
    return worldline.test.WorldlineSmokeAwait.awaitBlockMatching(
        actor, detector, state -> state.legacyId() == 28 && (state.metadata() & 8) == 0, "vacated detector 28", 200);
  }

  void persist(RemoteChunkSnapshot after, int cx, int cz, BlockState vacated) {
    require(at(after, support, cx, cz).equals(new BlockState(1, 0)), "support persist drift");
    require(at(after, high, cx, cz).equals(new BlockState(1, 0)), "high persist drift");
    require(at(after, detector, cx, cz).equals(vacated) && (vacated.metadata() & 8) == 0,
        "persisted detector still powered");
    require(at(after, landing, cx, cz).legacyId() == 66 && at(after, landing2, cx, cz).legacyId() == 66,
        "landing persist drift");
  }

  static BlockPosition raise(B173WireClient actor, RemoteChunkSnapshot initial, int cx, int cz, int[] column)
      throws Exception {
    BlockPosition top = foundation(initial, cx, cz);
    column[0] = 0;
    actor.selectHeldSlot(0);
    while (water(at(initial, BlockFace.UP.adjacent(top), cx, cz).legacyId())) {
      top = place(actor, top, BlockFace.UP, 1);
      actor.moveAndObserve(0D, 1D, 0D, 1);
      require(++column[0] <= 15, "water column exceeded detector-rail-vacate fixture");
    }
    for (int lift = 0; lift < 8; lift++) {
      top = place(actor, top, BlockFace.UP, 1);
      actor.moveAndObserve(0D, 1D, 0D, 1);
      column[0]++;
    }
    return top;
  }

  static BlockPosition place(B173WireClient actor, BlockPosition support, BlockFace face, int id) throws Exception {
    BlockPosition target = face.adjacent(support);
    actor.placeHeldBlock(support, face);
    actor.awaitBlock(target, new BlockState(id, 0));
    return target;
  }

  static BlockPosition placeId(B173WireClient actor, BlockPosition support, BlockFace face, int id) {
    BlockPosition target = face.adjacent(support);
    actor.placeHeldBlock(support, face);
    worldline.test.WorldlineSmokeAwait.awaitBlockMatching(
        actor, target, state -> state.legacyId() == id, "block " + id, 10);
    return target;
  }

  static BlockPosition foundation(RemoteChunkSnapshot chunk, int cx, int cz) {
    for (int x = 4; x <= 11; x++)
      for (int z = 4; z <= 11; z++)
        for (int y = 126; y >= 1; y--)
          if (chunk.blockAt(x, y, z).legacyId() == 3 && water(chunk.blockAt(x, y + 1, z).legacyId()))
            return new BlockPosition(cx * 16 + x, y, cz * 16 + z);
    throw new IllegalStateException("no deterministic detector-rail-vacate foundation");
  }

  static BlockState at(RemoteChunkSnapshot chunk, BlockPosition position, int cx, int cz) {
    return chunk.blockAt(position.x() - cx * 16, position.y(), position.z() - cz * 16);
  }

  static int id(B173WireClient actor, BlockPosition position) {
    RemoteWorldView world = worldline.test.WorldlineSmokeAwait.observe(actor, 1);
    return world.blockAt(position.x(), position.y(), position.z()).legacyId();
  }

  static boolean slope(int metadata) {
    int shape = metadata & 7;
    return shape >= 2 && shape <= 5;
  }

  static boolean water(int id) {
    return id == 8 || id == 9;
  }

  static String cell(BlockPosition position) {
    return position.x() + ":" + position.y() + ":" + position.z();
  }

  static String token(BlockPosition position, int id, int metadata) {
    return cell(position) + ":" + id + ":" + metadata;
  }

  static void awaitPlayers(B173DedicatedServer server, int count) throws Exception {
    long end = System.currentTimeMillis() + 5000;
    while (System.currentTimeMillis() < end) {
      if (server.players().size() == count)
        return;
      Thread.sleep(100);
    }
    throw new IllegalStateException("player count drift");
  }

  static String sha(String value) throws Exception {
    byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
    StringBuilder text = new StringBuilder();
    for (byte item : digest) text.append(String.format("%02x", item & 255));
    return text.toString();
  }

  static void require(boolean value, String message) {
    if (!value)
      throw new IllegalStateException(message);
  }
}
