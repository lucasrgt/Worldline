package worldline.smoke.bedspawnsetb173;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;
import worldline.api.BlockPosition;
import worldline.api.PlayerPose;
import worldline.api.RemoteRespawn;
import worldline.b173server.B173WireClient;

/** Smoke-local world-spawn NBT reader plus cactus-death Packet9 bed oracle. */
final class BedSpawnOracle {
  final int x, y, z;

  private BedSpawnOracle(int x, int y, int z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }

  static BedSpawnOracle read(Path level) {
    try (DataInputStream input =
             new DataInputStream(new GZIPInputStream(Files.newInputStream(level)))) {
      BedSpawnSupport.require(input.readUnsignedByte() == 10, "level.dat root is not a compound");
      input.readUTF();
      int[] spawn = new int[] {Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE};
      scan(input, spawn);
      BedSpawnSupport.require(spawn[0] != Integer.MIN_VALUE && spawn[1] != Integer.MIN_VALUE
              && spawn[2] != Integer.MIN_VALUE,
          "level.dat is missing SpawnX/Y/Z");
      return new BedSpawnOracle(spawn[0], spawn[1], spawn[2]);
    } catch (IOException error) {
      throw new IllegalStateException("could not read world spawn", error);
    }
  }

  PlayerPose cactusDeath(B173WireClient actor, PlayerPose pose, BlockPosition cactus)
      throws Exception {
    pose = enter(actor, pose, cactus);
    BedSpawnSupport.require(
        actor.health() == 20 && pose.y() > 0D, "cactus death must start alive on the pad");
    int waited = 0;
    while (actor.health() > 0) {
      BedSpawnSupport.require(
          ++waited <= 120, "cactus Packet8 health 0 absent health=" + actor.health());
      worldline.test.WorldlineSmokeAwait.observe(actor, 5);
      pose = enter(actor, pose, cactus);
    }
    int dead = actor.health();
    BedSpawnSupport.require(dead <= 0, "cactus death Packet8 drift health=" + dead);
    if (dead == 0)
      BedSpawnSupport.require(actor.awaitHealth(0) == 0, "awaitHealth(0) drift");
    RemoteRespawn respawn = actor.respawn();
    BedSpawnSupport.require(respawn.equals(new RemoteRespawn(0, 0, 20)) && actor.dimension() == 0
            && actor.health() == 20,
        "bed-spawn Packet9 respawn drift");
    worldline.test.WorldlineSmokeAwait.observe(actor, 1);
    return actor.moveAndObserve(0D, 0D, 0D, 1).resulting();
  }

  boolean atBed(PlayerPose pose, BlockPosition foot, BlockPosition head) {
    double bx = (foot.x() + head.x()) * 0.5D + 0.5D, bz = (foot.z() + head.z()) * 0.5D + 0.5D;
    double dx = pose.x() - bx, dz = pose.z() - bz, dy = pose.y() - foot.y();
    return dx * dx + dz * dz <= 16.0D && dy >= -0.5D && dy <= 3.0D;
  }

  boolean atWorld(PlayerPose pose) {
    double dx = pose.x() - (x + 0.5D), dy = pose.y() - y, dz = pose.z() - (z + 0.5D);
    return dx * dx + dy * dy + dz * dz <= 4.0D;
  }

  private static PlayerPose enter(B173WireClient actor, PlayerPose pose, BlockPosition cactus)
      throws Exception {
    return actor
        .moveAndObserve((cactus.x() + 0.5D) - pose.x(), cactus.y() - pose.y(),
            (cactus.z() + 0.5D) - pose.z(), 2)
        .resulting();
  }

  private static void scan(DataInputStream input, int[] spawn) throws IOException {
    while (true) {
      int type = input.readUnsignedByte();
      if (type == 0)
        return;
      String name = input.readUTF();
      if (type == 3 && name.equals("SpawnX"))
        spawn[0] = input.readInt();
      else if (type == 3 && name.equals("SpawnY"))
        spawn[1] = input.readInt();
      else if (type == 3 && name.equals("SpawnZ"))
        spawn[2] = input.readInt();
      else
        skip(input, type, spawn);
    }
  }

  private static void skip(DataInputStream input, int type, int[] spawn) throws IOException {
    switch (type) {
      case 1:
        input.readByte();
        return;
      case 2:
        input.readShort();
        return;
      case 3:
        input.readInt();
        return;
      case 4:
        input.readLong();
        return;
      case 5:
        input.readFloat();
        return;
      case 6:
        input.readDouble();
        return;
      case 7:
        drop(input, input.readInt());
        return;
      case 8:
        input.readUTF();
        return;
      case 9:
        int child = input.readUnsignedByte(), count = input.readInt();
        for (int index = 0; index < count; index++)
          skip(input, child, spawn);
        return;
      case 10:
        scan(input, spawn);
        return;
      case 11:
        drop(input, Math.multiplyExact(input.readInt(), 4));
        return;
      default:
        throw new IOException("unknown NBT tag " + type);
    }
  }

  private static void drop(DataInputStream input, int bytes) throws IOException {
    if (bytes < 0)
      throw new IOException("negative NBT length");
    for (int remaining = bytes; remaining > 0;) {
      int skipped = input.skipBytes(remaining);
      if (skipped == 0)
        throw new IOException("truncated NBT payload");
      remaining -= skipped;
    }
  }
}
