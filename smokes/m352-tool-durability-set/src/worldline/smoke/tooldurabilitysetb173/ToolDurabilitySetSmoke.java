package worldline.smoke.tooldurabilitysetb173;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Packet14-breaks cobble 4 and stone 1 with wooden, iron, and gold picks, freezing remaining held-stack damage. */
public final class ToolDurabilitySetSmoke {
  private static final BlockState AIR = new BlockState(0, 0), STONE = new BlockState(1, 0),
                                  COBBLE = new BlockState(4, 0);
  private ToolDurabilitySetSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 7)
      throw new IllegalArgumentException(
          "usage: ToolDurabilitySetSmoke server.jar workspace port seed username chunkX chunkZ");
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
    BlockPosition top, cobble, stone, goldCobble;
    int column;
    RemoteItemStack wood, iron, gold;
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1, 2, 3, 4},
          new int[] {1, 4, 270, 257, 285}, new int[] {32, 2, 1, 1, 1}, new int[] {0, 0, 0, 0, 0});
      actor.connect();
      actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() == 5 && id(actor.inventory(), 38) == 270
              && id(actor.inventory(), 39) == 257 && id(actor.inventory(), 40) == 285,
          "tool-durability-set inventory drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      top = foundation(initial, cx, cz);
      column = 0;
      actor.selectHeldSlot(0);
      while (
          water(initial.blockAt(local(top.x(), cx), top.y() + 1, local(top.z(), cz)).legacyId())) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        require(++column <= 15, "water column exceeded tool-durability-set fixture");
      }
      for (int lift = 0; lift < 8; lift++) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        column++;
      }
      actor.selectHeldSlot(1);
      cobble = place(actor, top, BlockFace.UP, 4);
      goldCobble = place(actor, top, BlockFace.WEST, 4);
      actor.selectHeldSlot(0);
      stone = place(actor, top, BlockFace.EAST, 1);
      RemoteWorldView live = worldline.test.WorldlineSmokeAwait.observe(actor, 5);
      require(live.blockAt(cobble.x(), cobble.y(), cobble.z()).equals(COBBLE)
              && live.blockAt(stone.x(), stone.y(), stone.z()).equals(STONE)
              && live.blockAt(goldCobble.x(), goldCobble.y(), goldCobble.z()).equals(COBBLE),
          "live cobble/stone fixture drift");
      actor.selectHeldSlot(2);
      harvest(actor, cobble, 50);
      wood = worn(actor, 270);
      actor.selectHeldSlot(3);
      harvest(actor, stone, 20);
      iron = worn(actor, 257);
      actor.selectHeldSlot(4);
      harvest(actor, goldCobble, 15);
      gold = worn(actor, 285);
      require(wood.damage() > 0 && iron.damage() > 0 && gold.damage() > 0 && wood.legacyId() == 270
              && iron.legacyId() == 257 && gold.legacyId() == 285,
          "held-stack durability damage absent");
      actor.close();
      awaitPlayers(server, 0);
      server.save();
      reader = new B173WireClient("127.0.0.1", port, user, timeout);
      reader.connect();
      reader.synchronizePose();
      RemoteInventoryView after = reader.awaitInventory();
      require(held(after, 270).equals(wood) && held(after, 257).equals(iron)
              && held(after, 285).equals(gold),
          "persisted held-stack durability drift");
      RemoteChunkSnapshot world = reader.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      require(world.blockAt(local(top.x(), cx), top.y(), local(top.z(), cz)).equals(STONE)
              && world.blockAt(local(cobble.x(), cx), cobble.y(), local(cobble.z(), cz)).equals(AIR)
              && world.blockAt(local(stone.x(), cx), stone.y(), local(stone.z(), cz)).equals(AIR)
              && world.blockAt(local(goldCobble.x(), cx), goldCobble.y(), local(goldCobble.z(), cz))
                  .equals(AIR),
          "persisted cobble/stone air leftover drift");
      String evidence = "column=" + column + ",support=" + cell(top, 1, 0)
          + ",cobble=" + cell(cobble, 4, 0) + "->0:0,stone=" + cell(stone, 1, 0)
          + "->0:0,goldcobble=" + cell(goldCobble, 4, 0) + "->0:0,wood=" + wood.legacyId() + ":"
          + wood.damage() + ",iron=" + iron.legacyId() + ":" + iron.damage() + ",gold="
          + gold.legacyId() + ":" + gold.damage() + ",persisted=true,clients=2,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=raised-stone+cobble4+stone1+cobble4|cause=packet14-woodpick270+ironpick257+goldpick285|wire=packet53-air+packet103-"
          + wood.legacyId() + ":" + wood.damage() + "+" + iron.legacyId() + ":" + iron.damage()
          + "+" + gold.legacyId() + ":" + gold.damage()
          + "|oracle=held-stack-durability+fresh-login|" + evidence;
      System.out.println("WORLDLINE_M352_SET=" + evidence);
      System.out.println("WORLDLINE_M352_TRACE=" + trace);
      System.out.println("WORLDLINE_M352_SIGNATURE=" + sha(trace));
    } finally {
      actor.close();
      if (reader != null)
        reader.close();
      server.close();
    }
  }
  private static void harvest(B173WireClient a, BlockPosition target, int ticks) throws Exception {
    a.beginBreak(target);
    worldline.test.WorldlineSmokeAwait.observe(a, ticks);
    a.finishBreak(target);
    a.awaitBlock(target, AIR);
  }
  private static RemoteItemStack worn(B173WireClient a, int id) {
    return worldline.test.WorldlineSmokeAwait.awaitEntity(a,
        ()
            -> find(a.inventory(), id),
        item -> item != null && item.damage() > 0, "held " + id + " durability damage", 40);
  }
  private static RemoteItemStack held(RemoteInventoryView view, int id) {
    RemoteItemStack item = find(view, id);
    if (item == null)
      throw new IllegalStateException("persisted " + id + " absent");
    return item;
  }
  private static RemoteItemStack find(RemoteInventoryView view, int id) {
    for (int slot = 0; slot < view.size(); slot++)
      if (!view.slot(slot).empty() && view.slot(slot).item().legacyId() == id)
        return view.slot(slot).item();
    return null;
  }
  private static int id(RemoteInventoryView view, int slot) {
    return view.slot(slot).empty() ? -1 : view.slot(slot).item().legacyId();
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
    throw new IllegalStateException("no deterministic tool-durability-set foundation");
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
