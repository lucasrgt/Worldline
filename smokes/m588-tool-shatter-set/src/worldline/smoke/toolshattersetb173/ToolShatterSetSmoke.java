package worldline.smoke.toolshattersetb173;
import static worldline.b173server.B173FixtureSupport.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Packet14-breaks cobble 4 with wooden pickaxe 270 at last remaining durability, freezing empty-hand shatter. */
public final class ToolShatterSetSmoke {
  private static final BlockState AIR = new BlockState(0, 0), STONE = new BlockState(1, 0),
                                  COBBLE = new BlockState(4, 0);
  private ToolShatterSetSmoke() {}
  public static void main(String[] a) throws Exception {
    if (a.length != 7)
      throw new IllegalArgumentException(
          "usage: ToolShatterSetSmoke server.jar workspace port seed username chunkX chunkZ");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    int cx = Integer.parseInt(a[5]), cz = Integer.parseInt(a[6]);
    Duration timeout = Duration.ofSeconds(90);
    require(seed == 17320110707L && user.equals("ToolShatr588") && user.length() <= 16, "tool-shatter identity drift");
    B173DedicatedServer server = new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout), reader = null;
    BlockPosition top, cobble;
    int column;
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1, 2, 3},
          new int[] {1, 4, 270, 270}, new int[] {32, 2, 1, 1}, new int[] {0, 0, 59, 0});
      actor.connect();
      actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() == 4 && id(actor.inventory(), 38) == 270
              && dmg(actor.inventory(), 38) == 59 && id(actor.inventory(), 39) == 270
              && dmg(actor.inventory(), 39) == 0,
          "tool-shatter inventory drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      top = foundation(initial, cx, cz);
      column = 0;
      actor.selectHeldSlot(0);
      while (water(initial.blockAt(local(top.x(), cx), top.y() + 1, local(top.z(), cz)).legacyId())) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        require(++column <= 15, "water column exceeded tool-shatter fixture");
      }
      for (int lift = 0; lift < 8; lift++) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        column++;
      }
      actor.selectHeldSlot(1);
      cobble = place(actor, top, BlockFace.UP, 4);
      RemoteWorldView live = worldline.test.WorldlineSmokeAwait.observe(actor, 5);
      require(live.blockAt(cobble.x(), cobble.y(), cobble.z()).equals(COBBLE), "live cobble fixture drift");
      actor.selectHeldSlot(2);
      harvest(actor, cobble, 50);
      worldline.test.WorldlineSmokeAwait.awaitEntity(
          actor, () -> actor.inventory(), inv -> inv.slot(38).empty(), "held wooden pickaxe shatter", 40);
      RemoteItemStack control = held(actor.inventory(), 270);
      require(control.damage() == 0 && control.count() == 1 && actor.inventory().slot(38).empty(),
          "held-stack shatter absent");
      actor.close();
      awaitPlayers(server, 0);
      server.save();
      reader = new B173WireClient("127.0.0.1", port, user, timeout);
      reader.connect();
      reader.synchronizePose();
      RemoteInventoryView after = reader.awaitInventory();
      require(after.slot(38).empty() && held(after, 270).equals(control), "persisted empty-hand shatter drift");
      RemoteChunkSnapshot world = reader.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      require(world.blockAt(local(top.x(), cx), top.y(), local(top.z(), cz)).equals(STONE)
              && world.blockAt(local(cobble.x(), cx), cobble.y(), local(cobble.z(), cz)).equals(AIR),
          "persisted cobble air leftover drift");
      String evidence = "column=" + column + ",support=" + cell(top, 1, 0) + ",cobble=" + cell(cobble, 4, 0)
          + "->0:0,wood=270:59->empty,control=270:0,persisted=true,clients=2,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + ("|fixture=raised-stone+cobble4+woodpick270-damage59|cause=packet14-woodpick270-last-use|wire=packet53-air+"
              + "packet103-empty|oracle=held-stack-shatter+fresh-login|")
          + evidence;
      System.out.println("WORLDLINE_M588_SET=" + evidence);
      System.out.println("WORLDLINE_M588_TRACE=" + trace);
      System.out.println("WORLDLINE_M588_SIGNATURE=" + sha(trace));
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
  private static int dmg(RemoteInventoryView view, int slot) {
    return view.slot(slot).empty() ? -1 : view.slot(slot).item().damage();
  }
  private static BlockPosition foundation(RemoteChunkSnapshot q, int cx, int cz) {
    for (int x = 4; x <= 11; x++)
      for (int z = 4; z <= 11; z++)
        for (int y = 126; y >= 1; y--)
          if (q.blockAt(x, y, z).legacyId() == 3 && water(q.blockAt(x, y + 1, z).legacyId()))
            return new BlockPosition(cx * 16 + x, y, cz * 16 + z);
    throw new IllegalStateException("no deterministic tool-shatter foundation");
  }
  private static String cell(BlockPosition p, int id, int meta) {
    return p.x() + ":" + p.y() + ":" + p.z() + ":" + id + ":" + meta;
  }
  private static void require(boolean v, String m) {
    if (!v)
      throw new IllegalStateException(m);
  }
}
