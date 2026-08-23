package worldline.smoke.repeaterdiodesetb173;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import worldline.api.BlockFace;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.RemoteChunkSnapshot;
import worldline.api.RemoteWorldView;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173WireClient;

/** West-facing repeater between input and output dust, with opposite floor levers. */
public final class RepeaterDiodeSetArm {
  final BlockPosition support, repeater, in, out, fwd, rev;
  final BlockState fwdOff, revOff;

  private RepeaterDiodeSetArm(BlockPosition support, BlockPosition repeater, BlockPosition in, BlockPosition out,
      BlockPosition fwd, BlockPosition rev, BlockState fwdOff, BlockState revOff) {
    this.support = support;
    this.repeater = repeater;
    this.in = in;
    this.out = out;
    this.fwd = fwd;
    this.rev = rev;
    this.fwdOff = fwdOff;
    this.revOff = revOff;
  }

  static RepeaterDiodeSetArm place(B173WireClient actor, RemoteChunkSnapshot initial, BlockPosition support, int chunkX,
      int chunkZ) throws Exception {
    BlockPosition west = BlockFace.WEST.adjacent(support);
    BlockPosition east = BlockFace.EAST.adjacent(support);
    BlockPosition repeater = BlockFace.UP.adjacent(support);
    BlockPosition in = BlockFace.UP.adjacent(east);
    BlockPosition out = BlockFace.UP.adjacent(west);
    BlockPosition fwd = BlockFace.SOUTH.adjacent(east);
    BlockPosition rev = BlockFace.SOUTH.adjacent(west);
    require(air(initial, repeater, chunkX, chunkZ) && air(initial, in, chunkX, chunkZ)
            && air(initial, out, chunkX, chunkZ) && air(initial, fwd, chunkX, chunkZ)
            && air(initial, rev, chunkX, chunkZ),
        "repeater-diode targets were not initial air");
    actor.selectHeldSlot(0);
    place(actor, support, BlockFace.WEST, 1);
    place(actor, support, BlockFace.EAST, 1);
    actor.look(90F, 0F);
    worldline.test.WorldlineSmokeAwait.observe(actor, 2);
    actor.selectHeldSlot(1);
    actor.useHeldItemOnBlock(support, BlockFace.UP);
    require(actor.awaitBlock(repeater, new BlockState(93, 3))
                .blockAt(repeater.x(), repeater.y(), repeater.z())
                .equals(new BlockState(93, 3)),
        "west 1-tick unpowered repeater drift");
    actor.selectHeldSlot(2);
    dust(actor, east, in);
    dust(actor, west, out);
    actor.selectHeldSlot(3);
    BlockState revOff = lever(actor, west, rev, 3);
    BlockState fwdOff = lever(actor, east, fwd, 3);
    return new RepeaterDiodeSetArm(support, repeater, in, out, fwd, rev, fwdOff, revOff);
  }

  RemoteWorldView reverse(B173WireClient actor, int ticks) throws Exception {
    actor.activateBlock(rev, BlockFace.UP);
    RemoteWorldView live = worldline.test.WorldlineSmokeAwait.observe(actor, ticks);
    require(live.blockAt(rev.x(), rev.y(), rev.z()).equals(on(revOff))
            && live.blockAt(repeater.x(), repeater.y(), repeater.z()).equals(new BlockState(93, 3))
            && live.blockAt(in.x(), in.y(), in.z()).equals(new BlockState(55, 0))
            && live.blockAt(out.x(), out.y(), out.z()).equals(new BlockState(55, 15)),
        "reverse isolation absent: " + cell(live, repeater) + "/" + cell(live, in) + "/" + cell(live, out) + "/"
            + cell(live, rev));
    actor.activateBlock(rev, BlockFace.UP);
    live = worldline.test.WorldlineSmokeAwait.observe(actor, ticks);
    require(live.blockAt(rev.x(), rev.y(), rev.z()).equals(revOff)
            && live.blockAt(repeater.x(), repeater.y(), repeater.z()).equals(new BlockState(93, 3))
            && live.blockAt(in.x(), in.y(), in.z()).equals(new BlockState(55, 0))
            && live.blockAt(out.x(), out.y(), out.z()).equals(new BlockState(55, 0)),
        "reverse recovery drift");
    return live;
  }

  RemoteWorldView forward(B173WireClient actor, int ticks) throws Exception {
    actor.activateBlock(fwd, BlockFace.UP);
    RemoteWorldView live = worldline.test.WorldlineSmokeAwait.observe(actor, ticks);
    require(live.blockAt(fwd.x(), fwd.y(), fwd.z()).equals(on(fwdOff))
            && live.blockAt(in.x(), in.y(), in.z()).equals(new BlockState(55, 15)),
        "forward input absent: " + cell(live, in) + "/" + cell(live, fwd));
    return live;
  }

  void persist(RemoteChunkSnapshot after, int chunkX, int chunkZ) {
    require(at(after, repeater, chunkX, chunkZ).equals(new BlockState(94, 3))
            && at(after, in, chunkX, chunkZ).equals(new BlockState(55, 15))
            && at(after, out, chunkX, chunkZ).equals(new BlockState(55, 15))
            && at(after, fwd, chunkX, chunkZ).equals(on(fwdOff)) && at(after, rev, chunkX, chunkZ).equals(revOff),
        "fresh repeater-diode conduction drift");
  }

  static BlockPosition raise(B173WireClient actor, RemoteChunkSnapshot initial, int chunkX, int chunkZ, int[] column)
      throws Exception {
    BlockPosition top = foundation(initial, chunkX, chunkZ);
    column[0] = 0;
    actor.selectHeldSlot(0);
    while (water(at(initial, BlockFace.UP.adjacent(top), chunkX, chunkZ).legacyId())) {
      top = place(actor, top, BlockFace.UP, 1);
      actor.moveAndObserve(0D, 1D, 0D, 1);
      require(++column[0] <= 15, "water column exceeded repeater-diode fixture");
    }
    for (int lift = 0; lift < 8; lift++) {
      top = place(actor, top, BlockFace.UP, 1);
      actor.moveAndObserve(0D, 1D, 0D, 1);
      column[0]++;
    }
    actor.moveAndObserve(0D, 0D, 2D, 1);
    return top;
  }

  static BlockPosition place(B173WireClient actor, BlockPosition support, BlockFace face, int id) throws Exception {
    BlockPosition target = face.adjacent(support);
    actor.placeHeldBlock(support, face);
    actor.awaitBlock(target, new BlockState(id, 0));
    return target;
  }

  static void dust(B173WireClient actor, BlockPosition pad, BlockPosition target) throws Exception {
    actor.useHeldItemOnBlock(pad, BlockFace.UP);
    require(worldline.test.WorldlineSmokeAwait.awaitBlock(actor, target, new BlockState(55, 0), 5)
                .blockAt(target.x(), target.y(), target.z())
                .equals(new BlockState(55, 0)),
        "unpowered dust drift at " + cell(target));
  }

  static BlockState lever(B173WireClient actor, BlockPosition pad, BlockPosition target, int meta) throws Exception {
    actor.placeHeldBlock(pad, BlockFace.SOUTH);
    BlockState live = worldline.test.WorldlineSmokeAwait.awaitBlock(actor, target, new BlockState(69, meta), 10)
                          .blockAt(target.x(), target.y(), target.z());
    require(live.equals(new BlockState(69, meta)), "south lever drift: " + live);
    return live;
  }

  static BlockPosition foundation(RemoteChunkSnapshot chunk, int chunkX, int chunkZ) {
    for (int x = 4; x <= 11; x++) {
      for (int z = 4; z <= 11; z++) {
        for (int y = 126; y >= 1; y--) {
          if (chunk.blockAt(x, y, z).legacyId() == 3 && water(chunk.blockAt(x, y + 1, z).legacyId())) {
            return new BlockPosition(chunkX * 16 + x, y, chunkZ * 16 + z);
          }
        }
      }
    }
    throw new IllegalStateException("no deterministic repeater-diode foundation");
  }

  static BlockState at(RemoteChunkSnapshot chunk, BlockPosition position, int chunkX, int chunkZ) {
    return chunk.blockAt(position.x() - chunkX * 16, position.y(), position.z() - chunkZ * 16);
  }

  static boolean air(RemoteChunkSnapshot chunk, BlockPosition position, int chunkX, int chunkZ) {
    return at(chunk, position, chunkX, chunkZ).legacyId() == 0;
  }

  static boolean water(int id) {
    return id == 8 || id == 9;
  }

  static BlockState on(BlockState lever) {
    return new BlockState(69, lever.metadata() | 8);
  }

  static String cell(BlockPosition position) {
    return position.x() + ":" + position.y() + ":" + position.z();
  }

  static String cell(RemoteWorldView world, BlockPosition position) {
    BlockState state = world.blockAt(position.x(), position.y(), position.z());
    return cell(position) + ":" + state.legacyId() + ":" + state.metadata();
  }

  static void awaitPlayers(B173DedicatedServer server, int count) throws Exception {
    long end = System.currentTimeMillis() + 5000;
    while (System.currentTimeMillis() < end) {
      if (server.players().size() == count) {
        return;
      }
      Thread.sleep(100);
    }
    throw new IllegalStateException("player count drift");
  }

  static String sha(String value) throws Exception {
    byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
    StringBuilder hex = new StringBuilder();
    for (byte item : digest) {
      hex.append(String.format("%02x", item & 255));
    }
    return hex.toString();
  }

  static void require(boolean value, String message) {
    if (!value) {
      throw new IllegalStateException(message);
    }
  }
}
