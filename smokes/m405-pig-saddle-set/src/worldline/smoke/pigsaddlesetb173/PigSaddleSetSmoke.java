package worldline.smoke.pigsaddlesetb173;
import static worldline.b173server.B173FixtureSupport.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Applies saddle 329 to Packet24 type-90 pig via Packet7 button 0, then empty-hand mounts Packet39. */
public final class PigSaddleSetSmoke {
  private PigSaddleSetSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 7)
      throw new IllegalArgumentException(
          "usage: PigSaddleSetSmoke server.jar workspace port seed username chunkX chunkZ");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    int cx = Integer.parseInt(a[5]), cz = Integer.parseInt(a[6]);
    require(seed == 17320110707L && user.equals("PigSaddle405") && user.length() <= 16,
        "pig-saddle-set identity drift");
    Duration timeout = Duration.ofSeconds(90);
    B173DedicatedServer server =
        B173DedicatedServer.animals(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout);
    BlockPosition top;
    int column;
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1, 2, 3},
          new int[] {1, 2, 52, 329}, new int[] {32, 48, 1, 1}, new int[] {0, 0, 0, 0});
      actor.connect();
      actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() == 4, "pig-saddle-set inventory drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      top = foundation(initial, cx, cz);
      column = 0;
      actor.selectHeldSlot(0);
      while (
          water(initial.blockAt(local(top.x(), cx), top.y() + 1, local(top.z(), cz)).legacyId())) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        require(++column <= 15, "water column exceeded pig-saddle-set fixture");
      }
      for (int lift = 0; lift < 8; lift++) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
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
      actor.selectHeldSlot(2);
      place(actor, top, BlockFace.UP, 52);
      RemoteMobSpawn spawn = actor.awaitMobSpawn(90);
      require(spawn.legacyType() == 90 && spawn.entityId() != actor.state().entityId(),
          "pig Packet24 type 90 identity drift");
      int saddle = find(actor.inventory(), 329);
      require(saddle >= 36, "saddle 329 absent from hotbar");
      actor.selectHeldSlot(saddle - 36);
      approach(actor, spawn);
      saddle = find(actor.inventory(), 329);
      require(saddle >= 36, "saddle 329 lost before use");
      actor.selectHeldSlot(saddle - 36);
      B173VehicleAccess.useSaddle(actor, spawn.entityId());
      B173VehicleAccess.awaitSaddleConsumed(actor);
      require(find(actor.inventory(), 329) < 0, "saddle 329 consume drift");
      B173VehicleAccess.useVehicle(actor, spawn.entityId());
      B173VehicleAttach ride = B173VehicleAccess.awaitAttach(actor, spawn.entityId());
      require(
          ride.passengerId() == actor.state().entityId() && ride.vehicleId() == spawn.entityId(),
          "pig Packet39 attach identity drift");
      actor.close();
      awaitPlayers(server, 0);
      server.save();
      String evidence = "column=" + column
          + ",platform=7x7-48grass,spawner=52:0,mob=type90,saddle=329+consumed,mount=packet7-button0+packet39-attach,clients=1,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=raised-7x7-grass-platform+default-spawner52|cause=packet7-button0-saddle329+empty-hand-packet7-mount|wire=packet24-type90+packet103-saddle-consume+packet39-attach|oracle=pig-type90-saddle329-mount|"
          + evidence;
      System.out.println("WORLDLINE_M405_SET=" + evidence);
      System.out.println("WORLDLINE_M405_TRACE=" + trace);
      System.out.println("WORLDLINE_M405_SIGNATURE=" + sha(trace));
    } finally {
      actor.close();
      server.close();
    }
  }
  private static int find(RemoteInventoryView view, int id) {
    for (int slot = 36; slot <= 44; slot++)
      if (!view.slot(slot).empty() && view.slot(slot).item().legacyId() == id)
        return slot;
    return -1;
  }
  private static void approach(B173WireClient a, RemoteMobSpawn spawn) {
    for (int step = 0; step < 8; step++) {
      PlayerPose here = a.moveAndObserve(0D, 0D, 0D, 1).resulting();
      double dx = spawn.x() - here.x(), dy = spawn.y() - here.y(), dz = spawn.z() - here.z(),
             dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
      if (dist <= 2.5D)
        return;
      double s = Math.min(1D, 4D / dist);
      a.moveAndObserve(dx * s, dy * s, dz * s, 8);
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
    throw new IllegalStateException("no deterministic pig-saddle-set foundation");
  }
  private static void require(boolean v, String m) {
    if (!v)
      throw new IllegalStateException(m);
  }
}
