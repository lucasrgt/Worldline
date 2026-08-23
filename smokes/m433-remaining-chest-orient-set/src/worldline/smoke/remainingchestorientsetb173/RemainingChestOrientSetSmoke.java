package worldline.smoke.remainingchestorientsetb173;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Places remaining chest 54 look-yaw facings plus EW and NS adjacent pairs without opening Packet100. */
public final class RemainingChestOrientSetSmoke {
  private RemainingChestOrientSetSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 7)
      throw new IllegalArgumentException(
          "usage: RemainingChestOrientSetSmoke server.jar workspace port seed username chunkX chunkZ");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    int cx = Integer.parseInt(a[5]), cz = Integer.parseInt(a[6]);
    require(seed == 17320110707L && user.equals("ChestOrnt433") && user.length() <= 16,
        "remaining-chest-orient-set identity drift");
    Duration timeout = Duration.ofSeconds(90);
    B173DedicatedServer server =
        new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout), reader = null;
    BlockPosition top, gapE, isoWPad, gapEw, ewLPad, ewRPad, gapS, nsNPad, nsSPad, isoE, isoW, ewL,
        ewR, nsN, nsS;
    int column;
    BlockState chest = new BlockState(54, 0), stone = new BlockState(1, 0);
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1},
          new int[] {1, 54}, new int[] {48, 8}, new int[] {0, 0});
      actor.connect();
      actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() == 2,
          "remaining-chest-orient-set inventory drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      top = foundation(initial, cx, cz);
      column = 0;
      actor.selectHeldSlot(0);
      while (
          water(initial.blockAt(local(top.x(), cx), top.y() + 1, local(top.z(), cz)).legacyId())) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        require(++column <= 15, "water column exceeded remaining-chest-orient-set fixture");
      }
      for (int lift = 0; lift < 8; lift++) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        column++;
      }
      gapE = place(actor, top, BlockFace.EAST, 1);
      isoWPad = place(actor, gapE, BlockFace.EAST, 1);
      gapEw = place(actor, isoWPad, BlockFace.EAST, 1);
      ewLPad = place(actor, gapEw, BlockFace.EAST, 1);
      ewRPad = place(actor, ewLPad, BlockFace.EAST, 1);
      gapS = place(actor, top, BlockFace.SOUTH, 1);
      nsNPad = place(actor, gapS, BlockFace.SOUTH, 1);
      nsSPad = place(actor, nsNPad, BlockFace.SOUTH, 1);
      actor.selectHeldSlot(1);
      isoE = chest(actor, top, -90F);
      isoW = chest(actor, isoWPad, 90F);
      ewL = chest(actor, ewLPad, 0F);
      ewR = chest(actor, ewRPad, 0F);
      nsN = chest(actor, nsNPad, 0F);
      nsS = chest(actor, nsSPad, 0F);
      RemoteWorldView live = worldline.test.WorldlineSmokeAwait.observe(actor, 5);
      require(live.blockAt(isoE.x(), isoE.y(), isoE.z()).equals(chest)
              && live.blockAt(isoW.x(), isoW.y(), isoW.z()).equals(chest)
              && live.blockAt(ewL.x(), ewL.y(), ewL.z()).equals(chest)
              && live.blockAt(ewR.x(), ewR.y(), ewR.z()).equals(chest)
              && live.blockAt(nsN.x(), nsN.y(), nsN.z()).equals(chest)
              && live.blockAt(nsS.x(), nsS.y(), nsS.z()).equals(chest) && ewR.x() == ewL.x() + 1
              && ewR.z() == ewL.z() && nsS.z() == nsN.z() + 1 && nsS.x() == nsN.x(),
          "live remaining-chest-orient-set drift");
      actor.close();
      awaitPlayers(server, 0);
      server.save();
      reader = new B173WireClient("127.0.0.1", port, user, timeout);
      reader.connect();
      reader.synchronizePose();
      RemoteChunkSnapshot after = reader.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      require(after.blockAt(local(top.x(), cx), top.y(), local(top.z(), cz)).equals(stone)
              && after.blockAt(local(isoE.x(), cx), isoE.y(), local(isoE.z(), cz)).equals(chest)
              && after.blockAt(local(isoW.x(), cx), isoW.y(), local(isoW.z(), cz)).equals(chest)
              && after.blockAt(local(ewL.x(), cx), ewL.y(), local(ewL.z(), cz)).equals(chest)
              && after.blockAt(local(ewR.x(), cx), ewR.y(), local(ewR.z(), cz)).equals(chest)
              && after.blockAt(local(nsN.x(), cx), nsN.y(), local(nsN.z(), cz)).equals(chest)
              && after.blockAt(local(nsS.x(), cx), nsS.y(), local(nsS.z(), cz)).equals(chest),
          "persisted remaining-chest-orient-set drift");
      String evidence = "column=" + column + ",support=" + cell(top, 1, 0)
          + ",iso=" + cell(isoE, 54, 0) + "+" + cell(isoW, 54, 0) + ",ew=" + cell(ewL, 54, 0) + "+"
          + cell(ewR, 54, 0) + ",ns=" + cell(nsN, 54, 0) + "+" + cell(nsS, 54, 0)
          + ",look=-90+90,persisted=true,clients=2,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=raised-stone+chest54-look-faces+ew-pair+ns-pair|cause=packet15-item54+look-90+look90+packet15-item54-ew+packet15-item54-ns|wire=packet53-chest54:0+54:0+54:0+54:0+54:0+54:0|oracle=look-facing+adjacent-pair-orientations+fresh-login|"
          + evidence;
      System.out.println("WORLDLINE_M433_SET=" + evidence);
      System.out.println("WORLDLINE_M433_TRACE=" + trace);
      System.out.println("WORLDLINE_M433_SIGNATURE=" + sha(trace));
    } finally {
      actor.close();
      if (reader != null)
        reader.close();
      server.close();
    }
  }
  private static BlockPosition chest(B173WireClient a, BlockPosition support, float yaw)
      throws Exception {
    BlockPosition target = BlockFace.UP.adjacent(support);
    a.look(yaw, 0F);
    a.placeHeldBlock(support, BlockFace.UP);
    a.awaitBlock(target, new BlockState(54, 0));
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
    throw new IllegalStateException("no deterministic remaining-chest-orient-set foundation");
  }
  private static String cell(BlockPosition p, int id, int meta) {
    return p.x() + ":" + p.y() + ":" + p.z() + ":" + id + ":" + meta;
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
