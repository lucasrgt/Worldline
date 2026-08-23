package worldline.smoke.dispensersetb173;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Places dispenser 23, loads cobble 4 and planks 5, and proves two lever pulses eject both Packet21 stacks. */
public final class DispenserSetSmoke {
  private DispenserSetSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 7)
      throw new IllegalArgumentException(
          "usage: DispenserSetSmoke server.jar workspace port seed username chunkX chunkZ");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    int cx = Integer.parseInt(a[5]), cz = Integer.parseInt(a[6]);
    Duration timeout = Duration.ofSeconds(90);
    B173DedicatedServer server =
        new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout);
    BlockPosition top, disp, lever;
    int column;
    RemoteDroppedItem cobble, planks;
    RemoteContainerWindow remain;
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1, 2, 3, 4},
          new int[] {1, 23, 69, 4, 5}, new int[] {32, 1, 1, 1, 1}, new int[] {0, 0, 0, 0, 0});
      actor.connect();
      actor.synchronizePose();
      actor.look(-90F, 0F);
      require(actor.awaitInventory().occupiedSlots() == 5, "dispenser-set inventory drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      top = foundation(initial, cx, cz);
      column = 0;
      actor.selectHeldSlot(0);
      while (
          water(initial.blockAt(local(top.x(), cx), top.y() + 1, local(top.z(), cz)).legacyId())) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        require(++column <= 15, "water column exceeded dispenser-set fixture");
      }
      for (int lift = 0; lift < 8; lift++) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        column++;
      }
      actor.selectHeldSlot(1);
      disp = place(actor, top, BlockFace.UP, 23);
      require(
          actor.sustainTicks(5).blockAt(disp.x(), disp.y(), disp.z()).equals(new BlockState(23, 4)),
          "west dispenser facing drift");
      lever = BlockFace.EAST.adjacent(top);
      actor.selectHeldSlot(2);
      actor.placeHeldBlock(top, BlockFace.EAST);
      require(actor.sustainTicks(5)
                  .blockAt(lever.x(), lever.y(), lever.z())
                  .equals(new BlockState(69, 1)),
          "side lever drift");
      actor.selectHeldSlot(1);
      RemoteContainerWindow opened = B173DispenserWindows.open(actor, disp, BlockFace.UP);
      require(opened.descriptor().kind() == RemoteWindowKind.DISPENSER
              && "Trap".equals(opened.descriptor().title()) && opened.inventory().size() == 45
              && opened.inventory().slot(0).empty()
              && opened.inventory().slot(39).item().equals(B173DispenserSetLoads.COBBLE)
              && opened.inventory().slot(40).item().equals(B173DispenserSetLoads.PLANKS),
          "dispenser-set open mapping drifted");
      RemoteDispenserLoad load = B173DispenserWindows.load(actor, 39, 0);
      require(load.takeAction() == 1 && load.storeAction() == 2
              && load.stack().equals(B173DispenserSetLoads.COBBLE)
              && actor.inventory().slot(39).empty(),
          "accepted cobble load drifted");
      actor.closeWindow();
      actor.selectHeldSlot(1);
      pulse(actor, lever, 9);
      cobble = actor.awaitDroppedItem(B173DispenserSetLoads.COBBLE);
      require(cobble.item().equals(B173DispenserSetLoads.COBBLE) && cobble.item().count() == 1,
          "dispenser-set Packet21 cobble 4 absent");
      pulse(actor, lever, 1);
      opened = B173DispenserWindows.open(actor, disp, BlockFace.UP);
      require(opened.inventory().slot(0).empty()
              && opened.inventory().slot(40).item().equals(B173DispenserSetLoads.PLANKS),
          "planks source drifted");
      B173DispenserSetLoads.planks(actor);
      require(actor.inventory().slot(40).empty(), "accepted planks load drifted");
      actor.closeWindow();
      actor.selectHeldSlot(1);
      pulse(actor, lever, 9);
      planks = actor.awaitDroppedItem(B173DispenserSetLoads.PLANKS);
      require(planks.item().equals(B173DispenserSetLoads.PLANKS) && planks.item().count() == 1,
          "dispenser-set Packet21 planks 5 absent");
      remain = B173DispenserWindows.open(actor, disp, BlockFace.UP);
      require(remain.inventory().slot(0).empty() && remain.inventory().slot(1).empty(),
          "official ejected stacks drift");
      actor.closeWindow();
      actor.close();
      awaitPlayers(server, 0);
      server.save();
      String evidence = "column=" + column + ",disp=" + disp.x() + ":" + disp.y() + ":" + disp.z()
          + ":23:4,lever=" + lever.x() + ":" + lever.y() + ":" + lever.z()
          + ":1->9,load=4+5,ejects=4+5,drop=packet21-4+5,remain=empty,clients=1,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=raised-dispenser23-west+side-lever69|cause=packet15-item23+packet102-load-4+5+packet15-lever-activate|wire=packet53-dispenser23+packet21-4+5|oracle=official-dispenser-set-eject|"
          + evidence;
      System.out.println("WORLDLINE_M333_SET=" + evidence);
      System.out.println("WORLDLINE_M333_TRACE=" + trace);
      System.out.println("WORLDLINE_M333_SIGNATURE=" + sha(trace));
    } finally {
      actor.close();
      server.close();
    }
  }
  private static void pulse(B173WireClient a, BlockPosition lever, int meta) throws Exception {
    a.activateBlock(lever, BlockFace.UP);
    require(
        a.sustainTicks(5).blockAt(lever.x(), lever.y(), lever.z()).equals(new BlockState(69, meta)),
        "lever metadata drift " + meta);
  }
  private static BlockPosition place(
      B173WireClient a, BlockPosition support, BlockFace face, int id) throws Exception {
    BlockPosition target = face.adjacent(support);
    a.placeHeldBlock(support, face);
    a.awaitBlock(target, id == 23 ? new BlockState(23, 4) : new BlockState(id, 0));
    return target;
  }
  private static BlockPosition foundation(RemoteChunkSnapshot q, int cx, int cz) {
    for (int x = 4; x <= 11; x++)
      for (int z = 4; z <= 11; z++)
        for (int y = 126; y >= 1; y--)
          if (q.blockAt(x, y, z).legacyId() == 3 && water(q.blockAt(x, y + 1, z).legacyId()))
            return new BlockPosition(cx * 16 + x, y, cz * 16 + z);
    throw new IllegalStateException("no deterministic dispenser-set foundation");
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
