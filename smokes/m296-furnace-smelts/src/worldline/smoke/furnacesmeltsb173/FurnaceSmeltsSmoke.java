package worldline.smoke.furnacesmeltsb173;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Official furnace family: iron 15→265, gold 14→266, and pork 319→320 in three idle 61:2 furnaces. */
public final class FurnaceSmeltsSmoke {
  private FurnaceSmeltsSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 7)
      throw new IllegalArgumentException(
          "usage: FurnaceSmeltsSmoke server.jar workspace port seed username chunkX chunkZ");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    int cx = Integer.parseInt(a[5]), cz = Integer.parseInt(a[6]);
    Duration timeout = Duration.ofSeconds(180);
    B173DedicatedServer server =
        new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout);
    RemoteItemStack iron = new RemoteItemStack(15, 1, 0), gold = new RemoteItemStack(14, 1, 0),
                    pork = new RemoteItemStack(319, 1, 0), coal = new RemoteItemStack(263, 1, 0);
    RemoteItemStack ironOut = new RemoteItemStack(265, 1, 0),
                    goldOut = new RemoteItemStack(266, 1, 0),
                    porkOut = new RemoteItemStack(320, 1, 0), glass = new RemoteItemStack(20, 1, 0);
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D,
          new int[] {0, 1, 2, 3, 4, 5, 6, 7}, new int[] {1, 61, 15, 14, 319, 263, 263, 263},
          new int[] {32, 3, 1, 1, 1, 1, 1, 1}, new int[] {0, 0, 0, 0, 0, 0, 0, 0});
      actor.connect();
      actor.synchronizePose();
      RemoteInventoryView inv = actor.awaitInventory();
      require(inv.occupiedSlots() == 8 && inv.slot(38).item().equals(iron)
              && inv.slot(39).item().equals(gold) && inv.slot(40).item().equals(pork),
          "furnace smelts inventory drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      BlockPosition top = foundation(initial, cx, cz);
      int column = 0;
      actor.selectHeldSlot(0);
      while (
          water(initial.blockAt(local(top.x(), cx), top.y() + 1, local(top.z(), cz)).legacyId())) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        require(++column <= 15, "water column exceeded furnace smelts");
      }
      for (int lift = 0; lift < 8; lift++) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        column++;
      }
      actor.selectHeldSlot(1);
      BlockPosition first = placeFurnace(actor, top, BlockFace.UP);
      actor.selectHeldSlot(0);
      BlockPosition pad = place(actor, top, BlockFace.EAST, 1);
      actor.selectHeldSlot(1);
      BlockPosition second = placeFurnace(actor, pad, BlockFace.UP);
      actor.selectHeldSlot(0);
      BlockPosition pad2 = place(actor, pad, BlockFace.EAST, 1);
      actor.selectHeldSlot(1);
      BlockPosition third = placeFurnace(actor, pad2, BlockFace.UP);
      RemoteFurnaceSmelt ironSmelt = smelt(actor, first, 38, 41, iron, ironOut, glass);
      RemoteFurnaceSmelt goldSmelt = smelt(actor, second, 39, 42, gold, goldOut, glass);
      RemoteFurnaceSmelt porkSmelt = smelt(actor, third, 40, 43, pork, porkOut, glass);
      String evidence = "column=" + column + ",support=" + top.x() + ":" + top.y() + ":" + top.z()
          + ":1:0,furnaces=3x61:2,iron=15->265,gold=14->266,pork=319->320,cook=199,burn=1600,completion=1401,clients=1,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=three-furnace61:2+ore15+ore14+pork319+coal263|cause=packet102-load-each-recipe|wire=packet103-outputs-265+266+320|oracle=idle-61:2+live-family-not-glass20|"
          + evidence + "|ironCook=" + ironSmelt.maximumCook() + "|goldOut="
          + goldSmelt.output().legacyId() + "|porkOut=" + porkSmelt.output().legacyId();
      System.out.println("WORLDLINE_M296_SMELTS=" + evidence);
      System.out.println("WORLDLINE_M296_TRACE=" + trace);
      System.out.println("WORLDLINE_M296_SIGNATURE=" + sha(trace));
    } finally {
      actor.close();
      server.close();
    }
  }
  private static RemoteFurnaceSmelt smelt(B173WireClient actor, BlockPosition furnace,
      int inputSlot, int fuelSlot, RemoteItemStack input, RemoteItemStack output,
      RemoteItemStack glass) throws Exception {
    actor.selectHeldSlot(1);
    RemoteContainerWindow opened = actor.openFurnace(furnace, BlockFace.UP);
    require(opened.descriptor().kind() == RemoteWindowKind.FURNACE
            && opened.inventory().size() == 39
            && opened.inventory().slot(inputSlot - 6).item().equals(input)
            && opened.inventory().slot(0).empty(),
        "furnace open mapping drifted");
    RemoteFurnaceLoad load = actor.loadFurnace(inputSlot, fuelSlot);
    require(load.input().equals(input) && load.fuel().equals(new RemoteItemStack(263, 1, 0)),
        "accepted furnace load drifted");
    worldline.test.WorldlineSmokeAwait.observe(actor, 5);
    RemoteFurnaceSmelt smelt = actor.awaitFurnaceSmelt();
    require(smelt.output().equals(output) && !smelt.output().equals(glass)
            && smelt.maximumCook() == 199
            && smelt.window().inventory().slot(2).item().equals(output),
        "completed furnace family smelt drifted");
    actor.closeWindow();
    return smelt;
  }
  private static BlockPosition placeFurnace(B173WireClient a, BlockPosition support, BlockFace face)
      throws Exception {
    BlockPosition target = face.adjacent(support);
    a.placeHeldBlock(support, face);
    a.awaitBlock(target, new BlockState(61, 2));
    require(worldline.test.WorldlineSmokeAwait.observe(a, 2)
                .blockAt(target.x(), target.y(), target.z())
                .equals(new BlockState(61, 2)),
        "idle furnace 61:2 drift");
    return target;
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
    throw new IllegalStateException("no deterministic furnace smelts foundation");
  }
  private static boolean water(int id) {
    return id == 8 || id == 9;
  }
  private static int local(int v, int c) {
    return v - c * 16;
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
