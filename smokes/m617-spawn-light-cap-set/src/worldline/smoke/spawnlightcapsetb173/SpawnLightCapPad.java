package worldline.smoke.spawnlightcapsetb173;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import worldline.api.BlockFace;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.RemoteChunkSnapshot;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173PlayerSeed;
import worldline.b173server.B173SpawnerSeed;
import worldline.b173server.B173WireClient;

/** Raises the isolated 7x7 grass pad and two spawners, then copies the world for the torch arm. */
final class SpawnLightCapPad {
  final BlockPosition first, second, sample;
  final int column;

  private SpawnLightCapPad(BlockPosition first, BlockPosition second, BlockPosition sample, int column) {
    this.first = first;
    this.second = second;
    this.sample = sample;
    this.column = column;
  }

  static SpawnLightCapPad build(
      Path jar, Path workspace, int port, long seed, String user, int cx, int cz, Duration timeout) throws Exception {
    B173DedicatedServer server = B173DedicatedServer.monsters(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout);
    BlockPosition top, first, second;
    int column;
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1, 2, 3, 4},
          new int[] {1, 2, 52, 52, 50}, new int[] {32, 48, 1, 1, 64}, new int[] {0, 0, 0, 0, 0});
      actor.connect();
      actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() == 5, "spawn-light-cap inventory drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      top = foundation(initial, cx, cz);
      column = 0;
      actor.selectHeldSlot(0);
      while (water(initial.blockAt(local(top.x(), cx), top.y() + 1, local(top.z(), cz)).legacyId())) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        require(++column <= 15, "water column exceeded spawn-light-cap fixture");
      }
      for (int lift = 0; lift < 8; lift++) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        column++;
      }
      actor.selectHeldSlot(1);
      grassRing(actor, top);
      actor.selectHeldSlot(2);
      first = place(actor, top, BlockFace.UP, 52);
      actor.selectHeldSlot(3);
      second = place(actor, first, BlockFace.EAST, 52);
      worldline.test.WorldlineSmokeAwait.observe(actor, 5);
      actor.close();
      awaitPlayers(server, 0);
      server.save();
    } finally {
      actor.close();
      server.close();
    }
    Thread.sleep(1000L);
    B173SpawnerSeed.entity(workspace, first, "Creeper");
    B173SpawnerSeed.entity(workspace, second, "Zombie");
    BlockPosition sample = new BlockPosition(first.x(), first.y(), first.z() + 1);
    return new SpawnLightCapPad(first, second, sample, column);
  }

  static void copyWorld(Path from, Path to) throws IOException {
    Path root = from.toAbsolutePath().normalize();
    Path src = root.resolve("world");
    Path dst = to.toAbsolutePath().normalize().resolve("world");
    require(src.startsWith(root) && Files.isDirectory(src), "spawn-light-cap world absent");
    if (Files.exists(dst)) {
      require(dst.startsWith(to.toAbsolutePath().normalize()) && !dst.equals(to.toAbsolutePath().normalize()),
          "unsafe lit copy");
      try (Stream<Path> paths = Files.walk(dst)) {
        for (Path file : paths.sorted(Comparator.reverseOrder()).collect(Collectors.toList())) Files.delete(file);
      }
    }
    try (Stream<Path> paths = Files.walk(src)) {
      for (Path file : paths.collect(Collectors.toList())) {
        if ("session.lock".equals(file.getFileName().toString()))
          continue;
        Path target = dst.resolve(src.relativize(file).toString());
        if (Files.isDirectory(file))
          Files.createDirectories(target);
        else {
          Files.createDirectories(target.getParent());
          Files.copy(file, target, StandardCopyOption.REPLACE_EXISTING);
        }
      }
    }
  }

  static int lightPad(B173WireClient actor, BlockPosition first) {
    actor.selectHeldSlot(4);
    int[] dx = {-2, -2, 2, 2};
    int[] dz = {-2, 2, -2, 2};
    for (int i = 0; i < 4; i++) torch(actor, new BlockPosition(first.x() + dx[i], first.y() - 1, first.z() + dz[i]));
    worldline.test.WorldlineSmokeAwait.observe(actor, 10);
    return 4;
  }

  static BlockPosition torch(B173WireClient actor, BlockPosition support) {
    BlockPosition target = BlockFace.UP.adjacent(support);
    actor.placeHeldBlock(support, BlockFace.UP);
    actor.awaitBlock(target, new BlockState(50, 5));
    return target;
  }

  static BlockPosition place(B173WireClient actor, BlockPosition support, BlockFace face, int id) {
    BlockPosition target = face.adjacent(support);
    actor.placeHeldBlock(support, face);
    actor.awaitBlock(target, new BlockState(id, 0));
    return target;
  }

  private static void grassRing(B173WireClient actor, BlockPosition top) {
    for (int r = 1; r <= 3; r++) {
      for (int z = -r + 1; z < r; z++) {
        grass(actor, new BlockPosition(top.x() - r + 1, top.y(), top.z() + z), BlockFace.WEST);
        grass(actor, new BlockPosition(top.x() + r - 1, top.y(), top.z() + z), BlockFace.EAST);
      }
      for (int x = -r + 1; x < r; x++) {
        grass(actor, new BlockPosition(top.x() + x, top.y(), top.z() - r + 1), BlockFace.NORTH);
        grass(actor, new BlockPosition(top.x() + x, top.y(), top.z() + r - 1), BlockFace.SOUTH);
      }
      grass(actor, new BlockPosition(top.x() - r, top.y(), top.z() - r + 1), BlockFace.NORTH);
      grass(actor, new BlockPosition(top.x() - r, top.y(), top.z() + r - 1), BlockFace.SOUTH);
      grass(actor, new BlockPosition(top.x() + r, top.y(), top.z() - r + 1), BlockFace.NORTH);
      grass(actor, new BlockPosition(top.x() + r, top.y(), top.z() + r - 1), BlockFace.SOUTH);
    }
  }

  private static void grass(B173WireClient actor, BlockPosition support, BlockFace face) {
    place(actor, support, face, 2);
  }

  private static BlockPosition foundation(RemoteChunkSnapshot chunk, int cx, int cz) {
    for (int x = 4; x <= 11; x++)
      for (int z = 4; z <= 11; z++)
        for (int y = 126; y >= 1; y--)
          if (chunk.blockAt(x, y, z).legacyId() == 3 && water(chunk.blockAt(x, y + 1, z).legacyId()))
            return new BlockPosition(cx * 16 + x, y, cz * 16 + z);
    throw new IllegalStateException("no deterministic spawn-light-cap foundation");
  }

  static String cell(BlockPosition position, int id, int meta) {
    return position.x() + ":" + position.y() + ":" + position.z() + ":" + id + ":" + meta;
  }

  static boolean water(int id) {
    return id == 8 || id == 9;
  }

  static int local(int value, int chunk) {
    return value - chunk * 16;
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
