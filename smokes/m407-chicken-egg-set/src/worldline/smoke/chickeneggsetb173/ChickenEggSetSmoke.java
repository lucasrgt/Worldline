package worldline.smoke.chickeneggsetb173;
import static worldline.b173server.B173FixtureSupport.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Observes Packet24 chicken type 93 then egg 344 as bounded Packet21 and/or thrown Packet23 type 62. */
public final class ChickenEggSetSmoke {
  private static final RemoteItemStack EGG = new RemoteItemStack(344, 1, 0);
  private ChickenEggSetSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 7)
      throw new IllegalArgumentException(
          "usage: ChickenEggSetSmoke server.jar workspace port seed username chunkX chunkZ");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    int cx = Integer.parseInt(a[5]), cz = Integer.parseInt(a[6]);
    require(seed == 17320110707L && user.equals("ChickEgg407") && user.length() <= 16,
        "chicken-egg-set identity drift");
    Duration timeout = Duration.ofSeconds(90);
    B173DedicatedServer server =
        B173DedicatedServer.animals(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout);
    BlockPosition top, spawner;
    int column;
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1, 2, 3},
          new int[] {1, 2, 52, 344}, new int[] {32, 48, 1, 16}, new int[] {0, 0, 0, 0});
      actor.connect();
      actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() == 4, "chicken-egg-set inventory drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      top = foundation(initial, cx, cz);
      column = 0;
      actor.selectHeldSlot(0);
      while (
          water(initial.blockAt(local(top.x(), cx), top.y() + 1, local(top.z(), cz)).legacyId())) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        require(++column <= 15, "water column exceeded chicken-egg-set fixture");
      }
      for (int lift = 0; lift < 8; lift++) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        column++;
      }
      actor.selectHeldSlot(1);
      for (int r = 1; r <= 3; r++) {
        for (int z = -r + 1; z < r; z++) {
          grass(actor, new BlockPosition(top.x() - r + 1, top.y(), top.z() + z), BlockFace.WEST);
          grass(actor, new BlockPosition(top.x() + r - 1, top.y(), top.z() + z), BlockFace.EAST);
        }
        for (int x = -r + 1; x < r; x++) {
          grass(actor, new BlockPosition(top.x() + x, top.y(), top.z() - r + 1), BlockFace.NORTH);
          grass(actor, new BlockPosition(top.x() + x, top.y(), top.z() + r - 1), BlockFace.SOUTH);
        }
        grass(actor, new BlockPosition(top.x() - r, top.y(), top.z() - r + 1), BlockFace.NORTH);
        grass(actor, new BlockPosition(top.x() - r, top.y(), top.z() + r - 1), BlockFace.SOUTH);
        grass(actor, new BlockPosition(top.x() + r, top.y(), top.z() - r + 1), BlockFace.NORTH);
        grass(actor, new BlockPosition(top.x() + r, top.y(), top.z() + r - 1), BlockFace.SOUTH);
      }
      actor.selectHeldSlot(2);
      spawner = place(actor, top, BlockFace.UP, 52);
      actor.sustainTicks(5);
      actor.close();
      awaitPlayers(server, 0);
      server.save();
    } finally {
      actor.close();
      server.close();
    }
    Thread.sleep(1000L);
    B173SpawnerSeed.chicken(workspace, spawner);
    server = B173DedicatedServer.animals(jar, workspace, port, seed, timeout, 3, true);
    actor = new B173WireClient("127.0.0.1", port, user, timeout);
    try {
      server.boot();
      actor.connect();
      actor.synchronizePose();
      require(
          actor.awaitInventory().occupiedSlots() >= 1, "chicken-egg-set reload inventory drift");
      RemoteMobSpawn chicken = actor.awaitMobSpawn(93);
      require(chicken.legacyType() == 93 && chicken.entityId() != actor.state().entityId(),
          "chicken Packet24 type 93 identity drift");
      RemoteDroppedItem laid = waitLaid(actor);
      RemoteObjectSpawn thrown = throwEgg(actor, chicken, top);
      require(thrown.type() == 62 && thrown.entityId() != chicken.entityId()
              && thrown.entityId() != actor.state().entityId(),
          "chicken-family Packet23 type 62 drift");
      require(thrown.throwerId() == 0 || thrown.throwerId() == actor.state().entityId(),
          "egg thrower drift");
      actor.close();
      awaitPlayers(server, 0);
      server.save();
      require(laid == null || laid.item().legacyId() == 344, "laid Packet21 egg 344 drift");
      String evidence = "column=" + column + ",platform=7x7-48grass,spawner=" + spawner.x() + ":"
          + spawner.y() + ":" + spawner.z()
          + ":52:0,mob=type93,egg=344+packet23-type62,laid=bounded,clients=1,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=raised-7x7-grass-platform+chicken-spawner52+egg344|cause=packet24-type93+bounded-packet21-344+packet15-dir255-egg344|wire=packet24-type93+packet23-type62|oracle=chicken-identity-plus-egg-344|"
          + evidence;
      System.out.println("WORLDLINE_M407_SET=" + evidence);
      System.out.println("WORLDLINE_M407_TRACE=" + trace);
      System.out.println("WORLDLINE_M407_SIGNATURE=" + sha(trace));
    } finally {
      actor.close();
      server.close();
    }
  }
  private static RemoteDroppedItem waitLaid(B173WireClient a) {
    RemoteDroppedItem found = null;
    for (int tick = 0; tick < 40 && found == null; tick++) {
      a.sustainTicks(1);
      found = a.peekDroppedItem(EGG);
    }
    return found;
  }
  private static RemoteObjectSpawn throwEgg(
      B173WireClient a, RemoteMobSpawn chicken, BlockPosition top) {
    int slot = find(a.inventory(), 344);
    require(slot >= 36, "egg 344 absent from hotbar");
    a.selectHeldSlot(slot - 36);
    a.look(0F, 0F);
    a.useSelectedItemInAir();
    RemoteObjectSpawn spawn = a.awaitObjectSpawn(62);
    require(spawn.type() == 62 && spawn.entityId() != chicken.entityId(),
        "thrown egg Packet23 type 62 absent");
    require(Math.abs(spawn.x() - (top.x() + 0.5D)) <= 8D
            && Math.abs(spawn.z() - (top.z() + 0.5D)) <= 8D,
        "thrown egg escaped chicken platform");
    return spawn;
  }
  private static int find(RemoteInventoryView view, int id) {
    for (int slot = 36; slot <= 44; slot++)
      if (!view.slot(slot).empty() && view.slot(slot).item().legacyId() == id)
        return slot;
    return -1;
  }
  private static void grass(B173WireClient a, BlockPosition support, BlockFace face)
      throws Exception {
    place(a, support, face, 2);
  }
  private static BlockPosition foundation(RemoteChunkSnapshot q, int cx, int cz) {
    for (int x = 4; x <= 11; x++)
      for (int z = 4; z <= 11; z++)
        for (int y = 126; y >= 1; y--)
          if (q.blockAt(x, y, z).legacyId() == 3 && water(q.blockAt(x, y + 1, z).legacyId()))
            return new BlockPosition(cx * 16 + x, y, cz * 16 + z);
    throw new IllegalStateException("no deterministic chicken-egg-set foundation");
  }
  private static void require(boolean v, String m) {
    if (!v)
      throw new IllegalStateException(m);
  }
}
