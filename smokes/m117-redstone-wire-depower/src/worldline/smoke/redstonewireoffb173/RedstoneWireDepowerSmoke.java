package worldline.smoke.redstonewireoffb173;
import static worldline.b173server.B173FixtureSupport.*;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.BlockFace;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.RemoteChunkSnapshot;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteWorldView;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173PlayerSeed;
import worldline.b173server.B173WireClient;

/** Depowers one official redstone wire by toggling its adjacent lever off. */
public final class RedstoneWireDepowerSmoke {
  private RedstoneWireDepowerSmoke() {
  }
  public static void main(String[] arguments) throws Exception {
    if (arguments.length != 9)
      throw new IllegalArgumentException(
          "usage: RedstoneWireDepowerSmoke server.jar workspace port seed username chunkX chunkZ fixtureTicks signalTicks");
    Path jar = Paths.get(arguments[0]), workspace = Paths.get(arguments[1]);
    int port = Integer.parseInt(arguments[2]);
    long seed = Long.parseLong(arguments[3]);
    String username = arguments[4];
    int chunkX = Integer.parseInt(arguments[5]), chunkZ = Integer.parseInt(arguments[6]);
    int fixtureTicks = Integer.parseInt(arguments[7]), signalTicks = Integer.parseInt(arguments[8]);
    Duration timeout = Duration.ofSeconds(90);
    B173DedicatedServer server =
        new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, username, timeout), reader = null;
    RemoteChunkSnapshot before, after;
    BlockPosition foundation, top, lever, wire;
    BlockState leverOn, leverOff, wireOn, wireOff;
    int column;
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, username, 4.5D, 60D, 4.5D, new int[] {0, 1, 2},
          new int[] {1, 69, 331}, new int[] {16, 1, 1}, new int[] {0, 0, 0});
      actor.connect();
      actor.synchronizePose();
      actor.look(-90F, 0F);
      RemoteInventoryView inventory = actor.awaitInventory();
      require(inventory.occupiedSlots() == 3, "wire fixture inventory drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(chunkX, chunkZ).chunkAt(chunkX, chunkZ);
      foundation = foundation(initial, chunkX, chunkZ);
      top = foundation;
      column = 0;
      actor.selectHeldSlot(0);
      while (water(initial.blockAt(local(top.x(), chunkX), top.y() + 1, local(top.z(), chunkZ))
              .legacyId())) {
        actor.placeHeldBlock(top, BlockFace.UP);
        top = BlockFace.UP.adjacent(top);
        actor.awaitBlock(top, new BlockState(1, 0));
        actor.moveAndObserve(0D, 1D, 0D, 1);
        column++;
        require(column <= 15, "water column exceeded fixture stack");
      }
      actor.placeHeldBlock(top, BlockFace.UP);
      top = BlockFace.UP.adjacent(top);
      actor.awaitBlock(top, new BlockState(1, 0));
      actor.moveAndObserve(0D, 1D, 0D, 1);
      column++;
      wire = BlockFace.UP.adjacent(top);
      lever = BlockFace.EAST.adjacent(top);
      require(initial.blockAt(local(wire.x(), chunkX), wire.y(), local(wire.z(), chunkZ)).legacyId()
                  == 0
              && initial.blockAt(local(lever.x(), chunkX), lever.y(), local(lever.z(), chunkZ))
                      .legacyId()
                  == 0,
          "signal targets were not initial air");
      actor.selectHeldSlot(2);
      actor.useHeldItemOnBlock(top, BlockFace.UP);
      RemoteWorldView dust = worldline.test.WorldlineSmokeAwait.observe(actor, 5);
      wireOff = dust.blockAt(wire.x(), wire.y(), wire.z());
      require(wireOff.equals(new BlockState(55, 0)), "unpowered wire drift: " + wireOff);
      actor.selectHeldSlot(1);
      actor.placeHeldBlock(top, BlockFace.EAST);
      RemoteWorldView placed = worldline.test.WorldlineSmokeAwait.observe(actor, 5);
      leverOff = placed.blockAt(lever.x(), lever.y(), lever.z());
      require(leverOff.equals(new BlockState(69, 1)), "side lever drift: " + leverOff);
      actor.selectHeldSlot(3);
      worldline.test.WorldlineSmokeAwait.observe(actor, fixtureTicks);
      actor.activateBlock(lever, BlockFace.UP);
      RemoteWorldView powered = worldline.test.WorldlineSmokeAwait.observe(actor, signalTicks);
      leverOn = powered.blockAt(lever.x(), lever.y(), lever.z());
      wireOn = powered.blockAt(wire.x(), wire.y(), wire.z());
      require(leverOn.equals(new BlockState(69, 9)) && wireOn.equals(new BlockState(55, 15)),
          "wire did not reach exact powered state: " + leverOn + " / " + wireOn);
      before =
          worldline.test.WorldlineSmokeAwait.observe(actor, signalTicks).chunkAt(chunkX, chunkZ);
      actor.activateBlock(lever, BlockFace.UP);
      RemoteWorldView live = worldline.test.WorldlineSmokeAwait.observe(actor, signalTicks);
      leverOff = live.blockAt(lever.x(), lever.y(), lever.z());
      wireOff = live.blockAt(wire.x(), wire.y(), wire.z());
      require(leverOff.equals(new BlockState(69, 1)) && wireOff.equals(new BlockState(55, 0)),
          "lever signal did not depower wire: " + leverOff + " / " + wireOff);
      actor.close();
      awaitPlayers(server, 0);
      server.save();
      reader = new B173WireClient("127.0.0.1", port, username, timeout);
      reader.connect();
      reader.synchronizePose();
      after = reader.awaitRemoteChunk(chunkX, chunkZ).chunkAt(chunkX, chunkZ);
      require(after.blockAt(local(lever.x(), chunkX), lever.y(), local(lever.z(), chunkZ))
                  .equals(leverOff)
              && after.blockAt(local(wire.x(), chunkX), wire.y(), local(wire.z(), chunkZ))
                  .equals(wireOff),
          "fresh depowered state drift");
    } finally {
      actor.close();
      if (reader != null)
        reader.close();
      server.close();
    }
    StateDelta delta = delta(before, after);
    require(delta.changed == 2, "depower changed unrelated states: " + delta);
    String evidence = "column=" + column + ",lever=" + lever.x() + ":" + lever.y() + ":" + lever.z()
        + ":" + leverOn.metadata() + "->" + leverOff.metadata() + ",wire=" + wire.x() + ":"
        + wire.y() + ":" + wire.z() + ":" + wireOn.metadata() + "->" + wireOff.metadata()
        + ",states=" + delta;
    String trace = "v1|server=official-b1.7.3|seed=" + seed
        + "|fixture=stone-column+lever69+dust331-wire55|settle=" + fixtureTicks + "+" + signalTicks
        + "ticks|precondition=lever69:9+wire55:15|cause=packet15-lever-activate|effect=packet53-wire-depower|observation=fresh-login-packet51|"
        + evidence + "|disconnect=clean";
    System.out.println("WORLDLINE_M117_SIGNAL=" + evidence);
    System.out.println("WORLDLINE_M117_TRACE=" + trace);
    System.out.println("WORLDLINE_M117_SIGNATURE=" + sha256(trace));
  }
  private static BlockPosition foundation(RemoteChunkSnapshot chunk, int chunkX, int chunkZ) {
    for (int x = 4; x <= 11; x++)
      for (int z = 4; z <= 11; z++)
        for (int y = 126; y >= 1; y--)
          if (chunk.blockAt(x, y, z).legacyId() == 3
              && water(chunk.blockAt(x, y + 1, z).legacyId()))
            return new BlockPosition(chunkX * 16 + x, y, chunkZ * 16 + z);
    throw new IllegalStateException("no deterministic redstone foundation");
  }
  private static StateDelta delta(RemoteChunkSnapshot before, RemoteChunkSnapshot after)
      throws Exception {
    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    ByteBuffer row = ByteBuffer.allocate(10);
    int changed = 0;
    for (int x = 0; x < 16; x++)
      for (int z = 0; z < 16; z++)
        for (int y = 0; y < 128; y++) {
          BlockState a = before.blockAt(x, y, z), b = after.blockAt(x, y, z);
          if (!a.equals(b)) {
            changed++;
            row.clear();
            row.putShort((short) x)
                .putShort((short) y)
                .putShort((short) z)
                .put((byte) a.legacyId())
                .put((byte) a.metadata())
                .put((byte) b.legacyId())
                .put((byte) b.metadata());
            digest.update(row.array());
          }
        }
    return new StateDelta(changed, hex(digest.digest()));
  }
  private static String sha256(String value) throws Exception {
    return hex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
  }
  private static String hex(byte[] value) {
    StringBuilder result = new StringBuilder();
    for (byte item : value)
      result.append(String.format("%02x", item & 255));
    return result.toString();
  }
  private static void require(boolean value, String message) {
    if (!value)
      throw new IllegalStateException(message);
  }
  private static final class StateDelta {
    final int changed;
    final String hash;
    StateDelta(int c, String h) {
      changed = c;
      hash = h;
    }
    @Override
    public String toString() {
      return changed + ":" + hash;
    }
  }
}
