package worldline.smoke.leveractivationb173;
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

/** Builds and toggles one official lever, then reloads its authoritative state. */
public final class LeverActivationSmoke {
  private LeverActivationSmoke() {
  }
  public static void main(String[] arguments) throws Exception {
    if (arguments.length != 8)
      throw new IllegalArgumentException(
          "usage: LeverActivationSmoke server.jar workspace port seed username chunkX chunkZ settleTicks");
    Path jar = Paths.get(arguments[0]), workspace = Paths.get(arguments[1]);
    int port = Integer.parseInt(arguments[2]);
    long seed = Long.parseLong(arguments[3]);
    String username = arguments[4];
    int chunkX = Integer.parseInt(arguments[5]);
    int chunkZ = Integer.parseInt(arguments[6]), settleTicks = Integer.parseInt(arguments[7]);
    Duration timeout = Duration.ofSeconds(90);
    B173DedicatedServer server =
        new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, username, timeout), reader = null;
    RemoteChunkSnapshot beforeActivation, after;
    BlockPosition foundation, stone, lever;
    BlockState off, on;
    int column;
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, username, 4.5D, 60D, 4.5D, new int[] {0, 1},
          new int[] {1, 69}, new int[] {16, 1}, new int[] {0, 0});
      actor.connect();
      actor.synchronizePose();
      actor.look(-90F, 0F);
      RemoteInventoryView inventory = actor.awaitInventory();
      require(inventory.occupiedSlots() == 2, "lever fixture inventory drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(chunkX, chunkZ).chunkAt(chunkX, chunkZ);
      foundation = foundation(initial, chunkX, chunkZ);
      stone = foundation;
      column = 0;
      actor.selectHeldSlot(0);
      while (
          water(initial.blockAt(local(stone.x(), chunkX), stone.y() + 1, local(stone.z(), chunkZ))
                  .legacyId())) {
        actor.placeHeldBlock(stone, BlockFace.UP);
        stone = BlockFace.UP.adjacent(stone);
        actor.awaitBlock(stone, new BlockState(1, 0));
        actor.moveAndObserve(0D, 1D, 0D, 1);
        column++;
        require(column <= 15, "water column exceeded fixture stack");
      }
      actor.placeHeldBlock(stone, BlockFace.UP);
      stone = BlockFace.UP.adjacent(stone);
      actor.awaitBlock(stone, new BlockState(1, 0));
      actor.moveAndObserve(0D, 1D, 0D, 1);
      column++;
      lever = BlockFace.EAST.adjacent(stone);
      require(
          initial.blockAt(local(lever.x(), chunkX), lever.y(), local(lever.z(), chunkZ)).legacyId()
              == 0,
          "lever target was not initial air");
      actor.selectHeldSlot(1);
      actor.placeHeldBlock(stone, BlockFace.EAST);
      RemoteWorldView placed = worldline.test.WorldlineSmokeAwait.observe(actor, 5);
      off = placed.blockAt(lever.x(), lever.y(), lever.z());
      require(off.legacyId() == 69 && off.metadata() < 8, "lever off-state drift: " + off);
      actor.selectHeldSlot(2);
      beforeActivation =
          worldline.test.WorldlineSmokeAwait.observe(actor, settleTicks).chunkAt(chunkX, chunkZ);
      off = beforeActivation.blockAt(local(lever.x(), chunkX), lever.y(), local(lever.z(), chunkZ));
      actor.activateBlock(lever, BlockFace.UP);
      on = worldline.test.WorldlineSmokeAwait.observe(actor, 5).blockAt(
          lever.x(), lever.y(), lever.z());
      require(on.legacyId() == 69 && on.metadata() != off.metadata(),
          "lever activation state absent: " + off + " -> " + on);
      actor.close();
      awaitPlayers(server, 0);
      server.save();
      reader = new B173WireClient("127.0.0.1", port, username, timeout);
      reader.connect();
      reader.synchronizePose();
      after = reader.awaitRemoteChunk(chunkX, chunkZ).chunkAt(chunkX, chunkZ);
      require(
          after.blockAt(local(lever.x(), chunkX), lever.y(), local(lever.z(), chunkZ)).equals(on),
          "fresh lever state drift");
    } finally {
      actor.close();
      if (reader != null)
        reader.close();
      server.close();
    }
    StateDelta delta = delta(beforeActivation, after);
    require(delta.changed == 1, "activation changed unrelated states: " + delta);
    String evidence = "column=" + column + ",lever=" + lever.x() + ":" + lever.y() + ":" + lever.z()
        + ",off=" + off.legacyId() + ":" + off.metadata() + ",on=" + on.legacyId() + ":"
        + on.metadata() + ",states=" + delta;
    String trace = "v1|server=official-b1.7.3|seed=" + seed
        + "|fixture=stone-column+lever69|settle=" + settleTicks
        + "ticks|cause=empty-hand-packet15-activate|confirmation=packet53|observation=fresh-login-packet51|"
        + evidence + "|disconnect=clean";
    System.out.println("WORLDLINE_M115_LEVER=" + evidence);
    System.out.println("WORLDLINE_M115_TRACE=" + trace);
    System.out.println("WORLDLINE_M115_SIGNATURE=" + sha256(trace));
  }
  private static BlockPosition foundation(RemoteChunkSnapshot chunk, int chunkX, int chunkZ) {
    for (int x = 4; x <= 11; x++)
      for (int z = 4; z <= 11; z++)
        for (int y = 126; y >= 1; y--)
          if (chunk.blockAt(x, y, z).legacyId() == 3
              && water(chunk.blockAt(x, y + 1, z).legacyId()))
            return new BlockPosition(chunkX * 16 + x, y, chunkZ * 16 + z);
    throw new IllegalStateException("no deterministic lever foundation");
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
