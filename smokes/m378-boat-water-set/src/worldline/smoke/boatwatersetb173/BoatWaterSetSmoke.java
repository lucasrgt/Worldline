package worldline.smoke.boatwatersetb173;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Places boat 333 on still water 9, freezes Packet23 type 1 spawn plus Packet39 ride then detach. */
public final class BoatWaterSetSmoke {
  private BoatWaterSetSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 8)
      throw new IllegalArgumentException(
          "usage: BoatWaterSetSmoke server.jar workspace port seed actor observer chunkX chunkZ");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String actorName = a[4], observerName = a[5];
    int cx = Integer.parseInt(a[6]), cz = Integer.parseInt(a[7]);
    require(seed == 17320110707L && actorName.equals("BoatWater378") && actorName.length() <= 16
            && observerName.equals("BoatSee378") && observerName.length() <= 16,
        "boat-water-set identity drift");
    Duration timeout = Duration.ofSeconds(90);
    B173DedicatedServer server =
        new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, actorName, timeout),
                   observer = new B173WireClient("127.0.0.1", port, observerName, timeout);
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, actorName, 4.5D, 60D, 4.5D, new int[] {0},
          new int[] {333}, new int[] {1}, new int[] {0});
      B173PlayerSeed.write(workspace, observerName, 4.5D, 60D, 4.5D);
      actor.connect();
      actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() == 1, "boat inventory drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      BlockPosition water = waterCell(initial, cx, cz);
      int fluid = initial.blockAt(local(water.x(), cx), water.y(), local(water.z(), cz)).legacyId();
      require(fluid == 9, "still-water 9 fixture drift");
      observer.connect();
      observer.synchronizePose();
      observer.awaitRemoteChunk(cx, cz);
      actor.selectHeldSlot(0);
      actor.look(0F, 90F);
      actor.useSelectedItemInAir();
      RemoteObjectSpawn spawn = actor.awaitObjectSpawn(1), peer = observer.awaitObjectSpawn(1);
      require(spawn.equals(peer) && spawn.type() == 1
              && spawn.entityId() != actor.state().entityId()
              && spawn.entityId() != observer.state().entityId(),
          "peer boat spawn drift");
      require(Math.abs(spawn.x() - 4.5D) <= 6D && Math.abs(spawn.z() - 4.5D) <= 6D,
          "boat packet pose escaped player water pose=" + spawn.x() + ":" + spawn.y() + ":"
              + spawn.z());
      worldline.test.WorldlineSmokeAwait.awaitEntity(
          actor, actor::inventory, v -> v.slot(36).empty(), "boat consumption", 20);
      actor.selectHeldSlot(0);
      B173VehicleAccess.useVehicle(actor, spawn.entityId());
      B173VehicleAttach ride = B173VehicleAccess.awaitAttach(actor, spawn.entityId());
      require(ride.passengerId() == actor.state().entityId() && ride.vehicleId() == spawn.entityId()
              && ride.vehicleId() != -1,
          "boat attach identity drift");
      worldline.test.WorldlineSmokeAwait.observe(actor, 5);
      B173VehicleAccess.useVehicle(actor, spawn.entityId());
      B173VehicleAttach leave = B173VehicleAccess.awaitDetach(actor, actor.state().entityId());
      require(leave.passengerId() == actor.state().entityId() && leave.vehicleId() == -1,
          "boat detach identity drift");
      worldline.test.WorldlineSmokeAwait.observe(actor, 5);
      worldline.test.WorldlineSmokeAwait.observe(observer, 5);
      actor.close();
      observer.close();
      awaitPlayers(server, 0);
      server.save();
      String evidence = "water=" + water.x() + ":" + water.y() + ":" + water.z() + ":" + fluid
          + ":0,boat=type1+shared-id+packet23+attach+detach,pose=" + spawn.fixedX() + ":"
          + spawn.fixedY() + ":" + spawn.fixedZ() + ",clients=2,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed + "|fixture=natural-water" + fluid
          + "|cause=packet15-dir255-boat333+empty-hand-packet7-mount+empty-hand-packet7-unmount|wire=packet23-type1+packet39-attach+packet39-detach|oracle=two-peer-identical-boat-spawn+ride-then-detach+not-craft|"
          + evidence;
      System.out.println("WORLDLINE_M378_SET=" + evidence);
      System.out.println("WORLDLINE_M378_TRACE=" + trace);
      System.out.println("WORLDLINE_M378_SIGNATURE=" + sha(trace));
    } finally {
      actor.close();
      observer.close();
      server.close();
    }
  }
  private static BlockPosition waterCell(RemoteChunkSnapshot q, int cx, int cz) {
    for (int x = 4; x <= 11; x++)
      for (int z = 4; z <= 11; z++)
        for (int y = 126; y >= 1; y--)
          if (q.blockAt(x, y, z).legacyId() == 3 && water(q.blockAt(x, y + 1, z).legacyId())
              && q.blockAt(x, 60, z).legacyId() == 9)
            return new BlockPosition(cx * 16 + x, 60, cz * 16 + z);
    throw new IllegalStateException("no deterministic still-water 9 boat cell");
  }
  private static boolean water(int id) {
    return id == 8 || id == 9;
  }
  private static int local(int v, int c) {
    return v - c * 16;
  }
  private static void awaitPlayers(B173DedicatedServer s, int n) throws Exception {
    long e = System.currentTimeMillis() + 5000;
    while (System.currentTimeMillis() < e) {
      if (s.players().size() == n)
        return;
      Thread.sleep(100);
    }
    throw new IllegalStateException("player count drift");
  }
  private static String sha(String s) throws Exception {
    byte[] b = MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));
    StringBuilder v = new StringBuilder();
    for (byte x : b)
      v.append(String.format("%02x", x & 255));
    return v.toString();
  }
  private static void require(boolean v, String m) {
    if (!v)
      throw new IllegalStateException(m);
  }
}
