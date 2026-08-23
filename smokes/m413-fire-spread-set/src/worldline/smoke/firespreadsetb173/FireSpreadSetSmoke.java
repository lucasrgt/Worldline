package worldline.smoke.firespreadsetb173;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Ignites netherrack fire 51 and proves that flame spreads onto adjacent planks 5, leaves 18, and wool 35. */
public final class FireSpreadSetSmoke {
  private FireSpreadSetSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 7)
      throw new IllegalArgumentException(
          "usage: FireSpreadSetSmoke server.jar workspace port seed username chunkX chunkZ");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    int cx = Integer.parseInt(a[5]), cz = Integer.parseInt(a[6]);
    Duration timeout = Duration.ofMinutes(20);
    require(user.length() <= 16, "username exceeds 16");
    B173DedicatedServer server =
        new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout), reader = null;
    BlockPosition top, rack, flame, plank, leaf, wool, plankFire, leafFire, woolFire;
    int column;
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D,
          new int[] {0, 1, 2, 3, 4, 5, 6}, new int[] {1, 87, 259, 5, 18, 35, 17},
          new int[] {64, 1, 1, 1, 1, 1, 1}, new int[] {0, 0, 0, 0, 0, 0, 0});
      actor.connect();
      actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() == 7, "fire-spread inventory drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      top = foundation(initial, cx, cz);
      column = 0;
      actor.selectHeldSlot(0);
      while (
          water(initial.blockAt(local(top.x(), cx), top.y() + 1, local(top.z(), cz)).legacyId())) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        require(++column <= 15, "water column exceeded fire-spread fixture");
      }
      for (int lift = 0; lift < 8; lift++) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        column++;
      }
      actor.selectHeldSlot(1);
      rack = place(actor, top, BlockFace.UP, 87);
      flame = BlockFace.UP.adjacent(rack);
      actor.selectHeldSlot(0);
      BlockPosition east = place(actor, rack, BlockFace.EAST, 1),
                    south = place(actor, rack, BlockFace.SOUTH, 1),
                    west = place(actor, rack, BlockFace.WEST, 1);
      actor.selectHeldSlot(5);
      wool = place(actor, east, BlockFace.UP, 35);
      actor.selectHeldSlot(4);
      leaf = place(actor, south, BlockFace.UP, 18);
      actor.selectHeldSlot(6);
      place(actor, leaf, BlockFace.SOUTH, 17);
      actor.selectHeldSlot(3);
      plank = place(actor, west, BlockFace.UP, 5);
      plankFire = plank;
      leafFire = leaf;
      woolFire = wool;
      RemoteWorldView placed = worldline.test.WorldlineSmokeAwait.observe(actor, 1);
      require(id(placed, plank) == 5 && id(placed, leaf) == 18 && id(placed, wool) == 35,
          "fuel cells missing before ignition");
      actor.selectHeldSlot(2);
      actor.useHeldItemOnBlock(rack, BlockFace.UP);
      actor.awaitBlock(flame, new BlockState(51, 0));
      actor.moveAndObserve(8D, 0D, 0D, 8);
      waitSpread(actor, flame, plank, leaf, wool);
      actor.close();
      awaitPlayers(server, 0);
      server.save();
      reader = new B173WireClient("127.0.0.1", port, user, timeout);
      reader.connect();
      reader.synchronizePose();
      RemoteChunkSnapshot after = reader.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      int flameId = after.blockAt(local(flame.x(), cx), flame.y(), local(flame.z(), cz)).legacyId(),
          plankId = after.blockAt(local(plank.x(), cx), plank.y(), local(plank.z(), cz)).legacyId(),
          leafId = after.blockAt(local(leaf.x(), cx), leaf.y(), local(leaf.z(), cz)).legacyId(),
          woolId = after.blockAt(local(wool.x(), cx), wool.y(), local(wool.z(), cz)).legacyId();
      require(flameId == 51
              && after.blockAt(local(rack.x(), cx), rack.y(), local(rack.z(), cz))
                  .equals(new BlockState(87, 0)),
          "netherrack fire persist drift after spread");
      require((plankId == 0 || plankId == 51) && (leafId == 0 || leafId == 51)
              && (woolId == 0 || woolId == 51),
          "relogin fuels not consumed or ignited plank=" + plankId + " leaf=" + leafId
              + " wool=" + woolId);
      String evidence = "column=" + column + ",support=" + cell(top) + ":1:0,rack=" + cell(rack)
          + ":87:0,flint=259,source-fire=" + cell(flame) + ":51,plank-fire=" + cell(plankFire)
          + ":51,leaf-fire=" + cell(leafFire) + ":51,wool-fire=" + cell(woolFire)
          + ":51,fuels=5+18+35,spread-steps=3,clients=2,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=raised-netherrack87+planks5+leaves18+wool35+flintsteel259|cause=packet15-item259+scheduled-fire-ticks|wire=packet53-fire51-multi-cell|oracle=live-source-fire51+adjacent-plank-leaf-wool-spread+fresh-login|"
          + evidence;
      System.out.println("WORLDLINE_M413_SET=" + evidence);
      System.out.println("WORLDLINE_M413_TRACE=" + trace);
      System.out.println("WORLDLINE_M413_SIGNATURE=" + sha(trace));
    } finally {
      actor.close();
      if (reader != null)
        reader.close();
      server.close();
    }
  }
  private static void waitSpread(B173WireClient a, BlockPosition flame, BlockPosition plank,
      BlockPosition leaf, BlockPosition wool) throws Exception {
    worldline.test.WorldlineSmokeAwait.awaitWorld(a,
        v
        -> id(v, flame) == 51 && burning(v, plank) && burning(v, leaf) && burning(v, wool),
        "plank leaf wool fire spread", 4800);
  }
  private static boolean burning(RemoteWorldView v, BlockPosition p) {
    int value = id(v, p);
    return value == 0 || value == 51;
  }
  private static BlockPosition place(
      B173WireClient a, BlockPosition support, BlockFace face, int id) throws Exception {
    BlockPosition target = face.adjacent(support);
    a.placeHeldBlock(support, face);
    a.awaitBlock(target, new BlockState(id, id == 18 ? 8 : 0));
    return target;
  }
  private static BlockPosition foundation(RemoteChunkSnapshot q, int cx, int cz) {
    for (int x = 4; x <= 11; x++)
      for (int z = 4; z <= 11; z++)
        for (int y = 126; y >= 1; y--)
          if (q.blockAt(x, y, z).legacyId() == 3 && water(q.blockAt(x, y + 1, z).legacyId()))
            return new BlockPosition(cx * 16 + x, y, cz * 16 + z);
    throw new IllegalStateException("no deterministic fire-spread foundation");
  }
  private static int id(RemoteWorldView v, BlockPosition p) {
    return v.blockAt(p.x(), p.y(), p.z()).legacyId();
  }
  private static String cell(BlockPosition p) {
    return p.x() + ":" + p.y() + ":" + p.z();
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
