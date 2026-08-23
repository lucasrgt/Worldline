package worldline.smoke.compasspointsetb173;
import static worldline.b173server.B173FixtureSupport.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Proves held compass 345, official spawn data, two positions, and persistence. */
public final class CompassPointSetSmoke {
  private CompassPointSetSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 7)
      throw new IllegalArgumentException(
          "usage: CompassPointSetSmoke server.jar workspace port seed username chunkX chunkZ");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    int cx = Integer.parseInt(a[5]), cz = Integer.parseInt(a[6]);
    Duration timeout = Duration.ofSeconds(90);
    require(user.length() <= 16, "username exceeds 16");
    B173DedicatedServer server =
        new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout), reader = null;
    BlockPosition top, east;
    int column;
    PlayerPose pose;
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1},
          new int[] {1, 345}, new int[] {32, 1}, new int[] {0, 0});
      actor.connect();
      pose = actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() == 2, "compass-point inventory drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      top = foundation(initial, cx, cz);
      column = 0;
      actor.selectHeldSlot(0);
      while (
          water(initial.blockAt(local(top.x(), cx), top.y() + 1, local(top.z(), cz)).legacyId())) {
        top = place(actor, top, BlockFace.UP, 1);
        pose = actor.moveAndObserve(0D, 1D, 0D, 1).resulting();
        require(++column <= 15, "water column exceeded compass-point fixture");
      }
      for (int lift = 0; lift < 8; lift++) {
        top = place(actor, top, BlockFace.UP, 1);
        pose = actor.moveAndObserve(0D, 1D, 0D, 1).resulting();
        column++;
      }
      east = place(actor, top, BlockFace.EAST, 1);
      actor.selectHeldSlot(1);
      requireHeld(actor.inventory());
      B173CompassPoint spawn = B173CompassPoint.read(workspace.resolve("world/level.dat"));
      String cell0 = spawn.cell(pose);
      pose = actor
                 .moveAndObserve((east.x() + 0.5D) - pose.x(), (east.y() + 1.0D) - pose.y(),
                     (east.z() + 0.5D) - pose.z(), 4)
                 .resulting();
      String cell1 = spawn.cell(pose);
      require(!cell0.equals(cell1) && floor(pose.x()) == east.x() && floor(pose.z()) == east.z(),
          "compass positions did not cover two cells");
      requireHeld(actor.inventory());
      actor.close();
      awaitPlayers(server, 0);
      server.save();
      require(server.player(user).inventoryItems() == 2, "compass persistence count drift");
      reader = new B173WireClient("127.0.0.1", port, user, timeout);
      reader.connect();
      reader.synchronizePose();
      requireHeld(reader.awaitInventory());
      RemoteChunkSnapshot after = reader.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      require(after.blockAt(local(top.x(), cx), top.y(), local(top.z(), cz))
                  .equals(new BlockState(1, 0))
              && after.blockAt(local(east.x(), cx), east.y(), local(east.z(), cz))
                  .equals(new BlockState(1, 0)),
          "persisted compass-point stone drift");
      String evidence = "column=" + column + ",support=" + top.x() + ":" + top.y() + ":" + top.z()
          + ":1:0,east=" + east.x() + ":" + east.y() + ":" + east.z()
          + ":1:0,compass=345,held=345,positions=2,spawn=level.dat,persisted=true,clients=2,disconnect=clean";
      String trace = "v2|server=official-b1.7.3|seed=" + seed
          + "|fixture=raised-stone+held-compass345|cause=packet16-hold345+packet13-east-cell|wire=level.dat-SpawnXYZ+packet103-compass345|oracle=spawn-read+held-compass+persistence-only|client-compass=separate-official-differential|"
          + evidence;
      System.out.println("WORLDLINE_M365_SPAWN=" + spawn.token());
      System.out.println("WORLDLINE_M365_SET=" + evidence);
      System.out.println("WORLDLINE_M365_TRACE=" + trace);
      System.out.println("WORLDLINE_M365_SIGNATURE=" + sha(trace));
    } finally {
      actor.close();
      if (reader != null)
        reader.close();
      server.close();
    }
  }
  private static void requireHeld(RemoteInventoryView view) {
    require(!view.slot(37).empty() && view.slot(37).item().equals(B173CompassPoint.COMPASS)
            && B173CompassPoint.COMPASS.legacyId() == 345,
        "held compass 345 drift");
  }
  private static BlockPosition foundation(RemoteChunkSnapshot q, int cx, int cz) {
    for (int x = 4; x <= 11; x++)
      for (int z = 4; z <= 11; z++)
        for (int y = 126; y >= 1; y--)
          if (q.blockAt(x, y, z).legacyId() == 3 && water(q.blockAt(x, y + 1, z).legacyId()))
            return new BlockPosition(cx * 16 + x, y, cz * 16 + z);
    throw new IllegalStateException("no deterministic compass-point foundation");
  }
  private static int floor(double v) {
    return (int) Math.floor(v);
  }
  private static void require(boolean v, String m) {
    if (!v)
      throw new IllegalStateException(m);
  }
}
