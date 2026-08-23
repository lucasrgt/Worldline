package worldline.smoke.remainingcartbreakb173;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Attacks Packet23 type-10 and type-11 minecarts until Packet21 drops 328 and 328+54. */
public final class RemainingCartBreakSmoke {
  private static final RemoteItemStack CART = new RemoteItemStack(328, 1, 0),
                                       CHEST = new RemoteItemStack(54, 1, 0);
  private RemainingCartBreakSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 7)
      throw new IllegalArgumentException(
          "usage: RemainingCartBreakSmoke server.jar workspace port seed username chunkX chunkZ");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    int cx = Integer.parseInt(a[5]), cz = Integer.parseInt(a[6]);
    require(seed == 17320110707L && user.equals("CartBrk404") && user.length() <= 16,
        "remaining-cart-break identity drift");
    Duration timeout = Duration.ofSeconds(90);
    B173DedicatedServer server =
        new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout);
    BlockPosition top, rail, east1, east2, chestRail;
    int column;
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1, 2, 3, 4},
          new int[] {1, 66, 328, 342, 276}, new int[] {32, 2, 1, 1, 1}, new int[] {0, 0, 0, 0, 0});
      actor.connect();
      actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() == 5, "remaining-cart-break inventory drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      top = foundation(initial, cx, cz);
      column = 0;
      actor.selectHeldSlot(0);
      while (
          water(initial.blockAt(local(top.x(), cx), top.y() + 1, local(top.z(), cz)).legacyId())) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        require(++column <= 15, "water column exceeded remaining-cart-break fixture");
      }
      for (int lift = 0; lift < 8; lift++) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        column++;
      }
      actor.selectHeldSlot(1);
      rail = place(actor, top, BlockFace.UP, 66);
      actor.selectHeldSlot(2);
      actor.useHeldItemOnBlock(rail, BlockFace.UP);
      RemoteObjectSpawn empty = actor.awaitObjectSpawn(10);
      require(empty.entityId() != actor.state().entityId() && empty.type() == 10
              && empty.type() != 11 && empty.throwerId() == 0 && empty.velocityX() == 0
              && empty.velocityY() == 0 && empty.velocityZ() == 0
              && empty.fixedX() == rail.x() * 32 + 16 && empty.fixedY() == rail.y() * 32 + 27
              && empty.fixedZ() == rail.z() * 32 + 16,
          "empty cart packet bounds drift: type=" + empty.type() + ",thrower=" + empty.throwerId()
              + ",fixed=" + empty.fixedX() + ":" + empty.fixedY() + ":" + empty.fixedZ()
              + ",rail=" + rail);
      actor.selectHeldSlot(0);
      east1 = place(actor, top, BlockFace.EAST, 1);
      east2 = place(actor, east1, BlockFace.EAST, 1);
      actor.selectHeldSlot(1);
      chestRail = place(actor, east2, BlockFace.UP, 66);
      actor.selectHeldSlot(3);
      actor.useHeldItemOnBlock(chestRail, BlockFace.UP);
      RemoteObjectSpawn chest = actor.awaitObjectSpawn(11);
      require(chest.entityId() != actor.state().entityId() && chest.entityId() != empty.entityId()
              && chest.type() == 11 && chest.type() != 10 && chest.type() != 12
              && chest.throwerId() == 0 && chest.velocityX() == 0 && chest.velocityY() == 0
              && chest.velocityZ() == 0 && chest.fixedX() == chestRail.x() * 32 + 16
              && chest.fixedY() == chestRail.y() * 32 + 27
              && chest.fixedZ() == chestRail.z() * 32 + 16,
          "chest cart packet bounds drift: type=" + chest.type() + ",thrower=" + chest.throwerId()
              + ",fixed=" + chest.fixedX() + ":" + chest.fixedY() + ":" + chest.fixedZ()
              + ",rail=" + chestRail);
      worldline.test.WorldlineSmokeAwait.observe(actor, 5);
      RemoteDroppedItem emptyDrop = breakCart(actor, empty.entityId(), CART, -1);
      require(emptyDrop.item().equals(CART) && emptyDrop.item().legacyId() == 328,
          "type10 Packet21 328 absent");
      RemoteDroppedItem chestBlock = breakCart(actor, chest.entityId(), CHEST, -1);
      RemoteDroppedItem chestCart = dropAfter(actor, CART, emptyDrop.entityId());
      require(chestBlock.item().equals(CHEST) && chestBlock.item().legacyId() == 54
              && chestCart.item().equals(CART) && chestCart.item().legacyId() == 328
              && chestCart.entityId() != emptyDrop.entityId(),
          "type11 Packet21 328+54 absent");
      actor.close();
      awaitPlayers(server, 0);
      server.save();
      String evidence = "column=" + column + ",rail=" + rail.x() + ":" + rail.y() + ":" + rail.z()
          + ":66:0,cart=type" + empty.type() + "+thrower0+fixed" + empty.fixedX() + ":"
          + empty.fixedY() + ":" + empty.fixedZ()
          + ",emptyDrop=packet21-328,chestRail=" + chestRail.x() + ":" + chestRail.y() + ":"
          + chestRail.z() + ":66:0,chest=type" + chest.type() + "+thrower0+fixed" + chest.fixedX()
          + ":" + chest.fixedY() + ":" + chest.fixedZ()
          + ",chestDrops=packet21-328+packet21-54,sword=276,button=1,clients=1,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=raised-rail66-pair|cause=packet7-attack-type10+packet7-attack-type11|wire=packet23-type10+packet21-328+packet23-type11+packet21-328+packet21-54|oracle=remaining-cart-break-type10-328+type11-328-54|"
          + evidence;
      System.out.println("WORLDLINE_M404_SET=" + evidence);
      System.out.println("WORLDLINE_M404_TRACE=" + trace);
      System.out.println("WORLDLINE_M404_SIGNATURE=" + sha(trace));
    } finally {
      actor.close();
      server.close();
    }
  }
  private static RemoteDroppedItem breakCart(
      B173WireClient a, int entity, RemoteItemStack item, int prior) {
    int sword = find(a.inventory(), 276);
    require(sword >= 36, "diamond sword absent from hotbar");
    a.selectHeldSlot(sword - 36);
    RemoteDroppedItem drop = null;
    for (int hit = 0; hit < 8 && drop == null; hit++) {
      a.attackMob(entity);
      drop = worldline.test.WorldlineSmokeAwait.awaitEntityOrNull(a,
          ()
              -> fresh(a.peekDroppedItem(item), prior),
          value -> value != null, "fresh cart drop", 10);
    }
    require(drop != null && drop.item().equals(item) && drop.item().count() == 1
            && drop.entityId() != prior,
        "cart Packet21 drop absent id=" + item.legacyId());
    return drop;
  }
  private static RemoteDroppedItem dropAfter(B173WireClient a, RemoteItemStack item, int prior) {
    RemoteDroppedItem drop = worldline.test.WorldlineSmokeAwait.awaitEntity(a,
        ()
            -> fresh(a.peekDroppedItem(item), prior),
        value -> value != null, "second cart drop", 80);
    require(drop.item().equals(item) && drop.entityId() != prior,
        "second Packet21 " + item.legacyId() + " absent");
    return drop;
  }
  private static RemoteDroppedItem fresh(RemoteDroppedItem seen, int prior) {
    return seen != null && seen.entityId() != prior ? seen : null;
  }
  private static int find(RemoteInventoryView view, int id) {
    for (int slot = 36; slot <= 44; slot++)
      if (!view.slot(slot).empty() && view.slot(slot).item().legacyId() == id)
        return slot;
    return -1;
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
    throw new IllegalStateException("no deterministic remaining-cart-break foundation");
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
