package worldline.smoke.vehiclecraftsb173;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Workbench-crafts boat 333, minecart 328, chest minecart 342, and furnace minecart 343. */
public final class VehicleCraftsSmoke {
  private VehicleCraftsSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 7)
      throw new IllegalArgumentException(
          "usage: VehicleCraftsSmoke server.jar workspace port seed username chunkX chunkZ");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    int cx = Integer.parseInt(a[5]), cz = Integer.parseInt(a[6]);
    Duration timeout = Duration.ofSeconds(90);
    B173DedicatedServer server =
        new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout), reader = null;
    BlockPosition top, bench;
    int column;
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1, 2, 3, 4, 5},
          new int[] {1, 58, 5, 265, 54, 61}, new int[] {32, 1, 5, 15, 1, 1},
          new int[] {0, 0, 0, 0, 0, 0});
      actor.connect();
      actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() == 6, "vehicle-craft inventory seed drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      top = foundation(initial, cx, cz);
      column = 0;
      actor.selectHeldSlot(0);
      while (
          water(initial.blockAt(local(top.x(), cx), top.y() + 1, local(top.z(), cz)).legacyId())) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        require(++column <= 15, "water column exceeded vehicle-craft fixture");
      }
      for (int lift = 0; lift < 8; lift++) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        column++;
      }
      actor.selectHeldSlot(1);
      bench = BlockFace.UP.adjacent(top);
      actor.placeHeldBlock(top, BlockFace.UP);
      actor.awaitBlock(bench, new BlockState(58, 0));
      worldline.test.WorldlineSmokeAwait.observe(actor, 5);
      actor.selectHeldSlot(1);
      actor.openWorkbench(bench, BlockFace.UP);
      int[] ids = B173VehicleCrafts.apply(actor);
      require(ids[0] == 333 && ids[1] == 328 && ids[2] == 342 && ids[3] == 343
              && hasFamily(actor.inventory()),
          "vehicle craft results drifted");
      actor.closeWindow();
      actor.close();
      awaitPlayers(server, 0);
      server.save();
      require(server.player(user).inventoryItems() == 5, "vehicle-craft persistence count drift");
      reader = new B173WireClient("127.0.0.1", port, user, timeout);
      reader.connect();
      reader.synchronizePose();
      require(hasFamily(reader.awaitInventory()), "persisted vehicle crafts drifted");
      RemoteChunkSnapshot after = reader.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      require(after.blockAt(local(top.x(), cx), top.y(), local(top.z(), cz))
                  .equals(new BlockState(1, 0))
              && after.blockAt(local(bench.x(), cx), bench.y(), local(bench.z(), cz))
                  .equals(new BlockState(58, 0)),
          "persisted workbench 58:0 drift");
      String evidence = "column=" + column + ",support=" + top.x() + ":" + top.y() + ":" + top.z()
          + ":1:0,workbench=" + bench.x() + ":" + bench.y() + ":" + bench.z()
          + ":58:0,crafts=333,328,342,343,persisted=true,clients=2,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=workbench58+planks5x5+ingot265x15+chest54+furnace61|cause=packet102-workbench-vehicle-family|wire=packet106-accepted+packet200-craft-stat|oracle=craft-output-333-328-342-343+fresh-login+not-ride+not-spawn|"
          + evidence;
      System.out.println("WORLDLINE_M326_CRAFTS=" + evidence);
      System.out.println("WORLDLINE_M326_TRACE=" + trace);
      System.out.println("WORLDLINE_M326_SIGNATURE=" + sha(trace));
    } finally {
      actor.close();
      if (reader != null)
        reader.close();
      server.close();
    }
  }
  private static boolean hasFamily(RemoteInventoryView view) {
    return has(view, 333) && has(view, 328) && has(view, 342) && has(view, 343) && !has(view, 5)
        && !has(view, 265) && !has(view, 54) && !has(view, 61);
  }
  private static boolean has(RemoteInventoryView view, int id) {
    for (int s = 9; s <= 44; s++)
      if (!view.slot(s).empty() && view.slot(s).item().legacyId() == id)
        return true;
    return false;
  }
  private static BlockPosition place(
      B173WireClient a, BlockPosition support, BlockFace face, int id) throws Exception {
    BlockPosition target = face.adjacent(support);
    a.placeHeldBlock(support, face);
    a.awaitBlock(target, new BlockState(id, 0));
    return target;
  }
  private static BlockPosition foundation(RemoteChunkSnapshot q, int cx, int cz) {
    for (int x = 4; x <= 11; x++)
      for (int z = 4; z <= 11; z++)
        for (int y = 126; y >= 1; y--)
          if (q.blockAt(x, y, z).legacyId() == 3 && water(q.blockAt(x, y + 1, z).legacyId()))
            return new BlockPosition(cx * 16 + x, y, cz * 16 + z);
    throw new IllegalStateException("no deterministic vehicle-craft foundation");
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
