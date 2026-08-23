package worldline.b173server;

import java.util.Random;
import worldline.api.PlayerPose;
import worldline.api.RemoteDroppedItem;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteItemStack;
import worldline.api.RemoteMobDeath;
import worldline.api.RemoteMobMovement;
import worldline.api.RemoteMobSpawn;
import worldline.api.RemoteWorldView;

/** Packet24 type-55 take, diamond-sword kill, and Packet21 slimeball 341 peek. */
public final class B173SlimeballAccess {
  public static final int TYPE = 55, BALL = 341;

  private B173SlimeballAccess() {
  }

  public static boolean slimeChunk(long seed, int cx, int cz) {
    Random random = new Random(seed + (long) (cx * cx * 0x4c1906) + (long) (cx * 0x5ac0db)
            + (long) (cz * cz) * 0x4307a7L + (long) (cz * 0x5f24f)
        ^ 987234911L);
    return random.nextInt(10) == 0;
  }

  public static RemoteWorldView waitChunk(B173WireClient client, int cx, int cz) {
    for (int n = 0; n < 60; n++) {
      client.sustainTicks(10);
      if (client.channel().inbound().cache().snapshot().containsChunk(cx, cz))
        return client.channel().inbound().cache().snapshot();
    }
    return client.awaitRemoteChunk(cx, cz);
  }

  public static RemoteMobSpawn huntNear(
      B173WireClient client, int seconds, double x, double y, double z) {
    for (int n = 0; n < seconds * 20; n++) {
      client.sustainTicks(1);
      RemoteMobSpawn spawn = client.channel().inbound().mobs().take(TYPE);
      if (ok(spawn, client)) {
        double dx = spawn.x() - x, dy = spawn.y() - y, dz = spawn.z() - z;
        if (dx * dx + dy * dy + dz * dz <= 25D)
          return spawn;
      }
    }
    throw new IllegalStateException("nearby Packet24 type 55 slime absent after " + seconds + "s");
  }

  public static RemoteDroppedItem ball(B173WireClient client) {
    for (int n = 1; n <= 2; n++) {
      RemoteDroppedItem item = client.peekDroppedItem(new RemoteItemStack(BALL, n, 0));
      if (item != null)
        return item;
    }
    return null;
  }

  private static boolean ok(RemoteMobSpawn spawn, B173WireClient client) {
    return spawn != null && spawn.legacyType() == TYPE
        && spawn.entityId() != client.state().entityId() && spawn.y() < 16D;
  }

  public static void kill(B173WireClient client, RemoteMobSpawn spawn) {
    int entity = spawn.entityId();
    double x = spawn.x(), y = spawn.y(), z = spawn.z();
    for (int hit = 0; hit < 24; hit++) {
      close(client, x, y, z);
      if (B173ShearsAccess.peekDeath(client, entity) != null)
        break;
      strike(client, entity);
      if (B173ShearsAccess.peekDeath(client, entity) != null)
        break;
      RemoteMobMovement move = client.channel().inbound().mobs().takeMovement(entity);
      if (move != null) {
        x = move.toX();
        y = move.toY();
        z = move.toZ();
      }
    }
    RemoteMobDeath death = client.awaitMobDeath(entity);
    if (death.entityId() != entity || !death.hurtObserved())
      throw new IllegalStateException("slime Packet38 status 3 death drift");
  }

  public static void go(B173WireClient client, double x, double y, double z) {
    for (int n = 0; n < 24; n++) {
      PlayerPose here = client.moveAndObserve(0D, 0D, 0D, 1).resulting();
      double dx = x - here.x(), dy = y - here.y(), dz = z - here.z();
      double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
      if (dist < 1.5D)
        return;
      double scale = Math.min(6D, dist) / dist;
      client.moveAndObserve(dx * scale, dy * scale, dz * scale, 8);
    }
    throw new IllegalStateException("failed to reach slime room");
  }

  private static void strike(B173WireClient client, int entity) {
    heal(client);
    int sword = find(client.inventory(), 276);
    if (sword < 36)
      throw new IllegalStateException("diamond sword lost");
    client.selectHeldSlot(sword - 36);
    client.attackMob(entity);
    client.sustainTicks(10);
    heal(client);
  }

  private static void close(B173WireClient client, double x, double y, double z) {
    for (int n = 0; n < 8; n++) {
      heal(client);
      PlayerPose here = client.moveAndObserve(0D, 0D, 0D, 1).resulting();
      double dx = x - here.x(), dy = y + 0.2D - here.y(), dz = z - here.z();
      double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
      if (dist < 2D)
        return;
      double scale = Math.min(1.5D, dist) / dist;
      client.moveAndObserve(dx * scale, dy * scale, dz * scale, 4);
    }
  }

  private static void heal(B173WireClient client) {
    for (int n = 0; n < 3; n++) {
      int health = client.health();
      if (health == 0)
        throw new IllegalStateException("actor died during slimeball set");
      if (health > 16)
        return;
      int food = find(client.inventory(), 320);
      if (food < 36)
        return;
      client.selectHeldSlot(food - 36);
      client.useSelectedItemInAir();
      client.sustainTicks(5);
    }
  }

  public static int find(RemoteInventoryView view, int id) {
    for (int slot = 36; slot <= 44; slot++)
      if (!view.slot(slot).empty() && view.slot(slot).item().legacyId() == id)
        return slot;
    return -1;
  }
}
