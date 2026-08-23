package worldline.smoke.monsterbedinterruptsetb173;
import static worldline.b173server.B173FixtureSupport.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Occupies one official bed at night, then a nearby zombie 54 interrupts sleep so 26:12 clears. */
public final class MonsterBedInterruptSetSmoke {
  private MonsterBedInterruptSetSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 7)
      throw new IllegalArgumentException(
          "usage: MonsterBedInterruptSetSmoke server.jar workspace port seed username chunkX chunkZ");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    int cx = Integer.parseInt(a[5]), cz = Integer.parseInt(a[6]);
    require(seed == 17320110707L && user.equals("BedIntr460") && user.length() <= 16,
        "monster-bed-interrupt-set identity drift");
    Duration timeout = Duration.ofSeconds(180);
    B173DedicatedServer server =
        B173DedicatedServer.monsters(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout), reader = null;
    BlockPosition top, foot, head, spawner;
    int column;
    RemoteBedUse sleep;
    PlayerPose pose, wake;
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1, 2, 3, 4},
          new int[] {1, 2, 52, 355, 85}, new int[] {32, 48, 1, 1, 24}, new int[] {0, 0, 0, 0, 0});
      actor.connect();
      pose = actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() == 5, "monster-bed-interrupt inventory drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      top = foundation(initial, cx, cz);
      column = 0;
      actor.selectHeldSlot(0);
      while (
          water(initial.blockAt(local(top.x(), cx), top.y() + 1, local(top.z(), cz)).legacyId())) {
        top = place(actor, top, BlockFace.UP, 1);
        pose = actor.moveAndObserve(0D, 1D, 0D, 1).resulting();
        require(++column <= 15, "water column exceeded monster-bed-interrupt fixture");
      }
      for (int lift = 0; lift < 8; lift++) {
        top = place(actor, top, BlockFace.UP, 1);
        pose = actor.moveAndObserve(0D, 1D, 0D, 1).resulting();
        column++;
      }
      actor.selectHeldSlot(1);
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
      arena(actor, top, 4);
      pose = go(actor, top.x() + 0.5D, top.y() + 1.0D, top.z() + 0.5D);
      actor.look(0F, 0F);
      actor.moveAndObserve(0D, 0D, 0D, 2);
      actor.selectHeldSlot(3);
      actor.useHeldItemOnBlock(top, BlockFace.UP);
      foot = BlockFace.UP.adjacent(top);
      head = BlockFace.SOUTH.adjacent(foot);
      actor.awaitBlock(foot, new BlockState(26, 0));
      actor.awaitBlock(head, new BlockState(26, 8));
      actor.selectHeldSlot(2);
      spawner = place(actor, new BlockPosition(top.x() + 2, top.y(), top.z()), BlockFace.UP, 52);
      actor.close();
      awaitPlayers(server, 0);
      server.save();
    } finally {
      actor.close();
      server.close();
    }
    Thread.sleep(1000L);
    B173SpawnerSeed.entity(workspace, spawner, "Zombie");
    server = B173DedicatedServer.monsters(jar, workspace, port, seed, timeout, 3, true);
    actor = new B173WireClient("127.0.0.1", port, user, timeout);
    try {
      server.boot();
      actor.connect();
      pose = actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() >= 1,
          "monster-bed-interrupt reload inventory drift");
      actor.awaitRemoteChunk(cx, cz);
      pose = go(actor, foot.x() + 0.5D, foot.y(), foot.z() + 0.5D);
      actor.selectHeldSlot(4);
      server.setTime(14000L);
      actor.activateBlock(foot, BlockFace.UP);
      sleep = B173BedAccess.await(actor);
      require(sleep.entityId() == actor.state().entityId() && sleep.unused() == 0
              && sleep.x() == head.x() && sleep.y() == head.y() && sleep.z() == head.z()
              && sleep.sleepPacket() == 17 && sleep.bedPacket() == 70
              && sleep.packet70() == RemoteBedUse.NO_PACKET70,
          "Packet17 sleep enter drift");
      actor.awaitBlock(head, new BlockState(26, 12));
      RemoteMobSpawn zombie = near(actor, 54, spawner, top);
      require(zombie.legacyType() == 54 && zombie.entityId() != actor.state().entityId()
              && zombie.legacyType() != 90,
          "zombie Packet24 identity drift");
      worldline.test.WorldlineSmokeAwait.awaitWorld(actor, view -> {
        if (actor.health() == 0)
          throw new IllegalStateException("actor died before bed leave");
        return view.blockAt(head.x(), head.y(), head.z()).equals(new BlockState(26, 8));
      }, "monster bed interrupt", 1200);
      require(
          actor.health() > 0 && actor.health() < 20, "monster did not interrupt occupied 26:12");
      wake = actor.moveAndObserve(0D, 0D, 0D, 4).resulting();
      require(wake.y() >= foot.y() - 0.5D && wake.y() <= foot.y() + 2.0D,
          "actor is not standing after monster interrupt");
      actor.close();
      awaitPlayers(server, 0);
      server.save();
      long time = server.state().worldTime();
      require(
          time >= 12000L && time < 24000L, "interrupt collapsed to M330 morning skip time=" + time);
      reader = new B173WireClient("127.0.0.1", port, user, timeout);
      reader.connect();
      pose = reader.synchronizePose();
      RemoteChunkSnapshot after = reader.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      require(after.blockAt(local(foot.x(), cx), foot.y(), local(foot.z(), cz))
                  .equals(new BlockState(26, 0))
              && after.blockAt(local(head.x(), cx), head.y(), local(head.z(), cz))
                  .equals(new BlockState(26, 8)),
          "persisted interrupt bed halves drift");
      require(pose.y() >= foot.y() - 0.5D && pose.y() <= foot.y() + 2.0D,
          "persisted interrupt is not standing");
      String evidence = "column=" + column + ",arena=fence85-24,foot=" + cell(foot, 26, 0)
          + ",head=" + cell(head, 26, 8) + ",spawner=" + cell(spawner, 52, 0)
          + ",entityid=Zombie,mob=type54,night=14000,enter=26:8->26:12,packet17=head,packet70=-1,leave=26:12->26:8,interrupt=type54,skip=false,wake=standing,persisted=leave,clients=2,disconnect=clean";
      String trace = "v2|server=official-b1.7.3|seed=" + seed
          + "|profile=spawn-monsters-true|fixture=raised-7x7-grass-platform+fence85-arena+item355-block26+spawner52|cause=nbt-entityid-zombie+time-14000+packet15-empty-hand-night-use+hostile-attack|wire=packet17-sleep+packet70=-1+packet24-type54+packet53-occupied-clear|oracle=sleep-enter+monster-interrupt-leave-not-morning-skip|"
          + evidence;
      System.out.println("WORLDLINE_M460_SET=" + evidence);
      System.out.println("WORLDLINE_M460_TRACE=" + trace);
      System.out.println("WORLDLINE_M460_SIGNATURE=" + sha(trace));
    } finally {
      actor.close();
      if (reader != null)
        reader.close();
      server.close();
    }
  }
  private static RemoteMobSpawn near(
      B173WireClient a, int type, BlockPosition p, BlockPosition top) {
    for (int n = 0; n < 32; n++) {
      RemoteMobSpawn s = a.awaitMobSpawn(type);
      double dx = s.x() - (top.x() + 0.5D), dz = s.z() - (top.z() + 0.5D);
      if (Math.abs(dx) <= 2.5D && Math.abs(dz) <= 2.5D && Math.abs(s.y() - p.y()) <= 2D)
        return s;
    }
    throw new IllegalStateException("arena-contained hostile type " + type + " absent");
  }
  private static PlayerPose go(B173WireClient a, double x, double y, double z) throws Exception {
    PlayerPose here = a.moveAndObserve(0D, 0D, 0D, 1).resulting();
    for (int step = 0; step < 16; step++) {
      double dx = x - here.x(), dy = y - here.y(), dz = z - here.z(),
             dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
      if (dist <= 1.25D)
        return here;
      double s = Math.min(1D, 9.0D / dist);
      here = a.moveAndObserve(dx * s, dy * s, dz * s, 2).resulting();
    }
    throw new IllegalStateException("movement cap missed monster-bed-interrupt fixture");
  }
  private static void arena(B173WireClient a, BlockPosition top, int slot) throws Exception {
    a.selectHeldSlot(slot);
    for (int x = -3; x <= 3; x++) {
      place(a, new BlockPosition(top.x() + x, top.y(), top.z() - 3), BlockFace.UP, 85);
      place(a, new BlockPosition(top.x() + x, top.y(), top.z() + 3), BlockFace.UP, 85);
    }
    for (int z = -2; z <= 2; z++) {
      place(a, new BlockPosition(top.x() - 3, top.y(), top.z() + z), BlockFace.UP, 85);
      place(a, new BlockPosition(top.x() + 3, top.y(), top.z() + z), BlockFace.UP, 85);
    }
  }
  private static void grass(B173WireClient a, BlockPosition support, BlockFace face)
      throws Exception {
    place(a, support, face, 2);
  }
  private static BlockPosition foundation(RemoteChunkSnapshot q, int cx, int cz) {
    for (int x = 4; x <= 11; x++)
      for (int z = 4; z <= 11; z++)
        for (int y = 126; y >= 1; y--)
          if (q.blockAt(x, y, z).legacyId() == 3 && water(q.blockAt(x, y + 1, z).legacyId()))
            return new BlockPosition(cx * 16 + x, y, cz * 16 + z);
    throw new IllegalStateException("no deterministic monster-bed-interrupt foundation");
  }
  private static String cell(BlockPosition p, int id, int meta) {
    return p.x() + ":" + p.y() + ":" + p.z() + ":" + id + ":" + meta;
  }
  private static void require(boolean v, String m) {
    if (!v)
      throw new IllegalStateException(m);
  }
}
