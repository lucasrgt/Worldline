package worldline.smoke.diamondarmorcraftsb173;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Places a workbench on raised stone and crafts the diamond armor family from gems 264. */
public final class DiamondArmorCraftsSmoke {
  private DiamondArmorCraftsSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 7)
      throw new IllegalArgumentException(
          "usage: DiamondArmorCraftsSmoke server.jar workspace port seed username chunkX chunkZ");
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
    BlockState placed;
    int[] crafted;
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1, 2},
          new int[] {1, 58, 264}, new int[] {32, 1, 24}, new int[] {0, 0, 0});
      actor.connect();
      actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() == 3, "diamond armor craft inventory drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      top = foundation(initial, cx, cz);
      column = 0;
      actor.selectHeldSlot(0);
      while (
          water(initial.blockAt(local(top.x(), cx), top.y() + 1, local(top.z(), cz)).legacyId())) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        require(++column <= 15, "water column exceeded diamond armor craft fixture");
      }
      for (int lift = 0; lift < 8; lift++) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        column++;
      }
      actor.selectHeldSlot(1);
      bench = BlockFace.UP.adjacent(top);
      actor.placeHeldBlock(top, BlockFace.UP);
      placed = new BlockState(58, 0);
      actor.awaitBlock(bench, placed);
      require(actor.sustainTicks(5).blockAt(bench.x(), bench.y(), bench.z()).equals(placed)
              && actor.inventory().slot(38).item().equals(new RemoteItemStack(264, 24, 0)),
          "live workbench or diamond drift");
      actor.selectHeldSlot(1);
      require(actor.openWorkbench(bench, BlockFace.UP)
                  .inventory()
                  .slot(39)
                  .item()
                  .equals(new RemoteItemStack(264, 24, 0)),
          "workbench diamond mapping drifted");
      crafted = B173DiamondArmorCraftsClick.apply(actor, 38, new int[] {37, 39, 40, 41});
      require(crafted[0] == 310 && crafted[1] == 311 && crafted[2] == 312 && crafted[3] == 313,
          "diamond armor craft results drifted");
      require(storage(actor.inventory()) && unequipped(actor.inventory()),
          "crafted diamond armor window drifted");
      actor.closeWindow();
      actor.close();
      awaitPlayers(server, 0);
      server.save();
      require(server.player(user).inventoryItems() == 5,
          "crafted diamond armor persistence count drifted");
      reader = new B173WireClient("127.0.0.1", port, user, timeout);
      reader.connect();
      reader.synchronizePose();
      require(storage(reader.awaitInventory()) && unequipped(reader.inventory()),
          "persisted diamond armor craft inventory drifted");
      reader.awaitBlock(bench, placed);
      RemoteChunkSnapshot after = reader.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      require(after.blockAt(local(top.x(), cx), top.y(), local(top.z(), cz))
                  .equals(new BlockState(1, 0))
              && after.blockAt(local(bench.x(), cx), bench.y(), local(bench.z(), cz))
                  .equals(placed),
          "persisted workbench 58:0 drift");
      String evidence = "column=" + column + ",support=" + top.x() + ":" + top.y() + ":" + top.z()
          + ":1:0,workbench=" + bench.x() + ":" + bench.y() + ":" + bench.z()
          + ":58:" + placed.metadata()
          + ",crafts=310,311,312,313,ingredient=264,equipped=false,persisted=true,clients=2,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=raised-stone+workbench58+diamond264|cause=packet102-workbench-diamond-armor|wire=packet103-result-310-311-312-313|oracle=live-craft+fresh-login+unequipped|"
          + evidence;
      System.out.println("WORLDLINE_M322_CRAFTS=" + evidence);
      System.out.println("WORLDLINE_M322_TRACE=" + trace);
      System.out.println("WORLDLINE_M322_SIGNATURE=" + sha(trace));
    } finally {
      actor.close();
      if (reader != null)
        reader.close();
      server.close();
    }
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
    throw new IllegalStateException("no deterministic diamond armor craft foundation");
  }
  private static boolean storage(RemoteInventoryView v) {
    return has(v, 310) && has(v, 311) && has(v, 312) && has(v, 313) && !has(v, 264);
  }
  private static boolean has(RemoteInventoryView v, int id) {
    for (int s = 9; s <= 44; s++)
      if (!v.slot(s).empty() && v.slot(s).item().legacyId() == id)
        return true;
    return false;
  }
  private static boolean unequipped(RemoteInventoryView v) {
    return v.slot(5).empty() && v.slot(6).empty() && v.slot(7).empty() && v.slot(8).empty();
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
