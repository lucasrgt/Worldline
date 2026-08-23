package worldline.smoke.fragilesetb173;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Packet14-breaks ice 79 and glass 20, then torch-melts a second ice to water. */
public final class FragileSetSmoke {
  private static final RemoteItemStack GLASS = new RemoteItemStack(20, 1, 0);
  private static final BlockState AIR = new BlockState(0, 0), ICE = new BlockState(79, 0),
                                  STONE = new BlockState(1, 0), TORCH = new BlockState(50, 5),
                                  GLASS_BLOCK = new BlockState(20, 0);
  private FragileSetSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 9)
      throw new IllegalArgumentException(
          "usage: FragileSetSmoke server.jar workspace port seed username chunkX chunkZ windowTicks meltWindows");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    int cx = Integer.parseInt(a[5]), cz = Integer.parseInt(a[6]), window = Integer.parseInt(a[7]),
        windows = Integer.parseInt(a[8]);
    Duration timeout = Duration.ofMinutes(12);
    require(user.length() <= 16 && window >= 1 && window <= 1200 && windows >= 1 && windows <= 8,
        "fragile-set arguments");
    B173DedicatedServer server =
        new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout), reader = null;
    BlockPosition top, west, ice, pad, torch, north, riser, glass, melt;
    int column;
    BlockState broken, melted = null, afterIce, afterGlass, afterMelt, afterTorch;
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1, 2, 3},
          new int[] {1, 79, 20, 50}, new int[] {32, 2, 1, 1}, new int[] {0, 0, 0, 0});
      actor.connect();
      actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() == 4, "fragile-set inventory drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      top = foundation(initial, cx, cz);
      column = 0;
      actor.selectHeldSlot(0);
      while (
          water(initial.blockAt(local(top.x(), cx), top.y() + 1, local(top.z(), cz)).legacyId())) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        require(++column <= 15, "water column exceeded fragile-set fixture");
      }
      for (int lift = 0; lift < 8; lift++) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        column++;
      }
      west = place(actor, top, BlockFace.WEST, 1);
      north = place(actor, top, BlockFace.NORTH, 1);
      riser = place(actor, north, BlockFace.UP, 1);
      actor.selectHeldSlot(1);
      ice = place(actor, west, BlockFace.UP, 79);
      require(worldline.test.WorldlineSmokeAwait.observe(actor, 5)
                  .blockAt(ice.x(), ice.y(), ice.z())
                  .equals(ICE),
          "live ice-break drift");
      harvest(actor, ice, 80);
      broken =
          worldline.test.WorldlineSmokeAwait.observe(actor, 20).blockAt(ice.x(), ice.y(), ice.z());
      require(broken.legacyId() != 79, "ice cell still 79 after Packet14: " + broken);
      actor.selectHeldSlot(2);
      glass = place(actor, riser, BlockFace.UP, 20);
      require(worldline.test.WorldlineSmokeAwait.observe(actor, 5)
                  .blockAt(glass.x(), glass.y(), glass.z())
                  .equals(GLASS_BLOCK),
          "live glass drift");
      harvest(actor, glass, 20);
      actor.awaitBlock(glass, AIR);
      require(actor.peekDroppedItem(GLASS) == null, "Packet21 glass drop");
      require(worldline.test.WorldlineSmokeAwait.observe(actor, 10)
                  .blockAt(glass.x(), glass.y(), glass.z())
                  .equals(AIR)
              && actor.peekDroppedItem(GLASS) == null,
          "live glass air drift");
      actor.selectHeldSlot(0);
      pad = place(actor, top, BlockFace.EAST, 1);
      actor.selectHeldSlot(1);
      melt = place(actor, top, BlockFace.UP, 79);
      actor.selectHeldSlot(3);
      torch = BlockFace.UP.adjacent(pad);
      actor.placeHeldBlock(pad, BlockFace.UP);
      actor.awaitBlock(torch, TORCH);
      require(worldline.test.WorldlineSmokeAwait.observe(actor, 5)
                  .blockAt(melt.x(), melt.y(), melt.z())
                  .equals(ICE),
          "live melt ice vanished before torch wait");
      melted = worldline.test.WorldlineSmokeAwait.awaitBlockMatching(
          actor, melt, s -> water(s.legacyId()), "melted ice", windows * window);
      RemoteWorldView end = worldline.test.WorldlineSmokeAwait.observe(actor, 20);
      if (melted != null)
        melted = end.blockAt(melt.x(), melt.y(), melt.z());
      broken = end.blockAt(ice.x(), ice.y(), ice.z());
      require(broken.legacyId() != 79 && end.blockAt(glass.x(), glass.y(), glass.z()).equals(AIR),
          "fragile leftover drift during melt wait");
      actor.close();
      awaitPlayers(server, 0);
      server.save();
      reader = new B173WireClient("127.0.0.1", port, user, timeout);
      reader.connect();
      reader.synchronizePose();
      RemoteChunkSnapshot after = reader.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      afterIce = after.blockAt(local(ice.x(), cx), ice.y(), local(ice.z(), cz));
      afterGlass = after.blockAt(local(glass.x(), cx), glass.y(), local(glass.z(), cz));
      afterMelt = after.blockAt(local(melt.x(), cx), melt.y(), local(melt.z(), cz));
      afterTorch = after.blockAt(local(torch.x(), cx), torch.y(), local(torch.z(), cz));
      if (!water(afterMelt.legacyId()))
        throw new IllegalStateException("BLOCKED ice did not melt cell=" + afterMelt
            + ",live=" + (melted == null ? "ice79" : melted) + ",torch=" + afterTorch);
      require(after.blockAt(local(top.x(), cx), top.y(), local(top.z(), cz)).equals(STONE)
              && after.blockAt(local(west.x(), cx), west.y(), local(west.z(), cz)).equals(STONE)
              && after.blockAt(local(pad.x(), cx), pad.y(), local(pad.z(), cz)).equals(STONE)
              && afterIce.equals(broken) && afterGlass.equals(AIR) && afterTorch.equals(TORCH),
          "persisted fragile-set leftover drift");
      String evidence = "column=" + column + ",support=" + cell(top, 1, 0)
          + ",west=" + cell(west, 1, 0) + ",ice=" + cell(ice, 79, 0) + "->" + afterIce.legacyId()
          + ":" + afterIce.metadata() + ",pad=" + cell(pad, 1, 0) + ",torch=" + cell(torch, 50, 5)
          + ",melt=" + cell(melt, 79, 0) + "->" + afterMelt.legacyId() + ":" + afterMelt.metadata()
          + ",north=" + cell(north, 1, 0) + ",riser=" + cell(riser, 1, 0)
          + ",glass=" + cell(glass, 20, 0)
          + "->0:0,drop=no-packet21-20,persisted=true,clients=2,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=raised-stone+ice79-break+glass20-break+ice79-torch-melt|cause=packet14-break-ice79+packet14-break-glass20+packet15-item50|wire=packet53-ice79:0->"
          + afterIce.legacyId() + ":" + afterIce.metadata()
          + "+packet53-glass20:0->0:0+packet53-melt79:0->water" + afterMelt.legacyId() + ":"
          + afterMelt.metadata()
          + "|oracle=live-leftover+no-packet21-glass20+official-random-tick-melt+fresh-login|"
          + evidence;
      System.out.println("WORLDLINE_M308_SET=" + evidence);
      System.out.println("WORLDLINE_M308_TRACE=" + trace);
      System.out.println("WORLDLINE_M308_SIGNATURE=" + sha(trace));
    } finally {
      actor.close();
      if (reader != null)
        reader.close();
      server.close();
    }
  }
  private static void harvest(B173WireClient a, BlockPosition target, int ticks) {
    a.beginBreak(target);
    worldline.test.WorldlineSmokeAwait.observe(a, ticks);
    a.finishBreak(target);
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
    throw new IllegalStateException("no deterministic fragile-set foundation");
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
